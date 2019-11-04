package edu.lanecc.lloydo.lab5_piggame;

public class Player {
    //CLASS FIELDS
    private int points;
    private String name;

    //CONSTRUCTOR
    public Player(String name) {
        this.name = name;
        this.points = 0;
    }

    //PROPERTIES
    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPoints() {
        return this.points;
    }

    //METHODS
    public void addPoints(int points) {
        this.points += points;
    }

    public void wipePoints() {
        this.points = 0;
    }
}