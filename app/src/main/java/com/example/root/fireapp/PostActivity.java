package com.example.root.fireapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.w3c.dom.Text;

import java.net.URI;

public class PostActivity extends AppCompatActivity {

    private ImageButton mSelectedImage;
    private EditText mPostTitle;
    private EditText mPostDesc;

    private Button mSubmitButton;

    private Uri mImageUri=null;

    private StorageReference mStorage;
    private DatabaseReference mDatabase;

    private ProgressDialog mProgress;



    private static final int GALLERY_REQUEST=1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        mStorage = FirebaseStorage.getInstance().getReference();
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Blog");

        mSelectedImage=(ImageButton)findViewById(R.id.imageSelected);

        mPostTitle = (EditText)findViewById(R.id.titleField);
        mPostDesc = (EditText)findViewById(R.id.descField);

        mSubmitButton = (Button)findViewById(R.id.submitBtn);

        mProgress = new ProgressDialog(this);


        mSelectedImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent,GALLERY_REQUEST);
            }
        });

        mSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startPosting();

            }

            private void startPosting() {
                mProgress.setMessage("Posting to Blog ...");

                final String title_val = mPostTitle.getText().toString().trim();
                final String desc_val = mPostDesc.getText().toString().trim();

                if(!TextUtils.isEmpty(title_val) && !TextUtils.isEmpty(desc_val) && mImageUri!=null){
                    mProgress.show();
                    StorageReference filepath = mStorage.child("Blog_Image").child(mImageUri.getLastPathSegment());

                    filepath.putFile(mImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Uri downloadUri = taskSnapshot.getDownloadUrl();

                            DatabaseReference newPost = mDatabase.push();

                            newPost.child("title").setValue(title_val);
                            newPost.child("desc").setValue(desc_val);
                            newPost.child("image").setValue(downloadUri.toString());
                            startActivity(new Intent(PostActivity.this,MainActivity.class));

                            mProgress.dismiss();
                        }
                    });
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode ==GALLERY_REQUEST && resultCode==RESULT_OK){
            //Toast.makeText(this,"aaa", Toast.LENGTH_SHORT).show();
            mImageUri = data.getData();
            mSelectedImage.setImageURI(mImageUri);
        }


    }
}
