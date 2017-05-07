package com.umbc.android.pictag;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.internal.BottomNavigationMenuView;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import clarifai2.api.ClarifaiBuilder;
import clarifai2.api.ClarifaiClient;
import clarifai2.api.ClarifaiResponse;
import clarifai2.dto.input.ClarifaiInput;
import clarifai2.dto.input.image.ClarifaiImage;
import clarifai2.dto.model.output.ClarifaiOutput;
import clarifai2.dto.prediction.Concept;

public class HomeActivity extends AppCompatActivity{

    private static final int PICK_A_PHOTO = 100;
    private static final int PICK_FROM_GALLERY = 101;
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
                    selectedFragment = NewsFeedFragment.newInstance(0);
                    break;
                case R.id.navigation_search:
                    selectedFragment = SearchFragment.newInstance();
                    break;
                case R.id.navigation_camera:
                    selectedFragment = CameraFragment.newInstance();
                    cameraFragment = (CameraFragment) selectedFragment;
                    final CharSequence[] options = { "Click epic", "Choose from Gallery","Cancel" };

                    AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);

                    builder.setTitle("Add Photo!");

                    builder.setItems(options, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int item) {
                            if (options[item].equals("Click epic")) {
                                takeAndSavePic();
                            } else if (options[item].equals("Choose from Gallery")) {
                                Intent intent = new   Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                                startActivityForResult(intent, PICK_FROM_GALLERY);
                            } else if (options[item].equals("Cancel")) {
                                dialog.dismiss();
                                displayNewsFeed();
                            }
                        }
                    });
                    builder.show();
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
    private ClarifaiClient client;

    public void displayNewsFeed(){
        View view = navigation.findViewById(R.id.navigation_newsfeed);
        view.performClick();
    }

    private void loadTags() {

    }

    public UserProfile getUserProfile(){
        return userProfile;
    }

    public void setUserProfile(UserProfile userProfile){
        this.userProfile = userProfile;
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
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.umbc.android.pictag",
                        photoFile);
                getApplicationContext().grantUriPermission("com.umbc.android.pictag", photoURI, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                Log.d("Photo URI: ", photoURI.toString());
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, PICK_A_PHOTO);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    takeAndSavePic();
                } else {
                    handler.post(new DisplayToast("Permissions not provided"));
                }
                break;
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

        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        boolean success = storageDir.mkdirs();
        Log.d("CHECKIFEXISTS", String.valueOf(success));
        requestPermissions();
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".png",         /* suffix */
                storageDir      /* directory */
        );
        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        Log.d("PATH OF IMAGE" , mCurrentPhotoPath);
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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PICK_A_PHOTO) {
            if (resultCode == RESULT_OK) {
                galleryAddPic();
                Uri file = Uri.fromFile(new File(mCurrentPhotoPath));
                Log.d("Sending file path: ", file.getPath());
                StorageReference riversRef = mStorageRef.child("images/"+imageFileName);
                riversRef.putFile(file)
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                // Get a URL to the uploaded content
                                Uri downloadUrl = taskSnapshot.getDownloadUrl();
                                // Get the dimensions of the View
                                int targetW = 200;
                                int targetH = 200;

                                // Get the dimensions of the bitmap
                                BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                                bmOptions.inJustDecodeBounds = true;
                                BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
                                int photoW = bmOptions.outWidth;
                                int photoH = bmOptions.outHeight;

                                // Determine how much to scale down the image
                                int scaleFactor = Math.min(photoW/targetW, photoH/targetH);

                                // Decode the image file into a Bitmap sized to fill the View
                                bmOptions.inJustDecodeBounds = false;
                                bmOptions.inSampleSize = scaleFactor;
                                Bitmap imageBitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
                                cameraFragment.setNewImage(imageBitmap);

                                if (downloadUrl != null) {
                                    cameraFragment.setDownloadUrl(downloadUrl.toString());
                                    String[] input = new String[1];
                                    input[0] = downloadUrl.toString().replace("\\/", "/").replace("//", "/").replace("https:/", "https://");
                                    ClarifaiTask clarifaiTask = new ClarifaiTask();
                                    clarifaiTask.execute(input);
                                }
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                // Handle unsuccessful uploads
                                // ...
                                Log.d("FirebaseException: ", exception.getMessage());
                                exception.printStackTrace();
                            }
                        });
            } else{
                Log.d("Error in capture intent", String.valueOf(resultCode));
                Bundle extras = data.getExtras();
            }
        } else if (requestCode == PICK_FROM_GALLERY){
            if (resultCode == RESULT_OK) {
                Uri selectedImage = data.getData();
                String[] filePath = {MediaStore.Images.Media.DATA};
                Cursor c = getContentResolver().query(selectedImage, filePath, null, null, null);
                if (c != null) {
                    c.moveToFirst();
                    int columnIndex = c.getColumnIndex(filePath[0]);
                    mCurrentPhotoPath = c.getString(columnIndex);
                    c.close();
                    //Bitmap thumbnail = (BitmapFactory.decodeFile(mCurrentPhotoPath));
                }
                Uri file = Uri.fromFile(new File(mCurrentPhotoPath));
                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                imageFileName = "PNG_" + timeStamp + ".png";
                Log.d("Sending file path: ", file.getPath());
                StorageReference riversRef = mStorageRef.child("images/"+imageFileName);
                riversRef.putFile(file)
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                // Get a URL to the uploaded content
                                Uri downloadUrl = taskSnapshot.getDownloadUrl();
                                // Get the dimensions of the View
                                int targetW = 200;
                                int targetH = 200;

                                // Get the dimensions of the bitmap
                                BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                                bmOptions.inJustDecodeBounds = true;
                                BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
                                int photoW = bmOptions.outWidth;
                                int photoH = bmOptions.outHeight;

                                // Determine how much to scale down the image
                                int scaleFactor = Math.min(photoW/targetW, photoH/targetH);

                                // Decode the image file into a Bitmap sized to fill the View
                                bmOptions.inJustDecodeBounds = false;
                                bmOptions.inSampleSize = scaleFactor;
                                Bitmap imageBitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
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
                                Log.d("FirebaseException: ", exception.getMessage());
                                exception.printStackTrace();
                            }
                        });
            } else{

            }
        }
    }

    public class ClarifaiTask extends AsyncTask<String, Void, ClarifaiResponse<List<ClarifaiOutput<Concept>>>> {
        @Override protected ClarifaiResponse<List<ClarifaiOutput<Concept>>> doInBackground(String... params) {
            return client.getDefaultModels().generalModel().predict()
                    .withInputs(
                            ClarifaiInput.forImage(ClarifaiImage.of(String.valueOf(params[0])))
                    )
                    .executeSync();
        }

        @Override protected void onPostExecute(ClarifaiResponse<List<ClarifaiOutput<Concept>>> response) {
            if (!response.isSuccessful()) {
                handler.post(new DisplayToast("Error in Clarifai"));
                return;
            }
            final List<ClarifaiOutput<Concept>> predictions = response.get();
            if (predictions.isEmpty()) {
                handler.post(new DisplayToast("Error in Clarifai: No results"));
                return;
            }
            Log.d("PREDICTIONS", predictions.get(0).data().get(0).name());
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
        BottomNavigationMenuView menuView = (BottomNavigationMenuView) navigation.getChildAt(0);
        for (int i = 0; i < menuView.getChildCount(); i++) {
            final View iconView = menuView.getChildAt(i).findViewById(android.support.design.R.id.icon);
            final ViewGroup.LayoutParams layoutParams = iconView.getLayoutParams();
            final DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
            // set your height here
            layoutParams.height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 26, displayMetrics);
            // set your width here
            layoutParams.width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 26, displayMetrics);
            iconView.setLayoutParams(layoutParams);
        }
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        displayNewsFeed();
        client = new ClarifaiBuilder(getString(R.string.clarifaiClientId), getString(R.string.clarifaiClientSecret)).buildSync();
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
