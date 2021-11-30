package ticketingsystem;

import java.util.concurrent.ThreadLocalRandom;

public class TicketSystemUtility {
    public static Ticket createTicket(
            long tid,
            String passenger,
            int route,
            int coach,
            int seat,
            int departure,
            int arrival) {
        Ticket ticket = new Ticket();
        ticket.tid = tid;
        ticket.passenger = passenger;
        ticket.route = route;
        ticket.coach = coach;
        ticket.seat = seat;
        ticket.departure = departure;
        ticket.arrival = arrival;
        return ticket;
    }

    public static boolean isSameTicket(Ticket a, Ticket b, boolean careID) {
        boolean flag = a.route == b.route &&
                a.coach == b.coach &&
                a.seat == b.seat &&
                a.departure == b.departure &&
                a.arrival == b.arrival &&
                a.passenger.equals(b.passenger);
        if (careID) {
            flag &= a.tid == b.tid;
        }
        return flag;
    }

    public static void printTicket(Ticket ticket) {
        if (ticket == null) {
            System.out.print("null\n");
        } else {
            System.out.format("{tid=%d, passenger=%s, route=%d, coach=%d, seat=%d, departure=%d, arrival=%d}\n",
                    ticket.tid, ticket.passenger, ticket.route, ticket.coach,
                    ticket.seat, ticket.departure, ticket.arrival);
        }
    }

    public static void randomShuffle(int[] arr) {
        // https://en.wikipedia.org/wiki/Fisher%E2%80%93Yates_shuffle#The_modern_algorithm
        for (int i = arr.length - 1; i > 0; --i) {
            int j = ThreadLocalRandom.current().nextInt(i + 1);
            int tmp = arr[i];
            arr[i] = arr[j];
            arr[j] = tmp;
        }
    }
}

