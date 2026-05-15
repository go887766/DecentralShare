package com.decentralshare.app.blockchain;

import android.util.Log;
import java.io.Serializable;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Block implements Serializable {
    public String hash;
    public String previousHash;
    public List<Transaction> transactions;
    public long timestamp;
    public int nonce;
    public String miner; // 矿工地址
    public int difficulty; // PoW难度
    
    private static final String TAG = "Block";
    public static final int DEFAULT_DIFFICULTY = 3; // 前3位为0

    public Block(String previousHash, String miner) {
        this.previousHash = previousHash;
        this.transactions = new ArrayList<>();
        this.timestamp = System.currentTimeMillis();
        this.nonce = 0;
        this.miner = miner;
        this.difficulty = DEFAULT_DIFFICULTY;
        this.hash = calculateHash();
    }

    public String calculateHash() {
        try {
            String data = previousHash + timestamp + nonce + transactionsToString() + miner + difficulty;
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data.getBytes());
            return bytesToHex(hash);
        } catch (Exception e) {
            Log.e(TAG, "Error calculating hash", e);
            return "";
        }
    }

    private String transactionsToString() {
        StringBuilder sb = new StringBuilder();
        for (Transaction tx : transactions) {
            sb.append(tx.calculateHash());
        }
        return sb.toString();
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }

    // 工作量证明 (PoW) - 挖矿
    public boolean mineBlock(int difficulty) {
        this.difficulty = difficulty;
        String target = new String(new char[difficulty]).replace('\0', '0');
        
        Log.d(TAG, "Start mining block... Difficulty: " + difficulty);
        long startTime = System.currentTimeMillis();
        
        while (!hash.substring(0, difficulty).equals(target)) {
            nonce++;
            hash = calculateHash();
            
            // 进度日志 (每10000次)
            if (nonce % 10000 == 0) {
                Log.d(TAG, "Mining... Nonce: " + nonce + ", Hash: " + hash);
            }
        }
        
        long endTime = System.currentTimeMillis();
        Log.d(TAG, "Block mined! Time: " + (endTime - startTime) + "ms, Nonce: " + nonce + ", Hash: " + hash);
        return true;
    }

    // 权益证明 (PoS) - 根据金币多少决定挖矿概率
    public boolean mineBlockWithPoS(long userCoins, int totalCoins) {
        if (totalCoins == 0) {
            return mineBlock(DEFAULT_DIFFICULTY);
        }
        
        // 金币越多，难度越低（PoS逻辑）
        int posDifficulty = Math.max(1, DEFAULT_DIFFICULTY - (int)(userCoins / 100));
        return mineBlock(posDifficulty);
    }

    public boolean addTransaction(Transaction transaction) {
        if (transaction == null) {
            return false;
        }
        
        if (previousHash != null && !previousHash.equals("0")) {
            if (!transaction.verify()) {
                Log.e(TAG, "Transaction verification failed");
                return false;
            }
        }
        
        transactions.add(transaction);
        Log.d(TAG, "Transaction added to block: " + transaction.type);
        return true;
    }

    public boolean isValid() {
        String target = new String(new char[difficulty]).replace('\0', '0');
        if (!hash.substring(0, difficulty).equals(target)) {
            return false;
        }
        
        if (!hash.equals(calculateHash())) {
            return false;
        }
        
        for (Transaction tx : transactions) {
            if (!tx.verify()) {
                return false;
            }
        }
        
        return true;
    }

    public String getMerkleRoot() {
        if (transactions.isEmpty()) {
            return "";
        }
        
        List<String> hashes = new ArrayList<>();
        for (Transaction tx : transactions) {
            hashes.add(tx.calculateHash());
        }
        
        while (hashes.size() > 1) {
            List<String> newHashes = new ArrayList<>();
            for (int i = 0; i < hashes.size(); i += 2) {
                String left = hashes.get(i);
                String right = (i + 1 < hashes.size()) ? hashes.get(i + 1) : left;
                try {
                    MessageDigest digest = MessageDigest.getInstance("SHA-256");
                    byte[] hash = digest.digest((left + right).getBytes());
                    newHashes.add(bytesToHex(hash));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            hashes = newHashes;
        }
        
        return hashes.get(0);
    }
}
