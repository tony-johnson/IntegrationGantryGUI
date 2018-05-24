package org.lsst.ccs.integrationgantrygui;

import java.io.IOException;
import java.time.Duration;
import java.util.logging.Logger;
import org.lsst.ccs.Subsystem;
import org.lsst.ccs.bus.data.KeyValueData;
import org.lsst.ccs.commons.annotations.LookupField;
import org.lsst.ccs.framework.AgentPeriodicTask;
import org.lsst.ccs.framework.HasLifecycle;
import org.lsst.ccs.services.AgentPeriodicTaskService;

/**
 * A main "subsystem" for running integration gantry GUI and sending trending
 * into to the database. Note that currently this couples the GUI and subsystem
 * which means that trending is only generated while the GUI is running. This
 * should probably be fixed.
 *
 * @author tonyj
 */
public class IGGUISubsystem implements HasLifecycle {

    private static final Logger LOG = Logger.getLogger(IGGUISubsystem.class.getName());

    @LookupField(strategy = LookupField.Strategy.TOP)
    private Subsystem subsys;

    @LookupField(strategy = LookupField.Strategy.TREE)
    private AgentPeriodicTaskService pts;

    private final Main main = new Main();
    private final long[] lastUpdateTime = new long[4];

    @Override
    public void postStart() {
        try {
            main.start();
        } catch (IOException ex) {
            throw new RuntimeException("Error calling start", ex);
        }
    }

    @Override
    public void build() {
        pts.scheduleAgentPeriodicTask(new AgentPeriodicTask("publish-trending", () -> {
            LOG.info("looking for stuff to publish");
            for (int i = 0; i < Main.NCAMERAS; i++) {
                KeyValueData data = main.getTrendingForCamera(i);
                if (data != null && data.getTimestamp() > lastUpdateTime[i]) {
                    subsys.publishSubsystemDataOnStatusBus(data);
                    lastUpdateTime[i] = data.getTimestamp();
                }
            }

        }).withPeriod(Duration.ofSeconds(15)));

    }

}
