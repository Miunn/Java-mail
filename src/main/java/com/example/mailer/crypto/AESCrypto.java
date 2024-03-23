package com.example.mailer.crypto;

import utils.Constants;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import java.util.Base64;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;


public class AESCrypto {

    public static byte[] encrypt(byte[] data, byte[] key) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException{
        //méthode de chiffrement AES d'un message m en utilisant la clef key

        Cipher cipher= Cipher.getInstance(Constants.AES_Padding);
        MessageDigest digest=MessageDigest.getInstance(Constants.Digest_Alg);
        digest.update(key);
        byte[] AESkey=Arrays.copyOf(digest.digest(),16);
        SecretKeySpec keyspec=new SecretKeySpec(AESkey, Constants.AES);
        cipher.init(Cipher.ENCRYPT_MODE, keyspec);

        return Base64.getEncoder().encode(cipher.doFinal(data));
    }


    public static String decrypt(byte[] cipherText, byte[] key) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException{

        Cipher cipher= Cipher.getInstance(Constants.AES_Padding);
        MessageDigest digest=MessageDigest.getInstance(Constants.Digest_Alg);
        digest.update(key);
        byte[] AESkey=Arrays.copyOf(digest.digest(),16);
        SecretKeySpec keyspec=new SecretKeySpec(AESkey, Constants.AES);
        cipher.init(Cipher.DECRYPT_MODE, keyspec);

        return new String(cipher.doFinal(Base64.getDecoder().decode(cipherText)));

    }


}

