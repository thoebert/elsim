package thoebert.elsim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static thoebert.elsim.Elevator.stopTime;
import static thoebert.elsim.Elevator.travelTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;


class ElevatorTest {

    @BeforeEach
    void resetIDCount() {
        Request.idCount = 0;
    }

    @Test
    void testAddRequest() {
        TreeMap<Integer, Set<Request>> requestsIn = asRequests(new int[][]{{4, 5}, {7, 8}}, true);
        Request newRequest = new Request(1, 2);
        TreeMap<Integer, Set<Request>> requestsOut = Elevator.addRequest(requestsIn, newRequest.getOrigin(), newRequest);
        assertEquals("{1=[R2: 1>2], 4=[R0: 4>5], 7=[R1: 7>8]}", requestsOut.toString());
    }

    @Test
    void testAddLoadedRequests() {
        TreeMap<Integer, Set<Request>> loadedRequests = asLoadedRequests(new int[][]{{6, 7}});
        TreeMap<Integer, Set<Request>> actualLoadedRequests = Elevator.addLoadedRequests(null, loadedRequests);
        assertEquals("{7=[R0: 6>7]}", actualLoadedRequests.toString());

        Set<Request> waitingRequestsAtFloor = new HashSet<Request>();
        waitingRequestsAtFloor.add(new Request(1, 2));
        waitingRequestsAtFloor.add(new Request(4, 5));
        actualLoadedRequests = Elevator.addLoadedRequests(waitingRequestsAtFloor, loadedRequests);
        assertEquals("{2=[R1: 1>2], 5=[R2: 4>5], 7=[R0: 6>7]}", actualLoadedRequests.toString());
    }

    @Test
    void testGetNextStop() {
        assertEquals(8, Elevator.getNextStop(2, 1,
                asWaitingRequests(new int[][]{{8, 10}}),
                asLoadedRequests(new int[][]{})
        ));

        assertEquals(8, Elevator.getNextStop(12, -1,
                asWaitingRequests(new int[][]{{8, 10}}),
                asLoadedRequests(new int[][]{})
        ));

        assertEquals(null, Elevator.getNextStop(10, -1,
                asWaitingRequests(new int[][]{}),
                asLoadedRequests(new int[][]{})
        ));

        assertEquals(6, Elevator.getNextStop(5, 1,
                asWaitingRequests(new int[][]{{6, 7}}),
                asLoadedRequests(new int[][]{{1, 10}})
        ));

        assertEquals(6, Elevator.getNextStop(5, 1,
                asWaitingRequests(new int[][]{{7, 8}}),
                asLoadedRequests(new int[][]{{1, 6}})
        ));
    }

    @Test
    void testCalcDurationOfWaitingRequests() {
        double actualCosts = Elevator.calcDuration(2, 1,
                asWaitingRequests(new int[][]{{8, 10}}),
                asLoadedRequests(new int[][]{})
        );
        assertEquals(8 * travelTime + 2 * stopTime, actualCosts);

        actualCosts = Elevator.calcDuration(8, -1,
                asWaitingRequests(new int[][]{{2, 0}}),
                asLoadedRequests(new int[][]{})
        );
        assertEquals(8 * travelTime + 2 * stopTime, actualCosts);
    }

    @Test
    void testCalcDurationOfLoadedRequests() {
        double actualCosts = Elevator.calcDuration(4, 1,
                asWaitingRequests(new int[][]{}),
                asLoadedRequests(new int[][]{{2, 6}})
        );
        assertEquals(2 * travelTime + 1 * stopTime, actualCosts);

        actualCosts = Elevator.calcDuration(4, -1,
                asWaitingRequests(new int[][]{}),
                asLoadedRequests(new int[][]{{6, 2}})
        );
        assertEquals(2 * travelTime + 1 * stopTime, actualCosts);
    }

    @Test
    void testCalcDurationOfWaitingAndLoadedRequests() {
        double actualCosts = Elevator.calcDuration(2, 1,
                asWaitingRequests(new int[][]{{5, 6}}),
                asLoadedRequests(new int[][]{{4, 5}})
        );
        assertEquals(4 * travelTime + 2 * stopTime, actualCosts);
    }

    @Test
    void testEstimateCostsAndScheduleRequest() {
        Elevator e = new Elevator("e1");
        Request r = new Request(1, 2);
        double actualCosts = e.estimateCosts(r);
        assertEquals(2 * travelTime + 2 * stopTime, actualCosts);

        e.scheduleRequest(r);

        actualCosts = e.estimateCosts(new Request(4, 5));
        assertEquals(5 * travelTime + 4 * stopTime, actualCosts);
    }

    private TreeMap<Integer, Set<Request>> asWaitingRequests(int[][] array) {
        return asRequests(array, true);
    }

    private TreeMap<Integer, Set<Request>> asLoadedRequests(int[][] array) {
        return asRequests(array, false);
    }

    private TreeMap<Integer, Set<Request>> asRequests(int[][] array, boolean origin) {
        TreeMap<Integer, Set<Request>> requests = new TreeMap<>();
        for (int[] record : array) {
            assert (record.length == 2);
            Request r = new Request(record[0], record[1]);
            requests = Elevator.addRequest(requests, origin ? r.getOrigin() : r.getDestination(), r);
        }
        return requests;
    }


}