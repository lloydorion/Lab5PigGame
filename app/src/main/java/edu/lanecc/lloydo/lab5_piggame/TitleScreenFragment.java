package edu.lanecc.lloydo.lab5_piggame;

import android.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
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
import android.widget.Toast;

import androidx.annotation.Nullable;

import static android.content.Context.MODE_PRIVATE;

public class TitleScreenFragment extends Fragment {
    //TITLE SCREEN FRAGMENT:
    //chances and sets game settings
    //sends player username(s) to game screen
    //sets the status of the gameProgress bool (is or is not running)


    //CLASS FIELDS
    private EditText player1UsernameTextEntry;
    private EditText player2UsernameTextEntry;
    private Button newGameButton;
    private Button resumeGameButton;

    private String player1Name = "";
    private String player2Name = "";
    private boolean gameInProgress = false;

    //SAVE STATE KEYS
    private String PLAYER1_USERNAME_KEY = "PLAYER1_USERNAME_KEY";
    private String PLAYER2_USERNAME_KEY = "PLAYER2_USERNAME_KEY";
    private String IS_GAME_RUNNING = "IS_GAME_RUNNING";
    private String OLD_PREF_ENABLE_AI = "OLD_PREF_ENABLE_AI";
    private String OLD_PREF_NUMBER_OF_DIE = "OLD_PREF_NUMBER_OF_DIE";
    private String ENABLE_CUSTOM_SCORE = "ENABLE_CUSTOM_SCORE";
    private String CUSTOM_SCORE = "CUSTOM_SCORE";

    //PREFERENCES/SETTINGS VARIABLES
    private SharedPreferences prefs;
    private SharedPreferences savedValues;
    private boolean enableAI;
    private int numberOfDie;
    private boolean enableCustomScore;
    private int customScore;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        // References to view elements need to be assigned here because onCreateView is the only place where I have access to fragment's layout
        // inflate layout (also stores a reference to the fragment's view that can be accessed here)
        View view = inflater.inflate(R.layout.title_screen_fragment, container, false);

        //turns on menu rendering
        setHasOptionsMenu(true);

        //Getting References to UI elements
        this.player1UsernameTextEntry = view.findViewById(R.id.player1UsernameTextEntry);
        this.player2UsernameTextEntry = view.findViewById(R.id.player2UsernameTextEntry);
        this.newGameButton = view.findViewById(R.id.newGame_Button);
        this.resumeGameButton = view.findViewById(R.id.resumeGame_Button);

        //Getting references to shared preferences objects
        savedValues = this.getActivity().getSharedPreferences("savedValues", MODE_PRIVATE);
        prefs = PreferenceManager.getDefaultSharedPreferences(this.getActivity());

