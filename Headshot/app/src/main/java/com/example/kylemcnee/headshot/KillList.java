package com.example.kylemcnee.headshot;

import android.content.Intent;
import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class KillList extends AppCompatActivity {
    TextView mTitle;
    EditText mTarget;
    Button mRegisterButton;
    final static int REQUEST_IMAGE_CAPTURE = 1;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kill_list);

        mTitle = (TextView) findViewById(R.id.title_text);
        mTarget = (EditText) findViewById(R.id.target_edittext);
        mRegisterButton = (Button) findViewById(R.id.register_button);

        // When the user clicks the Register Button, the camera is launched
        mRegisterButton.setOnClickListener(new View.OnClickListener() {
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
        // Retrieves the bitmap of the photo taken by the user, then launches the Hunting Activity

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK){
            Bitmap photo = (Bitmap)data.getExtras().get("data");
            Intent i = new Intent(KillList.this, HuntingActivity.class);
            i.putExtra("enroll_bitmap", photo);
            i.putExtra("target_name", mTarget.getText().toString());
            startActivity(i);
        }
    }
}