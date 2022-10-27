import java.util.ArrayList;
import java.util.Scanner;

/**
 * The <code>Simulator</code> class contains the main method that runs the simulation.
 * @author CodeReDarsh
 * <br>email: adarshcp2077@gmail.com
 */
public class Simulator {

    private static int totalServiceTime;   // Contains the running sum of the total time each packet is in the network.
                                        // The service time per packet is simply the time it has arrived to the Destination
                                        // minus the time when the packet was created. When a packet counter reaches 0,
                                        // dequeue it from the router queue and add the time to the total time.
                                        // Ignore the leftover Packets in the network when simulation time is up.
                                        // total service time includes the timeToDest time of each packet as well as the
                                        // time that packet waits for, in the intermediate router for when it's ready to
                                        // leave but can't since the bandwidth doesn't permit it

    private static int totalPacketsArrived;    // Contains the total number of packets that have been successfully forwarded
                                            // to the destination. When a packet counter reaches 0, dequeue it from the
                                            // router queue and increase this count by 1.

    public static int packetsDropped; // records the number of packets that have been dropped due to a congested network.
                                    // Note: this can only happen when sendPacketTo(Collection routers) throws an exception.

    public static final int MAX_PACKETS = 3;    // no of packets that can arrive int the dispatch router in a simulation
                                                // unit time.

