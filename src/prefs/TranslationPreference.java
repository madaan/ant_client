package prefs;
//sg



import android.os.Bundle;
import android.preference.PreferenceActivity;

import com.example.plotter.R;


public class TranslationPreference extends PreferenceActivity {
	  @SuppressWarnings("deprecation")
	@Override
	    public void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        addPreferencesFromResource(R.xml.preferences);
	 
	    }

}
