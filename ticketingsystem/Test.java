package ticketingsystem;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Random;
import java.util.concurrent.*;
import java.util.stream.IntStream;

public class Test {

    protected static final int BUY = 0, REFUND = 1, QUERY = 2;

    private static void testShuffleUtility() {
        int repeatTimes = 1000;
        boolean flag = true;
        for (int i = 0; i < repeatTimes && flag; ++i) {
            int n = ThreadLocalRandom.current().nextInt(1000) + 5;
            int[] arr = IntStream.range(0, n).toArray();
            int[] arr_cpy = new int[n];
            System.arraycopy(arr, 0, arr_cpy, 0, n);
            TicketSystemUtility.randomShuffle(arr);
            Arrays.sort(arr);
            Arrays.sort(arr_cpy);
            for (int j = 0; j < n && flag; ++j) {
                if (arr[j] != arr_cpy[j]) {
                    System.out.println("Error shuffle!");
                    flag = false;
                }
            }
        }
        if (flag) {
            System.out.println("Correct shuffle!\n");
        }
    }

    private static void testSegmentTree() {
        int repeatTimes = 1000;
        int segLength = 20;
        int initVal = 10;
        Random random = new Random();
        boolean flag = true;
        for (int rd = 0; rd < repeatTimes && flag; ++rd) {
            SegmentTree seg = new SegmentTree(1, segLength, initVal);
            int[] arr = new int[segLength + 1];
            for (int i = 1; i <= segLength; ++i) {
                arr[i] = initVal;
            }
            int opNum = 1000;
            for (int op = 0; op < opNum; ++op) {
                int x = random.nextInt(segLength) + 1;
                int y = random.nextInt(segLength) + 1;
                if (x > y) {
                    int tmp = x;
                    x = y;
                    y = tmp;
                }
                int v = -10 + random.nextInt(2 * 10);
                seg.update(x, y, v);
                for (int i = x; i <= y; ++i) {
                    arr[i] += v;
                }
            }
            for (int i = 1; i <= segLength; ++i) {
                for (int j = i; j <= segLength; ++j) {
                    int seg_res = seg.query(i, j);
                    int arr_res = Integer.MAX_VALUE;
                    for (int k = i; k <= j; ++k) {
                        arr_res = Math.min(arr_res, arr[k]);
                    }
                    if (seg_res != arr_res) {
                        flag = false;
                    }
                }
            }
        }
        if (flag) {
            System.out.println("Correct SegmentTree!\n");
        }
    }

    private static int getRandomOpType() {
        int opType;
        int rV = ThreadLocalRandom.current().nextInt(100);
        if (rV < 20) {
            opType = BUY;
        } else if (rV < 20 + 10) {
            opType = REFUND;
        } else {
            opType = QUERY;
        }
        return opType;
    }

