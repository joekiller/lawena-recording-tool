package vdm.Segments;

public class StartSkip extends PlayCommands {
    public StartSkip(int id, int previousEndTick, String skipStart) {
        super(id, "startskip", "starttick \""
            + (previousEndTick + 1) + "\"", "commands \"" + skipStart + "\"");
    }
}
