package gr.kzps.dd.DeadlockHandlersImpl;

import gr.kzps.dd.DeadlockHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.management.ThreadInfo;
import java.util.Map;

public class DeadlockHandlerLog implements DeadlockHandler {
    private final Logger LOG = LogManager.getLogger(DeadlockHandlerLog.class);

    public void handleDeadlock(ThreadInfo[] deadlockedThreads) {
        if (deadlockedThreads != null) {
            LOG.info("*** Deadlock detected ***");

            Map<Thread, StackTraceElement[]> stackTraceMap = Thread.getAllStackTraces();
            for (ThreadInfo threadInfo : deadlockedThreads) {
                if (threadInfo != null) {
                    for (Thread thread : stackTraceMap.keySet()) {
                        if (thread.getId() == threadInfo.getThreadId()) {
                            LOG.info(threadInfo.toString().trim());

                            for (StackTraceElement ste : thread.getStackTrace()) {
                                LOG.info("\t" + ste.toString().trim());
                            }
                        }
                    }
                }
            }
        }
    }
}