        //assigning UI elements to callbacks
        CreateUIEventListeners();
        return view;
    }

    @Override
    public void onPause() {
        super.onPause();
        // Scenarios in which this method is called:
        // user goes to android home screen
        // user goes to the settings menu
        // user goes to the game screen
        // user rotates screen

        SharedPreferences.Editor editor = savedValues.edit();
        editor.putBoolean(this.IS_GAME_RUNNING, this.gameInProgress);
        if(gameInProgress == true) {
            editor.putString(this.PLAYER1_USERNAME_KEY, player1Name);
            editor.putString(this.PLAYER2_USERNAME_KEY, player2Name);
        }
        editor.commit();
        //SAVING PREFERENCES (these are saved in onPause in the event the user changes the settings)
        SharedPreferences.Editor secondEditor = prefs.edit();
        secondEditor.putBoolean(OLD_PREF_ENABLE_AI,this.enableAI);
        secondEditor.putInt(OLD_PREF_NUMBER_OF_DIE,this.numberOfDie);
        secondEditor.putBoolean(ENABLE_CUSTOM_SCORE,this.enableCustomScore);
        secondEditor.putInt(CUSTOM_SCORE,this.customScore);
        secondEditor.commit();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Scenarios in which this method is called:
        // user return from android home screen
        // user returns from the settings menu
        // user returns from the game screen
        // session is restored after screen rotation

        // Getting game status (is or is not running)
        // note that this could be set to true in this class's newGame method and then set
        // to false in the actual game itself if a winner is declared
        this.gameInProgress = savedValues.getBoolean(IS_GAME_RUNNING,false);

        // GETTING OLD PREFERENCE SETTINGS
        boolean oldEnableAISetting = prefs.getBoolean(OLD_PREF_ENABLE_AI,false);
        int oldNumberOfDieSetting = prefs.getInt(OLD_PREF_NUMBER_OF_DIE,1);
        boolean oldEnableCustomScoreSetting = prefs.getBoolean(ENABLE_CUSTOM_SCORE,false);
        int oldCustomScoreSetting = prefs.getInt(CUSTOM_SCORE,100);

        // CHECKING TO SEE IF PREFERENCES HAVE CHANGED
        this.enableAI = prefs.getBoolean("pref_enable_AI",false);
        this.numberOfDie = Integer.parseInt(prefs.getString("pref_number_of_die","1"));
        this.enableCustomScore = prefs.getBoolean("pref_enable_custom_score",false);
        String customMaxScoreString = prefs.getString("pref_max_play_score","100");
        if(IsCustomScoreValid(customMaxScoreString)) {
            // executes if the string version of the user scoreInput is valid
            this.customScore = Integer.parseInt(customMaxScoreString);
        }
        else {
            // executes if the CustomScore is not valid
            this.customScore = 100;
        }

        // sets the gameInProgress variable to false and forces the user to create a new game
        if(oldEnableAISetting != this.enableAI) {
            gameInProgress = false;
        }
        if(oldNumberOfDieSetting != this.numberOfDie){
            gameInProgress = false;
        }
        if(oldEnableCustomScoreSetting != this.enableCustomScore) {
            gameInProgress = false;
        }
        if(oldCustomScoreSetting != this.customScore) {
            gameInProgress = false;
        }

        // SETTING TITLE FRAGMENT UI
        if(gameInProgress == true) {
            // set username fields and then disable them
            // disable the text entry fields
            // enable resume button
            // disable text entry fields for username(s)
            SetUsernameFields(player1Name,player2Name);
            DisableUsernameEntryFields();
            EnableResumeButton();
            DisableUsernameEntryFields();
        }
        else {
            // reset any leftover UI text
            // enable the text entry fields
            // disable resume button
            // enable text entry fields
            ResetUsernameFields();
            EnableUsernameEntryFields();
            DisableResumeButton();
            EnableUsernameEntryFields();
        }
        if(this.enableAI == true) {
            this.SetAndDisableAIUsernameField("Computer");
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        //mounting the XML to the main activity
        //will convert the XML items to java objects that can be displayed in the activity
        inflater.inflate(R.menu.settings_menu, menu);
        super.onCreateOptionsMenu(menu,inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            //determines which menu item was selected based off element IDs
            case R.id.about:
                Toast.makeText(getActivity(),"About", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.settings:
                Toast.makeText(getActivity(),"Settings", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(getActivity(),SettingsActivity.class));
                return true;
            default:
                //allows for default OptionsItemSelected behavior from the super class
                return super.onOptionsItemSelected((item));
        }
    }

    // EVENT LISTENERS
    private void CreateUIEventListeners() {
        this.newGameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NewGame();
            }
        });

        this.resumeGameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ResumeGame();
            }
        });

        this.player1UsernameTextEntry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(gameInProgress == false && player1UsernameTextEntry.getText().toString().equals("Enter username")) {
                    player1UsernameTextEntry.setText("");
                }
            }
        });
        this.player2UsernameTextEntry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(gameInProgress == false && player2UsernameTextEntry.getText().toString().equals("Enter username")) {
                    player2UsernameTextEntry.setText("");
                }
            }
        });
    }

    // EVENT CALLBACK METHODS
    public void NewGame() {
        //execute this block if the app was already running
        //delete existing username(s) in fields
        //set game bool to not active
        if(gameInProgress==true) {
            this.ResetUsernameFields(); //this is what prevents the second block in this handler from firing
            this.EnableUsernameEntryFields();
            if(this.enableAI == true) {
                SetAndDisableAIUsernameField("Computer");
            }
            this.gameInProgress = false;
            Toast.makeText(getActivity(),"Please enter valid usernames, press new game",Toast.LENGTH_SHORT).show();
        }
        //execute this block if the app was not running
        //get the text from both edit text fields
        //set game status to false
        if(AreUsernamesValid() == true) {
            String PLAYER_1_NAME = "PLAYER_1_NAME";
            String PLAYER_2_NAME = "PLAYER_2_NAME";
            String DIE_SIZE = "DIE_SIZE";
            String NUMBER_OF_DIE = "NUMBER_OF_DIE";
            String MAX_GAME_SCORE = "MAX_GAME_SCORE";
            String ON_NEW_GAME_CLICKED = "ON_NEW_GAME_CLICKED";

            this.DisableUsernameEntryFields();
            this.player1Name = this.player1UsernameTextEntry.getText().toString();
            this.player2Name = this.player2UsernameTextEntry.getText().toString();
            this.gameInProgress = true;
            // create intent (object that contains the activity to be launched)
            // send variables to intent's state that are required for the pigGame to start
            Intent intent = new Intent(getActivity(), GameScreenActivity.class);
            intent.putExtra(PLAYER_1_NAME, this.player1Name);
            intent.putExtra(PLAYER_2_NAME, this.player2Name);
            intent.putExtra(DIE_SIZE, 8);
            intent.putExtra(NUMBER_OF_DIE, this.numberOfDie);
            intent.putExtra(MAX_GAME_SCORE, this.customScore);
            intent.putExtra(ON_NEW_GAME_CLICKED, true);
            startActivity(intent);
            Toast.makeText(getActivity(),"New game started, good luck!",Toast.LENGTH_SHORT).show();
        }
    }

    public void ResumeGame() {
        // this callback method will simply launch the game screen
        // note that there is no passing of state or any variables
            /*
               the lack of state is based off the assumption that the resume button is accessible ONLY when the
               game has ALREADY had data passed into it
            */

        Intent intent = new Intent(getActivity(), GameScreenActivity.class);
        startActivity(intent);
    }

    // UI-RELATED METHODS
    private void SetUsernameFields(String player1Name, String player2Name) {
        // this is used when the UI needs to be synced from the inside to out (such as recovering state)
        this.player1UsernameTextEntry.setText(player1Name);
        this.player2UsernameTextEntry.setText(player2Name);
    }

    private void DisableResumeButton() {
        resumeGameButton.setEnabled(false);
    }

    private void EnableResumeButton() {
        resumeGameButton.setEnabled(true);
    }

    private boolean IsCustomScoreValid(String customScore) {
        Character[] acceptableValues = new Character[] {'0','1','2','3','4','5','6','7','8','9'};
        int numberOfValidCharacters = 0;
        for(int i = 0; i <customScore.length(); i++) {
            for(int j = 0; j <acceptableValues.length;j++) {
                if(customScore.charAt(i)==(acceptableValues[j])) {
                    numberOfValidCharacters = numberOfValidCharacters+1;
                }
            }
        }
        if(numberOfValidCharacters == customScore.length()) {
            return true; //returns true because ALL of the characters in the customScore are valid
        }
        else {
            return false; //returns false because at least one of the characters was not valid
        }
    }

    private void SetAndDisableAIUsernameField(String computerName) {
        //this sets the AI username in the UI and disables its text entry field
        this.player2UsernameTextEntry.setText(computerName);
        this.player2UsernameTextEntry.setEnabled(false);
    }

    private void EnableUsernameEntryFields() {
        //this.areEntryFieldsDisabled = false;
        this.player1UsernameTextEntry.setEnabled(true);
        this.player2UsernameTextEntry.setEnabled(true);
    }

    private void DisableUsernameEntryFields() {
        //this.areEntryFieldsDisabled = true;
        this.player1UsernameTextEntry.setEnabled(false);
        this.player2UsernameTextEntry.setEnabled(false);
    }

    private void ResetUsernameFields() {
        final String defaultPlaceHolderText = "Enter username";
        this.player1UsernameTextEntry.setText(defaultPlaceHolderText);
        this.player2UsernameTextEntry.setText(defaultPlaceHolderText);
    }

    private boolean AreUsernamesValid() {
        String player1Username = this.player1UsernameTextEntry.getText().toString();
        String player2Username = this.player2UsernameTextEntry.getText().toString();
        boolean isValid = true;
        //validation passes if both players have original names that are not the same
        if(player1Username.length() == 0 || player1Username.equals("Enter username")) {
            Toast.makeText(getActivity(),"Player 1 needs a valid username!",Toast.LENGTH_SHORT).show();
            isValid = false;
        }
        if(player2Username.length() == 0 || player2Username.equals("Enter username")) {
            Toast.makeText(getActivity(),"Player 2 needs a valid username!",Toast.LENGTH_SHORT).show();
            isValid = false;
        }
        if(player1Username.equals(player2Username)) {
            Toast.makeText(getActivity(),"Players cannot have the same username!",Toast.LENGTH_SHORT).show();
            isValid = false;
        }
        return isValid;
    }
}
