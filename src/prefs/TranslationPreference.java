package prefs;
//sg



import android.os.Bundle;
import android.preference.PreferenceActivity;


public class TranslationPreference extends PreferenceActivity {
	  @Override
	    public void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        addPreferencesFromResource(R.xml.preferences);
	 
	    }

}
