//MICHAEL COLISTRO
//0488092
//NETWORKS AND DISTRIBUTED SYSTEMS
//DR EL OCLA
//SELECTIVE REPEAT ARQ PIGGYBACK
//JAVA

package selectiverepeatarqpiggyback;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SelectiveRepeatARQPiggyback {
    //predetermined variables
    //windowNum is the window size
    static int windowNum = 5;
    //totalPacketNum determines how many packets total we want exchanged before the sim ends
    static int totalPacketNum = 20;
    //this is a counter to keep track of the amount sent
    static int hop1Count = 0;
    static int hop2Count = 0;
    //this variable just determines the length of the data that will be in the packet
    static int dataLen = 5;
    //this is just a string that we use to randomly select data for the packet
    static String alphabet = "abcdefghijklmopqrstuvwxyz0123456789";
    //these are the two nodes being initialized
    static Node hop1 = new Node(1);
    static Node hop2 = new Node(2);
    static int removeCounter1 = 0;
    static int removeCounter2 = 0;
    //this list keeps track of all the packets sent respectivelly 
    static ArrayList<Packet> hop1SentPackets = new ArrayList();
    static ArrayList<Packet> hop2SentPackets = new ArrayList();

    public static void main(String[] args) throws InterruptedException {

        RunnableDemo R1 = new RunnableDemo("Node-1", hop1);
        RunnableDemo R2 = new RunnableDemo("Node-1", hop2);
        //These are threads for each node
        R1.start();
        R2.start();
    }

    static class RunnableDemo implements Runnable {

        private Thread t;
        private String threadName;
        private Node thisHop;

        RunnableDemo(String name, Node myNode) {
            threadName = name;
            thisHop = myNode;
        }
        //the run function for each thread
        public void run() {
            //this checks to see if the total hasnt been reached, currently set to 20 packets
            while (thisHop.totalPacketsSucessfullySent != totalPacketNum) {
                //this is to check to make sure that the node hasnt exceeded the windowSize
                if (thisHop.outgoingPackets != windowNum) {
                    //This is the function that gets called if we want to send a packet
                    thisHop.sendPacket();
                    System.out.println("Sending Packet for Hop" + thisHop.Nodenum);
                }//this determines which node is actually being focused on
                if (thisHop.Nodenum == 1) {
                    int j = 0;
                    //this checks to see if any packets are incoming, if there are it retrieves the first one with the index j
                    if (!hop2SentPackets.isEmpty()) {
                        while (true) {
                            if (hop2SentPackets.get(j) != null) {

                                break;
                            }
                            j++;
                        }
                        //this is the actual function that retrives the packet
                        thisHop.receivePacket(hop2SentPackets.get(j), j);

                    }//This is the same as the upper module except with respect to node 2
                } else {
                    int j = 0;
                    if (!hop1SentPackets.isEmpty()) {

                        while (true) {
                            if (hop1SentPackets.get(j) != null) {

                                break;
                            }
                            j++;
                        }
                        thisHop.receivePacket(hop1SentPackets.get(j), j);

                    }
                }
            }
        }
        //the function that starts the thread
        public void start() {
            System.out.println("Starting " + threadName);
            if (t == null) {
                t = new Thread(this, threadName);
                t.start();
            }
        }

    }
    //This is the node class, the node class is basically the class for each computer on the network
    public static class Node {

        int Nodenum;
        //this is a list of all the packets to be sent
        ArrayList<Packet> Packets = new ArrayList();
        //this is a list to keep track of all received packets
        ArrayList<Packet> receivedPackets = new ArrayList();
        //counter for  how many packets are currently on their way to the other node
        int outgoingPackets = 0;
        //counter for how many packets were sent properly
        int totalPacketsSucessfullySent = 0;
        //this is the index for the packet about to be sent
        int currentPacket = 0;
        //this is the number to determine what the previous ack should be
        int previousPacket = -1;

        private Node(int i) {
            Nodenum = i;
            //this just initiliazes the packets
            for (int j = 0; j < totalPacketNum; j++) {
                createPacket(Packets);
            }
        }
        //this is the function used to send the packet
        public void sendPacket() {
            //just a double check to make sure that the window hasnt been reached
            if (this.outgoingPackets >= windowNum) {
                System.out.println("Cannot send packet, Window Reached");
            } else {
                //this 
                outgoingPackets++;
                //determines which node we currently are on
                if (Nodenum == 1) {
                    //sends the packet to the sent packets list
                    if (currentPacket < totalPacketNum) {
                        hop1SentPackets.add(Packets.get(currentPacket));
                        currentPacket++;
                    } else {
                        System.out.println("Hop1 has sent all packets");
                        totalPacketsSucessfullySent = currentPacket;
                    }
                } else if (Nodenum == 2) {
                    if (currentPacket < totalPacketNum) {
                        hop2SentPackets.add(Packets.get(currentPacket));
                        currentPacket++;
                    } else {
                        System.out.println("Hop 2 has sent all packets");
                        totalPacketsSucessfullySent = currentPacket;
                    }
                }
            }
        }
        //this is the function to receive the packet
        public void receivePacket(Packet newPacket, int j) {
            if (Nodenum == 1) {
                System.out.println("Hop 1 Received a packet!\nThe sequence number is: " + newPacket.Sequence + " and the data is: " + newPacket.Data);
                //just a check to make sure its not the first packet sent
                //and to check and see if the expected packet is this one
                //if not it adds the nack with the sequence number
                if ((previousPacket + 1 != newPacket.Sequence) && (previousPacket != -1)) {
                    
                    Packets.get(currentPacket).Data += "nack" + Integer.toString(previousPacket);

                    System.out.println("ERROR NOT EXPECTED PACKET");
                } else {
                    //this is a check to make sure its not the first packet   
                    if(previousPacket == -1){
                        previousPacket = 0;
                    }
                    //since it passed the error check it adds the ack and the sequence number to the next packet that it is goign to send out
                    Packets.get(currentPacket).Data += "ack" + Integer.toString(previousPacket);
                    System.out.println("Congratulations Expected Packet!");
                }
                //updates prevoiusPacket
                previousPacket = newPacket.Sequence;
                hop1.outgoingPackets--;
                //removes the packet from the list
                hop1SentPackets.remove(j);
                //everything is the same as the previous module just for node 2
            } else if (Nodenum == 2) {
                System.out.println("Hop 2 Received a packet!\nThe sequence number is: " + newPacket.Sequence + " and the data is: " + newPacket.Data);
                if ((previousPacket + 1 != newPacket.Sequence) && (previousPacket != -1)) {
                    //do nack here
                    Packets.get(currentPacket).Data += "nack" + Integer.toString(previousPacket);
                    //checkACK of newPacket, if its good continue, if its not switch currentpacket to that one
                    System.out.println("ERROR NOT EXPECTED PACKET");
                } else {
                    //do ack here   
                    if(previousPacket == -1){
                        previousPacket = 0;
                    }
                    Packets.get(currentPacket).Data += "ack" + Integer.toString(previousPacket);
                    //checkACK of newPacket, if its good continue, if its not switch currentpacket to that one
                    System.out.println("Congratulations Expected Packet!");
                }
                previousPacket = newPacket.Sequence;
                hop2.outgoingPackets--;
                hop2SentPackets.remove(j);
            }
        }
    }
    //this function checks the ack to make sure its the appropriate one
    //returns a negative number of the frame number if its a nack and posiitive if its an ack
    public static int checkACK(String myData) {
        int location = 0;
        if (myData.contains("nack")) {

            char temp = myData.charAt(myData.indexOf("nack") + 4);
            location = Character.getNumericValue(temp) * -1;
        } else if (myData.contains("ack")) {
            char temp = myData.charAt(myData.indexOf("ack") + 3);
            location = Character.getNumericValue(temp);
        }

        return location;
    }
//simple function to create packets
    public static void createPacket(ArrayList<Packet> list) {
        if (!list.isEmpty()) {
            list.add(new Packet(list.get(list.size() - 1).Sequence + 1, generateString()));
        } else {
            list.add(new Packet(0, generateString()));
        }

    }
//side function to help generate a random string
    public static String generateString() {
        Random rng = new Random();

        char[] text = new char[dataLen];
        for (int i = 0; i < dataLen; i++) {
            text[i] = alphabet.charAt(rng.nextInt(alphabet.length()));
        }
        return new String(text);
    }
//packet class that the nodes use.
    public static class Packet {

        int Sequence;
        String Data;

        public Packet(int num1, String str2) {
            Sequence = num1;
            Data = str2;
        }
    }

}
