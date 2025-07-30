package es.unican.atc;

import java.awt.Color;
import java.awt.Graphics;

import com.cburch.logisim.data.BitWidth;
import com.cburch.logisim.data.Bounds;
import com.cburch.logisim.data.Direction;
import com.cburch.logisim.data.Location;
import com.cburch.logisim.data.Value;
import com.cburch.logisim.instance.InstanceFactory;
import com.cburch.logisim.instance.InstancePainter;
import com.cburch.logisim.instance.InstanceState;
import com.cburch.logisim.instance.Port;
import com.cburch.logisim.util.GraphicsUtil;
import java.awt.Graphics2D;

public class RiscvSE_J extends InstanceFactory {
   int xp[], yp[];

   public static final int Imm = 0;  // Input Immediate (left)
   public static final int Mode = 1; // Control Signal (top)
   public static final int SignExtImm = 2; // Output (right)

   public RiscvSE_J() {
      super("RiscvSE_J");
      int width = 60;
      int height = 40;
      Bounds bounds = Bounds.create(-width/2, -height/2, width, height);
      setOffsetBounds(bounds);
      int x0 = bounds.getX();
      int x1 = x0 + bounds.getWidth();
      int y0 = bounds.getY();
      int y1 = y0 + bounds.getHeight();

      xp = new int[] { x0,            x0, x1, x1, x0 };
      yp = new int[] { y0 + height/2, y1, y1, y0, y0 + height/2 };

      setPorts(new Port[]{
         new Port(x0,           y0 + 3*height/4, Port.INPUT, 25), // Imm
             new Port(x0 + width/2, y0 + height/4,   Port.INPUT, 3), // Mode
             new Port(x1,           y0 + height/2,   Port.OUTPUT, 32) // SignExtImm
      });
   }


   @Override
   public void propagate(InstanceState state) {
      // Get the immediate and mode values
      long imm = state.getPortValue(Imm).toLongValue();  // Fetch opcode (bits 31:7)
      int mode = (int) state.getPortValue(Mode).toLongValue();  // Mode determines the type of instruction

      long signExtImm = 0;

      switch (mode) {
         case 0x0:  // I-type instruction
            // Immediate is in bits [31:20] of the instruction (contained within bits [31:7] of Imm)
            signExtImm = (imm >> 13) & 0xFFF;  // Extract bits 31:20 from Imm (31:7)
            if ((signExtImm & 0x800) != 0) {  // Sign extend if bit 11 is set
               signExtImm |= 0xFFFFF000;
            }
            break;

         case 0x1:  // S-type instruction
            // Immediate is formed from bits [31:25] and [11:7] (contained within bits [31:7] of Imm)
            signExtImm = ((imm >> 18) & 0x7F) << 5 | (imm & 0x1F);  // Extract imm[11:5] and imm[4:0]
            if ((signExtImm & 0x800) != 0) {  // Sign extend
               signExtImm |= 0xFFFFF000;
            }
            break;

            // TODO  Write a program that tests sign extension in S-type and I-type instructions

         case 0x2:  // B-type instruction
            // Immediate is formed from bits [31], [7], [30:25], and [11:8] (contained within bits [31:7] of Imm)
            // Immediate is formed from bits [24], [0], [23:18], and [04:1]
            //                                 11   10    9             0
            signExtImm = ((imm >> 24) & 0x1) << 12 | (imm & 0x1) << 11 |
               ((imm >> 18) & 0x3F) << 5 | ((imm >> 1) & 0xF) << 1;  // Extract immediate fields for B-type
            if ((signExtImm & 0x1000) != 0) {  // Sign extend
                signExtImm |= 0xFFFFE000;
            }
            break;

         case 0x3:  // U-type instruction
            // Immediate is in bits [31:12] of the instruction (contained within bits [31:7] of Imm)
            signExtImm = (imm >> 5) & 0xFFFFF;  // Use bits 31:12 directly
            signExtImm <<= 12;  // Left shift to place in upper 20 bits
            break;

         case 0x4: // J-type Imm formed bi bits [31], [19:12], [20] and `[30:21]
                   //                           [24], [12:5],  [13] and  [23:14]
            long bit_31     = (imm >> 24) & 1;      
            long bits_19_12 = (imm >> 5) & 0xFF;   
            long bit_20     = (imm >> 13) & 1;      
            long bits_30_21  = (imm >> 14) & 0x3FF; 
           
            System.out.println(Long.toBinaryString(imm));
            System.out.println(Long.toBinaryString(bit_31));
            System.out.println(Long.toBinaryString(bits_19_12));
            System.out.println(Long.toBinaryString(bit_20));
            System.out.println(Long.toBinaryString(bits_30_21));

            // Arrange them in order: [20], [10:1], [11], [19:12]
            signExtImm= bits_30_21 << 1|    // Bits 19:12 at position 0
                       (bit_20 << 11) |        // Bit 11 at position 8
                       (bits_19_12 << 12) |     // Bits 10:1 at position 9
                       (bit_31 << 20);        // Bit 20 at position 19
            
            if ((signExtImm & 0x100000) != 0) {  // Sign extend
                signExtImm |= 0xFFE00000;
                System.out.println("Extendiendo!");
            }
            break;
      }

      // Set the output port with the sign-extended immediate value
      Value signExtImmValue = Value.createKnown(BitWidth.create(32), signExtImm);
      state.setPort(SignExtImm, signExtImmValue, 32);
   }

   @Override
   public void paintInstance(InstancePainter painter) {
      Graphics2D g = (Graphics2D) painter.getGraphics().create();
      Location loc = painter.getLocation();
      g.translate(loc.getX(), loc.getY());
      GraphicsUtil.switchToWidth(g, 2);
      g.drawPolygon(xp, yp, xp.length);
      g.dispose();
      painter.drawPort(Imm, "", Direction.EAST);
      painter.drawPort(Mode, "", Direction.NORTH);
      painter.drawPort(SignExtImm, "", Direction.WEST);
   }

   @Override
   public void paintIcon(InstancePainter painter) {
      Graphics g = painter.getGraphics();
      g.setColor(Color.BLACK);
      int ixp[] = new int[xp.length];
      int iyp[] = new int[yp.length];
      for (int i = 0; i < xp.length; i++) {
         ixp[i] = xp[i] * 15 / 60;
         iyp[i] = yp[i] * 15 / 60;
      }
      g.drawPolygon(ixp, iyp, ixp.length);
   }
}
