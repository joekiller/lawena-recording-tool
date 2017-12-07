package vdm.Ticks;

import util.DemoPreview;
import vdm.Segments.Segment;

abstract public class Tick {



    public String getName() {
        return name;
    }
    public static final int MIN_START = 3;

    private final String name;

    private final String tickTemplate;
    private final String demoName;

    private int start;
    private int end;
    boolean valid;
    String reason;
    private final DemoPreview demoPreview;

    public boolean isValid() {
        return valid;
    }

    public String getReason() {
        return reason;
    }

    public Tick(DemoPreview demoPreview, int start, int end, String name, String tickTemplate) {
        this.demoName = demoPreview.getFileName();
        if (start < MIN_START)
            start = MIN_START;
        this.start = start;
        if (end > demoPreview.getMaxTick())
            end = demoPreview.getMaxTick();
        this.end = end;
        this.name = name;
        this.tickTemplate = tickTemplate;
        this.valid = true;
        this.demoPreview = demoPreview;
    }

    public String getDemoName() {
        return demoName;
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

    public String getTemplate() {
        return tickTemplate;
    }

    @Override
    public String toString() {
        return demoName + ": " + start + "-" + end;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((demoName == null) ? 0 : demoName.hashCode());
        result = prime * result + end;
        result = prime * result + start;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Tick other = (Tick) obj;
        if (demoName == null) {
            if (other.demoName != null) {
                return false;
            }
        } else if (!demoName.equals(other.demoName)) {
            return false;
        }
        if (end != other.end) {
            return false;
        }
        if (start != other.start) {
            return false;
        }
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        return true;
    }

    public DemoPreview getDemoPreview() {
        return demoPreview;
    }

    abstract public Segment getSegment(int count);
}
