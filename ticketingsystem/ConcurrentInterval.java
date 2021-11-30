package ticketingsystem;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

/*
 * 0-indexed concurrent interval.
 * */
public class ConcurrentInterval {
    protected long bitmap;
    protected final ReentrantLock lock;

    ConcurrentInterval(int length) {
        if (length >= 63) {
            throw new IllegalStateException("Illegal construction of ConcurrentInterval!");
        }
        bitmap = 0;
        lock = new ReentrantLock();
    }

    public boolean tryReserve(int beginInclusive, int endExclusive) {
        boolean result = true;
        long mask = ((1L << endExclusive) - 1) ^ ((1L << beginInclusive) - 1);

        lock.lock();
        if ((bitmap & mask) == 0) {
            bitmap |= mask;
        } else {
            result = false;
        }
        lock.unlock();
        return result;
    }

    public boolean tryFree(int beginInclusive, int endExclusive) {
        boolean result = true;
        long mask = ((1L << endExclusive) - 1) ^ ((1L << beginInclusive) - 1);

        lock.lock();
        if ((bitmap & mask) == mask) {
            bitmap ^= mask;
        } else {
            result = false;
        }
        lock.unlock();
        return result;
    }

    public boolean isAvailable(int beginInclusive, int endExclusive) {
        long mask = ((1L << endExclusive) - 1) ^ ((1L << beginInclusive) - 1);
        lock.lock();
        boolean result = (bitmap & mask) == 0;
        lock.unlock();
        return result;
    }
}
