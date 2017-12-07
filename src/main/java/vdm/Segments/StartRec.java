package vdm.Segments;

public class StartRec extends Segment {
    public StartRec(int id, int startTick, String command) {
        super(id, "PlayCommands", "startrec",
            "starttick \"" + startTick + "\"",
            "commands \"" + command + "\"");
    }
}
