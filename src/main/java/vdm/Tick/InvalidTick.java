package vdm.Tick;

import util.DemoPreview;

import java.io.File;

public class InvalidTick extends Tick {
    public InvalidTick(File demoFile, String demoname, int start, int end, String segment, String tick_template, String reason, DemoPreview demoPreview) {
        super(demoFile, demoname, start, end, segment, tick_template, demoPreview);
        this.valid = false;
        this.reason = reason;
    }
}
