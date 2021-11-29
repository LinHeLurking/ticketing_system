package ticketingsystem;

public class SeatGroup {
    int firstTicketId, size;
    private final SegmentTree seats;

    public SeatGroup(int firstTicketId, int lastTicketId, int stationNum) {
        this.firstTicketId = firstTicketId;
        this.size = lastTicketId - firstTicketId + 1;
        seats = new SegmentTree(1, stationNum, size, SegmentTree.MIN);
    }

    /*
     * Reserve a slot in group.
     *
     * Return the index over all groups(, which means firstTicketId is added).
     * -1 is returned if no available seat;
     * */
    synchronized int tryReserve(int departure, int arrival) {
        int remain = seats.query(departure, arrival);
        if (remain == 0) {
            return -1;
        } else {
            seats.update(departure, arrival, -1);
            return firstTicketId + remain - 1;
        }
    }

    /*
     * Free a slot in group.
     * No check is applied.
     * */
    synchronized void free(int departure, int arrival) {
        seats.update(departure, arrival, 1);
    }

    /*
     * Return the remaining slot within interval.
     * */
    synchronized int query(int departure, int arrival) {
        return seats.query(departure, arrival);
    }
}
