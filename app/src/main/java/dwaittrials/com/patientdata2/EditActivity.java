package dwaittrials.com.patientdata2;

import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/* The edit screen code that handles adding data to a session or editing previous sessions.
Also takes care of posting the data asynchronously to the online server. */

public class EditActivity extends AppCompatActivity {

    static List<String> remarks = new ArrayList<>();
    int editSessionIndex;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

/* Get session number of the session to be edited from the main screen list */

        Bundle fromMain = getIntent().getExtras();
        editSessionIndex = fromMain.getInt("editSessionIndex");

/* Switch to different  screen on pressing the receive file or receive from pi3 button */

        Button receiveDataButton = (Button) findViewById(R.id.dataReceiver);
        receiveDataButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(EditActivity.this, DataReceiver.class));
            }
        });

        Button shareAppButton = (Button) findViewById(R.id.fileShareApp);
        shareAppButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent toFileShareApp = new Intent(EditActivity.this, io.github.karuppiah7890.filesharer.ReceiverActivity.class);
                toFileShareApp.putExtra("editSessionIndex",editSessionIndex);
                startActivity(toFileShareApp);
            }
        });


/* Set text according to whether a file was received for this session */

        File sharedDir = new File("/storage/emulated/0/Patient Data/FileSharer/");
        String sharedFileName = sharedDir + "/To_avoid_NullPointerException";
        if(sharedDir.listFiles() != null) {
            for (File f : sharedDir.listFiles()) {
                if (f.getName().contains("Session " + (editSessionIndex + 1) + " - ")) {
                    sharedFileName = f.getAbsolutePath();
                }
            }
        }
        TextView sharedFileText = (TextView) findViewById(R.id.shared_file_text);
        File sharedFile = new File(sharedFileName);
        if (sharedFile.exists()) {
            sharedFileText.setText("Received file name: " + sharedFile.getName());
        }
        else {
            sharedFileText.setText("No file received for this session");
        }


        final EditText remarksText = (EditText) findViewById(R.id.remarksText);
        Button bloodButton_Gallery = (Button) findViewById(R.id.bloodButton_Gallery);
        Button xrayButton_Gallery = (Button) findViewById(R.id.xrayButton_Gallery);
        final ImageView xrayImage = (ImageView) findViewById(R.id.xrayImage);
        final ImageView bloodImage = (ImageView) findViewById(R.id.bloodImage);

        if (remarks.size() > editSessionIndex)    // Verify that it's not the first time opening edit for this session; if so,
            // then remarks.get(editSessionIndex) will be out of bounds as size of remarks
            // will be one less than required and hence equal to editSessionIndex
            // (example: when size is 0, remarks(0) (first element) is out of bounds.
            remarksText.setText(remarks.get(editSessionIndex));

        SimpleDateFormat format = new SimpleDateFormat("EEEE, MMMM d yyyy");
        final String imageNameString = format.format(MainActivity.datesOfSessions.get(editSessionIndex).getTime());

        int sessionCount = fromMain.getInt("sessionCount");
        Log.i("LOOK HERE: Method name", "onCreate of EditActivity");
        Log.i("LOOK HERE:", "Now (formatted) datesOfSessions is ");
        for (int indexVar = 0; indexVar < sessionCount; indexVar++)
            Log.i("LOOK HERE:", "At index " + indexVar + " - " + format.format(MainActivity.datesOfSessions.get(indexVar).getTime()));

        Log.i("LOOK HERE:", "Formatted value to be used for imageNameString is: datesOfSessions.get(editSessionIndex = " + editSessionIndex + ").getTime() is \n " +
                format.format(MainActivity.datesOfSessions.get(editSessionIndex).getTime()));

