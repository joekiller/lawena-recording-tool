package vdm.Tick;

import util.DemoPreview;
import util.Util;

public class ExecRecord extends AbstractExec {
    public static final String Segment = "exec_record";
    public static final String Template = "mirv_camimport start \"{{BVH_PATH}}\"";
    public static final String Text = "Add Exec + Record";

    public ExecRecord(DemoPreview demoPreview, int start, int end, String template) throws NumberFormatException {
        super(demoPreview, start, end, Segment, template);
        if (end == demoPreview.getMaxTick())
            end = demoPreview.getMaxTick() - 1;
        if (start >= end) {
            throw new NumberFormatException(String.format("end tick (%d) must be greater than start tick (%d)", end, start));
        }
    }

    public String getCommand(int count) {
        return "exec " + Util.stripFilenameExtension(this.getDemoPreview().getFileName()) + "_" + count + "; startrecording";
    }
}
