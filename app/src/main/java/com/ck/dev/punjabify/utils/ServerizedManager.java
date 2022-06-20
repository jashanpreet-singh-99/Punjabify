package com.ck.dev.punjabify.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import com.ck.dev.punjabify.model.ServerizedTrackData;

import java.util.ArrayList;
import java.util.Objects;

/**
 * create a main db with all tracks and a unique id                           X  1
 * now create database for each genre with unique id from the parent table    X  13
 * for each database one table artist database                                X  A
 * table to keep artist name                                                  X  1
 * table to keep downloaded image record // can say its a caching system
 */
public class ServerizedManager extends SQLiteOpenHelper {

    public ServerizedManager(Context context) {
        super(context, ServerizedConfig.DATABASE_ID, null, 1);
    }

    /**
     * Create the Table for Serverized Tracks
     * @param db The default DB
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
                "CREATE TABLE IF NOT EXISTS " + ServerizedConfig.TABLE_NAME +
                        "(" +
                        ServerizedConfig.COLUMN_INDEX + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        ServerizedConfig.COLUMN_ALBUM + " TEXT, " +
                        ServerizedConfig.COLUMN_ARTIST + " TEXT," +
                        ServerizedConfig.COLUMN_GEDI + " INTEGER NOT NULL CHECK (" + ServerizedConfig.COLUMN_GEDI +" IN (0,1))," +
                        ServerizedConfig.COLUMN_GENDER + " TEXT NOT NULL CHECK (" + ServerizedConfig.COLUMN_GENDER +" IN ('M','D','F'))," +
                        ServerizedConfig.COLUMN_HIP_HOP + " INTEGER NOT NULL CHECK (" + ServerizedConfig.COLUMN_HIP_HOP +" IN (0,1))," +
                        ServerizedConfig.COLUMN_JATTISM + " INTEGER NOT NULL CHECK (" + ServerizedConfig.COLUMN_JATTISM +" IN (0,1))," +
                        ServerizedConfig.COLUMN_LEGEND + " INTEGER NOT NULL CHECK (" + ServerizedConfig.COLUMN_LEGEND +" IN (0,1))," +
                        ServerizedConfig.COLUMN_LINK + " TEXT," +
                        ServerizedConfig.COLUMN_LONG_DRIVE + " INTEGER NOT NULL CHECK (" + ServerizedConfig.COLUMN_LONG_DRIVE +" IN (0,1))," +
                        ServerizedConfig.COLUMN_MAHFIL + " INTEGER NOT NULL CHECK (" + ServerizedConfig.COLUMN_MAHFIL +" IN (0,1))," +
                        ServerizedConfig.COLUMN_ORIGINAL + " INTEGER NOT NULL CHECK (" + ServerizedConfig.COLUMN_ORIGINAL +" IN (0,1))," +
                        ServerizedConfig.COLUMN_PARENTAL + " INTEGER NOT NULL CHECK (" + ServerizedConfig.COLUMN_PARENTAL +" IN (0,1))," +
                        ServerizedConfig.COLUMN_PARTY + " INTEGER NOT NULL CHECK (" + ServerizedConfig.COLUMN_PARTY +" IN (0,1))," +
                        ServerizedConfig.COLUMN_PRO + " INTEGER NOT NULL CHECK (" + ServerizedConfig.COLUMN_PRO +" IN (0,1))," +
                        ServerizedConfig.COLUMN_RAP + " INTEGER NOT NULL CHECK (" + ServerizedConfig.COLUMN_RAP +" IN (0,1))," +
                        ServerizedConfig.COLUMN_RELEASE + " TEXT," +
                        ServerizedConfig.COLUMN_ROMANTIC + " INTEGER NOT NULL CHECK (" + ServerizedConfig.COLUMN_ROMANTIC +" IN (0,1))," +
                        ServerizedConfig.COLUMN_SAD + " INTEGER NOT NULL CHECK (" + ServerizedConfig.COLUMN_SAD +" IN (0,1))," +
                        ServerizedConfig.COLUMN_TITLE + " TEXT " +
                        ")"
        );
        db.execSQL(
                "CREATE TABLE IF NOT EXISTS " + ServerizedConfig.TABLE_ARTIST_ALL +
                        "(" +
                        ServerizedConfig.COLUMN_INDEX + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        ServerizedConfig.COLUMN_ARTIST + " TEXT, " +
                        ServerizedConfig.COLUMN_FOLLOW + " INTEGER NOT NULL CHECK (" + ServerizedConfig.COLUMN_FOLLOW +" IN (0,1))" +
                        ")"
        );
        for (String genre: GenreConfig.getGenres()) {
            db.execSQL(
                    "CREATE TABLE IF NOT EXISTS " + ServerizedConfig.TABLE_GENRE + genre +
                            " (" +
                            ServerizedConfig.COLUMN_INDEX + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                            ServerizedConfig.COLUMN_RELEASE + " TEXT, " +
                            ServerizedConfig.COLUMN_ROW_ID + " INTEGER" +
                            ")"
            );
        }

        db.execSQL(
                "CREATE TABLE IF NOT EXISTS " + ServerizedConfig.DOWNLOAD_QUEUE +
                        " (" +
                        ServerizedConfig.COLUMN_INDEX + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                        ServerizedConfig.COLUMN_ROW_ID + " INTEGER UNIQUE" +
                        ")"
        );
    }

    public void dropOldData() {
        SQLiteDatabase database = this.getWritableDatabase();
        database.execSQL("DROP TABLE IF EXISTS " + ServerizedConfig.TABLE_NAME);
        database.execSQL("DROP TABLE IF EXISTS " + ServerizedConfig.TABLE_ARTIST_ALL);
        for (String genre: GenreConfig.getGenres()) {
            database.execSQL("DROP TABLE IF EXISTS " + ServerizedConfig.TABLE_GENRE + genre);
        }
        database.execSQL("DROP TABLE IF EXISTS " + ServerizedConfig.DOWNLOAD_QUEUE);
        onCreate(database);
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        database.execSQL("DROP TABLE IF EXISTS " + ServerizedConfig.TABLE_NAME);
        onCreate(database);
    }

    public int insertTrack(ServerizedTrackData serverizedTrackData) {
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(ServerizedConfig.COLUMN_ALBUM, serverizedTrackData.getAlbum());
        contentValues.put(ServerizedConfig.COLUMN_ARTIST, serverizedTrackData.getArtist());
        contentValues.put(ServerizedConfig.COLUMN_GEDI, serverizedTrackData.getGedi());
        contentValues.put(ServerizedConfig.COLUMN_GENDER, serverizedTrackData.getGender());
        contentValues.put(ServerizedConfig.COLUMN_HIP_HOP, serverizedTrackData.getHipHop());
        contentValues.put(ServerizedConfig.COLUMN_JATTISM, serverizedTrackData.getJattism());
        contentValues.put(ServerizedConfig.COLUMN_LEGEND, serverizedTrackData.getLegend());
        contentValues.put(ServerizedConfig.COLUMN_LINK, serverizedTrackData.getLink());
        contentValues.put(ServerizedConfig.COLUMN_LONG_DRIVE, serverizedTrackData.getLongDrive());
        contentValues.put(ServerizedConfig.COLUMN_MAHFIL, serverizedTrackData.getMahfil());
        contentValues.put(ServerizedConfig.COLUMN_ORIGINAL, serverizedTrackData.getOriginal());
        contentValues.put(ServerizedConfig.COLUMN_PARENTAL, serverizedTrackData.getParental());
        contentValues.put(ServerizedConfig.COLUMN_PARTY, serverizedTrackData.getParty());
        contentValues.put(ServerizedConfig.COLUMN_PRO, serverizedTrackData.getPro());
        contentValues.put(ServerizedConfig.COLUMN_RAP, serverizedTrackData.getRap());
        contentValues.put(ServerizedConfig.COLUMN_RELEASE, serverizedTrackData.getRelease());
        contentValues.put(ServerizedConfig.COLUMN_ROMANTIC, serverizedTrackData.getRomantic());
        contentValues.put(ServerizedConfig.COLUMN_SAD, serverizedTrackData.getSad());
        contentValues.put(ServerizedConfig.COLUMN_TITLE, serverizedTrackData.getTitle());
        return Integer.parseInt(database.insert(ServerizedConfig.TABLE_NAME, null, contentValues) + "");
    }

    public int insertGenreData(String genre, String release, int id) {
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(ServerizedConfig.COLUMN_ROW_ID, id);
        contentValues.put(ServerizedConfig.COLUMN_RELEASE, release);
        return Integer.parseInt(database.insert(ServerizedConfig.TABLE_GENRE + genre, null, contentValues) + "");
    }

    public int insertArtistData(String name) {
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(ServerizedConfig.COLUMN_ARTIST, name);
        contentValues.put(ServerizedConfig.COLUMN_FOLLOW, 0);
        return Integer.parseInt(database.insert(ServerizedConfig.TABLE_ARTIST_ALL, null, contentValues) + "");
    }

    public void updateArtistFollowed(String name) {
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(ServerizedConfig.COLUMN_FOLLOW, 1);
        String   whereClause = ServerizedConfig.COLUMN_ARTIST + " = ?";
        String[] whereArg    = {name};
        database.update(ServerizedConfig.TABLE_ARTIST_ALL, contentValues, whereClause, whereArg);
    }

    public void resetFollowedArtist() {
        SQLiteDatabase database = this.getWritableDatabase();
        try {
            database.execSQL("UPDATE " + ServerizedConfig.TABLE_ARTIST_ALL + " SET " + ServerizedConfig.COLUMN_FOLLOW + "=0");
        } catch (Exception e) {
            Config.LOG(Config.TAG_DATABASE, "Error Follow Reset to 0. " + e, false);
        }
    }

    public boolean updateTrackToDownloaded(int id) {
        SQLiteDatabase database = this.getWritableDatabase();
        try {
            database.execSQL("UPDATE " + ServerizedConfig.TABLE_NAME + " SET " +
                    ServerizedConfig.COLUMN_LINK + "='" + Config.DOWNLOADED_TRACK  + "' WHERE " +
                    ServerizedConfig.COLUMN_INDEX + "=" + id + "");
            return true;
        } catch (Exception e) {
            Config.LOG(Config.TAG_DATABASE, "Error Track downloaded update. " + e, false);
            return false;
        }
    }

    public void createArtistTable(String name) {
        SQLiteDatabase database = this.getWritableDatabase();
        name = name.replace(" ", "_");
        database.execSQL("DROP TABLE IF EXISTS " + ServerizedConfig.TABLE_ARTIST + name);
        database.execSQL(
                "CREATE TABLE IF NOT EXISTS " + ServerizedConfig.TABLE_ARTIST + name +
                        "(" +
                        ServerizedConfig.COLUMN_INDEX + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        ServerizedConfig.COLUMN_RELEASE + " TEXT," +
                        ServerizedConfig.COLUMN_ROW_ID + " INTEGER," +
                        "FOREIGN KEY (" + ServerizedConfig.COLUMN_ROW_ID + ") REFERENCES " +
                        ServerizedConfig.TABLE_NAME + " (" +
                        ServerizedConfig.COLUMN_INDEX + ")" +
                        ")"
        );
    }

    public int insertArtistTrackData(String name, String release, int id) {
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(ServerizedConfig.COLUMN_ROW_ID, id);
        contentValues.put(ServerizedConfig.COLUMN_RELEASE, release);
        return Integer.parseInt(database.insert(ServerizedConfig.TABLE_ARTIST + name, null, contentValues) + "");
    }

    public ArrayList<ServerizedTrackData> getArtists(Boolean followed) {
        SQLiteDatabase database = this.getReadableDatabase();
        ArrayList<ServerizedTrackData> data = new ArrayList<>();
        Cursor cursor;
        if (followed) {
            cursor = database.rawQuery("SELECT * FROM " + ServerizedConfig.TABLE_ARTIST_ALL + " WHERE " + ServerizedConfig.COLUMN_FOLLOW + "=1", null);
        } else {
            cursor = database.rawQuery("SELECT * FROM " + ServerizedConfig.TABLE_ARTIST_ALL, null);
        }
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            ServerizedTrackData serverizedTrackData = new ServerizedTrackData(
                    0,
                    null,
                    cursor.getString(cursor.getColumnIndex(ServerizedConfig.COLUMN_ARTIST)),
                    -1,
                    null,
                    -1,
                    -1,
                    -1,
                    "",
                    -1,
                    -1,
                    -1,
                    -1,
                    -1,
                    -1,
                    -1,
                    null,
                    -1,
                    -1,
                    null
            );
            data.add(serverizedTrackData);
            cursor.moveToNext();
        }
        cursor.close();
        return data;
    }

    public ArrayList<String> getArtistsNameOnly(int mode) {
        SQLiteDatabase database = this.getReadableDatabase();
        ArrayList<String> data = new ArrayList<>();
        Cursor cursor;
        switch (mode) {
            case ServerizedConfig.ARTIST_MODE_FOLLOWED:
                cursor = database.rawQuery("SELECT * FROM " + ServerizedConfig.TABLE_ARTIST_ALL + " WHERE " + ServerizedConfig.COLUMN_FOLLOW + "=1", null);
                break;
            case ServerizedConfig.ARTIST_MODE_UN_FOLLOWED:
                cursor = database.rawQuery("SELECT * FROM " + ServerizedConfig.TABLE_ARTIST_ALL + " WHERE " + ServerizedConfig.COLUMN_FOLLOW + "=0", null);
                break;
            default:
                cursor = database.rawQuery("SELECT * FROM " + ServerizedConfig.TABLE_ARTIST_ALL, null);
                break;
        }
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            data.add(cursor.getString(cursor.getColumnIndex(ServerizedConfig.COLUMN_ARTIST)));
            cursor.moveToNext();
        }
        cursor.close();
        return data;
    }

    public ArrayList<ServerizedTrackData> getArtistAllTracks(String artist) {
        SQLiteDatabase database = this.getReadableDatabase();
        ArrayList<ServerizedTrackData> data = new ArrayList<>();
        artist = artist.replace(" ", "_");
        Cursor cursor = database.rawQuery("SELECT * FROM " + ServerizedConfig.TABLE_ARTIST + artist + " ORDER BY " + ServerizedConfig.COLUMN_RELEASE + " DESC" , null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            data.add(getIdSpecificTrack(cursor.getInt(cursor.getColumnIndex(ServerizedConfig.COLUMN_ROW_ID))));
            cursor.moveToNext();
        }
        cursor.close();
        return data;
    }

    public ArrayList<ServerizedTrackData> getGenreAllTracks(String genre) {
        SQLiteDatabase database = this.getReadableDatabase();
        ArrayList<ServerizedTrackData> data = new ArrayList<>();
        Cursor cursor = database.rawQuery("SELECT * FROM " + ServerizedConfig.TABLE_GENRE + genre + " ORDER BY " + ServerizedConfig.COLUMN_RELEASE + " DESC", null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            ServerizedTrackData serverizedTrackData = getIdSpecificTrack(cursor.getInt(cursor.getColumnIndex(ServerizedConfig.COLUMN_ROW_ID)));
            if (serverizedTrackData != null) {
                data.add(serverizedTrackData);
            }
            cursor.moveToNext();
        }
        cursor.close();
        return data;
    }

    public ServerizedTrackData getIdSpecificTrack(int id) {
        SQLiteDatabase database = this.getReadableDatabase();
        Cursor cursor = database.rawQuery("SELECT * FROM " + ServerizedConfig.TABLE_NAME + " WHERE " + ServerizedConfig.COLUMN_INDEX + "=" + id + "", null);
        cursor.moveToFirst();
        ServerizedTrackData trackData = new ServerizedTrackData(
                cursor.getInt(cursor.getColumnIndex(ServerizedConfig.COLUMN_INDEX)),
                cursor.getString(cursor.getColumnIndex(ServerizedConfig.COLUMN_ALBUM)),
                cursor.getString(cursor.getColumnIndex(ServerizedConfig.COLUMN_ARTIST)),
                cursor.getInt(cursor.getColumnIndex(ServerizedConfig.COLUMN_GEDI)),
                cursor.getString(cursor.getColumnIndex(ServerizedConfig.COLUMN_GENDER)),
                cursor.getInt(cursor.getColumnIndex(ServerizedConfig.COLUMN_HIP_HOP)),
                cursor.getInt(cursor.getColumnIndex(ServerizedConfig.COLUMN_JATTISM)),
                cursor.getInt(cursor.getColumnIndex(ServerizedConfig.COLUMN_LEGEND)),
                cursor.getString(cursor.getColumnIndex(ServerizedConfig.COLUMN_LINK)),
                cursor.getInt(cursor.getColumnIndex(ServerizedConfig.COLUMN_LONG_DRIVE)),
                cursor.getInt(cursor.getColumnIndex(ServerizedConfig.COLUMN_MAHFIL)),
                cursor.getInt(cursor.getColumnIndex(ServerizedConfig.COLUMN_ORIGINAL)),
                cursor.getInt(cursor.getColumnIndex(ServerizedConfig.COLUMN_PARENTAL)),
                cursor.getInt(cursor.getColumnIndex(ServerizedConfig.COLUMN_PRO)),
                cursor.getInt(cursor.getColumnIndex(ServerizedConfig.COLUMN_PARTY)),
                cursor.getInt(cursor.getColumnIndex(ServerizedConfig.COLUMN_RAP)),
                cursor.getString(cursor.getColumnIndex(ServerizedConfig.COLUMN_RELEASE)),
                cursor.getInt(cursor.getColumnIndex(ServerizedConfig.COLUMN_ROMANTIC)),
                cursor.getInt(cursor.getColumnIndex(ServerizedConfig.COLUMN_SAD)),
                cursor.getString(cursor.getColumnIndex(ServerizedConfig.COLUMN_TITLE))
        );
        if (!isArtistFollowed(cursor.getString(cursor.getColumnIndex(ServerizedConfig.COLUMN_ARTIST)))) {
            return null;
        }
        cursor.close();
        return trackData;
    }

    public Boolean isArtistFollowed(String artist) {
        SQLiteDatabase database = this.getReadableDatabase();
        Cursor cursor = database.rawQuery("SELECT * FROM " + ServerizedConfig.TABLE_ARTIST_ALL + " WHERE " + ServerizedConfig.COLUMN_ARTIST + "='" + artist + "'", null);
        cursor.moveToFirst();
        int follow = cursor.getInt(cursor.getColumnIndex(ServerizedConfig.COLUMN_FOLLOW));
        cursor.close();
        return follow == 1;
    }


    public void createQueueDB() {
        SQLiteDatabase database = getWritableDatabase();
        database.execSQL("DROP TABLE IF EXISTS " + ServerizedConfig.QUEUE_NAME);
        database.execSQL(
                "CREATE TABLE " + ServerizedConfig.QUEUE_NAME +
                        "(" +
                        ServerizedConfig.COLUMN_INDEX + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                        ServerizedConfig.COLUMN_ROW_ID + " INTEGER" +
                 ")"
        );
    }

    public boolean insertQueueTrack(int indexID ) {
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(ServerizedConfig.COLUMN_ROW_ID, indexID);
        database.insert(ServerizedConfig.QUEUE_NAME, null, contentValues);
        return true;
    }

    public ArrayList<Integer> getQueue() {
        ArrayList<Integer> data = new ArrayList<>();
        try {
            SQLiteDatabase database = this.getReadableDatabase();
            Cursor cursor = database.rawQuery("SELECT * FROM " + ServerizedConfig.QUEUE_NAME, null);
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                data.add(cursor.getInt(cursor.getColumnIndex(ServerizedConfig.COLUMN_ROW_ID)));
                cursor.moveToNext();
            }
            cursor.close();
        } catch (SQLiteException e) {
            Config.LOG(Config.TAG_DATABASE, "Queue Table : " + e.getMessage(), false);
            return null;
        }
        return data;
    }

    public void dropQueue() {
        SQLiteDatabase database = this.getWritableDatabase();
        database.execSQL("DELETE FROM " + ServerizedConfig.QUEUE_NAME);
    }

    public void createDownloadQueueDB() {
        SQLiteDatabase database = getWritableDatabase();
        Config.LOG(Config.TAG_DATABASE, "Creating Download Queue : ", false);
        database.execSQL(
                "CREATE TABLE " + ServerizedConfig.DOWNLOAD_QUEUE +
                        " (" +
                        ServerizedConfig.COLUMN_INDEX + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        ServerizedConfig.COLUMN_ROW_ID + " INTEGER UNIQUE" +
                        ")"
        );
    }

    public void clearDownloadQueue() {
        SQLiteDatabase database = this.getWritableDatabase();
        database.execSQL("DELETE FROM " + ServerizedConfig.DOWNLOAD_QUEUE);
    }

    public boolean insertDownloadQueueTrack(int indexID ) {
        SQLiteDatabase database = this.getWritableDatabase();
        long val = 0;
        try {
            ContentValues contentValues = new ContentValues();
            contentValues.put(ServerizedConfig.COLUMN_ROW_ID, indexID);
            val = database.insert(ServerizedConfig.DOWNLOAD_QUEUE, null, contentValues);
            Config.LOG(Config.TAG_DATABASE, "New Track into Download " + val, false);
        }  catch (Exception e) {
            Config.LOG(Config.TAG_DATABASE, "Caught Exception " + e.getMessage(), true);
            if (Objects.requireNonNull(e.getMessage()).startsWith("no such table:")) {
                createDownloadQueueDB();
                return insertDownloadQueueTrack(indexID);
            }
        }
        return val > 0;
    }

    public void removeDownloadQueueTrack(int indexID ) {
        SQLiteDatabase database = this.getWritableDatabase();
        database.execSQL("DELETE FROM " + ServerizedConfig.DOWNLOAD_QUEUE + " WHERE " + ServerizedConfig.COLUMN_ROW_ID + "=" + indexID);
    }

    public ArrayList<ServerizedTrackData> getDownloadQueue() {
        ArrayList<ServerizedTrackData> data = new ArrayList<>();
        try {
            SQLiteDatabase database = this.getReadableDatabase();
            Cursor cursor = database.rawQuery("SELECT * FROM " + ServerizedConfig.DOWNLOAD_QUEUE, null);
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                data.add(getIdSpecificTrack(cursor.getInt(cursor.getColumnIndex(ServerizedConfig.COLUMN_ROW_ID))));
                cursor.moveToNext();
            }
            cursor.close();
        } catch (SQLiteException e) {
            if (Objects.requireNonNull(e.getMessage()).startsWith("no such table:")) {
                createDownloadQueueDB();
                return getDownloadQueue();
            }
            Config.LOG(Config.TAG_DATABASE, "Download Queue Table : " + e.getMessage(), false);
            return null;
        }
        return data;
    }

    public ArrayList<String> getDistinctDates() {
        SQLiteDatabase database = this.getReadableDatabase();
        ArrayList<String> data = new ArrayList<>();
        Cursor cursor = database.rawQuery("SELECT DISTINCT strftime('%Y'," + ServerizedConfig.COLUMN_RELEASE + ") FROM " + ServerizedConfig.TABLE_NAME, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            data.add(cursor.getString(0));
            cursor.moveToNext();
        }
        cursor.close();
        return data;
    }

    public ArrayList<ServerizedTrackData> getYearSpecificTrack(String year) {
        SQLiteDatabase database = this.getReadableDatabase();
        ArrayList<ServerizedTrackData> data = new ArrayList<>();
        Cursor cursor = database.rawQuery("SELECT * FROM " + ServerizedConfig.TABLE_NAME +" WHERE strftime('%Y', " + ServerizedConfig.COLUMN_RELEASE + ") = '" + year +"'", null);
        cursor.moveToFirst();
        Config.LOG(Config.TAG_DATABASE, "Track Year Added " + year + " " + cursor.getCount(), true);
        while (!cursor.isAfterLast()) {
            ServerizedTrackData trackData = new ServerizedTrackData(
                    cursor.getInt(cursor.getColumnIndex(ServerizedConfig.COLUMN_INDEX)),
                    cursor.getString(cursor.getColumnIndex(ServerizedConfig.COLUMN_ALBUM)),
                    cursor.getString(cursor.getColumnIndex(ServerizedConfig.COLUMN_ARTIST)),
                    cursor.getInt(cursor.getColumnIndex(ServerizedConfig.COLUMN_GEDI)),
                    cursor.getString(cursor.getColumnIndex(ServerizedConfig.COLUMN_GENDER)),
                    cursor.getInt(cursor.getColumnIndex(ServerizedConfig.COLUMN_HIP_HOP)),
                    cursor.getInt(cursor.getColumnIndex(ServerizedConfig.COLUMN_JATTISM)),
                    cursor.getInt(cursor.getColumnIndex(ServerizedConfig.COLUMN_LEGEND)),
                    cursor.getString(cursor.getColumnIndex(ServerizedConfig.COLUMN_LINK)),
                    cursor.getInt(cursor.getColumnIndex(ServerizedConfig.COLUMN_LONG_DRIVE)),
                    cursor.getInt(cursor.getColumnIndex(ServerizedConfig.COLUMN_MAHFIL)),
                    cursor.getInt(cursor.getColumnIndex(ServerizedConfig.COLUMN_ORIGINAL)),
                    cursor.getInt(cursor.getColumnIndex(ServerizedConfig.COLUMN_PARENTAL)),
                    cursor.getInt(cursor.getColumnIndex(ServerizedConfig.COLUMN_PRO)),
                    cursor.getInt(cursor.getColumnIndex(ServerizedConfig.COLUMN_PARTY)),
                    cursor.getInt(cursor.getColumnIndex(ServerizedConfig.COLUMN_RAP)),
                    cursor.getString(cursor.getColumnIndex(ServerizedConfig.COLUMN_RELEASE)),
                    cursor.getInt(cursor.getColumnIndex(ServerizedConfig.COLUMN_ROMANTIC)),
                    cursor.getInt(cursor.getColumnIndex(ServerizedConfig.COLUMN_SAD)),
                    cursor.getString(cursor.getColumnIndex(ServerizedConfig.COLUMN_TITLE))
            );
            Config.LOG(Config.TAG_DATABASE, "Track Year Added " + year + " " + trackData.getTitle(), false);
            data.add(trackData);
            cursor.moveToNext();
        }
        cursor.close();
        return data;
    }

}