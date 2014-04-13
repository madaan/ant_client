package caching;

import java.util.ArrayList;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TableRow.LayoutParams;
import android.widget.TextView;

import com.example.plotter.R;

public class ShowCachedActivity extends Activity {

	TableLayout tl;

	public ShowCachedActivity() {
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.show_cached);
		tl = (TableLayout) findViewById(R.id.cache);
		populate();
	}

	@SuppressWarnings("deprecation")
	void populate() {
		TranslationHandler th = new TranslationHandler(getApplicationContext());
		ArrayList<Translation> allTrans = th.getAllTranslations();
		for (int i = 0; i < allTrans.size(); i++) {
			TableRow tr = new TableRow(this);
			tr.setId(i);
			tr.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,
					LayoutParams.WRAP_CONTENT));
			TextView english = new TextView(this);
			english.setId(i + 10);
			english.setText(allTrans.get(i).getEnglish());

			english.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,
					LayoutParams.WRAP_CONTENT));
			tr.addView(english);

			TextView hindi = new TextView(this);
			hindi.setId(i + 10);
			hindi.setText(allTrans.get(i).getHindi());

			hindi.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,
					LayoutParams.WRAP_CONTENT));
			tr.addView(hindi);

			// Add the TableRow to the TableLayout
			tl.addView(tr, new TableLayout.LayoutParams(
					LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));

		}

	}
}
