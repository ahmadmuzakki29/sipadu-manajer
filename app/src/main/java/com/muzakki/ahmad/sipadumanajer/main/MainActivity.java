package com.muzakki.ahmad.sipadumanajer.main;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;


public class MainActivity extends Activity implements Handler.Callback{

    InternetConnection ic = new InternetConnection(this);
    String username = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout);

        Database db = new Database(this);

        String[] result = db.getLoginCache();
        if(result==null){
            showLogin();
        }else{
            String ip = db.getIP();
            Constants.setHost(ip);

            username = result[0];
            String token = result[1];

            HashMap<String,String> param = new HashMap<>();
            param.put("check","1");
            param.put("username", username);
            param.put("token",token);

            ic.request(Constants.getHost() + "/check_token/", param,this);
        }
    }

    private void showAduan() {
        Intent i = new Intent(this,Aduan.class);
        startActivity(i);
        finish();
    }

    private void showLogin(){
        Intent i = new Intent(this,Login.class);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
        finish();
    }

    @Override
    public boolean handleMessage(Message message) {
        int resp = message.getData().getInt("response_code");
        if(resp!=200){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showLogin();
                }
            });
            return false;
        }
        try {
            JSONObject response = new JSONObject(message.getData().getString("response"));
            String  r = response.getString("response");
            if(r.equals("OK")) {
                Constants.setUsername(username);
                showAduan();
            }else{
                showLogin();
            }
        }catch(JSONException e){
            e.printStackTrace();
            Log.i("jeki", "resp" + message.getData().getString("response"));
        }
        return true;
    }

}
