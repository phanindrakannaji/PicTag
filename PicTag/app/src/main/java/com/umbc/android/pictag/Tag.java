package com.umbc.android.pictag;

/**
 * Created by phani on 4/30/17.
 */

public class Tag {

    private int userId;
    private int tagId;
    private String tagName;
    private String notify;
    private int minVotes;

    public Tag(int userId, int tagId, String tagName, String notify, int minVotes) {
        this.userId = userId;
        this.tagId = tagId;
        this.tagName = tagName;
        this.notify = notify;
        this.minVotes = minVotes;
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
