import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;


public class Server {

    static int clientId;
    ArrayList<ClientThread> clientThreadArrayList;
    SimpleDateFormat sdf;
    private int port;
    private boolean isRunning;

    public Server(int port) {
        this.port = port;
        sdf = new SimpleDateFormat("HH:mm:ss");
        clientThreadArrayList = new ArrayList<ClientThread>();
    }

    public void start() {
        isRunning = true;
        try
        {
            ServerSocket serverSocket = new ServerSocket(port);
            while(isRunning)
            {
                System.out.println("Server waiting on port " + port + ".");
                Socket socket = serverSocket.accept();
                if(!isRunning)
                    break;
                ClientThread clientThread = new ClientThread(this, socket);
                clientThreadArrayList.add(clientThread);
                clientThread.start();
            }

            try {
                serverSocket.close();
                for(int i = 0; i < clientThreadArrayList.size(); ++i) {
                    ClientThread clientThread = clientThreadArrayList.get(i);
                    try {

                        clientThread.sInput.close();
                        clientThread.sOutput.close();
                        clientThread.socket.close();
                    }
                    catch(IOException ioE) {
                    }
                }
            }
            catch(Exception e) {
                System.out.println("Exception closing the server and clients: " + e);
            }
        }
        catch (IOException e) {
            String msg = sdf.format(new Date()) + " Exception on new ServerSocket: " + e + "\n";
            System.out.println(msg);
        }
    }

    synchronized boolean sendToAll(String message) {

        String time = sdf.format(new Date());
        String[] w = message.split(" ",3);
        boolean isPrivate = false;
        if(w[1].charAt(0)=='@')
            isPrivate=true;

        if(isPrivate)
        {
            String tocheck=w[1].substring(1, w[1].length());
            message=w[0]+w[2];
            String messageLf = time + " " + message + "\n";
            boolean found=false;
            for(int y = clientThreadArrayList.size(); --y>=0;)
            {
                ClientThread ct1= clientThreadArrayList.get(y);
                String check=ct1.getUsername();
                if(check.equals(tocheck))
                {

                    if(!ct1.writeMsg(messageLf)) {
                        clientThreadArrayList.remove(y);
                        System.out.println("Disconnected Client " + ct1.username + " removed from list.");
                    }
                    found=true;
                    break;
                }
            }

            if(!found)
            {
                return false;
            }
        }

        else
        {
            String messageLf = time + " " + message + "\n";
            System.out.print(messageLf);
            for(int i = clientThreadArrayList.size(); --i >= 0;) {
                ClientThread ct = clientThreadArrayList.get(i);

                if(!ct.writeMsg(messageLf)) {
                    clientThreadArrayList.remove(i);
                    System.out.println("Disconnected Client " + ct.username);
                }
            }
        }
        return true;
    }

    synchronized void remove(int id) {
        String disconnectedClient = "";
        for(int i = 0; i < clientThreadArrayList.size(); ++i) {
            ClientThread ct = clientThreadArrayList.get(i);

            if(ct.id == id) {
                disconnectedClient = ct.getUsername();
                clientThreadArrayList.remove(i);
                break;
            }
        }
        sendToAll(  disconnectedClient + " has left the chat room." );
    }

    public static void main(String[] args) {
        int portNumber = 8888;
        switch(args.length) {
            case 1:
                try {
                    portNumber = Integer.parseInt(args[0]);
                }
                catch(Exception e) {
                    System.out.println("Wrong port number.");
                    System.out.println("Try this > java Server [portNumber]");
                    return;
                }
            case 0:
                break;
            default:
                System.out.println("Usage is: > java Server [portNumber]");
                return;

        }
        Server server = new Server(portNumber);
        server.start();
    }


}