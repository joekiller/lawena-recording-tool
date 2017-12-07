package vdm.Ticks;

import util.DemoPreview;
import vdm.Segments.PlayCommands;
import vdm.Segments.Segment;

public class InvalidTick extends Tick {
    public InvalidTick(DemoPreview demoPreview, int start, int end, String name, String tick_template, String reason) {
        super(demoPreview, start, end, name, tick_template);
        this.valid = false;
        this.reason = reason;
    }

    @Override
    public Segment getSegment(int count) {
        return new PlayCommands(count, "invalidTick", getStart(), String.format("echo invalidTick at %d", count));
    }
}
