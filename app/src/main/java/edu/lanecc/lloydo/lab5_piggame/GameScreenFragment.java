package edu.lanecc.lloydo.lab5_piggame;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import android.app.Fragment;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;

import static android.content.Context.MODE_PRIVATE;

public class GameScreenFragment extends Fragment {
    // CLASS UI FIELDS
    private PigGame pigGame;
    private EditText player1UsernameTextEntry;
    private EditText player2UsernameTextEntry;
    private TextView player1ScoreLabel;
    private TextView player2ScoreLabel;
    private ImageView dieImage1;
    private ImageView dieImage2;
    private TextView currentPlayerLabel;
    private TextView pointsForRoundDescriptionLabel;
    private TextView pointsAccumulatorLabel;
    private Button rollDieButton;
    private Button endTurnButton;
    private Button newGameButton;

    // these string vars are used to store the desired usernames of players that will be passed into the PigGame object
    // these will not be used after the PigGame object is created
    private String player1Name;
    private String player2Name;
    // tracks if the game is running
    private boolean gameInProgress = false;
    // conditionally locks the roll die button if there is a winner
    private boolean isWinner =false;

    // Class-level sharedPreferences object and state variables
    private SharedPreferences savedValues;

    // SAVE STATE KEYS
    private String RUNNING_POINTS_TOTAL = "RUNNING_POINTS_TOTAL";
    private String PLAYER1_USERNAME_KEY = "PLAYER1_USERNAME_KEY";
    private String PLAYER2_USERNAME_KEY = "PLAYER2_USERNAME_KEY";
    private String PLAYER1_SCORE_KEY = "PLAYER1_SCORE_KEY";
    private String PLAYER2_SCORE_KEY = "PLAYER2_SCORE_KEY";
    private String CURRENT_PLAYER_KEY = "CURRENT_PLAYER_KEY";
    private String IS_GAME_RUNNING = "IS_GAME_RUNNING";
    private String DIE_IMAGE_NUMBER = "DIE_IMAGE_NUMBER";
    private String DIE_IMAGE_NUMBER_TWO = "DIE_IMAGE_NUMBER_TWO";

    private String OLD_PREF_ENABLE_AI = "OLD_PREF_ENABLE_AI";
    private String OLD_PREF_NUMBER_OF_DIE = "OLD_PREF_NUMBER_OF_DIE";
    private String ENABLE_CUSTOM_SCORE = "ENABLE_CUSTOM_SCORE";
    private String CUSTOM_SCORE = "CUSTOM_SCORE";


    //  SAVE STATE KEY SPECIFIC TO THIS CLASS
    private String NEW_GAME_BEEN_CREATED = "NEW_GAME_BEEN_CREATED";


    // PREFERENCES/SETTINGS VARIABLES
    private SharedPreferences prefs;
    private boolean enableAI;
    private int numberOfDie;
    private boolean enableCustomScore;
    private int customScore;
    private boolean hasNewGameBeenBuilt = false;

    // LIFECYCLES
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        // inflate layout
        View view = inflater.inflate(R.layout.game_screen_fragment, container, false);

        // turns on menu rendering
        setHasOptionsMenu(true);

