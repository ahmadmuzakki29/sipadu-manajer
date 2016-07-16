package com.muzakki.ahmad.sipadumanajer.main;

import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.muzakki.ahmad.sipadumanajer.service.ActionListener;
import com.muzakki.ahmad.sipadumanajer.service.MqttClient;

import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by jeki on 5/31/15.
 */
public class Aduan extends ActionBarActivity implements ActionBar.TabListener{


    Database db = new Database(this);
    MqttClient client = null;
    InternetConnection ic = new InternetConnection(this);

    private ViewPager viewPager;
    private FragmentAdapter adapterFrg;
    private ActionBar actionBar;
    private Handler.Callback syncAll = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            syncAllHandler(message);
            return true;
        }
    };

    private String[] tabs = { "Inbox", "Outbox"};
    private static final int TINDAK_LANJUT = 1;
    private static final int TINDAK_LANJUT_DETAIL = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.aduan);

        String[] result = db.getLoginCache();
        if(result==null){
            showLogin();
        }else {
            initView();
            connectAction();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        sync();
    }

    @Override
    protected void onResume() {
        super.onResume();

        Intent i = getIntent();
        String action = i.getStringExtra("ACTION");
        if(action!=null && action.equals(Constants.ACTION_NOTIFICATION)) {
            i.removeExtra("ACTION");
            i.removeExtra("type");
        }
    }

    @Override
    protected void onNewIntent(Intent i) {
        super.onNewIntent(i);
        String action = i.getStringExtra("ACTION");

        if(action!=null && action.equals(Constants.ACTION_NOTIFICATION)) {
            String type = i.getStringExtra("type");
            if(type.equals("inbox")){
                ((NotificationManager)
                        getSystemService(Context.NOTIFICATION_SERVICE))
                        .cancel(Constants.NOTIF_INBOX_ID);
                Constants.notif_inbox_count = 0;
                viewPager.setCurrentItem(0);
            }else{
                ((NotificationManager)
                        getSystemService(Context.NOTIFICATION_SERVICE))
                        .cancel(Constants.NOTIF_OUTBOX_ID);
                Constants.notif_outbox_count = 0;
                viewPager.setCurrentItem(1);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i("jeki", "onpause");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i("jeki", "ondestroy");
        ((NotificationManager)
                getSystemService(Context.NOTIFICATION_SERVICE)).cancelAll();
        Constants.notif_inbox_count = 0;
        Constants.notif_outbox_count = 0;
        client.unregisterResources();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.aduan_action, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_logout:
                dialogLogout();
                return true;
            case R.id.menu_map:
                showMap();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void initView(){
        viewPager = (ViewPager) findViewById(R.id.pager);

        actionBar = getSupportActionBar();
        viewPager.setOnPageChangeListener(
                new ViewPager.SimpleOnPageChangeListener() {
                    @Override
                    public void onPageSelected(int position) {
                        actionBar.setSelectedNavigationItem(position);
                    }
                });
        adapterFrg = new FragmentAdapter(getSupportFragmentManager());
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setIcon(R.drawable.ic_sipadu);

        viewPager.setAdapter(adapterFrg);
        actionBar.setHomeButtonEnabled(false);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // Adding Tabs
        for (String tab_name : tabs) {
            actionBar.addTab(actionBar.newTab().setText(tab_name)
                    .setTabListener(this));
        }

    }

    public void sync(){
        String result = db.getLatestInbox();
        if(result==null) {
            if (!ic.isConnected()) return;
            String url = Constants.getHost() + "/sync/";
            HashMap<String, String> param = new HashMap<>();
            param.put("sync", "all");
            ic.request(url, param, syncAll);
        }
    }

    private void syncAllHandler(Message msg) {
        int resp = msg.getData().getInt("response_code");

        if(resp!=200){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.i("jeki", "Sync error");
                }
            });return;
        }

        try{
            JSONObject response = new JSONObject(msg.getData().getString("response"));
            JSONArray inboxData = new JSONArray(response.getString("inbox"));
            JSONArray outboxData = new JSONArray(response.getString("outbox"));

            db.insertInbox(inboxData);
            db.insertOutbox(outboxData);

            ((ListUpdater) adapterFrg.getFragment(0)).showList();
            ((ListUpdater) adapterFrg.getFragment(1)).showList();

        }catch (JSONException e){
            e.printStackTrace();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void connectAction() {

        MqttConnectOptions conOpt = new MqttConnectOptions();

        String clientId = Constants.getUsername();
        String uri = "tcp://"+Constants.getIp()+":1883";
        client = new MqttClient(this, uri, clientId,Constants.getUsername());

        String clientHandle = uri + clientId;

        // connection options
        String[] actionArgs = new String[1];
        actionArgs[0] = clientId;

        conOpt.setCleanSession(true);
        conOpt.setConnectionTimeout(Constants.timeout);
        conOpt.setKeepAliveInterval(Constants.keepalive);
        conOpt.setUserName(Constants.mqtt_username);
        conOpt.setPassword(Constants.mqtt_password.toCharArray());

        client.setCallback(new MqttCallbackHandler(this, clientHandle));
        final ActionListener callback = new ActionListener(this,
                ActionListener.Action.CONNECT, clientHandle, actionArgs);
        try {
            client.connect(conOpt, null, callback);
        }
        catch (MqttException e) {
            Log.e(this.getClass().getCanonicalName(),
                    "MqttException Occured", e);
        }

    }

    public void requestNotif(){
        String url = Constants.getHost()+"/notification/";
        HashMap<String,String> param = new HashMap<>();
        param.put("action", "get_notif");
        param.put("username", Constants.getUsername());

        ic.request(url, param, null);
    }

    private void dialogLogout(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Anda yakin untuk keluar?");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                logout();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Log.i("jeki", "cancel");
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void logout(){
        NotificationManager nMgr = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        nMgr.cancelAll();
        db.clearCache();
        try{
            client.disconnect();
        }catch(MqttException e){}
    }

    public void showLogin(){
        Intent i = new Intent(this,Login.class);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
        finish();
    }

    public MqttClient getClient() {
        return client;
    }

    public void updateInbox(String latest){
        ((ListUpdater) adapterFrg.getFragment(0)).addItemList(db.getArrivedInbox(latest));
    }

    public void updateOutbox(String latest){
        List<HashMap<String, String>> result = db.getArrivedOutbox(latest);
        ListUpdater inbox = (ListUpdater) adapterFrg.getFragment(0);
        ListUpdater outbox = (ListUpdater) adapterFrg.getFragment(1);
        outbox.addItemList(result);

        String[] ids = new String[result.size()];
        int i=0;
        for(HashMap<String, String> rw : result){
            ids[i++] = rw.get("id");
        }
        inbox.removeItemList(ids);
    }

    private void showMap(){
        Intent i = new Intent(this,MapView.class);
        startActivity(i);
    }

    public void showTindakLanjut(String id){
        Intent intent = new Intent(this,TindakLanjut.class);
        intent.putExtra("id", id);
        startActivityForResult(intent, TINDAK_LANJUT);
    }

    public void showTindakLanjutDetail(String id) {
        Intent intent = new Intent(this,TindakLanjutDetail.class);
        intent.putExtra("id", id);
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==TINDAK_LANJUT && resultCode==RESULT_OK){
            String id = data.getStringExtra("id");
            ListUpdater inbox = (ListUpdater) adapterFrg.getFragment(0);
            ListUpdater outbox = (ListUpdater) adapterFrg.getFragment(1);

            ArrayList<HashMap<String, String>> ls = new ArrayList<>();
            ls.add(db.getOutbox(id));

            outbox.addItemList(ls);
            inbox.removeItemList(new String[]{id});
        }
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        viewPager.setCurrentItem(tab.getPosition());
        NotificationManager nm = ((NotificationManager)
                getSystemService(Context.NOTIFICATION_SERVICE));
        if(tab.getPosition()==0) {
            nm.cancel(Constants.NOTIF_INBOX_ID);
            Constants.notif_inbox_count = 0;
        }else{
            nm.cancel(Constants.NOTIF_OUTBOX_ID);
            Constants.notif_outbox_count = 0;
        }
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

    }


}
