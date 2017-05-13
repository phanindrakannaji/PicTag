package com.umbc.android.pictag;

/**
 * Created by phani on 5/10/17.
 */

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class SolventViewHolders extends RecyclerView.ViewHolder {

    public ImageView postImage;
    public TextView postDescription;
    public Button postUpVote;
    public TextView postUpCount;
    public TextView postPrice;
    public Button postPurchasePic;

    public SolventViewHolders(View itemView) {
        super(itemView);
        postImage = (ImageView) itemView.findViewById(R.id.post_image);
        postDescription = (TextView) itemView.findViewById(R.id.post_description);
        postUpVote = (Button) itemView.findViewById(R.id.post_upvote);
        postUpCount = (TextView) itemView.findViewById(R.id.post_upcount);
        postPrice = (TextView) itemView.findViewById(R.id.post_price);
        postPurchasePic = (Button) itemView.findViewById(R.id.post_purchasePic);
    }
}