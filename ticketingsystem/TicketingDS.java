package ticketingsystem;

public class TicketingDS implements TicketingSystem {
    int routeNum, coachNum, seatNum, stationNum, threadNum;

    public TicketingDS(int routeNum, int coachNum, int seatNum, int stationNum, int threadNum) {
        // Verify parameters
        if (routeNum <= 0 || coachNum <= 0 || seatNum <= 0 || stationNum <= 0 || threadNum <= 0) {
            throw new RuntimeException(String.format("Invalid parameter(s) for %s\n",
                    TicketingDS.class.getSimpleName()));
        }
        // Set fields
    }


    @Override
    public Ticket buyTicket(String passenger, int route, int departure, int arrival) {
        return null;
    }

    @Override
    public int inquiry(int route, int departure, int arrival) {
        return 0;
    }

    @Override
    public boolean refundTicket(Ticket ticket) {
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
