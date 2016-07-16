package com.muzakki.ahmad.sipadumanajer.main;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by jeki on 5/31/15.
 */
public class Database extends SQLiteOpenHelper{
    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 5;
    public static final String DATABASE_NAME = "sipadu.db";

    private static final String[] TABLES = new String[]{"login","inbox","outbox","host"};
    private static final String[] SQL_CREATE_ENTRIES = new String[]{
            "CREATE TABLE login (id INTEGER,username text,token text)",
            "CREATE TABLE inbox (id INTEGER,nosambungan text,nama text," +
                    "aduan text,waktu text,kategori text,lat text,long text)",
            "CREATE TABLE outbox (id INTEGER PRIMARY KEY,nosambungan text,nama text," +
                    "aduan text,waktu text,tindak_lanjut text, " +
                    "waktu_tindak_lanjut text,kategori text,manajer text,lat text,long text)",
            "create table host(ip text)"
    };

    public Database(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        for(String entry : SQL_CREATE_ENTRIES){
            db.execSQL(entry);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        for(String tb: TABLES){
            String delete = "DROP TABLE IF EXISTS "+tb;
            db.execSQL(delete);
        }
        onCreate(db);
    }

    private List<HashMap<String,String>> getResult(Cursor c){
        List<HashMap<String,String>> result = new ArrayList<>();
        if(c.getCount()>0) {
            while (c.moveToNext()) {
                HashMap<String, String> row = new HashMap<>();
                for(int col=0;col<c.getColumnCount();col++){
                    row.put(c.getColumnName(col),c.getString(col));
                }
                result.add(row);
            }
        }
        return result;
    }



    /** LOGIN **/
    public String getIP(){
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("select ip from host",null);

        try{
            if(c.moveToFirst()) {
                return c.getString(0);
            }else {
                return null;
            }
        }finally{
            db.close();
            c.close();
        }
    }

    public String[] getLoginCache(){
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("select username,token from login",null);

        try{
            if(c.moveToFirst()) {
                return new String[]{c.getString(0),c.getString(1)};
            }else {
                return null;
            }
        }finally{
            db.close();
            c.close();
        }
    }

    public void clearCache(){
        SQLiteDatabase db = getReadableDatabase();
        onUpgrade(db, 0, 0);
    }

    public void saveIP(String ip){
        SQLiteDatabase db = getReadableDatabase();
        db.execSQL("delete from host");
        db.execSQL("insert into host(ip) values(?)",new String[]{ip});
        db.close();
    }

    public void saveLoginCache(String username,String token){
        SQLiteDatabase db = getReadableDatabase();
        db.execSQL("insert into login(username,token) values(?,?)",new String[]{username,token});
        db.close();
    }



    public List<HashMap<String,String>> getInbox(){
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("select id,nosambungan,aduan,strftime('%d/%m/%Y',waktu) as waktu " +
                "from inbox order by datetime(waktu) desc",
                null);

        try{
            return getResult(c);
        }finally {
            db.close();
            c.close();
        }
    }

    public List<HashMap<String,String>> getOutbox(){
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("select id,aduan,strftime('%d/%m/%Y',waktu) as waktu," +
                        "tindak_lanjut, strftime('%d/%m/%Y',waktu_tindak_lanjut) as waktu_tindak_lanjut " +
                        "from outbox order by datetime(waktu_tindak_lanjut) desc",
                null);

        try{
            return getResult(c);
        }finally {
            db.close();
            c.close();
        }
    }


    public int insertInbox(JSONArray json_array)throws JSONException{ /* saat pertama kali login */
        if(json_array.length()==0) return 0;
        String[] column = new String[]{"id","nosambungan","nama","aduan","waktu",
                "kategori","lat","long"};
        String[] data = new String[json_array.length()*column.length];
        int a = 0;
        String field = "";
        String values = "";
        for(int i=0;i<json_array.length();i++){
            JSONObject obj = json_array.getJSONObject(i);
            values += "(";
            for(String col : column){
                if(i==0) field += col+",";
                String value = obj.getString(col);
                data[a++] = value.equals("None")? null:value;
                values += "?,";
            }
            values = values.substring(0,values.length()-1);
            values += "),";
        }
        values = values.substring(0,values.length()-1);
        field = field.substring(0,field.length()-1);
        String query = "insert into inbox("+field+") values"+values;
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL(query,data);
        db.close();
        return json_array.length();
    }

    public int insertOutbox(JSONArray json_array)throws JSONException{
        if(json_array.length()==0) return 0;
        String[] column = new String[]{"id","nosambungan","nama","aduan","waktu",
                "kategori","tindak_lanjut","waktu_tindak_lanjut","manajer","lat","long"};
        String[] data = new String[json_array.length()*column.length];
        int a = 0;
        String field = "";
        String values = "";
        String ids = "";
        for(int i=0;i<json_array.length();i++){
            JSONObject obj = json_array.getJSONObject(i);
            values += "(";
            for(String col : column){
                if(i==0) field += col+",";
                String value = obj.getString(col);
                if(col.equals("id")){
                    ids += value+",";
                }
                data[a++] = value.equals("None")? null:value;
                values += "?,";
            }
            values = values.substring(0,values.length()-1);
            values += "),";
        }
        values = values.substring(0,values.length()-1);
        ids = ids.substring(0,ids.length()-1);

        field = field.substring(0,field.length()-1);
        String query_delete_outbox = "delete from outbox where id in (?)";
        String query = "insert into outbox("+field+") values"+values;
        String query_delete_inbox = "delete from inbox where id in (?)";
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL(query_delete_outbox,new String[]{ids});
        db.execSQL(query,data);
        db.execSQL(query_delete_inbox,new String[]{ids});
        db.close();
        return json_array.length();
    }


    public String getLatestInbox(){
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("select waktu from inbox order by datetime(waktu) desc limit 1", null);

        try{
            List<HashMap<String, String>> result = getResult(c);
            return !result.isEmpty()? result.get(0).get("waktu"):"0";
        }finally {
            db.close();
            c.close();
        }
    }

    public String getLatestOutbox(){
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("select waktu from outbox order by datetime(waktu) desc limit 1", null);

        try{
            List<HashMap<String, String>> result = getResult(c);
            return !result.isEmpty()? result.get(0).get("waktu"):"0";
        }finally {
            db.close();
            c.close();
        }
    }


    public List<HashMap<String,String>> getArrivedInbox(String latest){
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("select id,nosambungan,aduan,strftime('%d/%m/%Y',waktu) as waktu " +
                        "from inbox where waktu > ? order by datetime(waktu) desc",
                new String[]{latest});
        try{
            return getResult(c);
        }finally {
            db.close();
            c.close();
        }
    }

    public List<HashMap<String,String>> getArrivedOutbox(String latest){
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("select id,aduan,strftime('%d/%m/%Y',waktu) as waktu,tindak_lanjut, " +
                        "strftime('%d/%m/%Y',waktu_tindak_lanjut) as waktu_tindak_lanjut " +
                        "from outbox where waktu > ? order by datetime(waktu) desc",
                new String[]{latest});
        try{
            return getResult(c);
        }finally {
            db.close();
            c.close();
        }
    }


    public HashMap<String,String> getInbox(String id){
        SQLiteDatabase db = getReadableDatabase();
        String query = "select nama,nosambungan,aduan,strftime('%d/%m/%Y',waktu) as waktu,kategori " +
                "from inbox where id=?";
        Cursor c = db.rawQuery(query, new String[]{id});

        try{
            List<HashMap<String, String>> result = getResult(c);
            return result.get(0);
        }finally {
            db.close();
            c.close();
        }

    }

    public HashMap<String,String> getInboxRaw(String id){
        SQLiteDatabase db = getReadableDatabase();
        String query = "select nama,nosambungan,aduan,waktu,kategori,lat,long " +
                "from inbox where id=?";
        Cursor c = db.rawQuery(query, new String[]{id});

        try{
            List<HashMap<String, String>> result = getResult(c);
            return result.get(0);
        }finally {
            db.close();
            c.close();
        }

    }

    public HashMap<String,String> getOutbox(String id){
        SQLiteDatabase db = getReadableDatabase();
        String query = "select nama,nosambungan,aduan,strftime('%d/%m/%Y',waktu) as waktu,kategori," +
                "tindak_lanjut,strftime('%d/%m/%Y',waktu_tindak_lanjut) as waktu_tindak_lanjut,manajer " +
                "from outbox where id=?";
        Cursor c = db.rawQuery(query, new String[]{id});

        try{
            List<HashMap<String, String>> result = getResult(c);
            return result.get(0);
        }finally {
            db.close();
            c.close();
        }

    }

    public List<HashMap<String,String>> getLatLng(){
        SQLiteDatabase db = getReadableDatabase();
        String query = "select nama,nosambungan,aduan,kategori,lat,long from inbox " +
                " union " +
                "select nama,nosambungan,aduan,kategori,lat,long from outbox";

        Cursor c = db.rawQuery(query, null);
        try{
            return getResult(c);
        }finally {
            db.close();
            c.close();
        }
    }


}
