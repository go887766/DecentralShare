package com.decentralshare.app.blockchain;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import com.decentralshare.app.data.DataManager;
import com.decentralshare.app.model.User;
import com.decentralshare.app.util.CryptoUtil;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Blockchain {
    private static final String TAG = "Blockchain";
    private static final String PREFS_NAME = "BlockchainPrefs";
    private static final String KEY_CHAIN = "chain";
    private static final String KEY_UTXO = "utxo";
    
    private static Blockchain instance;
    private List<Block> chain;
    private List<Transaction> pendingTransactions;
    private Map<String, Long> balances; // 账户余额
    private Context context;
    private DataManager dataManager;
    private Gson gson;
    
    // 共识参数
    public static final int BLOCK_SIZE = 5; // 每区块最多5个交易
    public static final long MINING_REWARD = 25; // 挖矿奖励
    public static final int MIN_DIFFICULTY = 2;
    public static final int MAX_DIFFICULTY = 5;
    
    private Blockchain(Context context) {
        this.context = context.getApplicationContext();
        this.dataManager = DataManager.getInstance(context);
        this.gson = new Gson();
        this.chain = new ArrayList<>();
        this.pendingTransactions = new ArrayList<>();
        this.balances = new HashMap<>();
        loadBlockchain();
    }
    
    public static synchronized Blockchain getInstance(Context context) {
        if (instance == null) {
            instance = new Blockchain(context);
        }
        return instance;
    }
    
    private void loadBlockchain() {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String chainJson = prefs.getString(KEY_CHAIN, null);
        
        if (chainJson != null) {
            Type listType = new TypeToken<List<Block>>(){}.getType();
            chain = gson.fromJson(chainJson, listType);
            if (chain == null) chain = new ArrayList<>();
        }
        
        if (chain.isEmpty()) {
            createGenesisBlock();
        }
        
        recalculateBalances();
    }
    
    private void saveBlockchain() {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_CHAIN, gson.toJson(chain)).apply();
    }
    
    private void createGenesisBlock() {
        Block genesis = new Block("0", "GENESIS");
        genesis.mineBlock(MIN_DIFFICULTY);
        chain.add(genesis);
        saveBlockchain();
        Log.d(TAG, "Genesis block created!");
    }
    
    public Block getLatestBlock() {
        return chain.get(chain.size() - 1);
    }
    
    // 添加交易到待处理池
    public boolean addTransaction(Transaction transaction) {
        if (transaction.fromAddress != null && !transaction.fromAddress.equals("SYSTEM")) {
            long fromBalance = balances.getOrDefault(transaction.fromAddress, 0L);
            if (fromBalance < transaction.amount) {
                Log.e(TAG, "Insufficient balance!");
                return false;
            }
        }
        
        transaction.sign(""); // 简化签名
        pendingTransactions.add(transaction);
        Log.d(TAG, "Transaction added to mempool: " + transaction.type);
        
        // 如果交易足够，触发挖矿
        if (pendingTransactions.size() >= BLOCK_SIZE) {
            User user = dataManager.getCurrentUser();
            if (user != null) {
                minePendingTransactions(user.getAddress());
            }
        }
        
        return true;
    }
    
    // 挖矿（PoW + PoS混合共识）
    public Block minePendingTransactions(String minerAddress) {
        if (pendingTransactions.isEmpty()) {
            Log.d(TAG, "No transactions to mine");
            return null;
        }
        
        User user = dataManager.getCurrentUser();
        long userCoins = (user != null) ? user.getCoins() : 0;
        int totalCoins = calculateTotalCoins();
        
        // 创建新区块
        Block newBlock = new Block(getLatestBlock().hash, minerAddress);
        
        // 添加交易（最多BLOCK_SIZE个）
        int count = Math.min(pendingTransactions.size(), BLOCK_SIZE);
        for (int i = 0; i < count; i++) {
            newBlock.addTransaction(pendingTransactions.get(i));
        }
        
        // 添加挖矿奖励交易
        Transaction rewardTx = new Transaction("SYSTEM", minerAddress, MINING_REWARD, "MINING_REWARD", "");
        newBlock.addTransaction(rewardTx);
        
        // 混合共识：PoW + PoS
        boolean mined;
        if (userCoins > 0) {
            mined = newBlock.mineBlockWithPoS(userCoins, totalCoins);
        } else {
            mined = newBlock.mineBlock(DEFAULT_DIFFICULTY);
        }
        
        if (mined && newBlock.isValid()) {
            chain.add(newBlock);
            
            // 移除已打包的交易
            for (int i = 0; i < count; i++) {
                pendingTransactions.remove(0);
            }
            
            recalculateBalances();
            saveBlockchain();
            
            Log.d(TAG, "Block mined successfully! Height: " + chain.size());
            return newBlock;
        }
        
        return null;
    }
    
    // PoS + PoW混合共识验证
    public boolean isChainValid() {
        for (int i = 1; i < chain.size(); i++) {
            Block current = chain.get(i);
            Block previous = chain.get(i - 1);
            
            if (!current.isValid()) {
                return false;
            }
            
            if (!current.previousHash.equals(previous.hash)) {
                return false;
            }
            
            // 验证难度适应
            int expectedDifficulty = calculateDifficulty(i);
            if (current.difficulty != expectedDifficulty) {
                Log.w(TAG, "Difficulty mismatch! Expected: " + expectedDifficulty + ", Actual: " + current.difficulty);
            }
        }
        return true;
    }
    
    // 根据出块时间动态调整难度
    private int calculateDifficulty(int height) {
        if (height <= 10) {
            return MIN_DIFFICULTY;
        }
        
        long totalTime = chain.get(height - 1).timestamp - chain.get(height - 11).timestamp;
        long avgTime = totalTime / 10; // 平均每10块的出块时间
        
        int currentDifficulty = chain.get(height - 1).difficulty;
        
        if (avgTime < 30000) { // 小于30秒，增加难度
            return Math.min(currentDifficulty + 1, MAX_DIFFICULTY);
        } else if (avgTime > 120000) { // 大于2分钟，降低难度
            return Math.max(currentDifficulty - 1, MIN_DIFFICULTY);
        }
        
        return currentDifficulty;
    }
    
    private void recalculateBalances() {
        balances.clear();
        
        for (Block block : chain) {
            for (Transaction tx : block.transactions) {
                if (tx.fromAddress != null && !tx.fromAddress.equals("SYSTEM")) {
                    long current = balances.getOrDefault(tx.fromAddress, 0L);
                    balances.put(tx.fromAddress, current - tx.amount);
                }
                
                long toBalance = balances.getOrDefault(tx.toAddress, 0L);
                balances.put(tx.toAddress, toBalance + tx.amount);
            }
        }
    }
    
    private int calculateTotalCoins() {
        int total = 0;
        for (long balance : balances.values()) {
            total += balance;
        }
        return total;
    }
    
    public long getBalance(String address) {
        return balances.getOrDefault(address, 0L);
    }
    
    public Map<String, Long> getAllBalances() {
        return new HashMap<>(balances);
    }
    
    public int getChainHeight() {
        return chain.size();
    }
    
    public List<Block> getChain() {
        return new ArrayList<>(chain);
    }
    
    public Block getBlock(int index) {
        if (index >= 0 && index < chain.size()) {
            return chain.get(index);
        }
        return null;
    }
    
    // 接受新链（分叉解决）
    public boolean replaceChain(List<Block> newChain) {
        if (newChain.size() > chain.size() && isChainValid(newChain)) {
            Log.d(TAG, "Replacing chain with longer valid chain");
            chain = new ArrayList<>(newChain);
            recalculateBalances();
            saveBlockchain();
            return true;
        }
        Log.d(TAG, "New chain is not longer or invalid");
        return false;
    }
    
    private boolean isChainValid(List<Block> chain) {
        Block genesis = chain.get(0);
        if (!genesis.previousHash.equals("0")) {
            return false;
        }
        
        for (int i = 1; i < chain.size(); i++) {
            Block current = chain.get(i);
            Block previous = chain.get(i - 1);
            
            if (!current.isValid()) {
                return false;
            }
            
            if (!current.previousHash.equals(previous.hash)) {
                return false;
            }
        }
        return true;
    }
}
