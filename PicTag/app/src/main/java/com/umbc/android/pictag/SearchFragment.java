package com.umbc.android.pictag;

import android.content.Context;
import android.content.res.Resources;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.plumillonforge.android.chipview.Chip;
import com.plumillonforge.android.chipview.ChipView;
import com.plumillonforge.android.chipview.ChipViewAdapter;

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

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SearchFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link SearchFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SearchFragment extends Fragment {
    UserProfile userProfile;

    // load screen elements from onCreateView

    private TagsFragment.OnFragmentInteractionListener mListener;
    private Handler handler = new Handler();
    private List<Tag> tags;
    List<Chip> chipList = new ArrayList<>();
    ChipView chipView;

    public SearchFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment SearchFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SearchFragment newInstance() {
        SearchFragment fragment = new SearchFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_search, container, false);
        userProfile = ((HomeActivity) getActivity()).getUserProfile();

        chipList.add(new TagView("Cat"));
        chipList.add(new TagView("Dogs"));
        chipList.add(new TagView("Wedding"));
        chipList.add(new TagView("Wedding"));
        chipList.add(new TagView("Wedding"));
        chipList.add(new TagView("Wedding"));
        chipList.add(new TagView("Wedding"));

        ChipView chipDefault = (ChipView) view.findViewById(R.id.chipview);
        chipDefault.setChipList(chipList);

        ChipViewAdapter adapter = new MainChipViewAdapter(this.getContext());
        //chipView.setBackgroundColor(getResources().getColor(R.color.green));
        //chipView.setAdapter(adapter);

        String[] input= new String[1];
        input[0] = "";//String.valueOf(userProfile.getId());
        GetTagsTask getTagsTask = new GetTagsTask();
        getTagsTask.execute(input);

        // Inflate the layout for this fragment
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    private class GetTagsTask extends AsyncTask<String, Integer, List<Tag>> {

        private static final String TAG = "SearchFragment";
        String error = "";
        @Override
        protected List<Tag> doInBackground(String... strings) {
            URL url;
            String response = "";
            String domain = getString(R.string.domain);
            String requestUrl = domain + "/pictag/getAllTags.php";
            tags = new ArrayList<>();
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
                        .put("search_term", strings[0])
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
                    error = "Unable to get tags!!";
                    handler.post(new DisplayToast(error));
                }
                Log.d("RESPONSE BODY: ", response);

                if (!response.equalsIgnoreCase("")) {
                    JSONArray jsonArray = new JSONArray(response);
                    if (jsonArray.length() > 0) {
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject childJsonObj = jsonArray.getJSONObject(i);
                            if (childJsonObj.getString("status").equalsIgnoreCase("S")) {
                                Tag tag = new Tag(
                                        childJsonObj.getInt("user_id"),
                                        childJsonObj.getInt("tag_id"),
                                        childJsonObj.getString("tag_name"),
                                        childJsonObj.getString("notify"),
                                        childJsonObj.getInt("minUpVotes"));
                                tags.add(tag);
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
            return tags;
        }

        @Override
        protected void onPostExecute(List<Tag> tags) {
            super.onPostExecute(tags);
            if (error.equalsIgnoreCase("") && tags != null && tags.size() > 0) {
                //TODO 1
                // display the list of tags in some list view
                Log.d(TAG, tags.get(0).getTagName());
            }
        }
    }

    private class UpdateTagTask extends AsyncTask<String, Integer, String> {

        String error = "";
        @Override
        protected String doInBackground(String... strings) {
            URL url;
            String response = "";
            String domain = getString(R.string.domain);
            String requestUrl = domain + "/pictag/updateTagForUser.php";
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
                        .put("notify", strings[2])
                        .put("minUpVotes", strings[3])
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
                    error = "Unable to update tag!!";
                    handler.post(new DisplayToast(error));
                }
                myConnection.disconnect();
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            return response;
        }

        @Override
        protected void onPostExecute(String response) {
            if (error.equalsIgnoreCase("") && response != null) {
                super.onPostExecute(response);
                Log.d("RESPONSE BODY: ", response);
            }
        }
    }

    private class DeleteTagTask extends AsyncTask<String, Integer, String> {

        String error = "";
        @Override
        protected String doInBackground(String... strings) {
            URL url;
            String response = "";
            String domain = getString(R.string.domain);
            String requestUrl = domain + "/pictag/removeTagForUser.php";
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
                    error = "Unable to delete tag!!";
                    handler.post(new DisplayToast(error));
                }
                myConnection.disconnect();
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            return response;
        }

        @Override
        protected void onPostExecute(String response) {
            if (error.equalsIgnoreCase("") && response != null) {
                super.onPostExecute(response);
                Log.d("RESPONSE BODY: ", response);
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

    private class TagView implements Chip {
        private String mName;
        private int mType = 0;

        public TagView(String name, int type) {
            this(name);
            mType = type;
        }

        public TagView(String name) {
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

    public class MainChipViewAdapter extends ChipViewAdapter {
        public MainChipViewAdapter(Context context) {
            super(context);
        }

        @Override
        public int getLayoutRes(int position) {
            TagView tag = (TagView) getChip(position);

            switch (tag.getType()) {
                default:
                case 2:
                case 4:
                    return 0;

                case 1:
                case 5:
                    //return R.layout.chip_double_close;
                    return R.layout.chip_close;

                case 3:
                    return R.layout.chip_close;
            }
        }

        @Override
        public int getBackgroundColor(int position) {
            TagView tag = (TagView) getChip(position);

            switch (tag.getType()) {
                default:
                    return 0;

                case 1:
                case 4:
                    return getColor(R.color.red);

                case 2:
                case 5:
                    return getColor(R.color.purple);

                case 3:
                    return getColor(R.color.green);
            }
        }

        @Override
        public int getBackgroundColorSelected(int position) {
            return 0;
        }

        @Override
        public int getBackgroundRes(int position) {
            return 0;
        }

        @Override
        public void onLayout(View view, int position) {
            TagView tag = (TagView) getChip(position);

            if (tag.getType() == 2)
                ((TextView) view.findViewById(android.R.id.text1)).setTextColor(getColor(R.color.blue));
        }


    }
}