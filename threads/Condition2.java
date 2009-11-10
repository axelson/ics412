package nachos.threads;

import nachos.machine.*;

/**
 * An implementation of condition variables that disables interrupt()s for
 * synchronization.
 *
 * <p>
 * You must implement this.
 *
 * @see nachos.threads.Condition
 */
public class Condition2 {
    /**
     * Allocate a new condition variable.
     *
     * @param   conditionLock   the lock associated with this condition
     *                          variable. The current thread must hold this
     *                          lock whenever it uses <tt>sleep()</tt>,
     *                          <tt>wake()</tt>, or <tt>wakeAll()</tt>.
     */
    public Condition2(Lock conditionLock) {
        this.conditionLock = conditionLock;

        this.waitingThreads = ThreadedKernel.scheduler.newThreadQueue(true);
    }

    /**
     * Atomically release the associated lock and go to sleep on this condition
     * variable until another thread wakes it using <tt>wake()</tt>. The
     * current thread must hold the associated lock. The thread will
     * automatically reacquire the lock before <tt>sleep()</tt> returns.
     */
    public void sleep() {
        boolean intStatus = Machine.interrupt().disable();

        /* If the current thread doesn't hold the lock, then abort */
        Lib.assertTrue(conditionLock.isHeldByCurrentThread());

        // Add thread to waitingThreads
        waitingThreads.waitForAccess(KThread.currentThread());

        // Release lock
        conditionLock.release();

        // Go to sleep
        KThread.sleep();

        // Get lock upon awakening
        conditionLock.acquire();

        Machine.interrupt().restore(intStatus);
    }

    /**
     * Wake up at most one thread sleeping on this condition variable. The
     * current thread must hold the associated lock.
     */
    public void wake() {
        Lib.assertTrue(conditionLock.isHeldByCurrentThread());
        boolean intStatus = Machine.interrupt().disable();

        KThread thread = waitingThreads.nextThread();
        if(thread != null) {
            thread.ready();
        }

        Machine.interrupt().restore(intStatus);
    }

    /**
     * Wake up all threads sleeping on this condition variable. The current
     * thread must hold the associated lock.
     */
    public void wakeAll() {
        Lib.assertTrue(conditionLock.isHeldByCurrentThread());
        boolean intStatus = Machine.interrupt().disable();

        /* Wake all threads that are waiting on the condition variable */
        KThread thread = null;
        do {
            thread = waitingThreads.nextThread();
            if(thread != null) {
                thread.ready();
            }
        } while(thread != null);
        Machine.interrupt().restore(intStatus);
    }

    /**
     * Tests whether this module is working.
     */
    public static void selfTest() {
        Condition2Test.runTest();
    }

    private static final char dbgCondition = 'c';

    /** Lock associated with this condition variable */
    private Lock conditionLock;

    /** Threads waiting for this condition to be signaled */
    private ThreadQueue waitingThreads = null;
}
