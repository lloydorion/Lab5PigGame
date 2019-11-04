package edu.lanecc.lloydo.lab5_piggame;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

public class SettingsActivityFragment extends PreferenceFragment implements OnSharedPreferenceChangeListener {

    //CLASS LEVEL FIELDS
    private SharedPreferences prefs;
    private boolean enableCustomScore;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //loads preferences from the XML files
        addPreferencesFromResource(R.xml.preferences);
        prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        enableCustomScore = prefs.getBoolean("pref_enable_custom_score",false);
    }

    @Override
    public void onResume() {
        //calls super
        super.onResume();
        //TODO
        prefs.registerOnSharedPreferenceChangeListener(this);
        setPointsThresholdUIBox(enableCustomScore);
    }

    @Override
    public void onPause() {
        prefs.unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }


    @Override
    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
        //TODO
        //if user clicks enable custom score pref box
        //enable the custom score label
        //else
        //disable the custom score label
        if(key.equals("pref_enable_custom_score")) {
            this.enableCustomScore = prefs.getBoolean("pref_enable_custom_score",false);
            setPointsThresholdUIBox(enableCustomScore);
        }
    }


    private void setPointsThresholdUIBox(boolean customScoreEnabled) {
        Preference customScore = findPreference("pref_max_play_score");
        if(customScoreEnabled == true) {
            customScore.setEnabled(true);
        }
        else {
            customScore.setEnabled(false);
        }
    }
}
