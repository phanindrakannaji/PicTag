package com.umbc.android.pictag;

import java.util.Date;

/**
 * Created by phani on 5/2/17.
 */

public class Post {
    // user_id, image_url, is_Priced, description, created_date, last_updated_date, status, is_private, watermark_id, category, up_count, down_count
    private int ownerId;
    private int postId;
    private String imageUrl;
    private boolean isPriced;
    private String description;
    private Date createdDate;
    private Date lastUpdatedDate;
    private String status;
    private boolean isPrivate;
    private int watermarkId;
    private int category;
    private int upCount;
    private int downCount;
    private boolean upVote;

    public Post(int ownerId, String imageUrl, boolean isPriced, String description, Date createdDate, Date lastUpdatedDate, String status, boolean isPrivate, int watermarkId, int category, int upCount, int downCount) {
        this.ownerId = ownerId;
        this.imageUrl = imageUrl;
        this.isPriced = isPriced;
        this.description = description;
        this.createdDate = createdDate;
        this.lastUpdatedDate = lastUpdatedDate;
        this.status = status;
        this.isPrivate = isPrivate;
        this.watermarkId = watermarkId;
        this.category = category;
        this.upCount = upCount;
        this.downCount = downCount;
    }

    public int getPostId() {
        return postId;
    }

    public void setPostId(int postId) {
        this.postId = postId;
    }

    public int getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(int ownerId) {
        this.ownerId = ownerId;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public boolean isPriced() {
        return isPriced;
    }

    public void setPriced(boolean priced) {
        isPriced = priced;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public Date getLastUpdatedDate() {
        return lastUpdatedDate;
    }

    public void setLastUpdatedDate(Date lastUpdatedDate) {
        this.lastUpdatedDate = lastUpdatedDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isPrivate() {
        return isPrivate;
    }

    public void setPrivate(boolean aPrivate) {
        isPrivate = aPrivate;
    }

    public int getWatermarkId() {
        return watermarkId;
    }

    public void setWatermarkId(int watermarkId) {
        this.watermarkId = watermarkId;
    }

    public int getCategory() {
        return category;
    }

    public void setCategory(int category) {
        this.category = category;
    }

    public int getUpCount() {
        return upCount;
    }

    public void setUpCount(int upCount) {
        this.upCount = upCount;
    }

    public int getDownCount() {
        return downCount;
    }

    public void setDownCount(int downCount) {
        this.downCount = downCount;
    }

    public boolean isUpVote() {
        return upVote;
    }

    public void setUpVote(boolean upVote) {
        this.upVote = upVote;
    }
}
