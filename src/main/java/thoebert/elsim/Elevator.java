package thoebert.elsim;

import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeMap;

/**
 * An elevator fulfills requests along its directional ride by picking up waiting requests at their
 * origins and dropping requests at their destinations. If no requests could be fulfilled in its current
 * moving direction the elevator changes direction and continues.
 */
public class Elevator extends Thread {

    /**
     * Delay time in milliseconds if somebody is entering/exiting
     */
    static long stopTime = 5000;
    /**
     * Delay time in milliseconds the elevator takes for one floor
     */
    static long travelTime = 1000;
    static long idCount = 0;
    private final String id;
    private int currentFloor;
    private int currentDirection;
    private TreeMap<Integer, Set<Request>> waitingRequests; // queues all waiting requests grouped by their origins (key)
    private TreeMap<Integer, Set<Request>> loadedRequests; // queues all currently loaded requests grouped by their destinations (key)

    /**
     * Creates a new elevator with an custom ID
     *
     * @param id the identifier of the elevator
     */
    public Elevator(String id) {
        this.id = id;
        this.currentFloor = 0;
        this.currentDirection = 1;
        this.waitingRequests = new TreeMap<>();
        this.loadedRequests = new TreeMap<>();
    }

    /**
     * Creates a new elevator with an incrementing ID including an 'E' as Prefix
     */
    public Elevator() {
        this("E" + (idCount++));
    }

    /**
     * Adds an request to the map of requests, by adding it to the set of values which is created if needed
     *
     * @param requests   the map of requests
     * @param newKey     the key of the new request
     * @param newRequest the new request to add
     * @return the map of requests including the new request
     */
    protected static TreeMap<Integer, Set<Request>> addRequest(TreeMap<Integer, Set<Request>> requests,
                                                               int newKey, Request newRequest) {
        Set<Request> set = requests.get(newKey);
        if (set == null) {
            set = new HashSet<>(newKey);
            requests.put(newKey, set);
        }
        set.add(newRequest);
        return requests;
    }

    /**
     * Adds new requests to the map of loadedrequests (by its destination as key)
     *
     * @param newRequests    the requests to add
     * @param loadedRequests the map of requests
     * @return the map of loadedrequests including the new requests
     */
    protected static TreeMap<Integer, Set<Request>> addLoadedRequests(Set<Request> newRequests,
                                                                      TreeMap<Integer, Set<Request>> loadedRequests) {
        if (newRequests != null) {
            for (Request r : newRequests) {
                loadedRequests = addRequest(loadedRequests, r.getDestination(), r);
            }
        }
        return loadedRequests;
    }

    /**
     * Calculates the duration until all waiting and loaded requests are fulfilled. This is accomplished by
     * virtually moving the elevator from stop to stop.
     *
     * @param currentFloor     the current floor the virtual elevator
     * @param currentDirection the current moving direction of the virtual elevator
     * @param waitingRequests  the next requests, grouped by the origin (=key)
     * @param loadedRequests   the currently loaded requests, grouped by the destination (=key)
     * @return
     */
    protected static double calcDuration(int currentFloor, int currentDirection,
                                         TreeMap<Integer, Set<Request>> waitingRequests,
                                         TreeMap<Integer, Set<Request>> loadedRequests) {
        int numStops = 0;
        int numFloors = 0;
        Integer nextStop = 0;
        while (nextStop != null) {
            nextStop = getNextStop(currentFloor, currentDirection,
                    waitingRequests, loadedRequests);
            if (nextStop == null) { // no requests to fulfill along current direction? -> change direction
                currentDirection *= -1;
                nextStop = getNextStop(currentFloor, currentDirection,
                        waitingRequests, loadedRequests);
            }
            if (nextStop != null) {
                numStops++;
                numFloors += Math.abs(currentFloor - nextStop.intValue()); // number of floors since last stop
                currentFloor = nextStop.intValue();
                // delete exiting requests, transfer entering requests
                loadedRequests.remove(currentFloor);
                Set<Request> waitingRequestsAtFloor = waitingRequests.remove(currentFloor);
                loadedRequests = addLoadedRequests(waitingRequestsAtFloor, loadedRequests);
            }
        }
        return numStops * stopTime + numFloors * travelTime;
    }

