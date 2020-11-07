package ru.nanit.vasyascheduler.api.util;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;

public final class HashUtil {

    public HashUtil(){}

    public static String getSHA1(final String str){
        try{
            return getSHA1(str.getBytes("UTF-8"));
        } catch (Exception e){
            return null;
        }
    }

    public static String getSHA1(final File file){
        try{
            byte[] bytes = Files.readAllBytes(file.toPath());
            return getSHA1(bytes);
        } catch (Exception e){
            return null;
        }
    }

    public static String getSHA1(final URL url){
        try{
            byte[] bytes = IOUtils.toByteArray(url);
            return getSHA1(bytes);
        } catch (Exception e) {
            return null;
        }
    }

    public static String getSHA1(final byte[] bytes) throws NoSuchAlgorithmException {
        MessageDigest crypt = MessageDigest.getInstance("SHA-1");
        crypt.reset();
        crypt.update(bytes);
        return byteToHex(crypt.digest());
    }

    public static String byteToHex(final byte[] hash) {
        Formatter formatter = new Formatter();
        for (byte b : hash) {
            formatter.format("%02x", b);
        }
        String result = formatter.toString();
        formatter.close();
        return result;
    }


}
