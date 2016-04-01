package com.example.kylemcnee.headshot;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class KillList extends AppCompatActivity {
    ImageButton mCrosshair;
    TextView mTitle;
    EditText mTarget;
    final static int REQUEST_IMAGE_CAPTURE = 1;
    String GALLERY_NAME;
    String key = getResources().getString(R.string.API_KEY);
    String appID = getResources().getString(R.string.APP_ID);
    String contentType = getResources().getString(R.string.CONTENT_TYPE);
    String baseURL = getResources().getString(R.string.BASE_URL);


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kill_list);

        mCrosshair = (ImageButton) findViewById(R.id.crosshair);
        mTitle = (TextView) findViewById(R.id.title_text);
        mTarget = (EditText) findViewById(R.id.target_edittext);

        // When the user clicks the crosshair, the camera is launched
        mCrosshair.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePictureIntent();
            }
        });
    }

    private void takePictureIntent(){
        // Launches Android camera app to take photo
        Intent takePicIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePicIntent.resolveActivity(getPackageManager()) != null){
            startActivityForResult(takePicIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Retrieves the bitmap of the photo taken by the user
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK){
            Bitmap photo = (Bitmap)data.getExtras().get("data");
        }
    }

    private void enrollFace(String matchURL) throws IOException {
        // This call will place an initial photo of the user in a "gallery" hosted by Kairos.  Further photos will be matched against it.
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            // If the connection is available:
            InputStream is = null;
            try {
                URL url = new URL(matchURL);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                connection.setRequestMethod("POST");
                connection.setDoInput(true);
                connection.setDoOutput(true);

                connection.setRequestProperty("app_key", key);
                connection.setRequestProperty("app_id", appID);
                connection.setRequestProperty("Content-Type", contentType);
                connection.connect();

                is = connection.getInputStream();

                OutputStream os = connection.getOutputStream();
                //TODO write the JSON object as part of the OutputStream
                os.write();
                os.flush();
                os.close();

            } catch (MalformedURLException e) {
                e.printStackTrace();
            }finally {
                if (is != null){
                    is.close();
                }
            }

        } else {
            Toast.makeText(KillList.this, "Please Check Network Connection", Toast.LENGTH_LONG).show();
        }
    }

    public String encodeImage(Bitmap bitmap){
        // Converts a bitmap into Base64 encoded image to be used in the Kairos API call
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] byteFormat = stream.toByteArray();

        return Base64.encodeToString(byteFormat, Base64.NO_WRAP);
    }
}
