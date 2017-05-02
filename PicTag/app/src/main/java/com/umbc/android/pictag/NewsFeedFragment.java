package com.umbc.android.pictag;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

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
import java.util.ArrayList;
import java.util.List;

import static com.facebook.FacebookSdk.getApplicationContext;

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

    Handler handler = new Handler();
    private View parentView;
    List<Post> posts;

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
        return parentView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        final RecyclerView recyclerView = (RecyclerView) view;
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(new ItemAdapter(getArguments().getInt(ARG_ITEM_COUNT)));
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        mListener = null;
        super.onDetach();
    }

    public interface Listener {
        void onItemClicked(int position);
    }

    private class ViewHolder extends RecyclerView.ViewHolder {

        final TextView text;

        ViewHolder(LayoutInflater inflater, ViewGroup parent) {
            // TODO: Customize the item layout
            super(inflater.inflate(R.layout.fragment_newsfeed_item, parent, false));
            text = (TextView) itemView.findViewById(R.id.text);
            text.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListener != null) {
                        mListener.onItemClicked(getAdapterPosition());
                        dismiss();
                    }
                }
            });
        }

    }

    private class ItemAdapter extends RecyclerView.Adapter<ViewHolder> {

        private final int mItemCount;

        ItemAdapter(int itemCount) {
            mItemCount = itemCount;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext()), parent);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            holder.text.setText(String.valueOf(position));
        }

        @Override
        public int getItemCount() {
            return mItemCount;
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
                                    //Post post = new Post();
                                    //posts.add(post);
                                } else if (childJsonObj.getString("status").equalsIgnoreCase("F")) {
                                    error = childJsonObj.getString("errorMessage");
                                    handler.post(new DisplayToast(error));
                                }
                            }
                        } else {
                            error = "No Tags selected!";
                            handler.post(new DisplayToast(error));
                        }
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
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
            }
        }
    }

}
