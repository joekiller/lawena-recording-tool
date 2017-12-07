package vdm.Segments;

public class NextDem extends PlayCommands {
    public NextDem(int id, int previousEndTick, String nextDemo) {
        super(id, "nextdem",  (previousEndTick + 1), "playdemo " + nextDemo);
    }
}
