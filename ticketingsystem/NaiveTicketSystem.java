package ticketingsystem;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

public class NaiveTicketSystem implements TicketingSystem {
    private final int routeNum, coachNum, seatNumPerCoach, stationNum, threadNum;
    private final AtomicLong ticketId = new AtomicLong(0);
    private final int routeCapacity;
    private final int[][] routeTicketCount;
    ReentrantLock[][] routeLock;
    public final ArrayList<ConcurrentHashMap<Long, Ticket>> soldTickets;

    public NaiveTicketSystem(int routeNum, int coachNum, int seatNum, int stationNum, int threadNum) {
        this.routeNum = routeNum;
        this.coachNum = coachNum;
        this.stationNum = stationNum;
        this.threadNum = threadNum;
        this.seatNumPerCoach = seatNum;

        this.routeCapacity = coachNum * seatNumPerCoach;
        this.routeTicketCount = new int[routeNum + 1][stationNum + 1];
        this.routeLock = new ReentrantLock[routeNum + 1][stationNum + 1];
        for (int i = 1; i <= routeNum; ++i) {
            for (int j = 1; j <= stationNum; ++j) {
                this.routeLock[i][j] = new ReentrantLock();
                routeTicketCount[i][j] = this.routeCapacity;
            }
        }
        this.soldTickets = new ArrayList<>(routeNum + 1);
        for (int i = 0; i <= routeNum; ++i) {
            this.soldTickets.add(new ConcurrentHashMap<>());
        }
    }

    boolean isValidTicket(Ticket ticket) {
        if (ticket == null) {
            System.out.println("Null ticket!");
            return false;
        } else if (!(ticket.route > 0 && ticket.route <= routeNum && ticket.departure > 0 && ticket.departure <= stationNum &&
                ticket.arrival > 0 && ticket.arrival <= stationNum && ticket.departure < ticket.arrival)) {
            System.out.println("Invalid ticket config!");
            return false;
        } else {
            return true;
        }
    }

    void registerSoldTicket(Ticket ticket) {
        soldTickets.get(ticket.route).put(ticket.tid, ticket);
    }

    void removeSoldTicket(int route, long tid) {
        Ticket ticket = soldTickets.get(route).get(tid);
        if (ticket != null) {
            soldTickets.get(route).remove(tid);
        }
    }

    Ticket querySoldTicket(int route, long tid) {
        return soldTickets.get(route).get(tid);
    }

    @Override
    public Ticket buyTicket(String passenger, int route, int departure, int arrival) {
        if (departure <= 0 || arrival > stationNum || route <= 0 || route > routeNum || departure >= arrival) {
            System.out.println("Trying to buy an invalid ticket!");
            return null;
        }
        for (int i = departure; i < arrival; ++i) {
            routeLock[route][i].lock();
        }
        Ticket ticket;
        int remain = inquiry(route, departure, arrival);
        if (remain == 0) {
            ticket = null;
        } else {
            for (int i = departure; i < arrival; ++i) {
                routeTicketCount[route][i]--;
            }
            int coach = 1 + (remain - 1) / seatNumPerCoach;
            int seat = 1 + (remain - 1) % seatNumPerCoach;
            ticket = TicketUtility.createTicket(ticketId.getAndIncrement(), passenger, route, coach, seat, departure, arrival);
            registerSoldTicket(ticket);
        }
        for (int i = departure; i < arrival; ++i) {
            routeLock[route][i].unlock();
        }
        return ticket;
    }

    @Override
    public int inquiry(int route, int departure, int arrival) {
        // We allow inquiry has inaccurate results
        // So we do not lock while inquiring
        int remain = routeCapacity + 1;
        for (int i = departure; i < arrival; ++i) {
            remain = Math.min(remain, routeTicketCount[route][i]);
        }
        return remain;
    }

    @Override
    public boolean refundTicket(Ticket ticket) {
        if (isValidTicket(ticket)) {
            for (int i = ticket.departure; i < ticket.arrival; ++i) {
                routeLock[ticket.route][i].lock();
            }
            boolean success;
            Ticket soldTicket = querySoldTicket(ticket.route, ticket.tid);
            if (soldTicket == null) {
                System.out.println("Cannot match any sold tickets!");
                TicketUtility.printTicket(ticket);
                success = false;
            } else if (!TicketUtility.isSameTicket(ticket, soldTicket, true)) {
                System.out.println("Not the same with already sold ticket!");
                TicketUtility.printTicket(ticket);
                TicketUtility.printTicket(soldTicket);
                success = false;
            } else {
                for (int i = ticket.departure; i < ticket.arrival; ++i) {
                    routeTicketCount[ticket.route][i]++;
                }
                removeSoldTicket(ticket.route, ticket.tid);
                success = true;
            }

            for (int i = ticket.departure; i < ticket.arrival; ++i) {
                routeLock[ticket.route][i].unlock();
            }
            return success;
        } else {
            return false;
        }
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
