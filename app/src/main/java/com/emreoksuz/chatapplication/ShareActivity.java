package com.emreoksuz.chatapplication;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.util.UUID;

public class ShareActivity extends AppCompatActivity {


    Uri selected;
    ImageView shareimageView;
    EditText nameText;
    EditText surnameText;
    ImageView userImageView;


    private FirebaseDatabase database;
    private DatabaseReference databaseReference;
    private StorageReference storageReference;
    private FirebaseAuth mAuth;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share);

        shareimageView = findViewById(R.id.shareimageView);



        database= FirebaseDatabase.getInstance();
        databaseReference = database.getReference();
        storageReference = FirebaseStorage.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();

    }

    public void shareUpload(View view){
        final UUID uuidImage = UUID.randomUUID();

        String imageName = "images/"+uuidImage+".jpg";

        StorageReference newReference = storageReference.child(imageName);

        newReference.putFile(selected).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                StorageReference profileImageRef = FirebaseStorage.getInstance().getReference("images/"+uuidImage+".jpg");
                profileImageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        String downloadURL=uri.toString();




                        UUID uuid = UUID.randomUUID();
                        String uuidString=uuid.toString();



                        FirebaseUser user = mAuth.getCurrentUser();
                        String useremail = user.getEmail().toString();

                        databaseReference.child("Shares").child(uuidString).child("userimageurl").setValue(downloadURL);

                        databaseReference.child("Shares").child(uuidString).child("useremail").setValue(useremail);


                        Toast.makeText(getApplicationContext(),"Uploaded!",Toast.LENGTH_LONG).show();



                    }
                });


            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(),e.getLocalizedMessage(),Toast.LENGTH_LONG).show();
            }
        });

    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.option_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId()==R.id.options_menu_profile){
            Intent intent = new Intent(this,ProfileActivity.class);
            startActivity(intent);
        }else if (item.getItemId()==R.id.options_menu_chat){
            Intent intent = new Intent(this,ChatActivity.class);
            startActivity(intent);
        } else if (item.getItemId()==R.id.options_menu_sign_out) {
            Intent intent = new Intent(this,MainActivity.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    public void share(View view){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)!=PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1);
        }else{
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent,2);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == 1){
            if (grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent intent = new Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent,2);

            }
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if (requestCode == 2 && resultCode == RESULT_OK && data !=null ){
            selected = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(),selected);
                shareimageView.setImageBitmap(bitmap);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }
}