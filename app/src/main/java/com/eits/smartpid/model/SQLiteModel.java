package com.eits.smartpid.model;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import com.eits.smartpid.CameraActivity;

import java.util.ArrayList;

public class SQLiteModel extends SQLiteOpenHelper {
    public static final String DATABASE_NAME="SMART_PID_Sqlite";
    public static final int DATABASE_VERSION=1;


    public SQLiteModel(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }



    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String facility_table="create table Facility_TABLE(Fac_ID int primary key,Fac_Name text)";
        String component_table="create table Component_TABLE(Comp_ID int primary key,Comp_Name text)";

        sqLiteDatabase.execSQL(facility_table);
        sqLiteDatabase.execSQL(component_table);

        sqLiteDatabase.execSQL("insert into Facility_TABLE values(100,'Select Facility')");
        sqLiteDatabase.execSQL("insert into Facility_TABLE values(101,'First')");
        sqLiteDatabase.execSQL("insert into Facility_TABLE values(102,'Second')");
        sqLiteDatabase.execSQL("insert into Facility_TABLE values(103,'Third')");
        sqLiteDatabase.execSQL("insert into Facility_TABLE values(104,'Fourth')");
        sqLiteDatabase.execSQL("insert into Facility_TABLE values(105,'Fifth')");

        sqLiteDatabase.execSQL("insert into Component_TABLE values(200,'Select Component')");
        sqLiteDatabase.execSQL("insert into Component_TABLE values(201,'Flange')");
        sqLiteDatabase.execSQL("insert into Component_TABLE values(202,'Valve')");
        sqLiteDatabase.execSQL("insert into Component_TABLE values(203,'Valve-Check')");
        sqLiteDatabase.execSQL("insert into Component_TABLE values(204,'Pump')");
        sqLiteDatabase.execSQL("insert into Component_TABLE values(205,'Compressor')");
        sqLiteDatabase.execSQL("insert into Component_TABLE values(206,'Valve-T')");


    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    public ArrayList<ComponentModel> getComponentList() {
        ArrayList<ComponentModel>arrayList=new ArrayList<>();

        SQLiteDatabase sqLiteDatabase=this.getReadableDatabase();
        Cursor cursor = sqLiteDatabase.rawQuery("select * from Component_TABLE", null);
        if (cursor.moveToFirst()) {
            do {
                int compID = cursor.getInt(0);
                String compName = cursor.getString(1);

                arrayList.add(new ComponentModel( compID, compName));
            } while (cursor.moveToNext());
        } else {

        }
        return arrayList;
    }

    public ArrayList<FacilityModel> getFacilityList() {
        ArrayList<FacilityModel>arrayList=new ArrayList<>();

        SQLiteDatabase sqLiteDatabase=this.getReadableDatabase();
        Cursor cursor = sqLiteDatabase.rawQuery("select * from Facility_TABLE", null);
        if (cursor.moveToFirst()) {
            do {
                int facID = cursor.getInt(0);
                String facName = cursor.getString(1);

                arrayList.add(new FacilityModel(facID, facName));
            } while (cursor.moveToNext());
        } else {

        }
        return arrayList;
    }

}
