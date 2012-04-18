package netlogo.arduino;

import java.io.*;//InputStream;
import java.util.Enumeration;
import java.util.List;
import java.util.ArrayList;
import java.util.ListIterator;
import gnu.io.*;


public class arddueController { //implements SerialPortEventListener{

    RXTXCommDriver driver;
    String portName;
    CommPortIdentifier portId;
    SerialPort port;
    public InputStream inputStream;
    public OutputStream outputStream;
    public InputStreamReader inputRead; //\\

    final String sHeartBeat = "H:"; //Heartbeat header -- not used right now
    final String sAnalog  = "A:";   //Analog packet header
    final String sDigital = "D:";   //Digital packet header
    final String sMotor   = "M:";   //Motor packet header
    final String sConfig  = "C:";   //Config packet header
    final String sError   = "E:";   //Error packet header
    final String sEndPack = ";";    //end packet character
    final String sDelim   = ",";    //packet delimiter character
    
    public static final int BAUD_RATE = 9600;

    public boolean debugging = true;
    public String chunk2;
    public Reader readThread;


    public int analog0,
               analog1,
               analog2,
               analog3,
               analog4,
               analog5;

    public int digital0,
               digital1,
               digital2,
               digital3,
               digital4,
               digital5;

    public int[] MotorArray = {0,0,0,0,0,0};
    public int motor0,
               motor1,
               motor2,
               motor3,
               motor4,
               motor5;


    public static CommPortIdentifier findPortByName(String portName) {
        Enumeration portList = CommPortIdentifier.getPortIdentifiers();
        CommPortIdentifier id;

        while (portList.hasMoreElements()) {
            id = (CommPortIdentifier) portList.nextElement();
            if (id.getPortType() == CommPortIdentifier.PORT_SERIAL) {
                if (id.getName().equals(portName)) {
                    return id;
                }
            }
        }
        return null;
    }

    public arddueController(String portName) {
        this.portName = portName;
        if(debugging){
            //System.out.println("init controller!");
        }
        readThread = new Reader();
        readThread.start();
     }

    public static List<String> availablePorts() {
        return listPorts(true);
    }

    public static List<String> serialPorts() {
        return listPorts(false);
    }

    public static List<String> listPorts(boolean onlyAvailable) {
        Enumeration portList;
        CommPortIdentifier portId;
        List<String> portNames = new ArrayList<String>();

        portList = CommPortIdentifier.getPortIdentifiers();
        while (portList.hasMoreElements()) {
            portId = (CommPortIdentifier) portList.nextElement();
            if (portId.getPortType() == CommPortIdentifier.PORT_SERIAL
                    && (!onlyAvailable || !portId.isCurrentlyOwned())) {
                portNames.add(portId.getName());
            }
        }
        return portNames;
    }

    public String currentPortName() {
        if (port != null) {
            return port.getName();
        }
        return null;
    }

    public SerialPort currentPort() {
        return port;
    }