    public static double simulate(int numIntRouters, double arrivalProb, int maxBufferSize, int minPacketSize, int maxPacketSize,
                                  int bandwidth, int duration) throws Exception {


        if(numIntRouters < 1 || arrivalProb <= 0 || arrivalProb > 1 || maxBufferSize < 1 || minPacketSize < 0 ||
                maxPacketSize < 1 || bandwidth < 1 || duration <= 0 || maxPacketSize < minPacketSize){
            System.out.println("\nNO SIMULATION");
            return 0;
        }

        /*
        EVENT 1: decide if a packet has arrived or not, max 3 can arrive, if arrived then generate new packet and store
                 in the dispatch router

        EVENT 2: check if the dispatcher contains unsent packets, if so then send them off to the intermediate routers
                 that are free. use sendPacketTo method to decide which router to send the packet to, if none of the
                 intermediate routers are free then it throw a buffer overflow exception and drop the packet
                 and increment the packetsDropped counter.

        EVENT 3: Since only the first packet in the queue can be processed while the rest remain in the queue,
                 decrement the timeToDest (which basically contains the total processing time of that packet) packet
                 counters for the packets at the beginning of the queue for each intermediate router until they reach 0.

        EVENT 4: check if any packets are ready to be sent to the destination (by seeing if their timeToDest counters are 0)
                 and if they are, do so only if bandwidth allows else keep the packets in the queue (remember that
                 time as difference of the time it arrived to the destination router minus the time it arrived at the
                 totalService time accounts for the time the packet waits in the intRouter queue by taking the processing
                 dispatch router).
                 Note regarding bandwidth: due to limited bandwidth, the destination router can only accept a limited
                 amount of packets, limit, which is determined by the user. For example, if 3 packets have been finished
                 processing by the Intermediate routers, but the limit is 2, only 2 packets can arrive at the destination
                 in a simulation unit. The third packet must arrive in the next simulation unit.
                 Also note: Your implementation must consider fairness. That is, Intermediate routers must take turns to
                 send packets. For example, if Intermediate routers 1, 2, 4, and 5 can send a packet at a given simulation
                 unit, and the bandwidth is 2, routers 1 and 2 can send their packet. If, in the next simulation unit,
                 Intermediate router 3 can also send a packet, routers 4 and 5 should send, not 3.

        EVENT 5 (this is within event 4 btw): once a packet arrives to the destination (which you do by dequeuing it
                                              from the intermediate router's queue) router, take note of its arrival by
                                              recording its total time in the network, i.e. update the totalServiceTime
                                              counter () and update the total Packets arrived counter too.
         */

        //SIMULATION VARIABLES

        // Level 1 router
        Router dispatcher = new Router();
        // Level 2 routers
        ArrayList<Router> intermediateRouters = new ArrayList<>();
        intermediateRouters.add(null);
        for (int i = 1; i <= numIntRouters; i++) {
            intermediateRouters.add(new Router());
        }
        totalServiceTime = 0;
        totalPacketsArrived = 0;
        packetsDropped = 0;
        ArrayList<Integer> rtrPriority = new ArrayList<>();
        
        //SIMULATION LOOP
        for (int currentSecond = 1; currentSecond <= duration ; currentSecond++) {

            System.out.println("\nTime: " + currentSecond);

            //EVENT 1
            try{
                for (int i = 0; i < MAX_PACKETS; i++) {
                    if (Math.random() < arrivalProb){
                        Packet.setPacketCount((Packet.getPacketCount() + 1));
                        int newPacketSize = randInt(minPacketSize, maxPacketSize);
                        int newPacketId = Packet.getPacketCount();
                        dispatcher.enqueue(new Packet(newPacketId, newPacketSize, currentSecond), maxBufferSize);
                        System.out.println("Packet " + Packet.getPacketCount() + " arrives at dispatcher with size " +
                                newPacketSize);
                    }
                }
            } catch (Exception e){
                Packet.setPacketCount((Packet.getPacketCount() - 1));
                System.out.println(e.getMessage());
            }


            if (Packet.getPacketCount() == 0)
                System.out.println("No packets arrived.");

            //EVENT 2
            try{
                if (dispatcher.size() > 0){     //unsent packets left in dispatcher
                    for (int i = 0; i < dispatcher.size(); i++) {
                        Packet p = dispatcher.dequeue();
                        int freeIntRouter = Router.sendPacketTo(intermediateRouters, maxBufferSize, p);
                        intermediateRouters.get(freeIntRouter).enqueue(p, maxBufferSize);
                        System.out.println("Packet " + p.getId() + " sent to Router " + freeIntRouter);
                    }
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }

            //EVENT 3
            for (int i = 1; i < intermediateRouters.size(); i++) {
                Router router = intermediateRouters.get(i);
                if (router.size() == 0)
                    continue;
                Packet firstPacket = router.peek();
                if (firstPacket != null && firstPacket.getTimeToDest() > 0)
                    firstPacket.setTimeToDest((firstPacket.getTimeToDest() - 1));
                if (firstPacket != null && firstPacket.getTimeToDest() == 0)
                    rtrPriority.add(i);
            }

            //EVENT 4 + 5
            try {
                int loopVar = 1;
                while (!rtrPriority.isEmpty() && loopVar <= bandwidth) {
                    Packet sentPacket = intermediateRouters.get(rtrPriority.remove(0)).dequeue();
                    totalServiceTime += currentSecond - sentPacket.getTimeArrive();
                    totalPacketsArrived++;
                    System.out.println("Packet " + sentPacket.getId() + " has successfully reached its destination: +" +
                            (currentSecond - sentPacket.getTimeArrive()));
                    loopVar++;
                }

                for (int i = 1; i < intermediateRouters.size(); i++) {
                    System.out.println("R" + i + ": " + intermediateRouters.get(i));
                }
            } catch (Exception e){
                System.out.println(e.getMessage());
                System.out.println();
            }
        }
        
        return ((double)totalServiceTime / totalPacketsArrived);
    }

    public static int randInt(int minVal, int maxVal){
        return ((int) (Math.random() * (maxVal - minVal + 1)) + minVal);
    }

    static boolean runCond = true;
    public static void main (String[] args) throws Exception {
        Scanner stdin = new Scanner(System.in);

        while (runCond){

            System.out.println("Starting simulator...");
            // The number of Intermediate routers in the network.
            int numIntRouters = getInt(stdin, "Enter the number of Intermediate routers: ", "Invalid " +
                    "input, please enter integer values");
            // The probability of a new packet arriving at the Dispatcher.
            double arrivalProb = getDouble(stdin, "Enter the arrival probability of a packet: ", "Invalid " +
                    "input, please enter a double value between 0 and 1");
            // The maximum number of Packets a Router can accommodate for.
            int maxBufferSize = getInt(stdin, "Enter the maximum buffer size of a router: ", "Invalid " +
                    "input, please enter integer values");
            // The minimum size of a Packet
            int minPacketSize = getInt(stdin, "Enter the minimum size of a packet: ", "Invalid " +
                    "input, please enter integer values");
            // The maximum size of a Packet
            int maxPacketSize = getInt(stdin, "Enter the maximum size of a packet: ", "Invalid " +
                    "input, please enter integer values");
            // The maximum number of Packets the Destination router can accept at a given simulation unit.
            int bandwidth = getInt(stdin, "Enter the bandwidth size: ", "Invalid " +
                    "input, please enter integer values");
            // The number of simulation units
            int duration = getInt(stdin, "Enter the simulation duration: ", "Invalid " +
                    "input, please enter integer values");

            double avgServTime = simulate(numIntRouters, arrivalProb, maxBufferSize, minPacketSize, maxPacketSize, bandwidth, duration);

            if (avgServTime != 0) {
                System.out.println("\nSimulation ending...");
                System.out.println("Total service time: " + totalServiceTime);
                System.out.println("Total packets served: " + totalPacketsArrived);
                System.out.println("Average service time per packet: " + avgServTime);
                System.out.println("Total packets dropped: " + packetsDropped + "\n");

            }

            while(true){
                String s = getString(stdin, "Do you want to try another simulation? (y/n):");
                if ((s.equals("n"))){
                    runCond = false;
                    break;
                }
                else if (s.equals("y")) {
                    break;
                }else
                    System.out.println("Invalid input!! please enter 'y' for yes and 'n' for no");
            }

        }

        System.out.println("Program terminating successfully....");
        stdin.close();
    }

    /**
     * This method is used to collect integer type input from the terminal/console
     * @param stdin
     * The Scanner variable used to collect input
     * @param instruction
     * The message to be displayed
     * @param invalidMessage
     * The message to be displayed when input is invalid
     * @return
     * The inputted double
     */
    private static double getDouble(Scanner stdin, String instruction,
                              String invalidMessage) {
        while (true) {
            System.out.print(instruction);
            String line = stdin.nextLine();

            try {
                double value = Double.parseDouble(line);
                return value;
            } catch (NumberFormatException ex) {
                System.out.println(invalidMessage);
            }
        }
    }

    /**
     * This method is used to collect integer type input from the terminal/console
     * @param stdin
     * The Scanner variable used to collect input
     * @param instruction
     * The message to be displayed
     * @param invalidMessage
     * The message to be displayed when input is invalid
     * @return
     * The inputted integer
     */
    private static int getInt(Scanner stdin, String instruction,
                              String invalidMessage) {
        while (true) {
            System.out.print(instruction);
            String line = stdin.nextLine();

            try {
                int value = Integer.parseInt(line);
                return value;
            } catch (NumberFormatException ex) {
                System.out.println(invalidMessage);
            }
        }
    }

    /**
     * This method is used to collect String type input from the terminal/console
     * @param stdin
     * The Scanner variable used to collect input
     * @param instruction
     * The message to be displayed
     * @return
     * The inputted String
     */
    private static String getString(Scanner stdin, String instruction) {
        System.out.print(instruction);
        String line = "";

        while (line.isBlank() || line.isEmpty()) {
            line = stdin.nextLine();
        }

        return line;
    }

}
