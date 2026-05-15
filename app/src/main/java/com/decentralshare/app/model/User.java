package com.decentralshare.app.model;

public class User {
    private String address;
    private String passwordHash;
    private String nickname;
    private long coins;
    private long createdAt;

    public User() {
    }

    public User(String address, String passwordHash, String nickname) {
        this.address = address;
        this.passwordHash = passwordHash;
        this.nickname = nickname;
        this.coins = 100;
        this.createdAt = System.currentTimeMillis();
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public long getCoins() {
        return coins;
    }

    public void setCoins(long coins) {
        this.coins = coins;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
}
