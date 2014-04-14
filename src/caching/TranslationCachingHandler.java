//sg
package caching;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class TranslationCachingHandler extends SQLiteOpenHelper {
	// All Static variables
	// Database Version
	private static final int DATABASE_VERSION = 1;

	// Database Name
	private static final String DATABASE_NAME = "cachedTranslationManager";

	// Translations table name
	private static final String TABLE_TRANSLATIONS = "translations";

	// Translations Table Columns names
	private static final String KEY_ID = "id";
	private static final String KEY_ENG = "english";
	private static final String KEY_HINDI = "hindi";

	private static final int ENG_INDEX = 1;
	private static final int HINDI_INDEX = 2;

	public TranslationCachingHandler(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	// Creating Tables
	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRANSLATIONS);

		
		String CREATE_TranslationS_TABLE = "CREATE TABLE " + TABLE_TRANSLATIONS
				+ "(" + KEY_ID + " INTEGER PRIMARY KEY," + KEY_ENG + " TEXT,"
				+ KEY_HINDI + " TEXT" + ")";
		db.execSQL(CREATE_TranslationS_TABLE);
	}

	// Upgrading database
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// Drop older table if existed
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRANSLATIONS);

		// Create tables again
		onCreate(db);
	}

	/**
	 * Add a new translation to the database
	 */
	public void addTranslation(CachedTranslation t) {

		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(KEY_ENG, t.getEnglish()); // Translation Name
		values.put(KEY_HINDI, t.getHindi()); // Translation Phone Number
		// Inserting Row
		db.insert(TABLE_TRANSLATIONS, null, values);
		db.close(); // Closing database connection
	}

	/**
	 * This method returns all the stored translations
	 * 
	 * @return
	 */
	public ArrayList<CachedTranslation> getAllTranslations() {
		ArrayList<CachedTranslation> TranslationList = new ArrayList<CachedTranslation>();
		// Select All Query
		String selectQuery = "SELECT  * FROM " + TABLE_TRANSLATIONS;

		SQLiteDatabase db = this.getWritableDatabase();
		Cursor cursor = db.rawQuery(selectQuery, null);

		// looping through all rows and adding to list
		if (cursor.moveToFirst()) {
			do {
				CachedTranslation Translation = new CachedTranslation();
				Translation.setId(Integer.parseInt(cursor.getString(0)));
				Translation.setEnglish(cursor.getString(ENG_INDEX));
				Translation.setHindi(cursor.getString(HINDI_INDEX));
				// Adding Translation to list
				TranslationList.add(Translation);
			} while (cursor.moveToNext());
		}

		// return Translation list
		return TranslationList;
	}

}
