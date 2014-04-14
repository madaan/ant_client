package caching;

import java.util.ArrayList;

import android.app.Activity;
import android.graphics.Typeface;
import android.os.Bundle;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TableRow.LayoutParams;
import android.widget.TextView;

import com.example.plotter.R;
import com.example.plotter.R.color;

public class ShowCachedActivity extends Activity {

	TableLayout tl;
	Typeface hindiTypeface;
	public ShowCachedActivity() {
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.show_cached);
		tl = (TableLayout) findViewById(R.id.cache);
		hindiTypeface = Typeface.createFromAsset(getAssets(), "DroidHindi.ttf");
		
		populate();
	}

	@SuppressWarnings("deprecation")
	void populate() {
		TranslationCachingHandler th = new TranslationCachingHandler(getApplicationContext());
		ArrayList<CachedTranslation> allTrans = th.getAllTranslations();
		for (int i = 0; i < allTrans.size(); i++) {
			TableRow tr = new TableRow(this);
			tr.setId(i);
			tr.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
					LayoutParams.WRAP_CONTENT));
			TextView english = new TextView(this);
			english.setId(i + 10);
			english.setPadding(20, 20, 20, 20);
			//english.setBackgroundColor(color.LightGrey);
			
			english.setText(allTrans.get(i).getEnglish());
			

			english.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
					LayoutParams.WRAP_CONTENT));
		
			tr.addView(english);

			TextView hindi = new TextView(this);
			hindi.setId(i + 10);
			hindi.setPadding(20, 20, 20, 20);
			//hindi.setBackgroundColor(color.SlateBlue);
			hindi.setText(allTrans.get(i).getHindi());
			hindi.setTypeface(hindiTypeface);
			hindi.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
					LayoutParams.WRAP_CONTENT));
			tr.addView(hindi);

			// Add the TableRow to the TableLayout
			tl.addView(tr, new TableLayout.LayoutParams(
					LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

		}

	}
}
