package com.example.vojjalateja.symphonia;

import android.os.Bundle;
import android.preference.PreferenceActivity;

/**
 * Created by VOJJALA TEJA on 08-12-2015.
 */
public class prefsactivity extends PreferenceActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.prefs);
    }
}
