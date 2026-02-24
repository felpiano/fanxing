package com.ruoyi.common.utils;

import javax.crypto.Cipher;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * @author admin
 * @Date 2024/10/6 16:18
 */
public class RSAUtil {

    /**
     * RSA 加密
     *
     * @param message   需要加密的数据
     * @param publicKey 公钥字符串 以 Base64 编码的字符串形式传入。
     * @return
     * @throws Exception
     */
    public static String encrypt(String message, String publicKey) throws Exception {
        //将传入的 Base64 编码的公钥字符串解码为字节数组
        byte[] publicBytes = Base64.getDecoder().decode(publicKey);
        //将解码后的字节数组封装为一个 X509EncodedKeySpec 对象。
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicBytes);
        //获取一个用于处理 RSA 密钥的工厂实例。
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        //根据提供的公钥规范（keySpec）生成一个实际的 PublicKey 对象，供加密使用。
        PublicKey pubKey = keyFactory.generatePublic(keySpec);
        //指定加密算法为 RSA
        Cipher cipher = Cipher.getInstance("RSA");
        //指定 Cipher 对象的模式为加密模式  使用前面生成的 pubKey 对象来初始化 Cipher 对象。
        cipher.init(Cipher.ENCRYPT_MODE, pubKey);
        //使用 Cipher 对消息进行加密。
        byte[] encryptedBytes = cipher.doFinal(message.getBytes());
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }


    /**
     * RSA 解密
     *
     * @param encryptedMessage 加密字符串
     * @param privateKey       私钥字符串 以 Base64 编码的字符串形式传入。
     * @return
     * @throws Exception
     */
    public static String decrypt(String encryptedMessage, String privateKey) throws Exception {
        //将传入的 Base64 编码的私钥字符串解码为字节数组。
        byte[] privateBytes = Base64.getDecoder().decode(privateKey);
        //将解码后的字节数组封装为一个 PKCS8EncodedKeySpec 对象。
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateBytes);
        //通过 KeyFactory.getInstance("RSA") 获取一个用于处理 RSA 密钥的工厂实例。
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        //使用 KeyFactory 将 PKCS8EncodedKeySpec 转换为一个 PrivateKey 对象。  供解密使用
        PrivateKey priKey = keyFactory.generatePrivate(keySpec);
        //创建一个 Cipher 实例，指定加密算法为 RSA。
        Cipher cipher = Cipher.getInstance("RSA");
        //指定 Cipher 对象的模式为解密模式，并使用前面生成的 priKey 对象来初始化 Cipher 对象。
        cipher.init(Cipher.DECRYPT_MODE, priKey);
        //将传入的 Base64 编码的加密消息解码为字节数组。
        byte[] encryptedBytes = Base64.getDecoder().decode(encryptedMessage);
        //加密的字节数组进行解密，返回解密后的字节数组
        byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
        return new String(decryptedBytes);
    }

    /**
     * RSA 签名
     *
     * @param message    需要签名的明文消息（字符串）
     * @param privateKey 私钥字符串 以 Base64 编码的字符串形式传入。
     * @return
     * @throws Exception
     */
    public static String sign(String message, String privateKey) throws Exception {
        // 将传入的 Base64 编码的私钥字符串解码为字节数组。
        byte[] privateBytes = Base64.getDecoder().decode(privateKey);
        // 将解码后的字节数组封装为 PKCS8EncodedKeySpec 对象。
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateBytes);
        // 通过 KeyFactory.getInstance("RSA") 获取一个用于处理 RSA 密钥的工厂实例。
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        // 使用 KeyFactory 将 PKCS8EncodedKeySpec 转换为一个 PrivateKey 对象。  供签名使用
        PrivateKey priKey = keyFactory.generatePrivate(keySpec);
        // 创建一个 Signature 实例，指定签名算法为 SHA256withRSA。   SHA256withRSA：指定使用 SHA-256 哈希算法和 RSA 签名算法的组合。
        Signature signature = Signature.getInstance("SHA256withRSA");
        //使用私钥初始化 Signature 对象，并设置为签名模式。
        signature.initSign(priKey);
        //方法将字节数组添加到签名对象中，以便进行签名。 将消息字符串转换为 UTF-8 编码的字节数组。
        signature.update(message.getBytes("UTF-8"));
        //执行签名操作，返回签名后的字节数组。
        byte[] signatureBytes = signature.sign();
        //将签名后的字节数组进行 Base64 编码，转换为字符串
        return Base64.getEncoder().encodeToString(signatureBytes);
    }


    /**
     * RSA 验签
     *
     * @param message      原始消息（字符串），用于与签名进行匹配。
     * @param signatureStr 签名的 Base64 编码字符串。
     * @param publicKey    公钥的 Base64 编码字符串。
     * @return true 表示签名有效，false 表示签名无效
     * @throws Exception
     */
    public static boolean verify(String message, String signatureStr, String publicKey) throws Exception {
        //将传入的 Base64 编码的公钥字符串解码为字节数组。
        byte[] publicBytes = Base64.getDecoder().decode(publicKey);
        //将解码后的字节数组封装为 X509EncodedKeySpec 对象。
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicBytes);
        //通过 KeyFactory.getInstance("RSA") 获取一个用于处理 RSA 密钥的工厂实例。
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        //使用 KeyFactory 将 X509EncodedKeySpec 转换为一个 PublicKey 对象。  供验签使用
        PublicKey pubKey = keyFactory.generatePublic(keySpec);
        //创建一个 Signature 实例，指定签名算法为 SHA256withRSA。   SHA256withRSA：指定使用 SHA-256 哈希算法和 RSA 签名算法的组合。
        Signature signature = Signature.getInstance("SHA256withRSA");
        //使用公钥初始化 Signature 对象，并设置为验证模式。
        signature.initVerify(pubKey);
        //将原始消息数据传递给 Signature 对象进行验证。
        signature.update(message.getBytes("UTF-8"));
        //将消息字符串转换为 UTF-8 编码的字节数组。
        byte[] signatureBytes = Base64.getDecoder().decode(signatureStr);
        // 使用 Signature 对象验证签名的有效性。
        return signature.verify(signatureBytes);
    }



    public static void main(String[] args) throws Exception {
        String pubKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAlgxSe2iGz5YfHxhriBl5" +
                "j3N+Wk+rv3/XZWypzgbhXzWxxdjx+CfoAh1TekXsT7Wus+xiIcdSicklm0GYn5aL" +
                "yPcOiFnQ7VttLdKLjyP83BO1QTX4n82GAyx9mn18FpDEm4whyIS/Nch8UhXyPl4n" +
                "rtp3cxVubDrZSRcc0XbWnhIlUsUk/tdUZPBG1Guu/Vuk0+1BQHEpKQaR3aTvzgLR" +
                "LQBuLpXZeGPFG7h2dfKkOZ71dl7Z5R6oFTkKhRXkvyWktq2qdUHXbH44tJ5dxxu+" +
                "u2M7fqrXSpUZokEioizyp16YDk12Wj2cDLUVR19xDeUpJVzYRgBbIJ4uHdu2Wen3" +
                "swIDAQAB";
        String priKey = "MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQCWDFJ7aIbPlh8f" +
                "GGuIGXmPc35aT6u/f9dlbKnOBuFfNbHF2PH4J+gCHVN6RexPta6z7GIhx1KJySWb" +
                "QZiflovI9w6IWdDtW20t0ouPI/zcE7VBNfifzYYDLH2afXwWkMSbjCHIhL81yHxS" +
                "FfI+Xieu2ndzFW5sOtlJFxzRdtaeEiVSxST+11Rk8EbUa679W6TT7UFAcSkpBpHd" +
                "pO/OAtEtAG4uldl4Y8UbuHZ18qQ5nvV2XtnlHqgVOQqFFeS/JaS2rap1Qddsfji0" +
                "nl3HG767Yzt+qtdKlRmiQSKiLPKnXpgOTXZaPZwMtRVHX3EN5SklXNhGAFsgni4d" +
                "27ZZ6fezAgMBAAECggEAHNlcqHaV6OajsC5ZSm8fdKnPgtyrmGOjjdAhK8gd39jq" +
                "h7ApAb3meTWbPbig8UhswRIzYew5Xd2LOfkYN6LR2H9Lz4J5TZGVAcjn5UsVjFjL" +
                "sjCGz27ME+MjeSckSxBlhMSiIDQJsXUNFhk7HKustI0zhzB5LRz5dviXJVzUccqe" +
                "+J2S7GESgobhte8UK2NOIEf4fO/RFxsnp3vNPOfReOXTIL3knHv7ACA/lMCsTNN4" +
                "t1CmFngw8u3RQoEwoBu33bNqwKYoUf824yJZEjFQ5iAYR0ol8rB3DlTCTumhj3br" +
                "T8lKc0M91olenfX6xnAr3gNBI5kA2f6ugL2bbgpujQKBgQDInfz6BPe1WAKcfGcE" +
                "HaiVRyXRHcJdm8AI5ONFpyYemaTH8U8HZd3TxQ3/CMcS+rxVMnjEK2KPIsYxAoGf" +
                "mPJPaNNVNFIl+I9JeDOc8QIgKnQqdEkakumuxv9d8tit1M3XrNfqxhhP/K2D9qXK" +
                "hneSIM80l1npGDkTnw8QNQDw9wKBgQC/eIWUSdRt12/UUl/d6PGtJ5RwEPXh7qX1" +
                "A0qlK4YMrPNx2f0dYOcrG1YRIc+AWjSkvT4jo9kZ46xKiQVEyaGANFG5i6R4rMIg" +
                "5JqIq4yUTrOh1Fx9m2kwtt+gOSDMEYwngP0QCs0cUq8tSivOE7nP0cTyycKGKqiG" +
                "PHFnSAz8JQKBgQCdEc0SNzd13O1WdbP2Lf5iueyq2EDoJ1xn2kTysNaQmLzB6nV0" +
                "llXiSuMVbVrnyjYQAk85T7p/1LsVfh/cw9PbRnhHuooivoP5AspuMuf2JScEYj0m" +
                "OCYLuDQR62OoXfe3x9TZNLWuUmfL7R2K/lK5r0RrqaA0dsmhSiRyDP2qqwKBgD4m" +
                "h+2XfX3eFW2tA3NLvs7+7v7lcnrQ9UuvstYkZ5HLvgXxxWh44PCsIebTu8AlB7uF" +
                "thrpf3oY3f8ftPccH/E2imSZAF0vbqAwtDyyuqIVXdek0BTxklZ+td4TTSMQexcD" +
                "lqFTcDrGWBhyX/+3hXQA2lYYyay1Qhi4m/Lzf0uFAoGBAKgLMopoyVuvEAHdPUcF" +
                "aPPTSoy1qbZjXkZaC3Kz1z3+veIOTon4HMFQp96gSBV03iuRbEO3zCmAb5f7NS2n" +
                "iIzIj44SddzUzZFWsVfULJNzFIWk9QMXk7nW+LAJdgMdYQSa9HB0M41zH2x1nVm1" +
                "X/chZuxOgeqVf5JV1s2ahZ/+";

        String originalMessage = "Hello, world!";
        System.out.println("准备的数据: " + originalMessage);
        System.out.println("公钥：" + pubKey);
        System.out.println("私钥：" + priKey);
        // 加密
        String encryptedMessage = encrypt(originalMessage, pubKey);
        System.out.println("加密数据: " + encryptedMessage);

        // 解密
        String decryptedMessage = decrypt(encryptedMessage, priKey);
        System.out.println("解密数据: " + decryptedMessage);
    }
}