    private static boolean singleThreadTest(TicketingSystem systemA, TicketingSystem systemB,
                                            int routeNum, int stationNum,
                                            int repeatTimes, boolean ignoreError) {
        Random random = new Random();
        boolean flag = true;
        long threadId = Thread.currentThread().getId();
        LinkedList<Ticket> aBought = new LinkedList<>(), bBought = new LinkedList<>();
        for (int testRound = 0; testRound < repeatTimes && (flag); ++testRound) {
            int opType = getRandomOpType();
            String passengerName = "TEST_USER";
            int route = random.nextInt(routeNum) + 1;
            int departure = random.nextInt(stationNum) + 1;
            int arrival = random.nextInt(stationNum) + 1;
            while (arrival == departure) {
                arrival = random.nextInt(stationNum) + 1;
            }
            if (arrival < departure) {
                int tmp = arrival;
                arrival = departure;
                departure = tmp;
            }
            switch (opType) {
                case BUY:
                    Ticket ticketA = systemA.buyTicket(passengerName, route, departure, arrival);
                    Ticket ticketB = systemB.buyTicket(passengerName, route, departure, arrival);
                    flag = (ticketA == null && ticketB == null) || (ticketA != null && ticketB != null);
                    if (ticketA != null && ticketB != null) {
                        aBought.add(ticketA);
                        bBought.add(ticketB);
                    }
                    if (!flag) {
                        System.out.format("Thread %d -- Error when testing at %d\n", threadId, testRound);
                        System.out.format("Thread %d -- Buy: passenger=%s, route=%d, departure=%d, arrival=%d\n",
                                threadId, passengerName, route, departure, arrival);
                        TicketSystemUtility.printTicket(ticketA);
                        TicketSystemUtility.printTicket(ticketB);
                    }
                    break;
                case REFUND:
                    if (!aBought.isEmpty() && !bBought.isEmpty()) {
                        int ind = random.nextInt(aBought.size());
                        Ticket ticketABought = aBought.get(ind);
                        Ticket ticketBBought = bBought.get(ind);
                        boolean aSuccess = systemA.refundTicket(ticketABought);
                        boolean bSuccess = systemB.refundTicket(ticketBBought);
                        aBought.remove(ind);
                        bBought.remove(ind);
                        flag = (aSuccess == bSuccess);
                        if (!flag) {
                            System.out.format("Thread %d -- Error when testing at %d\n", threadId, testRound);
                            System.out.format("Thread %d -- Refund:\n", threadId);
                            System.out.format("Thread %d -- A: %b, B: %b\n", threadId, aSuccess, bSuccess);
                            System.out.format("Thread %d -- Query for now:\n", threadId);
                            System.out.format("Thread %d -- A: %d, B: %d\n",
                                    threadId,
                                    systemA.inquiry(ticketABought.route, ticketABought.departure, ticketABought.arrival),
                                    systemB.inquiry(ticketBBought.route, ticketBBought.departure, ticketBBought.arrival));
                            TicketSystemUtility.printTicket(ticketABought);
                            TicketSystemUtility.printTicket(ticketBBought);
                        }
                    }
                    break;
                case QUERY:
                    int aResult = systemA.inquiry(route, departure, arrival);
                    int bResult = systemB.inquiry(route, departure, arrival);
                    flag = (aResult == bResult) || ignoreError;
                    if (!flag) {
                        System.out.format("Thread %d -- Error when testing at %d\n", threadId, testRound);
                        System.out.format("Thread %d -- Query: route=%d, departure=%d, arrival=%d\n",
                                threadId, route, departure, arrival);
                        System.out.format("Thread %d -- A: %d, B: %d\n", threadId, aResult, bResult);
//                        for (int i = departure; i < arrival; ++i) {
//                            int x = systemA.inquiry(route, i, i + 1);
//                            int y = systemB.inquiry(route, i, i + 1);
//                            System.out.format("%d - %d, A: %d, B: %d\n", i, i + 1, x, y);
//                        }
                    }
                    break;
                default: // Impossible
                    break;
            }
            // Check all internal intervals
//            for (int i = 1; i < stationNum && flag; ++i) {
//                for (int j = i + 1; j < stationNum && flag; ++j) {
//                    int x = systemA.inquiry(route, i, j);
//                    int y = systemB.inquiry(route, i, j);
//                    if (x != y) {
////                        System.out.format("Thread %d -- Error when testing at %d\n", threadId, testRound);
//                        System.out.format("Error pos: %d - %d, A: %d, B: %d (route: %d, round: %d)\n",
//                                i, j, x, y, route, testRound);
//                        System.out.println("Operation: " + opType);
//                        flag = false;
//                    }
//                }
//            }
        }
        return flag;
    }

    private static void singleThreadPerf(TicketingSystem system, int routeNum, int stationNum, int checkTimes) {
        Random random = new Random();
        LinkedList<Ticket> boughtTickets = new LinkedList<>();
        for (int i = 0; i < checkTimes; ++i) {
            int opType = getRandomOpType();
            String passengerName = "PERF_USER";
            int route = random.nextInt(routeNum) + 1;
            int departure = random.nextInt(stationNum) + 1;
            int arrival = random.nextInt(stationNum) + 1;
            while (arrival == departure) {
                arrival = random.nextInt(stationNum) + 1;
            }
            if (departure > arrival) {
                int tmp = departure;
                departure = arrival;
                arrival = tmp;
            }
            switch (opType) {
                case BUY:
                    Ticket ticket = system.buyTicket(passengerName, route, departure, arrival);
                    if (ticket != null) {
                        boughtTickets.add(ticket); // Don't add if it fails.
                    }
                    break;
                case REFUND:
                    if (!boughtTickets.isEmpty()) {
                        int cnt = random.nextInt(boughtTickets.size());
                        Ticket boughtTicket = boughtTickets.remove(cnt);
                        system.refundTicket(boughtTicket);
                    }
                    break;
                case QUERY:
                    system.inquiry(route, departure, arrival);
                    break;
                default: // Impossible
                    break;
            }
        }
    }

