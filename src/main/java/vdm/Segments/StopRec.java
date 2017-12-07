package vdm.Segments;

public class StopRec extends Segment {
    public StopRec(int id, int endTick) {
        super(id, "PlayCommands", "startrec",
            "starttick \"" + endTick + "\"",
            "commands \"stoprecording\"");
    }
}
