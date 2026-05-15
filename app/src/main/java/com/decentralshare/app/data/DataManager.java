package com.decentralshare.app.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import com.decentralshare.app.blockchain.Block;
import com.decentralshare.app.blockchain.Blockchain;
import com.decentralshare.app.blockchain.Transaction;
import com.decentralshare.app.crypto.CryptoManager;
import com.decentralshare.app.model.Comment;
import com.decentralshare.app.model.Post;
import com.decentralshare.app.model.User;
import com.decentralshare.app.p2p.P2PNetworkManager;
import com.decentralshare.app.storage.DistributedStorage;
import com.decentralshare.app.util.CryptoUtil;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.security.KeyPair;
import java.util.ArrayList;
import java.util.List;

public class DataManager {
    private static final String TAG = "DataManager";
    private static final String PREFS_NAME = "DecentralSharePrefs";
    private static final String KEY_USER = "current_user";
    private static final String KEY_POSTS = "posts";
    private static final String KEY_USERS = "users";
    private static final String KEY_PUBLIC_KEY = "public_key";
    private static final String KEY_PRIVATE_KEY = "private_key";
    
    private static DataManager instance;
    private SharedPreferences prefs;
    private Gson gson;
    private User currentUser;
    private List<Post> posts;
    private List<User> users;
    private CryptoManager cryptoManager;
    private DistributedStorage storage;
    private P2PNetworkManager p2pManager;
    private Blockchain blockchain;
    
    private DataManager(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
        cryptoManager = CryptoManager.getInstance(context);
        storage = DistributedStorage.getInstance(context);
        p2pManager = P2PNetworkManager.getInstance(context);
        blockchain = Blockchain.getInstance(context);
        loadData();
        initP2PCallback();
    }
    
    public static synchronized DataManager getInstance(Context context) {
        if (instance == null) {
            instance = new DataManager(context.getApplicationContext());
        }
        return instance;
    }
    
    private void initP2PCallback() {
        p2pManager.setCallback(new P2PNetworkManager.NetworkCallback() {
            @Override
            public void onPeerDiscovered(String endpointId, String endpointName) {
                Log.d(TAG, "Peer discovered: " + endpointName);
            }
            
            @Override
            public void onPeerConnected(String endpointId) {
                Log.d(TAG, "Peer connected: " + endpointId);
                syncDataWithPeer(endpointId);
            }
            
            @Override
            public void onPeerDisconnected(String endpointId) {
                Log.d(TAG, "Peer disconnected: " + endpointId);
            }
            
            @Override
            public void onDataReceived(String endpointId, byte[] data) {
                handleIncomingData(endpointId, data);
            }
            
            @Override
            public void onError(String error) {
                Log.e(TAG, "P2P Error: " + error);
            }
        });
    }
    
