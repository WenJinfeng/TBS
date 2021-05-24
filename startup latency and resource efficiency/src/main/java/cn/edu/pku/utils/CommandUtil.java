package cn.edu.pku.utils;

import com.google.common.util.concurrent.ListenableFuture;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class CommandUtil {

    public static void chmod(String file) {
        executeCMD(0,"/bin/chmod", null, "755");
    }


   public static void executeCMD(int type, String shellFile,  String dir, String... params) {
        if(type == 1)
            shellFile = "./" + shellFile;
        List paramList = params == null ? new ArrayList<>(1) : new ArrayList<>(1 + params.length);
        paramList.add(shellFile);
        for (String arg : params)
            paramList.add(arg);
        ProcessBuilder pb = new ProcessBuilder(paramList);
        if(dir != null)
            pb.directory(new File(dir));

        int runningStatus = 0;
        String s = null;
        Process p = null;
        try {
            p = pb.start();
            BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
            BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));
            while ((s = stdInput.readLine()) != null) {
                Logger.info(s);
            }

            while ((s = stdError.readLine()) != null) {
                Logger.error(s);
            }
            try {
                runningStatus = p.waitFor();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String executeCMDWithResponse(int type, String shellFile,  String dir, String... params) {

        StringBuilder result = new StringBuilder();
        if(type == 1)
            shellFile = "./" + shellFile;
        List paramList = params == null ? new ArrayList<>(1) : new ArrayList<>(1 + params.length);
        paramList.add(shellFile);
        for (String arg : params)
            paramList.add(arg);
        ProcessBuilder pb = new ProcessBuilder(paramList);
        if(dir != null)
            pb.directory(new File(dir));

        int runningStatus = 0;
        String s = null;
        Process p = null;
        try {
            p = pb.start();
            BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
            BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));
            while ((s = stdInput.readLine()) != null) {
                Logger.info(s);
                result.append(s).append("\n");
            }
            while ((s = stdError.readLine()) != null) {
                Logger.error(s);
                result.append(s).append("\n");
            }
            try {
                runningStatus = p.waitFor();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return result.toString();
    }
}