/* Formatting the name of the selected image file to be stored in Patient Data folder, and displaying previously selected images, if any */

        final File xrayFile = new File("/storage/emulated/0/Patient Data/X-ray from " + imageNameString + ".jpg");
        final File bloodFile = new File("/storage/emulated/0/Patient Data/Blood Report from " + imageNameString + ".jpg");
        if (xrayFile.exists())
            xrayImage.setImageURI(Uri.fromFile(xrayFile));
        if (bloodFile.exists())
            bloodImage.setImageURI(Uri.fromFile(bloodFile));

/* Selecting images and setting their name to the formatted strings */

        xrayButton_Gallery.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new PickerBuilder(EditActivity.this, PickerBuilder.SELECT_FROM_GALLERY)
                                .setOnImageReceivedListener(new PickerBuilder.onImageReceivedListener() {
                                    @TargetApi(Build.VERSION_CODES.KITKAT)
                                    @Override
                                    public void onImageReceived(Uri imageUri) {
                                        xrayImage.setImageURI(imageUri);
                                        xrayImage.invalidate();
                                        Log.i("Image URI is ", imageUri.toString());
                                        //ADD CODE TO SEND IMAGE TO SERVER

                                    }
                                })
                                .setImageName("X-ray from " + imageNameString)
                                .setImageFolderName("Patient Data")
                                .withTimeStamp(false)
                                .start();
                    }
                }
        );

        bloodButton_Gallery.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new PickerBuilder(EditActivity.this, PickerBuilder.SELECT_FROM_GALLERY)
                                .setOnImageReceivedListener(new PickerBuilder.onImageReceivedListener() {
                                    @Override
                                    public void onImageReceived(Uri imageUri) {
                                        Log.i("LOOK HERE:", "Image received");
                                        bloodImage.setImageURI(imageUri);
                                        bloodImage.invalidate();
                                        //Toast.makeText(EditActivity.this,"Image uri : " + imageUri.toString(),Toast.LENGTH_LONG).show();
                                        //ADD CODE TO SEND IMAGE TO SERVER

                                    }
                                })
                                .setImageName("Blood Report from " + imageNameString)
                                .setImageFolderName("Patient Data")
                                .withTimeStamp(false)
                                .start();
                    }
                }
        );

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_save);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i("LOOK HERE: Method", "Floating action button (to exit activity) onClick of EditActivity");
                finish();
            }
        });

    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    protected void onPause() {
        super.onPause();
        final EditText remarksText = (EditText) findViewById(R.id.remarksText);
        Bundle fromMain = getIntent().getExtras();
        final int editSessionIndex = fromMain.getInt("editSessionIndex");

        Log.i("LOOK HERE: Method name", "onPause of EditActivity");

        if (editSessionIndex < remarks.size())
            remarks.set(editSessionIndex, remarksText.getText().toString());
        else if (editSessionIndex == remarks.size())
            remarks.add(remarksText.getText().toString());
        Log.i("LOOK HERE:", "Complete remarks list is now:\n" + remarks.get(remarks.size() - 1));
        for (int indexVar = 0; indexVar < remarks.size(); indexVar++)
            Log.i("LOOK HERE:", "At index " + indexVar + " - " + remarks.get(indexVar));

/* Saving the edited remarks for the session in SharedPreferences */

        SharedPreferences sharedprefs = getSharedPreferences("Remarks", MODE_PRIVATE);
        SharedPreferences.Editor prefsEditor = sharedprefs.edit();
        prefsEditor.putString("remarks", TextUtils.join("&", remarks));
        prefsEditor.apply();

/* Starting a background process to send the selected data to the online server */

        String xrayFileName = "/storage/emulated/0/Patient Data/To_avoid_NullPointerException";
        String bloodFileName = "/storage/emulated/0/Patient Data/To_avoid_NullPointerException";


        SimpleDateFormat format = new SimpleDateFormat("EEEE, MMMM d yyyy");
        String imageNameString = format.format(MainActivity.datesOfSessions.get(editSessionIndex).getTime());

        if (new File("/storage/emulated/0/Patient Data/X-ray from " + imageNameString + ".jpg").exists()) {
           xrayFileName = "/storage/emulated/0/Patient Data/X-ray from " + imageNameString + ".jpg";
        }

        if (new File("/storage/emulated/0/Patient Data/Blood Report from " + imageNameString + ".jpg").exists()) {
            bloodFileName = "/storage/emulated/0/Patient Data/Blood Report from " + imageNameString + ".jpg";
        }

        File sharedDir = new File("/storage/emulated/0/Patient Data/FileSharer/");
        String sharedFileName = sharedDir + "/To_avoid_NullPointerException";
        if(sharedDir.listFiles() != null) {
            for (File f : sharedDir.listFiles()) {
                if (f.getName().contains("Session " + (editSessionIndex + 1) + " - ")) {
                    sharedFileName = f.getAbsolutePath();
                }
            }
        }

        //if(new File("/storage/emulated/0/Patient Data/X-ray from " + imageNameString + ".jpg").exists() &&
          //      new File("/storage/emulated/0/Patient Data/Blood Report from " + imageNameString + ".jpg").exists())
            new postData(xrayFileName, bloodFileName, remarks.get(editSessionIndex), sharedFileName).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void[]) null);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i("LOOK HERE: Method name", "onDestroy of EditActivity");
        Log.i("LOOK HERE", "EditActivity destroyed");
    }

