import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Scanner;

public class ClientTicTacToe{
	
	private DatagramSocket clientSocket;
	private InetAddress ip;
	private byte[] recivedData;
	private byte[] sendData;
	private String sendingMessage;
	private String recivedMessage;
	
	private Thread thread;
	private Thread listener;
	
	private long lastTime, timer;
	
	private boolean running = false;
	private boolean connected = false;
	private int id = 0;
	
	private int[] grid = {-1, -1, -1,
			  				-1 ,-1, -1,
			  				-1, -1, -1};
	
	private String prefixClient = "[ CLIENT ]";
	private String prefixServer = "[ SERVER ]";
	
	public static void main(String[] args)
	{
		ClientTicTacToe client = new ClientTicTacToe();
		client.start();
	}
	
	public ClientTicTacToe()
	{	
		try {
			clientSocket = new DatagramSocket();
			ip = InetAddress.getByName("localhost");
		} catch (SocketException | UnknownHostException e) {
			e.printStackTrace();
		}
		sendData("1"); // connecting to server
	}
	
	public void drawGrid()
	{
		System.out.println(grid[0] + " "+ grid[1] +" "+ grid[2]);
		System.out.println(grid[3] + " "+ grid[4] + " "+ grid[5]);
		System.out.println(grid[6] +" "+ grid[7] +" "+ grid[8]);
		System.out.println();
	}
	
	public void processData()
	{
		String[] tokens = recivedMessage.split(":");
		
		if(Integer.parseInt(tokens[0]) == 1)
		{
			connected = true;
			id = Integer.parseInt(tokens[1]);
			System.out.println("Po³¹czono z serwrrem z id: " + id );
		}else if(Integer.parseInt(tokens[0]) == 3)
		{
			
			for(int i = 0; i < grid.length; i++)
			{
				grid[i] = Integer.parseInt(tokens[i+1]);
//				System.out.println(tokens[2]);
			}
		}
		
		recivedMessage = null;
		
	}
	
	public void reciveData()
	{
		recivedData = new byte[1024];
		DatagramPacket recivedPacket = new DatagramPacket(recivedData, recivedData.length);
		try {
			clientSocket.receive(recivedPacket);

		} catch (IOException e) {
			e.printStackTrace();
		}
		recivedMessage = new String(recivedPacket.getData()).trim().replaceAll(" "," ").concat(":" + recivedPacket.getAddress() + ":" + recivedPacket.getPort());
		System.out.println(prefixClient + " " + "Data recived from: " + recivedPacket.getAddress() + ":" + recivedPacket.getPort());
		processData();
	}
	
	public void sendData(String message)
	{
		sendData = new byte[1024];
		byte[] b = message.getBytes();
		for(int i = 0; i < b.length; i++)
		{
			sendData[i] = b[i];
		}
		
		DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, ip, 61616);
		try {
			clientSocket.send(sendPacket);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		sendingMessage = null;
		
	}
	
	public synchronized void start()
	{
		if(running) return;
		running = true;
		
		thread = new Thread(() -> {
			while(running)
			{
				drawGrid();
				sendData("2:2:" + id);
				sendData("3");
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		});
		listener = new Thread(() ->{
			while(running)
			{
				System.out.println(prefixClient + " Listening...");
				reciveData();
			}
		});
		listener.start();
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
