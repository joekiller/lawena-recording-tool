package vdm.Ticks;

import util.DemoPreview;

public class Exec extends AbstractExec {
    public static final String Text = "Add Exec";
    public static final String Factory = "PlayCommands";
    public static final String Name = "exec";
    public static final String Template = "exec spec_player";
    public static final String QuitTemplate = "exec quit";

    public Exec(DemoPreview demoPreview, int start, String template) {
        super(demoPreview, start, start, Name, template);
    }

    public String getCommand(int count) {
        return getTemplate();
    }

    @Override
    public String segment(int count) {
        return Tick.segment(count, Factory, Name, "starttick \"" + getStart()
            + "\"", "commands \"" + getTemplate() + "\"");
    }
}