/* The background task that sends the remarks, images, and the received file to the online sever */

    @TargetApi(Build.VERSION_CODES.KITKAT)
    class postData extends AsyncTask<Object, Object, Void> {


        String url = "http://mlceeri.pythonanywhere.com/new-session/";

        File xrayFile,bloodFile,sharedFile;
        String remarks;
        SimpleDateFormat format = new SimpleDateFormat("MMMM d, yyyy");

        public postData(String xrayFileName, String bloodFileName, String remarks, String sharedFileName) {
            xrayFile = new File(xrayFileName);
            bloodFile = new File(bloodFileName);
            this.remarks = remarks;
            sharedFile = new File(sharedFileName);

        }

        @Override
        protected Void doInBackground(Object... params) {
            Log.i("LOOK HERE", "doInBackground method called");

            OkHttpClient client = new OkHttpClient();

            MultipartBody.Builder formBodyBuilder = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("remarks",remarks)
                    .addFormDataPart("session_name",format.format(MainActivity.datesOfSessions.get(editSessionIndex).getTime()))
                    .addFormDataPart("patient_id", "03");

            if (xrayFile.exists()){
                formBodyBuilder.addFormDataPart("xray", xrayFile.getName(),
                        RequestBody.create(MediaType.parse("image/jpg"), xrayFile));
            }

            if (bloodFile.exists()){
                formBodyBuilder .addFormDataPart("bloodwork", bloodFile.getName(),
                        RequestBody.create(MediaType.parse("image/jpg"), bloodFile));
            }

            if (sharedFile.exists()){
                formBodyBuilder.addFormDataPart("shared_file", sharedFile.getName(),
                        RequestBody.create(MediaType.parse(getMimeType(Uri.fromFile(sharedFile),EditActivity.this)), sharedFile));
            }

            RequestBody formBody = formBodyBuilder.build();
            Request request = new Request.Builder().url(url).post(formBody).build();

            try (Response response = client.newCall(request).execute()) {
                Log.i("LOOK HERE", "Response body is\n" + response.body().string());
            } catch (IOException e) {
                Log.e("LOOK HERE", "Error Response is: ", e);
            }

            return null;
        }


    }

/* Method to get filetype of received file */

    public String getMimeType(Uri uri, Context context) {
        String mimeType = null;
        if (uri.getScheme().equals(ContentResolver.SCHEME_CONTENT)) {
            ContentResolver cr = context.getContentResolver();
            mimeType = cr.getType(uri);
        } else {
            String fileExtension = MimeTypeMap.getFileExtensionFromUrl(uri
                    .toString());
            mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(
                    fileExtension.toLowerCase());
        }
        return mimeType;
    }


}
