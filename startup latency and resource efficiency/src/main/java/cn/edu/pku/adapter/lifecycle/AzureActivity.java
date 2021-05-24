package cn.edu.pku.adapter.lifecycle;

import cn.edu.pku.adapter.cli.AzureCLI;
import cn.edu.pku.adapter.log.AzureLog;
import cn.edu.pku.adapter.log.LogUtil;
import cn.edu.pku.model.AzureHTTPResponse;
import cn.edu.pku.utils.AzureUtil;
import cn.edu.pku.utils.CommandUtil;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.appservice.AppServicePlan;
import com.microsoft.azure.management.appservice.FunctionApp;
import com.microsoft.azure.management.appservice.PublishingProfile;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.fluentcore.utils.SdkContext;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class AzureActivity {

    //refer to https://github.com/Azure-Samples/app-service-java-configure-deployment-sources-for-functions/tree/master/src/main/resources

    public static Azure azure;
    static{
        try {
            //auth followed in https://github.com/Azure/azure-libraries-for-java/blob/master/AUTH.md
            azure = Azure.authenticate(new File("/Users/vector/Desktop/poppy/ServerlessBenchmark/ServerlessBenchmark/my.azureauth")).withDefaultSubscription();
            //If you have Azure CLI (>=2.0) installed and authenticated on your machine, the SDK client is able to use the current account and subscription Azure CLI is logged in.
            //azure = Azure.authenticate(AzureCliCredentials.create()).withDefaultSubscription();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    static String storage_account = "XXX";
    static String consumption_plan_location = "XXXX";
    static String resource_group = "XXXX";
    public static String subscription_id = "XXXX";
    static String function_version = "2";
    static String function_name = "HttpExample";


    public static String getFunctionInvocationCode(String appName){
        String applicationID = getApplicationID(appName);
        String cli = String.format(AzureCLI.GET_URL_CODE, applicationID, function_name);
        String[] args = cli.split(" ");
        String result = CommandUtil.executeCMDWithResponse(1, AzureCLI.CMD, AzureCLI.CMDPATH, args);
        //System.out.println(result);
        JsonObject jsonObject = new JsonParser().parse(result).getAsJsonObject();
        return jsonObject.get("default").toString().replace("\"","");
    }

    public static String getApplicationID(String functionAppName){
        return String.format("/subscriptions/%s/resourceGroups/%s/providers/Microsoft.Web/sites/%s", subscription_id, resource_group, functionAppName);
    }

    public static String getFunctionInvokeUrl(String appName){
        String code = getFunctionInvocationCode(appName);
        //System.out.println(code);
        String url = String.format("https://%s.azurewebsites.net/api/%s?code=%s", appName, function_name, code);
        return url;
    }


    static String PYTHON = "python";
    static String PYTHIONVERSIONS = "3.7";

    static String testPath = "/Users/vector/Desktop/poppy/ServerlessBenchmark/ServerlessBenchmark/src/main/resources";

    /*
        functionapp create --name %s --runtime %s --runtime-version %s --resource-group %s --consumption-plan-location %s --functions-version %s --storage-account %s
    */

    public static void createFunction(String name, String runtime, String runtime_version, String resource_group, String consumption_plan_location, String storage_account){
        String deployCLI = String.format(AzureCLI.CREATE_FUNCTION, name, runtime, runtime_version,resource_group, consumption_plan_location, function_version, storage_account);
        String[] args = deployCLI.split(" ");
        System.out.println(deployCLI);
        CommandUtil.executeCMD(0, AzureCLI.CMD, "/usr/local/bin/", args);
    }

    public static void createFunction(String name, String runtime, String runtime_version){
        createFunction(name, runtime, runtime_version, resource_group, consumption_plan_location, storage_account);
    }


    //az functionapp deployment source config-zip -g serverlesstestforazure -n liuyitestfromcli --src /Users/vector/Desktop/poppy/azure/hello-python.zip
    public static void deployFunction(String resourceGroup, String functionName,  String localPath){
        String deployCLI = String.format(AzureCLI.DEPLOYMENT, resourceGroup, functionName, localPath);
        String[] args = deployCLI.split(" ");
        System.out.println(deployCLI);
        CommandUtil.executeCMD(0, AzureCLI.CMD, "/usr/local/bin/", args);
    }


    public static void creeateFunction(){

    }


    private static OkHttpClient httpClient;

    /**
     * Main function which runs the actual sample.
     * @param azure instance of the azure client
     * @return true if sample runs successfully
     */
    public static boolean runSample(Azure azure) {
        // New resources
        final String suffix         = ".azurewebsites.net";
        final String app1Name       = SdkContext.randomResourceName("webapp1-", 20);
        final String app2Name       = SdkContext.randomResourceName("webapp2-", 20);
        final String app3Name       = SdkContext.randomResourceName("webapp3-", 20);
        final String app4Name       = SdkContext.randomResourceName("webapp4-", 20);
        final String app5Name       = SdkContext.randomResourceName("webapp5-", 20);
        final String app6Name       = SdkContext.randomResourceName("webapp6-", 20);
        final String app1Url        = app1Name + suffix;
        final String app2Url        = app2Name + suffix;
        final String app3Url        = app3Name + suffix;
        final String app4Url        = app4Name + suffix;
        final String app5Url        = app5Name + suffix;
        final String app6Url        = app6Name + suffix;
        final String rgName         = SdkContext.randomResourceName("rg1NEMV_", 24);

        try {
            //============================================================
            // Create a function app with a new app service plan
            System.out.println("Creating function app " + app1Name + " in resource group " + rgName + "...");
//            FunctionApp app1 = azure.appServices().functionApps().define(app1Name)
//                    .withRegion(Region.US_WEST)
//                    //.withExistingResourceGroup(resource_group)
//                    .withNewResourceGroup(rgName)
//                    .withRuntime(PYTHON)
//                    .withRuntimeVersion("2.0")
//                    .withPythonVersion(PythonVersion.fromString("3.7"))
//                    .create();
            FunctionApp app1 = azure.appServices().functionApps().define(app1Name)
                    .withRegion(Region.US_WEST)
                    .withNewResourceGroup(rgName)
                    .create();

            System.out.println("Created function app " + app1.name());
            AzureUtil.print(app1);
            //============================================================
            // Deploy to app 1 through FTP
            System.out.println("Deploying a function app to " + app1Name + " through FTP...");

            AzureUtil.uploadFileToFunctionApp(app1.getPublishingProfile(), "host.json", new FileInputStream(testPath + "/host.json"));
            AzureUtil.uploadFileToFunctionApp(app1.getPublishingProfile(), "azure/function.json",  new FileInputStream(testPath + "/azure/function.json"));
            AzureUtil.uploadFileToFunctionApp(app1.getPublishingProfile(), "azure/__init__.py",  new FileInputStream(testPath + "/azure/__init__.py"));
            AzureUtil.uploadFileToFunctionApp(app1.getPublishingProfile(), "requirements.txt",  new FileInputStream(testPath + "/azure/requirements.txt"));

            // sync triggers
            app1.syncTriggers();

            System.out.println("Deployment azure app to function app " + app1.name() + " completed");
            AzureUtil.print(app1);

            // warm up
            System.out.println("Warming up " + app1Url + "/api/azure...");
            post("http://" + app1Url + "/api/azure", "625");
            SdkContext.sleep(5000);
            System.out.println("CURLing " + app1Url + "/api/azure...");
            System.out.println("Azure of 625 is " + post("http://" + app1Url + "/api/azure", "625"));



            AppServicePlan plan = azure.appServices().appServicePlans().getById(app1.appServicePlanId());
            System.out.println("Creating another function app " + app6Name + "...");
            FunctionApp app6 = azure.appServices().functionApps()
                    .define(app6Name)
                    .withExistingAppServicePlan(plan)
                    .withExistingResourceGroup(rgName)
                    .create();
//
            System.out.println("Created function app " + app6.name());

            //============================================================
            // Deploy to the 6th function app through ZIP deploy

            System.out.println("Deploying square-function-app.zip to " + app6Name + " through ZIP deploy...");

            app6.zipDeploy(new File(testPath + "/test.zip"));

            // warm up
            System.out.println("Warming up " + app6Url + "/api/square...");
            post("http://" + app6Url + "/api/square", "926");
            SdkContext.sleep(5000);
            System.out.println("CURLing " + app6Url + "/api/square...");
            System.out.println("Square of 926 is " + post("http://" + app6Url + "/api/square", "926"));

            return true;
        } catch (Exception e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        } finally {
            try {
//                System.out.println("Deleting Resource Group: " + rgName);
//                azure.resourceGroups().beginDeleteByName(rgName);
//                System.out.println("Deleted Resource Group: " + rgName);
            } catch (NullPointerException npe) {
                System.out.println("Did not create any resources in Azure. No clean up is necessary");
            } catch (Exception g) {
                g.printStackTrace();
            }
        }
        return false;
    }
    /**
     * Main entry point.
     * @param args the parameters
     */
    public static void main(String[] args) {
        try {

            // Print selected subscription
            System.out.println("Selected subscription: " + azure.subscriptionId());
//            runSample(azure);
            PagedList<FunctionApp> apps = azure.appServices().functionApps().list();
            for(FunctionApp app : apps) {
                AzureUtil.print(app);
                PublishingProfile publishingProfile = app.getPublishingProfile();
                System.out.println("ftp server: " + publishingProfile.ftpUrl());
                System.out.println("ftp profile.ftpUsername(): " + publishingProfile.ftpUsername());
                System.out.println("ftp profile.ftpPassword(): " + publishingProfile.ftpPassword());
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    private static String curl(String url) {
        Request request = new Request.Builder().url(url).get().build();
        try {
            return httpClient.newCall(request).execute().body().string();
        } catch (IOException e) {
            return null;
        }
    }

    private static String post(String url, String body) {
        Request request = new Request.Builder().url(url).post(RequestBody.create(MediaType.parse("text/plain"), body)).build();
        try {
            return httpClient.newCall(request).execute().body().string();
        } catch (IOException e) {
            return null;
        }
    }

    static {
        httpClient = new OkHttpClient.Builder().readTimeout(1, TimeUnit.MINUTES).build();
    }

    public static void updateFunction(String appName){
        String appId = getApplicationID(appName);
        FunctionApp app = azure.appServices().functionApps().getById(appId);

    }


    public static String invokeFunction(String appName, String data, String saveKey){
        String result = invokeFunction(appName,data);
        if(saveKey != null && saveKey.trim().length() > 0)
            LogUtil.put(saveKey, result, true);
        return result;
    }

    static String invokeFunction(String appName, String data){
        String invokeUrl = getFunctionInvokeUrl(appName);
        if(data != null && data.trim().length() > 0)
            invokeUrl = invokeUrl + "&" + data;
        //System.out.println(invokeUrl);
        return curl(invokeUrl);
    }

   public static String getCodeStart(String functionName, String data){
        String invoke1 = invokeFunction(functionName, data);
        try {
            Thread.sleep(10*1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        String invoke2 = invokeFunction(functionName, data);
        AzureHTTPResponse response1 = new Gson().fromJson(invoke1, AzureHTTPResponse.class);
        AzureHTTPResponse response2 = new Gson().fromJson(invoke2, AzureHTTPResponse.class);
        List<String> result = new ArrayList<String>();
        result.add(response1.invocationid);
        result.add(response2.invocationid);
        return new Gson().toJson(result);
    }

   public static String getCodeStartTemporal(String functionName, String data, String saveName){
        String invoke = invokeFunction(functionName, data, saveName);
        System.out.println(invoke);
        AzureHTTPResponse response = new Gson().fromJson(invoke, AzureHTTPResponse.class);
        List<String> result = new ArrayList<String>();
        result.add(response.invocationid);
        return new Gson().toJson(result);
    }

    public static long getCodeStartTime(String app, String ids){
        List<String> exeIds = new Gson().fromJson(ids, new TypeToken<List<String>>(){}.getType());
       if(exeIds.size() == 2) {
            String log1 = AzureLog.getRequestByInvocationId(app, exeIds.get(0));
            String log2 = AzureLog.getRequestByInvocationId(app, exeIds.get(1));

            Map<String, String> info1 = AzureLog.parseRequestLog(log1);
            Map<String, String> info2 = AzureLog.parseRequestLog(log2);
            float time1 = Float.parseFloat(info1.get("exeTime"));
            float time2 = Float.parseFloat(info2.get("exeTime"));
            System.out.println("code start: " + (time1 - time2));
            return (long) (time1 - time2);
        }else if(exeIds.size() == 1){
            String log =  AzureLog.getRequestByInvocationId(app, exeIds.get(0));
            Map<String, String> info = AzureLog.parseRequestLog(log);
            float time1 = Float.parseFloat(info.get("exeTime"));
            float time2 = Float.parseFloat(info.get("functionTime"));
            System.out.println("start time: " + (time1 - time2));
            System.out.println("host instance id: " + info.get("HostInstanceId"));
            return (long) (time1 - time2);
        }
        return -1;
    }
}
