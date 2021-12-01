package ticketingsystem;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class TicketingDS implements TicketingSystem {
    private final int routeNum, coachNum, seatNumPerCoach, stationNum, threadNum, routeCapacity;

    private final ConcurrentHashMap<Long, Ticket>[] soldTickets;
    private final ConcurrentInterval[][] seatStatus;
    //    private final AtomicLong[] ticketIdCounter;
    private final ThreadLocal<Integer>[] ticketIdCounter;

    public TicketingDS(int routeNum, int coachNum, int seatNumPerCoach, int stationNum, int threadNum) {
        // Verify parameters
        if (routeNum <= 0 || coachNum <= 0 || seatNumPerCoach <= 0 || stationNum <= 0 || threadNum <= 0) {
            throw new RuntimeException("Invalid parameter for TicketingDS");
        }

        // Set fields
        this.routeNum = routeNum;
        this.coachNum = coachNum;
        this.seatNumPerCoach = seatNumPerCoach;
        this.stationNum = stationNum;
        this.threadNum = threadNum;
        this.routeCapacity = coachNum * seatNumPerCoach;

        // Init internal members
        this.soldTickets = new ConcurrentHashMap[routeNum + 1];
        for (int i = 0; i <= routeNum; ++i) {
            this.soldTickets[i] = new ConcurrentHashMap<>();
        }

//        ticketIdCounter = new AtomicLong[routeNum + 1];
//        for (int r = 1; r <= routeNum; ++r) {
//            ticketIdCounter[r] = new AtomicLong(r);
//        }
        ticketIdCounter = new ThreadLocal[routeNum + 1];
        for (int r = 1; r <= routeNum; ++r) {
            int finalR = r;
            ticketIdCounter[r] = new ThreadLocal<Integer>() {
                @Override
                protected Integer initialValue() {
                    return (int) (finalR * threadNum + Thread.currentThread().getId() % threadNum);
                }
            };
        }

        seatStatus = new ConcurrentInterval[routeNum + 1][];
        for (int r = 1; r <= routeNum; ++r) {
            seatStatus[r] = new ConcurrentInterval[routeCapacity];
            for (int s = 0; s < routeCapacity; ++s) {
                seatStatus[r][s] = new ConcurrentInterval(stationNum + 1);
            }
        }
    }

    private boolean invalidParameter(int route, int departure, int arrival) {
        return route <= 0 || route > routeNum ||
                departure <= 0 || departure > stationNum ||
                arrival <= 0 || arrival > stationNum ||
                departure >= arrival;
    }

    private long getUniqueTicketId(int route) {
        int oldVal = ticketIdCounter[route].get();
        int newVal = oldVal + routeNum * threadNum;
        ticketIdCounter[route].set(newVal);
        return oldVal;
    }

    @Override
    public Ticket buyTicket(String passenger, int route, int departure, int arrival) {
        if (invalidParameter(route, departure, arrival)) {
            throw new RuntimeException("Invalid purchase parameter!");
        }
        for (int s = 0; s < routeCapacity; ++s) {
            if (seatStatus[route][s].tryReserve(departure, arrival)) {
                long tid = getUniqueTicketId(route);
                int coachId = 1 + s / seatNumPerCoach;
                int seatId = 1 + s % seatNumPerCoach;
                Ticket ticket = TicketUtility.createTicket(tid, passenger, route, coachId, seatId, departure, arrival);
                soldTickets[route].put(tid, ticket);
                return ticket;
            }
        }
        return null;
    }

    @Override
    public int inquiry(int route, int departure, int arrival) {
        // Verify parameter
        if (invalidParameter(route, departure, arrival)) {
            throw new RuntimeException("Invalid inquiry parameter!");
        }
        int remain = 0;
        RandomTraverse order = new RandomTraverse(routeCapacity);
        for (int s = 0; s < routeCapacity; ++s) {
            if (seatStatus[route][order.next()].isAvailable(departure, arrival)) {
                remain++;
            }
        }
        return remain;
    }

    @Override
    public boolean refundTicket(Ticket ticket) {
        if (ticket == null || invalidParameter(ticket.route, ticket.departure, ticket.arrival)) {
            throw new RuntimeException("Invalid refund parameter!");
        }
        if (soldTickets[ticket.route].containsKey(ticket.tid)) {
            Ticket soldTicket = soldTickets[ticket.route].get(ticket.tid);
            if (soldTicket == null) {
                System.out.println("Cannot match any sold tickets!");
                TicketUtility.printTicket(ticket);
            } else if (!TicketUtility.isSameTicket(ticket, soldTicket, true)) {
                System.out.println("Not the same with already sold ticket!");
                TicketUtility.printTicket(ticket);
                TicketUtility.printTicket(soldTicket);
            } else {
                int s = (soldTicket.coach - 1) * seatNumPerCoach + (soldTicket.seat - 1);
                if (!seatStatus[soldTicket.route][s].tryFree(soldTicket.departure, soldTicket.arrival)) {
                    return false;
                }
                soldTickets[soldTicket.route].remove(soldTicket.tid);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean buyTicketReplay(Ticket ticket) {
        return false;
    }

    @Override
    public boolean refundTicketReplay(Ticket ticket) {
        return false;
    }
}
