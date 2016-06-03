package com.gov.culturems.utils;


import java.security.MessageDigest;

/**
 * 标准MD5加密方法，使用java类库的security包的MessageDigest类处理
 *
 * @author Sarin
 */

public class EncodeUtil {

    //采用小写加密
    private final static String[] hexDigits = {"0", "1", "2", "3", "4", "5",
            "6", "7", "8", "9", "a", "b", "c", "d", "e", "f"};

    /**
     * 获得MD5加密密码的方法
     */
    public static String getMD5ofStr(String origString) {
        String origMD5 = null;
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            byte[] result = md5.digest(origString.getBytes());
            origMD5 = byteArray2HexStr(result);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return origMD5;
    }

    /**
     * 仅用于此项目中的加密
     */
    public static String getCustomMD5ofStr(String origString, String salt) {
        if (salt == null || salt.equals("")) {
            return "";
        }
        String origMD5 = null;
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            byte[] result = md5.digest(origString.getBytes());
            origMD5 = byteArray2HexStr(result);
        } catch (Exception e) {
            e.printStackTrace();
        }

        String finalMd5 = null;
        try {
            MessageDigest md52 = MessageDigest.getInstance("MD5");
            byte[] result2 = md52.digest((origMD5 + "{" + salt + "}").getBytes());
            finalMd5 = byteArray2HexStr(result2);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return finalMd5;
    }

    /**
     * 处理字节数组得到MD5密码的方法
     */
    private static String byteArray2HexStr(byte[] bs) {
        StringBuffer sb = new StringBuffer();
        for (byte b : bs) {
            sb.append(byte2HexStr(b));
        }
        return sb.toString();
    }

    /**
     * 字节标准移位转十六进制方法
     */
    private static String byte2HexStr(byte b) {
        int n = b;
        if (n < 0)
            n = 256 + n;
        int d1 = n / 16;
        int d2 = n % 16;
        return hexDigits[d1] + hexDigits[d2];
//        String hexStr = null;
//        int n = b;
//        if (n < 0) {
//            若需要自定义加密, 请修改这个移位算法即可
//            n = b & 0x7F + 128;
//        }
//        hexStr = Integer.toHexString(n / 16) + Integer.toHexString(n % 16);
//        return hexStr.toUpperCase();
    }

    /**
     * 提供一个MD5多次加密方法
     */
    public static String getMD5ofStr(String origString, int times) {
        String md5 = getMD5ofStr(origString);
        for (int i = 0; i < times - 1; i++) {
            md5 = getMD5ofStr(md5);
        }
        return getMD5ofStr(md5);
    }

    /**
     * 密码验证方法
     */
    public static boolean verifyPassword(String inputStr, String MD5Code) {
        return getMD5ofStr(inputStr).equals(MD5Code);
    }

    /**
     * 重载一个多次加密时的密码验证方法
     */
    public static boolean verifyPassword(String inputStr, String MD5Code, int times) {
        return getMD5ofStr(inputStr, times).equals(MD5Code);
    }

    /**
     * 提供一个测试的主函数
     */
    public static void main(String[] args) {
//        String firstMd5 = getMD5ofStr("lis6");

        System.out.println("encoded  " + getCustomMD5ofStr("lis6", "lis6"));
//        System.out.println("123456789:" + getMD5ofStr("123456789"));
//        System.out.println("sarin:" + getMD5ofStr("sarin"));
//        System.out.println("123:" + getMD5ofStr("123", 4));
    }
}
