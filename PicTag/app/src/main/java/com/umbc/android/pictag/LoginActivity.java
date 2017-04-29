package com.umbc.android.pictag;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
import java.util.ArrayList;
import java.util.List;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener{

    private static final String TAG = "LoginActivity";
    EditText loginEmail, loginPassword;
    Button loginButton, signUpButton;
    UserProfile userProfile;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        loginEmail = (EditText) findViewById(R.id.login_email);
        loginPassword = (EditText) findViewById(R.id.login_password);
        loginButton = (Button) findViewById(R.id.normalLoginButton);
        signUpButton = (Button) findViewById(R.id.signUpButton);

        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    String[] input = new String[3];
                    UserTask userTask = new UserTask();
                    userTask.execute(input);
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
        loginButton.setOnClickListener(this);
        signUpButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.normalLoginButton:
                mAuth.signInWithEmailAndPassword(loginEmail.getText().toString(), loginPassword.getText().toString())
                        .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                Log.d(TAG, "signInWithEmail:onComplete:" + task.isSuccessful());
                                // If sign in fails, display a message to the user. If sign in succeeds
                                // the auth state listener will be notified and logic to handle the
                                // signed in user can be handled in the listener.
                                if (!task.isSuccessful()) {
                                    Log.w(TAG, "signInWithEmail:failed", task.getException());
                                    Toast.makeText(LoginActivity.this, R.string.auth_failed,
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                break;
            case R.id.signUpButton:
                Intent myIntent = new Intent(LoginActivity.this, SignUpActivity.class);
                startActivity(myIntent);
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
            String requestUrl = "";
            if (strings[0].equalsIgnoreCase("login")){
                requestUrl = domain + "/pictag/login.php";
            } else if (strings[0].equalsIgnoreCase("register")){
                requestUrl = domain + "/pictag/registerUser.php";
            }
            List<UserProfile> users = new ArrayList<>();
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

                String requestJsonString = "";
                if (strings[0].equalsIgnoreCase("login")){
                    requestJsonString = new JSONObject()
                            .put("email", strings[1])
                            .toString();
                } else if (strings[0].equalsIgnoreCase("register")){
                    requestJsonString = new JSONObject()
                            .put("email", strings[1])
                            .put("fullName", strings[2])
                            .put("password", strings[3])
                            .toString();
                }

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
                }
                Log.d("RESPONSE BODY: ", response);

                JSONArray jsonArray = new JSONArray(response);
                if(jsonArray.length() > 0){
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
                        } else if (childJsonObj.getString("status").equalsIgnoreCase("F")){
                            error = childJsonObj.getString("errorMessage");
                            DisplayToast displayToast = new DisplayToast(error);
                            handler.post(displayToast);
                        }
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
            if (error.equalsIgnoreCase("")) {
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
                Intent myIntent = new Intent(LoginActivity.this, HomeActivity.class);
                startActivity(myIntent);
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