    public void closePort() {
        synchronized (inputStream) {
            synchronized (outputStream) {
                if (port != null) {
                    port.removeEventListener();
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                            inputStream = null;
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    if (outputStream != null) {
                        try {
                            outputStream.close();
                            outputStream = null;
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    port.close();
                    port = null;
                }
            }
        }
    }

    public boolean openPort() {
        // if already open, just return true
        System.out.println("Opening Port!");
        if (port != null) {
            return true;
        }

        portId = findPortByName(portName);

        if (portId == null) {
            throw new RuntimeException(
                    "Cannot find port: " + portName);
        }

        try {
            port = (SerialPort) portId.open("arddueController", 2000);
            System.out.println("Port Assigned");
        } catch (PortInUseException e) {
            throw new RuntimeException(
                    "Port is already in use: " + e);
        } catch (RuntimeException e) {
            throw new RuntimeException(
                    "Unable to open port: " + e);
        }

        if (port != null) {

            try {
                inputStream = new PushbackInputStream(port.getInputStream());
                outputStream = port.getOutputStream();
                System.out.println("input/output Streams constructed");
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                port.setSerialPortParams(BAUD_RATE,   //9600 //    57600,        //28800,// 9600,         //115200,
                        SerialPort.DATABITS_8,
                        SerialPort.STOPBITS_1,
                        SerialPort.PARITY_NONE);


            } catch (UnsupportedCommOperationException e) {
                e.printStackTrace();
            }
            try {
               
            // add event listeners
	    // port.addEventListener(this);
	    // port.notifyOnDataAvailable(true);
            // System.out.println("Event listener activated");
            }
            catch (Exception e){
                e.printStackTrace();
            }
            return true;
        } else {
            return false;
        }
    }

    void parseAnalogData(String analogPacket) {
        String[] Values;

        if(analogPacket.startsWith(sAnalog)){
            analogPacket = analogPacket.substring(sAnalog.length());
        }

        Values = analogPacket.split(sDelim);

        if(Values.length == 6){
            analog0 = Integer.parseInt(Values[0]);
            analog1 = Integer.parseInt(Values[1]);
            analog2 = Integer.parseInt(Values[2]);
            analog3 = Integer.parseInt(Values[3]);
            analog4 = Integer.parseInt(Values[4]);
            analog5 = Integer.parseInt(Values[5]);
        }
        else{
            System.out.println("Analog read malfunction");
            analog0 = -1;
            analog1 = -1;
            analog2 = -1;
            analog3 = -1;
            analog4 = -1;
            analog5 = -1;
            
        }
    }

    public int[] getAnalogData() {
        int[] analogData = {analog0, analog1, analog2, analog3, analog4, analog5};
        return analogData;
    }

    void parseDigitalData(String DigitalPacket) {
        String[] Values;

        if(DigitalPacket.startsWith(sDigital)){
            DigitalPacket = DigitalPacket.substring(sDigital.length());
        }

        Values = DigitalPacket.split(sDelim);
 
        if(Values.length == 6){
            digital0 = Integer.parseInt(Values[0]);
            digital1 = Integer.parseInt(Values[1]);
            digital2 = Integer.parseInt(Values[2]);
            digital3 = Integer.parseInt(Values[3]);
            digital4 = Integer.parseInt(Values[4]);
            digital5 = Integer.parseInt(Values[5]);
        }
        else{
            System.out.println("digital read malfunction");
            digital0 = -1;
            digital1 = -1;
            digital2 = -1;
            digital3 = -1;
            digital4 = -1;
            digital5 = -1;
        }
    }

    public int[] getDigitalData(){
        int[] digitalData = {digital0, digital1, digital2, digital3, digital4, digital5};
        return digitalData;
    }


/*
 * I need a way to just type commands in to test.  No error checking or feedback.
 */
    public void write(String Command) {

        try{
            System.out.println("sending command");
            outputStream.write(Command.getBytes());
        }
        catch(Exception e) {
            System.out.println("Something Screwed up!!!!!!!!!");
        }
    }

    public void sendMotor() {
        int j ;
        String MotorOrder = "M:";
        for (int i = 0; i < 6; i++) {
            MotorOrder = MotorOrder.concat(Integer.toString(MotorArray[i]));
            if(i == 5){
                MotorOrder = MotorOrder.concat(";");
            }
            else {
                MotorOrder = MotorOrder.concat(",");
            }
        }

        //System.out.println("MotorOrder = "+MotorOrder);

        try{
            System.out.println("Writing Motor commands");
            outputStream.write(MotorOrder.getBytes());
        }
        catch(Exception e) {
            System.out.println("Something Screwed up!!!!!!!!!");
        }
    }

    public void sendConfig(String sstring) {
// Need to add in error checking for config packet format
        try{
            System.out.println("Writing Configuration commands");
            outputStream.write(sstring.getBytes());
        }
        catch(Exception e) {
            System.out.println("Something Screwed up!!!!!!!!!");
        }
    }

    public String getSerialData() {
        String msg = "";
        int Tag;
        try {
            BufferedReader r = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
            StreamTokenizer st = new StreamTokenizer(r);

            st.resetSyntax();
            st.wordChars(44, 122);
            st.quoteChar(';');  //59 = ;

            if(st.nextToken() == -3){
                msg = st.sval;

                if(msg.startsWith(sAnalog)){
                    parseAnalogData(msg);
                }
                else if (msg.startsWith(sDigital)){
                    parseDigitalData(msg);
                }
                else {
                    System.out.println("Dropped packet");
                }

            }
        } catch(Exception e) {
            System.out.println(e);
        }
        return msg;
    }


    /*
     * recently added 
     */
    class Reader extends Thread {
        boolean shouldRead;
        boolean isPaused;
        String msg;

        public Reader() {
            shouldRead = true;
        }

        public void run() {
            try{
                Thread.sleep(1000);
                BufferedReader r = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
                StreamTokenizer st = new StreamTokenizer(r);
                st.resetSyntax();
                st.wordChars(44, 122);
                st.quoteChar(';');

                while (shouldRead) {
                    Thread.sleep(100);
                    while(!isPaused){
                        synchronized (this) {
                            if(st.nextToken() == -3){
                                msg = st.sval;
                                if(msg.startsWith(sAnalog)){
                                    parseAnalogData(msg);
                                }
                                else if (msg.startsWith(sDigital)){
                                    parseDigitalData(msg);
                                }
                                else {
                                    System.out.println("Dropped packet: Unknown Packet header");
                                }
                            }
                        }
                    }
                }
                System.out.println("Exiting Reader thread...");
            }
            catch (Exception e) {
                System.out.println("e = "+ e);
            }
        }

        public void halt() {
            System.out.println("Halting Reader thread...");
            shouldRead = false;
        }

        public void pause() {

            if(isPaused == true){
                isPaused = false;
                System.out.println("Unpausing Reader thread...");
            }
            else if(isPaused == false) {
                isPaused = true;
                System.out.println("Pausing Reader thread...");
            }
        }
    }
    
//    public void setReadTimeout(int ms) {
//        try {
//            synchronized (inputStream) {
//                port.enableReceiveTimeout(ms);
//                // update our input stream
//                inputStream = new PushbackInputStream(port.getInputStream());
//            }
//        } catch (UnsupportedCommOperationException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

    // main for arddueController class, which functions as a small utility
    public static void main(String[] args)
            throws java.io.IOException {
        String port = null;

        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-l")) {
                ListIterator<String> portIterator = serialPorts().listIterator();
                while (portIterator.hasNext()) {
                    System.out.println( portIterator.next() );
                }
                System.exit(0);
            } else if (args[i].equals("-p")) {
                i++;
                port = args[i];
            }
        }
    }
}
