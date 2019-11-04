package edu.lanecc.lloydo.lab5_piggame;
/*
Die Properties:
Number of sides
Die Methods:
Roll
 */
public class Die {

    private int numberOfSides;

    public Die(int sides) {
        this.numberOfSides = sides;
    }


    public int roll() {
        int outcome = numberGenerator(1,this.numberOfSides);
        return outcome;
    }

    private int numberGenerator(int min, int max) {
        int outcome = (int)(Math.random()*((max-min)+1)+min);
        return outcome;
    }
}
