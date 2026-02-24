package com.ruoyi.common.utils;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESedeKeySpec;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.util.Base64;
import java.util.Map;
import java.util.TreeMap;

/**
 *
 * @Date 2024/4/1 22:40
 */
public class DESUtil {
    /**
     * 加解密统一编码方式
     */
    private final static String ENCODING = "utf-8";

    /**
     * 加解密方式
     */
    private final static String ALGORITHM  = "DESede";

    /**
     *加密模式及填充方式
     */
    private final static String PATTERN = "DESede/ECB/pkcs5padding";

    public static String createSecret() throws NoSuchAlgorithmException {
        // 创建一个3DES的KeyGenerator对象
        KeyGenerator keyGen = KeyGenerator.getInstance("DESede");

        // 初始化密钥生成器
        keyGen.init(112); // 3DES的密钥长度可以是112或168位，常用的是192位

        // 生成密钥
        SecretKey key = keyGen.generateKey();

        // 将SecretKey转换为字节数组
        byte[] keyBytes = key.getEncoded();

        // 输出密钥
        return bytesToHex(keyBytes);
    }


    /**
     * 3DES加密
     *
     * @param plainText 普通文本
     * @param sK 秘钥（24位密码）
     * @return
     * @throws Exception
     */
    public static String encode(String plainText,String sK){
        try {
            String secret = Base64.getEncoder().encodeToString(sK.getBytes(ENCODING));
            DESedeKeySpec dks = new DESedeKeySpec(secret.getBytes(ENCODING));
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(ALGORITHM);
            SecretKey secretKey = keyFactory.generateSecret(dks);
            Cipher cipher = Cipher.getInstance(PATTERN);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] encryptData = cipher.doFinal(plainText.getBytes(ENCODING));
            return bytesToHex(encryptData);
        }catch (Exception e) {
            return null;
        }
    }

    /**
     *
     *@Date 2024/4/22 23:09
     *@Description sk不进行base65
     *
     */
    public static String encodePlat(String plainText,String sK){
        try {
            DESedeKeySpec dks = new DESedeKeySpec(sK.getBytes(ENCODING));
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(ALGORITHM);
            SecretKey secretKey = keyFactory.generateSecret(dks);
            Cipher cipher = Cipher.getInstance(PATTERN);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] encryptData = cipher.doFinal(plainText.getBytes(ENCODING));
            return bytesToHex(encryptData);
        }catch (Exception e) {
            return null;
        }
    }

    public static String decode(String encryptText, String sK){
        try {
            String secret = Base64.getEncoder().encodeToString(sK.getBytes(ENCODING));
            DESedeKeySpec dks = new DESedeKeySpec(secret.getBytes(ENCODING));
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(ALGORITHM);
            SecretKey secretKey = keyFactory.generateSecret(dks);
            // 3DES加密采用pkcs5padding填充
            Cipher cipher = Cipher.getInstance(PATTERN);

            // 用密匙初始化Cipher对象
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            // 正式执行解密操作
            byte[] decryptData = cipher.doFinal(hexStringToByteArray(encryptText));
            return new String(decryptData, ENCODING);
        }catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String decodePlat(String encryptText, String sK){
        try {
            DESedeKeySpec dks = new DESedeKeySpec(sK.getBytes(ENCODING));
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(ALGORITHM);
            SecretKey secretKey = keyFactory.generateSecret(dks);
            // 3DES加密采用pkcs5padding填充
            Cipher cipher = Cipher.getInstance(PATTERN);

            // 用密匙初始化Cipher对象
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            // 正式执行解密操作
            byte[] decryptData = cipher.doFinal(hexStringToByteArray(encryptText));
            return new String(decryptData, ENCODING);
        }catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String bytesToHex(byte[] byteArray) {
        return String.format("%0" + (byteArray.length * 2) + "x", new BigInteger(1, byteArray));
    }

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }

    public static String getSingByMap(Map<String,Object> mapreq){
        TreeMap<String, Object> treeMap = MapUtil.sort(mapreq);
        StringBuilder sb = new StringBuilder();
        for (String key : treeMap.keySet()) {
            Object value = treeMap.get(key);
            if (ObjectUtil.isNotEmpty(value)) {
                sb.append(key).append("=").append(value).append("&");
            }
        }
        return sb.substring(0, sb.length() - 1);
    }
}
