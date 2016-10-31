import java.io.*;
import java.net.*;
import java.util.*;

//Client can connect to a Server, and send ChatMessages through it to other connected Clients. User may enter messages to send, or
//issue commands WHOISIN to see who is currently connected, or LOGOUT to close the connection to the Server
public class Client{
	private Socket clientSocket;
	private int port;
	private ObjectInputStream ois;
	private ObjectOutputStream oos;
	private String serverName, username;

	Client(String serverName, int port, String username){
		this.port = port;
		this.serverName = serverName;
	}
	
	public void sendMessage(String msg){
		System.out.println("Message from client to server: " + msg);			//Test
		try{
		oos.writeUTF(msg);
		oos.flush();
		} catch(IOException e){
			System.out.println("Caught something: " + e);
		}
	}
	
	//Attempts to initialize the connection to the Server, and initializes the Thread to listen for data coming from Server
	public boolean start(){
			
		try{
			clientSocket = new Socket(serverName, port);
		} catch(Exception e){
			System.out.println("Caught something" + e);
		}
		
		try{
			oos = new ObjectOutputStream(clientSocket.getOutputStream());
			oos.flush();		
			ois = new ObjectInputStream(clientSocket.getInputStream());	
		} catch(IOException e){
			System.out.println("Caught something" + e);
		}
		
		new Listen().start();
		try{
			oos.writeObject(username);
		} catch(IOException e){
			System.out.println("Caught something: " + e);
			stop();
			return false;
		}
		return true;
	}
	
	//Send a ChatMessage
	public void sendMessage(ChatMessage msg){
		try{
			oos.writeObject(msg);
		} catch(IOException e){
			System.out.println("Something happened: " + e);
		}
	}
	
	//Attempts to close all data streams
	public void stop(){
			
		try { 
			if(ois != null) ois.close();
		}
		catch(Exception e){
		  System.out.println("Caught something: " + e);
		}
		try {
			if(oos != null) oos.close();
		}
		catch(Exception e){
		  System.out.println("Caught something: " + e);
		}
        try{
			if(clientSocket != null) clientSocket.close();
		}
		catch(Exception e){
		  System.out.println("Caught something: " + e);
		}
	
	}

	
	public static void main(String[] args){		
		int portNum = 1500;
		String serverAddress = "localhost";
		String username = "Anonymous";

		switch(args.length) {
			case 3:
				serverAddress = args[2];
			case 2:
				try {
					portNum = Integer.parseInt(args[1]);
				}
				catch(Exception e) {
					System.out.println("Invalid port number.");
					System.out.println("Format: > java Client [username] [portNumber] [serverAddress]");
					return;
				}
			case 1: 
				username = args[0];
			case 0:
				break;
			default:
				System.out.println("Format: > java Client [username] [portNumber] {serverAddress]");
			return;
		}
		
		Client client = new Client(serverAddress, portNum, username);
		if(!client.start()){
			return;
		}
		
		Scanner keyboard = new Scanner(System.in);
		
		while(true) {
			System.out.print("> ");
			String msg = keyboard.nextLine();
			if(msg.equalsIgnoreCase("LOGOUT")) {
				client.sendMessage(new ChatMessage(ChatMessage.LOGOUT, ""));
				break;
			}
			else if(msg.equalsIgnoreCase("WHOISIN")) {
				client.sendMessage(new ChatMessage(ChatMessage.WHOISIN, ""));
			}
			else {				
				client.sendMessage(new ChatMessage(ChatMessage.MESSAGE, msg));
			}
		}
		client.stop();
		keyboard.close();
		
	}

	//Listening Thread that waits for incoming data from Server
	class Listen extends Thread {

		public void run() {
			while(true) {
				try {
					String msg = (String) ois.readObject();
					System.out.println(msg);
					System.out.print("> ");
				}
				catch(IOException e) {
					System.out.println("Server has closed the connection: " + e);
					break;
				}
				catch(ClassNotFoundException e2) {}
			}
		}
	}
}