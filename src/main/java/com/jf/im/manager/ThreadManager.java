package com.jf.im.manager;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 封装的线程池 方便调用
 */
public class ThreadManager {
	private static ThreadManager instance = new ThreadManager();
	private ExecutorService pool = null;
	public static ThreadManager getInstance() {return instance;}
	private ThreadManager() {pool = Executors.newFixedThreadPool(25);}
	public void execute(Runnable r)
	{
		pool.execute(r);
	}
	public void shutdown()
	{
		pool.shutdown();
	}
}
