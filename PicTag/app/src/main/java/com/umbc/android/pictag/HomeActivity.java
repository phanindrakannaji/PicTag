package com.umbc.android.pictag;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class HomeActivity extends AppCompatActivity{

    private static final int PICK_A_PHOTO = 100;
    private static final String TAG = "HomeActivity";
    private static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 110;
    CameraFragment cameraFragment;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private UserProfile userProfile;
    private StorageReference mStorageRef;
    private String mCurrentPhotoPath;
    private String imageFileName;
    SharedPreferences data;
    BottomNavigationView navigation;
    Handler handler = new Handler();


    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            Fragment selectedFragment = null;
            switch (item.getItemId()) {
                case R.id.navigation_newsfeed:
                    selectedFragment = NewsFeedFragment.newInstance();
                    break;
                case R.id.navigation_search:
                    selectedFragment = SearchFragment.newInstance();
                    break;
                case R.id.navigation_camera:
                    selectedFragment = CameraFragment.newInstance();
                    cameraFragment = (CameraFragment) selectedFragment;
                    takeAndSavePic();
                    break;
                case R.id.navigation_tags:
                    selectedFragment = TagsFragment.newInstance();
                    loadTags();
                    break;
                case R.id.navigation_profile:
                    selectedFragment = ProfileFragment.newInstance();
                    break;
            }
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.display_fragment, selectedFragment);
            transaction.commit();
            return true;
        }

    };

    public void displayNewsFeed(){
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.display_fragment, NewsFeedFragment.newInstance()).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .addToBackStack(null);
        transaction.commit();
        navigation.getMenu().getItem(0).setChecked(true);
        navigation.getMenu().getItem(1).setChecked(false);
        navigation.getMenu().getItem(2).setChecked(false);
        navigation.getMenu().getItem(3).setChecked(false);
        navigation.getMenu().getItem(4).setChecked(false);
    }

    private void loadTags() {

    }

    public UserProfile getUserProfile(){
        return userProfile;
    }

    private void takeAndSavePic() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            if (photoFile != null) {
                startActivityForResult(takePictureIntent, PICK_A_PHOTO);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    takeAndSavePic();
                } else {

                }
                return;
            }
        }
    }

    private void requestPermissions(){
        if (ContextCompat.checkSelfPermission(this, // request permission when it is not granted.
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            Log.d("myAppName", "permission:WRITE_EXTERNAL_STORAGE: NOT granted!");
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
            }
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        imageFileName = "PNG_" + timeStamp + "_";

        File storageDir = new File(Environment.getExternalStorageDirectory() + File.separator + "DCIM" + File.separator + "PicTag");
        boolean success = storageDir.mkdirs();
        Log.d("CHECKIFEXISTS", String.valueOf(success));
        requestPermissions();
        //File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".png",         /* suffix */
                storageDir      /* directory */
        );
        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(mCurrentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PICK_A_PHOTO) {
            if (resultCode == RESULT_OK) {
                Bundle extras = data.getExtras();
                final Bitmap imageBitmap = (Bitmap) extras.get("data");
                try {
                    FileOutputStream fileOutputStream = new FileOutputStream(new File(mCurrentPhotoPath));
                    if (imageBitmap != null) {
                        imageBitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream);
                    }
                    fileOutputStream.flush();
                    fileOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                galleryAddPic();

                Uri file = Uri.fromFile(new File(mCurrentPhotoPath));
                StorageReference riversRef = mStorageRef.child("images/"+imageFileName);

                riversRef.putFile(file)
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                // Get a URL to the uploaded content
                                Uri downloadUrl = taskSnapshot.getDownloadUrl();
                                cameraFragment.setNewImage(imageBitmap);
                                if (downloadUrl != null) {
                                    cameraFragment.setDownloadUrl(downloadUrl.toString());
                                }
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                // Handle unsuccessful uploads
                                // ...
                            }
                        });
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    Intent myIntent = new Intent(HomeActivity.this, LoginActivity.class);
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

        mStorageRef = FirebaseStorage.getInstance().getReference();

        navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.home_menu, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.homemenu_logout:
                FirebaseAuth.getInstance().signOut();
                return true;
            case R.id.homemenu_help:
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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
