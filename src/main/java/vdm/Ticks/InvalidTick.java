package vdm.Ticks;

import util.DemoPreview;

public class InvalidTick extends Tick {
    public InvalidTick(DemoPreview demoPreview, int start, int end, String name, String tick_template, String reason) {
        super(demoPreview, start, end, name, tick_template);
        this.valid = false;
        this.reason = reason;
    }
}
