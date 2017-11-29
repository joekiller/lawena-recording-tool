package vdm.Tick;

import util.DemoPreview;

import java.io.File;

public abstract class AbstractExec extends Tick {

    public AbstractExec(File demoFile, String demoName, int start, int end, String segment, String template, DemoPreview demoPreview) {
        super(demoFile, demoName, start, end, segment, template, demoPreview);
    }

    public abstract String getCommand(int count);
}
