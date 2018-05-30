import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Scanner;

public class ServerTicTacToe implements Runnable{

	private DatagramSocket serverSocket;
	private byte[] recivedData;
	private byte[] sendData;
	
	private InetAddress feedbackAddress;
	private int feedbackPort;
	
	private Thread thread;
	
	private boolean running = false;
	private boolean busy = false;
	private int clients = 0;
	
	private String recivedMessage;
	
	private String prefixClient = "[ CLIENT ]";
	private String prefixServer = "[ SERVER ]";
	
	private int[] grid = {-1, -1, -1,
						  -1 ,-1, -1,
						  -1, -1, -1};
	
	public static void main(String[] args)
	{
		ServerTicTacToe server = new ServerTicTacToe();
		server.start();
	}
	
	public ServerTicTacToe()
	{
		try {
			serverSocket = new DatagramSocket(61616);
		} catch (SocketException e) {
			e.printStackTrace();
		}
		
		
	}

	@Override
	public void run() {
		while(running)
		{
			if(busy) return;
			System.out.println("Online: " + clients +"/2");
			System.out.println(prefixServer + " Listening...");
			reciveData();
		}
	}
	
	public void reciveData()
	{
		busy = true;
		recivedData = new byte[1024];
		DatagramPacket recivedPacket = new DatagramPacket(recivedData, recivedData.length);
		try {
			serverSocket.receive(recivedPacket);

		} catch (IOException e) {
			e.printStackTrace();
		}
		recivedMessage = new String(recivedPacket.getData()).trim().replaceAll(" "," ").concat(":" + recivedPacket.getAddress() + ":" + recivedPacket.getPort());
		System.out.println(prefixServer + " " + "Data recived from: " + recivedPacket.getAddress() + ":" + recivedPacket.getPort());
		processData();
	}
	
	public void sendData(String s)
	{
		busy = true;
		sendData = new byte[1024];
		
		byte[] b = s.getBytes();
		for(int i = 0; i < b.length; i++)
		{
			sendData[i] = b[i];
		}
		
		DatagramPacket sendingPacket = new DatagramPacket(sendData, sendData.length, feedbackAddress, feedbackPort);

		try {
			serverSocket.send(sendingPacket);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		feedbackAddress = null;
		feedbackPort = -1;
		busy = false;
	}
	
	public void processData()
	{
		String[] tokens = recivedMessage.split(":");
		try {
			feedbackAddress = InetAddress.getByName(tokens[tokens.length - 2].replaceAll("/", ""));
			feedbackPort = Integer.parseInt(tokens[tokens.length-1]);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		if(Integer.parseInt(tokens[0]) == 0)
		{
			clients --;
		}
		else if(Integer.parseInt(tokens[0]) == 1)
		{
			clients++;
			sendData("1:" + Integer.toString(clients));
		}else if(Integer.parseInt(tokens[0]) == 2)
		{
			grid[Integer.parseInt(tokens[1])] = Integer.parseInt(tokens[2]);
		}else if(Integer.parseInt(tokens[0]) == 3)
		{
			sendData("3:" + grid[0] +":" + grid[1] +":"+ grid[2] +":"+ grid[3] +":"+ grid[4] +":"+ grid[5] +":"+ grid[6] +":"+ grid[7] +":" + grid[8]);
		}
		
		recivedMessage = null;
		busy = false;
		
	}
	
	public synchronized void start()
	{
		if(running) return;
		running = true;
		
		thread = new Thread(this);
		thread.start();
	}
	
	public synchronized void stop()
	{
		if(!running) return;
		running = false;
		try {
			thread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
}
