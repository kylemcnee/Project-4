package com.example.kylemcnee.headshot;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by Kyle McNee on 4/4/2016.
 */
public class RecognizeAsync extends AsyncTask <String, Void, Void> {

    String key = "87ca62a889d69a2b3749f8ec464c112d";
    String appID = "8569521d";
    String contentType = "application/json";
    String baseURLrecognize = "http://api.kairos.com/recognize";
    Context mContext;
    String mEncodedBitmap;
    String mTargetName;
    boolean preyEliminated = false;
    String jsonAsString;

    public RecognizeAsync (Context context, String encodedBitmap, String targetName){
        mContext = context;
        mEncodedBitmap = encodedBitmap;
        mTargetName = targetName;
    }

    @Override
    protected Void doInBackground(String... params) {
        try {
            recognizeFace(baseURLrecognize, mEncodedBitmap);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void recognizeFace(String matchURL, String encodedImage) throws IOException {
        ConnectivityManager connectivityManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
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

                String JSONbody = "{\"image\":"+"\""+encodedImage+"\","+"\"gallery_name\":"+"\""+mTargetName+"\"}";
                byte[] byteArray = JSONbody.getBytes();
                os.write(byteArray);
                os.flush();
                os.close();

                //TODO is this gonna work?
                jsonAsString = decodeJSON(stream);
                Log.d("RESPONSE", jsonAsString);
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
            Toast.makeText(mContext, "Please check network connection", Toast.LENGTH_LONG).show();
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

    @Override
    protected void onPostExecute(Void aVoid) {
        if (preyEliminated) {
            Toast.makeText(mContext, "PREY SLAUGHTERED!", Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(mContext, "SHOT MISSED!", Toast.LENGTH_SHORT).show();
        }
    }
}
