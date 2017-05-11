package com.umbc.android.pictag.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.GridView;

import com.daimajia.slider.library.Indicators.PagerIndicator;
import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.daimajia.slider.library.SliderTypes.TextSliderView;
import com.umbc.android.pictag.PostsOfTagActivity;
import com.umbc.android.pictag.TagImages;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by phani on 5/10/17.
 */

public class ImageAdapter extends BaseAdapter implements BaseSliderView.OnSliderClickListener {
    private final int screenWidth;
    private final int screenHeight;
    private Context mContext;
    private List<TagImages> tagImages;
    private List<SliderLayout.Transformer> transformerList;

    public ImageAdapter(Context c, List<TagImages> tagImages ) {
        mContext = c;
        this.tagImages = tagImages;
        transformerList = new ArrayList<>();
        transformerList.add(SliderLayout.Transformer.Accordion);
        transformerList.add(SliderLayout.Transformer.DepthPage);
        transformerList.add(SliderLayout.Transformer.Stack);
        transformerList.add(SliderLayout.Transformer.Tablet);

        Display display = ((WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        screenWidth = size.x;
        screenHeight = size.y;
    }

    public int getCount() {
        return tagImages.size();
    }

    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return 0;
    }

    // create a new ImageView for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {

        SliderLayout sliderLayout;
        if (convertView == null) {
            sliderLayout = new SliderLayout(mContext);
            sliderLayout.setIndicatorVisibility(PagerIndicator.IndicatorVisibility.Invisible);
            int res = 0;
            if (screenHeight > screenWidth){
                res = Math.round(screenWidth/2)-2;
            } else{
                res = Math.round(screenHeight/2)-2;
            }
            sliderLayout.setLayoutParams(new GridView.LayoutParams(res, res));
            SliderLayout.Transformer currentTransformer = transformerList.get(ThreadLocalRandom.current().nextInt(0, transformerList.size()));
            sliderLayout.setPresetTransformer(currentTransformer);
            Long currentDuration = (long) ThreadLocalRandom.current().nextInt(2000, 6000);
            sliderLayout.setDuration(currentDuration);
            sliderLayout.setPadding(2,2,2,2);
        } else {
            sliderLayout = (SliderLayout) convertView;
        }
        String tagName = tagImages.get(position).getTagName();
        if (tagImages!=null && tagImages.size() > 0 && tagImages.get(position).getImageUrls().size() > 0) {
            for (String imageUrl : tagImages.get(position).getImageUrls()) {
                TextSliderView textSliderView = new TextSliderView(mContext);
                textSliderView.image(imageUrl).description(tagName);
                textSliderView.setOnSliderClickListener(this);
                sliderLayout.addSlider(textSliderView);
            }
            sliderLayout.setTag(tagImages.get(position).getTagId());
        }
        return sliderLayout;
    }

    @Override
    public void onSliderClick(BaseSliderView slider) {
        String tagName = slider.getDescription();
        Log.d("TEST", tagName);
        Intent myIntent = new Intent(mContext, PostsOfTagActivity.class);
        myIntent.putExtra("selected_tag_id", tagName);
        myIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(myIntent);
    }
}