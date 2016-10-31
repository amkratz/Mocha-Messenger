import java.io.*;
import java.text.SimpleDateFormat;
import java.net.*;
import java.util.*;

//Server can receive multiple connection from Clients, giving them all unique id's and storing them into an array
public class Server{

	private int port;
	private SimpleDateFormat sdf;
	private boolean keepGoing;
	private ArrayList<ClientThread> al;
	private int uniqueId;

	
	public Server(int port){
		this.port = port;
		al = new ArrayList<ClientThread>();
		sdf = new SimpleDateFormat("HH:mm:ss");
	}
	
	//Waits to accept any incoming connnections, if keepGoing value becomes false the Server will attempt to close its connections
	public void start(){
		keepGoing = true;
		
		try{
			ServerSocket serverSocket = new ServerSocket(port);
			while(keepGoing){
				System.out.println("Started properly, waiting for connection(s): ");
				Socket clientSocket = serverSocket.accept();
				System.out.println("Accepted connection, waiting for IO streams to be established: ");	
				
				if(!keepGoing)
					break;
				
				//Create new ClientThread and add to the ArrayList
				ClientThread t = new ClientThread(clientSocket);
				al.add(t);
				t.start();
			}
		
			serverSocket.close();
			for(int x = 0; x < al.size(); x++){
				ClientThread tTemp = al.get(x);
				tTemp.ois.close();
				tTemp.oos.close();
			}
		}catch(IOException e){
				System.out.println("Caught something: " + e);
		}	
	}
	
	public void setStop(){
		this.keepGoing = false;
	}
	
	public void removeClient(int id){
		for(int x = 0; x < al.size(); x++){
			ClientThread tTemp = al.get(x);
			if(tTemp.id == id){
				al.remove(x);
				return;
			}
		}
	}
	
	//Broadcasts a message to all connected clients
	private synchronized void broadcast(String message) {
		String time = sdf.format(new Date());
		String messageWF = time + " " + message + "\n";

		for(int i = al.size(); --i >= 0;) {
			ClientThread tTemp = al.get(i);
			if(!tTemp.writeMsg(messageWF)) {
				al.remove(i);
				System.out.println("Disconnected Client " + tTemp.username + " removed from list.");
			}
		}
	}
	
	public static void main(String[] args){ //Starts the server using  port 1500 if no port is designated in args
		int portNumber = 1500;
		
		switch(args.length) {
		case 1:
			try {
				portNumber = Integer.parseInt(args[0]);
			}
			catch(Exception e) {
				System.out.println("Invalid port number.");
				System.out.println("Format: > java Server [portNumber]");
				return;
			}
		case 0:
			break;
		default:
			System.out.println("Format: > java Server [portNumber]");
			return;
			
	}
		Server server = new Server(portNumber);
		server.start();
		
		System.out.println("Goodbye!");
		System.exit(0);
	}
	
	//Thread listening for incoming data from this Client
	class ClientThread extends Thread{
		Socket clientSocket;
		ObjectInputStream ois;
		ObjectOutputStream oos;
		String fromClient = "";
		int id = 0;
		ChatMessage msg;
		String username;
		Date date;
		
		ClientThread(Socket socket){
			id = ++uniqueId;
			this.clientSocket = socket;
			try{
				oos = new ObjectOutputStream(clientSocket.getOutputStream());
				oos.flush();
				ois = new ObjectInputStream(clientSocket.getInputStream());
				username = (String) ois.readObject();                           //Take in first object expecting username from Client
				System.out.println("IO Streams set, waiting for client input: ");
			} catch(IOException e){
			System.out.println("Caught something: " + e);
			} catch(ClassNotFoundException ee){}	//Do nothing
		}
		
		public void run(){
			boolean keepGoing = true;
			
			while(keepGoing){
				try{
					msg = (ChatMessage) ois.readObject();
				} catch(IOException e){
					System.out.println("Something happened: " + e);
				}catch(ClassNotFoundException ee){}
				String message = msg.getMessage();
				
				switch(msg.getType()) {

				case ChatMessage.MESSAGE:
					broadcast(username + ": " + message);
					break;
				case ChatMessage.LOGOUT:
					System.out.println(username + " disconnected");
					keepGoing = false;
					break;
				case ChatMessage.WHOISIN:
					writeMsg("List of the users connected at " + sdf.format(new Date()) + "\n");
					
					for(int i = 0; i < al.size(); ++i) {
						ClientThread ct = al.get(i);
						writeMsg((i+1) + ") " + ct.username + " since " + ct.date);
					}
					break;
				
				}
			}
		}
		
		private void close() {
			try {
				if(oos != null) oos.close();
			}
			catch(Exception e){
			  System.out.println("Caught something: " + e);
			}
			try {
				if(ois != null) ois.close();
			}
			catch(Exception e){
			  System.out.println("Caught something: " + e);
			}
			try {
				if(clientSocket != null) clientSocket.close();
			}
			catch (Exception e){
			  System.out.println("Caught something: " + e);
			}
		}

		private boolean writeMsg(String msg) {
			if(!clientSocket.isConnected()) {
				close();
				return false;
			}
			try {
				oos.writeObject(msg);
			}
			catch(IOException e) {
				System.out.println("Something went wrong: ");
			}
			return true;
		}
	}
}
