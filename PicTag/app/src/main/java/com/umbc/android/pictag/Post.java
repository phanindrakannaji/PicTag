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
    private String price;
    private String description;
    private Date createdDate;
    private Date lastUpdatedDate;
    private String status;
    private boolean isPrivate;
    private String watermark;
    private int category;
    private int upCount;
    private int downCount;
    private boolean upVote;

    public Post(int ownerId, String imageUrl, boolean isPriced, String price, String description,
                Date createdDate, Date lastUpdatedDate, String status, boolean isPrivate,
                String watermark, int category, int upCount, int downCount) {
        this.ownerId = ownerId;
        this.imageUrl = imageUrl;
        this.isPriced = isPriced;
        this.description = description;
        this.createdDate = createdDate;
        this.lastUpdatedDate = lastUpdatedDate;
        this.status = status;
        this.isPrivate = isPrivate;
        this.watermark = watermark;
        this.category = category;
        this.upCount = upCount;
        this.downCount = downCount;
    }

    public Post(int ownerId, int postId, String imageUrl, boolean isPriced, String price, String description,
                Date createdDate, Date lastUpdatedDate, String status, boolean isPrivate, String watermark,
                int category, int upCount, int downCount, boolean upVote) {
        this.ownerId = ownerId;
        this.postId = postId;
        this.imageUrl = imageUrl;
        this.isPriced = isPriced;
        this.description = description;
        this.createdDate = createdDate;
        this.lastUpdatedDate = lastUpdatedDate;
        this.price = price;
        this.status = status;
        this.isPrivate = isPrivate;
        this.watermark = watermark;
        this.category = category;
        this.upCount = upCount;
        this.downCount = downCount;
        this.upVote = upVote;
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

    public String getWatermark() {
        return watermark;
    }

    public void setWatermark(String watermark) {
        this.watermark = watermark;
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

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }
}
