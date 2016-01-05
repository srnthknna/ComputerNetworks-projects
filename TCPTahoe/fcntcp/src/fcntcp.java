import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.file.Files;
import java.util.LinkedList;
import java.util.Queue;
import java.util.zip.CRC32;
import java.util.zip.Checksum;


/**
 * Program for Reliable data transfer
 * Packet Class
 * @author srnthknna
 *
 */
@SuppressWarnings("serial")
class packet implements Serializable{
	//packet members
	int sourceport=0;
	int destport=0;
	InetAddress sourceip;
	String desip;
	long filesize;
	long checksum=0;
	byte data[]=new byte[500];
	int seqnum=-1;
	int numofpackets=0;
}
/**
 * Class for tcp algorithm
 * @author srnthknna
 *
 */
class fcntcp extends Thread implements Serializable {
	// Global class variables

	private static final long serialVersionUID = 1L;
	//varialble to differentiate threads
	public boolean client;
	public boolean server;
	public boolean sender;
	public static String serverIP;
	public static InetAddress clientIP;
	public static int portAddressTCPC = 6060;
	public static int windowSize = 1;
	public static int portAddressTCPS;
	public static String filename = "";
	public static int timeout = 1000;
	public static int quiet = 1;
	public static byte fileinbytes[];
	public static double datasize = 500.0;
	public static int numofpackets = 0;
	public static packet cpackarr[];
	public static packet spackarr[];
	public static int ackprepre = -3;
	public static int ackpre = -2;
	public static int ackcur = -1;
	public static volatile boolean ssender = false;
	public static volatile boolean sreceive = true;
	public static volatile boolean csender = true;
	public static volatile boolean creceive = false;
	public static volatile int lastreceived = 0;
	public static volatile boolean firsttime = true;
	public static int lastpacketsent = -1;
	public static volatile int windowsize = 1;
	public static volatile int pwindowsize = 1;
	public static volatile boolean problem = false;
	public static DatagramSocket receiveSocket;
	public static volatile int plastreceived = 0;
	public static  Queue<packet> window=new LinkedList<packet>();
	public static boolean cover=false;
	public static String algo=""; 
	public static String outputfile="output.bin";
	public static int threshold=128;
	/**
	 * method to create a array of packets before sending them
	 * @throws IOException
	 */
	public void packetmaker() throws IOException {

		System.out.println("Packet generation in progress: ");
		File file = new File(filename);
		fileinbytes = Files.readAllBytes(file.toPath());
		double n = (file.length() / datasize);
		//find the number of packets needed to send the file
		numofpackets = (int) Math.ceil(n);
		cpackarr = new packet[numofpackets];
		//create the packets
		for (int i = 0; i < numofpackets-1; i++) {
			cpackarr[i] = new packet();
			quiet("Creating packet : " + (i + 1));
			cpackarr[i].destport = portAddressTCPS;
			cpackarr[i].sourceport = portAddressTCPC;
			cpackarr[i].desip = serverIP;
			cpackarr[i].seqnum = i;
			cpackarr[i].filesize = file.length();
			cpackarr[i].numofpackets = numofpackets;
			cpackarr[i].sourceip = clientIP;
			//for (int j = 0; j  < numofpackets-1; j++) 
			{
				//cpackarr[i].data = Arrays.copyOfRange(fileinbytes, i*500, i*500+500);
				//fileinbytes[i * 500 + j];
				
				System.arraycopy(fileinbytes, i * 500 ,cpackarr[i].data, 0, 500);
			}
			//create the checksum for the packet with crc32
			Checksum value = new CRC32();
			value.update(cpackarr[i].data, 0, 500);
			cpackarr[i].checksum = value.getValue();

		}
		{
			cpackarr[numofpackets-1] = new packet();
			quiet("Creating packet : " + ((numofpackets)));
			cpackarr[numofpackets-1].destport = portAddressTCPS;
			cpackarr[numofpackets-1].sourceport = portAddressTCPC;
			cpackarr[numofpackets-1].desip = serverIP;
			cpackarr[numofpackets-1].seqnum = (numofpackets-1);
			cpackarr[numofpackets-1].filesize = file.length();
			cpackarr[numofpackets-1].numofpackets = numofpackets;
			cpackarr[numofpackets-1].sourceip = clientIP;
			//for (int j = 0; j  < numofpackets-1; j++) 
			{
				//cpackarr[i].data = Arrays.copyOfRange(fileinbytes, i*500, i*500+500);
				//fileinbytes[i * 500 + j];
				
				System.arraycopy(fileinbytes, (numofpackets-1) * 500 ,cpackarr[(numofpackets-1)].data, 0, (int)file.length()-(numofpackets-1)*500);
			}
			//create the checksum for the packet with crc32
			Checksum value = new CRC32();
			value.update(cpackarr[numofpackets-1].data, 0, 500);
			cpackarr[numofpackets-1].checksum = value.getValue();
		}
	}
	/**
	 * method to handle the quiet mode
	 * @param line
	 */
	public void quiet(String line) {
		if (fcntcp.quiet == 1)
			System.out.println(line);

	}
	/**
	 * method to find if the last three acks are same
	 * @return
	 */
	public boolean threeacks() {
		if (ackpre == ackprepre && ackprepre == ackcur)
			return true;
		return false;
	}
	/**
	 * method to check the checksum of the packet with the embedded checksum
	 * @param obj-packet to be checked
	 * @return boolean value of the result
	 */
	public boolean checksumchecker(packet obj) {
		long checksum;
		Checksum value = new CRC32();
		value.update(obj.data, 0, 500);
		checksum = value.getValue();
		//check if the packet checksum is correct
		if (obj.checksum == checksum)
			return true;
		return false;
	}
	/**
	 * constructor to handle threads for server and client
	 * @param send
	 */
	public fcntcp(boolean send) {
		this.sender = send;
	}
	/**
	 * default constructor to handle timer thread
	 */
	public fcntcp() {

	}
	/**
	 * method to get window size
	 * @return window size
	 */
	public synchronized int getWindow() {
		return windowsize;

	}
	/**
	 * method to set window size
	 * @param size of the window to be set
	 */
	public synchronized void setWindow(int size) {
		windowsize = size;
	}
	/**
	 * method to get the last packet received
	 * @return the last packet index
	 */
	public synchronized int getlastreceived() {
		return lastreceived;

	}
		public synchronized int getplastreceived() {
		return plastreceived;

	}
	/**
	 * method to set the last packet received
	 * @param size to set the last packet  received
	 */
	public synchronized void setlastreceived(int size) {
		lastreceived = size;
	}
	/**
	 * method to set the previously last packet received
	 * @param size to set the previously last packet received
	 */
	public synchronized void setplastreceived(int size) {
		plastreceived = size;
	}
	/**
	 * method to get the finish flag in server
	 * @return the flag
	 */
	public synchronized boolean getproblem() {
		return problem;

	}
	/**
	 * method to set the finish flag of server
	 * @param val to set the finish flag 
	 */
	public synchronized void setproblem(boolean val) {
		problem = val;
	}
	/**
	 * method to get the previously used window size
	 * @return the previously used window size
	 */
	public synchronized int pgetWindow() {
		return pwindowsize;

	}
	/**
	 * method to set the previously used window size
	 * @param size to set the previously used window size
	 */
	public synchronized void psetWindow(int size) {
		pwindowsize = size;
	}

