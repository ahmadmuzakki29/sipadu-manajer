package com.muzakki.ahmad.sipadumanajer.main;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.HashMap;
import java.util.List;

/**
 * Created by jeki on 6/21/15.
 */
public class MapView extends ActionBarActivity implements OnMapReadyCallback{
    Database db = new Database(this);
    static final CameraPosition SIDOARJO =
            new CameraPosition.Builder().target(new LatLng(-7.4552489, 112.702071))
                    .zoom(11)
                    .build();
    private List<HashMap<String, String>> result;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_view);
        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        result = db.getLatLng();
    }

    @Override
    public void onMapReady(GoogleMap map) {
        map.moveCamera(CameraUpdateFactory.newCameraPosition(SIDOARJO));

        for(HashMap<String,String> rw : result){
            MarkerOptions opt = new MarkerOptions();
            Log.i("jeki", rw.get("lat"));
            if(rw.get("lat").equals("0")) continue;

            opt.position(new LatLng(Double.parseDouble(rw.get("lat")),
                    Double.parseDouble(rw.get("long"))));
            if(rw.get("kategori").equals("Mampet")){
                opt.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
            }else if(rw.get("kategori").equals("Bocor")){
                opt.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
            }else{
                opt.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
            }

            opt.title(rw.get("nosambungan")+" - "+rw.get("nama"));
            opt.snippet(rw.get("aduan"));
            map.addMarker(opt);
        }
    }
}
