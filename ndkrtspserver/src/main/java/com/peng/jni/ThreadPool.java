package com.peng.jni;

import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by liuhonghai on 2016/5/31.
 */
public class ThreadPool {
    private static final String TAG = "ThreadPool";

    // CPU的核心数
    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
    // 线程池的核心线程数
    private static final int CORE_POOL_SIZE = CPU_COUNT*6 + 1;
    // 线程池的最大的数量
//    private static final int MAX_POOL_SIZE = CPU_COUNT * 6 + 1;
    private static final int MAX_POOL_SIZE = Integer.MAX_VALUE;
    // 允许线程闲置多长时间，如果超过这个时间则回收
    private static final long KEEP_ALIVE = 1L;
    // 线程工厂
    private static final ThreadFactory mThreadFactory = new ThreadFactory() {
        // 安全的计数器
        private final AtomicInteger mCount = new AtomicInteger(1);

        @Override
        public Thread newThread(Runnable runnable) {
            return new Thread(runnable, TAG + "#" + mCount.getAndIncrement());
        }
    };
    // 自定义的线程池
    private static final ThreadPoolExecutor THREAD_POOL_EXECUTOR = new ThreadPoolExecutor(CORE_POOL_SIZE,
            MAX_POOL_SIZE, KEEP_ALIVE, TimeUnit.SECONDS, new LinkedBlockingDeque<Runnable>(), mThreadFactory);

    private static ThreadPool mInstance;

    public static ThreadPool getInstance() {
        if (mInstance == null) {
            mInstance = new ThreadPool();
            //是否允许核心线程空闲退出
            THREAD_POOL_EXECUTOR.allowCoreThreadTimeOut(true);
        }
        return mInstance;
    }

    public void execute(Runnable runnable) {
//        getPoolSize 获取实际的线程数量
//
//        getActiveCount 获取正在运行的线程数量
//
//        getQueue().size() 获取队列中等待的线程数量
        ELog.e(TAG,"getPoolSize:"+THREAD_POOL_EXECUTOR.getPoolSize()+" getActiveCount:"+THREAD_POOL_EXECUTOR.getActiveCount()+
                " getQueue().size："+THREAD_POOL_EXECUTOR.getQueue().size());
        THREAD_POOL_EXECUTOR.execute(runnable);
    }
}