        // GETTING UI ELEMENTS
        this.pointsForRoundDescriptionLabel = view.findViewById(R.id.pointsDescription_Label);
        this.player1UsernameTextEntry = view.findViewById(R.id.player1UsernameTextEntry);
        this.player1ScoreLabel = view.findViewById(R.id.player1Score_Label);
        this.player2ScoreLabel = view.findViewById(R.id.player2Score_Label);
        this.player2UsernameTextEntry = view.findViewById(R.id.player2UsernameTextEntry);
        this.currentPlayerLabel = view.findViewById(R.id.currentPlayer_Label);
        this.dieImage1 = view.findViewById(R.id.dieRollResult_Label);
        this.dieImage2 = view.findViewById(R.id.dieRollResult_Label2);
        this.pointsAccumulatorLabel = view.findViewById(R.id.runningPointsTotal_Label);
        this.rollDieButton = view.findViewById(R.id.rollDie_Button);
        this.endTurnButton = view.findViewById(R.id.endTurn_Button);
        this.newGameButton = view.findViewById(R.id.newGame_Button);


        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //  ETS REFERENCE TO SharedPrefs OBJECT
        savedValues = this.getActivity().getSharedPreferences("savedValues", MODE_PRIVATE);
        prefs = PreferenceManager.getDefaultSharedPreferences(this.getActivity());
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d("PigGame", "inisde onPause method");
        SharedPreferences.Editor editor = savedValues.edit();
        // fetching and storing data to be put into saveInstance
        if(pigGame != null) {
            editor.putBoolean(this.IS_GAME_RUNNING, this.gameInProgress);
            editor.putInt(this.PLAYER1_SCORE_KEY, pigGame.getPlayerScore(1));
            editor.putInt(this.PLAYER2_SCORE_KEY, pigGame.getPlayerScore(2));
            editor.putInt(this.CURRENT_PLAYER_KEY,  pigGame.getCurrentPlayerNumber());
            editor.putInt(this.RUNNING_POINTS_TOTAL, pigGame.getPointsForCurrentTurn());
            editor.putInt(this.DIE_IMAGE_NUMBER, pigGame.getLastRolledNumber());
            if(this.numberOfDie == 2) {
                editor.putInt(this.DIE_IMAGE_NUMBER_TWO, pigGame.getLastRolledNumber2());
            }
            editor.commit();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // checks if the game was running before the state change
        // pointless to update and recover state if it was never running in the first place
        // IMPORTANT: onResume only takes effect if saveState is null (otherwise there will be conflicts!!!
        // testing to see if the class-level variable stateHasbeenRecovered is true, meaning that oncreate already restored the state
        // boolean isGameRunning = savedValues.getBoolean(IS_GAME_RUNNING,false);
        if(hasNewGameBeenBuilt == false ) {
            BuildGame();
        }
        hasNewGameBeenBuilt = false;
        // Methods calls
        CreateUIEventListeners();
    }

