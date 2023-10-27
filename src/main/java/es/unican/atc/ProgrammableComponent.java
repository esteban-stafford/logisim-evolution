package es.unican.atc;

import com.cburch.logisim.instance.InstanceFactory;
import com.cburch.logisim.instance.InstancePainter;
import com.cburch.logisim.instance.InstanceState;

public class ProgrammableComponent extends InstanceFactory
{

    protected ProgrammableComponent()
    {
        //super("RegisterFile16", new SimpleStringGetter("16x32 Register File"));
        super("ProgrammableComponent");
    }

    @Override
    public void paintInstance(InstancePainter painter) {
        painter.drawRectangle(painter.getBounds(), "");
    }

    @Override
    public void propagate(InstanceState state) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'propagate'");
    }    
}