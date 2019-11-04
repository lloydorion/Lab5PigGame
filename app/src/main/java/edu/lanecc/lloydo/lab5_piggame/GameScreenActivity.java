package edu.lanecc.lloydo.lab5_piggame;

import android.app.Activity;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class GameScreenActivity extends AppCompatActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.game_screen);

        // Get reference to second fragment
        GameScreenFragment gameFragment = (GameScreenFragment)getFragmentManager()
                .findFragmentById(R.id.game_fragment);

        // Getting intent values that were passed from the first activity
        boolean hasNewGameBeenClicked = getIntent().getExtras().getBoolean("ON_NEW_GAME_CLICKED");
        PigGame gameObject;
        if(hasNewGameBeenClicked == true) {
            // if this block executes, the new game button was clicked
            String player1Username = getIntent().getExtras().getString("PLAYER_1_NAME");
            String player2Username = getIntent().getExtras().getString("PLAYER_2_NAME");
            int dieSize = getIntent().getExtras().getInt("DIE_SIZE");
            int numberOfDie = getIntent().getExtras().getInt("NUMBER_OF_DIE");
            int maxScore = getIntent().getExtras().getInt("MAX_GAME_SCORE");

            // Building game object and then passing it to second fragment
            gameObject = new PigGame(player1Username,player2Username,dieSize,numberOfDie,maxScore);
            gameFragment.BuildGame(gameObject);
        }
    }
}