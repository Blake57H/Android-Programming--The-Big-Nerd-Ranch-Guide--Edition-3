package com.example.androidprogrammingbook02;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.androidprogrammingbook02.database.CrimeBaseHelper;
import com.example.androidprogrammingbook02.database.CrimeCursorWrapper;
import com.example.androidprogrammingbook02.database.CrimeDBSchema;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CrimeLab {
    private static CrimeLab sCrimeLab;
    private Context mContext;
    private SQLiteDatabase mDatabase;

    private static final String TAG_LOG = "log";

    public static CrimeLab get(Context context) {
        if (sCrimeLab == null) {
            sCrimeLab = new CrimeLab(context);
        }
        return sCrimeLab;
    }

    private CrimeLab(Context context) {
        mContext = context.getApplicationContext();
        mDatabase = new CrimeBaseHelper(mContext).getWritableDatabase();

/*
        for (int i = 0; i < 100; i++) {
            Crime crime = new Crime();
            crime.setTitle("Crime #" + i);
            crime.setSolved(i % 2 == 0);
            crime.setSeriousness(i % 10 == 1 ? 1 : 0);
            mCrimes.add(crime);
        }
*/
    }

    public void addCrime(Crime crime) {
        ContentValues contentValues = getContentValues(crime);
        mDatabase.insert(CrimeDBSchema.CrimeTable.NAME, null, contentValues);
    }

    public void updateCrime(Crime crime) {
        String uuidString = crime.getID().toString();
        ContentValues values = getContentValues(crime);

        mDatabase.update(CrimeDBSchema.CrimeTable.NAME, values,
                CrimeDBSchema.CrimeTable.Columns.UUID + "= ?",
                new String[]{uuidString});
    }

    private CrimeCursorWrapper queryCrimes(String whereClause, String[] whereArgs) {
        Cursor cursor = mDatabase.query(CrimeDBSchema.CrimeTable.NAME, null, whereClause, whereArgs, null, null, null);
        return new CrimeCursorWrapper(cursor);
    }

    public void deleteCrimeByIndex(int index) {
        List<Crime> crimes = getCrimes();
        deleteCrimeByID(crimes.get(index).getID());
    }

    public void deleteCrimeByID(UUID id) {
        mDatabase.delete(CrimeDBSchema.CrimeTable.NAME,
                CrimeDBSchema.CrimeTable.Columns.UUID + " = ?",
                new String[]{id.toString()});
    }

    public List<Crime> getCrimes() {
        List<Crime> crimes = new ArrayList<>();

        CrimeCursorWrapper cursor = queryCrimes(null, null);

        try {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                crimes.add(cursor.getCrime());
                cursor.moveToNext();
            }
        } finally {
            cursor.close();
        }

        return crimes;
    }

    public Crime getCrimeByID(UUID id) {
        CrimeCursorWrapper cursorWrapper = queryCrimes(
                CrimeDBSchema.CrimeTable.Columns.UUID + " = ?",
                new String[]{id.toString()}
        );

        try {
            if (cursorWrapper.getCount() == 0) return null;

            cursorWrapper.moveToFirst();
            return cursorWrapper.getCrime();
        } finally {
            cursorWrapper.close();
        }
    }

    public int getIndexByID(UUID uuid) {
        List<Crime> crimes = getCrimes();
        int loop = 0;
        while (loop < crimes.size()) {
            //Log.d(TAG_LOG, "id = "+uuid.toString() + " && crime = " + crimes.get(loop).getID().toString());
            //Log.d(TAG_LOG, "uuid.compareTo(crimes.get(loop).getID() = " + uuid.compareTo(crimes.get(loop).getID()));
            if (uuid.compareTo(crimes.get(loop).getID()) == 0) return loop;
            loop++;
        }
        return -1;
    }

    public Crime getCrimeByIndex(int index) {
        List<Crime> crimes = getCrimes();
        if (crimes.size() == 0 || crimes.size() < index) return null;
        return crimes.get(index);
    }

    public File getPhotoFile(Crime crime){
        File fileDir = mContext.getFilesDir();
        return new File(fileDir, crime.getPhotoFilename());
    }

    private static ContentValues getContentValues(Crime crime) {
        ContentValues values = new ContentValues();
        values.put(CrimeDBSchema.CrimeTable.Columns.UUID, crime.getID().toString());
        values.put(CrimeDBSchema.CrimeTable.Columns.TITLE, crime.getTitle());
        values.put(CrimeDBSchema.CrimeTable.Columns.DATE, crime.getDate().getTime());
        values.put(CrimeDBSchema.CrimeTable.Columns.SOLVED, crime.isSolved() ? 1 : 0);
        values.put(CrimeDBSchema.CrimeTable.Columns.SUSPECT, crime.getSuspect());
        return values;
    }



}
