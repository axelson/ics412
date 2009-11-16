package nachos.threads;

import nachos.machine.*;

import java.util.*;

/**
 * Uses the hardware timer to provide preemption, and to allow threads to sleep
 * until a certain time.
 */
public class Alarm {
    /**
     * Allocate a new Alarm. Set the machine's timer interrupt handler to this
     * <p><b>Note</b>: Nachos will not function correctly with more than one
     * alarm.
     */
    public Alarm() {
        Lib.debug(dbgAlarm, "Creating Alarm" + Machine.timer().getTime());
        timeCreated = Machine.timer().getTime();
        waitQueue = new ArrayList<KThread>();
        timeQueue = new ArrayList<Long>();
        queueLock = new Lock();
        Machine.timer().setInterruptHandler(new Runnable() {
                public void run() { timerInterrupt(); }
            });
    }

    /**
     * The timer interrupt handler. This is called by the machine's timer
     * periodically (approximately every 500 clock ticks). Causes the current
     * thread to yield, forcing a context switch if there is another thread
     * that should be run.
     */
    public void timerInterrupt() {
        Lib.debug(dbgAlarm,"In Interrupt Handler (time = "+Machine.timer().getTime()+")");

        //Disable interrupts
        boolean intStatus = Machine.interrupt().disable();

        KThread thread;
        //If there is a task that is waiting, restore it to ready status
        for (int i = 0; i < waitQueue.size(); i++) {
            if (Machine.timer().getTime() > timeQueue.get(i)) {
                waitQueue.get(i).ready();
                waitQueue.remove(i);
                timeQueue.remove(i);
            }
        }

        //Restore interrupts
        Machine.interrupt().restore(intStatus);

        //Current thread yields and context switches
        KThread.yield();

    }

    /**
     * Put the current thread to sleep for at least <i>x</i> ticks,
     * waking it up in the timer interrupt handler. The thread must be
     * woken up (placed in the scheduler ready set) during the first timer
     * interrupt where
     *
     * <p><blockquote>
     * (current time) >= (WaitUntil called time)+(x)
     * </blockquote>
     *
     * @param   x       the minimum number of clock ticks to wait.
     *
     * @see     nachos.machine.Timer#getTime()
     */
    public void waitUntil(long x) {
        //Sets current thread as waitThread

        //Initializes wakeTime with x ticks
        long wakeTime = Machine.timer().getTime() + x;

        //Disable interrupts
        boolean intStatus = Machine.interrupt().disable();
        queueLock.acquire();

        //Puts task to sleep for x ticks
        waitQueue.add(KThread.currentThread());
        Lib.debug(dbgAlarm, "Added new task size="+ waitQueue.size() + " timeCreated="+this.timeCreated);
        timeQueue.add(wakeTime);
        int i;

        queueLock.release();

        KThread.sleep();

        //Restores interrupts
        Machine.interrupt().restore(intStatus);
    }

    /**
     * Tests whether this module is working.
     */
    public static void selfTest() {
        AlarmTest.runTest();
    }

    private static final char dbgAlarm = 'a';
    private List<KThread> waitQueue;
    private List<Long> timeQueue;
    private Lock queueLock;
    private long timeCreated;
}
