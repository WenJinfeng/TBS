package cn.edu.pku.adapter.cli;


//https://docs.azure.cn/zh-cn/azure-functions/functions-create-first-azure-function-azure-cli?tabs=browser
//https://docs.azure.cn/zh-cn/cli/functionapp/config/appsettings?view=azure-cli-latest#az-functionapp-config-appsettings-set
//https://docs.azure.cn/zh-cn/cli/functionapp?view=azure-cli-latest
//https://docs.microsoft.com/zh-cn/cli/azure/functionapp?view=azure-cli-latest#az-functionapp-create
//https://docs.microsoft.com/en-us/azure/azure-functions/functions-reference-python

public class AzureCLI implements CLI{

    public static String CMDPATH = "/usr/local/bin/";
    //resource group, app name, zip file path
    //https://docs.microsoft.com/en-us/azure/azure-functions/deployment-zip-push
    public static String CMD = "az";
    public static String DEPLOYMENT = "functionapp deployment source config-zip -g %s -n %s --src %s";
    //az group create --name AzureFunctionsQuickstart-rg --location chinanorth
    public static String CREATE_GROUP = "group create --name %s --location %s";
    //az storage account create --name <STORAGE_NAME> --location chinanorth --resource-group AzureFunctionsQuickstart-rg --sku Standard_LRS
    public static String CREATE_STORAGE="storage account create --name %s --location %s --resource-group %s --sku Standard_LRS";
    //az functionapp create --resource-group AzureFunctionsQuickstart-rg --consumption-plan-location chinanorth --runtime node --runtime-version 10 --functions-version 2 --name <APP_NAME> --storage-account <STORAGE_NAME>
    public static String CREATE_FUNCTION = "functionapp create --name %s --runtime %s --runtime-version %s --resource-group %s --consumption-plan-location %s --functions-version %s --storage-account %s --os-type Linux";

    //func azure functionapp publish <APP_NAME>
    public static String PUBLISJ_FUNCTION = "";

   public static String GETAllLOGS = "monitor app-insights query --apps %s --analytics-query requests --start-time %s --end-time %s";


    public static String QUERY_LOG = "monitor app-insights query --apps %s --analytics-query %s --start-time %s --end-time %s";

    public static String REQUEST_BY_INVOCATION_ID = "requests | where customDimensions[\"InvocationId\"]=~\"%s\"";
    public static String REQUEST_DETAIL_BY_INVOCATION_ID = "union traces\n" +
            "    | union exceptions\n" +
            "    | where timestamp > ago(30d)\n" +
            "    | where customDimensions['InvocationId'] == '%s'\n" +
            "    | order by timestamp asc\n" +
            "    | project timestamp, message = iff(message != '', message, iff(innermostMessage != '', innermostMessage, customDimensions.['prop__{OriginalFormat}'])), logLevel = customDimensions.['LogLevel']";


    //APPID, and func name
    public static String GET_URL_CODE = "rest --method post --uri https://management.azure.com%s/functions/%s/listKeys?api-version=2018-02-01";
}
