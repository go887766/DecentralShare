package com.decentralshare.app.p2p;

import android.content.Context;
import android.util.Log;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.*;
import com.decentralshare.app.data.DataManager;
import java.util.*;

public class P2PNetworkManager {
    private static final String TAG = "P2PNetworkManager";
    private static final String SERVICE_ID = "com.decentralshare.app.P2P";
    private static P2PNetworkManager instance;
    
    private Context context;
    private ConnectionsClient connectionsClient;
    private Map<String, Endpoint> connectedEndpoints;
    private Map<String, String> pendingEndpointNames;
    private NetworkCallback callback;
    private String localEndpointId;
    private boolean isDiscovering;
    private boolean isAdvertising;
    
    public interface NetworkCallback {
        void onPeerDiscovered(String endpointId, String endpointName);
        void onPeerConnected(String endpointId);
        void onPeerDisconnected(String endpointId);
        void onDataReceived(String endpointId, byte[] data);
        void onError(String error);
    }
    
    private P2PNetworkManager(Context context) {
        this.context = context.getApplicationContext();
        this.connectionsClient = Nearby.getConnectionsClient(context);
        this.connectedEndpoints = new HashMap<>();
        this.pendingEndpointNames = new HashMap<>();
        this.localEndpointId = "device_" + UUID.randomUUID().toString().substring(0, 8);
    }
    
    public static synchronized P2PNetworkManager getInstance(Context context) {
        if (instance == null) {
            instance = new P2PNetworkManager(context);
        }
        return instance;
    }
    
    public void setCallback(NetworkCallback callback) {
        this.callback = callback;
    }
    
    public void startAdvertising() {
        if (isAdvertising) {
            Log.d(TAG, "Already advertising");
            return;
        }
        
        AdvertisingOptions advertisingOptions = new AdvertisingOptions.Builder()
            .setStrategy(Strategy.P2P_CLUSTER)
            .build();
            
        connectionsClient.startAdvertising(
            localEndpointId,
            SERVICE_ID,
            connectionLifecycleCallback,
            advertisingOptions
        ).addOnSuccessListener(aVoid -> {
            isAdvertising = true;
            Log.d(TAG, "Advertising started successfully");
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Failed to start advertising", e);
            if (callback != null) callback.onError("Advertising failed: " + e.getMessage());
        });
    }
    
    public void stopAdvertising() {
        connectionsClient.stopAdvertising();
        isAdvertising = false;
    }
    
    public void startDiscovery() {
        if (isDiscovering) {
            Log.d(TAG, "Already discovering");
            return;
        }
        
        DiscoveryOptions discoveryOptions = new DiscoveryOptions.Builder()
            .setStrategy(Strategy.P2P_CLUSTER)
            .build();
            
        connectionsClient.startDiscovery(
            SERVICE_ID,
            endpointDiscoveryCallback,
            discoveryOptions
        ).addOnSuccessListener(aVoid -> {
            isDiscovering = true;
            Log.d(TAG, "Discovery started successfully");
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Failed to start discovery", e);
            if (callback != null) callback.onError("Discovery failed: " + e.getMessage());
        });
    }
    
    public void stopDiscovery() {
        connectionsClient.stopDiscovery();
        isDiscovering = false;
    }
    
    public void connectToEndpoint(String endpointId) {
        ConnectionOptions connectionOptions = new ConnectionOptions.Builder()
            .build();
            
        connectionsClient.requestConnection(
            localEndpointId,
            endpointId,
            connectionLifecycleCallback
        ).addOnSuccessListener(aVoid -> {
            Log.d(TAG, "Connection request sent to " + endpointId);
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Failed to connect to " + endpointId, e);
            if (callback != null) callback.onError("Connection failed: " + e.getMessage());
        });
    }
    
    public void sendData(String endpointId, byte[] data) {
        if (!connectedEndpoints.containsKey(endpointId)) {
            Log.e(TAG, "Not connected to " + endpointId);
            return;
        }
        
        Payload payload = Payload.fromBytes(data);
        connectionsClient.sendPayload(endpointId, payload);
    }
    
