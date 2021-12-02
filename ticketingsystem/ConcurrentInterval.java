package ticketingsystem;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/*
 * 0-indexed concurrent interval.
 * */
public class ConcurrentInterval {
    protected int bitmap;

    ConcurrentInterval(int length) {
        if (length >= 31) {
            throw new IllegalStateException("Illegal construction of ConcurrentInterval!");
        }
        bitmap = 0;
    }

    synchronized public boolean tryReserve(int beginInclusive, int endExclusive) {
        int mask = ((1 << endExclusive) - 1) ^ ((1 << beginInclusive) - 1);
        // By directly returning in if-else block, we save up a branch instruction. Maybe :)
        if ((bitmap & mask) == 0) {
            bitmap |= mask;
            return true;
        } else {
            return false;
        }
    }

//    synchronized public boolean tryReserve(int mask) {
//        // By directly returning in if-else block, we save up a branch instruction. Maybe :)
//        if ((bitmap & mask) == 0) {
//            bitmap |= mask;
//            return true;
//        } else {
//            return false;
//        }
//    }

    synchronized public boolean tryFree(int beginInclusive, int endExclusive) {
        int mask = ((1 << endExclusive) - 1) ^ ((1 << beginInclusive) - 1);
        if ((bitmap & mask) == mask) {
            bitmap ^= mask;
            return true;
        } else {
            return false;
        }
    }

//    synchronized public boolean tryFree(int mask) {
//        if ((bitmap & mask) == mask) {
//            bitmap ^= mask;
//            return true;
//        } else {
//            return false;
//        }
//    }

    public boolean isAvailable(int beginInclusive, int endExclusive) {
        int mask = ((1 << endExclusive) - 1) ^ ((1 << beginInclusive) - 1);
        return (bitmap & mask) == 0;
    }

//    public boolean isAvailable(int mask) {
//        return (bitmap & mask) == 0;
//    }
}
