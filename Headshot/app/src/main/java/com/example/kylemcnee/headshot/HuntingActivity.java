package com.example.kylemcnee.headshot;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class HuntingActivity extends AppCompatActivity {

    String BASE_URL_ENROLL = getResources().getString(R.string.BASE_URL_ENROLL);
    String TARGET_NAME;
    Bitmap ENROLL_IMAGE;
    Bitmap HEADSHOT_IMAGE;
    String key = getResources().getString(R.string.API_KEY);
    String appID = getResources().getString(R.string.APP_ID);
    String contentType = getResources().getString(R.string.CONTENT_TYPE);
    TextView mPreyName;
    ImageButton mCrosshair;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hunting);

        mPreyName = (TextView) findViewById(R.id.target_text);
        mCrosshair = (ImageButton) findViewById(R.id.crosshair);

        Intent i = getIntent();
        ENROLL_IMAGE = i.getParcelableExtra("enroll_bitmap");
        TARGET_NAME = i.getStringExtra("target_name");

        try {
            enrollFace(BASE_URL_ENROLL, encodeImage(ENROLL_IMAGE));
        } catch (IOException e) {
            Toast.makeText(HuntingActivity.this, "ERROR: Please Try Again", Toast.LENGTH_SHORT).show();
        }

        mCrosshair.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    private void enrollFace(String matchURL, String encodedImage) throws IOException {
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
            //    {"url":"http://i.imgur.com/v1WGb0K.jpg","gallery_name":"test","subject_id":"kyle"}
                //TODO what do we call the first param?
                String JSONbody = "{&quot;base64&quot;:"+"&quot;"+encodedImage
                        +"&quot;,"+"&quot;gallery_name&quot;:"+"&quot;"+TARGET_NAME
                        +"&quot;,"+"&quot;subject_id&quot;:"+"&quot;"+TARGET_NAME+"&quot;}";
                byte[] byteArray = JSONbody.getBytes();
                os.write(byteArray);
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
            Toast.makeText(HuntingActivity.this, "Please Check Network Connection", Toast.LENGTH_LONG).show();
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