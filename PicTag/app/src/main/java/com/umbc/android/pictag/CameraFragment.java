package com.umbc.android.pictag;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link CameraFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link CameraFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CameraFragment extends Fragment implements View.OnClickListener{
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

    private OnFragmentInteractionListener mListener;

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
        View view = inflater.inflate(R.layout.fragment_camera, container, false);
        newImage = (ImageView) view.findViewById(R.id.newImage);

        goBack = (Button) view.findViewById(R.id.postTopBack);
        topDone = (Button) view.findViewById(R.id.postTopDone);
        postPic = (Button) view.findViewById(R.id.postPic);

        tvPriceSymbol = (TextView) view.findViewById(R.id.tv_price_symbol);

        description = (EditText) view.findViewById(R.id.description);
        price = (EditText) view.findViewById(R.id.price);

        priceSwitch = (Switch) view.findViewById(R.id.priceSwitch);
        privateSwitch = (Switch) view.findViewById(R.id.privateSwitch);
        watermarkSwitch = (Switch) view.findViewById(R.id.watermarkSwitch);

        watermark = (Spinner) view.findViewById(R.id.watermark);
        category = (Spinner) view.findViewById(R.id.category);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        goBack.setOnClickListener(this);
        topDone.setOnClickListener(this);
        postPic.setOnClickListener(this);
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
}
