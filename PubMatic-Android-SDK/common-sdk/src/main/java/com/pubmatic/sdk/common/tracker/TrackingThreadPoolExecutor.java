package com.pubmatic.sdk.common.tracker;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import android.util.Log;

public class TrackingThreadPoolExecutor extends ThreadPoolExecutor {

	/*
	 * The number of threads to keep in the pool, even if they are idle, unless
	 * allowCoreThreadTimeOut is set
	 */
	private static final int CORE_POOL_SIZE = TrackingthreadPoolConstants.CORE_POOL_SIZE;
	
	/*
	 * The maximum number of threads to allow in the pool
	 */
	private static final int MAXIMUM_POOL_SIZE = 5;

	/*
	 * When the number of threads is greater than the core, this is the maximum
	 * time that excess idle threads will wait for new tasks before terminating.
	 */
	private static final int KEEP_ALIVE_TIME = 5;
	
	/*
	 * The time unit for the keepAliveTime argument
	 */
	private static final TimeUnit KEEP_ALIVE_TIME_UNIT = TimeUnit.MILLISECONDS;
	
	/*
	 * Wait Timeout after shutdown 
	 */
	private static final int SHUTDOWN_TIMEOUT = 60;
	
    /*
     * Creates a work queue for the pool of Thread objects used for firing tracking URLs, using a linked
     * list queue that blocks when the queue is empty.
     */
	private static BlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<Runnable>();

	private TrackingThreadPoolExecutor() {
		super(CORE_POOL_SIZE, MAXIMUM_POOL_SIZE, KEEP_ALIVE_TIME,
				KEEP_ALIVE_TIME_UNIT, workQueue);
		
		allowCoreThreadTimeOut(true);
	}

	/**
	 * Initializes singleton.
	 *
	 * SingletonHolder is loaded on the first execution of
	 * Singleton.getInstance() or the first access to SingletonHolder.INSTANCE,
	 * not before.
	 */
	private static class SingletonHolder {
		private static TrackingThreadPoolExecutor INSTANCE = new TrackingThreadPoolExecutor();
	}

	public static TrackingThreadPoolExecutor getInstance() {

		if (TrackingThreadPoolExecutor.SingletonHolder.INSTANCE.isShutdown()
				|| TrackingThreadPoolExecutor.SingletonHolder.INSTANCE
						.isTerminated()
				|| TrackingThreadPoolExecutor.SingletonHolder.INSTANCE
						.isTerminating()) {
			SingletonHolder.INSTANCE = new TrackingThreadPoolExecutor();
		}

		return SingletonHolder.INSTANCE;
	}
	
	public void shutdownAndAwaitTermination() 
	{
		// Disable new tasks from being submitted
		this.shutdown(); 
		
		try 
		{
			// Wait a while for existing tasks to terminate
		    if (!this.awaitTermination(SHUTDOWN_TIMEOUT, TimeUnit.SECONDS)) 
		    {
		    	// Cancel currently executing tasks
		    	this.shutdownNow();
		    	
		    	// Wait a while for tasks to respond to being cancelled
		    	if (!this.awaitTermination(SHUTDOWN_TIMEOUT, TimeUnit.SECONDS))	
		           System.err.println("Pool did not terminate");
		     
		    }
		}
		catch (InterruptedException ie) 
		{
			// (Re-)Cancel if current thread also interrupted
			this.shutdownNow();
		    // Preserve interrupt status
		    Thread.currentThread().interrupt();
		}
		
		Log.d("ThreadPoolExecutor",  "TrackingThreadPoolExecutor :  Shutting down");
	}
}
