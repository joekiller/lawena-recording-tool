package vdm.Segments;

public class StopDem extends PlayCommands {
    public StopDem(int id, int previousEndTick) {
        super(id, "stopdem",  (previousEndTick + 1), "stopdemo");
    }
}
