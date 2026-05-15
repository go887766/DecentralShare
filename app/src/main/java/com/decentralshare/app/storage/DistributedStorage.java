package com.decentralshare.app.storage;

import android.content.Context;
import android.util.Log;
import com.decentralshare.app.data.DataManager;
import com.decentralshare.app.model.Post;
import com.decentralshare.app.util.CryptoUtil;
import com.google.gson.Gson;
import java.io.*;
import java.security.MessageDigest;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class DistributedStorage {
    private static final String TAG = "DistributedStorage";
    private static DistributedStorage instance;
    private Context context;
    
    private Map<String, byte[]> localStore;
    private Map<String, List<String>> dhtRoutingTable;
    private File storageDir;
    private Gson gson;
    
    private DistributedStorage(Context context) {
        this.context = context.getApplicationContext();
        this.localStore = new ConcurrentHashMap<>();
        this.dhtRoutingTable = new ConcurrentHashMap<>();
        this.gson = new Gson();
        initStorage();
    }
    
    public static synchronized DistributedStorage getInstance(Context context) {
        if (instance == null) {
            instance = new DistributedStorage(context);
        }
        return instance;
    }
    
    private void initStorage() {
        storageDir = new File(context.getFilesDir(), "decentral_storage");
        if (!storageDir.exists()) {
            storageDir.mkdirs();
        }
        loadLocalStore();
    }
    
    private void loadLocalStore() {
        File[] files = storageDir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    try {
                        byte[] data = readFile(file);
                        localStore.put(file.getName(), data);
                    } catch (IOException e) {
                        Log.e(TAG, "Failed to load file: " + file.getName(), e);
                    }
                }
            }
        }
    }
    
    public String storeData(byte[] data) {
        String hash = calculateHash(data);
        localStore.put(hash, data);
        saveToFile(hash, data);
        Log.d(TAG, "Data stored locally with hash: " + hash);
        return hash;
    }
    
    public String storePost(Post post) {
        String json = gson.toJson(post);
        return storeData(json.getBytes());
    }
    
    public byte[] retrieveData(String hash) {
        if (localStore.containsKey(hash)) {
            return localStore.get(hash);
        }
        Log.d(TAG, "Data not found locally, will query network: " + hash);
        return null;
    }
    
    public Post retrievePost(String hash) {
        byte[] data = retrieveData(hash);
        if (data != null) {
            try {
                return gson.fromJson(new String(data), Post.class);
            } catch (Exception e) {
                Log.e(TAG, "Failed to parse post", e);
            }
        }
        return null;
    }
    
    public void addToDHT(String key, String peerId) {
        if (!dhtRoutingTable.containsKey(key)) {
            dhtRoutingTable.put(key, new ArrayList<>());
        }
        List<String> peers = dhtRoutingTable.get(key);
        if (!peers.contains(peerId)) {
            peers.add(peerId);
        }
        Log.d(TAG, "Added to DHT: " + key + " -> " + peerId);
    }
    
    public List<String> findInDHT(String key) {
        return dhtRoutingTable.getOrDefault(key, new ArrayList<>());
    }
    
    private String calculateHash(byte[] data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data);
            return bytesToHex(hash);
        } catch (Exception e) {
            Log.e(TAG, "Failed to calculate hash", e);
            return UUID.randomUUID().toString();
        }
    }
    
    private String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }
    
    private void saveToFile(String hash, byte[] data) {
        File file = new File(storageDir, hash);
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(data);
            fos.flush();
        } catch (IOException e) {
            Log.e(TAG, "Failed to save file", e);
        }
    }
    
    private byte[] readFile(File file) throws IOException {
        try (FileInputStream fis = new FileInputStream(file)) {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len;
            while ((len = fis.read(buffer)) != -1) {
                bos.write(buffer, 0, len);
            }
            return bos.toByteArray();
        }
    }
    
    public Set<String> getAllLocalHashes() {
        return localStore.keySet();
    }
    
    public void syncWithPeers() {
        DataManager dataManager = DataManager.getInstance(context);
        List<Post> posts = dataManager.getPosts();
        for (Post post : posts) {
            if (post.getId() != null) {
                String hash = storePost(post);
                addToDHT(hash, "local");
            }
        }
        Log.d(TAG, "Synced " + posts.size() + " posts to distributed storage");
    }
    
    public void clear() {
        localStore.clear();
        dhtRoutingTable.clear();
        File[] files = storageDir.listFiles();
        if (files != null) {
            for (File file : files) {
                file.delete();
            }
        }
    }
}