    private void handleIncomingData(String endpointId, byte[] data) {
        try {
            String json = new String(data);
            NetworkMessage message = gson.fromJson(json, NetworkMessage.class);
            
            if (message.type == NetworkMessage.TYPE_POST) {
                Post post = gson.fromJson(message.payload, Post.class);
                addPostFromPeer(post);
            } else if (message.type == NetworkMessage.TYPE_SYNC_REQUEST) {
                sendDataToPeer(endpointId);
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to handle incoming data", e);
        }
    }
    
    private void addPostFromPeer(Post post) {
        boolean exists = false;
        for (Post p : posts) {
            if (p.getId() != null && p.getId().equals(post.getId())) {
                exists = true;
                break;
            }
        }
        
        if (!exists) {
            posts.add(0, post);
            saveData();
            Log.d(TAG, "Added post from peer: " + post.getTitle());
        }
    }
    
    private void syncDataWithPeer(String endpointId) {
        NetworkMessage request = new NetworkMessage();
        request.type = NetworkMessage.TYPE_SYNC_REQUEST;
        request.payload = "sync";
        
        String json = gson.toJson(request);
        p2pManager.sendData(endpointId, json.getBytes());
        Log.d(TAG, "Sync request sent to " + endpointId);
    }
    
    private void sendDataToPeer(String endpointId) {
        for (Post post : posts) {
            NetworkMessage message = new NetworkMessage();
            message.type = NetworkMessage.TYPE_POST;
            message.payload = gson.toJson(post);
            
            String json = gson.toJson(message);
            p2pManager.sendData(endpointId, json.getBytes());
        }
        Log.d(TAG, "Sent " + posts.size() + " posts to peer");
    }
    
    public void startP2PNetwork() {
        p2pManager.startAdvertising();
        p2pManager.startDiscovery();
        Log.d(TAG, "P2P network started");
    }
    
    public void stopP2PNetwork() {
        p2pManager.stopAll();
        Log.d(TAG, "P2P network stopped");
    }
    
    public P2PNetworkManager getP2PManager() {
        return p2pManager;
    }
    
    public DistributedStorage getStorage() {
        return storage;
    }
    
    public Blockchain getBlockchain() {
        return blockchain;
    }
    
    // 挖矿方法
    public boolean startMining() {
        if (currentUser == null) {
            return false;
        }
        
        Block minedBlock = blockchain.minePendingTransactions(currentUser.getAddress());
        if (minedBlock != null) {
            // 更新用户金币（挖矿奖励）
            currentUser.setCoins(currentUser.getCoins() + Blockchain.MINING_REWARD);
            updateUser(currentUser);
            return true;
        }
        
        return false;
    }
    
    private void loadData() {
        String userJson = prefs.getString(KEY_USER, null);
        if (userJson != null) {
            currentUser = gson.fromJson(userJson, User.class);
        }
        
        String postsJson = prefs.getString(KEY_POSTS, null);
        Type postListType = new TypeToken<List<Post>>() {}.getType();
        posts = postsJson != null ? gson.fromJson(postsJson, postListType) : new ArrayList<>();
        
        String usersJson = prefs.getString(KEY_USERS, null);
        Type userListType = new TypeToken<List<User>>() {}.getType();
        users = usersJson != null ? gson.fromJson(usersJson, userListType) : new ArrayList<>();
    }
    
    private void saveData() {
        SharedPreferences.Editor editor = prefs.edit();
        if (currentUser != null) {
            editor.putString(KEY_USER, gson.toJson(currentUser));
        }
        editor.putString(KEY_POSTS, gson.toJson(posts));
        editor.putString(KEY_USERS, gson.toJson(users));
        editor.apply();
    }
    
    public User registerUser(String nickname, String password) {
        String address = CryptoUtil.generateUserAddress();
        String passwordHash = CryptoUtil.hashPassword(password);
        User user = new User(address, passwordHash, nickname);
        
        try {
            KeyPair keyPair = cryptoManager.generateKeyPair();
            String publicKey = cryptoManager.publicKeyToString(keyPair.getPublic());
            String privateKey = cryptoManager.privateKeyToString(keyPair.getPrivate());
            
            prefs.edit()
                .putString(KEY_PUBLIC_KEY, publicKey)
                .putString(KEY_PRIVATE_KEY, privateKey)
                .apply();
        } catch (Exception e) {
            Log.e(TAG, "Failed to generate keys", e);
        }
        
        users.add(user);
        currentUser = user;
        saveData();
        return user;
    }
    
    public User login(String address, String password) {
        String passwordHash = CryptoUtil.hashPassword(password);
        for (User user : users) {
            if (user.getAddress().equals(address) && user.getPasswordHash().equals(passwordHash)) {
                currentUser = user;
                saveData();
                startP2PNetwork();
                return user;
            }
        }
        return null;
    }
    
    public void logout() {
        stopP2PNetwork();
        currentUser = null;
        prefs.edit().remove(KEY_USER).apply();
    }
    
    public User getCurrentUser() {
        return currentUser;
    }
    
    public boolean isLoggedIn() {
        return currentUser != null;
    }
    
    public Post createPost(String title, String content, List<String> images, List<String> videos) {
        Post post = new Post();
        post.setId(CryptoUtil.generateId());
        post.setAuthorAddress(currentUser.getAddress());
        post.setAuthorNickname(currentUser.getNickname());
        post.setTitle(title);
        post.setContent(content);
        post.setImageUrls(images != null ? images : new ArrayList<>());
        post.setVideoUrls(videos != null ? videos : new ArrayList<>());
        posts.add(0, post);
        
        // 区块链交易：发布帖子奖励
        Transaction rewardTx = new Transaction("SYSTEM", currentUser.getAddress(), 10, "POST_REWARD", post.getId());
        blockchain.addTransaction(rewardTx);
        
        currentUser.setCoins(currentUser.getCoins() + 10);
        updateUser(currentUser);
        
        saveData();
        
        storage.storePost(post);
        
        if (p2pManager.isConnected()) {
            NetworkMessage message = new NetworkMessage();
            message.type = NetworkMessage.TYPE_POST;
            message.payload = gson.toJson(post);
            p2pManager.broadcastData(gson.toJson(message).getBytes());
        }
        
        return post;
    }
    
    public List<Post> getPosts() {
        return new ArrayList<>(posts);
    }
    
    public Post getPost(String postId) {
        for (Post post : posts) {
            if (post.getId().equals(postId)) {
                return post;
            }
        }
        return null;
    }
    
    public void likePost(Post post) {
        if (!post.isLikedByMe()) {
            post.setLikes(post.getLikes() + 1);
            post.setLikedByMe(true);
            
            // 区块链交易：点赞奖励
            Transaction likeTx = new Transaction("SYSTEM", post.getAuthorAddress(), 1, "LIKE_REWARD", post.getId());
            blockchain.addTransaction(likeTx);
            
            User author = findUserByAddress(post.getAuthorAddress());
            if (author != null) {
                author.setCoins(author.getCoins() + 1);
                updateUser(author);
            }
            
            saveData();
        }
    }
    
    public void unlikePost(Post post) {
        if (post.isLikedByMe()) {
            post.setLikes(post.getLikes() - 1);
            post.setLikedByMe(false);
            saveData();
        }
    }
    
    public Comment addComment(Post post, String content) {
        Comment comment = new Comment(
            post.getId(),
            currentUser.getAddress(),
            currentUser.getNickname(),
            content
        );
        comment.setId(CryptoUtil.generateId());
        post.getComments().add(comment);
        
        // 区块链交易：评论奖励
        Transaction commentTx = new Transaction("SYSTEM", currentUser.getAddress(), 2, "COMMENT_REWARD", post.getId());
        blockchain.addTransaction(commentTx);
        
        currentUser.setCoins(currentUser.getCoins() + 2);
        updateUser(currentUser);
        
        saveData();
        return comment;
    }
    
    private User findUserByAddress(String address) {
        for (User user : users) {
            if (user.getAddress().equals(address)) {
                return user;
            }
        }
        return null;
    }
    
    private void updateUser(User user) {
        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).getAddress().equals(user.getAddress())) {
                users.set(i, user);
                break;
            }
        }
        if (currentUser != null && currentUser.getAddress().equals(user.getAddress())) {
            currentUser = user;
        }
        saveData();
    }
    
    public List<Post> getMyPosts() {
        List<Post> myPosts = new ArrayList<>();
        if (currentUser == null) return myPosts;
        for (Post post : posts) {
            if (post.getAuthorAddress().equals(currentUser.getAddress())) {
                myPosts.add(post);
            }
        }
        return myPosts;
    }
    
    public static class NetworkMessage {
        public static final int TYPE_POST = 1;
        public static final int TYPE_SYNC_REQUEST = 2;
        
        public int type;
        public String payload;
    }
}
