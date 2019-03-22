package com.xsrt.camerademo.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.xsrt.camerademo.bean.Student;

public class DBHelper extends SQLiteOpenHelper {
    public static final String CREATE_DB = "create table db_student( " +
            "id integer primary key," +
            "name varchar(20)," +
            "gender varchar(10)," +
            "age integer)";

    public DBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d("chufei", "db oncreat");
        db.execSQL(CREATE_DB);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public void insert(Student student) {
        Log.d("chufei", "db insert");
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("insert into db_student(name,gender,age) values(?,?,?)",
                new Object[]{student.getName(), student.getGender(), student.getAge()});
        db.close();
    }

    public Cursor update() {
        SQLiteDatabase DB = getReadableDatabase();
        return DB.rawQuery("", new String[]{});
    }
}
