package thoebert.elsim;

/**
 * A request represents a future ride of one or more passengers from an origin floor to a destination floor.
 */
public class Request {

    /**
     * automatic id counter
     */
    static long idCount = 0;
    private final String id;
    private int origin;
    private int destination;

    /**
     * Creates a new request with custom ID
     *
     * @param origin      the origin floor
     * @param destination the destination floor
     * @param id          the identifier of the request
     */
    public Request(int origin, int destination, String id) {
        this.origin = origin;
        this.destination = destination;
        this.id = id;
    }

    /**
     * Creates a new request with an incrementing ID including an 'R' as prefix
     *
     * @param origin      the origin floor
     * @param destination the destination floor
     */
    public Request(int origin, int destination) {
        this(origin, destination, "R" + (idCount++));
    }

    /**
     * Returns the origin floor of the request
     *
     * @return the origin floor
     */
    public int getOrigin() {
        return origin;
    }

    /**
     * Returns the destination floor of the request
     *
     * @return the destination floor
     */
    public int getDestination() {
        return destination;
    }

    /**
     * Creates a string representation of the request in the form ID: ORIGIN>DESTINATION
     *
     * @return a string representation of the request
     */
    public String toString() {
        return id + ": " + origin + ">" + destination;
    }

}
