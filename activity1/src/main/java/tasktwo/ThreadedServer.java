package tasktwo;

import taskone.StringList;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ThreadedServer {
    static int port = 8000;

    public static void main(String args[]) {

        if (args.length == 1) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (Exception e) {
                System.out.println("Unable to parse port number");
                System.exit(0);
            }
        }
        ServerSocket server = null;
        StringList strings = new StringList();
        Integer id = 1;
        try {
            server = new ServerSocket(port);
            server.setReuseAddress(true);
            while (true) {
                Socket client = server.accept();
                ClientHandler clientSock = new ClientHandler(client, strings, id++);
                System.out.printf("New client connected, id:%d at %s \n", id, client.getInetAddress().getHostAddress());
                new Thread(clientSock).start();
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            if (server != null) {
                try {
                    server.close();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}

