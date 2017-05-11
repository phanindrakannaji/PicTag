package com.umbc.android.pictag;

import java.util.List;

/**
 * Created by phani on 5/10/17.
 */

public class TagImages {
    String tagId;
    String tagName;
    List<String> imageUrls;

    public TagImages(String tagId, String tagName, List<String> imageUrls) {
        this.tagId = tagId;
        this.tagName = tagName;
        this.imageUrls = imageUrls;
    }

    public String getTagId() {
        return tagId;
    }

    public void setTagId(String tagId) {
        this.tagId = tagId;
    }

    public String getTagName() {
        return tagName;
    }

    public void setTagName(String tagName) {
        this.tagName = tagName;
    }

    public List<String> getImageUrls() {
        return imageUrls;
    }

    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls;
    }

    public TagImages() {
    }
}
