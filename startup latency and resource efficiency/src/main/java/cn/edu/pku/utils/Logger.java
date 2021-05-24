package cn.edu.pku.utils;

import com.amazonaws.util.Base64;
import sun.util.logging.PlatformLogger;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.logging.Level;

public class Logger {

    static java.util.logging.Logger logger = java.util.logging.Logger.getLogger("SVLESSLogger");

    public static void info(String msg){
        logger.log(Level.INFO, msg);
    }

    public static void error(String msg){
        logger.log(Level.WARNING, msg);
    }

    public static String byteBufferToString(ByteBuffer buffer){
        Charset charset = null;
        CharsetDecoder decoder = null;
        CharBuffer charBuffer = null;
        try
        {
            charset = Charset.forName("UTF-8");
            decoder = charset.newDecoder();
            charBuffer = decoder.decode(buffer.asReadOnlyBuffer());
            return charBuffer.toString();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            return "";
        }
    }

    public static ByteBuffer getByteBuffer(String str)
    {
        return ByteBuffer.wrap(str.getBytes());
    }

    public static String base64Decode(String str){
        byte[] contents = Base64.decode(str);
        return new String(contents);
    }

    public static void logParser(String log){
 
    }

}
