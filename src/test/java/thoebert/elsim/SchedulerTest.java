package thoebert.elsim;

import org.junit.jupiter.api.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;


class SchedulerTest {

    public static final int TOLERANCE = 500;
    public static final int BOOST = 100;
    private ByteArrayOutputStream cbuffer;
    private PrintStream console;

    @BeforeAll
    static void setupClass() {
        Elevator.stopTime = (long) Elevator.stopTime / BOOST;
        Elevator.travelTime = (long) Elevator.travelTime / BOOST;
    }

    @AfterAll
    static void tearDownClass() {
        Elevator.stopTime = (long) Elevator.stopTime * BOOST;
        Elevator.travelTime = (long) Elevator.travelTime * BOOST;
    }

    private static void waitForElevator(int numFloors, int numStops) {
        try {
            Thread.sleep(TOLERANCE + (long) (
                    numFloors * Elevator.travelTime +
                            numStops * Elevator.stopTime));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @BeforeEach
    void setup() {
        Elevator.idCount = 0;
        Request.idCount = 0;

        console = System.out;
        cbuffer = new ByteArrayOutputStream();
        System.setOut(new PrintStream(cbuffer));
    }

    @AfterEach
    void tearDown() throws Exception {
        console.println("Rest of output:");
        getOutput();
        System.setOut(console);
    }

    private String getOutput() throws Exception {
        String output = new String(cbuffer.toByteArray());
        cbuffer.reset();
        console.print(output);
        return output;
    }

    @Test
    public void testStart() throws Exception {
        Scheduler s = new Scheduler();
        s.start(7);

        Thread.sleep(TOLERANCE);
        String output = getOutput();
        assertTrue(output.contains("E0 @ 0: suspended"));
        assertTrue(output.contains("E4 @ 0: suspended"));
        assertTrue(output.contains("E6 @ 0: suspended"));
    }

    @Test
    public void testNonStartedSystem() throws Exception {
        Scheduler s = new Scheduler();
        assertNull(s.addRequest(new Request(1, 1)));
    }

    @Test
    public void testAddSingleRequest() throws Exception {
        Scheduler s = new Scheduler();
        s.start(7);

        s.addRequest(new Request(0, 35));
        waitForElevator(35, 1);

        String output = getOutput();
        assertTrue(output.contains("@ 0: started"));
        assertTrue(output.contains("@ 0: +R0: 0>35"));
        assertTrue(output.contains("@35: -R0: 0>35"));
        assertTrue(output.contains("@35: suspended"));

        s.stop();
    }

    @Test
    public void testAddParallelRequests() throws Exception {
        Scheduler s = new Scheduler();
        s.start(7);

        s.addRequest(new Request(0, 35));
        s.addRequest(new Request(34, 0));
        waitForElevator(34 + 35, 2);

        String output = getOutput();
        assertTrue(output.contains("@ 0: started"));
        assertTrue(output.contains("@ 0: +R0: 0>35"));
        assertTrue(output.contains("@35: -R0: 0>35"));
        assertTrue(output.contains("@35: suspended"));

        assertTrue(output.contains("@ 0: started"));
        assertTrue(output.contains("@34: +R1: 34>0"));
        assertTrue(output.contains("@ 0: -R1: 34>0"));
        assertTrue(output.contains("@ 0: suspended"));

        s.stop();
    }

    @Test
    public void testStop() throws Exception {
        Scheduler s = new Scheduler();
        s.start(7);
        Thread.sleep(TOLERANCE);
        s.stop();
        Thread.sleep(TOLERANCE);
        String output = getOutput();
        assertTrue(output.contains("E0 @ 0: shutdown"));
        assertTrue(output.contains("E3 @ 0: shutdown"));
        assertTrue(output.contains("E6 @ 0: shutdown"));
    }

}