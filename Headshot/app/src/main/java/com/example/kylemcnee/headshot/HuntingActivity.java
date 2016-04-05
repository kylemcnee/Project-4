package com.example.kylemcnee.headshot;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Paint;
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;


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

        EnrollAsync enrollTask = new EnrollAsync(HuntingActivity.this, encodeImage(ENROLL_IMAGE), TARGET_NAME );
        enrollTask.execute();

        //Calls Kairos API to enroll target image
//        try {
//            enrollFace(BASE_URL_ENROLL, encodeImage(ENROLL_IMAGE));
//        } catch (IOException e) {
//            Toast.makeText(HuntingActivity.this, "ERROR: Please Try Again", Toast.LENGTH_SHORT).show();
//        }

        //Clicking the crosshair allows the user to attempt a HeadShot
        mCrosshair.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takeHeadshot();
            }
        });
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
//            try {
//                recognizeFace(BASE_URL_RECOGNIZE, encodeImage(HEADSHOT_IMAGE));
//
//            } catch (IOException e) {
//                e.printStackTrace();
//            }

            RecognizeAsync recognizeTask = new RecognizeAsync(HuntingActivity.this, encodeImage(HEADSHOT_IMAGE), TARGET_NAME);
            recognizeTask.execute();

            if (preyEliminated){
                Toast.makeText(HuntingActivity.this, "PREY SLAUGHTERED", Toast.LENGTH_LONG).show();
                mPreyName.setPaintFlags(mPreyName.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            }else{
                Toast.makeText(HuntingActivity.this, "SHOT MISSED!", Toast.LENGTH_SHORT).show();
            }

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