	/**
	 * Main starts from here
	 * 
	 * @param args
	 *            - command line arguments
	 * @throws Exception
	 *             - Exception for IO and Network Errors
	 */
	public static void main(String args[]) throws Exception {
		//set the command line arguments for client
		if (args[0].equals("-c") || args[0].equals("--client")) {
			//two objects for client sender and client receiver
			fcntcp clientsender = new fcntcp(true);
			fcntcp clientreciever = new fcntcp(false);
			clientsender.client = true;
			fcntcp.serverIP = args[args.length - 2];
			fcntcp.portAddressTCPS = Integer.parseInt(args[args.length - 1]);
			fcntcp.clientIP = InetAddress.getLocalHost();
			clientreciever.client = true;
			for (int i = 1; i < args.length - 2; i++) {
				//to set the file name to be read
				if (args[i].equals("-f") || args[i].equals("--file")) {
					fcntcp.filename = args[i + 1];
					i++;
				} 
				//to set the timeout
				else if (args[i].equals("-t") || args[i].equals("--timeout")) {
					fcntcp.timeout = Integer.parseInt(args[i + 1]);
					i++;
				} 
				//to set the algorithm name
				else if (args[i].equals("-a") || args[i].equals("--algorithm")) {
					fcntcp.algo = args[i + 1];
					i++;
				}
				//to set the quiet mode
				else if (args[i].equals("--quiet") || args[i].equals("-q")) {
					fcntcp.quiet = 0;
					
				}
			}
			//start the two threads
			clientsender.start();
			clientreciever.start();
		}
		//set the command line arguments for server
		else if (args[0].equals("-s") || args[0].equals("--server")) {
			fcntcp serverreceiver = new fcntcp(false);
			fcntcp.serverIP = InetAddress.getLocalHost().getHostAddress();
			fcntcp.portAddressTCPS = Integer.parseInt(args[args.length - 1]);
			serverreceiver.server = true;
			for (int i = 1; i < args.length - 1; i++) {
				//to set the quiet mode
				if (args[i].equals("--quiet") || args[i].equals("-q")) {
					fcntcp.quiet = 0;
				}
				//to set the algorithm name
				else if (args[i].equals("-a") || args[i].equals("--algorithm")) {
					fcntcp.algo = args[i + 1];
					i++;
				}
			}
			//start the server thread
			serverreceiver.start();
		}
	}

