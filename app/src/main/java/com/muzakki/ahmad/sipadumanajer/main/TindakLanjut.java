package com.muzakki.ahmad.sipadumanajer.main;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * Created by jeki on 6/19/15.
 */
public class TindakLanjut extends ActionBarActivity {
    private Database db = new Database(this);
    private InternetConnection ic = new InternetConnection(this);
    private String id = null;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tindak_lanjut);

    }

    @Override
    protected void onStart() {
        super.onStart();

        id = getIntent().getStringExtra("id");
        HashMap<String, String> result = db.getInbox(id);
        String nosambungan = result.get("nosambungan")+" - "+result.get("nama");
        ((TextView) findViewById(R.id.nosambunganTindakLanjut)).setText(nosambungan);
        ((TextView) findViewById(R.id.vwIsiAduan)).setText(result.get("aduan"));
        ((TextView) findViewById(R.id.vwTanggalAduan)).setText(result.get("waktu"));
    }

    public void simpanTidakLanjut(View v){
        findViewById(R.id.btnSimpan).setVisibility(View.GONE);
        findViewById(R.id.loading).setVisibility(View.VISIBLE);
        String url = Constants.getHost()+"/tindak_lanjut/";
        final String tindak_lanjut = ((TextView)
                findViewById(R.id.txtTindakLanjut)).getText().toString();
        HashMap<String,String> param = new HashMap<>();
        param.put("action","simpan");
        param.put("id",id);
        param.put("tindak_lanjut", tindak_lanjut);
        param.put("username", Constants.getUsername());

        Handler.Callback cb = new Handler.Callback() {
            @Override
            public boolean handleMessage(Message message) {
                handler(message,tindak_lanjut);
                return true;
            }
        };

        ic.request(url, param, cb);
    }

    private void handler(Message msg,String tindak_lanjut){
        String response = msg.getData().getString("response");
        Intent output = new Intent();
        output.putExtra("id", id);
        try{
            JSONObject obj = new JSONObject(response);
            String waktu_tindak_lanjut = obj.getString("waktu_tindak_lanjut");
            HashMap<String, String> in = db.getInboxRaw(id);

            obj.put("id",id);
            obj.put("nosambungan",in.get("nosambungan"));
            obj.put("nama",in.get("nama"));
            obj.put("aduan",in.get("aduan"));
            obj.put("waktu",in.get("waktu"));
            obj.put("kategori",in.get("kategori"));
            obj.put("lat",in.get("lat"));
            obj.put("long",in.get("long"));
            obj.put("tindak_lanjut",tindak_lanjut);
            obj.put("waktu_tindak_lanjut", waktu_tindak_lanjut);
            obj.put("manajer", Constants.getUsername());

            JSONArray array = new JSONArray();
            array.put(obj);

            db.insertOutbox(array);

            setResult(RESULT_OK, output);
            finish();
        }catch(Exception e){
            e.printStackTrace();
            setResult(RESULT_CANCELED, output);
            finish();
        }
    }
}
