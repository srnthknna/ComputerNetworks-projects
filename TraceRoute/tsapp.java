import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Calendar;
/**
 * Program for trace route
 * @author Srinath Kanna Dhandapani
 *
 */
class tsapp extends Thread {
	// Global class variables
	public long milliSeconds;
	public boolean proxy;
	public boolean client;
	public boolean server;
	public boolean tcp;
	public boolean udp;
	public boolean tcpproxy;
	public boolean udpproxy;
	public int task = 0;
	public String serverIP;
	public int portAddressTCPS;
	public int portAddressUDPS;
	public int portAddressTCPP;
	public int portAddressUDPP;
	public String userName = "";
	public String passWord = "";
	public static String actualUserName = "--user";
	public static String actualPassWord = "--user";
	public static long name = 0;
	public static int n = 1;

	/**
	 * Constructor to start the thread for tcp and udp ports
	 * 
	 * @param connectionType
	 *            - type of connection
	 */
	public tsapp(String connectionType) {
		if (connectionType.equals("tcp"))
			this.tcp = true;
		else
			this.udp = true;
	}

	/**
	 * Default constructor
	 */
	public tsapp() {

	}

	/**
	 * Main strats from here
	 * 
	 * @param args
	 *            - command line arguments
	 * @throws Exception
	 *             - Exception for IO and Network Errors
	 */
	public static void main(String args[]) throws Exception {
		// If arguments are less than minimum values
		if (args.length < 3) {
			System.out.println("Atleast 4 arguments expected");
		}
		// if the mode for tsapp is server
		if (args[0].equals("-s")) {
			// create the thread for the thread for tcp and udp server and
			// assign the corresponding variables
			tsapp a = new tsapp("tcp");
			tsapp b = new tsapp("udp");
			a.server = true;
			a.portAddressTCPS = Integer.parseInt(args[args.length - 1]);
			a.portAddressUDPS = Integer.parseInt(args[args.length - 2]);
			b.server = true;
			b.portAddressTCPS = Integer.parseInt(args[args.length - 1]);
			b.portAddressUDPS = Integer.parseInt(args[args.length - 2]);
			for (int i = 1; i < args.length - 2; i++) {
				if (args[i].equals("-T")) {
					tsapp.name = Long.parseLong(args[i + 1]);
					i++;
				} else if (args[i].equals("--user")) {
					tsapp.actualUserName = args[i + 1];
					tsapp.actualUserName = args[i + 1];
					i++;
				} else if (args[i].equals("--pass")) {
					tsapp.actualPassWord = args[i + 1];
					tsapp.actualPassWord = args[i + 1];
					i++;
				}
			}
			// two threads for udp and tcp
			a.start();
			b.start();
		}
		// if the mode for tsapp is client
		else if (args[0].equals("-c")) {
			// create a thread and assign the command line arguments to the
			// respective variables
			tsapp a = new tsapp();
			a.client = true;
			a.serverIP = args[1];
			for (int i = 2; i < args.length - 1; i++) {
				if (args[i].equals("-T")) {
					tsapp.name = Long.parseLong(args[i + 1]);
					a.task = 2;
					i++;
				} else if (args[i].equals("--user")) {
					a.userName = args[i + 1];
					i++;
				} else if (args[i].equals("--pass")) {
					a.passWord = args[i + 1];
					i++;
				} else if (args[i].equals("-n")) {
					tsapp.n = Integer.parseInt(args[i + 1]);
					i++;
				} else if (args[i].equals("-u")) {
					a.portAddressUDPS = Integer.parseInt(args[args.length - 1]);
					a.udp = true;
				} else if (args[i].equals("-t")) {
					a.portAddressTCPS = Integer.parseInt(args[args.length - 1]);
					a.tcp = true;
				} else if (args[i].equals("-z")) {
					a.task = 1;
				}
			}
			if (a.tcp == false && a.udp == false) {
				a.udp = true;
				a.portAddressUDPS = Integer.parseInt(args[args.length - 1]);
			}
			a.start();
		}
		// if the mode for tsapp is proxy
		else if (args[0].equals("-p")) {
			// create two threads for tcp and udp
			tsapp a = new tsapp("tcp");
			tsapp b = new tsapp("udp");
			// assignt he variables for the threads
			a.proxy = true;
			a.serverIP = args[1];
			a.portAddressTCPP = Integer.parseInt(args[args.length - 1]);
			a.portAddressUDPP = Integer.parseInt(args[args.length - 2]);
			b.proxy = true;
			b.serverIP = args[1];
			b.portAddressTCPP = Integer.parseInt(args[args.length - 1]);
			b.portAddressUDPP = Integer.parseInt(args[args.length - 2]);
			for (int i = 2; i < args.length - 2; i++) {
				if (args[i].equals("--proxy-udp")) {
					a.portAddressUDPS = Integer.parseInt(args[i + 1]);
					b.portAddressUDPS = Integer.parseInt(args[i + 1]);
					i++;
				} else if (args[i].equals("--proxy-tcp")) {
					a.portAddressTCPS = Integer.parseInt(args[i + 1]);
					b.portAddressTCPS = Integer.parseInt(args[i + 1]);
					i++;
				} else if (args[i].equals("-u")) {
					a.udpproxy = true;
					b.udpproxy = true;
				} else if (args[i].equals("-t")) {
					a.tcpproxy = true;
					b.tcpproxy = true;
				}
			}
			// assign the server address based on the -t or -u mentioned in the
			// command line arguments
			if (a.portAddressTCPS == 0 && a.portAddressUDPS != 0)
				a.portAddressTCPS = a.portAddressUDPS;
			if (b.portAddressTCPS != 0 && b.portAddressUDPS == 0)
				b.portAddressUDPS = b.portAddressTCPS;
			if (a.portAddressTCPS != 0 && a.portAddressUDPS == 0)
				a.portAddressUDPS = a.portAddressTCPS;
			if (b.portAddressTCPS == 0 && b.portAddressUDPS != 0)
				b.portAddressTCPS = b.portAddressUDPS;
			// start the threads for proxy udp and tcp
			a.start();
			b.start();
		} else
			throw new Exception();
	}

