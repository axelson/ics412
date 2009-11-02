package nachos.threads;

import nachos.machine.*;

/**
 * A Simple Tester for the KThread class. 
 */
public class KThreadSimpleTest {

    /**
     * Tests whether this module is working.
     */
    public static void runTest() {

	System.out.println("**** Simple KThread testing begins ****");

	// Create 2 Objects
	AThread object1 = new AThread();
	AThread object2 = new AThread();

	// Start the two KThreads
	KThread thread1 = new KThread(object1);
	KThread thread2 = new KThread(object2);

	// Fork the two KThreads
	thread1.fork();
	thread2.fork();

	// Name the threads
	thread1.setName("HelloWorldThread-1");
	thread2.setName("HelloWorldThread-2");

	KThread.yield();
	System.out.println("**** Simple KThread testing ends ****");
    }

}

/**
 * Example thread class
 */
class AThread implements Runnable {
	public void run() {
		System.out.println("Hello World");
	}
}
