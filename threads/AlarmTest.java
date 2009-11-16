package nachos.threads;

import nachos.machine.*;
import java.util.Random;


/**
 * A Tester for the Alarm class
 */
public class AlarmTest {

    public static void runTest() {
	System.out.println("**** Alarm testing begins ****");

        Alarm alm = new Alarm();
	long wakeTime = 600;
	System.out.println("System will now sleep for " + wakeTime + " ticks...");
	alm.waitUntil(wakeTime);
	
	System.out.println("**** Alarm testing end ****");
    }

}
