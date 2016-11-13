package gr.kzps.dd;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertTrue;

import java.lang.management.ThreadInfo;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class TestDeadLockDetector {
    private final Logger LOG = LogManager.getLogger(TestDeadLockDetector.class);

    private DeadlockDetector dd;
    private volatile boolean deadlockDetected = false;

    @Before
    public void setUp() {
        LOG.debug("Starting Deadlock detector");
        dd = new DeadlockDetector(new DummyDDHandler(), 500, TimeUnit.MILLISECONDS);
        dd.start();
    }

    @After
    public void tearDown() {
        if (dd != null) {
            dd.stop();
            LOG.debug("Stopped Deadlock detector");
        }
    }

    @Test
    public void testSimpleDeadlock() throws Exception {
        final ReentrantLock lock0 = new ReentrantLock(true);
        final ReentrantLock lock1 = new ReentrantLock(true);

        Thread thread0 = new Thread(new Runnable() {
            public void run() {
                lock0.lock();
                LOG.debug("Acquired lock0");
                try {
                    LOG.debug("Sleeping for a while");
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException ex) {
                    LOG.debug(ex, ex);
                }
                lock1.lock();
                LOG.debug("Acquired lock1");
                lock1.unlock();
                lock0.unlock();
            }
        });
        thread0.setName("Thread-0");

        Thread thread1 = new Thread(new Runnable() {
            public void run() {
                lock1.lock();
                LOG.debug("Acquired lock1");
                lock0.lock();
                LOG.debug("Acquired lock0");
                lock0.unlock();
                lock1.unlock();
            }
        });

        thread1.setName("Thread-1");

        thread0.start();
        thread1.start();

        // Give some time to the Deadlock detector to detector to detect it
        TimeUnit.SECONDS.sleep(2);

        assertTrue("Deadlock should have been detected", deadlockDetected);
    }

    private class DummyDDHandler implements DeadlockHandler {

        public void handleDeadlock(ThreadInfo[] deadlockedThreads) {
            deadlockDetected = true;
            LOG.debug("Deadlock detected");
        }
    }
}
