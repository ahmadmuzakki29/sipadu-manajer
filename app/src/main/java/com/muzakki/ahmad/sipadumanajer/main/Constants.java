package com.muzakki.ahmad.sipadumanajer.main;



/**
 * Created by jeki on 6/1/15.
 */
public class Constants {
    private final static String PROTOCOL = "http://";
    private final static String PORT = ":89";

    private static String ip = "10.0.2.2";
    private static String host = PROTOCOL+ip+PORT;
    private static String username = "";


    static int timeout = 60;
    static int keepalive = 200;
    static String mqtt_username = "user";
    static String mqtt_password = "rahasia";

    static boolean ISRECEIVING_INBOX = false;
    static boolean ISRECEIVING_OUTBOX = false;

    final static int NOTIF_INBOX_ID = 1;
    final static int NOTIF_OUTBOX_ID = 2;

    static int notif_inbox_count = 0;
    static int notif_outbox_count = 0;

    static String ACTION_NOTIFICATION = Constants.class.getPackage().getName();

    static void setHost(String ip){
        Constants.ip = ip;
        host = PROTOCOL+ip+PORT;
    }

    static String getHost() {
        return host;
    }

    static void setUsername(String username) {
        Constants.username = username;
    }

    static String getUsername() {
        return username;
    }

    public static String getIp() {
        return ip;
    }
}
