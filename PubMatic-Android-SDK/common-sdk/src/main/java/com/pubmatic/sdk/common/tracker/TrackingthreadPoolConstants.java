package com.pubmatic.sdk.common.tracker;

public class TrackingthreadPoolConstants {

	/*
	 * Maximum number of threads that the ThreadPoolexecutor will create to
	 * process impressions. If more than CORE_POOL_SIZE number of threads are to
	 * be processed , all the remaining threads (Each containing
	 * MAX_URL_PER_THREAD number of impressions) will be queed in the workQueue
	 * of TrackingthreadPoolExecutor
	 * 
	 * At a time TrackingThreadPoolExecutor would be able to process
	 * CORE_POOL_SIZE * MAX_URL_PER_THREAD impressions
	 */
	public static final int CORE_POOL_SIZE = 5;

	/*
	 * Maximum number of Impression URLs that can be grouped in a thread , to be
	 * submitted to TrackingThreadPoolExecutor. One thread will be created with
	 * MAX_URL_PER_THREAD number of Impression URLs in it and given to
	 * ThreadPoolExecutor
	 * 
	 * At a time TrackingThreadPoolExecutor would be able to process
	 * CORE_POOL_SIZE * MAX_URL_PER_THREAD impressions
	 */
	public static int MAX_URL_PER_THREAD = 5;
}
