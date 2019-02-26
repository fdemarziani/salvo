package com.codeoftheweb.salvo;


import org.hibernate.annotations.GenericGenerator;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.persistence.*;

@Entity
public class Player {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private long id;

    @OneToMany(mappedBy="player", fetch=FetchType.EAGER)
    private Set<GamePlayer> gamePlayers = new HashSet<>();

    @OneToMany(mappedBy="player", fetch=FetchType.EAGER)
    private List<Score> scores;

    private String userName;

    private String password;

    //CONSTRUCTORES
    public Player() {}

    public Player(String userName, String password){
        this.setUserName(userName);
        this.setPassword(password);
    }

    //MÉTODOS
    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName){
        this.userName = userName;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Set<GamePlayer> getGamePlayers() {
        return gamePlayers;
    }

    public void setGamePlayers (Set<GamePlayer> gamePlayers) {
        this.gamePlayers = gamePlayers;
    }

    public List<Score> getScores() { return scores; }

    public void setScores(List<Score> scores) { this.scores = scores; }

    public double getWins(List<Score> scores){
        return this.scores.stream().filter(s -> s.getScore() == 1).count();
    }

    public double getLosses(List<Score> scores){
        return this.scores.stream().filter(s -> s.getScore() == 0).count();
    }

    public double getTies(List<Score> scores) {
        return this.scores.stream().filter(s -> s.getScore() == 0.5).count();
    }

    public double getTotalScore(List<Score> scores){
        return (getWins(this.scores) + (getTies(this.scores)*0.5));
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
