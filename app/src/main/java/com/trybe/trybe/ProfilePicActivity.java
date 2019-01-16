package com.trybe.trybe;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.squareup.picasso.Picasso;
import com.trybe.trybe.app.Config;
import com.trybe.trybe.dto.LoginDTO;
import com.trybe.trybe.gcm.NotificationUtils;
import com.trybe.trybe.helper.UserSessionManager;
import com.trybe.trybe.helper.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

import cz.msebera.android.httpclient.Header;
import de.hdodenhof.circleimageview.CircleImageView;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class ProfilePicActivity extends AppCompatActivity {

    ImageView profilePic, openGallery, openCamera;

    private UserSessionManager session;
    private LoginDTO user;
    private static final int PICKFILE_REQUEST_CODE = 101;
    private static final int CAMERA_REQUEST_CODE = 102;

    private static final int REQUEST_EXTERNAL_STORAGE_PERM = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private static final int REQUEST_CAMERA_PERM = 2;
    private static String[] PERMISSIONS_CAMERA = {
            Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private BroadcastReceiver mRegistrationBroadcastReceiver;
    private String mCurrentPhotoPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_pic);

        session = UserSessionManager.getInstance();
        user = session.getUserProfile();

        String userImageUrl = user.user_def.UD_IMG;
        profilePic = (CircleImageView) findViewById(R.id.profilePic);
        Utils.loadImageByPicasso(getApplicationContext(), profilePic, userImageUrl);

        ImageButton done = (ImageButton) findViewById(R.id.done);
        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        openGallery = (ImageView) findViewById(R.id.galleryIcon);
        openCamera = (ImageView) findViewById(R.id.cameraIcon);
        TextView galleryText = (TextView) findViewById(R.id.galleryText);
        TextView cameraText = (TextView) findViewById(R.id.cameraText);

        openGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openGallery();
            }
        });
        galleryText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openGallery();
            }
        });
        openCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openCamera();
            }
        });
        cameraText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openCamera();
            }
        });

        mRegistrationBroadcastReceiver = Utils.getDefaultGCMBroadcastReceiver(getApplicationContext());
    }

    @Override
    public void onResume() {
        super.onResume();
        Utils.registerGCMReceiver(ProfilePicActivity.this, mRegistrationBroadcastReceiver);
        NotificationUtils.clearNotifications();
    }

    @Override
    public void onPause() {
        Utils.unregisterGCMReceiver(ProfilePicActivity.this, mRegistrationBroadcastReceiver);
        super.onPause();
    }

    private void openGallery() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
            int permission = checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);

            if (permission != PackageManager.PERMISSION_GRANTED) {
                // We don't have permission so prompt the user
                requestPermissions(
                        PERMISSIONS_STORAGE,
                        REQUEST_EXTERNAL_STORAGE_PERM
                );
            } else {
                startImageChooserIntent();
            }
        } else {
            startImageChooserIntent();
        }
    }

    private void openCamera() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
            int permission = checkSelfPermission(Manifest.permission.CAMERA);
            int permissionStorage = checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);

            if (permission != PackageManager.PERMISSION_GRANTED || permissionStorage != PackageManager.PERMISSION_GRANTED) {
                // We don't have permission so prompt the user
                requestPermissions(
                        PERMISSIONS_CAMERA,
                        REQUEST_CAMERA_PERM
                );
            } else {
                showCamera();
            }
        } else {
            showCamera();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_EXTERNAL_STORAGE_PERM: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("test", "yay! perm granted");
                    startImageChooserIntent();
                } else {
                    Log.d("test", "perm denied");
                    Toast.makeText(ProfilePicActivity.this, "You need to provide permission to upload your profile image", Toast.LENGTH_SHORT).show();
                }
                break;
            }
            case REQUEST_CAMERA_PERM: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("test", "yay! perm granted");
                    showCamera();
                } else {
                    Log.d("test", "perm denied");
                    Toast.makeText(ProfilePicActivity.this, "You need to provide permission to access the camera", Toast.LENGTH_SHORT).show();
                }
                break;
            }
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void startImageChooserIntent() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        try {
            startActivityForResult(intent, PICKFILE_REQUEST_CODE);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(ProfilePicActivity.this, "File Manager not found. Install a file manager first.", Toast.LENGTH_SHORT).show();
        }
    }

    private File createImageFile() {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "IMG_" + timeStamp + ".jpg";
        File storageDir = Utils.getImagesFolder();
        File image = null;
        if (storageDir != null)
            image = new File(storageDir, imageFileName);

        return image;
    }

    private void showCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = createImageFile();
            if (photoFile != null) {
                // Save a file: path for use with ACTION_VIEW intents
                mCurrentPhotoPath = photoFile.getAbsolutePath();

                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                startActivityForResult(takePictureIntent, CAMERA_REQUEST_CODE);
            } else
                Toast.makeText(ProfilePicActivity.this, "IO Error while creating image file", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(ProfilePicActivity.this, "No camera hardware found.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case PICKFILE_REQUEST_CODE:
                if (resultCode == RESULT_OK && data.getData() != null) {
                    // TODO check size, file extension & other file constraints

                    Uri uri = data.getData();
                    Log.d("test", "File Uri: " + uri.toString());
                    String path = Utils.getPath(ProfilePicActivity.this, uri);
                    Log.d("test", "File Path: " + uri.getPath());
                    Log.d("test", "FileUtils Path: " + path);

                    if (path != null) {
                        File imageFile = new File(path);
                        Log.d("test", "Image exists: " + (imageFile.exists() ? "true" : "false"));

                        // copy file to trybe folder
                        // File newImageFile = new File(Utils.getImagesFolder(), user.user_def.UD_ID + ".jpg");
                        File compressedImage = scaleAndCompressImage(imageFile);
                        // compress copies the image to trybe folder so no need to copy
                        // Utils.copyFile(compressedImage, newImageFile);

                        // Initiate the upload
                        uploadImage(compressedImage);
                    } else {
                        Toast.makeText(ProfilePicActivity.this, "Invalid file", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.d("test", resultCode + "");
                    Toast.makeText(ProfilePicActivity.this, "No file selected", Toast.LENGTH_SHORT).show();
                }
                break;
            case CAMERA_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    // Bundle extras = data.getExtras();
                    //Bitmap imageBitmap = (Bitmap) extras.get("data");
                    //mImageView.setImageBitmap(imageBitmap);

                    // TODO handle screen rotation
                    if (mCurrentPhotoPath != null) {
                        File origPhotoFile = new File(mCurrentPhotoPath);
                        File compressedImage = scaleAndCompressImage(origPhotoFile);
                        Utils.deleteFile(origPhotoFile);
                        uploadImage(compressedImage);
                    } else {
                        Toast.makeText(ProfilePicActivity.this, "IO Error", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(ProfilePicActivity.this, "Picture wasn't taken", Toast.LENGTH_SHORT).show();
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void uploadImage(final File imageFile) {
        if (!checkValidImage(imageFile)) {
            return;
        }

        final MaterialDialog uploadProgressDialog = new MaterialDialog.Builder(ProfilePicActivity.this)
                .title("Uploading Image")
                .content("Please wait")
                .progress(true, 0)
                .cancelable(false)
                .show();

        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        try {
            params.put("FileToUpload", imageFile);
        } catch (FileNotFoundException e) {
            Log.d("test", "file not found");
            uploadProgressDialog.dismiss();
            Toast.makeText(ProfilePicActivity.this, "IO Error", Toast.LENGTH_SHORT).show();
            return;
        }
        params.put("TYPE", "profilepic");
        params.put("UD_ID", user.user_def.UD_ID);
        client.setTimeout(20 * 1000);
        client.post(this, Config.Server.FILE_UPLOAD_URL, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                uploadProgressDialog.dismiss();
                Log.d("test", response.toString());
                try {
                    String url = (String) response.get("FILEURL");
                    url.replaceAll("\\\\", "");
                    Log.d("test", "image url: " + url);
                    Utils.loadImageByPicasso(getApplicationContext(), profilePic, url);
                    Picasso.with(getApplicationContext())
                            .load(imageFile)
                            .resize(200, 200)
                            .centerCrop()
                            .into(profilePic);
                    user.user_def.UD_IMG = url;
                    session.setUserProfile(user);
                    finish();
                    Toast.makeText(ProfilePicActivity.this, "Image successfully updated", Toast.LENGTH_SHORT).show();
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(ProfilePicActivity.this, "Server error", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(ProfilePicActivity.this, "Server error", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable t, JSONObject errorResponse) {
                uploadProgressDialog.dismiss();
                // called when response HTTP status is "4XX" (eg. 401, 403, 404)
                Log.d("test", "error: " + t.getLocalizedMessage());
                if (errorResponse != null)
                    Log.d("test", errorResponse.toString());
                Toast.makeText(ProfilePicActivity.this, "Network error", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String res, Throwable t) {
                uploadProgressDialog.dismiss();
                // called when response HTTP status is "4XX" (eg. 401, 403, 404)
                Log.d("test", "error: " + t.getLocalizedMessage());
                Log.d("test", res);
                Toast.makeText(ProfilePicActivity.this, "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private File scaleAndCompressImage(File f) {
        return scaleAndCompressImage(f, 200);
    }

    private File scaleAndCompressImage(File f, int requiredSize) {
        try {
            // Decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(new FileInputStream(f), null, o);

            // The new size we want to scale to
            final int REQUIRED_SIZE = requiredSize;

            // Find the correct scale value. It should be the power of 2.
            int scale = 1;
            while (o.outWidth / scale / 2 >= REQUIRED_SIZE &&
                    o.outHeight / scale / 2 >= REQUIRED_SIZE) {
                scale *= 2;
            }

            // Decode with inSampleSize
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inJustDecodeBounds = false;
            o2.inSampleSize = scale;
            Bitmap scaledBitmap = BitmapFactory.decodeStream(new FileInputStream(f), null, o2);
            File outputImage = createImageFile();
            FileOutputStream outStream = new FileOutputStream(outputImage);
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 75, outStream);
            return outputImage;
        } catch (FileNotFoundException e) {
            Log.d("COMPRESS", "IO Error");
            e.printStackTrace();
        }
        return null;
    }

    private boolean checkValidImage(File image) {
        boolean check = true;

        long fileSize = image.length() / 1024;
        if (fileSize >= 5 * 1024) {
            Toast.makeText(ProfilePicActivity.this, "Image size must be less than 5MB", Toast.LENGTH_SHORT).show();
            check = false;
        }

        String filePath = image.getAbsolutePath();
        String extn = filePath.substring(filePath.lastIndexOf(".") + 1);
        String[] validExtns = {"jpg", "jpeg", "png"};
        if (!Arrays.asList(validExtns).contains(extn)) {
            Toast.makeText(ProfilePicActivity.this, "Invalid file type", Toast.LENGTH_SHORT).show();
            check = false;
        }

        return check;
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }
}