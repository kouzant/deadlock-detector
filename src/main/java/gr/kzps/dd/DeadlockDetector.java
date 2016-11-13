package gr.kzps.dd;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DeadlockDetector {
    private final Logger LOG = LogManager.getLogger(DeadlockDetector.class);

    private final DeadlockHandler deadlockHandler;
    private final long period;
    private final TimeUnit timeUnit;
    private final ThreadMXBean mbean = ManagementFactory.getThreadMXBean();
    private final ScheduledExecutorService exec =
            Executors.newScheduledThreadPool(1);

    public DeadlockDetector(DeadlockHandler deadlockHandler, long period, TimeUnit timeUnit) {
        this.deadlockHandler = deadlockHandler;
        this.period = period;
        this.timeUnit = timeUnit;
    }

    public void start() {
        LOG.info("Starting Deadlock Detector Thread");
        exec.scheduleAtFixedRate(new DeadlockChecker(), 0, period, timeUnit);
    }

    public void stop() {
        LOG.info("Stopping Deadlock Detector Thread");
        exec.shutdownNow();
    }

    private class DeadlockChecker implements Runnable {

        public void run() {
            long[] deadLockedThreadIds = mbean.findDeadlockedThreads();

            if (deadLockedThreadIds != null) {
                ThreadInfo[] threadInfos = mbean.getThreadInfo(deadLockedThreadIds);
                deadlockHandler.handleDeadlock(threadInfos);
            }
        }
    }
}