    // Event Listener Assigner
    private void CreateUIEventListeners() {
        this.endTurnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EndTurn();
            }
        });
        this.rollDieButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Roll();
            }
        });
    }

    // COMPUTER AI
    public void AITurn(int numberOfPlayDie) {
        // WORKING ASSUMPTION: EXISTING PLAYER 2 LOGIC WILL BE RIGGED TO HOLD COMPUTER ROLL DATA
        // looping -->
        // roll pigGame die
        // set the die image
        // if rolled number is not 8
        // update running total label
        // if running total equals 20
        // stop rolling and end turn
        // else roll a total of 3 times or until an 8 is rolled
        // else
        // set canRoll to false
        // reset running total label AND pigGame internal runningTotal
        // end looping <--
        UpdateCurrentPlayer();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                int maxNumberOfRolls = 4;
                int maxComputerTurnPoints;
                if(numberOfDie == 2) {
                    maxComputerTurnPoints = 30;
                }
                else {
                    maxComputerTurnPoints = 20;
                }
                boolean rolledAnEight = false;
                int numberOfRolls = 0;

                while(rolledAnEight == false &&
                        numberOfRolls < maxNumberOfRolls &&
                        pigGame.getPointsForCurrentTurn() < maxComputerTurnPoints) {
                    if(numberOfDie == 2) {

                        int rollResult = pigGame.RollAndCalc();
                        int rollResult2 = pigGame.RollAndCalc();
                        pigGame.setLastRolledNumber(rollResult);
                        pigGame.setLastRolledNumber2(rollResult2);
                        UpdateDieImage(rollResult,1);
                        UpdateDieImage(rollResult2,2);
                        UpdatePointsRunningTotal(pigGame.getPointsForCurrentTurn());

                        if(rollResult == 8 || rollResult2 == 8) {
                            rolledAnEight = true;
                        }
                    }
                    else {
                        int rollResult = pigGame.RollAndCalc();
                        pigGame.setLastRolledNumber(rollResult);
                        UpdateDieImage(rollResult,1);
                        UpdatePointsRunningTotal(pigGame.getPointsForCurrentTurn());

                        if(rollResult == 8) {
                            rolledAnEight = true;
                        }
                    }
                    numberOfRolls++;
                }
                if(rolledAnEight==true) {
                    Toast.makeText(getActivity(),("Ouch, "+pigGame.getCurrentPlayerName()+" has rolled an 8!"),Toast.LENGTH_SHORT);
                }
                UpdatePlayerScore(pigGame.getCurrentPlayerNumber(),0);
                AIEndTurn();
            }
        }, 500);

    }

    public void AIEndTurn() {
        // THIS CODE IS MEANT TO PRIME THE UI FOR A HUMAN USER
        // end turn auto switches the player turn if there is no winner, so all we have to do here reflect the change in the UI
        // re-enable the endturn button
        // re-enable the roll button
        int playerThatJustWent = pigGame.getCurrentPlayerNumber();
        int winnerNumber = pigGame.EndTurn();

        this.UpdatePlayerScore(playerThatJustWent,pigGame.getPlayerScore(playerThatJustWent));
        this.ResetRunningTotalLabel();

        if(winnerNumber != 0) {
            // display winner on UI
            // update winner description label
            this.DisplayWinner(winnerNumber);
            this.SetWinnerDescription();
            // update turn label to reflect end of game
            this.UpdateCurrentPlayer("End of Game!");
            // disable game buttons
            this.DisableEndTurnButton();
            this.DisableRollButton();
            // display toast message
            Toast.makeText(getActivity(), (pigGame.getPlayerName(winnerNumber) + " has won!"),Toast.LENGTH_SHORT);
            // turn isWinner to true, preventing the roll die button from activating
            this.isWinner = true;
            this.gameInProgress = false;
        }
        else {
            UpdateCurrentPlayer();
            EnableEndTurnButton();
            EnableRollButton();
        }
    }

    // EVENT HANDLERS
    public void Roll() {
        // THIS METHOD ADDS TO PLAYER RUNNING TOTAL AND CALCULATES SCORE -------------------------------------
        // lock roll button
        // roll pigGame die
        // if rolled number is not 8
        // set the die image
        // update running total label
        // else reset currentPlayerUI elements
        // execute endturn methods

        this.DisableRollButton();
        final int DIE_ONE_UI_POSITION = 1;
        final int DIE_TWO_UI_POSITION = 2;

        if(this.numberOfDie == 2) {
            // this block rolls once and checks if the result was not an 8
            // roll again is the result wasn't 8
            // block is written to roll one at a time because the pigGame auto switches the player turn if an 8 is rolled
            // so rolling twice and getting an 8 on the first roll means that the second roll's points would get added to the player that DIDN'T roll
            int rollResult1;
            int rollResult2;
            rollResult1 = pigGame.RollAndCalc();
            if(rollResult1 != 8 ) {
                rollResult2 = pigGame.RollAndCalc();
            }
            else {
                rollResult2 = 8;
            }

            pigGame.setLastRolledNumber(rollResult1);
            pigGame.setLastRolledNumber2(rollResult2);
            this.UpdateDieImage(rollResult1,DIE_ONE_UI_POSITION);
            this.UpdateDieImage(rollResult2,DIE_TWO_UI_POSITION);

            if(rollResult2 != 8) {
                this.UpdatePointsRunningTotal(pigGame.getPointsForCurrentTurn());
            }
            else {
                Toast.makeText(getActivity(),("Ouch, "+pigGame.getCurrentPlayerName()+" has rolled an 8!"),Toast.LENGTH_SHORT);
                this.UpdatePlayerScore(pigGame.getCurrentPlayerNumber(),0);
                this.ResetRunningTotalLabel();
                this.EndTurn();
            }
        }
        else {
            int rollResult = pigGame.RollAndCalc();
            pigGame.setLastRolledNumber(rollResult);
            this.UpdateDieImage(rollResult,DIE_ONE_UI_POSITION);
            if(rollResult != 8) {
                this.UpdatePointsRunningTotal(pigGame.getPointsForCurrentTurn());
            }
            else {
                Toast.makeText(getActivity(),("Ouch, "+pigGame.getCurrentPlayerName()+" has rolled an 8!"),Toast.LENGTH_SHORT);
                this.UpdatePlayerScore(pigGame.getCurrentPlayerNumber(),0);
                this.ResetRunningTotalLabel();
                this.EndTurn();
            }
        }
        try {
            // this process is done to slow down the user and prevent spamming
            Thread.sleep(200);
        }
        catch (InterruptedException e) {
            Log.d("PigGame","sleep function for roll button was interrupted");
        }
        if(this.isWinner == false) {
            this.EnableRollButton();
        }
    }

    public void EndTurn() {
        // THIS METHOD ADDS TO SCORE AND DETERMINES IF THERE IS A WINNER -------------------------------------
        // lock roll button
        // move running total to total points label
        // calc winner
        // if current player is winner, display name in turn label
        // give toast message saying click newgame to start again
        // else switch current player to next player
        // unlock roll button
        this.DisableRollButton();
        this.DisableEndTurnButton();

        int playerThatJustWent = pigGame.getCurrentPlayerNumber();
        int winnerNumber = pigGame.EndTurn();

        this.UpdatePlayerScore(playerThatJustWent,pigGame.getPlayerScore(playerThatJustWent));
        this.ResetRunningTotalLabel();

        if(winnerNumber != 0) {
            // display winner on UI
            // update winner description label
            this.DisplayWinner(winnerNumber);
            this.SetWinnerDescription();
            // update turn label to reflect end of game
            this.UpdateCurrentPlayer("End of Game!");
            // disable game buttons
            this.DisableEndTurnButton();
            this.DisableRollButton();
            // display toast message
            Toast.makeText(getActivity(), (pigGame.getPlayerName(winnerNumber) + " has won!"),Toast.LENGTH_SHORT);
            // turn isWinner to true, preventing the roll die button from activating
            this.isWinner = true;
            this.gameInProgress = false;
        }
        else {
            if(this.enableAI == true && playerThatJustWent == 1) {
                // show that computer is taking its turn
                // execute AI logic method
                AITurn(numberOfDie);
            }
            else {
                UpdateCurrentPlayer();
                EnableEndTurnButton();
                EnableRollButton();
            }
        }
    }

    public void BuildGame() {
        //  THIS METHOD SHOULD BE CALLED IF A GAME IS ALREADY SET (AKA NEEDS TO BE RESUMED)
        //  Scenarios in which this method would be called:
        //  user returns from android home screen
        //  user returns from the title screen
        //  user rotates screen

        //  GETTING AND SETTING CLASS PREFERENCE SETTINGS
        this.enableAI = prefs.getBoolean(OLD_PREF_ENABLE_AI,false);
        this.numberOfDie = prefs.getInt(OLD_PREF_NUMBER_OF_DIE,1);
        this.enableCustomScore = prefs.getBoolean(ENABLE_CUSTOM_SCORE,false);
        this.customScore = prefs.getInt(CUSTOM_SCORE,100);

        //  Getting variables from sharedPrefs
        boolean isGameRunning = savedValues.getBoolean(IS_GAME_RUNNING,false);
        String player1Username = savedValues.getString(PLAYER1_USERNAME_KEY, "");
        String player2Username = savedValues.getString(PLAYER2_USERNAME_KEY, "");
        int player1Score = savedValues.getInt(PLAYER1_SCORE_KEY,0);
        int player2Score = savedValues.getInt(PLAYER2_SCORE_KEY,0);
        int currentPlayerTurn = savedValues.getInt(CURRENT_PLAYER_KEY,0);
        int runningPointsTotal = savedValues.getInt(RUNNING_POINTS_TOTAL,0);
        int lastRolledNumber = savedValues.getInt(DIE_IMAGE_NUMBER,8);
        int lastRolledNumber2 = 8;
        if(this.numberOfDie==2) {
            lastRolledNumber2 = savedValues.getInt(DIE_IMAGE_NUMBER_TWO,8);
        }

        // rebuild game objects and settings
        pigGame = new PigGame(player1Username,player2Username,8,this.numberOfDie,(this.enableCustomScore == true? this.customScore : 100));
        this.gameInProgress = isGameRunning;
        pigGame.setPlayerScore(1,player1Score);
        pigGame.setPlayerScore(2,player2Score);
        pigGame.setCurrentPlayerTurn(currentPlayerTurn);
        pigGame.setPointsForCurrentTurn(runningPointsTotal);
        pigGame.setLastRolledNumber(lastRolledNumber);
        if(this.numberOfDie == 2) {
            pigGame.setLastRolledNumber2(lastRolledNumber2);
        }
        if(this.enableAI == true) {
            // ensures that the AI is displayed and the user cannot change it
            this.SetUsernameFields(this.pigGame.getPlayerName(1),"Computer");
        }

        // UI preparation and restoration
        this.DisableUsernameEntryFields();
        this.ResetWinnerDescription();
        this.SetUsernameFields(player1Username,player2Username);
        this.UpdatePlayerScore(1,pigGame.getPlayerScore(1));
        this.UpdatePlayerScore(2,pigGame.getPlayerScore(2));
        this.UpdatePointsRunningTotal(pigGame.getPointsForCurrentTurn());
        this.UpdateCurrentPlayer();
        this.UpdateDieImage(lastRolledNumber,1);
        if(this.numberOfDie==2) {
            this.dieImage2.setVisibility(View.VISIBLE);
            this.UpdateDieImage(lastRolledNumber2,2);
        }
        else {
            this.dieImage2.setVisibility(View.GONE);
        }
    }

    public void BuildGame(PigGame gameObject) {
        //  THIS METHOD IS CALLED WHEN THE NEW GAME BUTTON IS CLICKED
        //  SETS A VARIABLE CALLED newGameHasBeenBuilt
        //  Scenarios in which this method would be called:
        //  user clicks new game button in title screen
        //  the point of this method is to handle to creation or rebuilding of the game object, removing excess code from the lifecycle methods

        //  GETTING AND SETTING CLASS PREFERENCE SETTINGS
        this.enableAI = prefs.getBoolean(OLD_PREF_ENABLE_AI,false);
        this.numberOfDie = prefs.getInt(OLD_PREF_NUMBER_OF_DIE,1);
        this.enableCustomScore = prefs.getBoolean(ENABLE_CUSTOM_SCORE,false);
        this.customScore = prefs.getInt(CUSTOM_SCORE,100);

        this.pigGame = gameObject;
        this.DisableRollButton();
        this.DisableEndTurnButton();
        this.DisableUsernameEntryFields();
        this.ResetRunningTotalLabel();
        this.ResetScoreLabels();
        this.ResetWinnerDescription();
        this.player1Name = savedValues.getString(PLAYER1_USERNAME_KEY, "");
        this.player2Name = savedValues.getString(PLAYER2_USERNAME_KEY, "");
        this.gameInProgress = savedValues.getBoolean(IS_GAME_RUNNING, true);
        this.SetUsernameFields(player1Name,player2Name);
        this.UpdateCurrentPlayer();
        // manipulating UI based off preferences set in the main page
        if(this.enableAI == true) {
            // ensures that the AI is displayed and the user cannot change it
            this.SetUsernameFields(this.pigGame.getPlayerName(1),"Computer");
        }
        if(this.numberOfDie==2) {
            this.dieImage2.setVisibility(View.VISIBLE);
            this.UpdateDieImage(8,2);
        }
        else {
            this.dieImage2.setVisibility(View.GONE);
        }

        //this value is set to prevent the pigGame object and game screen UI from being set twice
        this.hasNewGameBeenBuilt = true;

        this.EnableRollButton();
        this.EnableEndTurnButton();
    }

    // METHODS
    private void DisplayWinner(int winnerNumber) {
        // set the display points label to the name of the winner
        this.pointsAccumulatorLabel.setText(pigGame.getPlayerName(winnerNumber));
    }

    private void SetWinnerDescription() {
        this.pointsForRoundDescriptionLabel.setText("Winner for this round is:");
    }

    private void ResetWinnerDescription() {
        this.pointsForRoundDescriptionLabel.setText("Points for this round:");
    }

    private void EnableEndTurnButton() {
        this.endTurnButton.setEnabled(true);
    }

    private void DisableEndTurnButton() {
        this.endTurnButton.setEnabled(false);
    }

    private void EnableRollButton() {
        this.rollDieButton.setEnabled(true);
    }

    private void DisableRollButton() {
        this.rollDieButton.setEnabled(false);
    }

    private void DisableUsernameEntryFields() {
        this.player1UsernameTextEntry.setEnabled(false);
        this.player2UsernameTextEntry.setEnabled(false);
    }

    private void UpdateDieImage(int rolledNumber,int diePositionInUI) {
        int referenceID = 0;
        switch(rolledNumber) {
            case 1: {
                referenceID = R.drawable.die8side1;
                break;
            }
            case 2: {
                referenceID = R.drawable.die8side2;
                break;
            }
            case 3: {
                referenceID = R.drawable.die8side3;
                break;
            }
            case 4: {
                referenceID = R.drawable.die8side4;
                break;
            }
            case 5: {
                referenceID = R.drawable.die8side5;
                break;
            }
            case 6: {
                referenceID = R.drawable.die8side6;
                break;
            }
            case 7: {
                referenceID = R.drawable.die8side7;
                break;
            }
            case 8: {
                referenceID = R.drawable.die8side8;
                break;
            }
            default: {
                referenceID = R.drawable.die8side8;
                break;
            }
        }
        Drawable drawableImage = getResources().getDrawable(referenceID);
        if(diePositionInUI == 1) {
            this.dieImage1.setImageDrawable(drawableImage);
        }
        else {
            this.dieImage2.setImageDrawable(drawableImage);
        }

    }

    private void UpdatePlayerScore(int playerNumber, int score) {
        if(playerNumber==1) {
            this.player1ScoreLabel.setText(String.valueOf(score + " total points"));
        }
        else {
            this.player2ScoreLabel.setText(String.valueOf(score + " total points"));
        }
    }

    private void ResetScoreLabels() {
        this.player1ScoreLabel.setText("0 total points");
        this.player2ScoreLabel.setText("0 total points");
    }

    private void UpdatePointsRunningTotal(int points) {
        this.pointsAccumulatorLabel.setText(String.valueOf(points));
    }

    private void ResetRunningTotalLabel() {
        this.pointsAccumulatorLabel.setText("0 Points");
    }

    private void ResetUsernameFields() {
        final String defaultPlaceHolderText = "Enter username";
        this.player1UsernameTextEntry.setText(defaultPlaceHolderText);
        this.player2UsernameTextEntry.setText(defaultPlaceHolderText);
    }

    private void SetUsernameFields(String player1Name, String player2Name) {
        // this is used when the UI needs to be synced from the inside to out (such as recovering state)
        this.player1UsernameTextEntry.setText(player1Name);
        this.player2UsernameTextEntry.setText(player2Name);
    }

    private void UpdateCurrentPlayer() {
        // this is the default updateCurrentPlayer method and will automatically set the UI to reflect the currentPlayerName
        this.currentPlayerLabel.setText(pigGame.getCurrentPlayerName()+"'s turn");
    }

    private void UpdateCurrentPlayer(String optionalMessage) {
        // this is for optional and specific messages that need to be displayed in the UpdateCurrentPlayer UI label
        this.currentPlayerLabel.setText(optionalMessage);
    }

    private void ResetCurrrentPlayerLabel() {
        this.currentPlayerLabel.setText("Nobody's turn");
    }
}
