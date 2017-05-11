package com.umbc.android.pictag;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageView;

import com.umbc.android.pictag.adapter.ImageAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


/**
 * <p>A fragment that shows a list of items as a modal bottom sheet.</p>
 * <p>You can show this modal bottom sheet from your activity like this:</p>
 * <pre>
 *     NewsFeedFragment.newInstance(30).show(getSupportFragmentManager(), "dialog");
 * </pre>
 * <p>You activity (or fragment) needs to implement {@link NewsFeedFragment.Listener}.</p>
 */
public class NewsFeedFragment extends BottomSheetDialogFragment {

    // TODO: Customize parameter argument names
    private static final String ARG_ITEM_COUNT = "item_count";
    private static final String TAG = "NEWSFEEDFRAGMENT";
    private Listener mListener;

    private View parentView;
    List<TagImages> tagImages;
    private UserProfile userProfile;
    private GridView gridview;

    // TODO: Customize parameters
    public static NewsFeedFragment newInstance(int itemCount) {
        final NewsFeedFragment fragment = new NewsFeedFragment();
        final Bundle args = new Bundle();
        args.putInt(ARG_ITEM_COUNT, itemCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        parentView = inflater.inflate(R.layout.fragment_news_feed, container, false);
        userProfile = ((HomeActivity) getActivity()).getUserProfile();
        gridview = (GridView) parentView.findViewById(R.id.gridview);

        String[] input = new String[1];
        input[0] = userProfile.getId();
        GetPostsTask getPostsTask = new GetPostsTask();
        getPostsTask.execute(input);

        return parentView;
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    public interface Listener {
        void onItemClicked(int position);
    }

    private class ImageLoader extends AsyncTask<Void, Integer, Bitmap>{

        String imageUrl;
        ImageView imageView;

        ImageLoader(String imageUrl, ImageView imageView) {
            this.imageUrl = imageUrl;
            this.imageView = imageView;
        }

        @Override
        protected Bitmap doInBackground(Void... voids) {
            Bitmap bitmap = null;
            InputStream in = null; //Reads whatever content found with the given URL Asynchronously And returns.
            try {
                Log.d("URL: ", imageUrl);
                in = (InputStream) new URL(imageUrl).getContent();
                BitmapFactory.Options opts = new BitmapFactory.Options();
                opts.inSampleSize = 4;
                bitmap = BitmapFactory.decodeStream(in, null, opts); //Decodes the stream returned from getContent and converts It into a Bitmap Format
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            imageView.setImageBitmap(bitmap);
        }
    }

    private class GetPostsTask extends AsyncTask<String, Integer, List<TagImages>> {

        String error = "";
        @Override
        protected List<TagImages> doInBackground(String... strings) {
            URL url;
            String response = "";
            String domain = getString(R.string.domain);
            String requestUrl = domain + "/pictag/getPostsForUser.php";
            tagImages = new ArrayList<>();
            try{
                url = new URL(requestUrl);
                HttpURLConnection myConnection = (HttpURLConnection) url.openConnection();
                myConnection.setReadTimeout(15000);
                myConnection.setConnectTimeout(15000);
                myConnection.setRequestMethod("POST");
                myConnection.setDoInput(true);
                myConnection.setDoOutput(true);

                OutputStream os = myConnection.getOutputStream();
                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(os));

                String requestJsonString = new JSONObject()
                        .put("user_id", strings[0])
                        .toString();

                Log.d("REQUEST BODY : ", requestJsonString);
                bw.write(requestJsonString);
                bw.flush();
                bw.close();

                int responseCode = myConnection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK){
                    String line;
                    BufferedReader br = new BufferedReader(new InputStreamReader(myConnection.getInputStream()));
                    line = br.readLine();
                    while(line != null){
                        response += line;
                        line = br.readLine();
                    }
                    br.close();
                } else{
                    error = "Unable to get tagImages!!";
                    ((HomeActivity) getActivity()).displayToast(error);
                }
                Log.d("RESPONSE BODY: ", response);

                if (!response.equalsIgnoreCase("")) {
                    JSONArray jsonArray = new JSONArray(response);
                    if (jsonArray.length() > 0) {
                        for (int i = 0; i < jsonArray.length(); i++) {
                            TagImages tagImagesObj = new TagImages();
                            JSONObject childJsonObj = jsonArray.getJSONObject(i);
                            tagImagesObj.setTagName(childJsonObj.getString("tag_name"));
                            List<String> imagesList = new ArrayList<>();
                            JSONArray imagesListArray = childJsonObj.getJSONArray("images_list");
                            if (imagesListArray.length() > 0) {
                                tagImagesObj.setTagId(imagesListArray.getJSONObject(0).getString("tag_id"));
                                for (int j = 0; j < imagesListArray.length(); j++) {
                                    JSONObject childImagesJsonObj = imagesListArray.getJSONObject(j);
                                    imagesList.add(j, childImagesJsonObj.getString("image_url"));
                                }
                                tagImagesObj.setImageUrls(imagesList);
                            }
                            tagImages.add(i, tagImagesObj);
                        }
                    } else {
                        error = "No Tags selected!";
                        ((HomeActivity) getActivity()).displayToast(error);
                    }
                    Log.d("Posts info: ", String.valueOf(tagImages.size()));
                }
                myConnection.disconnect();
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            return tagImages;
        }

        @Override
        protected void onPostExecute(List<TagImages> tagImages) {
            super.onPostExecute(tagImages);
            if (error.equalsIgnoreCase("") && tagImages != null && tagImages.size() > 0) {
                updatePosts();
            } else{
                ((HomeActivity) getActivity()).displayToast("Select tags to continue!!");
                ((HomeActivity)getActivity()).displaySearchTags();
            }
        }
    }

    public void updatePosts(){
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (tagImages!=null && tagImages.size()>0) {
                    gridview.setAdapter(new ImageAdapter(getActivity().getApplicationContext(), tagImages));
                }
            }
        });
    }

    public static Bitmap mark(Bitmap src, String watermark, Point location, int color, int alpha, int size, boolean underline) {
        int w = src.getWidth();
        int h = src.getHeight();
        Bitmap result = Bitmap.createBitmap(w, h, src.getConfig());

        Canvas canvas = new Canvas(result);
        canvas.drawBitmap(src, 0, 0, null);

        Paint paint = new Paint();
        paint.setColor(color);
        paint.setAlpha(alpha);
        paint.setTextSize(size);
        paint.setAntiAlias(true);
        paint.setUnderlineText(underline);
        canvas.drawText(watermark, location.x, location.y, paint);

        return result;
    }

}
