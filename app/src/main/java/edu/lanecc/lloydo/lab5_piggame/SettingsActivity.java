package edu.lanecc.lloydo.lab5_piggame;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.app.Activity;
import android.preference.PreferenceManager;

public class SettingsActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //show fragment class as main content
        //MUST DECLARE ACTIVITY IN MANIFEST

        /*
        adds fragment to activity by
            1. opening up a transaction between the fragment and activity
            2. replacing the content in the activity with the fragment object
            3. committing those changes to the new activity
         */
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content,new SettingsActivityFragment())
                .commit();
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false); //this only runs when the app is first started on a device
    }
}
