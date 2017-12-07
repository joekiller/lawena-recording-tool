package vdm;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import lwrt.SettingsManager;
import lwrt.SettingsManager.Key;
import util.Util;
import vdm.Segments.*;
import vdm.Ticks.Exec;
import vdm.Ticks.Record;
import vdm.Ticks.Tick;

import java.io.IOException;
import java.io.StringReader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import static util.Util.n;

class VDMGenerator {

    private static final Logger log = Logger.getLogger("lawena");

    private List<Tick> ticklist;
    private SettingsManager cfg;

    public VDMGenerator(List<Tick> ticklist, SettingsManager cfg) {
        this.ticklist = ticklist;
        this.cfg = cfg;
    }

    public List<Path> generate() throws IOException {
        List<Path> paths = new ArrayList<>();
        Map<String, VDM> vdms = new LinkedHashMap<>();
        Map<String, String> peeknext = new LinkedHashMap<>();
        VDM previous = null;
        VDM current = null;
        for (Tick tick : ticklist) {
            if(previous != null || current == null) {
                if(current == null || !previous.getDemoName().equals(tick.getDemoName())) {
                    current = vdms.computeIfAbsent(tick.getDemoName(),
                        k -> new VDM(tick.getDemoName(),
                            cfg.getInt(Key.VdmTickPadding),
                            cfg.getString(Key.VdmSkipStartCommand),
                            cfg.getString(Key.VdmSkipStopCommand),
                            cfg.getString(Key.VdmSkipMode)));
                }
            }
            current.add(tick);
            if (previous != null) {
                if (!peeknext.containsKey(previous.getDemoName()) && !previous.getDemoName().equals(tick.getDemoName())) {
                    peeknext.put(previous.getDemoName(), tick.getDemoName());
                }
            }
            previous = current;
        }
        for (Entry<String, VDM> e : vdms.entrySet()) {
            CFGFactory cfgFactory = new CFGFactory(cfg.getTfPath(), cfg.getMoviePath(), Paths.get(""));
            String demo = e.getKey();
            VDM vdm = e.getValue();
            log.finer("Creating VDM file for demo: " + demo);
            List<String> lines = new ArrayList<>();
            lines.add("demoactions" + n + "{");
            int segmentCount = 1;
            int previousEndTick = Tick.MIN_START;
            for (Tick tick : e.getValue().getTicks()) {
                int safeStart = Math.max(Tick.MIN_START, tick.getStart() - vdm.getPadding());
                // no need to skip if the next getSegment is closer than the padding length
                boolean needsSkip = previousEndTick + 1 < safeStart;
                if (needsSkip) {
                    if (vdm.getSkipMode() == SkipMode.DEMO_TIMESCALE) {
                        lines.add(new StartSkip(segmentCount++, previousEndTick, vdm.getSkipStartCommand()).toString());
                        lines.add(new StopSkip(segmentCount++, safeStart, vdm.getSkipStopCommand()).toString());
                    } else if (vdm.getSkipMode() == SkipMode.SKIP_AHEAD) {
                        lines.add(new SkipAhead(segmentCount++, safeStart, previousEndTick).toString());
                    }
                }
                if (tick.getName().startsWith("exec")) {
                    if (!tick.getTemplate().equals(Record.Template)
                        && !tick.getTemplate().isEmpty()
                        && !tick.getTemplate().toLowerCase().startsWith("exec ")) {
                            cfgFactory.makeCfg(tick, segmentCount);
                    }
                }
                if (tick.getName().equals(Exec.Name)) {
                    lines.add(tick.getSegment(segmentCount++).toString());
                } else {
                    lines.add(new StartRec(segmentCount++, tick.getStart(), tick.getTemplate()).toString());
                    lines.add(new StopRec(segmentCount++, tick.getEnd()).toString());
                }
                previousEndTick = tick.getEnd();
            }
            String nextdemo = peeknext.get(demo);
            if (nextdemo != null) {
                lines.add(new NextDem(segmentCount, previousEndTick, nextdemo).toString());
            } else {
                lines.add(new StopDem(segmentCount, previousEndTick).toString());
            }
            lines.add("}\n");

            // TODO: check for potential bugs for demos located in folders other than TF dir
            Path added =
                Files.write(
                    cfg.getTfPath()
                        .resolve(Util.stripFilenameExtension(demo) + ".vdm"), lines,
                    Charset.defaultCharset());
            paths.add(added);
            log.fine("VDM file written to " + added);

        }
        return paths;
    }
}
