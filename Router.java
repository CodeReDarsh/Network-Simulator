/**
 * @author CodeReDarsh
 * <br>email: adarshcp2077@gmail.com
 */

import java.util.ArrayList;

/**
 * The <code>Router</code> class represents a router in the network, which is ultimately a queue.
 */
public class Router extends ArrayList {

    ArrayList<Packet> packetQueue;  //Holds the packets in each router

    /**
     * The default constructor of the router class
     */
    public Router(){
        packetQueue = new ArrayList<>();
    }

    /**
     * Adds a new Packet to the end of the router buffer.
     * @param p
     *  The packet to be added
     * @param maxBufferSize
     *  The max number of packets a router can have
     * @throws Exception
     *  When router becomes full
     */
    public void enqueue(Packet p, int maxBufferSize) throws Exception {
        if (packetQueue.size() == maxBufferSize) throw new Exception("Router is at max capacity, cannot add more Packets");
        packetQueue.add(p);
    }

    /**
     * Removes the first Packet in the router buffer.
     * @return
     *  The first packet from the router buffer
     * @throws Exception
     *  When the router is empty
     */
    public Packet dequeue() throws Exception {
        if (packetQueue.size() < 1) throw new Exception("Router is empty cannot dequeue anymore");
        return packetQueue.remove(0);
    }

    /**
     * Returns, but does not remove the first Packet in the router buffer.
     * @return
     *  The first packet in the router buffer
     */
    public Packet peek(){
        return packetQueue.get(0);
    }

    /**
     * Returns the size of the router buffer queue
     * @return
     *  The size of the router buffer queue
     */
    public int size(){
        return packetQueue.size();
    }

    /**
     * Checks if the router is empty or not
     * @return
     *  A boolean value representing whether or not the router is empty
     */
    public boolean isEmpty(){
        return packetQueue.isEmpty();
    }

    /**
     * Returns a String representation of the router buffer in the following format:
     * <br>{[packet1], [packet2], ... , [packetN]}
     * @return
     *  A string representation of the router buffer
     */
    @Override
    public String toString() {
        String str = "";
        for (int i = 0; i < packetQueue.size(); i++) {
            str += packetQueue.get(i).toString() + ", ";
        }
        return "{" +
                "" + str +
                "}";
    }

    /**
     * This method loops through the collection/list of Intermediate routers, finds the router with the most free buffer
     * space (contains the least Packets), and returns the index of that router.
     * @param intermediateRouters
     *  The signifies the group of all intermediate routers
     * @param maxBufferSize
     *  The maximum number of Packets a Router can accommodate for.
     * @return
     *  The index of the router with the most available space
     * @throws Exception
     *  When all the routers are full
     */
    public static int sendPacketTo(ArrayList<Router> intermediateRouters, int maxBufferSize, Packet p) throws Exception{
        int curSize = maxBufferSize;
        int findIdx = 1;
        for (int i = 1; i < intermediateRouters.size(); i++) {
            if (curSize > intermediateRouters.get(i).size()) {
                curSize = intermediateRouters.get(i).size();
                findIdx = i;
            }
        }
        if (curSize >= maxBufferSize) {
            Simulator.packetsDropped++;
            throw new Exception("Network is congested. Packet " + p.getId() + " is dropped.");
        }
        else
            return findIdx;
    }
}
