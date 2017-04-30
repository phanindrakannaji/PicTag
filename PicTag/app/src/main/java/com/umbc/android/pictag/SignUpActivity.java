package com.umbc.android.pictag;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

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

public class SignUpActivity extends AppCompatActivity implements View.OnClickListener{

    private static final String TAG = "SignupActivity";
    TextInputEditText firstName, lastName, email, mobileNumber, dateOfBirth, password;
    RadioGroup radioGroup;
    RadioButton gender;
    Button signupButton;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    UserProfile userProfile;
    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        firstName = (TextInputEditText) findViewById(R.id.signupFirstName);
        lastName = (TextInputEditText) findViewById(R.id.signupLastName);
        email = (TextInputEditText) findViewById(R.id.signupEmail);
        mobileNumber = (TextInputEditText) findViewById(R.id.signupPhoneNumber);
        dateOfBirth = (TextInputEditText) findViewById(R.id.signupDob);
        password = (TextInputEditText) findViewById(R.id.signupPassword);

        radioGroup = (RadioGroup) findViewById(R.id.signupGender);

        signupButton = (Button) findViewById(R.id.signupButton);

        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    String[] input = new String[7];
                    input[0] = email.getText().toString();
                    input[1] = firstName.getText().toString();
                    input[2] = lastName.getText().toString();
                    input[3] = "";
                    input[4] = dateOfBirth.getText().toString();
                    input[5] = gender.getText().toString();
                    input[6] = "";
                    UserTask signUpUserTask = new UserTask();
                    signUpUserTask.execute(input);

                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
                // ...
            }
        };
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        signupButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.signupButton:
                int selectedId = radioGroup.getCheckedRadioButtonId();
                gender = (RadioButton) findViewById(selectedId);
                String error = "";
                if (firstName.getText().toString().equals("")){
                    error = "First name cannot be blank!";
                } else if (lastName.getText().toString().equals("")){
                    error = "Last name cannot be blank!";
                } else if (email.getText().toString().equals("")){
                    error = "Email cannot be blank!";
                } else if (mobileNumber.getText().toString().equals("")){
                    error = "Mobile number cannot be blank!";
                } else if (dateOfBirth.getText().toString().equals("")){
                    error = "Date of birth cannot be blank!";
                } else if (gender.getText().toString().equals("")){
                    error = "Gender cannot be blank!";
                } else if (password.getText().toString().equals("")){
                    error = "Password cannot be blank!";
                } else {
                    mAuth.createUserWithEmailAndPassword(email.getText().toString(), password.getText().toString())
                            .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    Log.d(TAG, "createUserWithEmail:onComplete:" + task.isSuccessful());

                                    // If sign in fails, display a message to the user. If sign in succeeds
                                    // the auth state listener will be notified and logic to handle the
                                    // signed in user can be handled in the listener.
                                    if (!task.isSuccessful()) {
                                        Toast.makeText(SignUpActivity.this, R.string.auth_failed,
                                                Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
                if (!error.equals("")){
                    DisplayToast displayToast = new DisplayToast(error);
                    handler.post(displayToast);
                }
                break;
        }
    }

    private class UserTask extends AsyncTask<String, Integer, UserProfile> {

        String error = "";
        @Override
        protected UserProfile doInBackground(String... strings) {
            URL url;
            String response = "";
            String domain = getString(R.string.domain);
            String requestUrl = domain + "/pictag/registerUser.php";
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
                        .put("email", strings[0])
                        .put("firstName", strings[1])
                        .put("lastName", strings[2])
                        .put("fbProfileId", strings[3])
                        .put("dob", strings[4])
                        .put("gender", strings[5])
                        .put("profilePicUrl", strings[6])
                        .toString();

                Log.d("SIGNUP REQUEST BODY : ", requestJsonString);
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
                                DisplayToast displayToast = new DisplayToast(error);
                                handler.post(displayToast);
                            }
                        }
                    } else {
                        error = "Sign Up failed!!";
                        DisplayToast displayToast = new DisplayToast(error);
                        handler.post(displayToast);
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
                Intent myIntent = new Intent(SignUpActivity.this, HomeActivity.class);
                startActivity(myIntent);
            } else{
                error = "Sign Up failed!!!";
                DisplayToast displayToast = new DisplayToast(error);
                handler.post(displayToast);
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
