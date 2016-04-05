package com.example.kylemcnee.headshot;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by Kyle McNee on 4/4/2016.
 */
public class EnrollAsync extends AsyncTask <String, Void, Void>{
    String key = "87ca62a889d69a2b3749f8ec464c112d";
    String appID = "8569521d";
    String contentType = "application/json";
    String baseURLenroll = "http://api.kairos.com/enroll";
    Context mContext;
    String mEncodedBitmap;
    String mTargetName;

    public EnrollAsync (Context context, String encodedBitmap, String targetName){
        mContext = context;
        mEncodedBitmap = encodedBitmap;
        mTargetName = targetName;
    }

    @Override
    protected Void doInBackground(String... params) {
        try {
            enrollFace(baseURLenroll);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void enrollFace(String matchURL) throws IOException {
        // This call will place an initial photo of the user in a "gallery" hosted by Kairos.  Further photos will be matched against it.
        ConnectivityManager connMgr = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
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
                String JSONbody = "{\"image\":"+"\""+mEncodedBitmap+"\","+"\"gallery_name\":"+"\""+mTargetName+"\","+"\"subject_id\":"+"\""+mTargetName+"\"}";
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
            Toast.makeText(mContext, "Please Check Network Connection", Toast.LENGTH_LONG).show();
        }
    }



}
