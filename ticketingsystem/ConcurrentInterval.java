package ticketingsystem;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

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

    synchronized public boolean tryReserve(int beginInclusive, int endExclusive) {
        long mask = ((1L << endExclusive) - 1L) ^ ((1L << beginInclusive) - 1L);

        // By directly returning in if-else block, we save up a branch instruction. Maybe :)
        if ((bitmap & mask) == 0) {
            bitmap |= mask;
            return true;
        } else {
            return false;
        }
    }

    synchronized public boolean tryFree(int beginInclusive, int endExclusive) {
        long mask = ((1L << endExclusive) - 1L) ^ ((1L << beginInclusive) - 1L);
        if ((bitmap & mask) == mask) {
            bitmap ^= mask;
            return true;
        } else {
            return false;
        }
    }

    public boolean isAvailable(int beginInclusive, int endExclusive) {
        long mask = ((1L << endExclusive) - 1L) ^ ((1L << beginInclusive) - 1L);
        return (bitmap & mask) == 0;
    }
}
