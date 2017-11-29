package vdm.Tick;

import util.DemoPreview;

import java.io.File;

public class TickFactory {

    public static Tick makeTick(File demoFile, String demoname, int start, int end, String segment, DemoPreview demoPreview) {
        return makeTick(demoFile, demoname, start, end, segment, null, demoPreview);
    }

    public static Tick makeTick(File demoFile, String demoname, int start, int end, String segment, String template, DemoPreview demoPreview) {
        Tick t;
        try {
            switch (segment) {
                case Record.Segment:
                    t = new Record(demoFile, demoname, start, end, demoPreview);
                    break;
                case ExecRecord.Segment:
                    t = new ExecRecord(demoFile, demoname, start, end, (template == null || template.equals(Record.Template)) ? ExecRecord.Template : template, demoPreview);
                    break;
                case Exec.Segment:
                    t = new Exec(demoFile, demoname, start, (template == null || template.equals(Record.Template)) ? Exec.Template : template, demoPreview);
                    break;
                default:
                    t = new InvalidTick(demoFile, demoname, start, end, segment, template, "Unknown Segment Type", demoPreview);
            }
        } catch (NumberFormatException e) {
            t = new InvalidTick(demoFile, demoname, start, end, segment, template, e.getMessage(), demoPreview);
        }
        return t;
    }
}
