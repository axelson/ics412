package nachos.threads;

import nachos.machine.*;

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
	
	//If there is a task that is waiting, restore it to ready status
	if (waitThread != null) {
	  waitThread.ready();
	}
	
	//Restore interrupts
	Machine.interrupt().restore(intStatus);

	//Current thread yields and context switches
	KThread.currentThread().yield();

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
     * @param	x	the minimum number of clock ticks to wait.
     *
     * @see	nachos.machine.Timer#getTime()
     */
    public void waitUntil(long x) {
	//Sets current thread as waitThread
        waitThread = KThread.currentThread();
	
	//Sets wakeTime with x ticks
	long wakeTime = Machine.timer().getTime() + x;
	
	//Disable interrupts
	boolean intStatus = Machine.interrupt().disable();
	
	//Puts task to sleep for x ticks
	while(wakeTime > Machine.timer().getTime()){
	  KThread.sleep();
	}

	//Restores interrupts
	Machine.interrupt().restore(intStatus);
	
	//Sets waitThread as null
	waitThread = null;
	
	// This is a bad busy waiting solution 
	// long wakeTime = Machine.timer().getTime() + x;
	// while (wakeTime > Machine.timer().getTime())
	//    KThread.yield();
    }

    /**
     * Tests whether this module is working.
     */
    public static void selfTest() {
	AlarmTest.runTest();
    }

    private static final char dbgAlarm = 'a';
    private KThread waitThread = null;
}
