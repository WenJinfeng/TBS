package cn.edu.pku.adapter.log;

import com.sleepycat.je.*;
import sun.rmi.runtime.Log;

import java.io.File;

public class LogUtil {
    static String dbEnvFilePath = "./resources/logs/";
    static String databaseName = "serverless";
    static Environment myEnvironment = null;
    static Database serverlessDatabase = null;

    public static String GOOGLE_PLATFORM = "google";
    public static String AWS_PLATFORM = "AWS";
    public static String AZURE_PLATFORM = "azure";
    public static String ALIYUN_PLATFORM = "aliyun";

    public static String MEMORY_TEST = "memory_test";
    public static String COLD_START_TEST = "code_start_test";
    public static String CONCURRENCY_TEST = "concurrency_test";
    public static String PACKAGE_SIZE_TEST = "package_size_test";
    public static String LANGUAGE_TEST = "language_test";

    public static String BASE_TEST="base_test";
    public static String FLOAT_TEST="float_test";
    public static String LINPACK_TEST="linpack_test";
    public static String FIB_TEST="fib_test";
    public static String RANDOMIO_TEST="randomio_test";
    public static String SEQIO_TEST="sequentiaoio_test";

    public static String GOOGLE_EXE_IDS = "ids";
    public static String RESPONSE = "response";


    static{
    
        try {
            File file = new File(dbEnvFilePath);
            if (!file.exists()) {
                file.mkdirs();
            }
            EnvironmentConfig envConfig = new EnvironmentConfig();
            envConfig.setAllowCreate(true);
            DatabaseConfig dbConfig = new DatabaseConfig();
            dbConfig.setAllowCreate(true);
            dbConfig.setDeferredWrite(true);
            myEnvironment = new Environment(file, envConfig);
            serverlessDatabase = myEnvironment.openDatabase(null, databaseName,
                    dbConfig);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Database getInstance(){
        return serverlessDatabase;
    }



    public static boolean put(String key, String value, boolean isSync) {
        try {
            DatabaseEntry theKey = new DatabaseEntry(key.getBytes("UTF-8"));
            DatabaseEntry theValue = new DatabaseEntry(value.getBytes("UTF-8"));
            serverlessDatabase.put(null, theKey, theValue);
            if (isSync) {
                sync();
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

 
    public static boolean delete(String key) {
        try {
            DatabaseEntry theKey = new DatabaseEntry(key.getBytes("UTF-8"));
            serverlessDatabase.delete(null, theKey);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

  
    public static String getValue(String key) {
        try {
            DatabaseEntry theKey = new DatabaseEntry(key.getBytes("UTF-8"));
            DatabaseEntry theValue = new DatabaseEntry();
            serverlessDatabase.get(null, theKey, theValue, LockMode.DEFAULT);
            if (theValue.getData() == null) {
                return null;
            }
            return new String(theValue.getData(), "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public static boolean sync() {
        if (serverlessDatabase != null) {
            try {
                serverlessDatabase.sync();
                return true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }


    public static boolean close() {
        try {
            
            if (serverlessDatabase != null) {
                serverlessDatabase.close();
            }
            
            if (myEnvironment != null) {
                myEnvironment.sync();
                myEnvironment.cleanLog(); 
                myEnvironment.close();
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;

    }


    public static void main(String[] args) {
        
        String key = "XXX";
        LogUtil.put(key, "1", false);
        LogUtil.put(key, "2", false);
        LogUtil.sync();
        System.out.println(LogUtil.getValue(key));
    }
}