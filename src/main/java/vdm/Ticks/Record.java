package vdm.Ticks;

import util.DemoPreview;

public class Record extends Tick {
    public static final String Name = "record";
    public static final String Template = "N/A";
    public static final String Text = "Add Record";

    public Record(DemoPreview demoPreview, int start, int end) throws NumberFormatException {
        super(demoPreview, start, end, Name, Template, "startrecording");
        if (end == demoPreview.getMaxTick())
            end = demoPreview.getMaxTick() - 1;
        if (start >= end) {
            throw new NumberFormatException(String.format("end tick (%d) must be greater than start tick (%d)", end, start));
        }
    }
}
