package thoebert.elsim;

import java.util.ArrayList;

/**
 * A scheduler transfers requests to its connected elevators based on the minimal costs to fulfill this request.
 */
public class Scheduler {

    private ArrayList<Elevator> elevators;

    /**
     * Creates a new Scheduler with no connected Elevators
     */
    public Scheduler() {
        this.elevators = new ArrayList<>();
    }

    /**
     * Creates the given number of elevators and starts them.
     *
     * @param numElevators the number of new elevators to start
     */
    public void start(int numElevators) {
        for (int i = 0; i < numElevators; i++) {
            Elevator e = new Elevator();
            this.elevators.add(e);
            e.start();
        }
    }

    /**
     * Stops all connected elevators
     */
    public void stop() {
        for (Elevator e : this.elevators) {
            e.interrupt();
        }
    }

    /**
     * Schedules the given request to the elevator which is returning the lowest costs to fulfill this request.
     *
     * @param request the new request to fulfill
     * @return the elevator which is fulfilling the request
     */
    public Elevator addRequest(Request request) {
        double minCost = Double.MAX_VALUE;
        Elevator minElevator = null;
        for (Elevator e : this.elevators) {
            double estimatedCosts = e.estimateCosts(request);
            if (estimatedCosts < minCost) {
                minCost = estimatedCosts;
                minElevator = e;
            }
        }
        if (minElevator != null) {
            minElevator.scheduleRequest(request);
            return minElevator;
        } else {
            return null;
        }
    }
}