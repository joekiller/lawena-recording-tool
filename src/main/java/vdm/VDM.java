package vdm;

import vdm.Ticks.Tick;

import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Logger;

public class VDM {

    private static final Logger log = Logger.getLogger(VDM.class.toString());
    public String getDemoName() {
        return demoName;
    }

    public int getPadding() {
        return padding;
    }

    public String getSkipStartCommand() {
        return skipStartCommand;
    }

    public String getSkipStopCommand() {
        return skipStopCommand;
    }

    private String demoName;
    private final int padding;
    private final String skipStartCommand;
    private final String skipStopCommand;
    private ArrayList<Tick> ticks;

    public SkipMode getSkipMode() {
        return skipMode;
    }

    private SkipMode skipMode;

    public VDM(String demoName, int padding, String skipStartCommand, String skipStopCommand, String skipMode) {
        this.demoName = demoName;
        this.padding = padding;
        this.skipStartCommand = skipStartCommand;
        this.skipStopCommand = skipStopCommand;
        try {
            this.skipMode = SkipMode.valueOf(skipMode);
        } catch (IllegalArgumentException ex) {
            log.warning("Invalid value detected for skip mode: " + skipMode);
        }
        ticks = new ArrayList<>();
    }

    public void add(Tick t){
        ticks.add(t);
    }

    public ArrayList<Tick> getTicks() {
        return ticks;
    }
}
