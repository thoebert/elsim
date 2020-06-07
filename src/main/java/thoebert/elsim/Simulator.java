package thoebert.elsim;

import java.util.Random;

/**
 * Simulates multiple elevators in a building by creating random requests at random times
 */
public class Simulator {

    public static final int FLOORS = 55;
    public static final int ELEVATORS = 7;
    public static final int BOOST = 10; // Boost factor for stop and travel time of the elevators

    public static final Random r = new Random();

    public static void main(String[] args) throws Exception {
        Elevator.stopTime = (long) Elevator.stopTime / BOOST;
        Elevator.travelTime = (long) Elevator.travelTime / BOOST;
        Scheduler s = new Scheduler();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down");
            s.stop();
        }));
        s.start(ELEVATORS);
        while (true) {
            s.addRequest(new Request(r.nextInt(FLOORS), r.nextInt(FLOORS)));
            Thread.sleep(r.nextInt((int) (Elevator.stopTime * 5)));
        }
    }
}
