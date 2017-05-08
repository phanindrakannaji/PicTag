package com.umbc.android.pictag;

import com.plumillonforge.android.chipview.Chip;

/**
 * Created by phani on 5/8/17.
 */

public class DisplayTag implements Chip {
    private String mName;
    private int mType = 0;

    public DisplayTag(String name, int type) {
        this(name);
        mType = type;
    }

    public DisplayTag(String name) {
        mName = name;
    }

    @Override
    public String getText() {
        return mName;
    }

    public int getType() {
        return mType;
    }
}