	/**
	 * method to start the threads
	 */
	public void run() {
		// handle the client threads
		if (this.client == true) {
			if (this.sender == true) {
				try {
					//to create the file packets before sending
					packetmaker();
				} catch (IOException e) {

					e.printStackTrace();
				}
				try {
					//start the client sender
					this.clientsender(fcntcp.serverIP, fcntcp.portAddressTCPS);
				} catch (IOException e) {

					e.printStackTrace();
				} catch (InterruptedException e) {

					e.printStackTrace();
				}

			} else {
				try {
					//start the  client receiver
					this.clientreceive(fcntcp.portAddressTCPC);
				} catch (IOException e) {
					e.printStackTrace();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		//start the server thread
		else if (server == true) {
			try {
				try {
					this.serverreceive(fcntcp.portAddressTCPS);
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}

		} 
		//start the timer thread
		else {
			try {
				callerfor();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	/**
	 * method to handle the timer thread which 
	 * collects all the packets sent from the same window
	 * @throws InterruptedException
	 * @throws IOException
	 */
	@SuppressWarnings("resource")
	public void callerfor() throws InterruptedException, IOException 
	{
		DatagramSocket sendSocket = new DatagramSocket();
		//until there is no more packets arrival at the server
		while(window.isEmpty()!=true)
		{
			
			packet l=new packet();
			//check the validity of packets checksum
			if(window.peek()!=null)
			l=window.remove();
			if(checksumchecker(l)==true)
			{
				//add the packet to arrived list 
				spackarr[l.seqnum] = l;
				quiet("Server accepting packet : " + (l.seqnum));
				//find the highest sequence of packet received in the window
				if(l.seqnum>lastreceived  )
				{
					spackarr[l.seqnum] = l;
					setplastreceived(lastreceived);	
					setlastreceived(l.seqnum);	
				}
			}
			//drop the packet if checksum is not valid
			else if(checksumchecker(l)==false) 
			{
				quiet("Server dropping packet checksum error : " + (l.seqnum));
			}
			sleep(5);
	   }
		//generate the appropriate acknowledgement for the current batch of packets
		int acki=0,d;
		byte[] rbuffer ;
		for(d=0;d<getlastreceived();d++)
		{
			//first missing packet is searched
			if(spackarr[d]==null)
			{
				acki=d;
				break;
			}
		}
		//if no missing packets in between ask for next sequence
		if(d==getlastreceived())
			acki=getlastreceived()+1;
		//send the acknowledgement
		rbuffer=((acki)+"").getBytes();
		DatagramPacket sendPacket = new DatagramPacket(rbuffer,
				rbuffer.length, clientIP,portAddressTCPC );		
		
		sendSocket.send(sendPacket);
		quiet("Server sending ack for : "+(acki));

	}

	/**
	 * method to send packets from client
	 * 
	 * @param serverIp
	 * @param portAddress
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public void clientsender(String serverIp, int portAddress)
			throws IOException, InterruptedException {
		
		int tempwindow;
		quiet("Client Started: ");
		while (true) 
		{
				//till all the packets are sent to server
				tempwindow = getWindow();
				//set the window elements accordingly
				int ender = 0;
				if (tempwindow + lastpacketsent < numofpackets)
					ender = tempwindow + lastpacketsent + 1;
				else if(tempwindow==1&&lastpacketsent==numofpackets-1)
				{
					lastpacketsent=lastpacketsent-1;
					ender=numofpackets;
				}
				else
					ender = numofpackets ;
				//send the packets in the window
				for (int send = lastpacketsent + 1;send < ender; send++)				
				{
					byte[] buffer = new byte[1024];
					buffer = new byte[1024];
					InetAddress inet = InetAddress.getByName(cpackarr[0].desip);
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					ObjectOutputStream oos = new ObjectOutputStream(baos);
					oos.writeObject(cpackarr[send]);
					buffer = baos.toByteArray();
					DatagramPacket sendPacket = new DatagramPacket(buffer,
							buffer.length, inet, cpackarr[send].destport);
					receiveSocket.send(sendPacket);
					lastpacketsent = cpackarr[send].seqnum;
					//for last packet in the window make window size to zero to wait for ack
					if (send + 1 == ender) 
					{
						psetWindow(tempwindow);
						setWindow(0);
					}
					quiet("Client sending packet : " + (send + 1));
				}
				//if file is transferred successfully 
				if (cover==true)
					break;
		}
		//print the sent file details to check the validity at both sides
		quiet("Client transferred the file: ");
		System.out.println("Client File details");
		printfilehaschcode(filename);
	}
	/**
	 * method to receive the acknowledgments form server
	 * @param portaddress
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public void clientreceive(int portaddress) throws IOException,
			InterruptedException 
	{
		receiveSocket = new DatagramSocket(portAddressTCPC);
		int  ptempwindow,temp=0;
		receiveSocket.setSoTimeout(timeout);
		//till goodbye handshake from server
		while (true) 
		{
			
			ptempwindow = pgetWindow();
			byte[] receivebuffer = new byte[1024];
			DatagramPacket receivePacket = new DatagramPacket(
					receivebuffer, receivebuffer.length);
			//if acknowledgement does not receive before set timeout
			try{
			receiveSocket.receive(receivePacket);
			String name = new String(receivePacket.getData()).trim();
			//if the acknowledgement is for goodbye break
			if(name.equals("done"))
			{
				cover=true;
				break;
			}
			//set the last acks and received message
			if (name.matches("[0-9]+")) {
			temp = Integer.parseInt(name);
			ackprepre = ackpre;
			ackpre = ackcur;
			ackcur = temp;
			lastpacketsent=ackcur-1;
			//if last three acknowledgments are same set the window size as 1
			if(threeacks()==true)
			{
				quiet("3 ack failure case: Setting window size as 1");
				setWindow(1);			
				if(threshold/2==0)
					threshold=1;
				else
					threshold=threshold/2;

			}
			//else double the window size and send the packets from received acknowledgement 
			else 
			{
				if(ptempwindow*2<=threshold){
				setWindow(ptempwindow * 2);
				}
				else
				{
					setWindow(ptempwindow +1);	
					threshold=threshold+1;
				}
				quiet("Doubling the window size: " +windowsize);
			} 
			quiet("Ack received as : "	+ ackcur);
			}
			}
			//if timeout then set the window size as 1 and retransmit
			catch (java.net.SocketTimeoutException e) {
				if(lastpacketsent>0)
				lastpacketsent=lastpacketsent-1;
				setWindow(1); 
				if(threshold/2==0)
					threshold=1;
				else
					threshold=threshold/2;

				quiet("No ack from server: Timeout: Setting window size as 1");
		    }
		}
		
	}


	/**
	 * method to receive packets in server
	 * 
	 * @param portAddress
	 * @throws IOException
	 * @throws ClassNotFoundException
	 * @throws InterruptedException 
	 */
	@SuppressWarnings("resource")
	public void serverreceive(int portAddress) throws IOException,
			ClassNotFoundException, InterruptedException {	
		DatagramSocket receiveSocket = new DatagramSocket(portAddress);
		fcntcp timer=new fcntcp();
		quiet("Server started: ");
		//till all the packets are not recevied
		while (true) 
		{
			byte[] receivebuffer = new byte[1024];
			DatagramPacket receivePacket = new DatagramPacket(receivebuffer,
					receivebuffer.length);
			//receive the packet
			receiveSocket.receive(receivePacket);
			byte packetb[] = new byte[1024];
			packetb = receivePacket.getData();
			ByteArrayInputStream bais = new ByteArrayInputStream(packetb);
			ObjectInputStream oos = new ObjectInputStream(bais);
			packet p;
			p = (packet) oos.readObject();
			//add the packet to window
			window.add(p);
			//handshake in TCP to set the values
			if (firsttime == true) 
			{				
				numofpackets = p.numofpackets;
				spackarr = new packet[numofpackets];
				firsttime = false;
				clientIP = receivePacket.getAddress();
				portAddressTCPC = receivePacket.getPort();
					
			}
			//if the timer thread is not alive restart it
			if(timer.isAlive()==false)
			{
			    timer=new fcntcp();
				
				timer.start();	
			}
			//close the bytearraystream
			bais.close();	
			setproblem(false);
			//check if all packets are received
			for(int e=0;e<numofpackets;e++)
			{
				if(spackarr[e]==null)
				{			
					setproblem(true);			
				}
			}
			//if all packets are received send goodbye acknowledgment
			if(getproblem()==false)
			{
				DatagramSocket sendSocket = new DatagramSocket();
				byte rbuffer[]=("done").getBytes();
				DatagramPacket sendPacket = new DatagramPacket(rbuffer,	rbuffer.length, clientIP,portAddressTCPC );
				sendSocket.send(sendPacket);
				quiet("Sending file received acknowledgement: done ");	
					break;
			}
		}
		//write the packets  read from client in a file
		quiet("All packets received now writing in file ");	
		filewriting(outputfile);
	}
	/**
	 * method to write the packets in file
	 * @param filepath to be read
	 * @throws IOException
	 */
	private void filewriting(String filepath) throws IOException 
	{
		FileOutputStream fos = new FileOutputStream(
				filepath);
		//write n-1 packets in file
		for (int j = 0; j < spackarr.length - 1; j++) 
		{
			quiet("Writing packet in file : " + (j + 1));
			fos.write((spackarr[j].data));
		}
		//write the last packet
		int lastsize = (int) (spackarr[0].filesize - ((spackarr[0].numofpackets - 1) * datasize));
		byte lastpack[] = new byte[lastsize];
		quiet("Writing packet in file : " + (spackarr.length));
		for (int l = 0; l < lastsize; l++)
			lastpack[l] = spackarr[spackarr.length - 1].data[l];
		fos.write(lastpack);
		fos.close();
		//print the hash code for written file
		System.out.println("Server File details");
		printfilehaschcode(outputfile);
	}
	/**
	 * method to print the file details
	 * @param filepath to be read
	 * @throws IOException
	 */
	private void printfilehaschcode(String filepath) throws IOException
	{
		//print the file HashCode in long format and file size
		File file = new File(filepath);
		System.out.println("Size of file is: "+file.length());
		Checksum value=new CRC32();
		byte[] fb=Files.readAllBytes(file.toPath());
		value.update(fb,0,fb.length);
		System.out.println("Hashcode for file is: "+Long.toHexString(value.getValue()));
	}
}
