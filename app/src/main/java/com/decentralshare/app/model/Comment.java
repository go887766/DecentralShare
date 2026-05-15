package com.decentralshare.app.model;

public class Comment {
    private String id;
    private String postId;
    private String authorAddress;
    private String authorNickname;
    private String content;
    private long createdAt;

    public Comment() {
    }

    public Comment(String postId, String authorAddress, String authorNickname, String content) {
        this.postId = postId;
        this.authorAddress = authorAddress;
        this.authorNickname = authorNickname;
        this.content = content;
        this.createdAt = System.currentTimeMillis();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public String getAuthorAddress() {
        return authorAddress;
    }

    public void setAuthorAddress(String authorAddress) {
        this.authorAddress = authorAddress;
    }

    public String getAuthorNickname() {
        return authorNickname;
    }

    public void setAuthorNickname(String authorNickname) {
        this.authorNickname = authorNickname;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
}
