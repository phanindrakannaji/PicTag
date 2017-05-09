package com.umbc.android.pictag;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.crystal.crystalrangeseekbar.interfaces.OnSeekbarChangeListener;
import com.crystal.crystalrangeseekbar.interfaces.OnSeekbarFinalValueListener;
import com.crystal.crystalrangeseekbar.widgets.CrystalSeekbar;
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
 * {@link TagsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link TagsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TagsFragment extends Fragment implements OnChipClickListener, CompoundButton.OnCheckedChangeListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String TAG = "TagFragment";
    int finalValue = 0;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    UserProfile userProfile;
    int currentTagId;

    // load screen elements from onCreateView

    private OnFragmentInteractionListener mListener;
    private Handler handler = new Handler();
    private List<Tag> tags;

    private List<Chip> chipList;
    private ChipView chipView;
    private  ChipViewAdapter adapter;
    private boolean notifyValue;
    private Button delete;
    private AlertDialog dialog;


    public TagsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment TagsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static TagsFragment newInstance() {
        TagsFragment fragment = new TagsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tags, container, false);

        //load the screen elements here
        // from view.findViewById

        chipView = (ChipView) view.findViewById(R.id.chipviewTags);
        adapter = new TagsFragment.MainChipViewAdapter(getActivity().getApplicationContext());
        chipView.setAdapter(adapter);

        userProfile = ((HomeActivity) getActivity()).getUserProfile();
        String[] input = new String[1];
        input[0] = userProfile.getId();
        GetTagsTask getTagsTask = new GetTagsTask();
        getTagsTask.execute(input);

        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onChipClick(Chip chip) {
        LayoutInflater inflater = getActivity().getLayoutInflater();

        final TagsFragment.TagView selectedTag = (TagsFragment.TagView) chip;
        String tagName = selectedTag.getText();
        currentTagId = selectedTag.getTagId();

        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Options");
        View rootView = inflater.inflate(R.layout.fragment_tag_dialog, null);
        Switch notify = (Switch) rootView.findViewById(R.id.notify_switch);
        if (selectedTag.getNotify().equalsIgnoreCase("Y")){
            notify.setChecked(true);
        } else{
            notify.setChecked(false);
        }
        notify.setOnCheckedChangeListener(this);
        Button delete = (Button) rootView.findViewById(R.id.deleteTag);
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String[] input = new String[2];
                input[0] = userProfile.getId();
                input[1] = String.valueOf(currentTagId);
                DeleteTagTask deleteTagTask = new DeleteTagTask();
                deleteTagTask.execute(input);
                finalValue = 0;
                currentTagId = 0;
                dialog.dismiss();
            }
        });
        // get seekbar from view
        CrystalSeekbar seekbar = (CrystalSeekbar) rootView.findViewById(R.id.rangeSeekbar1);
        seekbar.setMinStartValue(selectedTag.getMinUpVotes()).apply();

// get min and max text view
        final TextView tvMax = (TextView) rootView.findViewById(R.id.minUpVoteValue);
        tvMax.setText(String.valueOf(selectedTag.getMinUpVotes()));

// set listener
        seekbar.setOnSeekbarChangeListener(new OnSeekbarChangeListener() {
            @Override
            public void valueChanged(Number minValue) {
                tvMax.setText(String.valueOf(minValue));
            }
        });

// set final value listener
        seekbar.setOnSeekbarFinalValueListener(new OnSeekbarFinalValueListener() {
            @Override
            public void finalValue(Number value) {
                Log.d("CRS=>", String.valueOf(value));
                finalValue = Integer.valueOf(String.valueOf(value));
            }
        });
        builder.setView(rootView);
        // Add the buttons
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked OK button
                String[] input = new String[4];
                input[0] = userProfile.getId();
                input[1] = String.valueOf(selectedTag.getTagId());
                input[2] = notifyValue?"Y":"N";
                input[3] = String.valueOf(finalValue);
                UpdateTagTask updateTagTask = new UpdateTagTask();
                updateTagTask.execute(input);
                finalValue = 0;
                currentTagId = 0;
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                finalValue = 0;
                currentTagId = 0;
                dialog.dismiss();
            }
        });



        dialog = builder.create();
        dialog.show();


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
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        notifyValue = b;
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

        String error = "";
        @Override
        protected List<Tag> doInBackground(String... strings) {
            URL url;
            String response = "";
            String domain = getString(R.string.domain);
            String requestUrl = domain + "/pictag/getTagsForUser.php";
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
                                        Integer.valueOf(strings[0]),
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
                chipList = new ArrayList<>();
                for (Tag tag : tags) {
                    chipList.add(new TagsFragment.TagView(tag.getTagId(), tag.getTagName(), 0,
                            tag.getNotify(), tag.getMinVotes(), tag.isSelected()));
                }

                chipView.setChipList(chipList);
                chipView.setOnChipClickListener(TagsFragment.this);
                chipView.setChipSpacing(15);
                chipView.setChipSidePadding(15);
                chipView.setLineSpacing(15);
                chipView.setChipPadding(15);

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
                String[] input = new String[1];
                input[0] = userProfile.getId();
                GetTagsTask getTagsTask = new GetTagsTask();
                getTagsTask.execute(input);
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
            String[] input = new String[1];
            input[0] = userProfile.getId();
            GetTagsTask getTagsTask = new GetTagsTask();
            getTagsTask.execute(input);
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
        private int tagId;
        private String notify;
        private int minUpVotes;
        private boolean mSelected = false;

        public TagView(int tagId, String mName, int mType, String notify, int minUpVotes, boolean mSelected) {
            this.mName = mName;
            this.mType = mType;
            this.tagId = tagId;
            this.notify = notify;
            this.minUpVotes = minUpVotes;
            this.mSelected = mSelected;
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

        public int getTagId() {
            return tagId;
        }

        public void setTagId(int tagId) {
            this.tagId = tagId;
        }

        public String getmName() {
            return mName;
        }

        public void setmName(String mName) {
            this.mName = mName;
        }

        public int getMinUpVotes() {
            return minUpVotes;
        }

        public void setMinUpVotes(int minUpVotes) {
            this.minUpVotes = minUpVotes;
        }

        public boolean ismSelected() {
            return mSelected;
        }

        public void setmSelected(boolean mSelected) {
            this.mSelected = mSelected;
        }

        public int getmType() {
            return mType;
        }

        public void setmType(int mType) {
            this.mType = mType;
        }

        public String getNotify() {
            return notify;
        }

        public void setNotify(String notify) {
            this.notify = notify;
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

            if (tag.getSelection()){
                return getColor(R.color.tagSelectedColor);
            } else{
                return getColor(R.color.tagBackgroundColor);
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
            TextView text1 = (TextView) view.findViewById(android.R.id.text1);
            text1.setTextSize(18);
            text1.setTextColor(getActivity().getResources().getColor(R.color.primary_text_default_material_light, getActivity().getTheme()));
            text1.setPadding(1, 2, 1, 2);
        }
    }

}
