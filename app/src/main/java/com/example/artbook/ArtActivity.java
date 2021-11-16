package com.example.artbook;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Toast;

import com.example.artbook.databinding.ActivityArtBinding;
import com.google.android.material.snackbar.Snackbar;

import java.io.ByteArrayOutputStream;

public class ArtActivity extends AppCompatActivity {
    private Bitmap selectedImage;
    private ActivityArtBinding binding;
    private ActivityResultLauncher<Intent> activityResultLauncher;
    private ActivityResultLauncher<String> permissionLauncher;
    protected SQLiteDatabase database;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityArtBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        registerLauncher();

        database = this.openOrCreateDatabase("Arts", MODE_PRIVATE, null);

        Intent intent = getIntent();
        String info = intent.getStringExtra("info");

        if (!info.matches("new")) {
            int artId = intent.getIntExtra("artId",1);
            binding.button.setVisibility(View.INVISIBLE);
            binding.button2.setVisibility(View.INVISIBLE);

            try {
                String sql = "SELECT * FROM arts WHERE id = " + artId;
                Cursor cursor = database.rawQuery(sql, null);

                int artNameIx = cursor.getColumnIndex("artname");
                int painterNameIx = cursor.getColumnIndex("paintername");
                int yearIx = cursor.getColumnIndex("year");
                int imageIx = cursor.getColumnIndex("image");

                while (cursor.moveToNext()) {

                    binding.artName.setText(cursor.getString(artNameIx));
                    binding.artistName.setText(cursor.getString(painterNameIx));
                    binding.year.setText(cursor.getString(yearIx));

                    byte[] bytes = cursor.getBlob(imageIx);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(bytes,0,bytes.length);
                    binding.imageView.setImageBitmap(bitmap);


                }

                cursor.close();

            } catch (Exception e) {
                e.printStackTrace();
            }


        } else {
            binding.artName.setText("");
            binding.artistName.setText("");
            binding.year.setText("");
            binding.button.setVisibility(View.VISIBLE);
            binding.button2.setVisibility(View.VISIBLE);

            Bitmap selectImage = BitmapFactory.decodeResource(getApplicationContext().getResources(),R.drawable.ic_launcher_background);
            binding.imageView.setImageBitmap(selectImage);
        }
    }
    public void save (View view) {
        String name = binding.artName.getText().toString();
        String artistName = binding.artistName.getText().toString();
        String year = binding.year.getText().toString();
        Bitmap smallImage = makeSmallerImage(selectedImage, 300);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        smallImage.compress(Bitmap.CompressFormat.PNG, 50, outputStream);
        byte[] byteArray = outputStream.toByteArray();

        try {
            database = this.openOrCreateDatabase("Arts", MODE_PRIVATE, null);
            database.execSQL("CREATE TABLE IF NOT EXISTS arts(id INTEGER PRIMARY KEY, artname VARCHAR, paintername VARCHAR, year VARCHAR, image BLOB)");
            String sqlString = "INSERT INTO arts (artname, paintername, year, image) VALUES (?, ?, ?, ?)";
            SQLiteStatement sqLiteStatement = database.compileStatement(sqlString);
            sqLiteStatement.bindString(1, name);
            sqLiteStatement.bindString(2, artistName);
            sqLiteStatement.bindString(3, year);
            sqLiteStatement.bindBlob(4, byteArray);
            sqLiteStatement.execute();

        }
        catch(Exception e){
            e.printStackTrace();
        }
//        finish();
        Intent intent = new Intent(ArtActivity.this, MainActivity.class);
        intent.addFlags(intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);

    }

    public void select_image(View view){
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)){
                Snackbar.make(view, "Permission needed for gallary", Snackbar.LENGTH_INDEFINITE).setAction("Give Permission", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
                    }
                }).show();
            }
            else{
                permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
        }
        else{
            Intent intentToGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            activityResultLauncher.launch(intentToGallery);
        }
    }
    public Bitmap makeSmallerImage(Bitmap image, int maximumSize){
        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float) width / (float) height;
        if(bitmapRatio > 1){
            width = maximumSize;
            height = (int) (width/bitmapRatio);
        }
        else{
            height = maximumSize;
            width = (int) (height * bitmapRatio);
        }

        return image.createScaledBitmap(image,width,height, true);
    }
    private void registerLauncher() {
        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if(result.getResultCode() == RESULT_OK){
                    Intent intentFromResult = result.getData();
                    if (intentFromResult != null){
                        Uri imageData = intentFromResult.getData();
//                        binding.imageView.setImageURI(imageData);
                        try {
                            if (Build.VERSION.SDK_INT > 28){
                                ImageDecoder.Source source = ImageDecoder.createSource(ArtActivity.this.getContentResolver(), imageData);
                                selectedImage = ImageDecoder.decodeBitmap(source);
                                binding.imageView.setImageBitmap(selectedImage);
                            }
                            else{
                                selectedImage = MediaStore.Images.Media.getBitmap(ArtActivity.this.getContentResolver(), imageData);
                            }
                        }
                        catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
        permissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
            @Override
            public void onActivityResult(Boolean result) {
                if (result){
                    // permission granted
                    Intent intentToGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    activityResultLauncher.launch(intentToGallery);
                }
                else{
//                    permission denied
                    Toast.makeText(ArtActivity.this, "Permission needed!", Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}