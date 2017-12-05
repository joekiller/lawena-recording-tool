package vdm.Segments;

import vdm.Factories.SkipAhead;

public class StopSkip extends PlayCommands implements SkipAhead {
    public StopSkip(int id, int safeStart, String skipStop) {
        super(id, "stopskip", "starttick \"" + safeStart + "\"",
            "commands \"" + skipStop + "\"");
    }
}
