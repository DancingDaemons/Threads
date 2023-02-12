package tasktwo;


import taskone.Performer;
import taskone.StringList;

import java.io.IOException;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private final Socket clientSocket;
    private StringList stringList;
    private final int id;

    public ClientHandler(Socket socket, StringList stringList, int id) {
        this.clientSocket = socket;
        this.stringList = stringList;
        this.id = id;
    }

    public void run() {
        try {
            Performer performer = new Performer(clientSocket, stringList);
            performer.doPerform();
            try {
                System.out.println("Close client socket #" + id);
                clientSocket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } finally {
            try {
                clientSocket.close();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}