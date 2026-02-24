package com.ruoyi.common.utils;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class TripleDESUtil {
    // 算法标识：DESede对应.NET的TripleDES
    private static final String ALGORITHM = "DESede";
    // 转换格式：ECB模式+PKCS7填充（Java中PKCS5Padding等价于PKCS7）
    private static final String TRANSFORMATION = "DESede/ECB/PKCS5Padding";

    public static void main(String[] args) throws Exception {
        String str = "[\n" +
                "    {\n" +
                "      id:'123456789',\n" +
                "      shop_id:'999988888',\n" +
                "      order_no:'HB716984298443374592',\n" +
                "      status:1,\n" +
                "      amount:1500,\n" +
                "      remark:'34016',\n" +
                "      order_time:'2025-11-08 20:14:16',\n" +
                "      Payment_time:'2025-11-08 20:14:17'\n" +
                "    }\n" +
                "  ]";
        String key = "Tx&sf$3BxoQdlEbxTx8$#x0p";
        System.out.println(encrypt(str, key));
    }

    public static String encrypt(String plainText, String key) throws Exception {
        // 将密钥字符串转换为字节数组（等价于C#的Encoding.UTF8.GetBytes）
        byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
        // 处理密钥长度：TripleDES需要24字节（192位）
        byte[] validKey = adjustKeyLength(keyBytes);

        SecretKeySpec keySpec = new SecretKeySpec(validKey, ALGORITHM);
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.ENCRYPT_MODE, keySpec);

        byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(encrypted);
    }

    public static String decrypt(String cipherText, String key) throws Exception {
        byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
        byte[] validKey = adjustKeyLength(keyBytes);

        SecretKeySpec keySpec = new SecretKeySpec(validKey, ALGORITHM);
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.DECRYPT_MODE, keySpec);

        byte[] decoded = Base64.getDecoder().decode(cipherText);
        byte[] decrypted = cipher.doFinal(decoded);
        return new String(decrypted, StandardCharsets.UTF_8);
    }

    // 密钥长度适配方法（TripleDES需要24字节）
    private static byte[] adjustKeyLength(byte[] keyBytes) {
        // 如果密钥长度不足24字节，使用PKCS7填充
        if (keyBytes.length < 24) {
            int padding = 24 - keyBytes.length;
            byte[] padded = new byte[24];
            System.arraycopy(keyBytes, 0, padded, 0, keyBytes.length);
            // 填充剩余部分为0（实际应使用更安全的填充方式）
            return padded;
        }
        // 如果超过24字节则截断
        return java.util.Arrays.copyOf(keyBytes, 24);
    }

}