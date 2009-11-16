package nachos.threads;

import nachos.machine.*;

/**
 * A <i>communicator</i> allows threads to synchronously exchange 32-bit
 * messages. Multiple threads can be waiting to <i>speak</i>,
 * and multiple threads can be waiting to <i>listen</i>. But there should never
 * be a time when both a speaker and a listener are waiting, because the two
 * threads can be paired off at this point.
 */
public class Communicator {
    /**
     * Allocate a new communicator.
     */
    public Communicator() {
      this.listenerQueue = ThreadedKernel.scheduler.newThreadQueue(true);
      this.speakerQueue = ThreadedKernel.scheduler.newThreadQueue(true);
      this.mutex = new Lock();
    }

    /**
     * Wait for a thread to listen through this communicator, and then transfer
     * <i>word</i> to the listener.
     *
     * <p>
     * Does not return until this thread is paired up with a listening thread.
     * Exactly one listener should receive <i>word</i>.
     *
     * @param   word    the integer to transfer.
     */
    public void speak(int word) {
	boolean intStatus = Machine.interrupt().disable();
	//mutex.acquire();
	KThread thread = null;
	while((thread = listenerQueue.nextThread()) == null){
	  this.speakerQueue.waitForAccess(KThread.currentThread());
       	  KThread.sleep();
	}
	buffer = word;
	thread.ready();
	
	//mutex.release();
	Machine.interrupt().restore(intStatus);
    }

    /**
     * Wait for a thread to speak through this communicator, and then return
     * the <i>word</i> that thread passed to <tt>speak()</tt>.
     *
     * @return  the integer transferred.
     */    
    public int listen() {
	boolean intStatus = Machine.interrupt().disable();
	
	KThread thread = null;
	
	thread = speakerQueue.nextThread();
	this.listenerQueue.waitForAccess(KThread.currentThread());

	if(thread != null){
	    thread.ready();
	}
	KThread.sleep();
	
	Machine.interrupt().restore(intStatus);
        return buffer;
    }

    /**
     * Tests whether this module is working.
     */
    public static void selfTest() {
        CommunicatorTest.runTest();
    }
  private ThreadQueue listenerQueue;
  private ThreadQueue speakerQueue;
  private Lock mutex;
  private int buffer = 0;
}
