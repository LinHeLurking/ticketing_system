package ticketingsystem;

import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class TicketingDS implements TicketingSystem {
    private final int routeNum, coachNum, seatNumPerCoach, stationNum, threadNum, routeCapacity;

    private final ConcurrentHashMap<Long, Ticket>[] soldTickets;
    private final ConcurrentInterval[][] seatStatus;
    private final AtomicLong[] ticketIdCounter;
    private final Random random;

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

        ticketIdCounter = new AtomicLong[routeNum + 1];
        for (int r = 1; r <= routeNum; ++r) {
            ticketIdCounter[r] = new AtomicLong(r);
        }

        random = new Random();

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

    @Override
    public Ticket buyTicket(String passenger, int route, int departure, int arrival) {
        if (invalidParameter(route, departure, arrival)) {
            throw new RuntimeException("Invalid purchase parameter!");
        }
        for (int s = 0; s < routeCapacity; ++s) {
            if (seatStatus[route][s].tryReserve(departure, arrival)) {
                long tid = ticketIdCounter[route].getAndAdd(routeNum);
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
        int start = random.nextInt(routeCapacity);
        for (int s = 0; s < routeCapacity; ++s) {
            int pos = start + s;
            pos = pos >= routeCapacity ? pos - routeCapacity : pos;
            if (seatStatus[route][pos].isAvailable(departure, arrival)) {
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
                // Someone has already refunded!
                return false;
            } else if (!TicketUtility.isSameTicket(ticket, soldTicket, true)) {
                System.out.println("Not the same with already sold ticket!");
                TicketUtility.printTicket(ticket);
                TicketUtility.printTicket(soldTicket);
            } else {
                Ticket removed = soldTickets[soldTicket.route].remove(soldTicket.tid);
                if (removed != null) {
                    int s = (soldTicket.coach - 1) * seatNumPerCoach + (soldTicket.seat - 1);
                    return seatStatus[soldTicket.route][s].tryFree(soldTicket.departure, soldTicket.arrival);
                } else {
                    return false;
                }
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