    /**
     * Calculates the floor number where the virtual elevator would have to stop next. This also includes the current
     * floor, if, for example, a second request from the same floor is added after the first is already transfered. If
     * no other request could be fulfilled (picked up at origin or dropped at destination) in the current direction,
     * null is returned.
     *
     * @param currentFloor     the current floor of the virtual elevator
     * @param currentDirection the current direction of the virtual elevator
     * @param waitingRequests  the next requests, grouped by the origin (=key)
     * @param loadedRequests   the currently loaded requests, grouped by the destination (=key)
     * @return the next floor where the virtual elevator would have to stop, including the current floor, or null if
     * no other request could be fulfilled in the current direction
     */
    protected static Integer getNextStop(int currentFloor, int currentDirection,
                                         TreeMap<Integer, Set<Request>> waitingRequests,
                                         TreeMap<Integer, Set<Request>> loadedRequests) {
        if (waitingRequests.containsKey(currentFloor)) return currentFloor;
        if (loadedRequests.containsKey(currentFloor)) return currentFloor;
        Integer nextWaiting = null;
        Integer nextLoaded = null;
        if (currentDirection > 0) {
            nextWaiting = waitingRequests.higherKey(currentFloor); // if going up, the next stop must be a higher number
            nextLoaded = loadedRequests.higherKey(currentFloor);
        } else {
            nextWaiting = waitingRequests.lowerKey(currentFloor); // if going down, the next stop must be a lower number
            nextLoaded = loadedRequests.lowerKey(currentFloor);
        }
        if (nextWaiting != null && nextLoaded != null) {
            return currentDirection > 0 ?
                    Math.min(nextWaiting, nextLoaded) : // if going up the closer stop of two floors is the smaller number (min)
                    Math.max(nextWaiting, nextLoaded); // if going down the closer stop of two foors is the higher number (max)
        } else {
            return nextWaiting != null ? nextWaiting : nextLoaded; // one or two nextStops are null, return the non-null one if possible
        }
    }

    /**
     * Estimates the total optimization costs if the given request would be fulfilled.
     * <p>
     * Currently this costs equal the currently needed time to finish plus the additional time needed if the new Request
     * would be fulfilled.
     *
     * @param request the new request
     * @return the costs if the given request would be accepted
     */
    public synchronized double estimateCosts(Request request) {
        double finishingTime = calcDuration(this.currentFloor, this.currentDirection,
                new TreeMap<>(this.waitingRequests), new TreeMap<>(this.loadedRequests));
        // HINT: future versions could cache the finishing time

        TreeMap<Integer, Set<Request>> extendedWaitingRequests = new TreeMap<>(this.waitingRequests);
        extendedWaitingRequests = addRequest(extendedWaitingRequests, request.getOrigin(), request);

        double additionalTime = calcDuration(this.currentFloor, this.currentDirection,
                extendedWaitingRequests, new TreeMap<>(this.loadedRequests)) - finishingTime;
        // HINT: future versions should scale the costs of additionalTime with the workload of the whole system to get
        // interpolate between local and global soluation. For exampe, if most of the elevators are empty,
        // the finishing time should have a higher impact to enable each request being immediately receiving its own
        // elevator (local optimization), not an elevator which easily can fulfill this request along its way.
        // If most of the elevators are busy, the additional time should have a higher
        // which results in globally more efficient rides by focusing on grouping of neighboring requests/requests along
        // its way.
        return finishingTime + additionalTime;
    }

    /**
     * Adds the Request to the queue to be fulfilled in the future
     *
     * @param request the new request
     */
    public synchronized void scheduleRequest(Request request) {
        log("Accepted " + request);
        this.waitingRequests = addRequest(this.waitingRequests, request.getOrigin(), request);
        this.notify();
    }

    @Override
    public void run() {
        // CONCURRENCY:
        // Scheduler in Main-Thread should be able to concurrently estimate and schedule requests, while
        // Elevator is moving. Thread.sleep() should not block Main-Thread, therefore access of shared
        // memory is synchronized in all critical parts.
        try {
            while (!this.isInterrupted()) {
                Integer nextStop;
                synchronized (this) {
                    nextStop = getNextStop(currentFloor, currentDirection,
                            waitingRequests, loadedRequests);
                    if (nextStop == null) { // no requests to fulfill along current direction? -> change direction
                        currentDirection *= -1;
                        nextStop = getNextStop(currentFloor, currentDirection,
                                waitingRequests, loadedRequests);
                    }
                    if (nextStop == null) { // no requests to fulfill in both directions? -> nothin to do
                        log("suspended");
                        this.wait(); // wait until notified in scheduleRequest()
                        log("started");
                    }
                }
                if (nextStop != null) {
                    boolean stoppingAtFloor = true;
                    if (currentFloor != nextStop) { // currentFloor && currentDirection is only written in this Thread no synchronization needed
                        currentFloor += currentDirection;
                        Thread.sleep(travelTime);
                    }
                    synchronized (this) {
                        // delete exiting requests, transfer entering requests
                        Set<Request> loading = waitingRequests.remove(currentFloor);
                        Set<Request> unloading = loadedRequests.remove(currentFloor);
                        loadedRequests = addLoadedRequests(loading, loadedRequests);
                        stoppingAtFloor = loading != null || unloading != null; // only stop at floor if someone needs to get in/out
                        if (stoppingAtFloor) {
                            if (loading != null) for (Request r : loading) log("+" + r);
                            if (unloading != null) for (Request r : unloading) log("-" + r);
                        }
                    }
                    if (stoppingAtFloor) Thread.sleep(stopTime);
                }
            }
        } catch (InterruptedException e) {
            log("shutdown");
        }
    }

    private void log(String message) {
        System.out.format("%1$tH:%1$tM:%1$tS.%1$tL %2$s @%3$2d: %4$s\n", Calendar.getInstance(), this.id, this.currentFloor, message);
    }
}