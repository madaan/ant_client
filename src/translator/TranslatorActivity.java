//sg
package translator;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
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
import android.widget.Toast;

import com.example.plotter.R;

@SuppressLint("InlinedApi")
public class TranslatorActivity extends Activity {

	private EditText input;
	private TextView output, align;
	public static final int MOSES_TYPE = 1;
	public static final int DICT_TYPE = 0;
	int translationType;
	String HOST;
	ArrayList<String> alignedPair;
	HashMap<String, String[]> synonymn;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		HOST = "10.129.26.111";
		input = (EditText) findViewById(R.id.textInput);
		align = (TextView)findViewById(R.id.textViewAlign);
		input.setText("Hello how are you");
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
		SharedPreferences sharedPref = PreferenceManager
				.getDefaultSharedPreferences(this);
		HOST = sharedPref.getString("pref_hostname", "NULL");
		translationType = Integer.parseInt(sharedPref.getString(
				"pref_translation_type", "0"));

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			new TranslateTask()
					.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		} else {
			new TranslateTask().execute("Here");
		}

	}

	class TranslateTask extends AsyncTask<String, String, String> {

		 @Override
		    protected void onPreExecute() {
		        super.onPreExecute();
		        showDialog(DIALOG_DOWNLOAD_PROGRESS);
		    }
		 
		protected String doInBackground(String... v) {
			final int PORT = 1234;

			Log.d("SENDING", "SENDING REQUEST");
			 publishProgress("" + 10);
			String translatedSentence = "";

			try {
				Socket clientSocket = new Socket(HOST, PORT);
				DataOutputStream outToServer = new DataOutputStream(
						clientSocket.getOutputStream());
				BufferedReader inFromServer = new BufferedReader(
						new InputStreamReader(clientSocket.getInputStream()));

				// get the numbers from the users
				String sentence = input.getText().toString();
				Log.d("TEXT :", sentence);
				publishProgress("" + 20);
				if (translationType == TranslatorActivity.MOSES_TYPE) {
					Log.d("XLATOR CHOSEN", "Moses");
					outToServer.writeBytes("MOSES\n");
					outToServer.writeBytes(sentence + '\n');
					translatedSentence = inFromServer.readLine();
					publishProgress("" + 50);
					StringBuilder sbr = new StringBuilder();
					String inputWords[] = sentence.split(" ");
					HashMap<Integer, String> ipAlignMap = new HashMap<Integer, String>();
					HashMap<Integer, String> translateAlignMap = new HashMap<Integer, String>();
					alignedPair = new ArrayList<String>();
					StringBuilder selectionBuilder = new StringBuilder();
					StringBuilder justTranslation = new StringBuilder();
					for (int i = 0; i < translatedSentence.length(); i++) {
						if (translatedSentence.charAt(i) == '|') {
							// extract the starting point
							StringBuilder startNumber = new StringBuilder();
							int j = 0;
							for (j = i + 1; translatedSentence.charAt(j) != '-'; j++) {
								startNumber
										.append(translatedSentence.charAt(j));
							}
							j++; // skip the -
							StringBuilder endNumber = new StringBuilder();
							for (; translatedSentence.charAt(j) != '|'; j++) {
								endNumber.append(translatedSentence.charAt(j));
							}
							int start = Integer
									.parseInt(startNumber.toString());
							int end = Integer.parseInt(endNumber.toString());
							String inputPortion[] = Arrays.copyOfRange(
									inputWords, start, end + 1);
							String translationPortion = selectionBuilder
									.toString();
							selectionBuilder = new StringBuilder();
							alignedPair.add(Arrays.toString(inputPortion) + "="
									+ translationPortion);
							i = j;
							Log.d("START-END", "S : " + start + " E : " + end);
							Log.d("Original : ", Arrays.toString(inputPortion));
							Log.d("Translation : ", translationPortion);
						} else {
							selectionBuilder.append(translatedSentence
									.charAt(i));
							justTranslation
									.append(translatedSentence.charAt(i));
						}
					}
					publishProgress("" + 70);
					Log.d("XLATION : ", translatedSentence);
					//translatedSentence = justTranslation.toString();

				} // end MOSES
				else if (translationType == TranslatorActivity.DICT_TYPE) {
					outToServer.writeBytes("DICT\n");
					outToServer.writeBytes(sentence + '\n');
					translatedSentence = inFromServer.readLine();
					publishProgress("" + 50);
					// now read the synonyms for each word
					// Format -> word : syn1, syn2...
					System.out.println(translatedSentence);
					synonymn = new HashMap<String, String[]>();
					String synLine = "";
					while ((synLine = inFromServer.readLine()) != null) {
						String l1[] = synLine.replaceAll("\n", "").split(":");
						String word = l1[0].trim();
						String syns[] = l1[1].split(",");
						synonymn.put(word, syns);
					}
					Log.d("Synset : ", synonymn.toString());
					publishProgress("" + 70);
				}

				clientSocket.close();
				publishProgress("" + 100);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return translatedSentence;
		}
		
		protected void onProgressUpdate(String... progress) {        
		    mProgressDialog.setProgress(Integer.parseInt(progress[0]));
		}
		
		protected void onPostExecute(final String translatedSentence) {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					if (translationType == TranslatorActivity.DICT_TYPE) {
						output.setText(translatedSentence, BufferType.SPANNABLE);
						output.setMovementMethod(LinkMovementMethod
								.getInstance());
						initClickableWords(translatedSentence);
						align.setText("");
					} else if (translationType == TranslatorActivity.MOSES_TYPE) {
							
							output.setText(translatedSentence, BufferType.SPANNABLE);
							output.setMovementMethod(LinkMovementMethod
									.getInstance());
							//align.setText(alignedPair.toString());
					}

				}
			});
			 dismissDialog(DIALOG_DOWNLOAD_PROGRESS);

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
				Log.d("SHOWING SYNS FOR", s);
				try {
					Log.d("MAP :", synonymn.toString());
					String synset[] = synonymn.get(s);
					// Log.d("Val :", synset.toString());

					if (null != synset) {
						StringBuilder synStr = new StringBuilder();
						for (String syn : synset) {
							synStr.append(syn);
							synStr.append("\n");
						}
						Toast.makeText(getApplicationContext(),
								synStr.toString(), Toast.LENGTH_SHORT).show();
						/* Set up the drop down list */
						// tv.setText("Clicked");
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
	public static final int DIALOG_DOWNLOAD_PROGRESS = 0;
	private ProgressDialog mProgressDialog;

	@Override
	protected Dialog onCreateDialog(int id) {
	    switch (id) {
	    case DIALOG_DOWNLOAD_PROGRESS:
	        mProgressDialog = new ProgressDialog(this);
	        mProgressDialog.setMessage("Getting your translation");
	        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
	        mProgressDialog.setCancelable(false);
	        mProgressDialog.show();
	        return mProgressDialog;
	    default:
	    return null;
	    }
	}

}
