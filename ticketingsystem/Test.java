package ticketingsystem;

import java.util.LinkedList;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Test {

    protected static final int BUY = 0, REFUND = 1, QUERY = 2;

    private static int getRandomOpType(Random random) {
        int opType;
        int rV = random.nextInt(100);
        if (rV < 20) {
            opType = BUY;
        } else if (rV < 30) {
            opType = REFUND;
        } else {
            opType = QUERY;
        }
        return opType;
    }

    private static boolean singleThreadTest(TicketingSystem systemA, TicketingSystem systemB,
                                            int checkTimes, int routeNum, int stationNum, boolean ignoreError) {
        Random random = new Random();
        boolean flag = true;
        long threadId = Thread.currentThread().getId();
        LinkedList<Ticket> aBought = new LinkedList<>(), bBought = new LinkedList<>();
        for (int i = 0; i < checkTimes && (flag); ++i) {
            int opType = getRandomOpType(random);
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
                        System.out.format("Thread %d -- Error when testing at %d\n", threadId, i);
                        System.out.format("Thread %d -- Buy: passenger=%s, route=%d, departure=%d, arrival=%d\n",
                                threadId, passengerName, route, departure, arrival);
                        TicketUtility.printTicket(ticketA);
                        TicketUtility.printTicket(ticketB);
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
                            System.out.format("Thread %d -- Error when testing at %d\n", threadId, i);
                            System.out.format("Thread %d -- Refund:\n", threadId);
                            System.out.format("Thread %d -- A: %b, B: %b\n", threadId, aSuccess, bSuccess);
                            System.out.format("Thread %d -- Query for now:\n", threadId);
                            System.out.format("Thread %d -- A: %d, B: %d\n",
                                    threadId,
                                    systemA.inquiry(ticketABought.route, ticketABought.departure, ticketABought.arrival),
                                    systemB.inquiry(ticketBBought.route, ticketBBought.departure, ticketBBought.arrival));
                            TicketUtility.printTicket(ticketABought);
                            TicketUtility.printTicket(ticketBBought);
                        }
                    }
                    break;
                case QUERY:
                    int aResult = systemA.inquiry(route, departure, arrival);
                    int bResult = systemB.inquiry(route, departure, arrival);
                    flag = (aResult == bResult) || ignoreError;
                    if (!flag) {
                        System.out.format("Thread %d -- Error when testing at %d\n", threadId, i);
                        System.out.format("Thread %d -- Query: route=%d, departure=%d, arrival=%d\n",
                                threadId, route, departure, arrival);
                        System.out.format("Thread %d -- A: %d, B: %d\n", threadId, aResult, bResult);
                    }
                    break;
                default: // Impossible
                    break;
            }
        }
        return flag;
    }

    private static boolean compareTicketSystemConcurrent(TicketingSystem systemA, TicketingSystem systemB, int checkTimes,
                                                         int threadNum, int routeNum, int stationNum)
            throws InterruptedException {
        System.out.println("Testing concurrent correctness...");
        System.out.format("A=%s\n", systemA.getClass().getSimpleName());
        System.out.format("B=%s\n", systemB.getClass().getSimpleName());

        ExecutorService executor = Executors.newCachedThreadPool();
        for (int i = 0; i < threadNum; ++i) {
            executor.execute(() -> singleThreadTest(systemA, systemB, checkTimes, routeNum, stationNum, true));
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


    private static boolean compareTicketSystemSequential(TicketingSystem systemA, TicketingSystem systemB,
                                                         int checkTimes, int routeNum, int stationNum) {
        System.out.println("Testing sequential correctness...");
        System.out.format("A=%s\n", systemA.getClass().getSimpleName());
        System.out.format("B=%s\n", systemB.getClass().getSimpleName());

        boolean flag = singleThreadTest(systemA, systemB, checkTimes, routeNum, stationNum, false);
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

    private static void testSequential(int routeNum, int coachNum, int seatNum, int stationNum, int threadNum) {
        TicketingSystem naiveDS;
        final LinkedList<TicketingSystem> systems = new LinkedList<>();
        systems.add(new TicketingDS(routeNum, coachNum, seatNum, stationNum, threadNum));

        int checkTimes = 10000;
        boolean sequentialResult;
        for (TicketingSystem system : systems) {
            naiveDS = new NaiveTicketSystem(routeNum, coachNum, seatNum, stationNum, threadNum);
            sequentialResult = compareTicketSystemSequential(naiveDS, system, checkTimes, routeNum, stationNum);
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
        final LinkedList<TicketingSystem> systems = new LinkedList<>();
        systems.add(new TicketingDS(routeNum, coachNum, seatNum, stationNum, threadNum));

        int checkTimes = 100000;
        boolean concurrentResult;
        for (TicketingSystem system : systems) {
            naiveDS = new NaiveTicketSystem(routeNum, coachNum, seatNum, stationNum, threadNum);
            concurrentResult = compareTicketSystemConcurrent(naiveDS, system, checkTimes, threadNum, routeNum, stationNum);
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
        int routeNum = 10, coachNum = 10, seatNum = 100, stationNum = 20, threadNum = 6;
        testSequential(routeNum, coachNum, seatNum, stationNum, threadNum);
        testConcurrent(routeNum, coachNum, seatNum, stationNum, threadNum);
    }
}
