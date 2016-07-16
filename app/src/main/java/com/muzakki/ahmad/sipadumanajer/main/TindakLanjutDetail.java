package com.muzakki.ahmad.sipadumanajer.main;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.widget.TextView;

import java.util.HashMap;

/**
 * Created by jeki on 6/19/15.
 */
public class TindakLanjutDetail extends ActionBarActivity {
    private String id = null;
    private Database db = new Database(this);
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tindak_lanjut_detail);
    }

    @Override
    protected void onStart() {
        super.onStart();
        id = getIntent().getStringExtra("id");
        HashMap<String, String> result = db.getOutbox(id);
        String nosambungan = result.get("nosambungan")+" - "+result.get("nama");
        ((TextView) findViewById(R.id.nosambunganTindakLanjut)).setText(nosambungan);
        ((TextView) findViewById(R.id.vwIsiAduan)).setText(result.get("aduan"));
        ((TextView) findViewById(R.id.vwTanggalAduan)).setText(result.get("waktu"));
        ((TextView) findViewById(R.id.vwTindakLanjut)).setText(result.get("tindak_lanjut"));
        ((TextView) findViewById(R.id.vwWaktuTindakLanjut)).setText(result.get("waktu_tindak_lanjut"));
        ((TextView) findViewById(R.id.vwManajer)).setText(result.get("manajer"));
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

}
