package ticketingsystem;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/*
 * 0-indexed concurrent interval.
 * */
public class ConcurrentInterval {
    protected AtomicInteger bitmap;

    ConcurrentInterval(int length) {
        if (length >= 31) {
            throw new IllegalStateException("Illegal construction of ConcurrentInterval!");
        }
        bitmap = new AtomicInteger(0);
    }

    public boolean tryReserve(int beginInclusive, int endExclusive) {
        int mask = ((1 << endExclusive) - 1) ^ ((1 << beginInclusive) - 1);
        // By directly returning in if-else block, we save up a branch instruction. Maybe :)
//        if ((bitmap & mask) == 0) {
//            bitmap |= mask;
//            return true;
//        } else {
//            return false;
//        }
        while (true) {
            int oldVal = bitmap.get();
            if ((oldVal & mask) == 0) {
                if (bitmap.compareAndSet(oldVal, oldVal | mask)) {
                    return true;
                }
            } else {
                return false;
            }
        }
    }

    public boolean tryFree(int beginInclusive, int endExclusive) {
        int mask = ((1 << endExclusive) - 1) ^ ((1 << beginInclusive) - 1);
//        if ((bitmap & mask) == mask) {
//            bitmap ^= mask;
//            return true;
//        } else {
//            return false;
//        }
        while (true) {
            int oldVal = bitmap.get();
            if ((oldVal & mask) == mask) {
                if (bitmap.compareAndSet(oldVal, oldVal ^ mask)) {
                    return true;
                }
            } else {
                return false;
            }
        }
    }

    public boolean isAvailable(int beginInclusive, int endExclusive) {
        int mask = ((1 << endExclusive) - 1) ^ ((1 << beginInclusive) - 1);
        return (bitmap.get() & mask) == 0;
    }
}
