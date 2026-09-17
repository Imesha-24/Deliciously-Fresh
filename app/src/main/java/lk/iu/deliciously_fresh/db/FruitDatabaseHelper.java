package lk.iu.deliciously_fresh.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class FruitDatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME    = "deliciouslyfresh.db";
    private static final int    DB_VERSION = 2;
    private static final String TABLE      = "fruit_images";
    private static final String COL_ID     = "fruit_id";
    private static final String COL_URL    = "image_url";

    public FruitDatabaseHelper(Context context) {
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
    public void onUpgrade(SQLiteDatabase db, int old, int newV) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE);
        onCreate(db);
    }

    private void seedData(SQLiteDatabase db) {
        String[][] data = {
                {"fruit1", "https://images.unsplash.com/photo-1553279768-865429fa0078?w=600"},
                {"fruit2", "https://images.unsplash.com/photo-1550258987-190a2d41a8ba?w=600"},
                {"fruit3", "https://images.unsplash.com/photo-1603833665858-e61d17a86224?w=600"},
                {"fruit4", "https://images.unsplash.com/photo-1580984969071-a8da5656c2fb?w=600"},
                {"fruit5", "https://images.unsplash.com/photo-1567306226416-28f0efdc88ce?w=600"},
                {"fruit6", "https://images.unsplash.com/photo-1582979512210-99b6a53386f9?w=600"},
                {"fruit7", "https://images.unsplash.com/photo-1464965911861-746a04b4bca6?w=600"},
                {"fruit8", "https://images.unsplash.com/photo-1571575173700-afb9492e6a50?w=600"}
        };
        for (String[] row : data) {
            ContentValues cv = new ContentValues();
            cv.put(COL_ID,  row[0]);
            cv.put(COL_URL, row[1]);
            db.insertWithOnConflict(TABLE, null, cv, SQLiteDatabase.CONFLICT_IGNORE);
        }
    }

    public String getImageUrl(String fruitId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(TABLE, new String[]{COL_URL},
                COL_ID + "=?", new String[]{fruitId},
                null, null, null);
        String url = null;
        if (c.moveToFirst()) url = c.getString(0);
        c.close();
        return url;
    }

    /**
     * Inserts/updates a cached image representation (raw URL or Base64 string)
     * for a given fruit id.
     */
    public void upsertImage(String fruitId, String imageData) {
        if (fruitId == null || fruitId.isEmpty()) return;
        if (imageData == null || imageData.isEmpty()) return;

        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_ID, fruitId);
        cv.put(COL_URL, imageData);
        db.insertWithOnConflict(TABLE, null, cv, SQLiteDatabase.CONFLICT_REPLACE);
    }
}
