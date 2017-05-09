package com.umbc.android.pictag;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.umbc.android.pictag.adapter.PostsOfTagAdapter;

import org.json.JSONArray;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PostsOfTagActivity extends AppCompatActivity {

    TextView tagHeading;
    ListView postsListView;
    private int tagId;
    private List<Post> posts;
    private Handler handler = new Handler();
    private UserProfile userProfile;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    SharedPreferences data;
    private String TAG = "PostOfTagActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_posts_of_tag);

        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    Intent myIntent = new Intent(PostsOfTagActivity.this, LoginActivity.class);
                    startActivity(myIntent);
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
            }
        };

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            data = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            String user_id = data.getString("user_id", "");
            String email = data.getString("email", "");
            String firstName = data.getString("firstName", "");
            String lastName = data.getString("lastName", "");
            String gender = data.getString("gender", "");
            String dob = data.getString("dob", "");
            String fbProfileId = data.getString("fb_profile_id", "");
            int reputation = Integer.valueOf(data.getString("reputation" ,""));
            String profilePicUrl = data.getString("profilePicUrl", "");
            String token = data.getString("token", "");

            userProfile = new UserProfile(user_id, email, firstName, lastName,
                    gender, dob, fbProfileId, reputation, profilePicUrl, token);
        } else{
            mAuth.signOut();
        }

        tagHeading = (TextView) findViewById(R.id.tagHeading);
        postsListView = (ListView) findViewById(R.id.postsList);
        Intent myIntent = getIntent();
        String selectedTagId = myIntent.getStringExtra("selected_tag_id");
        String[] input = new String[2];
        input[0] = userProfile.getId();
        input[1] = selectedTagId;
        GetPostsForUserTagTask getPostsForUserTagTask = new GetPostsForUserTagTask();
        getPostsForUserTagTask.execute(input);
    }

    private class GetPostsForUserTagTask extends AsyncTask<String, Integer, List<Post>> {

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
                        .put("tag_id", strings[1])
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
                postsListView.setAdapter(new PostsOfTagAdapter(getApplicationContext(), posts, userProfile.getId()));
                synchronized (postsListView) {
                    postsListView.notifyAll();
                }
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
            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
        }
    }
}
