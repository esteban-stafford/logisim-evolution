package es.unican.atc; 

import java.awt.Font;
import java.awt.Color;
import java.awt.Graphics;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static com.cburch.logisim.std.Strings.S;

import com.cburch.logisim.data.Attribute;
import com.cburch.logisim.data.AttributeOption;
import com.cburch.logisim.data.Attributes;
import com.cburch.logisim.data.Bounds;
import com.cburch.logisim.data.BitWidth;
import com.cburch.logisim.data.Direction;
import com.cburch.logisim.data.Value;
import com.cburch.logisim.instance.InstanceFactory;
import com.cburch.logisim.instance.Instance;
import com.cburch.logisim.instance.InstancePainter;
import com.cburch.logisim.instance.InstanceState;
import com.cburch.logisim.instance.Port;
import com.cburch.logisim.instance.StdAttr;
import com.cburch.logisim.util.GraphicsUtil;
import com.cburch.logisim.util.StringUtil;


class GenericInDevice extends InstanceFactory{

   public static final int ADDR = 0;
   public static final int DATA_REC = 1;
   public static final int DATA_SEND = 2;
   public static final int WRITE_ENABLE = 3;

   public static final int INTERRUPT = 4;
   public static final int INTERRUPT_SERVED = 5;

   public static final int CLEAR = 6;
   public static final int CLOCK = 7;

   private static String[] labels = { "a", "d_r", "d_s", "we", "i", "i_s", "clr", "clk"};

   public enum State {
        IDLE,
        SENSING,
        MEASUREMENT_READY
    }

   public enum AddrValue {
        CTRL_REG,
        DATA_REG,
        STATUS_REG
    }

    private State currentState;
    private long num_samples;
    private long taken_samples;
    private long current_cycle;
    private Random random;
    private final long RANDOM_SEED = 12345L; // Seed fijo para reproducibilidad
    private Value lastClock;


    public static final Attribute<Integer> SAMPLE_LATENCY =
      Attributes.forInteger("sampleLatency", S.getter("sampleLatency"));
    
   GenericInDevice() {
      super("GenericInDevice", new SimpleStringGetter("Generic In Device"));
      setAttributes(new Attribute[] {
         StdAttr.TRIGGER,
         SAMPLE_LATENCY,
      }, new Object[] {
         StdAttr.TRIG_RISING,
         25
      });

      int spacing = 10;
      int width = 16 * spacing;
      int height = 6 * spacing;
      //int address_width = (int)(Math.log(NUM_REGISTERS)/Math.log(2));
      
      Bounds bounds = Bounds.create(-width/2, -height/2, width, height);
      setOffsetBounds(bounds);
      int x0 = bounds.getX();
      int x1 = x0 + bounds.getWidth();
      int y0 = bounds.getY();
      int y1 = y0 + bounds.getHeight();
      Port[] ports = new Port[labels.length];
      ports[WRITE_ENABLE]  = new Port(x0 + 3 * spacing, y0, Port.INPUT, 1);
      ports[ADDR]  = new Port(x0 + 6 * spacing, y0, Port.INPUT, 32);
      ports[DATA_REC]  = new Port(x0 + 9 * spacing, y0, Port.INPUT, 32);
      ports[DATA_SEND] = new Port(x0 + 12 *spacing, y0, Port.OUTPUT, 32);

      ports[INTERRUPT] = new Port(x0 + 3*spacing, y1, Port.OUTPUT, 1);
      ports[INTERRUPT_SERVED] = new Port(x0 + 12*spacing, y1, Port.INPUT, 1);
      
      ports[CLEAR] = new Port(x0, y0 + 2*spacing, Port.OUTPUT, 1);
      ports[CLOCK] = new Port(x0, y0 + 4*spacing, Port.INPUT, 1);

      setPorts(ports);

      reset();
   }

   private void reset()
   {
      this.currentState=State.IDLE;
      this.num_samples = 0;
      this.taken_samples = 0;
      this.current_cycle = 0;
      this.random = new Random(RANDOM_SEED);
      this.lastClock=null;
   }

