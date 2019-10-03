package com.example.audiotext.pages.fragments;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.DialogInterface;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.speech.tts.TextToSpeech;
import android.speech.tts.Voice;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.audiotext.R;
import com.example.audiotext.database.AppDatabase;
import com.example.audiotext.database.entity.SettingsEntry;
import com.example.audiotext.pages.Loading;
import com.example.audiotext.task.ConnectionManager;
import com.example.audiotext.task.MyPackageManager;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class ImageText extends Fragment {
    private final String TAG = ImageText.class.getSimpleName();
    private Context mContext;
    private Dialog dialog;

    private EditText mResultEt;
    private ImageView mPreviewIv;
    private LinearLayout mAudioPreview_LL;
    private TextToSpeech textToSpeech;
    private Button mAudioListen, mAudioSave;

    private static final int CAMERA_REQUEST_CODE = 200;
    private static final int STORAGE_REQUEST_CODE = 400;
    private static final int IMAGE_PICK_GALLERY_CODE = 1000;
    private static final int IMAGE_PICK_CAMERA_CODE = 1001;
    private static final String SPECIFIC_TTS_PACKAGE_NAME = "com.google.android.tts";

    private String cameraPermission[];
    private String storagePermission[];

    private Uri image_uri;
    private AppDatabase db;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = AppDatabase.getInstance(mContext);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_audio_text, container, false);
        mResultEt = (EditText)v.findViewById(R.id.resultEt);
        mPreviewIv = (ImageView)v.findViewById(R.id.imageIv);
        mAudioPreview_LL = (LinearLayout)v.findViewById(R.id.fragment_audio_audio_preview_LL);
        mAudioListen = (Button)v.findViewById(R.id.fragment_audio_text_listen_audio_btn);
        mAudioSave = (Button)v.findViewById(R.id.fragment_audio_text_save_audio_btn);
        setHasOptionsMenu(true);
        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        ActionBar actionBar = ((AppCompatActivity)getActivity()).getSupportActionBar();
        actionBar.setSubtitle("Click image icon to insert Image");

        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        //camera permission
        cameraPermission = new String[]{
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        };
        storagePermission = new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        };

        mAudioListen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                speakAudio();
            }
        });
        mAudioSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!mResultEt.getText().toString().isEmpty()) {
                    saveAudio();
                }else {
                    Toast.makeText(mContext, "Require some text in the 'Result' area!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        if ((MyPackageManager.isPackageInstalled(mContext.getPackageManager(), SPECIFIC_TTS_PACKAGE_NAME))) {
            textToSpeech = new TextToSpeech(mContext, new TextToSpeech.OnInitListener() {
                @Override
                public void onInit(int status) {
                    Log.e(TAG, "TextToSpeech: onInit() ==> " + status);
                    if (status == TextToSpeech.SUCCESS) {
                        SettingsEntry settingsEntry = db.settingsDao().getSettings().get(0);
                        Log.e(TAG, settingsEntry.getLocaleLanguage()+" || "+settingsEntry.getLocaleCountry()+" || "+settingsEntry.getLocaleVariant());
                        Locale db_locale = new Locale(settingsEntry.getLocaleLanguage(),settingsEntry.getLocaleCountry(),settingsEntry.getLocaleVariant());

                        int result = textToSpeech.setLanguage(db_locale);
                        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                            Log.e(TAG, "TextToSpeech: Language not supported");
                            Toast.makeText(mContext, "Language not supported.\nPlease select an other language.", Toast.LENGTH_LONG).show();
                        } else {
                            mAudioPreview_LL.setVisibility(View.VISIBLE);
                        }
                    } else {
                        // Fire off an intent to check if a TTS engine is installed
                        final AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                        builder.setTitle("Text To Speech");
                        builder.setTitle(getString(R.string.app_name)+" require Text To Speech to be Enable and/or downloaded.\n" +
                                "This technology is useful so save audio files and listen to text written in the result section.");
                        builder.setPositiveButton("Enable/Update", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                installSpecificTTSEngine();
                                dialog.dismiss();
                            }
                        });

                        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //kill the app
                                System.exit(0);
                            }
                        });

                        builder.setCancelable(false);
                        dialog = builder.show();
                        Log.e(TAG, "1 ==> TextToSpeech: initilization faild");
                    }
                }
            }, SPECIFIC_TTS_PACKAGE_NAME);
        }else {
            Log.e(TAG, "2 ==> Intended TTS engine is not installed");
            installSpecificTTSEngine();
        }
    }

    private void installSpecificTTSEngine() {
        if (ConnectionManager.isPhoneConnected(mContext)) {
            Intent installIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + SPECIFIC_TTS_PACKAGE_NAME));
            installIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET | Intent.FLAG_ACTIVITY_MULTIPLE_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);

            try {
                Log.e(TAG, "Installing TTS engine: " + installIntent.toUri(0));
                startActivity(installIntent);
            } catch (ActivityNotFoundException ex) {
                Log.e(TAG, "Failed to install TTS engine, no activity found for " + installIntent + ")");
            }
        } else {
            Log.e(TAG, "Internet is not connected for download tts engine");
        }
    }


    /** Action menu **/
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.main_menu, menu);
    }

    /** Handle action item clicks **/
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.main_menu_addImage:
                showImageImportDialog();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showImageImportDialog(){
        //items to display in dialog
        String[] items = {" Camera", " Gallery"};
        AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);

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
        image_uri = mContext.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

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
        boolean result = ContextCompat.checkSelfPermission(mContext,
                Manifest.permission.CAMERA) == (PackageManager.PERMISSION_GRANTED);
        boolean result1 = ContextCompat.checkSelfPermission(mContext,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        return result && result1;
    }

    private void requestCameraPermission(){
        ActivityCompat.requestPermissions(getActivity(), cameraPermission, CAMERA_REQUEST_CODE);
    }

    private boolean checkStoragePermission(){
        boolean result1 = ContextCompat.checkSelfPermission(mContext,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        return result1;
    }

    private void requestStoragePermission(){
        ActivityCompat.requestPermissions(getActivity(), storagePermission, STORAGE_REQUEST_CODE);
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
                        Toast.makeText(mContext, "Permission denied", Toast.LENGTH_SHORT).show();
                    }
                }
                break;

            case STORAGE_REQUEST_CODE:
                if (grantResults.length > 0){
                    boolean writeStorageAccepted = grantResults[0] == PackageManager.PERMISSION_DENIED;
                    if (writeStorageAccepted){
                        picGallery();
                    }else{
                        Toast.makeText(mContext, "Permission denied", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
        }
    }

    /** Handle image result **/
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        Log.e(TAG, "Testing JL Debug...");

        if (resultCode == getActivity().RESULT_OK){
            Log.e(TAG, "Testing JL Debug...");
            if (requestCode == IMAGE_PICK_GALLERY_CODE){
                //got image from gallery now crop it
                CropImage.activity(data.getData()).setGuidelines(CropImageView.Guidelines.ON).start(getActivity(), ImageText.this);
            }
            if (requestCode == IMAGE_PICK_CAMERA_CODE){
                //got image from camera now crop it
                CropImage.activity(image_uri).setGuidelines(CropImageView.Guidelines.ON).start(getActivity(), ImageText.this);
            }

            //get cropped image
            if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
                Log.e(TAG, "Testing JL Debug...");
                CropImage.ActivityResult result = CropImage.getActivityResult(data);
                if (resultCode == getActivity().RESULT_OK){
                    Log.e(TAG, "Testing JL Debug...");
                    Uri resultUri = result.getUri();    //get image uri
                    //set image to image view
                    mPreviewIv.setImageURI(resultUri);

                    //get drawable bitmap for text recognition
                    BitmapDrawable bitmapDrawable = (BitmapDrawable)mPreviewIv.getDrawable();
                    Bitmap bitmap = bitmapDrawable.getBitmap();
                    TextRecognizer recognizer = new TextRecognizer.Builder(mContext.getApplicationContext()).build();

                    if (!recognizer.isOperational()){
                        Toast.makeText(mContext, "Error[1000]", Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(mContext, ""+error, Toast.LENGTH_SHORT).show();
                }
            }
            Log.e(TAG, "Testing JL Debug... 22222");
        }
    }

    /** Handle audio result **/
    private void speakAudio(){
        SettingsEntry settingsEntry = db.settingsDao().getSettings().get(0);
        String text = mResultEt.getText().toString();
        float pitch = (float) settingsEntry.getSpeekPitch() / 50;
        float speed = (float) settingsEntry.getSpeekSpeed() / 50;
        Locale locale = new Locale(settingsEntry.getLocaleLanguage(), settingsEntry.getLocaleCountry(), settingsEntry.getLocaleVariant());

        if (pitch < 0.1) pitch = 0.1f;
        if (speed < 0.1) speed = 0.1f;

        textToSpeech.setPitch(pitch);
        textToSpeech.setSpeechRate(speed);
        textToSpeech.setLanguage(locale);
        textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null);
    }

    private void saveAudio(){
        final ProgressDialog progressDialog = new ProgressDialog(mContext);
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);

        final Dialog thisDialog = new Dialog(mContext);
        thisDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        thisDialog.setContentView(R.layout.dialog_add_audio_name);
        final EditText textName = (EditText) thisDialog.findViewById(R.id.dialog_add_audio_name_text_et);
        ImageView close_btn = (ImageView) thisDialog.findViewById(R.id.dialog_add_audio_name_close_btn);
        Button save_btn = (Button) thisDialog.findViewById(R.id.dialog_add_audio_name_save_btn);

        close_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //kill the app
                thisDialog.dismiss();
            }
        });

        save_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressDialog.setMessage("Creating '"+textName.getText().toString()+".mp3'...");
                progressDialog.show();

                String text = mResultEt.getText().toString();
                HashMap<String, String> myHashMap = new HashMap<String, String>();
                myHashMap.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, text);

                File audioDirectory = new File(Environment.getExternalStorageDirectory().getAbsolutePath(),"AudioText/sounds");
                boolean isDirectoryCreated = audioDirectory.mkdirs();
                if (isDirectoryCreated){
                    Log.e(TAG, " directory: "+audioDirectory+" is created: "+isDirectoryCreated);
                }else{
                    if (!audioDirectory.exists()) {
                        Log.e(TAG, " directory: " + audioDirectory + " does not exist and could not be created: " + isDirectoryCreated);
                        progressDialog.dismiss();
                        return;
                    }
                }

                String audioFile = textName.getText().toString()+".mp3";
                //The space character code is 0x20
                audioFile = audioFile.replace(" ","0x20");

                String tempDestFile = audioDirectory.getAbsolutePath()+File.separator+audioFile;
                Log.e(TAG, "tempDestFile : "+tempDestFile);
                new MySaveText(progressDialog, text, myHashMap, tempDestFile);

                thisDialog.dismiss();
            }
        });
        thisDialog.show();
    }

    class MySaveText implements TextToSpeech.OnInitListener
    {
        TextToSpeech mTts;
        ProgressDialog progressDialog;
        String speakTextTxt;
        HashMap<String, String> myHashMap;
        String tempDestFile;

        public MySaveText(ProgressDialog progressDialog, String text, HashMap<String, String> myHashMap, String tempDestFile)
        {
            this.progressDialog = progressDialog;
            this.speakTextTxt = text;
            this.myHashMap = myHashMap;
            this.tempDestFile = tempDestFile;
            mTts = new TextToSpeech(mContext, this);
        }

        @Override
        public void onInit(int status)
        {
            int i = mTts.synthesizeToFile(speakTextTxt, myHashMap, tempDestFile);
            Log.e("MySaveText", "onInit ==> "+i);
            if(i == TextToSpeech.SUCCESS)
            {
                progressDialog.dismiss();
                Toast.makeText(mContext, "The audio is saved.", Toast.LENGTH_SHORT).show();
            }
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (textToSpeech != null){
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
    }
}






















