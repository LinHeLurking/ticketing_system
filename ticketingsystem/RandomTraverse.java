package ticketingsystem;

import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

/*
 * Return a random permutation of [0,1,...,n-1] on demand.
 * */
public class RandomTraverse {
    private final int[] arr;
    private int last;

    public RandomTraverse(int n) {
        last = n - 1;
        arr = IntStream.range(0, n).toArray();
    }

    public int next() {
        // https://en.wikipedia.org/wiki/Fisher%E2%80%93Yates_shuffle#The_modern_algorithm
        int j = ThreadLocalRandom.current().nextInt(last + 1);
        int tmp = arr[j];
        arr[j] = arr[last];
        arr[last] = tmp;
        last -= 1;
        return tmp;
    }
}