    public void broadcastData(byte[] data) {
        List<String> endpoints = new ArrayList<>(connectedEndpoints.keySet());
        if (!endpoints.isEmpty()) {
            Payload payload = Payload.fromBytes(data);
            connectionsClient.sendPayload(endpoints, payload);
            Log.d(TAG, "Broadcasted data to " + endpoints.size() + " peers");
        }
    }
    
    public void disconnectFromEndpoint(String endpointId) {
        connectionsClient.disconnectFromEndpoint(endpointId);
        connectedEndpoints.remove(endpointId);
    }
    
    public void stopAll() {
        stopAdvertising();
        stopDiscovery();
        connectionsClient.stopAllEndpoints();
        connectedEndpoints.clear();
    }
    
    public Set<String> getConnectedPeers() {
        return connectedEndpoints.keySet();
    }
    
    public boolean isConnected() {
        return !connectedEndpoints.isEmpty();
    }
    
    private final ConnectionLifecycleCallback connectionLifecycleCallback = 
        new ConnectionLifecycleCallback() {
            @Override
            public void onConnectionInitiated(String endpointId, ConnectionInfo connectionInfo) {
                Log.d(TAG, "Connection initiated with " + endpointId);
                pendingEndpointNames.put(endpointId, connectionInfo.getEndpointName());
                connectionsClient.acceptConnection(endpointId, payloadCallback);
            }
            
            @Override
            public void onConnectionResult(String endpointId, ConnectionResolution result) {
                if (result.getStatus().isSuccess()) {
                    String endpointName = pendingEndpointNames.getOrDefault(endpointId, endpointId);
                    Endpoint endpoint = new Endpoint(endpointId, endpointName);
                    connectedEndpoints.put(endpointId, endpoint);
                    pendingEndpointNames.remove(endpointId);
                    Log.d(TAG, "Connected to " + endpointId);
                    if (callback != null) callback.onPeerConnected(endpointId);
                } else {
                    Log.e(TAG, "Connection failed with " + endpointId);
                    pendingEndpointNames.remove(endpointId);
                    if (callback != null) callback.onError("Connection failed");
                }
            }
            
            @Override
            public void onDisconnected(String endpointId) {
                connectedEndpoints.remove(endpointId);
                pendingEndpointNames.remove(endpointId);
                Log.d(TAG, "Disconnected from " + endpointId);
                if (callback != null) callback.onPeerDisconnected(endpointId);
            }
        };
        
    private final EndpointDiscoveryCallback endpointDiscoveryCallback = 
        new EndpointDiscoveryCallback() {
            @Override
            public void onEndpointFound(String endpointId, DiscoveredEndpointInfo info) {
                Log.d(TAG, "Found endpoint: " + endpointId);
                if (callback != null) {
                    callback.onPeerDiscovered(endpointId, info.getEndpointName());
                }
            }
            
            @Override
            public void onEndpointLost(String endpointId) {
                Log.d(TAG, "Lost endpoint: " + endpointId);
            }
        };
        
    private final PayloadCallback payloadCallback = new PayloadCallback() {
        @Override
        public void onPayloadReceived(String endpointId, Payload payload) {
            if (payload.getType() == Payload.Type.BYTES) {
                byte[] data = payload.asBytes();
                if (data != null) {
                    Log.d(TAG, "Received data from " + endpointId);
                    if (callback != null) {
                        callback.onDataReceived(endpointId, data);
                    }
                }
            }
        }
        
        @Override
        public void onPayloadTransferUpdate(String endpointId, PayloadTransferUpdate update) {
            if (update.getStatus() == PayloadTransferUpdate.Status.SUCCESS) {
                Log.d(TAG, "Payload transfer complete");
            }
        }
    };
    
    private static class Endpoint {
        String id;
        String name;
        
        Endpoint(String id, String name) {
            this.id = id;
            this.name = name;
        }
    }
}
