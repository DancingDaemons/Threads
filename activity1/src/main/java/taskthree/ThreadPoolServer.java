package taskthree;

import tasktwo.ClientHandler;
import taskone.StringList;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class ThreadPoolServer {

    public static void main(String[] args) {
        int port = 8000;
        int workers = 1;

        if (args.length == 2) {
            System.out.println("Expected Arguments: <portNumber> <workers(int)>");
            port = Integer.parseInt(args[0]);
            workers = Integer.parseInt(args[1]);
        }
        Executor pool = Executors.newFixedThreadPool(workers);
        ServerSocket server = null;
        StringList strings = new StringList();
        Integer id = 1;
        try {
            server = new ServerSocket(port);
            server.setReuseAddress(true);
            for (int i = 0; i < workers; i++) {
                Socket client = server.accept();
                ClientHandler clientSock = new ClientHandler(client, strings, id++);
                System.out.printf("New client connected, id:%d at %s \n", id, client.getInetAddress().getHostAddress());
                //new Thread(clientSock).start();
                pool.execute(clientSock);
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
