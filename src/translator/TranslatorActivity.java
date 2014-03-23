//sg
package translator;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Spannable;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.BufferType;

import com.example.plotter.R;

@SuppressLint("InlinedApi")
public class TranslatorActivity extends Activity {

	private EditText input;
	private TextView output;
	String HOST;
	HashMap<String, HashSet<String>> synonymn;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		HOST = "10.129.26.111";
		input = (EditText) findViewById(R.id.textInput);
		output = (TextView) findViewById(R.id.textViewTranslated);
		Typeface Hindi = Typeface
				.createFromAsset(getAssets(), "DroidHindi.ttf");
		output.setTypeface(Hindi);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.settings, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_settings:
			Intent i = new Intent(this, prefs.TranslationPreference.class);
			startActivity(i);
			break;
		}
		return true;
	}

	/** Handler for the send button */
	public void translateButtonPressed(View v) {
		Log.d("EVENT", "SENDING EVENT");
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
		HOST = sharedPref.getString("pref_hostname", "NULL");
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			new TranslateTask()
					.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		} else {
			new TranslateTask().execute("Here");
		}

	}

	class TranslateTask extends AsyncTask<String, Void, String> {

		protected String doInBackground(String... v) {
			final int PORT = 1234;
			
			Log.d("SENDING", "SENDING REQUEST");
			
			String translatedSentence = "";

			try {
				Socket clientSocket = new Socket(HOST, PORT);
				DataOutputStream outToServer = new DataOutputStream(
						clientSocket.getOutputStream());
				BufferedReader inFromServer = new BufferedReader(
						new InputStreamReader(clientSocket.getInputStream()));

				// get the numbers fro m the users
				String sentence = input.getText().toString();
				Log.d("TEXT :", sentence);
				outToServer.writeBytes(sentence + '\n');
				translatedSentence = inFromServer.readLine();
				System.out.println(translatedSentence);
				ObjectInputStream objIn = new ObjectInputStream(
						clientSocket.getInputStream());
				synonymn = ((HashMap<String, HashSet<String>>) objIn
						.readObject());
				System.out.println("Here " + synonymn);
				clientSocket.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			return translatedSentence;
		}

		protected void onPostExecute(final String translatedSentence) {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					output.setText(translatedSentence, BufferType.SPANNABLE);
					output.setMovementMethod(LinkMovementMethod.getInstance());
					initClickableWords(translatedSentence);

				}
			});

		}

	}

	void initClickableWords(String str) {

		Spannable spans = (Spannable) output.getText();
		Integer[] indices = getIndices(output.getText().toString(), ' ');
		int start = 0;
		int end = 0;
		// to cater last/only word loop will run equal to the length of
		// indices.length
		for (int i = 0; i <= indices.length; i++) {
			ClickableSpan clickSpan = getClickableSpan();
			// to cater last/only word
			end = (i < indices.length ? indices[i] : spans.length());
			spans.setSpan(clickSpan, start, end,
					Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			start = end + 1;
		}

	}

	private ClickableSpan getClickableSpan() {
		return new ClickableSpan() {
			@Override
			public void onClick(View widget) {

				TextView tv = (TextView) widget;
				String s = tv
						.getText()
						.subSequence(tv.getSelectionStart(),
								tv.getSelectionEnd()).toString();
				try {
					HashSet<String> synset = synonymn.get(s);
					if (null != synset) {
						String syns[] = (String[]) synset
								.toArray(new String[0]);
						/* Set up the drop down list */
						tv.setText("Clicked");
					}
				} catch (NullPointerException npe) {
					npe.printStackTrace();
				}
			}

			public void updateDrawState(TextPaint ds) {
				super.updateDrawState(ds);
			}
		};
	}

	Integer[] getIndices(String s, char c) {
		int pos = s.indexOf(c, 0);
		List<Integer> indices = new ArrayList<Integer>();
		while (pos != -1) {
			indices.add(pos);
			pos = s.indexOf(c, pos + 1);
		}
		return (Integer[]) indices.toArray(new Integer[0]);
	}

}
