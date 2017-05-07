package com.umbc.android.pictag;

import java.util.List;

/**
 * Created by phani on 4/30/17.
 */

class Tag {

    private int userId;
    private int tagId;
    private String tagName;
    private String notify;
    private int minVotes;
    private int count;
    private int currentIndex;
    private List<Post> posts;

    Tag(int userId, int tagId, String tagName, String notify, int minVotes, int count, int currentIndex, List<Post> posts) {
        this.userId = userId;
        this.tagId = tagId;
        this.tagName = tagName;
        this.notify = notify;
        this.minVotes = minVotes;
        this.count = count;
        this.currentIndex = currentIndex;
        this.posts = posts;
    }

    Tag(int userId, int tagId, String tagName, String notify, int minVotes) {
        this.userId = userId;
        this.tagId = tagId;
        this.tagName = tagName;
        this.notify = notify;
        this.minVotes = minVotes;
    }

    public int getCurrentIndex() {
        return currentIndex;
    }

    public void setCurrentIndex(int currentIndex) {
        this.currentIndex = currentIndex;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public List<Post> getPosts() {
        return posts;
    }

    public void setPosts(List<Post> posts) {
        this.posts = posts;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getTagId() {
        return tagId;
    }

    public void setTagId(int tagId) {
        this.tagId = tagId;
    }

    public String getTagName() {
        return tagName;
    }

    public void setTagName(String tagName) {
        this.tagName = tagName;
    }

    public String getNotify() {
        return notify;
    }

    public void setNotify(String notify) {
        this.notify = notify;
    }

    public int getMinVotes() {
        return minVotes;
    }

    public void setMinVotes(int minVotes) {
        this.minVotes = minVotes;
    }
}
