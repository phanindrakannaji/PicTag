package com.umbc.android.pictag.adapter;

import android.content.Context;
import android.graphics.Point;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.umbc.android.pictag.Post;
import com.umbc.android.pictag.R;
import com.umbc.android.pictag.SolventViewHolders;
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
 * Created by phani on 5/10/17.
 */

public class SolventRecyclerViewAdapter  extends RecyclerView.Adapter<SolventViewHolders> {

    private List<Post> posts;
    private Context context;
    private final int screenWidth;
    private final int screenHeight;
    private int res;
    private String userId;
    ViewGroup parent;

    public SolventRecyclerViewAdapter(Context context, List<Post> posts, String userId) {
        this.posts = posts;
        this.context = context;
        this.userId = userId;

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
    public SolventViewHolders onCreateViewHolder(ViewGroup parent, int viewType) {
        this.parent = parent;
        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.solvent_list, null);
        SolventViewHolders rcv = new SolventViewHolders(layoutView);
        return rcv;
    }

    @Override
    public void onBindViewHolder(SolventViewHolders holder, int position) {
        holder.postUpVote.setTag(position);
        holder.postUpCount.setTag("upCount"+position);
        holder.postDescription.setText(posts.get(position).getDescription());
        holder.postUpCount.setText(posts.get(position).getUpCount() + " upvotes");
        if (posts.get(position).getWatermark() == null
                || posts.get(position).getWatermark().equalsIgnoreCase("")
                || posts.get(position).getWatermark().equalsIgnoreCase("null")){
            Picasso.with(context)
                    .load(posts.get(position).getImageUrl().replace("\\/", "/").replace("//", "/").replace("https:/", "https://"))
                    .centerCrop()
                    .resize(res, res)
                    .placeholder(R.drawable.progress_animation)
                    .into(holder.postImage);
        } else {
            Picasso.with(context)
                    .load(posts.get(position).getImageUrl().replace("\\/", "/").replace("//", "/").replace("https:/", "https://"))
                    .transform(new WatermarkTransformation(posts.get(position).getWatermark(), context.getResources().getDisplayMetrics().density, R.color.blue))
                    .centerCrop()
                    .resize(res, res)
                    .placeholder(R.drawable.progress_animation)
                    .into(holder.postImage);
        }
        if (posts.get(position).isUpVote()) {
            holder.postUpVote.setBackground(context.getResources().getDrawable(R.drawable.ic_thumb_up_accent, context.getTheme()));
        } else{
            holder.postUpVote.setBackground(context.getResources().getDrawable(R.drawable.ic_thumb_up, context.getTheme()));
        }
        if (posts.get(position).isPriced()){
            holder.postPrice.setVisibility(View.VISIBLE);
            holder.postPurchasePic.setVisibility(View.VISIBLE);
        } else{
            holder.postPrice.setVisibility(View.INVISIBLE);
            holder.postPurchasePic.setVisibility(View.INVISIBLE);
        }
        holder.postPrice.setText("$ " + posts.get(position).getPrice());
        holder.postPurchasePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, "Future Work \\m/", Toast.LENGTH_SHORT).show();
            }
        });
        holder.postUpVote.setOnClickListener(new View.OnClickListener() {
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
    }

    @Override
    public int getItemCount() {
        return this.posts.size();
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