    private static boolean compareTicketSystemSequential(TicketingSystem systemA, TicketingSystem systemB,
                                                         int routeNum, int stationNum, int repeatTimes) {
        System.out.println("Testing sequential correctness...");
        System.out.format("A=%s\n", systemA.getClass().getSimpleName());
        System.out.format("B=%s\n", systemB.getClass().getSimpleName());

        boolean flag = singleThreadTest(systemA, systemB, routeNum, stationNum, repeatTimes, false);
        System.out.println("Inquiry results after sequential tests...");
        for (int route = 1; route <= routeNum; ++route) {
            for (int departure = 1; departure < stationNum; ++departure) {
                int arrival = departure + 1;
                int resA = systemA.inquiry(route, departure, arrival);
                int resB = systemB.inquiry(route, departure, arrival);
                if (resA != resB) {
                    System.out.format("route: %d, departure: %d, arrival: %d, A: %d, B: %d\n",
                            route, departure, arrival, resA, resB);
                }
                flag &= resA == resB;
            }
        }
        return flag;
    }

    private static boolean compareTicketSystemConcurrent(TicketingSystem systemA, TicketingSystem systemB,
                                                         int threadNum, int routeNum, int stationNum, int repeatTimes)
            throws InterruptedException {
        System.out.println("Testing concurrent correctness...");
        System.out.format("A=%s\n", systemA.getClass().getSimpleName());
        System.out.format("B=%s\n", systemB.getClass().getSimpleName());

        ExecutorService executor = Executors.newCachedThreadPool();
        for (int i = 0; i < threadNum; ++i) {
            executor.execute(() ->
                    singleThreadTest(systemA, systemB, routeNum, stationNum, repeatTimes, true));
        }
        executor.shutdown();
        if (!executor.awaitTermination(40, TimeUnit.SECONDS)) {
            System.out.println("Timeout when waiting concurrent tests finish.");
            return false;
        }

        boolean flag = true;
        System.out.println("Inquiry results after concurrent tests...");
        for (int route = 1; route <= routeNum; ++route) {
            for (int departure = 1; departure < stationNum; ++departure) {
                int arrival = departure + 1;
                int resA = systemA.inquiry(route, departure, arrival);
                int resB = systemB.inquiry(route, departure, arrival);
                if (resA != resB) {
                    System.out.format("route: %d, departure: %d, arrival: %d, A: %d, B: %d\n",
                            route, departure, arrival, resA, resB);
                }
                flag &= resA == resB;
            }
        }
        return flag;
    }

    private static void testSequential(int routeNum, int coachNum, int seatNum, int stationNum, int threadNum) {
        TicketingSystem naiveDS;
        TicketingSystem tds = new TicketingDS(routeNum, coachNum, seatNum, stationNum, threadNum);
        final LinkedList<TicketingSystem> systems = new LinkedList<>();
        systems.add(tds);

        int repeatTimes = 100000;
        boolean sequentialResult;
        for (TicketingSystem system : systems) {
            naiveDS = new NaiveTicketSystem(routeNum, coachNum, seatNum, stationNum, threadNum);
            sequentialResult = compareTicketSystemSequential(naiveDS, system, routeNum, stationNum, repeatTimes);
            if (sequentialResult) {
                System.out.println("[YES]");
                System.out.format("Our %s has the same results with naive baseline!\n\n"
                        , system.getClass().getSimpleName());
            } else {
                System.out.println("[No]");
                System.out.format("Our %s has different results with naive baseline!\n\n"
                        , system.getClass().getSimpleName());
            }
        }
    }

    private static void testConcurrent(int routeNum, int coachNum, int seatNum, int stationNum, int threadNum)
            throws InterruptedException {
        TicketingSystem naiveDS;
        TicketingSystem tds = new TicketingDS(routeNum, coachNum, seatNum, stationNum, threadNum);
        final LinkedList<TicketingSystem> systems = new LinkedList<>();
        systems.add(tds);

        int repeatTimes = 100000;
        boolean concurrentResult;
        for (TicketingSystem system : systems) {
            naiveDS = new NaiveTicketSystem(routeNum, coachNum, seatNum, stationNum, threadNum);
            concurrentResult =
                    compareTicketSystemConcurrent(naiveDS, system, threadNum, routeNum, stationNum, repeatTimes);
            if (concurrentResult) {
                System.out.println("[YES]");
                System.out.format("Our %s has the same results with naive baseline!\n\n",
                        system.getClass().getSimpleName());
            } else {
                System.out.println("[No]");
                System.out.format("Our %s has different results with naive baseline!\n\n",
                        system.getClass().getSimpleName());
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        testShuffleUtility();
        testSegmentTree();

        int routeNum = 10, coachNum = 10, seatNum = 100, stationNum = 20, threadNum = 6;
        testSequential(routeNum, coachNum, seatNum, stationNum, threadNum);
//        testConcurrent(routeNum, coachNum, seatNum, stationNum, threadNum);
    }
}
