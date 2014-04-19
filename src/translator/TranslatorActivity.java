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
import java.util.Locale;

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
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.text.Html;
import android.text.Spannable;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.BufferType;
import android.widget.Toast;
import caching.CachedTranslation;
import caching.ShowCachedActivity;
import caching.TranslationCachingHandler;

import com.example.plotter.R;

@SuppressLint({ "InlinedApi", "NewApi" })
public class TranslatorActivity extends Activity implements OnInitListener {

	private TextToSpeech tts;
	private EditText input;
	TranslationCachingHandler tch;
	String currEnglish;
	String currHindi;
	String currNormed;
	private TextView output, align;
	public static final int MOSES_TYPE = 1;
	public static final int DICT_TYPE = 0;
	int translationType;
	String HOST;
	Typeface hindiTypeface;
	ArrayList<String> alignedPair;
	HashMap<String, String[]> synonymn;
	Button translateButton;
	Button speakButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		tts = new TextToSpeech(getApplicationContext(), this);
		tch = new TranslationCachingHandler(getApplicationContext());
		HOST = "10.0.2.2";
		input = (EditText) findViewById(R.id.textInput);
		align = (TextView) findViewById(R.id.textViewAlign);
		translateButton = (Button) findViewById(R.id.translateButton);
		input.setText("we r meeting 2nite ?");
		output = (TextView) findViewById(R.id.textViewTranslated);
		hindiTypeface = Typeface.createFromAsset(getAssets(), "DroidHindi.ttf");
		translateButton.setTypeface(hindiTypeface);
		output.setTypeface(hindiTypeface);
		speakButton = (Button) findViewById(R.id.speakButton);
		speakButton.setTypeface(hindiTypeface);
		speakButton.setEnabled(false);
		speakButton.setText("सुने ");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.settings, menu);
		getMenuInflater().inflate(R.menu.cached, menu);
		getMenuInflater().inflate(R.menu.add_translation, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent i = null;
		switch (item.getItemId()) {
		case R.id.menu_settings:
			i = new Intent(this, prefs.TranslationPreference.class);
			startActivity(i);
			break;
		case R.id.stored_translations:
			i = new Intent(this, ShowCachedActivity.class);
			startActivity(i);
			break;
		case R.id.add_translation:
			if (currEnglish == null || currHindi == null) {
				Toast err = new Toast(getApplicationContext());
				TextView tr = new TextView(getApplicationContext());
				tr.setTypeface(hindiTypeface);
				tr.setText("पहले अनुवाद तो कीजिए!");
				err.setView(tr);
				err.setDuration(Toast.LENGTH_SHORT);
				err.show();
			} else {
				tch.addTranslation(new CachedTranslation(currNormed, currHindi));
				Toast msg = new Toast(getApplicationContext());
				TextView tr = new TextView(getApplicationContext());
				tr.setTypeface(hindiTypeface);
				tr.setText("अनुवाद संग्रहित!");
				msg.setView(tr);
				msg.setDuration(Toast.LENGTH_SHORT);
				msg.show();
			}

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

		@SuppressWarnings("deprecation")
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			showDialog(DIALOG_DOWNLOAD_PROGRESS);
		}

		protected String doInBackground(String... v) {
			final int PORT = 1234;

			Log.d("SENDING", "SENDING REQUEST");
			Log.d("HOST : ", HOST);
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
				currEnglish = new String(sentence);
				Log.d("TEXT :", sentence);
				publishProgress("" + 20);
				int SCORE_INDEX = 0;
				int XLATION_INDEX = 2;
				int NORMED_INDEX = 1;
				if (translationType == TranslatorActivity.MOSES_TYPE) {
					Log.d("XLATOR CHOSEN", "Moses");
					outToServer.writeBytes("MOSES\n");
					outToServer.writeBytes(sentence + '\n');

					String inp[] = inFromServer.readLine().split("##");
					translatedSentence = inp[XLATION_INDEX];
					currNormed = inp[NORMED_INDEX];
					double score = Double.parseDouble(inp[SCORE_INDEX]);
					Log.d("Got from server :", inp[0]);
					publishProgress("" + 50);
					String inputWords[] = currNormed.split(" ");
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
					translatedSentence = justTranslation.toString();
					Log.d("XLATION : ", translatedSentence);

				} // end MOSES
				else if (translationType == TranslatorActivity.DICT_TYPE) {
					outToServer.writeBytes("DICT\n");
					outToServer.writeBytes(sentence + '\n');
					translatedSentence = inFromServer.readLine();
					publishProgress("" + 50);
					// now read the synonyms for each word
					// Format -> word : syn1, syn2...
					Log.d("XLATION : ", translatedSentence);
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
			currHindi = translatedSentence;
			return translatedSentence;
		}

		protected void onProgressUpdate(String... progress) {
			mProgressDialog.setProgress(Integer.parseInt(progress[0]));
		}

		@SuppressWarnings("deprecation")
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
						input.setText(currNormed);
						Log.d("YES!!!", "I DO COME HERE");
						String colors[] = { "Red", "Orange", "Green", "Blue", "Gray", "Purple", "Magenta", "Aqua", "Pink" };
						output.setText(translatedSentence, BufferType.SPANNABLE);
						output.setMovementMethod(LinkMovementMethod
								.getInstance());

						StringBuilder ori = new StringBuilder();
						StringBuilder trans = new StringBuilder();
						String fontS = "<font color=";

						String fontE = "</font>";
						int colorNum = 0;

						for (String apair : alignedPair) {
							String pair[] = apair.split("=");
							String fontC = "'" + colors[colorNum] + "'>";
							colorNum++;
							colorNum = colorNum % colors.length;
							ori.append(fontS + fontC + pair[0] + fontE + " ");
							trans.append(fontS + fontC + pair[1] + fontE + " ");
						}
						Log.d("Colored : ", ori.toString());
						align.setText(
								Html.fromHtml("अनुवाद :   <b>"
										+ translatedSentence
										+ "</b><br/><br/> <b>प्रक्रिया जानिए : </b> <br/>"
										+ ori.toString() + "<br/>"
										+ trans.toString()),
								TextView.BufferType.SPANNABLE);
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

	@Override
	public void onInit(int status) {
		if (status == TextToSpeech.SUCCESS) {

			int result = tts.setLanguage(Locale.US);

			if (result == TextToSpeech.LANG_MISSING_DATA
					|| result == TextToSpeech.LANG_NOT_SUPPORTED) {
				Log.e("TTS", "This Language is not supported");
			} else {
				speakButton.setEnabled(true);
			}

		} else {
			Log.e("TTS", "Initilization Failed!");
		}

	}

	public void speakOut(View v) {
		String sentence = input.getText().toString();
		SharedPreferences sharedPref = PreferenceManager
				.getDefaultSharedPreferences(this);
		float speed = Float.parseFloat(sharedPref.getString("speech_speed",
				"1.0"));
		tts.setSpeechRate(speed);
		tts.speak(sentence, TextToSpeech.QUEUE_FLUSH, null);
	}

}
