package server;

import buffers.RequestProtos.Logs;
import buffers.RequestProtos.Message;
import buffers.RequestProtos.Request;
import buffers.ResponseProtos;
import buffers.ResponseProtos.Response;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

class SockBaseServer extends Thread{
    static String logFilename = "logs.txt";
    static String leaderboardFile = "leaderboard.xml";
    ServerSocket serv = null;
    InputStream in = null;
    OutputStream out = null;
    Socket clientSocket = null;
    int port = 9099; // default port
    Game game;
    private int id;

    HashMap<String, Integer> letterIntEquivalence = new HashMap<>() {
        {
            put("a", 1);
            put("b", 2);
            put("c", 3);
            put("d", 4);
        }
    };
    List<LeaderBoardEntry> leaderboard;
    XmlMapper mapper;

    String name = "";
    int t1row = 0;
    int t1col = 0;
    int t2row = 0;
    int t2col = 0;
    boolean closeSocket = false;
    public SockBaseServer(Socket sock, Game game, Integer id)  {
        this.clientSocket = sock;
        this.game = game;
        this.id = id;
        mapper = new XmlMapper();
    }

    public void start() {
        try {
            in = clientSocket.getInputStream();
            out = clientSocket.getOutputStream();
            System.out.println("Connected: ID# "+ id);
            //need to loop, but only process request once
            while(true) {
                Request op = Request.parseDelimitedFrom(in);
                Response response = requestHandler(op);
                response.writeDelimitedTo(out);
                if(closeSocket){
                    clientSocket.close();
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            error(ex.getMessage());
        }
    }

    public Response requestHandler(Request op) {
        String errorMessage = "Unknown operation";
        // LOGGING IN
        Request.OperationType type = op.getOperationType();
        switch (type) {
            case NAME -> {
                name = op.getName();
                writeToLog(name, Message.CONNECT);
                System.out.println("Got a connection and a name: " + name);
                return Response.newBuilder()
                        .setResponseType(Response.ResponseType.GREETING)
                        .setMessage("Hello " + name + " and welcome.")
                        .build();
            }
            case LEADER -> {return leaderboardResponse();}
            case NEW ->  {
                game.newGame();
                return Response.newBuilder()
                        .setResponseType(Response.ResponseType.PLAY)
                        .setBoard(game.showBoard())
                        .setEval(false)
                        .setSecond(false)
                        .build();
            }
            case TILE1 -> {return parseTile1Request(op);}
            case TILE2 -> {return parseTile2Request(op);}
            case QUIT -> {
                closeSocket = true;
                return Response.newBuilder()
                        .setResponseType(Response.ResponseType.BYE)
                        .setMessage("Goodbye " + name)
                        .build();
            }
            default -> error(errorMessage);
        }
        return error(errorMessage);
    }



    private Response parseTile2Request(Request op){
        String r = op.getTile().trim();
        System.out.println(r);
        if (r.length() > 2) {
            return error("Invalid Tile: Unable to parse appropriately, please enter only 1 letter and 1 number");
        }
        String row = op.getTile().substring(0, 1);
        try {
            t2row = letterIntEquivalence.get(row);
        } catch (Exception e) {
            return error("Invalid Tile: " + row + " is out of bounds");
        }
        String col = op.getTile().substring(1, 2);
        t2col = Integer.parseInt(col) * 2;
        if (game.invalidTile(t2row, t2col)) {
            return error("Invalid Tile: Out of Bounds");
        }
        if (game.alreadyFlipped(t2row, t2col)) {
            return error("Invalid Tile: Already flipped");
        } else {
            String flippedBoard = game.tempFlipWrongTiles(t1row, t1col, t2row, t2col);
            //Required Fields: board, flippedBoard, second = false, eval (is match?)
            if (game.compareTwoTiles(t1row, t1col, t2row, t2col)) {
                game.replaceMatchingCharacters(t1row, t1col, t2row, t2col);
                game.checkWin();

                if (game.getWon()) {
                    //need to handle case for existing leader
                    LeaderBoardEntry entry = new LeaderBoardEntry(name, 1, 1);
                    writeToLeaderboard(entry);

                    return Response.newBuilder()
                            .setResponseType(Response.ResponseType.WON)
                            .setBoard(game.getBoard())
                            .build();
                } else {
                    return Response.newBuilder()
                            .setResponseType(Response.ResponseType.PLAY)
                            .setFlippedBoard(flippedBoard)
                            .setBoard(game.getBoard())
                            .setEval(true)
                            .setSecond(false)
                            .build();
                }
            } else {
                return Response.newBuilder()
                        .setResponseType(Response.ResponseType.PLAY)
                        .setFlippedBoard(flippedBoard)
                        .setBoard(game.getBoard())
                        .setEval(false)
                        .setSecond(false)
                        .build();
            }
        }
    }

    private Response parseTile1Request(Request op){
        String r = op.getTile().trim();
        if (r.length() != 2){
            return error("Invalid Tile: Unable to parse appropriately, please enter only 1 letter and 1 number");
        } else {
            String row = op.getTile().substring(0, 1);
            String col = op.getTile().substring(1, 2);
            try {
                t1row = letterIntEquivalence.get(row);
            } catch (Exception e) {
                return error("Invalid Tile: " + row + " is out of bounds");
            }
            t1col = Integer.parseInt(col) * 2;
            if (game.invalidTile(t1row, t1col)) {
                return error("Invalid Tile: Out of Bounds");
            }
            if (game.alreadyFlipped(t1row, t1col)) {
                return error("Invalid Tile: Already flipped");
            }
        }

        return Response.newBuilder()
                .setResponseType(Response.ResponseType.PLAY)
                .setBoard(game.getBoard())
                .setFlippedBoard(game.tempFlipWrongTiles(t1row, t1col))
                .setEval(false)
                .setSecond(true)
                .build();
    }

    public Response error(String error) {
        return Response.newBuilder()
                .setResponseType(Response.ResponseType.ERROR)
                .setMessage(error)
                .build();
    }


    private Response leaderboardResponse(){
        List<LeaderBoardEntry> leadersFromFile = readLeaderboardFile();
        //show leaderboard
        List<ResponseProtos.Entry> displayLeaders = new ArrayList<>();
        for (int i = 0; i < leadersFromFile.size(); i++) {
            ResponseProtos.Entry leader = ResponseProtos.Entry.newBuilder()
                    .setName(leadersFromFile.get(i).getName())
                    .setLogins(leadersFromFile.get(i).getLogins())
                    .setWins(leadersFromFile.get(i).getWins()).build();
            displayLeaders.add(leader);
        }

        return Response.newBuilder()
                .setResponseType(Response.ResponseType.LEADER)
                .addAllLeader(displayLeaders)
                .build();
    }
    synchronized public void writeToLeaderboard(LeaderBoardEntry entry){
        System.out.println("New Entry: " + entry);
        LeaderBoardEntry leader = entry;
        List<LeaderBoardEntry> leaders = readLeaderboardFile();
        System.out.println("Leaders size = " + leaders.size());
        for (LeaderBoardEntry l : leaders) {
            if(l.getName().equals(name)){
                l.incrementWins();
                l.incrementLogins();
                leader = l;
            }
        }
        try {
            File file = new File(leaderboardFile);
            mapper = new XmlMapper();
            mapper.writeValue(file, leader);
        } catch (IOException e) {
            System.out.println("Unable to write");;
        }
    }

    public List<LeaderBoardEntry> readLeaderboardFile(){
        List<LeaderBoardEntry> leaderBoardEntryList = new ArrayList<>();
        try {
            File file = new File(leaderboardFile);
            leaderBoardEntryList= Collections.singletonList(mapper.readValue(file, LeaderBoardEntry.class));
        } catch (FileNotFoundException e) {
            System.out.println(leaderboardFile + ": File not found.");
        } catch (IOException e) {
            System.out.println("Error reading file");
        }
        return leaderBoardEntryList;
    }

    public static void writeToLog(String name, Message message){
        try {
            // read old log file 
            Logs.Builder logs = readLogFile();

            // get current time and data
            Date date = java.util.Calendar.getInstance().getTime();
            System.out.println(date);

            // we are writing a new log entry to our log
            // add a new log entry to the log list of the Protobuf object
            logs.addLog(date.toString() + ": " +  name + " - " + message);
            // open log file
            FileOutputStream output = new FileOutputStream(logFilename);
            Logs logsObj = logs.build();

            // This is only to show how you can iterate through a Logs object which is a protobuf object
            // which has a repeated field "log"

            for (String log: logsObj.getLogList()){
                System.out.println(log);
            }
            // write to log file
            logsObj.writeTo(output);
        }catch(Exception e){
            System.out.println("Issue while trying to save");
        }
    }

    public static Logs.Builder readLogFile() throws Exception{
        Logs.Builder logs = Logs.newBuilder();

        try {
            // just read the file and put what is in it into the logs object
            return logs.mergeFrom(new FileInputStream(logFilename));
        } catch (FileNotFoundException e) {
            System.out.println(logFilename + ": File not found.  Creating a new file.");
            return logs;
        }
    }


    public static void main (String args[]) throws Exception {
        Game game = new Game();
        Integer connections = 0;
        if (args.length != 2) {
            System.out.println("Expected arguments: <port(int)> <delay(int)>");
            System.exit(1);
        }
        int port = 9099; // default port
        int sleepDelay = 10000; // default delay
        Socket socket;
        try {
            port = Integer.parseInt(args[0]);
            sleepDelay = Integer.parseInt(args[1]);
        } catch (NumberFormatException nfe) {
            System.out.println("[Port|sleepDelay] must be an integer");
            System.exit(2);
        }
        try {
            ServerSocket server = new ServerSocket(port);
            while (true) {
                server.setReuseAddress(true);
                socket = server.accept();
                connections++;
                SockBaseServer baseServer = new SockBaseServer(socket, game, connections);
                baseServer.start();
            }

        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}

