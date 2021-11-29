package ticketingsystem;

import java.util.Random;

public class Test {
    public static void testSegTree() {
        int len = 20;
        SegmentTree segTree = new SegmentTree(1, len, 0, SegmentTree.MIN);
        int[] array = new int[len + 1];
        int repeat = 10000000;
        long start, end;
        Random random = new Random();
        start = System.nanoTime();
        for (int i = 0; i < repeat; ++i) {
            int x = random.nextInt(len) + 1;
            int y = random.nextInt(len) + 1;
            while (y == x) {
                y = random.nextInt(len) + 1;
            }
            segTree.update(Math.min(x, y), Math.max(x, y), 1);
        }
        end = System.nanoTime();
        System.out.format("SegmentTree(update): %f op/us\n", (double) repeat / ((end - start) / 1000));
        start = System.nanoTime();
        for (int i = 0; i < repeat; ++i) {
            int x = random.nextInt(len) + 1;
            int y = random.nextInt(len) + 1;
            while (y == x) {
                y = random.nextInt(len) + 1;
            }
            for (int j = Math.min(x, y); j <= Math.max(x, y); ++j) {
                array[j] += 1;
            }
        }
        end = System.nanoTime();
        System.out.format("Array(update): %f op/us\n", (double) repeat / ((end - start) / 1000));

        start = System.nanoTime();
        for (int i = 0; i < repeat; ++i) {
            int x = random.nextInt(len) + 1;
            int y = random.nextInt(len) + 1;
            while (y == x) {
                y = random.nextInt(len) + 1;
            }
            segTree.query(Math.min(x, y), Math.max(x, y));
        }
        end = System.nanoTime();
        System.out.format("SegmentTree(query): %f op/us\n", (double) repeat / ((end - start) / 1000));
        start = System.nanoTime();
        for (int i = 0; i < repeat; ++i) {
            int x = random.nextInt(len) + 1;
            int y = random.nextInt(len) + 1;
            while (y == x) {
                y = random.nextInt(len) + 1;
            }
            int min = Integer.MAX_VALUE;
            for (int j = Math.min(x, y); j <= Math.max(x, y); ++j) {
                min = Math.min(min, array[j]);
            }
        }
        end = System.nanoTime();
        System.out.format("Array(query): %f op/us\n", (double) repeat / ((end - start) / 1000));
    }

    public static void main(String[] args) throws InterruptedException {
        testSegTree();

//        int routeNum = 0, coachNum = 0, seatNum = 0, stationNum = 0, threadNum = 0;
//        final TicketingDS tds = new TicketingDS(routeNum, coachNum, seatNum, stationNum, threadNum);
    }
}
