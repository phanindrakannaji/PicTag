package com.umbc.android.pictag;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.plumillonforge.android.chipview.Chip;
import com.plumillonforge.android.chipview.ChipView;

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
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link CameraFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link CameraFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CameraFragment extends Fragment implements View.OnClickListener, CompoundButton.OnCheckedChangeListener, AdapterView.OnItemSelectedListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    ImageView newImage;
    private String downloadUrl;
    Button goBack, topDone, postPic;
    TextView tvPriceSymbol;
    EditText description, price;
    Switch priceSwitch, privateSwitch, watermarkSwitch;
    Spinner watermark, category;
    List<StringWithTag> categories;
    List<StringWithTag> watermarks;
    int selectedCategory = 0, selectedWatermark = 0;
    ChipView newPicChipView;
    List<String> tagNames;

    UserProfile userProfile;

    private OnFragmentInteractionListener mListener;
    Handler handler = new Handler();

    public CameraFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment CameraFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static CameraFragment newInstance() {
        CameraFragment fragment = new CameraFragment();
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
        View parentView = inflater.inflate(R.layout.fragment_camera, container, false);
        newPicChipView = (ChipView) parentView.findViewById(R.id.newPicChipview);
        newImage = (ImageView) parentView.findViewById(R.id.newImage);

        goBack = (Button) parentView.findViewById(R.id.postTopBack);
        topDone = (Button) parentView.findViewById(R.id.postTopDone);
        postPic = (Button) parentView.findViewById(R.id.postPic);

        tvPriceSymbol = (TextView) parentView.findViewById(R.id.tv_price_symbol);

        description = (EditText) parentView.findViewById(R.id.description);
        price = (EditText) parentView.findViewById(R.id.price);

        priceSwitch = (Switch) parentView.findViewById(R.id.priceSwitch);
        privateSwitch = (Switch) parentView.findViewById(R.id.privateSwitch);
        watermarkSwitch = (Switch) parentView.findViewById(R.id.watermarkSwitch);

        priceSwitch.setOnCheckedChangeListener(this);
        privateSwitch.setOnCheckedChangeListener(this);
        watermarkSwitch.setOnCheckedChangeListener(this);

        watermark = (Spinner) parentView.findViewById(R.id.watermark);
        category = (Spinner) parentView.findViewById(R.id.category);

        watermark.setOnItemSelectedListener(this);
        category.setOnItemSelectedListener(this);

        userProfile = ((HomeActivity) getActivity()).getUserProfile();
        return parentView;
    }

    @Override
    public void onResume() {
        super.onResume();
        String[] categoryInput = new String[2];
        categoryInput[0] = "CATEGORY";
        categoryInput[1] = "G";
        GetParametersTask categoriesTask = new GetParametersTask();
        categoriesTask.execute(categoryInput);

        String[] watermarkInput = new String[2];
        watermarkInput[0] = "WATERMARK";
        watermarkInput[1] = "G";
        GetParametersTask watermarksTask = new GetParametersTask();
        watermarksTask.execute(watermarkInput);

        goBack.setOnClickListener(this);
        topDone.setOnClickListener(this);
        postPic.setOnClickListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        goBack.setOnClickListener(null);
        topDone.setOnClickListener(null);
        postPic.setOnClickListener(null);
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
            case R.id.postTopBack:
                ((HomeActivity) getActivity()).displayNewsFeed();
                break;
            case R.id.postTopDone:
                PostPic();
                break;
            case R.id.postPic:
                PostPic();
                break;
        }
    }

    private void PostPic() {
        String[] input = new String[8];
        input[0] = userProfile.getId();
        input[1] = downloadUrl;
        input[2] = priceSwitch.isChecked()?"Y":"N";
        input[3] = (String.valueOf(price.getText()).equalsIgnoreCase(""))?"0":String.valueOf(price.getText());
        input[4] = String.valueOf(description.getText());
        input[5] = privateSwitch.isChecked()?"Y":"N";
        input[6] = String.valueOf(selectedWatermark);
        input[7] = String.valueOf(selectedCategory);

        CreatePostTask createPostTask = new CreatePostTask();
        createPostTask.execute(input);
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        switch(compoundButton.getId()){
            case R.id.priceSwitch:
                if (b) {
                    tvPriceSymbol.setVisibility(View.VISIBLE);
                    price.setVisibility(View.VISIBLE);
                } else{
                    tvPriceSymbol.setVisibility(View.INVISIBLE);
                    price.setVisibility(View.INVISIBLE);
                }
                break;
            case R.id.privateSwitch:

                break;
            case R.id.watermarkSwitch:
                if (b) {
                    watermark.setVisibility(View.VISIBLE);
                } else{
                    watermark.setVisibility(View.INVISIBLE);
                }
                break;
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        switch(adapterView.getId()){
            case R.id.watermark:
                StringWithTag watermarkSWT = (StringWithTag) adapterView.getItemAtPosition(i);
                selectedWatermark = Integer.valueOf((String) watermarkSWT.tag);
                break;
            case R.id.category:
                StringWithTag categorySWT = (StringWithTag) adapterView.getItemAtPosition(i);
                selectedCategory = Integer.valueOf((String) categorySWT.tag);
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

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
        void setNewImage(Bitmap bitmap);
    }

    public void setNewImage(Bitmap bitmap) {
        newImage.setImageBitmap(bitmap);
    }

    public void setDownloadUrl(String downloadUrl){
        this.downloadUrl = downloadUrl;
    }

    private class CreatePostTask extends AsyncTask<String, Integer, String> {

        String error = "";
        @Override
        protected String doInBackground(String... strings) {
            URL url;
            String response = "";
            String domain = getString(R.string.domain);
            String requestUrl = domain + "/pictag/createPost.php";
            String message = "";
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
                        .put("userId", strings[0])
                        .put("picUrl", strings[1])
                        .put("isPriced", strings[2])
                        .put("price", strings[3])
                        .put("description", strings[4])
                        .put("isPrivate", strings[5])
                        .put("watermarkId", strings[6])
                        .put("category", strings[7])
                        .put("tags", tagNames)
                        .toString();

                Log.d("PostREQUESTBODY:", requestJsonString);
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
                Log.d("RESPONSE BODY: ", response);

                if (!response.equalsIgnoreCase("")) {
                    JSONArray jsonArray = new JSONArray(response);
                    if (jsonArray.length() > 0) {
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject childJsonObj = jsonArray.getJSONObject(i);
                            if (childJsonObj.getString("status").equalsIgnoreCase("F")) {
                                error = childJsonObj.getString("errorMessage");
                                message = error;
                            }
                        }
                    } else {
                        error = "Empty Response!";
                        message = error;
                    }
                }
                myConnection.disconnect();
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            return message;
        }

        @Override
        protected void onPostExecute(String message) {
            super.onPostExecute(message);
            if (error.equalsIgnoreCase("") && message.equalsIgnoreCase("")) {
                handler.post(new DisplayToast("Posted successfully!"));
                ((HomeActivity) getActivity()).displayNewsFeed();
            } else{
                handler.post(new DisplayToast(message));
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

    private static class StringWithTag {
        public String string;
        public Object tag;

        public StringWithTag(String string, Object tag) {
            this.string = string;
            this.tag = tag;
        }

        @Override
        public String toString() {
            return string;
        }
    }

    private class GetParametersTask extends AsyncTask<String, Integer, List<StringWithTag>> {

        String error = "";
        @Override
        protected List<StringWithTag> doInBackground(String... strings) {
            URL url;
            String response = "";
            String domain = getString(R.string.domain);
            String requestUrl = domain + "/pictag/getParameters.php";
            List<StringWithTag> list = new ArrayList<>();
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
                        .put("name", strings[0])
                        .put("type", strings[1])
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
                            StringWithTag cat = new StringWithTag(
                                    childJsonObj.getString("value"),
                                    childJsonObj.getString("name"));
                            list.add(cat);
                        }
                    } else {
                        error = "No Parameters found!";
                        handler.post(new DisplayToast(error));
                    }
                }
                myConnection.disconnect();
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            if (strings[0].equalsIgnoreCase("CATEGORY")){
                categories = list;
                setCategories();
            } else if(strings[0].equalsIgnoreCase("WATERMARK")){
                watermarks = list;
                setWatermarks();
            }
            return list;
        }

        @Override
        protected void onPostExecute(List<StringWithTag> list) {
            super.onPostExecute(list);
        }
    }

    public void setCategories(){
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ArrayAdapter<StringWithTag> categoryAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, categories);
                categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                category.setAdapter(categoryAdapter);
            }
        });
    }

    public void setWatermarks(){
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ArrayAdapter<StringWithTag> watermarkAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, watermarks);
                watermarkAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                watermark.setAdapter(watermarkAdapter);
            }
        });
    }

    public void setChipList(List<Chip> chipList){
        newPicChipView.setChipList(chipList);
        newPicChipView.setChipBackgroundColor(getActivity().getResources().getColor(R.color.colorPrimary, getActivity().getTheme()));
    }

    public void setTagNames(List<String> tagNames){
        this.tagNames = tagNames;
    }
}
