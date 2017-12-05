package vdm.Ticks;

import util.DemoPreview;

public abstract class AbstractExec extends Tick {

    public AbstractExec(DemoPreview demoPreview, int start, int end, String name, String template) {
        super(demoPreview, start, end, name, template);
    }

    public abstract String getCommand(int count);
}
