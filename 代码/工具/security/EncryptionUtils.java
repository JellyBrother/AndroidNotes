package com.jelly.app.main.security;

import android.util.Base64;

import java.security.AlgorithmParameters;
import java.security.Key;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * AES加密，加密方和解密方的密钥和算法需要保持一致。
 */
public class EncryptionUtils {
    /**
     * AES加密算法：密钥算法，java6支持56位密钥，bouncycastle支持64位
     * CBC：有向量模式
     * PKCS5Padding: 加密内容不足8位用余位数补足8位
     */
    private static final String CRYPTION_CBC_ALGORITHM = "AES/CBC/PKCS5Padding";
    private static final String KEY_ALGORITHM = "AES";
    private static final byte[] KEY_BYTES = {58, -103, 63, -112, -70, 35, 74, -111, 64, -120, -19, 10, -44, -108, 112, -75};

    /**
     * 生成密钥，java6只支持56位密钥，bouncycastle支持64位密钥
     *
     * @return byte[] 二进制密钥
     */
    public static SecretKey initkey(int keyLength) throws Exception {
        //实例化密钥生成器
        KeyGenerator kg = KeyGenerator.getInstance(KEY_ALGORITHM);
        //初始化密钥生成器，AES要求密钥长度为128位、192位、256位
        kg.init(keyLength);
        return kg.generateKey();
    }

    /**
     * 转换密钥
     *
     * @param key 二进制密钥
     * @return Key 密钥
     */
    public static Key toKey(byte[] key) {
        return new SecretKeySpec(key, KEY_ALGORITHM);
    }

    /**
     * Description：CBC模式加密
     *
     * @param data 待加密数据
     * @param key  密钥
     * @return 加密后的数据
     */
    public static byte[] encrypt(byte[] data, byte[] key) throws Exception {
        //还原密钥
        Key k = toKey(key);
        Cipher cipher = Cipher.getInstance(CRYPTION_CBC_ALGORITHM);
        //初始化，设置为加密模式
        cipher.init(Cipher.ENCRYPT_MODE, k, generateIV(key));
        //执行操作
        return cipher.doFinal(data);
    }

    /**
     * Description：CBC模式解密
     *
     * @param data 待解密数据
     * @param key  密钥
     * @return 解密后的数据
     */
    public static byte[] decrypt(byte[] data, byte[] key) throws Exception {
        //还原密钥
        Key k = toKey(key);
        Cipher cipher = Cipher.getInstance(CRYPTION_CBC_ALGORITHM);
        //初始化，设置为解密模式
        cipher.init(Cipher.DECRYPT_MODE, k, generateIV(key));
        //执行操作
        return cipher.doFinal(data);
    }

    /**
     * Description：生成IvParameterSpec：规范加密的参数，使用CBC模式时必须传入该参数
     *
     * @param key 密钥
     * @return 规范加密的参数，使用CBC模式时必须传入该参数
     */
    public static AlgorithmParameters generateIV(byte[] key) throws Exception {
        AlgorithmParameters params = AlgorithmParameters.getInstance(KEY_ALGORITHM);
        params.init(new IvParameterSpec(key));
        return params;
    }

    /**
     * Description：使用私钥KEY_BYTES，将需要加密的byte[]加密成byte[]
     */
    public static byte[] encryptB2B(byte[] data) throws Exception {
        return encrypt(data, KEY_BYTES);
    }

    /**
     * Description：使用私钥KEY_BYTES，将需要加密的byte[]加密成String
     */
    public static String encryptB2S(byte[] data) throws Exception {
        byte[] bytes = encrypt(data, KEY_BYTES);
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }

    /**
     * Description：使用私钥KEY_BYTES，将需要加密的String加密成byte[]
     */
    public static byte[] encryptS2B(String data) throws Exception {
        return encrypt(data.getBytes("utf-8"), KEY_BYTES);
    }

    /**
     * Description：使用私钥KEY_BYTES，将需要加密的String加密成String
     */
    public static String encryptS2S(String data) throws Exception {
        byte[] bytes = encrypt(data.getBytes("utf-8"), KEY_BYTES);
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }

    /**
     * Description：使用私钥KEY_BYTES，将加密的byte[]解密成byte[]
     */
    public static byte[] decryptB2B(byte[] data) throws Exception {
        return decrypt(data, KEY_BYTES);
    }

    /**
     * Description：使用私钥KEY_BYTES，将加密的byte[]解密成String
     */
    public static String decryptB2S(byte[] data) throws Exception {
        byte[] bytes = decrypt(data, KEY_BYTES);
        return new String(bytes, "utf-8");
    }

    /**
     * Description：使用私钥KEY_BYTES，将加密的String解密成byte[]
     */
    public static byte[] decryptS2B(String data) throws Exception {
        return decrypt(Base64.decode(data, Base64.DEFAULT), KEY_BYTES);
    }

    /**
     * Description：使用私钥KEY_BYTES，将加密的String解密成String
     */
    public static String decryptS2S(String data) throws Exception {
        byte[] bytes = decrypt(Base64.decode(data, Base64.DEFAULT), KEY_BYTES);
        return new String(bytes, "utf-8");
    }

    /**
     * Description：使用外部私钥key，将需要加密的byte[]加密成String
     */
    public static String encryptB2S(byte[] data, byte[] key) throws Exception {
        byte[] bytes = encrypt(data, key);
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }

    /**
     * Description：使用外部私钥key，将需要加密的String加密成byte[]
     */
    public static byte[] encryptS2B(String data, byte[] key) throws Exception {
        return encrypt(data.getBytes("utf-8"), key);
    }

    /**
     * Description：使用外部私钥key，将需要加密的String加密成String
     */
    public static String encryptS2S(String data, byte[] key) throws Exception {
        byte[] bytes = encrypt(data.getBytes("utf-8"), key);
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }

    /**
     * Description：使用外部私钥key，将需要解密的byte[]解密成String
     */
    public static String decryptB2S(byte[] data, byte[] key) throws Exception {
        byte[] bytes = decrypt(data, key);
        return new String(bytes, "utf-8");
    }

    /**
     * Description：使用外部私钥key，将需要解密的String解密成byte[]
     */
    public static byte[] decryptS2B(String data, byte[] key) throws Exception {
        return decrypt(Base64.decode(data, Base64.DEFAULT), key);
    }

    /**
     * Description：使用外部私钥key，将需要解密的String解密成String
     */
    public static String decryptS2S(String data, byte[] key) throws Exception {
        byte[] bytes = decrypt(Base64.decode(data, Base64.DEFAULT), key);
        return new String(bytes, "utf-8");
    }
}
