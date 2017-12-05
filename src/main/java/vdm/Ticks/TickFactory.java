package vdm.Ticks;

import util.DemoPreview;

public class TickFactory {

    public static Tick makeTick(DemoPreview demoPreview, int start, int end, String segment) {
        return makeTick(demoPreview, start, end, segment, null);
    }

    public static Tick makeTick(DemoPreview demoPreview, int start, int end, String segment, String template) {
        Tick t;
        try {
            switch (segment) {
                case Record.Name:
                    t = new Record(demoPreview, start, end);
                    break;
                case ExecRecord.Name:
                    t = new ExecRecord(demoPreview, start, end, (template == null || template.equals(Record.Template)) ? ExecRecord.Template : template);
                    break;
                case Exec.Name:
                    t = new Exec(demoPreview, start, (template == null || template.equals(Record.Template)) ? Exec.Template : template);
                    break;
                default:
                    t = new InvalidTick(demoPreview, start, end, segment, template, "Unknown Name Type");
            }
        } catch (NumberFormatException e) {
            t = new InvalidTick(demoPreview, start, end, segment, template, e.getMessage());
        }
        return t;
    }
}
