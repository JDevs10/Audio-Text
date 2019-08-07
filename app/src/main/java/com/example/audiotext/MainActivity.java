package com.example.audiotext;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.DialogInterface;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.common.wrappers.PackageManagerWrapper;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

public class MainActivity extends AppCompatActivity {

    private EditText mResultEt;
    private ImageView mPreviewIv;

    private static final int CAMERA_REQUEST_CODE = 200;
    private static final int STORAGE_REQUEST_CODE = 400;
    private static final int IMAGE_PICK_GALLERY_CODE = 1000;
    private static final int IMAGE_PICK_CAMERA_CODE = 1001;

    private String cameraPermission[];
    private String storagePermission[];

    private Uri image_uri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setSubtitle("Click + button to insert Image");

        mResultEt = (EditText)findViewById(R.id.resultEt);
        mPreviewIv = (ImageView)findViewById(R.id.imageIv);

        //camera permission
        cameraPermission = new String[]{
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        };
        storagePermission = new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        };
    }

    /** Action menu **/
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //Inflate menu
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    /** Handle action item clicks **/
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.main_menu_addImage:
                showImageImportDialog();
                break;

            case R.id.main_menu_settings:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showImageImportDialog(){
        //items to display in dialog
        String[] items = {" Camera", " Gallery"};
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);

        dialog.setTitle("Select Image");
        dialog.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (i == 0){
                    //Camera option clicked
                    if (!checkCameraPermission()){
                        //request it
                        requestCameraPermission();
                    }else{
                        //take picture
                        picCamera();
                    }
                }
                if (i == 1){
                    //Gallery option clicked
                    if (!checkStoragePermission()){
                        //request it
                        requestStoragePermission();
                    }
                    else{
                        //take picture
                        picGallery();
                    }
                }
            }
        });
        dialog.create().show();
    }

    private void picCamera(){
        //intent to take image from camera, it will also be save to storage to get high quality image
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "NewPic");
        values.put(MediaStore.Images.Media.DESCRIPTION, "Image To text");
        image_uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
        startActivityForResult(cameraIntent, IMAGE_PICK_CAMERA_CODE);
    }

    private void picGallery(){
        //intent to pick image from gallery
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_PICK_GALLERY_CODE);
    }

    private boolean checkCameraPermission(){
        /*Check camera permission and return the result*/
        boolean result = ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA) == (PackageManager.PERMISSION_GRANTED);
        boolean result1 = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        return result && result1;
    }

    private void requestCameraPermission(){
        ActivityCompat.requestPermissions(this, cameraPermission, CAMERA_REQUEST_CODE);
    }

    private boolean checkStoragePermission(){
        boolean result1 = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        return result1;
    }

    private void requestStoragePermission(){
        ActivityCompat.requestPermissions(this, storagePermission, STORAGE_REQUEST_CODE);
    }

    /** Handle permission result **/
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case CAMERA_REQUEST_CODE:
                if (grantResults.length > 0){
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean writeStorageAccepted = grantResults[0] == PackageManager.PERMISSION_DENIED;
                    if (cameraAccepted && writeStorageAccepted){
                        picCamera();
                    }else{
                        Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
                    }
                }
                break;

            case STORAGE_REQUEST_CODE:
                if (grantResults.length > 0){
                    boolean writeStorageAccepted = grantResults[0] == PackageManager.PERMISSION_DENIED;
                    if (writeStorageAccepted){
                        picGallery();
                    }else{
                        Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
        }
    }

    /** Handle image result **/
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK){
            if (requestCode == IMAGE_PICK_GALLERY_CODE){
                //got image from gallery now crop it
                CropImage.activity(data.getData()).setGuidelines(CropImageView.Guidelines.ON).start(this);
            }
            if (requestCode == IMAGE_PICK_CAMERA_CODE){
                //got image from camera now crop it
                CropImage.activity(image_uri).setGuidelines(CropImageView.Guidelines.ON).start(this);
            }

            //get cropped image
            if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
                CropImage.ActivityResult result = CropImage.getActivityResult(data);
                if (resultCode == RESULT_OK){
                    Uri resultUri = result.getUri();    //get image uri
                    //set image to image view
                    mPreviewIv.setImageURI(resultUri);

                    //get drawable bitmap for text recognition
                    BitmapDrawable bitmapDrawable = (BitmapDrawable)mPreviewIv.getDrawable();
                    Bitmap bitmap = bitmapDrawable.getBitmap();
                    TextRecognizer recognizer = new TextRecognizer.Builder(getApplicationContext()).build();

                    if (!recognizer.isOperational()){
                        Toast.makeText(this, "Error[1000]", Toast.LENGTH_SHORT).show();
                    }else{
                        Frame frame = new Frame.Builder().setBitmap(bitmap).build();
                        SparseArray<TextBlock> items = recognizer.detect(frame);
                        StringBuilder sb = new StringBuilder();

                        //get text from StringBuilder until there is no text
                        for (int i=0; i<items.size(); i++){
                            TextBlock myItems = items.valueAt(i);
                            sb.append(myItems.getValue());
                            sb.append("\n");

                            //set text to editText
                            mResultEt.setText(sb.toString());
                        }
                    }
                }else if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE){
                    //if there is any error to show
                    Exception error = result.getError();
                    Toast.makeText(this, ""+error, Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}






















