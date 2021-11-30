package ticketingsystem;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

/*
 * 0-indexed concurrent interval.
 * */
public class ConcurrentInterval {
    protected long bitmap;

    ConcurrentInterval(int length) {
        if (length >= 63) {
            throw new IllegalStateException("Illegal construction of ConcurrentInterval!");
        }
        bitmap = 0;
    }

    public boolean tryReserve(int beginInclusive, int endExclusive) {
        long mask = ((1L << endExclusive) - 1) ^ ((1L << beginInclusive) - 1);
        if ((bitmap & mask) == 0) {
            bitmap |= mask;
            return true;
        } else {
            return false;
        }
    }

    public boolean tryFree(int beginInclusive, int endExclusive) {
        long mask = ((1L << endExclusive) - 1) ^ ((1L << beginInclusive) - 1);
        if ((bitmap & mask) == mask) {
            bitmap ^= mask;
            return true;
        } else {
            return false;
        }
    }

    public boolean isAvailable(int beginInclusive, int endExclusive) {
        long mask = ((1L << endExclusive) - 1) ^ ((1L << beginInclusive) - 1);
        return (bitmap & mask) == 0;
    }

    public boolean isAllOccupied(int beginInclusive, int endExclusive) {
        long mask = ((1L << endExclusive) - 1) ^ ((1L << beginInclusive) - 1);
        return (bitmap & mask) == mask;
    }
}
