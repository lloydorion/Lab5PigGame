package edu.lanecc.lloydo.lab5_piggame;


import edu.lanecc.lloydo.lab5_piggame.Die;
import edu.lanecc.lloydo.lab5_piggame.Player;

public class PigGame {
    //CLASS FIELDS
    private Player player1;
    private Player player2;
    private Die eightSidedDie;
    private int dieNumber;
    private int currentPlayerTurn;
    private int runningTotal;
    private int lastRolledNumber;
    private int lastRolledNumber2;
    private int maxGameScore;
    private boolean playerOneReachedMaxScore;

    //CONSTRUCTOR
    public PigGame(String playerOneName, String playerTwoName, int dieSize, int numberOfDie, int maxGameScore) {
        this.player1 = new Player(playerOneName);
        this.player2 = new Player(playerTwoName);
        this.eightSidedDie = new Die(dieSize);
        this.currentPlayerTurn = 1;
        this.dieNumber = numberOfDie;
        this.maxGameScore = maxGameScore;
    }

    //PROPERTIES
    public int getNumberOfDie() {
        return this.dieNumber;
    }

    public String getPlayerName(int playerNumber) {
        if(playerNumber==1) {
            return player1.getName();
        }
        else {
            return player2.getName();
        }
    }

    public String getCurrentPlayerName() {
        //this method takes the class's currentPlayerTurn variable and gets the current player's name as a string
        if(currentPlayerTurn == 1) {
            return this.player1.getName();
        }
        else {
            return this.player2.getName();
        }
    }


    public int getCurrentPlayerNumber() {
        //this method returns the number of the player that is currently rolling
        return this.currentPlayerTurn;
    }

    public void setCurrentPlayerTurn(int playerNumber) {
        this.currentPlayerTurn = playerNumber;
    }


    public int getPlayerScore(int playerNumber) {
        int points;
        if(playerNumber==1) {
            points = player1.getPoints();
        }
        else {
            points = player2.getPoints();
        }
        return points;
    }

    public void setPlayerScore(int playerNumber, int points) {
        //this method will be used to restore player point numbers after some lifecycle change has occured
        if(playerNumber==1) {
            this.player1.addPoints(points);
        }
        else {
            this.player2.addPoints(points);
        }
    }


    public int getPointsForCurrentTurn() {
        return runningTotal;
    }

    public void setPointsForCurrentTurn(int points) {
        this.runningTotal = points;
    }


    public int getLastRolledNumber() {
        return this.lastRolledNumber;
    }

    public void setLastRolledNumber(int rolledNumber) {
        this.lastRolledNumber = rolledNumber;
    }

    public int getLastRolledNumber2() {
        return this.lastRolledNumber2;
    }

    public void setLastRolledNumber2(int rolledNumber) {
        this.lastRolledNumber2 = rolledNumber;
    }


    public int RollAndCalc() {

        int rolledNumber = this.rollDie();
        if(rolledNumber != 8) {
            addToRunningTotal(rolledNumber);
            return rolledNumber;
        }
        else {
            this.resetRunningTotal();
            resetPlayerScore(currentPlayerTurn);
            return rolledNumber;
        }
    }

    public int EndTurn() {

        this.addToPlayerScore(currentPlayerTurn,runningTotal);
        int winner = this.CalcWinner();
        if(winner != 0) {
            //if the return int is not 0, then there is a winner
            return winner;
        }
        else {
            //otherwise reset the internal game mechanisms for next player
            this.resetRunningTotal();
            this.setNextPlayerTurn();
        }
        //returns zero if no winner
        return 0;
    }

    public void RestartGame(String player1Name, String player2Name) {
        //reset player names
        //reset player scores
        //reset internal running score
        //set player turn back to default
        //reset points limit first tracker
        this.player1.setName(player1Name);
        this.player2.setName(player2Name);
        this.resetPlayerScore(1);
        this.resetPlayerScore(2);
        this.resetPlayerTurn();
        this.resetRunningTotal();
        this.playerOneReachedMaxScore = false;
    }

    public int CalcWinner() {

        final int player1Points = this.player1.getPoints();
        final int player2Point = this.player2.getPoints();

        final int player1 = 1;
        final int player2 = 2;

        if (player2Point >= maxGameScore) {
            return player2;
        }
        else if(player1Points >= maxGameScore) {
            if(this.playerOneReachedMaxScore == true && player1Points > player2Point) {
                return player1;
            }
            this.playerOneReachedMaxScore = true;
        }
        //this returns if there are no winners
        return 0;
    }

    //PRIVATE CLASS METHODS
    private int rollDie() {
        return this.eightSidedDie.roll();
    }

    private void setNextPlayerTurn() {
        if(this.currentPlayerTurn == 1)
            this.currentPlayerTurn = 2;
        else
            this.currentPlayerTurn = 1;
    }

    private void resetPlayerScore(int playerNumber) {
        if(playerNumber==1) {
            this.player1.wipePoints();
        }
        else {
            this.player2.wipePoints();
        }
    }

    private void resetRunningTotal() {
        this.runningTotal = 0;
    }

    private void addToRunningTotal(int points) {
        this.runningTotal += points;
    }

    private void resetPlayerTurn() {
        this.currentPlayerTurn = 1;
    }

    private void addToPlayerScore(int playerNumber, int points) {
        if(playerNumber==1) {
            this.player1.addPoints(points);
        }
        else {
            this.player2.addPoints(points);
        }
    }
}

