package cn.edu.pku.adapter.cli;

public class LambdaCLI implements CLI{

    public static String ZIPPY = "zip %s %s";

    //aws lambda update-function-code --function-name my-function --zip-file fileb://function.zip
    public static String UPDATE_FUNCTION = "aws lambda update-function-code --function-name %s --zip-file fileb://%s";
}
