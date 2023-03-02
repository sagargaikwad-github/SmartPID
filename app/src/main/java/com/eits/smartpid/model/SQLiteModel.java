package com.eits.smartpid.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import java.util.ArrayList;

public class SQLiteModel extends SQLiteOpenHelper {
    public static final String DATABASE_NAME="SMART_PID_Sqlite";
    public static final int DATABASE_VERSION=1;


    public SQLiteModel(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }



    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String facility_table="create table Facility_TABLE(Fac_ID int primary key,Fac_Name text,Fac_Filter int)";
        String component_table="create table Component_TABLE(Comp_ID int primary key,Comp_Name text,Comp_Filter int)";

        sqLiteDatabase.execSQL(facility_table);
        sqLiteDatabase.execSQL(component_table);

        sqLiteDatabase.execSQL("insert into Facility_TABLE values(100,'Select Facility',0)");
        sqLiteDatabase.execSQL("insert into Facility_TABLE values(101,'First',0)");
        sqLiteDatabase.execSQL("insert into Facility_TABLE values(102,'Second',0)");
        sqLiteDatabase.execSQL("insert into Facility_TABLE values(103,'Third',0)");
        sqLiteDatabase.execSQL("insert into Facility_TABLE values(104,'Fourth',0)");
        sqLiteDatabase.execSQL("insert into Facility_TABLE values(105,'Fifth',0)");

        sqLiteDatabase.execSQL("insert into Component_TABLE values(200,'Select Component',0)");
        sqLiteDatabase.execSQL("insert into Component_TABLE values(201,'Flange',0)");
        sqLiteDatabase.execSQL("insert into Component_TABLE values(202,'Valve',0)");
        sqLiteDatabase.execSQL("insert into Component_TABLE values(203,'Valve-Check',0)");
        sqLiteDatabase.execSQL("insert into Component_TABLE values(204,'Pump',0)");
        sqLiteDatabase.execSQL("insert into Component_TABLE values(205,'Compressor',0)");
        sqLiteDatabase.execSQL("insert into Component_TABLE values(206,'Valve-T',0)");


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
                int compFilter = cursor.getInt(2);

                arrayList.add(new ComponentModel( compID, compName,compFilter));
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
                int facFilter = cursor.getInt(2);

                arrayList.add(new FacilityModel(facID, facName,facFilter));
            } while (cursor.moveToNext());
        } else {

        }
        return arrayList;
    }

    public ArrayList<String> getFacilityNames() {
        ArrayList<String>arrayList=new ArrayList<>();

        SQLiteDatabase sqLiteDatabase=this.getReadableDatabase();
        Cursor cursor = sqLiteDatabase.rawQuery("select * from Facility_TABLE", null);
        if (cursor.moveToFirst()) {
            do {
                if(cursor.isFirst())
                {

                }else
                {
                    String facName = cursor.getString(1);
                    arrayList.add(facName);
                }


            } while (cursor.moveToNext());
        } else {

        }
        return arrayList;

    }

    public ArrayList<String> getComponentNames() {
        ArrayList<String>arrayList=new ArrayList<>();

        SQLiteDatabase sqLiteDatabase=this.getReadableDatabase();
        Cursor cursor = sqLiteDatabase.rawQuery("select * from Component_TABLE", null);
        if (cursor.moveToFirst()) {
            do {
               if(cursor.isFirst())
               {

               }else
               {
                   String compName = cursor.getString(1);
                   arrayList.add(compName);
               }
            } while (cursor.moveToNext());
        } else {

        }
        return arrayList;
    }


    public String getComponentName(int compID) {

        String compName=null;

        SQLiteDatabase sqLiteDatabase=this.getReadableDatabase();
        Cursor cursor = sqLiteDatabase.rawQuery("select Comp_Name from Component_TABLE where Comp_ID=?", new String[]{String.valueOf(compID)});
        if (cursor.moveToFirst()) {
            do {
                   compName = cursor.getString(0);
            } while (cursor.moveToNext());
        } else {

        }
        return compName;
    }

    public String getFacilityName(int facID) {

        String facName=null;

        SQLiteDatabase sqLiteDatabase=this.getReadableDatabase();
        Cursor cursor = sqLiteDatabase.rawQuery("select Fac_Name from Facility_TABLE where Fac_ID=?", new String[]{String.valueOf(facID)});
        if (cursor.moveToFirst()) {
            do {
                 facName = cursor.getString(0);
            } while (cursor.moveToNext());
        } else {

        }
        return facName;
    }


    public ArrayList<Integer> getCompFilterList() {
        ArrayList<Integer> getList = new ArrayList<>();
        SQLiteDatabase sqLiteDatabase = this.getReadableDatabase();
        String qry = "select * from Component_TABLE where Comp_Filter=1";
        Cursor cursor = sqLiteDatabase.rawQuery(qry, null);

        if (cursor.moveToFirst()) {
            do {
                int compID = cursor.getInt(0);
                getList.add(compID);
            } while (cursor.moveToNext());
        } else {

        }
        return getList;
    }

    public ArrayList<Integer> getFacFilterList() {
        ArrayList<Integer> getList = new ArrayList<>();
        SQLiteDatabase sqLiteDatabase = this.getReadableDatabase();
        String qry = "select * from Facility_TABLE where Fac_Filter=1";
        Cursor cursor = sqLiteDatabase.rawQuery(qry, null);

        if (cursor.moveToFirst()) {
            do {
                int facID = cursor.getInt(0);
                getList.add(facID);
            } while (cursor.moveToNext());
        } else {

        }
        return getList;
    }

    public void updateCompFilter(Integer integer) {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("Comp_Filter", 1);
        sqLiteDatabase.update("Component_TABLE",cv,"Comp_ID=?",new String[]{String.valueOf(integer)});
    }
    public void updateFacFilter(Integer integer) {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("Fac_Filter", 1);
        sqLiteDatabase.update("Facility_TABLE",cv,"Fac_ID=?",new String[]{String.valueOf(integer)});
    }
    public void clearCompFilter() {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("Comp_Filter", 0);
        sqLiteDatabase.update("Component_TABLE",cv,null,null);
    }
    public void clearFacFilter() {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("Fac_Filter", 0);
        sqLiteDatabase.update("Facility_TABLE",cv,null,null);
    }
}
