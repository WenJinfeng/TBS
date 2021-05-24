package cn.edu.pku.model;

import java.util.HashMap;

public class GoogleLogDetailItem {
    public String insertId;
    public String logName;
    public String receiveTimestamp;
    public String trace;
    public String textPayload;
    public GoogleLogDetailItem() {
    }

    public String getTraceID(){
        String[] args = trace.split("/");
        return args[args.length - 1];
    }

    public static void main(String[] args){
        GoogleLogDetailItem googleLogDetailItem = new GoogleLogDetailItem();
        googleLogDetailItem.trace = "projects/serverlessfunly/traces/6cbac3d2fb6f2742ecc6ba85cfb3fd8c";
        System.out.println(googleLogDetailItem.getTraceID());
    }
}
