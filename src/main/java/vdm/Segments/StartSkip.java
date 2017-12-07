package vdm.Segments;

public class StartSkip extends PlayCommands {
    public StartSkip(int id, int previousEndTick, String skipStart) {
        super(id, "startskip",  (previousEndTick + 1),  skipStart);
    }
}
