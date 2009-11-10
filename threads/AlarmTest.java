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
	alm.waitUntil(5000000);
	
	System.out.println("**** Alarm testing end ****");
    }

}
