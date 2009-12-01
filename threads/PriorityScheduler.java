package nachos.threads;

import nachos.machine.*;

import java.util.TreeSet;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Iterator;
import java.util.Stack;

import java.util.PriorityQueue;
import java.util.Comparator;

/**
 * A scheduler that chooses threads based on their priorities.
 *
 * <p>
 * A priority scheduler associates a priority with each thread. The next thread
 * to be dequeued is always a thread with priority no less than any other
 * waiting thread's priority. Like a round-robin scheduler, the thread that is
 * dequeued is, among all the threads of the same (highest) priority, the
 * thread that has been waiting longest.
 *
 * <p>
 * Essentially, a priority scheduler gives access in a round-robin fassion to
 * all the highest-priority threads, and ignores all other threads. This has
 * the potential to
 * starve a thread if there's always a thread waiting with higher priority.
 *
 * <p>
 * A priority scheduler must partially solve the priority inversion problem; in
 * particular, priority must be donated through locks, and through joins.
 */
public class PriorityScheduler extends Scheduler {
    /**
     * Allocate a new priority scheduler.
     */
    public PriorityScheduler() {
    }

    /**
     * Allocate a new priority thread queue.
     *
     * @param   transferPriority        <tt>true</tt> if this queue should
     *                                  transfer priority from waiting threads
     *                                  to the owning thread.
     * @return  a new priority thread queue.
     */
    public ThreadQueue newThreadQueue(boolean transferPriority) {
        return new PriorityQueue(transferPriority);
    }

    /**
     * getPriority()
     */
    public int getPriority(KThread thread) {
        Lib.assertTrue(Machine.interrupt().disabled());

        return getThreadState(thread).getPriority();
    }

    /**
     * getEffectivePriority()
     */
    public int getEffectivePriority(KThread thread) {
        Lib.assertTrue(Machine.interrupt().disabled());

        return getThreadState(thread).getEffectivePriority();
    }

    /**
     * setPriority()
     */
    public void setPriority(KThread thread, int priority) {
        Lib.assertTrue(Machine.interrupt().disabled());

        Lib.assertTrue(priority >= priorityMinimum &&
                       priority <= priorityMaximum);

        getThreadState(thread).setPriority(priority);
    }

    /**
     * The self test
     */
    public static void selfTest() {
      PrioritySchedulerTest.runTest();
    }

    /**
     * The default priority for a new thread. Do not change this value.
     */
    public static final int priorityDefault = 1;
    /**
     * The minimum priority that a thread can have. Do not change this value.
     */
    public static final int priorityMinimum = 0;
    /**
     * The maximum priority that a thread can have. Do not change this value.
     */
    public static final int priorityMaximum = 7;

    /**
     * Return the scheduling state of the specified thread.
     *
     * @param   thread  the thread whose scheduling state to return.
     * @return  the scheduling state of the specified thread.
     */
    protected ThreadState getThreadState(KThread thread) {
        if (thread.schedulingState == null) {
            thread.schedulingState = new ThreadState(thread);
        }

        return (ThreadState) thread.schedulingState;
    }

    /**
     * A <tt>ThreadQueue</tt> that sorts threads by priority. Only
     * parts of this are implemented and you should complete it, by adding
     * code wherever you see fit and whichever method you see fit.
     */
    protected class PriorityQueue extends ThreadQueue {

        PriorityQueue(boolean transferPriority) {
            this.transferPriority = transferPriority;

        }

        /**
         * Tests if <tt>PriorityQueue</tt> enables priority transfer
         * @return  True if <tt>PriorityQueue</tt> has enabled priority transfer
         */
        public boolean transferPriority() {
          return this.transferPriority;
        }

        /**
         * The thread declares its intent to wait for access to the
         * "resource" guarded by this priority queue. This method is only called
         * if the thread cannot immediately obtain access.
         *
         * @param       thread       The thread
         *
         * @see nachos.threads.ThreadQueue#waitForAccess
         */

        public void waitForAccess(KThread thread) {
            Lib.assertTrue(Machine.interrupt().disabled());

            waitQueue.offer(thread);
        }

        /* print(): Prints the priority queue, for potential debugging
         */
        public void print() {
	  System.out.println("***Printing waitQueue***");
	  Iterator itr = waitQueue.iterator();
	  int i = 0;
	  while (itr.hasNext()) {
	    KThread thread = (KThread)itr.next();
	    System.out.println(i + ":\t" + thread.getName());
	    i++;
	  }
        }

