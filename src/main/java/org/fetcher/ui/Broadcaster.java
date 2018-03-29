package org.fetcher.ui;

import com.google.common.util.concurrent.RateLimiter;

import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SuppressWarnings("serial")
public class Broadcaster implements Serializable {
  public interface BroadcastListener {
    void receiveBroadcast(String message);
  }
  static ExecutorService executorService = Executors.newSingleThreadExecutor();

  // Limit broadcasts to 1 every 2 seconds
  static RateLimiter broadcastLimit = RateLimiter.create(0.5);

  private static ConcurrentHashMap<BroadcastListener, Integer> listeners = new ConcurrentHashMap<>();

  /**
   * Broadcast at the particular rate limit.
   * 
   * @param message
   * @return was the message broadcast
   */
  public static boolean broadcast(final String message) {
    if (broadcastLimit.tryAcquire()) {
      broadcastLimit.acquire();
      for (final BroadcastListener listener : listeners.keySet()) {
        executorService.execute(() -> listener.receiveBroadcast(message));
      }
      return true;
    }
    return false;
  }

  public static void register(BroadcastListener listener) {
    listeners.put(listener, 1);
  }

  public static void unregister(BroadcastListener listener) {
    listeners.remove(listener);
  }
}