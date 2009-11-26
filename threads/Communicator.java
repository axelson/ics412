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
	//Disable interrupts
	boolean intStatus = Machine.interrupt().disable();
	
	KThread thread = null;
	
	//While there is no listener in the listener queue
	while((thread = listenerQueue.nextThread()) == null){
	    //Puts speaker in the speaker queue
	    this.speakerQueue.waitForAccess(KThread.currentThread());
	    //Puts speaker to sleep
	    KThread.sleep();
	}
       
	buffer = word;
	
	//Wakes up the listener
	thread.ready();
	
	//Restores interrupts
	Machine.interrupt().restore(intStatus);
    }
    
    /**
     * Wait for a thread to speak through this communicator, and then return
     * the <i>word</i> that thread passed to <tt>speak()</tt>.
     *
     * @return  the integer transferred.
     */    
    public int listen() {
	//Disable interrupts
	boolean intStatus = Machine.interrupt().disable();
	
	KThread thread = null;
	
	//Gets the first speaker in the speaker queue
	thread = speakerQueue.nextThread();
	
	//Puts the listener in the listener queue
	this.listenerQueue.waitForAccess(KThread.currentThread());
	
	//If there is a speaker
	if(thread != null){
	    //Wake up that speaker
	    thread.ready();
	}
	
	//Puts the listener to sleep
	KThread.sleep();
	
	//Restores interrupts and returns the buffer (word)
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
    private int buffer;
}
