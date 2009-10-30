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

        
	KThread.yield();
	System.out.println("**** Simple KThread testing ends ****");
    }

}
