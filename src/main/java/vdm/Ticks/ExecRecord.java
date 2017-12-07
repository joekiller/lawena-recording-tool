package vdm.Ticks;

import util.DemoPreview;
import util.Util;
import vdm.Segments.PlayCommands;
import vdm.Segments.Segment;
import vdm.Segments.StartRec;

public class ExecRecord extends Tick {
    public static final String Name = "exec_record";
    public static final String Template = "mirv_camimport start \"{{BVH_PATH}}\"";
    public static final String Text = "Add Exec + Record";

    public ExecRecord(DemoPreview demoPreview, int start, int end, String template) throws NumberFormatException {
        super(demoPreview, start, end, Name, template);
        if (end == demoPreview.getMaxTick())
            end = demoPreview.getMaxTick() - 1;
        if (start >= end) {
            throw new NumberFormatException(String.format("end tick (%d) must be greater than start tick (%d)", end, start));
        }
    }

    public Segment getSegment(int count) {
        return new StartRec(count, getStart(), "exec " + Util.stripFilenameExtension(this.getDemoPreview().getFileName()) + "_" + count + "; startrecording");
    }
}