   public State getCurrentState() {
        return currentState;
   }

   @Override
   public void propagate(InstanceState state) {
     AttributeOption triggerType = state.getAttributeValue(StdAttr.TRIGGER);
     BitWidth WIDTH = BitWidth.create(32);
    
     final var attrs = state.getAttributeSet();

     Value addrValue = state.getPortValue(ADDR);
     Value writeEnable = state.getPortValue(WRITE_ENABLE); // 0 para indicar que es WRITE_ENABLE
     Value dataRec = state.getPortValue(DATA_REC); // true para indicar que es DATA_REC
     Value clk = state.getPortValue(CLOCK); // true para indicar que es DATA_REC

     if (state.getPortValue(CLEAR) == Value.TRUE) {
          reset();
          state.setPort(INTERRUPT, Value.createKnown(BitWidth.create(1),0), 1);
          state.setPort(DATA_SEND, Value.createKnown(BitWidth.create(1),0), 1);
          return;
     }

//     state.setPort(INTERRUPT, Value.createUnknown(WIDTH), 1);
//     state.setPort(DATA_SEND, Value.createUnknown(WIDTH), 1);

     switch (currentState) {
         case IDLE:
             handleIdleState(state, addrValue, writeEnable, dataRec);
             break;
         case SENSING:
             handleSensingState(state, addrValue, writeEnable, dataRec, clk);
             break;
         case MEASUREMENT_READY:
             handleMeasurementReadyState(state, addrValue, writeEnable, dataRec);
             break;
     }

     // Lógica para lectura de STATUS_REG (aplica en cualquier estado)
      if (addrValue.toLongValue() == AddrValue.STATUS_REG.ordinal() && writeEnable.toLongValue() == 0) { // Lectura de STATUS_REG
         state.setPort(DATA_SEND, Value.createKnown(BitWidth.create(32), currentState.ordinal()), 1);
         System.out.println("Chequeando Estado: "+currentState.ordinal());
      }
   }


   private void handleIdleState(InstanceState state, Value addrValue, Value writeEnable, Value dataRec) {
        if (writeEnable.toLongValue() == 1) { // Escritura
            if (addrValue.toLongValue() == AddrValue.DATA_REG.ordinal()) {
                // Si el estado es IDLE y se recibe una escritura en DATA_REG,
                // el valor recibido será guardado en num_samples.
                this.num_samples = dataRec.toLongValue();
                System.out.println("IDLE: num_samples fijado a " + num_samples);
                // No hay cambio de estado.
            } else if (addrValue.toLongValue() == AddrValue.CTRL_REG.ordinal() && dataRec.toLongValue() == 1) {
                // Si el estado es IDLE y se recibe una escritura en CTRL_REG y el valor es 1,
                // se pasa a estado SENSING y se fija taken_samples a 0 y current_cycle a 0.
                this.currentState = State.SENSING;
                this.taken_samples = 0;
                this.current_cycle = 0;
                System.out.println("IDLE -> SENSING: Inicializando taken_samples y current_cycle.");
            }
        }
    }

    private void handleSensingState(InstanceState state, Value addrValue, Value writeEnable, Value dataRec, Value clk) {

        if (updateClock(clk, state.getAttributeValue(StdAttr.TRIGGER))) 
        {
             // Cada vez que se llama a propagate se incrementa current_cycle en 1.
            this.current_cycle++;
            System.out.println("SENSING: current_cycle incrementado a " + current_cycle);

            // Cuando current_cycle alcance el valor de latency_per_sample,
            // se incrementa taken_samples y se reinicia current_cycle.
            final var attrs = state.getAttributeSet();
            long sample_latency=attrs.getValue(SAMPLE_LATENCY);
            if (this.current_cycle >= sample_latency) {
                this.taken_samples++;
                this.current_cycle = 0;
                System.out.println("SENSING: taken_samples incrementado a " + taken_samples + ", current_cycle reiniciado.");

                // Esto se repite hasta que taken_samples sea igual a num_samples,
                // momento en el cual se pasa al estado MEASUREMENT_READY.
                if (this.taken_samples >= this.num_samples && this.num_samples > 0) { // num_samples > 0 para evitar transiciones si no se ha configurado
                    this.currentState = State.MEASUREMENT_READY;
                    state.setPort(INTERRUPT, Value.createKnown(BitWidth.create(1),1), 1);
                    System.out.println("SENSING -> MEASUREMENT_READY: Todas las muestras tomadas.");
                }
            }
        }
    }

