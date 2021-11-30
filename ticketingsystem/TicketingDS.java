package ticketingsystem;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

public class TicketingDS implements TicketingSystem {
    public final int routeNum, coachNum, seatNumPerCoach, stationNum, threadNum, seatNumPerGroup;

    public final SeatGroup[][] seatGroup;
    public final ArrayList<ConcurrentHashMap<Long, Ticket>> soldTickets;

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
        this.seatNumPerGroup = 10;

        // Init internal members
        int totalSeatNum = seatNumPerCoach * coachNum;
        int seatGroupNum = (totalSeatNum + seatNumPerGroup - 1) / seatNumPerGroup;
        seatGroup = new SeatGroup[routeNum + 1][seatGroupNum];

        for (int r = 1; r <= routeNum; ++r) {
            seatGroup[r] = new SeatGroup[seatGroupNum];
            for (int i = 0; i < seatGroupNum; ++i) {
                int firstIndex = i * seatNumPerGroup;
                int lastIndex = Math.min(firstIndex + seatNumPerGroup, totalSeatNum) - 1;
                seatGroup[r][i] = new SeatGroup(firstIndex, lastIndex, stationNum);
            }
        }
        this.soldTickets = new ArrayList<>(routeNum + 1);
        for (int i = 0; i <= routeNum; ++i) {
            this.soldTickets.add(new ConcurrentHashMap<>());
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

    @Override
    public Ticket buyTicket(String passenger, int route, int departure, int arrival) {
        if (invalidParameter(route, departure, arrival)) {
            throw new RuntimeException("Invalid purchase parameter!");
        }

        int[] traverseOrder = IntStream.range(0, seatGroup[route].length).toArray();
        TicketSystemUtility.randomShuffle(traverseOrder);
        Ticket ticket = null;
        for (int j : traverseOrder) {
            int id = seatGroup[route][j].tryReserve(departure, arrival - 1);
            if (id != -1) {
//                System.out.format("Successfully reserve route %d group %d from %d to %d\n", route, j, departure, arrival);
                long tid = getUniqueTicketId(route, departure, arrival);
                int coach = 1 + id / seatNumPerCoach;
                int seat = 1 + id % seatNumPerCoach;
                ticket = TicketSystemUtility.createTicket(tid, passenger, route, coach, seat, departure, arrival);
                soldTickets.get(route).put(tid, ticket);
                break;
            }
        }

        return ticket;
    }

    @Override
    public int inquiry(int route, int departure, int arrival) {
        // Verify parameter
        if (invalidParameter(route, departure, arrival)) {
            throw new RuntimeException("Invalid inquiry parameter!");
        }

        int[] traverseOrder = IntStream.range(0, seatGroup[route].length).toArray();
        TicketSystemUtility.randomShuffle(traverseOrder);
        int[] resultSeg = new int[seatGroup[route].length]; // default 0 value.
        int segNum = -1;
        for (int j : traverseOrder) {
            int[] curSeg = seatGroup[route][j].splitQuery(departure, arrival - 1);
            if (segNum == -1) {
                segNum = curSeg.length;
            }
            for (int i = 0; i < segNum; ++i) {
                resultSeg[i] += curSeg[i];
            }
        }
        int remain = Integer.MAX_VALUE;
        for (int i = 0; i < segNum; ++i) {
            remain = Math.min(remain, resultSeg[i]);
        }
        return remain;
    }

    @Override
    public boolean refundTicket(Ticket ticket) {
        if (ticket == null || invalidParameter(ticket.route, ticket.departure, ticket.arrival)) {
            throw new RuntimeException("Invalid refund parameter!");
        }
        if (!soldTickets.get(ticket.route).containsKey(ticket.tid)) {
            // Refunding ticket not sold!
            return false;
        } else {
            Ticket soldTicket = soldTickets.get(ticket.route).get(ticket.tid);
            if (!TicketSystemUtility.isSameTicket(ticket, soldTicket, true)) {
                System.out.println("Not the same with already sold ticket!");
                TicketSystemUtility.printTicket(ticket);
                TicketSystemUtility.printTicket(soldTicket);
                return false;
            } else {
                int id = (ticket.coach - 1) * seatNumPerCoach + (ticket.seat - 1);
                int gid = id / seatNumPerGroup;
                seatGroup[ticket.route][gid].free(ticket.departure, ticket.arrival - 1);
                soldTickets.get(ticket.route).remove(ticket.tid, soldTicket);

//                System.out.format("Successfully refund for route %d from %d to %d\n",
//                        ticket.route, ticket.departure, ticket.arrival);

                return true;
            }
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