	/**
	 * method to start the threads
	 */
	public void run() {
		// to send the client thread for tcp and udp connections
		if (this.client == true) {
			if (this.tcp == true) {
				this.TCPClient(this.serverIP, this.portAddressTCPS);
			} else if (this.udp == true) {
				try {
					this.UDPClient(this.serverIP, this.portAddressUDPS);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		// to send the server thread to start udp and tcp ports
		else if (server == true) {
			if (this.tcp == true) {
				this.TCPServer(this.portAddressTCPS);
			} else if (this.udp == true) {
				try {
					this.UDPServer(this.portAddressUDPS);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		// to send the proxy thread to start tcp and udp ports
		else if (proxy == true) {
			if (this.tcp == true)
				try {
					if (this.tcpproxy == true)
						this.proxyTCP(this.serverIP, this.portAddressTCPP,
								this.portAddressTCPS);
					this.proxyTCP(this.serverIP, this.portAddressTCPP,
							this.portAddressUDPS);
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			else if (this.udp == true)
				try {
					if (this.tcpproxy == true)
						this.proxyUDP(this.serverIP, this.portAddressUDPP,
								this.portAddressTCPS);
					this.proxyUDP(this.serverIP, this.portAddressUDPP,
							this.portAddressUDPS);
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
	}

	/**
	 * method to support proxy connections with server establishing tcp
	 * connections
	 * 
	 * @param serverIp
	 * @param proxyport
	 * @param serverport
	 * @throws IOException
	 */
	@SuppressWarnings("resource")
	public void proxyTCP(String serverIp, int proxyport, int serverport)
			throws IOException {
		// sockets for client and server
		Socket sockets = null;
		Socket socketc = null;
		ServerSocket serverSocket = new ServerSocket(proxyport);
		while (true) {
			// accept the connection from the client
			sockets = serverSocket.accept();
			// input output stream for message transfer
			InputStream is = sockets.getInputStream();
			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader br = new BufferedReader(isr);
			OutputStream os1 = sockets.getOutputStream();
			DataOutputStream dos1 = new DataOutputStream(os1);
			// get the message from the client
			String number = br.readLine().trim();
			this.milliSeconds = System.currentTimeMillis();
			// if the connection type from proxy is not mentioned then use the
			// same client connection type
			if (this.tcpproxy == false && this.udpproxy == false) {
				if (number.charAt(1) == 't')
					this.tcpproxy = true;
				else if (number.charAt(1) == 'u')
					this.udpproxy = true;
			}
			// tcp connection to server for -t in command line
			if (this.tcpproxy == true) {
				socketc = new Socket(serverIp, this.portAddressTCPS);
				InputStream is1 = socketc.getInputStream();
				InputStreamReader isr1 = new InputStreamReader(is1);
				BufferedReader br1 = new BufferedReader(isr1);
				OutputStream os = socketc.getOutputStream();
				DataOutputStream dos = new DataOutputStream(os);
				dos.writeBytes(number + "\n");
				dos.flush();
				number = br1.readLine().trim() + "$$$spliter$$$";
			}
			// udp connection to server -u in command line
			if (this.udpproxy == true) {
				DatagramSocket socket = new DatagramSocket();
				byte[] receivebuffer = new byte[1024];
				byte[] buffer = new byte[1024];
				InetAddress inet = InetAddress.getByName(serverIp);
				DatagramPacket receivePacket = new DatagramPacket(
						receivebuffer, receivebuffer.length);
				buffer = number.getBytes();
				DatagramPacket sendPacket = new DatagramPacket(buffer,
						buffer.length, inet, this.portAddressUDPS);
				socket.send(sendPacket);
				socket.receive(receivePacket);
				number = new String(receivePacket.getData()).trim();
				number = number + "$$$spliter$$$";
			}
			// append the RTT time from proxy to server
			number = number.trim();
			number = number + serverIP + "\t"
					+ (System.currentTimeMillis() - this.milliSeconds)
					+ "$$$spliter$$$";
			dos1.writeBytes(number + "\n");
			dos1.flush();
		}

	}

	/**
	 * method to support proxy connection with server with udp connection
	 * 
	 * @param serverIp
	 * @param proxyport
	 * @param serverport
	 * @throws IOException
	 */
	@SuppressWarnings("resource")
	public void proxyUDP(String serverIp, int proxyport, int serverport)
			throws IOException {
		DatagramSocket receiveSockets = new DatagramSocket(proxyport);
		while (true) {
			long milliSeconds;
			String modsentence = null;
			byte[] receivebuffer = new byte[1024];
			byte[] buffer = new byte[1024];
			DatagramPacket receivePacket = new DatagramPacket(receivebuffer,
					receivebuffer.length);
			receiveSockets.receive(receivePacket);
			milliSeconds = System.currentTimeMillis();
			InetAddress inet1 = receivePacket.getAddress();
			int port1 = receivePacket.getPort();
			modsentence = new String(receivePacket.getData()).trim();
			// if the connection type from proxy is not mentioned then use the
			// same client connection type
			if (this.tcpproxy == false && this.udpproxy == false) {
				if (modsentence.charAt(1) == 't')
					this.tcpproxy = true;
				else if (modsentence.charAt(1) == 'u')
					this.udpproxy = true;
			}
			// tcp connection to server for -t in command line
			if (this.tcpproxy == true) {
				Socket socket = new Socket(serverIp, this.portAddressTCPS);
				OutputStream os = socket.getOutputStream();
				DataOutputStream dos = new DataOutputStream(os);
				InputStream is = socket.getInputStream();
				InputStreamReader isr = new InputStreamReader(is);
				BufferedReader br = new BufferedReader(isr);
				dos.writeBytes(modsentence + "\n");
				dos.flush();
				modsentence = br.readLine();
			}
			// udp connection to server for -u in command line
			else if (this.udpproxy == true) {
				DatagramSocket sockets = new DatagramSocket();
				buffer = modsentence.getBytes();
				InetAddress inet = InetAddress.getByName(serverIp);
				DatagramPacket sendPacket = new DatagramPacket(buffer,
						buffer.length, inet, this.portAddressUDPS);
				sockets.send(sendPacket);
				sockets.receive(receivePacket);
				modsentence = new String(receivePacket.getData()).trim();
			}
			// append the RTT time from proxy to server
			modsentence = modsentence + "$$$spliter$$$";
			modsentence = modsentence + serverIP + "\t"
					+ (System.currentTimeMillis() - milliSeconds);
			buffer = modsentence.getBytes();
			DatagramPacket sendPacket1 = new DatagramPacket(buffer,
					buffer.length, inet1, port1);
			receiveSockets.send(sendPacket1);
		}
	}

	/**
	 * method to start client connection to server
	 * 
	 * @param serverIp
	 * @param portAddress
	 * @throws IOException
	 */
	@SuppressWarnings("resource")
	public void UDPClient(String serverIp, int portAddress) throws IOException {
		DatagramSocket socket = new DatagramSocket();
		// to run the functionality n times as -n
		for (int k = 0; k < tsapp.n; k++) {
			this.milliSeconds = System.currentTimeMillis();
			byte[] receivebuffer = new byte[1024];
			byte[] buffer = new byte[1024];
			buffer = new byte[1024];
			InetAddress inet = InetAddress.getByName(serverIp);
			DatagramPacket receivePacket = new DatagramPacket(receivebuffer,
					receivebuffer.length);
			String sendAllDetails = "";
			// if user wants to get the time
			if (this.task == 1 || this.task == 0) {
				sendAllDetails = this.task + "u" + "\n";
			}
			// if user wants to set the time
			else if (this.task == 2) {
				sendAllDetails = this.task + "u" + "$$$spliter$$$";
				sendAllDetails = sendAllDetails + this.userName + this.passWord
						+ "$$$spliter$$$";
				sendAllDetails = sendAllDetails + tsapp.name + "$$$spliter$$$";
			}
			// send the packet to server port
			buffer = sendAllDetails.getBytes();
			DatagramPacket sendPacket = new DatagramPacket(buffer,
					buffer.length, inet, portAddress);
			socket.send(sendPacket);
			// receive the message from server and parse it
			socket.receive(receivePacket);
			String modsentence = new String(receivePacket.getData()).trim();
			modsentence = modsentence + "$$$spliter$$$";
			long rtt=(System.currentTimeMillis() - this.milliSeconds);
			modsentence = modsentence + serverIP + "\t"
					+ rtt
					+ "$$$spliter$$$";
			// display the message recieved and the trace route
			String temp[];
			modsentence = modsentence
					.replace("$$$spliter$$$$$$spliter$$$", "|");
			temp = modsentence.replace("$$$spliter$$$", "|").split("\\|");
			System.out.println("Message received from server is " + temp[0]);
			System.out.println("S.NO\tIP Address\tTime Taken");
			System.out.println("-----------------------------------");
			for (int i = 1; i < temp.length; i++)
				System.out.println(i + "\t" + temp[i]);
			System.out.println("Total RTT is : "+rtt);
			System.out.println("");
		}
	}

	/**
	 * method to start udp server
	 * 
	 * @param portAddress
	 * @throws IOException
	 */
	@SuppressWarnings("resource")
	public void UDPServer(int portAddress) throws IOException {
		DatagramSocket receiveSocket = new DatagramSocket(portAddress);
		while (true) {
			byte[] receivebuffer = new byte[1024];
			byte sendBuffer[] = new byte[1024];
			DatagramPacket receivePacket = new DatagramPacket(receivebuffer,
					receivebuffer.length);
			receiveSocket.receive(receivePacket);
			String number = new String((receivePacket.getData())).trim();
			String temp[];
			temp = number.replace("$$$spliter$$$", "|").split("\\|");
			int port = receivePacket.getPort();
			InetAddress inet = receivePacket.getAddress();
			// if the user wants to get time
			if (temp[0].charAt(0) == '1' || temp[0].charAt(0) == '0') {
				String returnMessage = tsapp.name + "\n";
				// to send time user in calender format
				if (temp[0].charAt(0) == '0') {
					Calendar c = Calendar.getInstance();
					c.setTimeInMillis(tsapp.name);
					returnMessage = c.toInstant() + "\n";
				}
				sendBuffer = returnMessage.getBytes();
				DatagramPacket sendPacket = new DatagramPacket(sendBuffer,
						sendBuffer.length, inet, port);
				receiveSocket.send(sendPacket);
			}
			// if the user wants to set the time
			else if (temp[0].charAt(0) == '2') {
				String sendMessage;
				// if the user didnt send the username or password
				if (temp[1].equals(""))
					sendMessage = ("Invalid User:\n");
				// if username and password is sent from user
				else if (temp[1].equals(tsapp.actualUserName
						+ tsapp.actualPassWord)){
					sendMessage = ("Valid User: Data set on the server is"
							+ temp[2] + "\n");
					tsapp.name = Long.parseLong(temp[2]);
				}
				// username and password does not match
				else
					sendMessage = ("Invalid User:\n");
				// send the message to the client
				sendBuffer = sendMessage.getBytes();

				DatagramPacket sendPacket = new DatagramPacket(sendBuffer,
						sendBuffer.length, inet, port);
				receiveSocket.send(sendPacket);
			}
		}
	}

	/**
	 * method to handle client with tcp
	 * 
	 * @param serverIp
	 * @param portAddress
	 */
	public void TCPClient(String serverIp, int portAddress) {
		// to do the functionality n times for -n in command line
		for (int k = 0; k < tsapp.n; k++) {
			this.milliSeconds = System.currentTimeMillis();
			Socket socket = null;
			try {
				InetAddress.getByName(serverIp);
				socket = new Socket(serverIp, portAddress);
				OutputStream os = socket.getOutputStream();
				DataOutputStream dos = new DataOutputStream(os);
				InputStream is = socket.getInputStream();
				InputStreamReader isr = new InputStreamReader(is);
				BufferedReader br = new BufferedReader(isr);
				String sendAllDetails = new String();
				// if the user wants to get the time from server
				if (this.task == 1 || this.task == 0) {
					sendAllDetails = this.task + "t" + "\n";
				}
				// if the user wants to set the time from server
				else if (this.task == 2) {
					sendAllDetails = this.task + "u" + "$$$spliter$$$";
					sendAllDetails = sendAllDetails + this.userName
							+ this.passWord + "$$$spliter$$$";
					sendAllDetails = sendAllDetails + tsapp.name
							+ "$$$spliter$$$";
				}
				dos.writeBytes(sendAllDetails + "\n");
				dos.flush();
				// parse the message from the server to print the time and trace
				// route
				String message = br.readLine();
				long rtt=(System.currentTimeMillis() - this.milliSeconds);
				message = message.trim() + "$$$spliter$$$" + serverIP + "\t"
						+ rtt
						+ "$$$spliter$$$";
				String temp[];
				message = message.replace("$$$spliter$$$$$$spliter$$$", "|");
				temp = message.replace("$$$spliter$$$", "|").split("\\|");
				System.out
						.println("Message received from server is " + temp[0]);
				System.out.println("S.NO\tIP Address\tTime Taken");
				System.out.println("-----------------------------------");
				for (int i = 1; i < temp.length; i++)
					System.out.println(i + "\t" + temp[i]);
				System.out.println("Total RTT is : "+rtt);
				System.out.println("");
				
			} catch (Exception exception) {
				exception.printStackTrace();
			}

			finally {
				try {
					socket.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * method to start the udp server
	 * 
	 * @param portAddress
	 */
	@SuppressWarnings("resource")
	public void TCPServer(int portAddress) {

		Socket socket = null;
		try {
			ServerSocket serverSocket = new ServerSocket(portAddress);
			while (true) {
				socket = serverSocket.accept();
				InputStream is = socket.getInputStream();
				InputStreamReader isr = new InputStreamReader(is);
				BufferedReader br = new BufferedReader(isr);
				OutputStream os = socket.getOutputStream();
				DataOutputStream dos = new DataOutputStream(os);
				String sendMessage = "";
				String number = br.readLine();
				String temp[];
				temp = number.replace("$$$spliter$$$", "|").split("\\|");
				// if user wants to get the time from the server
				if (temp[0].charAt(0) == '1' || temp[0].charAt(0) == '0') {
					String sendMessage1 = tsapp.name + "";
					// if the user wants to get time in calender format
					if (temp[0].charAt(0) == '0') {
						Calendar c = Calendar.getInstance();
						c.setTimeInMillis(tsapp.name);
						sendMessage1 = c.toInstant() + "\n";
					}
					dos.writeBytes(sendMessage1 + "\n");
					dos.flush();
				}
				// if the user wants to set the time
				else if (temp[0].charAt(0) == '2') {
					// if the user did'nt send username or password
					if (temp[1].equals(""))
						sendMessage = ("Invalid User:\n");
					// if the user sends username and password
					else if (temp[1].equals(tsapp.actualUserName
							+ tsapp.actualPassWord)){
						sendMessage = ("Valid User: Data set on the server is"
								+ temp[2] + "\n");
						tsapp.name = Long.parseLong(temp[2]);
					}
					// if the username and password does'nt match
					else
						sendMessage = ("Invalid User:\n");
					dos.writeBytes(sendMessage + "\n");
					dos.flush();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				socket.close();
			} catch (Exception e) {
			}
		}
	}
}
