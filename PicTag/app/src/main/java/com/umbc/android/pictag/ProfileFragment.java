package com.umbc.android.pictag;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.umbc.android.pictag.adapter.UserProfileAdapter;
import com.umbc.android.pictag.utils.CircleTransformation;
import com.umbc.android.pictag.view.RevealBackgroundView;

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

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.facebook.FacebookSdk.getApplicationContext;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ProfileFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProfileFragment extends Fragment implements View.OnClickListener , RevealBackgroundView.OnStateChangeListener{
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";



    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    Handler handler = new Handler();
    Button updateUserButton;
    EditText firstName, lastName, dateOfBirth;
    RadioGroup radioGroup;
    RadioButton gender;




    RevealBackgroundView vRevealBackground;

    RecyclerView rvUserProfile;


    TabLayout tlUserProfileTabs;

    ImageView ivUserProfilePhoto;
    TextView tvUsername,tvPhonenumber,tvEmail;
    FloatingActionButton fbEditUser;
    View vUserDetails;

    View vUserProfileRoot;

    private int avatarSize;
    private String profilePhoto;
    private UserProfileAdapter userPhotosAdapter;

    private static final int USER_OPTIONS_ANIMATION_DELAY = 300;
    private static final Interpolator INTERPOLATOR = new DecelerateInterpolator();

    private OnFragmentInteractionListener mListener;
    UserProfile userProfile;
    private View parentView;

    public ProfileFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment ProfileFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ProfileFragment newInstance() {
        ProfileFragment fragment = new ProfileFragment();
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
        // Inflate the layout for this fragment
        parentView = inflater.inflate(R.layout.fragment_profile, container, false);
        updateUserButton = (Button) parentView.findViewById(R.id.postTopBack);//TODO change this

        userProfile = ((HomeActivity) getActivity()).getUserProfile();

        //ButterKnife.bind(getActivity());

        vRevealBackground = (RevealBackgroundView) parentView.findViewById(R.id.vRevealBackground);
        rvUserProfile = (RecyclerView) parentView.findViewById(R.id.rvUserProfile);
        tlUserProfileTabs = (TabLayout) parentView.findViewById(R.id.tlUserProfileTabs);
        ivUserProfilePhoto = (ImageView) parentView.findViewById(R.id.ivUserProfilePhoto);
        vUserDetails = (View) parentView.findViewById(R.id.vUserDetails);
        vUserProfileRoot = (View) parentView.findViewById(R.id.vUserProfileRoot);

        this.avatarSize = getResources().getDimensionPixelSize(R.dimen.user_profile_avatar_size);
        this.profilePhoto = getString(R.string.user_profile_photo);

        tvUsername = (TextView) parentView.findViewById(R.id.tv_username);
        tvPhonenumber = (TextView) parentView.findViewById(R.id.tv_phonenumber);
        tvEmail = (TextView) parentView.findViewById(R.id.tv_userid);
        setupUserProfile();

        Picasso.with(getApplicationContext())
                .load(profilePhoto)
                .placeholder(R.drawable.img_circle_placeholder)
                .resize(avatarSize, avatarSize)
                .centerCrop()
                .transform(new CircleTransformation())
                .into(ivUserProfilePhoto);



        setupTabs();
        setupUserProfileGrid();
        setupRevealBackground(savedInstanceState);

        return parentView;
    }

    private void setupUserProfile() {
        tvUsername.setText(userProfile.getFirstName() + " "+ userProfile.getLastName());
        tvEmail.setText(userProfile.getEmail());
        tvPhonenumber.setText(String.valueOf(userProfile.getReputation()));
        profilePhoto = userProfile.getProfilePicUrl();
        profilePhoto = "http://scriptmode.com/googleandroidstudio/img/anyone.png";
    }


    private void setupTabs() {
        tlUserProfileTabs.addTab(tlUserProfileTabs.newTab().setIcon(R.drawable.ic_grid_on_white));
        tlUserProfileTabs.addTab(tlUserProfileTabs.newTab().setIcon(R.drawable.ic_list_white));

    }

    private void setupUserProfileGrid() {
        final StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL);
        rvUserProfile.setLayoutManager(layoutManager);
        rvUserProfile.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                userPhotosAdapter.setLockedAnimations(true);
            }
        });
    }


    private void setupRevealBackground(Bundle savedInstanceState) {
        vRevealBackground.setOnStateChangeListener(this);

            vRevealBackground.setToFinishedFrame();
            userPhotosAdapter.setLockedAnimations(true);

    }

    @Override
    public void onResume() {
        super.onResume();
        //updateUserButton.setOnClickListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        //updateUserButton.setOnClickListener(null);
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
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
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.postTopDone: //TODO change this too
                int selectedId = radioGroup.getCheckedRadioButtonId();
                gender = (RadioButton) parentView.findViewById(selectedId);

                String[] input = new String[7];
                input[0] = userProfile.getId();
                input[1] = userProfile.getEmail();
                input[2] = firstName.getText().toString();
                input[3] = lastName.getText().toString();
                input[4] = "";
                input[5] = dateOfBirth.getText().toString();
                input[6] = gender.getText().toString();
                input[7] = "";
                UpdateUserTask updateUserTask = new UpdateUserTask();
                //updateUserTask.execute(input);
                break;
        }
    }

    @Override
    public void onStateChange(int state) {
        if (RevealBackgroundView.STATE_FINISHED == state) {
            rvUserProfile.setVisibility(View.VISIBLE);
            tlUserProfileTabs.setVisibility(View.VISIBLE);
            vUserProfileRoot.setVisibility(View.VISIBLE);
            userPhotosAdapter = new UserProfileAdapter(getApplicationContext());
            rvUserProfile.setAdapter(userPhotosAdapter);
            animateUserProfileOptions();
            animateUserProfileHeader();
        } else {
            tlUserProfileTabs.setVisibility(View.INVISIBLE);
            rvUserProfile.setVisibility(View.INVISIBLE);
            vUserProfileRoot.setVisibility(View.INVISIBLE);
        }
    }


    private void animateUserProfileOptions() {
        tlUserProfileTabs.setTranslationY(-tlUserProfileTabs.getHeight());
        tlUserProfileTabs.animate().translationY(0).setDuration(300).setStartDelay(USER_OPTIONS_ANIMATION_DELAY).setInterpolator(INTERPOLATOR);
    }

    private void animateUserProfileHeader() {
        vUserProfileRoot.setTranslationY(-vUserProfileRoot.getHeight());
        ivUserProfilePhoto.setTranslationY(-ivUserProfilePhoto.getHeight());
        vUserDetails.setTranslationY(-vUserDetails.getHeight());


        vUserProfileRoot.animate().translationY(0).setDuration(300).setInterpolator(INTERPOLATOR);
        ivUserProfilePhoto.animate().translationY(0).setDuration(300).setStartDelay(100).setInterpolator(INTERPOLATOR);
        vUserDetails.animate().translationY(0).setDuration(300).setStartDelay(200).setInterpolator(INTERPOLATOR);

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

    private class UpdateUserTask extends AsyncTask<String, Integer, UserProfile> {

        String error = "";
        @Override
        protected UserProfile doInBackground(String... strings) {
            URL url;
            String response = "";
            String domain = getString(R.string.domain);
            String requestUrl = domain + "/pictag/updateUserDetails.php";
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
                        .put("email", strings[1])
                        .put("firstName", strings[2])
                        .put("lastName", strings[3])
                        .put("fbProfileId", strings[4])
                        .put("dob", strings[5])
                        .put("gender", strings[6])
                        .put("profilePicUrl", strings[7])
                        .toString();

                Log.d("UPDATEUSERREQUESTBODY", requestJsonString);
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
                }
                Log.d("UPDATEUSERRESPONSEBODY", response);

                if (!response.equalsIgnoreCase("")) {
                    JSONArray jsonArray = new JSONArray(response);
                    if (jsonArray.length() > 0) {
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject childJsonObj = jsonArray.getJSONObject(i);
                            if (childJsonObj.getString("status").equalsIgnoreCase("S")) {
                                userProfile = new UserProfile(
                                        childJsonObj.getString("user_id"),
                                        childJsonObj.getString("email"),
                                        childJsonObj.getString("firstName"),
                                        childJsonObj.getString("lastName"),
                                        childJsonObj.getString("gender"),
                                        childJsonObj.getString("dob"),
                                        childJsonObj.getString("fb_profile_id"),
                                        childJsonObj.getInt("reputation"),
                                        childJsonObj.getString("profilePicUrl"),
                                        childJsonObj.getString("token"));
                            } else if (childJsonObj.getString("status").equalsIgnoreCase("F")) {
                                error = childJsonObj.getString("errorMessage");
                                handler.post(new DisplayToast(error));
                            }
                        }
                    } else {
                        error = "Update user failed!!";
                        handler.post(new DisplayToast(error));
                    }
                }
                myConnection.disconnect();
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            return userProfile;
        }

        @Override
        protected void onPostExecute(UserProfile user) {
            if (error.equalsIgnoreCase("") && user!=null) {
                super.onPostExecute(user);
                SharedPreferences userDetails = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                SharedPreferences.Editor editor = userDetails.edit();
                editor.putString("user_id", String.valueOf(user.getId()));
                editor.putString("email", String.valueOf(user.getEmail()));
                editor.putString("firstName", String.valueOf(user.getFirstName()));
                editor.putString("lastName", String.valueOf(user.getLastName()));
                editor.putString("gender", String.valueOf(user.getGender()));
                editor.putString("dob", String.valueOf(user.getDateOfBirth()));
                editor.putString("fb_profile_id", String.valueOf(user.getFbProfileId()));
                editor.putString("reputation", String.valueOf(user.getReputation()));
                editor.putString("profilePicUrl", String.valueOf(user.getProfilePicUrl()));
                editor.putString("token", String.valueOf(user.getTokenId()));
                editor.apply();
                ((HomeActivity) getActivity()).setUserProfile(user);
            } else{
                error = "Update user failed!!!";
                handler.post(new DisplayToast(error));
            }
        }
    }

}
