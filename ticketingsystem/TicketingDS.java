package ticketingsystem;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

public class TicketingDS implements TicketingSystem {
    private final int routeNum, coachNum, seatNumPerCoach, stationNum, threadNum, routeCapacity;

    private final ConcurrentHashMap<Integer, ConcurrentHashMap<Long, Ticket>> soldTickets;
    private final ConcurrentInterval[] routeStatus;

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
        this.soldTickets = new ConcurrentHashMap<>(routeNum + 1);
        for (int i = 0; i <= routeNum; ++i) {
            this.soldTickets.put(i, new ConcurrentHashMap<>());
        }

        routeStatus = new ConcurrentInterval[routeNum + 1];
        for (int r = 1; r <= routeNum; ++r) {
            routeStatus[r] = new ConcurrentInterval(stationNum + 1, routeCapacity);
        }
    }

    private boolean invalidParameter(int route, int departure, int arrival) {
        return route <= 0 || route > routeNum ||
                departure <= 0 || departure > stationNum ||
                arrival <= 0 || arrival > stationNum ||
                departure >= arrival;
    }

    private long getUniqueTicketId(int route, int departure, int arrival) {
        return System.nanoTime() ^ ThreadLocalRandom.current().nextLong() ^ route ^ departure ^ arrival;
    }

//    public ArrayList<Integer> reservedSeats()

    @Override
    public Ticket buyTicket(String passenger, int route, int departure, int arrival) {
        if (invalidParameter(route, departure, arrival)) {
            throw new RuntimeException("Invalid purchase parameter!");
        }
        int prior = routeStatus[route].tryReserve(departure, arrival);
        if (prior == 0) {
            return null;
        } else {
            int coach = 1 + (prior - 1) / seatNumPerCoach;
            int seat = 1 + (prior - 1) % seatNumPerCoach;
            long tid = getUniqueTicketId(route, departure, arrival);
            Ticket ticket = TicketUtility.createTicket(tid, passenger, route, coach, seat, departure, arrival);
            soldTickets.get(route).put(tid, ticket);
            return ticket;
        }
    }

    @Override
    public int inquiry(int route, int departure, int arrival) {
        // Verify parameter
        if (invalidParameter(route, departure, arrival)) {
            throw new RuntimeException("Invalid inquiry parameter!");
        }
        return routeStatus[route].countAvailable(departure, arrival);
    }

    @Override
    public boolean refundTicket(Ticket ticket) {
        if (ticket == null || invalidParameter(ticket.route, ticket.departure, ticket.arrival)) {
            throw new RuntimeException("Invalid refund parameter!");
        }
        if (soldTickets.get(ticket.route).containsKey(ticket.tid)) {
            Ticket soldTicket = soldTickets.get(ticket.route).get(ticket.tid);
            if (soldTicket == null) {
                System.out.println("Cannot match any sold tickets!");
                TicketUtility.printTicket(ticket);
            } else if (!TicketUtility.isSameTicket(ticket, soldTicket, true)) {
                System.out.println("Not the same with already sold ticket!");
                TicketUtility.printTicket(ticket);
                TicketUtility.printTicket(soldTicket);
            } else {
                routeStatus[ticket.route].cancel(ticket.departure, ticket.arrival);
                soldTickets.get(ticket.route).remove(soldTicket.tid);
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
