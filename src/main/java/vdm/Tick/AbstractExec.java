package vdm.Tick;

import util.DemoPreview;

public abstract class AbstractExec extends Tick {

    public AbstractExec(DemoPreview demoPreview, int start, int end, String segment, String template) {
        super(demoPreview, start, end, segment, template);
    }

    public abstract String getCommand(int count);
}