    private void handleMeasurementReadyState(InstanceState state, Value addrValue, Value writeEnable, Value dataRec) {
        if (writeEnable.toLongValue() == 0 && addrValue.toLongValue() == AddrValue.DATA_REG.ordinal()) { // Lectura de DATA_REG
            // Una lectura de DATA_REG debe llamar a state.setPort(DATA_SEND, r, 1),
            // donde r es un número aleatorio con un seed fijo.
            int randomValue = random.nextInt(10000); // Ejemplo: número aleatorio entre 0 y 99
            state.setPort(DATA_SEND, Value.createKnown(BitWidth.create(32), randomValue), 1);
            System.out.println("MEASUREMENT_READY: Leyendo DATA_REG. Enviando valor aleatorio: " + randomValue);

            // Esto también decrementa num_samples.
            this.num_samples--;
            System.out.println("MEASUREMENT_READY: num_samples decrementado a " + num_samples);

            // Cuando num_samples llega a cero, se pasa a estado IDLE.
            if (this.num_samples == 0) {
                this.currentState = State.IDLE;
                System.out.println("MEASUREMENT_READY -> IDLE: num_samples ha llegado a cero.");
                state.setPort(INTERRUPT, Value.createKnown(BitWidth.create(1), 0), 1);
            }
            else{
               state.setPort(INTERRUPT, Value.createKnown(BitWidth.create(32), 1), 1);
            }
        }
        else{
           state.setPort(INTERRUPT, Value.createKnown(BitWidth.create(32), 1), 1);
        }
    }
   
   @Override
   public void paintInstance(InstancePainter painter) {
      Bounds bounds = painter.getBounds();
      painter.drawRectangle(bounds, "");
      painter.drawPort(ADDR, labels[ADDR], Direction.NORTH);
      painter.drawPort(DATA_REC, labels[DATA_REC], Direction.NORTH);
      painter.drawPort(WRITE_ENABLE, labels[WRITE_ENABLE], Direction.NORTH);
      painter.drawPort(DATA_SEND, labels[DATA_SEND], Direction.NORTH);

      painter.drawPort(INTERRUPT, labels[INTERRUPT], Direction.SOUTH);
      painter.drawPort(INTERRUPT_SERVED, labels[INTERRUPT_SERVED], Direction.SOUTH);

      painter.drawPort(CLEAR, labels[CLEAR], Direction.WEST);
      painter.drawPort(CLOCK, labels[CLOCK], Direction.WEST);

      Graphics g = painter.getGraphics();

      Font font = g.getFont().deriveFont(7f);

      if (!painter.getShowState()) {
         return;
      }

   }
    
   public boolean updateClock(Value newClock, Object trigger) {
        Value oldClock = lastClock;
        lastClock = newClock;
        if (trigger == null || trigger == StdAttr.TRIG_RISING) {
            return oldClock == Value.FALSE && newClock == Value.TRUE;
        }
        else if (trigger == StdAttr.TRIG_FALLING) {
            return oldClock == Value.TRUE && newClock == Value.FALSE;
        }
        else if (trigger == StdAttr.TRIG_HIGH) {
            return newClock == Value.TRUE;
        }
        else if (trigger == StdAttr.TRIG_LOW) {
            return newClock == Value.FALSE;
        }
        else {
            return oldClock == Value.FALSE && newClock == Value.TRUE;
        }
   }
  
}
