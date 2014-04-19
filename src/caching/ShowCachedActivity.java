package caching;

import java.util.ArrayList;

import android.app.Activity;
import android.graphics.Color;
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
		for (int i = 1; i < allTrans.size(); i++) {
			TableRow tr = new TableRow(this);
			TableLayout.LayoutParams trl = new TableLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
					LayoutParams.WRAP_CONTENT); 
			trl.width = R.string._0dp;
			trl.weight = R.string._1;
			tr.setId(i);
			tr.setLayoutParams(trl);
			TextView english = new TextView(this);
			english.setId(i + 10);
			english.setPadding(20, 20, 20, 20);
			//english.setBackgroundColor(color.LightGrey);
			
			String englishTrans = allTrans.get(i).getEnglish();
			String hindiTrans = allTrans.get(i).getHindi();
			
			english.setText(englishTrans + "- >" + hindiTrans);
			

			english.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
					LayoutParams.WRAP_CONTENT));
			
			tr.setBackgroundColor(Color.rgb((i * 10) % 256 + 100, (i * 10) % 256  + 100, (i * 10) % 256  + 100));
			//tr.setBackgroundColor(Color.)
			tr.addView(english);
/*
			TextView hindi = new TextView(this);
			hindi.setId(i + 10);
			//hindi.setPadding(20, 20, 20, 20);
			//hindi.setBackgroundColor(color.SlateBlue);
			hindi.setText(allTrans.get(i).getHindi());
			hindi.setTypeface(hindiTypeface);
			hindi.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
					LayoutParams.WRAP_CONTENT));
			tr.addView(hindi);
*/
			// Add the TableRow to the TableLayout
			tl.addView(tr, new TableLayout.LayoutParams(
					LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

		}

	}
}
