package com.example.kylemcnee.headshot;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class KillList extends AppCompatActivity {
    ImageButton mCrosshair;
    TextView mTitle;
    ListView mKillList;
    final static int REQUEST_IMAGE_CAPTURE = 1;
    String GALLERY_NAME;
    String key = getResources().getString(R.string.API_KEY);
    String appID = getResources().getString(R.string.APP_ID);
    String baseURL = getResources().getString(R.string.BASE_URL);


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kill_list);

        mCrosshair = (ImageButton) findViewById(R.id.crosshair);
        mTitle = (TextView) findViewById(R.id.title_text);
        mKillList = (ListView) findViewById(R.id.kill_listview);

        mCrosshair.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePictureIntent();
            }
        });
    }

    private void takePictureIntent(){
        Intent takePicIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePicIntent.resolveActivity(getPackageManager()) != null){
            startActivityForResult(takePicIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    private boolean enrollFace(String matchURL) throws IOException {
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            // the connection is available
            InputStream is = null;
            try {
                URL url = new URL(matchURL);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setDoInput(true);
                connection.setDoOutput(true);
                connection.setRequestProperty("app_key", key);
                connection.setRequestProperty("app_id", appID);
                connection.connect();
                is = connection.getInputStream();

                OutputStream os = connection.getOutputStream();
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
            // the connection is not
            Toast.makeText(KillList.this, "Please Check Network Connection", Toast.LENGTH_LONG).show();
        }
    }
}
