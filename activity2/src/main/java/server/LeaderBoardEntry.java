package server;

import java.io.Serializable;

public class LeaderBoardEntry implements Serializable {
    private String name;
    private Integer wins;
    private Integer logins;

    LeaderBoardEntry(){};

    public LeaderBoardEntry(String name, int wins, int logins) {
        this.name = name;
        this.wins = wins;
        this.logins = logins;
    }

    @Override
    public String toString(){
        return "Name: " + name + " Wins: " + wins + " Logins: " + logins;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getWins() {
        return wins;
    }

    public void setWins(Integer wins) {
        this.wins = wins;
    }

    public Integer getLogins() {
        return logins;
    }

    public void setLogins(Integer logins) {
        this.logins = logins;
    }

    public void incrementWins(){
        wins++;
    }
    public void incrementLogins(){
        logins++;
    }
}