        /**
	 * The specified thread has received exclusive access, without using
	 * <tt>waitForAccess()</tt> or <tt>nextThread()</tt>. Assert that no
         * threads are waiting for access.
         */
        public void acquire(KThread thread) {
            Lib.assertTrue(Machine.interrupt().disabled());

            Lib.assertTrue(waitQueue.isEmpty());

            getThreadState(thread).addResource(this);

            // Record who currently has access to the resource
            currentOwner = thread;
        }



        /**
         * Select the next thread in the ThreadQueue
         */
        public KThread nextThread() {
            Lib.assertTrue(Machine.interrupt().disabled());

            /* Current owner is releasing this resource, so remove this resource from the owners list of resources */
            if(currentOwner != null) {
              getThreadState(currentOwner).removeResource(this);
            }

            if (waitQueue.isEmpty()) {
              // No one owns this queue any more
              currentOwner = null;
            } else {
              /* Record the new current owner */
              currentOwner = (KThread) waitQueue.poll();
            }

            return currentOwner;
        }

        /**
         * Return the next thread that <tt>nextThread()</tt> would return,
         * without modifying the state of this queue.
         *
         * @return      the next thread that <tt>nextThread()</tt> would
         *              return.
         */
        protected KThread pickNextThread() {

          return (KThread) waitQueue.peek();
        }

        /**
         * Returns true if queue is empty
         * @return      True if queue is empty
         */
        public boolean empty() {
          return (this.waitQueue.size() == 0);
        }

        /**
         * <tt>true</tt> if this queue should transfer priority from waiting
         * threads to the owning thread.
         */
        public boolean transferPriority;

        /** Records who currently has access to the resource */
        private KThread currentOwner = null;

        /** Priority Queue to hold threads. Returns them based on priority */
        private java.util.PriorityQueue waitQueue = new java.util.PriorityQueue<KThread>(11, new CompareThreadsByPriority());


    }

    /** Class to compare threads */
    public class CompareThreadsByPriority implements Comparator<KThread> {
      public int compare(KThread x, KThread y) {
        return getThreadState(x).getEffectivePriority() - getThreadState(y).getEffectivePriority();
      }
    }

    /**
     * The scheduling state of a thread. This should include the thread's
     * priority, its effective priority, any objects it owns, and the queues
     * it's waiting for, etc. This is a convenience class so that
     * no modification to the KThread class are needed for a new scheduler.
     * Each scheduler keeps track of scheduler specific KThread information
     * in its own declaration of the ThreadState class.
     *
     * @see     nachos.threads.KThread#schedulingState
     */
    protected class ThreadState {

        /**
         * Allocate a new <tt>ThreadState</tt> object and associate it with the
         * specified thread.
         *
         * @param       thread  the thread this state belongs to.
         */
        public ThreadState(KThread thread) {
            this.thread = thread;
            this.priority = priorityDefault;
            this.ownedResources = new LinkedList();
        }

        /**
         * Return the priority of the associated thread.
         *
         * @return      the priority of the associated thread.
         */
        public int getPriority() {
            return priority;
        }

        /**
         * Set the priority of the associated thread to the specified value.
         *
         * @param       priority        the new priority.
         */
        public void setPriority(int priority) {
            this.priority = priority;
        }

        /**
         * Adds resource to list of owned resources
         */
        public void addResource(PriorityQueue resource) {
          this.ownedResources.add(resource);
        }

        /**
         * Remove a resource. Returns true if it sucessfully removes
         */
        public boolean removeResource(PriorityQueue resource) {
          return this.ownedResources.remove(resource);
        }

        /**
         * Return the effective priority of the associated thread.
         *
         * @return      the effective priority of the associated thread.
         */
        public int getEffectivePriority() {
          // Initialize effective priority to actual priority
          int effectivePriority = this.priority;

          /* Loop over priority queue to find the "highest" priority thread waiting on
           * the current thread
           */
          for(PriorityQueue q : this.ownedResources) {
            // Only transfer priority if this queue allows priority to be transferred
            if(q.transferPriority() && !q.empty()) {
              if(effectivePriority > getThreadState(q.pickNextThread()).getEffectivePriority()) {
                effectivePriority = getThreadState(q.pickNextThread()).getEffectivePriority();
              }
            }
          }
          return effectivePriority;
        }

        /** The thread with which this object is associated. */
        protected KThread thread;

        /** The priority of the associated thread. */
        protected int priority;

        /** List of resource owned by this thread */
        protected LinkedList<PriorityQueue> ownedResources;
    }

}
