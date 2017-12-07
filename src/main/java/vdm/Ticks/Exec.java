package vdm.Ticks;

import util.DemoPreview;
import vdm.Segments.PlayCommands;
import vdm.Segments.Segment;

public class Exec extends Tick {
    public static final String Text = "Add Exec";
    public static final String Name = "exec";
    public static final String Template = "exec spec_player";
    public static final String QuitTemplate = "exec quit";

    public Exec(DemoPreview demoPreview, int start, String template) {
        super(demoPreview, start, start, Name, template);
    }

    public Segment getSegment(int count) {
        return new PlayCommands(count, Name, getStart(), getTemplate());
    }
}
