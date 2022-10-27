/**
 * @author CodeReDarsh
 * <br>email: adarshcp2077@gmail.com
 */

/**
 * The <code>Packet</code> class represents a packet that will be sent through the network
 */
public class Packet {

    private static int packetCount = 0; // this value is used to assign an id to a newly created packet. It will start
                                        // with the value 0, and every time a new packet object is created, increment
                                        // this counter and assign the value as the id of the Packet.

    private int id; //a unique identifier for the packet. This will be systematically determined by using packetCount.

    private int packetSize; //the size of the packet being sent. This value is randomly determined by the simulator by
                            //using the Math.random() method.

    private int timeArrive; //the time this Packet is created should be recorded in this variable

    private int timeToDest; //this variable contains the number of simulation units that it takes for a packet to arrive
                            //at the destination router (actually determines the processing time each packet will take while in the intermediate router).
                            //The value will start at one hundredth of the packet size,that is: packetSize/100.
                            //At every simulation time unit, this counter will decrease. Once it reaches 0, we can assume
                            // that the packet has arrived at the destination.

    /**
     * The Default constructor of the Packet class, creates packets with default values
     */
    public Packet(){    
        id = 0;
        packetSize = 0;
        timeArrive = 0;
        timeToDest = 0;
    }

    /**
     * The constructor of the Packet class, creates packets with specified information regarding it
     * @param id
     *  A unique identifier for the packet.
     * @param packetSize
     *  the size of the packet being sent.
     * @param timeArrive
     *  The simulation time unit at which this Packet is created.
     */
    public Packet(int id, int packetSize, int timeArrive) {
        this.id = id;
        this.packetSize = packetSize;
        this.timeArrive = timeArrive;
        timeToDest = (packetSize / 100);
    }

    public static int getPacketCount() {
        return packetCount;
    }

    public static void setPacketCount(int packetCount) {
        Packet.packetCount = packetCount;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPacketSize() {
        return packetSize;
    }

    public void setPacketSize(int packetSize) {
        this.packetSize = packetSize;
    }

    public int getTimeArrive() {
        return timeArrive;
    }

    public void setTimeArrive(int timeArrive) {
        this.timeArrive = timeArrive;
    }

    public int getTimeToDest() {
        return timeToDest;
    }

    public void setTimeToDest(int timeToDest) {
        this.timeToDest = timeToDest;
    }

    @Override
    public String toString() {
        return "[" + id + ", " + timeArrive + ", " + timeToDest + "]";
    }
}
