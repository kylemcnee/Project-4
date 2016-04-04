package com.example.kylemcnee.headshot;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class HuntingActivity extends AppCompatActivity {
    String BASE_URL_ENROLL;
    String BASE_URL_RECOGNIZE;
    String TARGET_NAME;
    Bitmap ENROLL_IMAGE;
    Bitmap HEADSHOT_IMAGE;
    String key;
    String appID;
    String contentType;
    TextView mPreyName;
    TextView mHeadline;
    ImageButton mCrosshair;
    final static int REQUEST_HEADSHOT_CAPTURE = 1;
    JSONObject jsonObject;
    JSONArray jsonArray;
    boolean preyEliminated = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hunting);
        BASE_URL_ENROLL = getResources().getString(R.string.BASE_URL_ENROLL);
        BASE_URL_RECOGNIZE = getResources().getString(R.string.BASE_URL_RECOGNIZE);
        key = getResources().getString(R.string.API_KEY);
        appID = getResources().getString(R.string.APP_ID);
        contentType = getResources().getString(R.string.CONTENT_TYPE);

        mPreyName = (TextView) findViewById(R.id.target_name);
        mHeadline = (TextView) findViewById(R.id.target_text);
        mCrosshair = (ImageButton) findViewById(R.id.crosshair);

        //Grabs the bitmap to be enrolled in Kairos API gallery, as well as the target name.
        Intent i = getIntent();
        ENROLL_IMAGE = i.getParcelableExtra("enroll_bitmap");
        TARGET_NAME = i.getStringExtra("target_name");
        mPreyName.setText(TARGET_NAME);

        //Calls Kairos API to enroll target image
        try {
            enrollFace(BASE_URL_ENROLL, encodeImage(ENROLL_IMAGE));
        } catch (IOException e) {
            Toast.makeText(HuntingActivity.this, "ERROR: Please Try Again", Toast.LENGTH_SHORT).show();
        }

        //Clicking the crosshair allows the user to attempt a HeadShot
        mCrosshair.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takeHeadshot();
            }
        });
    }

    private void recognizeFace(String matchURL, String encodedImage) throws IOException {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = connectivityManager.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnected()){
            InputStream stream = null;
            try {
                URL url = new URL(matchURL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);

                conn.setRequestProperty("app_key", key);
                conn.setRequestProperty("app_id", appID);
                conn.setRequestProperty("Content-Type", contentType);
                conn.connect();

                stream = conn.getInputStream();

                OutputStream os = conn.getOutputStream();
                //    {"url":"http://i.imgur.com/v1WGb0K.jpg","gallery_name":"test"}
                //TODO what do we call the first param?
                String JSONbody = "{\"url\":"+"\""+encodedImage+"\","+"\"gallery_name\":"+"\""+TARGET_NAME+"\"}";
                byte[] byteArray = JSONbody.getBytes();
                os.write(byteArray);
                os.flush();
                os.close();

                //TODO is this gonna work?
                String jsonAsString = decodeJSON(stream);
                if (jsonAsString.contains("success")){
                    preyEliminated = true;
                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
            }finally {
                if (stream != null){
                    stream.close();
                }
            }
        }else{
            Toast.makeText(HuntingActivity.this, "Please check network connection", Toast.LENGTH_LONG).show();
        }


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
                String JSONbody = "{\"url\":"+"\""+encodedImage+"\","+"\"gallery_name\":"+"\""+TARGET_NAME+"\","+"\"subject_id\":"+"\""+TARGET_NAME+"\"}";
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

    private void takeHeadshot(){
        // Launches Android camera app to take photo
        Intent takePicIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePicIntent.resolveActivity(getPackageManager()) != null){
            startActivityForResult(takePicIntent, REQUEST_HEADSHOT_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_HEADSHOT_CAPTURE && resultCode == RESULT_OK){
            HEADSHOT_IMAGE = (Bitmap) data.getExtras().get("data");
            try {
                recognizeFace(BASE_URL_RECOGNIZE, encodeImage(HEADSHOT_IMAGE));

            } catch (IOException e) {
                e.printStackTrace();
            }

            if (preyEliminated){
                Toast.makeText(HuntingActivity.this, "PREY SLAUGHTERED", Toast.LENGTH_LONG).show();
                mPreyName.setPaintFlags(mPreyName.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            }else{
                Toast.makeText(HuntingActivity.this, "SHOT MISSED!", Toast.LENGTH_SHORT).show();
            }

        }

    }

    public String decodeJSON(InputStream stream) throws IOException {
        StringBuilder sb = new StringBuilder();
        BufferedReader br = new BufferedReader(new InputStreamReader(stream));
        String stringData;

        while ((stringData = br.readLine()) != null){
            sb.append(stringData);
        }
        return sb.toString();
    }

    public String encodeImage(Bitmap bitmap){
        // Converts a bitmap into Base64 encoded image to be used in the Kairos API call
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] byteFormat = stream.toByteArray();

        return Base64.encodeToString(byteFormat, Base64.NO_WRAP);
    }

}