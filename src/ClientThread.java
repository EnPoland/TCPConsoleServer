import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Date;

 public class ClientThread extends Thread {

    private Server server;
    Socket socket;
    ObjectInputStream sInput;
    ObjectOutputStream sOutput;
    int id;
    String username;
    Message cm;
    String date;

    ClientThread(Server server, Socket socket) {
        this.server = server;
        id = ++Server.clientId;
        this.socket = socket;
        try
        {
            sOutput = new ObjectOutputStream(socket.getOutputStream());
            sInput  = new ObjectInputStream(socket.getInputStream());

            username = (String) sInput.readObject();
            server.sendToAll( username + " has joined the chat room." );
        }
        catch (IOException e) {
            System.out.println("Exception creating new Input/output Streams: " + e);
            return;
        }
        catch (ClassNotFoundException e) {
        }
        date = new Date().toString() + "\n";
    }

    public String getUsername() {
        return username;
    }

    public void run() {
        boolean keepGoing = true;
        while(keepGoing) {

            try {
                cm = (Message) sInput.readObject();
            }
            catch (IOException e) {
                System.out.println(username + " Exception reading Streams: " + e);
                break;
            }
            catch(ClassNotFoundException e2) {
                break;
            }
            String message = cm.getMessage();

            switch(cm.getType()) {

                case Message.MESSAGE:
                    boolean confirmation =  server.sendToAll(username + ": " + message);
                    if(confirmation==false){
                        String msg = "Sorry. No such user exists." ;
                        writeMsg(msg);
                    }
                    break;
                case Message.LOGOUT:
                    System.out.println(username + " disconnected with a LOGOUT message.");
                    keepGoing = false;
                    break;
                case Message.LIST:
                    writeMsg("List of the users connected at " + server.sdf.format(new Date()) + "\n");
                    for(int i = 0; i < server.clientThreadArrayList.size(); ++i) {
                        ClientThread ct = server.clientThreadArrayList.get(i);
                        writeMsg((i+1) + ") " + ct.username + " since " + ct.date);
                    }
                    break;
            }
        }
        server.remove(id);
        close();
    }


    private void close() {
        try {
            if(sOutput != null) sOutput.close();
        }
        catch(Exception e) {}
        try {
            if(sInput != null) sInput.close();
        }
        catch(Exception e) {};
        try {
            if(socket != null) socket.close();
        }
        catch (Exception e) {}
    }


    boolean writeMsg(String msg) {
        if(!socket.isConnected()) {
            close();
            return false;
        }
        try {
            sOutput.writeObject(msg);
        }
        catch(IOException e) {
            System.out.println("Error sending message to " + username );
            System.out.println(e.toString());
        }
        return true;
    }
}
