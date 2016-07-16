package com.muzakki.ahmad.sipadumanajer.main;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.NotificationCompat;

import com.muzakki.ahmad.sipadumanajer.service.MqttServiceConstants;

import org.json.JSONArray;

import java.util.HashMap;

/**
 * Created by jeki on 6/10/15.
 */
public class Receiver extends BroadcastReceiver {
    InternetConnection ic = null;
    Database db = null;
    Context context = null;

    @Override
    public void onReceive(Context context, Intent intent) {
        String payload = intent.getStringExtra("payload");
        if(this.context==null) {
            this.context = context;
            ic = new InternetConnection(context);
            db = new Database(context);
        }

        if(payload.equals("inbox") && !Constants.ISRECEIVING_INBOX){
            Constants.ISRECEIVING_INBOX = true;
            getInbox();
        }else if(payload.equals("outbox") && !Constants.ISRECEIVING_OUTBOX){
            Constants.ISRECEIVING_OUTBOX = true;
            getOutbox();
        }
    }

    public void getInbox(){
        String url = Constants.getHost()+"/sync/";
        HashMap<String,String> param = new HashMap<>();
        param.put("sync","get_inbox");
        param.put("username",Constants.getUsername());
        final String latest = db.getLatestInbox();
        
        param.put("last_msg",latest);

        Handler.Callback callback = new Handler.Callback() {
            @Override
            public boolean handleMessage(Message message) {
                receiveInbox(latest,message);
                return false;
            }
        };
        ic.request(url, param, callback);
    }

    private void receiveInbox(String latest,Message msg) {
        Constants.ISRECEIVING_INBOX = false;
        String response = msg.getData().getString("response");
        try {
            JSONArray array = new JSONArray(response);
            Constants.notif_inbox_count += array.length();

            db.insertInbox(array);

            Intent i = new Intent(MqttServiceConstants.CALLBACK_TO_ACTIVITY);
            i.putExtra(MqttServiceConstants.CALLBACK_ACTION, MqttServiceConstants.MESSAGE_ARRIVED_ACTION);
            i.putExtra("type","inbox");
            i.putExtra("latest",latest);
            context.sendBroadcast(i);

            notifyInbox();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private synchronized void notifyInbox(){
        String ns = Context.NOTIFICATION_SERVICE;
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(ns);

        Intent resultIntent = new Intent(context, Aduan.class);
        resultIntent.putExtra("ACTION",Constants.ACTION_NOTIFICATION);
        resultIntent.putExtra("type","inbox");
        PendingIntent pending = PendingIntent.getActivity(context, Constants.NOTIF_INBOX_ID,
                resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        Bitmap icon = BitmapFactory.decodeResource(context.getResources(),
                R.drawable.ic_sipadu);
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.ic_notif)
                        .setLargeIcon(icon)
                        .setNumber(Constants.notif_inbox_count)
                        .setContentTitle(Constants.notif_inbox_count + " Aduan Baru")
                        .setContentText("Tekan untuk membuka.");

        mBuilder.setAutoCancel(false);
        mBuilder.setContentIntent(pending);
        mNotificationManager.notify(Constants.NOTIF_INBOX_ID, mBuilder.build());
    }

    public void getOutbox(){
        String url = Constants.getHost()+"/sync/";
        HashMap<String,String> param = new HashMap<>();
        param.put("sync","get_outbox");
        param.put("username",Constants.getUsername());
        final String latest = db.getLatestOutbox();
        param.put("last_msg", latest);

        Handler.Callback callback = new Handler.Callback() {
            @Override
            public boolean handleMessage(Message message) {
                receiveOutbox(latest,message);
                return false;
            }
        };
        ic.request(url, param, callback);
    }

    private void receiveOutbox(String latest,Message msg) {
        Constants.ISRECEIVING_OUTBOX = false;
        String response = msg.getData().getString("response");
        try {
            JSONArray array = new JSONArray(response);
            Constants.notif_outbox_count += array.length();

            db.insertOutbox(array);

            Intent i = new Intent(MqttServiceConstants.CALLBACK_TO_ACTIVITY);
            i.putExtra(MqttServiceConstants.CALLBACK_ACTION, MqttServiceConstants.MESSAGE_ARRIVED_ACTION);
            i.putExtra("type","outbox");
            i.putExtra("latest",latest);
            context.sendBroadcast(i);

            notifyOutbox();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private synchronized void notifyOutbox(){
        String ns = Context.NOTIFICATION_SERVICE;
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(ns);

        Intent resultIntent = new Intent(context, Aduan.class);
        resultIntent.putExtra("ACTION",Constants.ACTION_NOTIFICATION);
        resultIntent.putExtra("type","outbox");
        PendingIntent pending = PendingIntent.getActivity(context, Constants.NOTIF_OUTBOX_ID,
                resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        Bitmap icon = BitmapFactory.decodeResource(context.getResources(),
                R.drawable.ic_sipadu);
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.ic_notif_check)
                        .setLargeIcon(icon)
                        .setNumber(Constants.notif_outbox_count)
                        .setContentTitle(Constants.notif_outbox_count+ " Tindak Lanjut Baru")
                        .setContentText("Tekan untuk membuka.");
        mBuilder.setContentIntent(pending);
        mNotificationManager.notify(Constants.NOTIF_OUTBOX_ID, mBuilder.build());
    }
}
