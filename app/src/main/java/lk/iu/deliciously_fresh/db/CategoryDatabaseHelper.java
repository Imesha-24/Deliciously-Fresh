package lk.iu.deliciously_fresh.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class CategoryDatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME    = "deliciouslyfresh.db";
    private static final int    DB_VERSION = 2;
    private static final String TABLE      = "category_images";
    private static final String COL_ID     = "category_id";
    private static final String COL_URL    = "image_url";

    public CategoryDatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE + " (" +
                COL_ID  + " TEXT PRIMARY KEY, " +
                COL_URL + " TEXT NOT NULL)");
        seedData(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE);
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Defensive fallback: align with upgrade behavior instead of crashing.
        onUpgrade(db, oldVersion, newVersion);
    }

    private void seedData(SQLiteDatabase db) {
        String[][] data = {
                {"cat1", "https://images.unsplash.com/photo-1567306226416-28f0efdc88ce?w=400"},
                {"cat2", "https://images.unsplash.com/photo-1547514701-42782101795e?w=400"},
                {"cat3", "https://images.unsplash.com/photo-1537640538966-79f369143f8f?w=400"},
                {"cat4", "https://images.unsplash.com/photo-1502741338009-cac2772e18bc?w=400"},
                {"cat5", "https://images.unsplash.com/photo-1464965911861-746a04b4bca6?w=400"},
                {"cat6", "https://images.unsplash.com/photo-1571575173700-afb9492e6a50?w=400"},
                {"cat7", "https://images.unsplash.com/photo-1574226516831-e1dff420e12b?w=400"},
                {"cat8", "https://images.unsplash.com/photo-1523049673857-eb18f1d7b578?w=400"}
        };
        for (String[] row : data) {
            ContentValues cv = new ContentValues();
            cv.put(COL_ID,  row[0]);
            cv.put(COL_URL, row[1]);
            db.insertWithOnConflict(TABLE, null, cv, SQLiteDatabase.CONFLICT_IGNORE);
        }
    }

    public String getImageUrl(String categoryId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE, new String[]{COL_URL},
                COL_ID + "=?", new String[]{categoryId},
                null, null, null);
        String url = null;
        if (cursor.moveToFirst()) {
            url = cursor.getString(0);
        }
        cursor.close();
        return url;
    }

    public void insertOrUpdate(String categoryId, String imageUrl) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_ID,  categoryId);
        cv.put(COL_URL, imageUrl);
        db.insertWithOnConflict(TABLE, null, cv, SQLiteDatabase.CONFLICT_REPLACE);
    }
}
