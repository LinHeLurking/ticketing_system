package ticketingsystem;

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
        boolean result = true;
        long mask = ((1L << endExclusive) - 1) ^ ((1L << beginInclusive) - 1);

        if ((bitmap & mask) == 0) {
            bitmap |= mask;
        } else {
            result = false;
        }
        return result;
    }

    synchronized public boolean tryFree(int beginInclusive, int endExclusive) {
        boolean result = true;
        long mask = ((1L << endExclusive) - 1) ^ ((1L << beginInclusive) - 1);

        if ((bitmap & mask) == mask) {
            bitmap ^= mask;
        } else {
            result = false;
        }
        return result;
    }

    public boolean isAvailable(int beginInclusive, int endExclusive) {
        long mask = ((1L << endExclusive) - 1) ^ ((1L << beginInclusive) - 1);
        return (bitmap & mask) == 0;
    }
}
