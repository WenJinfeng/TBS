package cn.edu.pku.adapter.cli;

public class GoogleCLI {
    public static String CLI = "gcloud";
    public static String deploy = "functions " +
            "deploy " +
            "float " +
            "--entry-point function_handler " +
            "--runtime python37 " +
            "--memory 512MB " +
            "--trigger-http " +
            "--allow-unauthenticated " +
            "--source /Users/vector/Desktop/poppy/ServerlessBenchmark/ServerlessBenchmark/resources/benchmark/python/cpu/float/google " +
            "--timeout=540";
    public static String DEPLOY = "functions deploy " +
            "%s " +
            "--entry-point %s " +
            "--runtime %s " +
            "--memory %s " +
            "--%s " +
            "--allow-unauthenticated " +
            "--source %s " +
            "--timeout=%s";

}
