package vdm.Segments;

import vdm.Ticks.Tick;

public abstract class Segment {
    private static final String n = System.getProperty("line.separator");

    public String getName() {
        return name;
    }

    private final int id;
    private final String factory, name;
    private final String[] args;

    public Segment(int id, String factory, String name, String... args) {
        this.id = id;
        this.factory = factory;
        this.name = name;
        this.args = args;
    }

    public Segment(int id, Tick t) {
        this.id = id;
        this.factory = t.getFactory();
        this.name = t.getName();
        this.args = new String[]{t.getTemplate()};
    };

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("\t\"").append(id).append("\"").append(n);
        sb.append("\t{").append(n);
        sb.append("\t\tfactory \"").append(factory).append("\"").append(n);
        sb.append("\t\tname \"").append(name).append("\"").append(n);
        for (String arg : args) {
            sb.append("\t\t").append(arg).append(n);
        }
        sb.append("\t}");
        return sb.toString();
    }
}
