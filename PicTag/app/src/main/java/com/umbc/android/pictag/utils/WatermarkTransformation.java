package com.umbc.android.pictag.utils;

/**
 * Created by phani on 5/9/17.
 */

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.v7.graphics.Palette;

import com.squareup.picasso.Transformation;

/**
 * Created by oded on 9/15/15.
 * Watermark Transformation for the Picasso image loading library (https://github.com/square/picasso).
 * The transformation will add the text you provide in the constructor to the image.
 * This was created to be implemented in http://wheredatapp.com, android's greatest search engine.
 */
public class WatermarkTransformation implements Transformation {

    private String waterMark;
    private static final int PADDING = 25;
    float density;
    int color;

    public WatermarkTransformation(String waterMark, float density, int color) {
        this.waterMark = waterMark;
        this.density = density;
        this.color = color;
    }

    @Override
    public Bitmap transform(Bitmap source) {
        //choose the color of the text based on the color contents of the image
        Palette palette = Palette.generate(source);

        Bitmap workingBitmap = Bitmap.createBitmap(source);
        Bitmap mutableBitmap = workingBitmap.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(mutableBitmap);

        Paint paint2 = new Paint();
        paint2.setColor(color);
        paint2.setTextSize(50*density);
        paint2.setTextAlign(Paint.Align.RIGHT);
        paint2.setAntiAlias(true);
        paint2.setAlpha(50);
        Rect textBounds = new Rect();
        paint2.getTextBounds(waterMark, 0, waterMark.length(), textBounds);
        int x = source.getHeight()/2;
        int y = source.getWidth()/2;


        canvas.drawText(waterMark, x, y, paint2);
        source.recycle();

        return mutableBitmap;
    }

    @Override
    public String key() {
        return "WaterMarkTransformation-" + waterMark;
    }
}
