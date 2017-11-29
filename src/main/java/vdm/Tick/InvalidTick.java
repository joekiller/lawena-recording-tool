package vdm.Tick;

import util.DemoPreview;

public class InvalidTick extends Tick {
    public InvalidTick(DemoPreview demoPreview, int start, int end, String segment, String tick_template, String reason) {
        super(demoPreview, start, end, segment, tick_template);
        this.valid = false;
        this.reason = reason;
    }
}
