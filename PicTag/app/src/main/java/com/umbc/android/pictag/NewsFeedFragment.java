package com.umbc.android.pictag;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * <p>A fragment that shows a list of items as a modal bottom sheet.</p>
 * <p>You can show this modal bottom sheet from your activity like this:</p>
 * <pre>
 *     NewsFeedFragment.newInstance(30).show(getSupportFragmentManager(), "dialog");
 * </pre>
 * <p>You activity (or fragment) needs to implement {@link NewsFeedFragment.Listener}.</p>
 */
public class NewsFeedFragment extends BottomSheetDialogFragment implements View.OnClickListener {

    // TODO: Customize parameter argument names
    private static final String ARG_ITEM_COUNT = "item_count";
    private static final String TAG = "NEWSFEEDFRAGMENT";
    private Listener mListener;
    Button tempButton;

    Handler handler = new Handler();
    private View parentView;
    List<Post> posts;
    private UserProfile userProfile;

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
        tempButton = (Button) parentView.findViewById(R.id.tempButton);
        tempButton.setOnClickListener(this);
        return parentView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.tempButton:
                Intent myIntent = new Intent(getActivity(), PostsOfTagActivity.class);
                myIntent.putExtra("selected_tag_id", "44");
                startActivity(myIntent);
                break;
        }
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

    private class GetPostsTask extends AsyncTask<String, Integer, List<Post>> {

        String error = "";
        @Override
        protected List<Post> doInBackground(String... strings) {
            URL url;
            String response = "";
            String domain = getString(R.string.domain);
            String requestUrl = domain + "/pictag/getPostsForUser.php";
            posts = new ArrayList<>();
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
                    error = "Unable to get posts!!";
                    handler.post(new DisplayToast(error));
                }
                Log.d("RESPONSE BODY: ", response);

                if (!response.equalsIgnoreCase("")) {
                    JSONArray jsonArray = new JSONArray(response);
                    if (jsonArray.length() > 0) {
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject childJsonObj = jsonArray.getJSONObject(i);
                            if (childJsonObj.getString("status").equalsIgnoreCase("S")) {

                                int juserId = childJsonObj.getInt("user_id");
                                String jImageUrl = childJsonObj.getString("image_url");
                                boolean jIsPriced = childJsonObj.getString("is_Priced").equals("Y");
                                String jPrice = childJsonObj.getString("price");
                                String jDescription = childJsonObj.getString("description");
                                Date jcreatedDate = null;
                                try {
                                    jcreatedDate = (Date) new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").parse(childJsonObj.getString("created_date"));
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                                Date jLastUpdatedDate = null;
                                try {
                                    jLastUpdatedDate = (Date) new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").parse(childJsonObj.getString("last_updated_date"));
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                                String jStatus = childJsonObj.getString("postStatus");
                                boolean jIsPrivate = childJsonObj.getString("is_private").equals("Y");
                                int jWatermark = childJsonObj.getInt("watermark_id");
                                int jCategory = childJsonObj.getInt("category");
                                int jUpCount = childJsonObj.getInt("up_count");
                                int jDownCount = childJsonObj.getInt("down_count");
                                Post post = new Post(juserId, jImageUrl, jIsPriced, jDescription,
                                        jcreatedDate, jLastUpdatedDate, jStatus, jIsPrivate,
                                        jWatermark, jCategory, jUpCount, jDownCount);
                                posts.add(post);
                            } else if (childJsonObj.getString("status").equalsIgnoreCase("F")) {
                                error = childJsonObj.getString("errorMessage");
                                handler.post(new DisplayToast(error));
                            }
                        }
                    } else {
                        error = "No Tags selected!";
                        handler.post(new DisplayToast(error));
                    }
                    Log.d("Posts info: ", String.valueOf(posts.size()));
                }
                myConnection.disconnect();
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            return posts;
        }

        @Override
        protected void onPostExecute(List<Post> posts) {
            super.onPostExecute(posts);
            if (error.equalsIgnoreCase("") && posts != null && posts.size() > 0) {
                //TODO 1
                // display the list of posts in some list view
                updatePosts();
                Log.d(TAG, posts.get(0).getDescription());
            }
        }
    }

    private class DisplayToast implements Runnable{

        String message;

        DisplayToast(String message){
            this.message = message;
        }
        @Override
        public void run() {
            Toast.makeText(getActivity().getApplicationContext(), message, Toast.LENGTH_SHORT).show();
        }
    }

    public void updatePosts(){
        ((HomeActivity)getActivity()).runOnUiThread(new Runnable() {
            @Override
            public void run() {

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
