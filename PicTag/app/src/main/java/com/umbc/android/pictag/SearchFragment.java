package com.umbc.android.pictag;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.plumillonforge.android.chipview.Chip;
import com.plumillonforge.android.chipview.ChipView;
import com.plumillonforge.android.chipview.ChipViewAdapter;
import com.plumillonforge.android.chipview.OnChipClickListener;

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
public class SearchFragment extends Fragment implements OnChipClickListener {
    UserProfile userProfile;

    // load screen elements from onCreateView

    private TagsFragment.OnFragmentInteractionListener mListener;
    private Handler handler = new Handler();
    private List<Tag> tags;
    private List<Chip> chipList = new ArrayList<>();
    private ChipView chipView;

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

        chipView = (ChipView) view.findViewById(R.id.chipview);
        ChipViewAdapter adapter = new MainChipViewAdapter(getActivity().getApplicationContext());
        chipView.setAdapter(adapter);


        String[] input= new String[2];
        input[0] = String.valueOf(userProfile.getId());
        input[1] = ""; //search term  has to be here, if not send empty
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

    @Override
    public void onChipClick(Chip chip) {
        TagView selectedTag = (TagView) chip;
        String tagName = selectedTag.getText();

        //select if unselected and vice-versa
        selectedTag.setSelection(!selectedTag.getSelection());

        if(selectedTag.getSelection()){
            chipView.setChipBackgroundColorSelected(ContextCompat.getColor(getActivity().getApplicationContext(), R.color.green));
        }
        else{
            chipView.setChipBackgroundColorSelected(ContextCompat.getColor(getActivity().getApplicationContext(), R.color.blue));
        }


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
                        .put("user_id", strings[0])
                        .put("search_term", strings[1])
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
                                Tag tag = new Tag(childJsonObj.getInt("tag_id"),
                                        childJsonObj.getString("tag_name"),
                                        childJsonObj.getBoolean("isSelected"));
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
                // dummy data start
                chipList.add(new TagView("Cat", 0, false));
                chipList.add(new TagView("Dogs", 0, true));
                chipList.add(new TagView("Wedding"));
                chipList.add(new TagView("Wedding"));
                chipList.add(new TagView("Wedding"));
                chipList.add(new TagView("Wedding"));
                chipList.add(new TagView("Wedding"));
                // dummy data end


                chipView.setChipList(chipList);
                chipView.setOnChipClickListener(SearchFragment.this);
                chipView.setBackgroundColor(ContextCompat.getColor(getActivity().getApplicationContext(), R.color.green));

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
            String requestUrl = domain + "/pictag/toggleTagForUser.php";
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
                handler.post(new DisplayToast(response));
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
        private boolean mSelected = false;

        public TagView(String name, int type, boolean selected) {
            this(name);
            mType = type;
            mSelected = selected;
        }

        public TagView(String name) {
            mName = name;
        }

        @Override
        public String getText() {
            return mName;
        }

        public void setSelection(boolean value) {
            mSelected = value;
        }

        public boolean getSelection() {
            return mSelected;
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
                    return getColor(R.color.blue);

                case 2:
                case 5:
                    return getColor(R.color.purple);

                case 3:
                    return getColor(R.color.red);
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