package ticketingsystem;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

/*
 * 0-indexed concurrent interval.
 * */
public class ConcurrentInterval {
    private final int[] slot;
    private final ReentrantLock[] lock;

    ConcurrentInterval(int length, int initVal) {
        slot = new int[length];
        lock = new ReentrantLock[length];
        for (int i = 0; i < length; ++i) {
            slot[i] = initVal;
            lock[i] = new ReentrantLock();
        }
    }

    private void lockInterval(int beginInclusive, int endExclusive) {
        for (int i = beginInclusive; i < endExclusive; ++i) {
            lock[i].lock();
        }
    }

    private void unlockInterval(int beginInclusive, int endExclusive) {
        for (int i = beginInclusive; i < endExclusive; ++i) {
            lock[i].unlock();
        }
    }

    public int tryReserve(int beginInclusive, int endExclusive) {
        lockInterval(beginInclusive, endExclusive);
        int remain = countAvailable(beginInclusive, endExclusive);
        if (remain > 0) {
            for (int i = beginInclusive; i < endExclusive; ++i) {
                slot[i]--;
            }
        }
        unlockInterval(beginInclusive, endExclusive);
        return remain;
    }

    public void cancel(int beginInclusive, int endExclusive) {
        lockInterval(beginInclusive, endExclusive);
        for (int i = beginInclusive; i < endExclusive; ++i) {
            slot[i]++;
        }
        unlockInterval(beginInclusive, endExclusive);
    }

    public int countAvailable(int beginInclusive, int endExclusive) {
        int remain = Integer.MAX_VALUE;
        for (int i = beginInclusive; i < endExclusive; ++i) {
            remain = Math.min(remain, slot[i]);
        }
        return remain;
    }
}
