package com.umbc.android.pictag.adapter;

import android.content.Context;
import android.graphics.Point;
import android.os.AsyncTask;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.umbc.android.pictag.Post;
import com.umbc.android.pictag.R;
import com.umbc.android.pictag.utils.WatermarkTransformation;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

/**
 * Created by phani on 5/9/17.
 */

public class PostsOfTagAdapter extends BaseAdapter {
    private static LayoutInflater inflater=null;
    List<Post> posts;
    Context context;
    private String userId;
    private final int screenWidth;
    private final int screenHeight;
    private int res;

    public PostsOfTagAdapter(Context context, List<Post> posts, String userId) {
        this.context = context;
        this.posts = posts;
        this.userId = userId;
        inflater = ( LayoutInflater )context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        Display display = ((WindowManager) this.context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        screenWidth = size.x;
        screenHeight = size.y;
        if (screenWidth > screenHeight){
            res = screenHeight;
        } else{
            res = screenWidth;
        }
    }

    @Override
    public int getCount() {
        return posts.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public class DisplayPost
    {
        ImageView postfragImageview;
        TextView postfragDesc;
        TextView postfragUpCount;
        Button postFragUpVote;
    }

    @Override
    public View getView(final int position, final View convertView, final ViewGroup parent) {
        DisplayPost displayPost =new DisplayPost();
        Log.d("INSIDE LAYOUT", "Hi" + position);
        View rowView;
        rowView = inflater.inflate(R.layout.fragment_post, null);
        displayPost.postfragDesc=(TextView) rowView.findViewById(R.id.postfrag_desc);
        displayPost.postfragImageview=(ImageView) rowView.findViewById(R.id.postfrag_imageview);
        displayPost.postFragUpVote=(Button) rowView.findViewById(R.id.postfrag_upvote);
        displayPost.postfragUpCount = (TextView) rowView.findViewById(R.id.postfrag_upCount);
        displayPost.postFragUpVote.setTag(position);
        displayPost.postfragUpCount.setTag("upCount"+position);
        displayPost.postfragDesc.setText(posts.get(position).getDescription());
        displayPost.postfragUpCount.setText(posts.get(position).getUpCount() + " upvotes");
        Picasso.with(context)
                .load(posts.get(position).getImageUrl().replace("\\/", "/").replace("//", "/").replace("https:/", "https://"))
                .transform(new WatermarkTransformation("Pictag"))
                .centerCrop()
                .resize(res, res)
                .placeholder( R.drawable.progress_animation )
                .into(displayPost.postfragImageview);
        if (posts.get(position).isUpVote()) {
            displayPost.postFragUpVote.setBackground(context.getResources().getDrawable(R.drawable.ic_thumb_up_accent, context.getTheme()));
        } else{
            displayPost.postFragUpVote.setBackground(context.getResources().getDrawable(R.drawable.ic_thumb_up, context.getTheme()));
        }
        displayPost.postFragUpVote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int postPosition = (Integer) v.getTag();
                if (posts.get(postPosition).isUpVote()) {
                    posts.get(postPosition).setUpVote(false);
                    posts.get(postPosition).setUpCount(posts.get(postPosition).getUpCount()-1);
                    v.setBackground(context.getResources().getDrawable(R.drawable.ic_thumb_up, context.getTheme()));
                } else{
                    posts.get(postPosition).setUpVote(true);
                    posts.get(postPosition).setUpCount(posts.get(postPosition).getUpCount()+1);
                    v.setBackground(context.getResources().getDrawable(R.drawable.ic_thumb_up_accent, context.getTheme()));
                }
                ((TextView)parent.findViewWithTag("upCount" + postPosition)).setText(posts.get(postPosition).getUpCount() + " upvotes");
                String[] input = new String[3];
                input[0] = userId;
                input[1] = String.valueOf(posts.get(postPosition).getPostId());
                input[2] = String.valueOf(posts.get(postPosition).getOwnerId());
                ToggleUpvotePost toggleUpvotePost = new ToggleUpvotePost();
                toggleUpvotePost.execute(input);
            }
        });
        return rowView;
    }

    private class ToggleUpvotePost extends AsyncTask<String, Integer, Void> {

        String error = "";

        @Override
        protected Void doInBackground(String... strings) {
            URL url;
            String response = "";
            String domain = context.getString(R.string.domain);
            String requestUrl = domain + "/pictag/toggleUpvotePost.php";
            try {
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
                        .put("post_id", strings[1])
                        .put("post_owner_id", strings[2])
                        .toString();

                Log.d("REQUEST BODY : ", requestJsonString);
                bw.write(requestJsonString);
                bw.flush();
                bw.close();

                int responseCode = myConnection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    String line;
                    BufferedReader br = new BufferedReader(new InputStreamReader(myConnection.getInputStream()));
                    line = br.readLine();
                    while (line != null) {
                        response += line;
                        line = br.readLine();
                    }
                    br.close();
                } else {
                    error = "Unable to get posts!!";
                    Log.d("POSTTAGS", error);
                }
                Log.d("RESPONSE BODY: ", response);
                myConnection.disconnect();
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
