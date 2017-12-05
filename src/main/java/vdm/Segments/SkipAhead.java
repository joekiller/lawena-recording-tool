package vdm.Segments;

public class SkipAhead extends Segment {
    public SkipAhead(int id, int safeStart, int previousEndTick) {
        super(id, factory, "skip", "starttick \"" + (previousEndTick + 1)
            + "\"", "skiptotick \"" + safeStart + "\"");
    }
}
