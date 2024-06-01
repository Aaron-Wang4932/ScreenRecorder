package org.example.recording;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
public class CaptureScheduler {
    private final int threadCount = 6;
    private final int fpsPerThread = 5;
    private final Rectangle captureSize = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
    public final ScheduledExecutorService scheduledThreadPool = Executors.newScheduledThreadPool(threadCount);
    private final ConcurrentLinkedQueue<BufferedImage> store = new ConcurrentLinkedQueue<>();
    private static long fractionToNanos(long a, long b){
        return a * 1_000_000_000 / b;
    }

    public void init() throws AWTException {

        // Starts all 6 threads.
        for (int i = 0; i < threadCount; i++) {
            scheduledThreadPool.scheduleAtFixedRate(
                    new CaptureWorker(new Robot(),captureSize, store),
                    /*
                    * There are 6 total threads;
                    * Threads that are running should not overlap these processes,
                    * thus they start one after the next.
                    * In this example, they start at 0/6s, 1/6s, 2/6s, etc.*/
                    fractionToNanos(i,threadCount),
                    /*
                    * 10 fps per thread.
                    * Thus, each thread should action every 0.1s.
                    */
                    fractionToNanos(1,fpsPerThread),
                    // Specify that all time measurements are in nanoseconds for greatest precision.
                    TimeUnit.NANOSECONDS
            );
        }
        // All threads will now run indefinitely until the scheduledThreadPool is shut down.
    }

    public void stop(){
        scheduledThreadPool.shutdown();
    }

    public ConcurrentLinkedQueue<BufferedImage> getStore(){
        return store;
    }

}