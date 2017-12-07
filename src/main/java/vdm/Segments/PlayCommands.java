package vdm.Segments;

public class PlayCommands extends Segment {

    public PlayCommands(int id, String name, int startTick, String commands) {
        super(id, "PlayCommands", name,
            "starttick \"" + startTick + "\"",
            "commands \"" + commands + "\"");
    }
}
