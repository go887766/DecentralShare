package com.decentralshare.app.crypto;

import android.content.Context;
import android.util.Base64;
import android.util.Log;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class CryptoManager {
    private static final String TAG = "CryptoManager";
    private static CryptoManager instance;
    private Context context;
    
    private static final String KEY_ALGORITHM = "RSA";
    private static final String SIGNATURE_ALGORITHM = "SHA256withRSA";
    private static final String CIPHER_TRANSFORMATION = "RSA/ECB/PKCS1Padding";
    private static final int KEY_SIZE = 2048;
    
    static {
        Security.addProvider(new BouncyCastleProvider());
    }
    
    private CryptoManager(Context context) {
        this.context = context.getApplicationContext();
    }
    
    public static synchronized CryptoManager getInstance(Context context) {
        if (instance == null) {
            instance = new CryptoManager(context);
        }
        return instance;
    }
    
    public KeyPair generateKeyPair() throws Exception {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance(KEY_ALGORITHM, "BC");
        keyGen.initialize(KEY_SIZE, new SecureRandom());
        return keyGen.generateKeyPair();
    }
    
    public String signData(PrivateKey privateKey, String data) throws Exception {
        Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM, "BC");
        signature.initSign(privateKey);
        signature.update(data.getBytes());
        byte[] signedBytes = signature.sign();
        return Base64.encodeToString(signedBytes, Base64.NO_WRAP);
    }
    
    public boolean verifySignature(PublicKey publicKey, String data, String signatureStr) throws Exception {
        Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM, "BC");
        signature.initVerify(publicKey);
        signature.update(data.getBytes());
        byte[] signatureBytes = Base64.decode(signatureStr, Base64.DEFAULT);
        return signature.verify(signatureBytes);
    }
    
    public String encrypt(PublicKey publicKey, String data) throws Exception {
        Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORMATION, "BC");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        byte[] encryptedBytes = cipher.doFinal(data.getBytes());
        return Base64.encodeToString(encryptedBytes, Base64.NO_WRAP);
    }
    
    public String decrypt(PrivateKey privateKey, String encryptedData) throws Exception {
        Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORMATION, "BC");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        byte[] decryptedBytes = cipher.doFinal(Base64.decode(encryptedData, Base64.DEFAULT));
        return new String(decryptedBytes);
    }
    
    public String publicKeyToString(PublicKey publicKey) {
        return Base64.encodeToString(publicKey.getEncoded(), Base64.NO_WRAP);
    }
    
    public PublicKey stringToPublicKey(String keyStr) throws Exception {
        byte[] keyBytes = Base64.decode(keyStr, Base64.DEFAULT);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM, "BC");
        return keyFactory.generatePublic(keySpec);
    }
    
    public String privateKeyToString(PrivateKey privateKey) {
        return Base64.encodeToString(privateKey.getEncoded(), Base64.NO_WRAP);
    }
    
    public PrivateKey stringToPrivateKey(String keyStr) throws Exception {
        byte[] keyBytes = Base64.decode(keyStr, Base64.DEFAULT);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM, "BC");
        return keyFactory.generatePrivate(keySpec);
    }
    
    public String calculateHash(String data) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(data.getBytes());
        return Base64.encodeToString(hash, Base64.NO_WRAP);
    }
    
    public byte[] calculateHash(byte[] data) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        return digest.digest(data);
    }
}
