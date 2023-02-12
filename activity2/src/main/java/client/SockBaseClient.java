package client;

import buffers.RequestProtos.Request;
import buffers.ResponseProtos.Response;

import java.io.*;
import java.net.Socket;

class SockBaseClient {
    private static String menu = "* \nWhat would you like to do? \n 1 - to see the leader board \n 2 - to enter a game \n 3 - quit the game";
    public static void main (String args[]) throws Exception {
        Socket serverSock = null;
        OutputStream out = null;
        InputStream in = null;
        int port = 9099; // default port

        if (args.length != 2) {
            System.out.println("Expected arguments: <host(String)> <port(int)>");
            System.exit(1);
        }
        String host = args[0];
        try {
            port = Integer.parseInt(args[1]);
        } catch (NumberFormatException nfe) {
            System.out.println("[Port] must be integer");
            System.exit(2);
        }

        try {
            serverSock = new Socket(host, port);
            out = serverSock.getOutputStream();
            in = serverSock.getInputStream();

            System.out.println("Please provide your name for the server.");
            BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
            String strToSend = stdin.readLine();
            Request op = Request.newBuilder().setOperationType(Request.OperationType.NAME).setName(strToSend).build();
            Response response;
            op.writeDelimitedTo(out);

            response = Response.parseDelimitedFrom(in);
            System.out.println(response.getMessage());

            String input = "";
            while(serverSock.isConnected()){
                System.out.println(menu);
                input = stdin.readLine().trim();
                if(input.equals("1")){
                    viewLeaderBoard().writeDelimitedTo(out);
                    response = Response.parseDelimitedFrom(in);
                    System.out.println("Leader Board");
                    System.out.println(response.getLeaderList());
                }
                if (input.equals("2")) {
                    play(out, in);
                }
                if (input.equals("3") || !input.trim().equals("quit")) {
                    exit().writeDelimitedTo(out);
                    System.out.println(response.getMessage());
                    System.exit(0);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (in != null)   in.close();
            if (out != null)  out.close();
            if (serverSock != null) serverSock.close();
        }
    }


    private static Request viewLeaderBoard(){
        return Request.newBuilder().setOperationType(Request.OperationType.LEADER).build();
    }

    private static void play(OutputStream out, InputStream in) throws IOException {
        BufferedReader gamePlay = new BufferedReader(new InputStreamReader(System.in));

        boolean gameIncomplete = true;
        //Logic for client side gameplay
        newGame().writeDelimitedTo(out);
        Response response = Response.parseDelimitedFrom(in);
        System.out.println(response.getBoard());
        String userInput;
        while(gameIncomplete) {

                System.out.println("Tile1: Enter tile as letter number: (ex. a1)");
                userInput = gamePlay.readLine();
                if(userInput.equals("quit")){
                    exit().writeDelimitedTo(out);
                }else {
                    flipTile1(userInput).writeDelimitedTo(out);
                }
                response = Response.parseDelimitedFrom(in);
                if (response.getResponseType() == Response.ResponseType.ERROR) {
                    System.out.println(response.getMessage());
                } else if (response.getSecond()) {
                    System.out.println("Flipped tiles....");
                    System.out.println(response.getFlippedBoard());
                    System.out.println("Current board...");
                    System.out.println(response.getBoard());

                    System.out.println("Tile2: Enter tile as letter number: (ex. a1)");
                    userInput = gamePlay.readLine();
                    if(userInput.equals("quit")){
                        exit().writeDelimitedTo(out);
                    }else {
                        flipTile2(userInput).writeDelimitedTo(out);
                    }
                    response = Response.parseDelimitedFrom(in);
                    if (response.getResponseType() == Response.ResponseType.ERROR) {
                        System.out.println(response.getMessage());
                    } else if (!response.getSecond()) {
                        if (response.getEval()) {
                            System.out.println("Tiles match!");
                        } else {
                            System.out.println("The tiles don't match");
                        }
                        System.out.println("Flipped tiles....");
                        System.out.println(response.getFlippedBoard());

                        System.out.println("Current board...");
                        System.out.println(response.getBoard());
                    }
                }

                if (response.getResponseType() == Response.ResponseType.WON) {
                    gameIncomplete = false;
                    System.out.println("VICTORY");
                    System.out.println(response.getBoard());
                } else if (response.getResponseType() == Response.ResponseType.BYE) {
                    System.out.println(response.getMessage());
                    System.exit(0);
                }
        }
    }


    private static Request newGame(){
        return Request.newBuilder().setOperationType(Request.OperationType.NEW).build();
    }

    private static Request flipTile1(String tile1){
        return Request.newBuilder().setOperationType(Request.OperationType.TILE1)
                .setTile(tile1)
                .build();

    }

    private static Request flipTile2(String tile2){
        return Request.newBuilder().setOperationType(Request.OperationType.TILE2)
                .setTile(tile2)
                .build();
    }

    private static Request exit(){
        return Request.newBuilder().setOperationType(Request.OperationType.QUIT).build();
    }
}


