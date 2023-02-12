package server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Class: Game 
 * Description: Game class that can load an ascii image
 * Class can be used to hold the persistent state for a game for different threads synchronization is not taken care of .
 * You can change this Class in any way you like or decide to not use it at all
 * I used this class in my SockBaseServer to create a new game and keep track of the current image evenon differnt threads. 
 * My threads each get a reference to this Game
 */

public class Game {
    private char[][] original;
    private char[][] hidden;
    private int col;
    private int row;
    private boolean won;
    private List<String> files = new ArrayList<>();


    public Game(){
        won = true; // setting it to true, since then in newGame() a new image will be created
        files.add("board1.txt");
        files.add("board2.txt");
        files.add("board3.txt");
        files.add("board4.txt");
        files.add("board5.txt");
        files.add("board6.txt");
    }

    public void setWon(){
        won = true;
    }

    public boolean getWon() {
        return won;
    }

    public void newGame(){
        if (won) {
            won = false; 
            List<String> rows = new ArrayList<String>();

            try{
                Random rand = new Random();
                col = 0;
                int randInt = rand.nextInt(files.size());
                File file = new File(
                        Game.class.getResource("/"+files.get(randInt)).getFile()
                        );
                BufferedReader br = new BufferedReader(new FileReader(file));
                String line;
                while ((line = br.readLine()) != null) {
                    if (col < line.length()) {
                        col = line.length();
                    }
                    rows.add(line);
                }
            }
            catch (Exception e){
                System.out.println("File load error");
            }

            String[] rowsASCII = rows.toArray(new String[0]);

            row = rowsASCII.length;

            original = new char[row][col];
            for(int i = 0; i < row; i++) {
                char[] splitRow = rowsASCII[i].toCharArray();
                for (int j = 0; j < splitRow.length; j++) {
                    original[i][j] = splitRow[j];
                }
            }

            hidden = new char[row][col];
            for(int i = 0; i < row; i++){
                for(int j = 0; j < col; j++){
                    if(i == 0 || j < 2)
                        hidden[i][j] = original[i][j];
                    else if(original[i][j] != '|')
                        hidden[i][j] = '?';
                    else
                        hidden[i][j] = '|';
                }
            }
        }
        else {
        }
    }

    public String showBoard() {
        StringBuilder sb = new StringBuilder();
        for (char[] subArray : original) {
            sb.append(subArray);
            sb.append("\n");
        }
        return sb.toString();
    }

    synchronized public String getBoard(){
        StringBuilder sb = new StringBuilder();
        for (char[] subArray : hidden) {
            sb.append(subArray);
            sb.append("\n");
        }
        return sb.toString();
    }

    public String tempFlipWrongTiles(int tile1row, int tile1col) {
        hidden[tile1row][tile1col] = original[tile1row][tile1col];
        StringBuilder sb = new StringBuilder();
        for (char[] subArray : hidden) {
            sb.append(subArray);
            sb.append("\n");
        }
        hidden[tile1row][tile1col] = '?';
        return sb.toString();
    }

    public String tempFlipWrongTiles(int tile1row, int tile1col, int tile2row, int tile2col) {
        hidden[tile1row][tile1col] = original[tile1row][tile1col];
        hidden[tile2row][tile2col] = original[tile2row][tile2col];
        StringBuilder sb = new StringBuilder();
        for (char[] subArray : hidden) {
            sb.append(subArray);
            sb.append("\n");
        }
        hidden[tile1row][tile1col] = '?';
        hidden[tile2row][tile2col] = '?';
        return sb.toString();
    }

    public synchronized char getTile(int row, int col) {
        if (row < original.length && col < original[0].length)
            return original[row][col];
        else
            return '?';
    }
    public boolean invalidTile(int r, int c){
        return (c > col || r > row);
    }

    public boolean alreadyFlipped(int row, int column){
        return hidden[row][column] == original[row][column];
    }

    public synchronized boolean compareTwoTiles(int r1, int c1, int r2, int c2){
        return original[r1][c1] == original[r2][c2];
    }

    public synchronized void replaceMatchingCharacters(int t1r, int t1c, int t2r, int t2c) {
        hidden[t1r][t1c] = original[t1r][t1c];
        hidden[t2r][t2c] = original[t2r][t2c];
        System.out.println(getBoard());
    }

    public int getCol(){
        return col;
    }

    public  int getRow(){
        return row;
    }

    public void checkWin() {
        boolean equal = true;
        for (int r = 0; r < row; r++) {
            for (int c = 0; c < col; c++) {
                if (hidden[r][c] != original[r][c]) {
                    equal = false;
                }
            }
        }
        if (equal) {
            setWon();
        }
    }
}
