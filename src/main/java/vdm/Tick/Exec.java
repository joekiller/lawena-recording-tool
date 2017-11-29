package vdm.Tick;

import util.DemoPreview;

public class Exec extends AbstractExec {
    public static final String Text = "Add Exec";
    public static final String Segment = "exec";
    public static final String Template = "exec spec_player";
    public static final String QuitTemplate = "exec quit";

    public Exec(DemoPreview demoPreview, int start, String template) {
        super(demoPreview, start, start, Segment, template);
    }

    public String getCommand(int count) {
        return getTemplate();
    }
}
