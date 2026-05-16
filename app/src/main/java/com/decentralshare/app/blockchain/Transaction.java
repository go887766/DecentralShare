package com.decentralshare.app.blockchain;

import com.decentralshare.app.util.CryptoUtil;
import java.io.Serializable;
import java.security.MessageDigest;

public class Transaction implements Serializable {
    public String id;
    public String fromAddress;
    public String toAddress;
    public long amount;
    public String type;
    public String data;
    public long timestamp;
    public String signature;

    public Transaction() {
        this.id = CryptoUtil.generateId();
        this.timestamp = System.currentTimeMillis();
    }

    public Transaction(String from, String to, long amount, String type, String data) {
        this();
        this.fromAddress = from;
        this.toAddress = to;
        this.amount = amount;
        this.type = type;
        this.data = data;
    }

    public String calculateHash() {
        try {
            String data = id + fromAddress + toAddress + amount + type + timestamp;
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data.getBytes());
            return bytesToHex(hash);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }

    public void sign(String privateKey) {
        this.signature = "SIGNED_" + calculateHash();
    }

    public boolean verify() {
        return signature != null && signature.startsWith("SIGNED_");
    }
}
