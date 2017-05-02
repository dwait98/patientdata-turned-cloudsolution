package dwaittrials.com.patientdata2;

import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/* The view screen code that displays all information related to a session when clicked */

public class ViewActivity extends AppCompatActivity {


    List<String> remarks = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Log.i("LOOK HERE: Method name","onCreate of ViewActivity");
        Bundle sessiondata = getIntent().getExtras();
        int viewSessionIndex = sessiondata.getInt("viewSessionIndex");
        Log.i("LOOK HERE:", "Index is "+viewSessionIndex);

/* Check if there is a received file for this session and set text accordingly */

        File sharedDir = new File("/storage/emulated/0/Patient Data/FileSharer/");
        String sharedFileName = sharedDir + "/To_avoid_NullPointerException";
        if(sharedDir.listFiles() != null) {
            for (File f : sharedDir.listFiles()) {
                if (f.getName().contains("Session " + (viewSessionIndex + 1) + " - ")) {
                    sharedFileName = f.getAbsolutePath();
                }
            }
        }
        TextView sharedFileText = (TextView) findViewById(R.id.shared_file_text2);
        File sharedFile = new File(sharedFileName);
        if (sharedFile.exists()) {
            sharedFileText.setText(sharedFile.getName());
        }
        else {
            sharedFileText.setText("No file received for this session");
        }

/* Get remarks for the session from memory (SharedPreferences) */

        SharedPreferences sharedprefs = getSharedPreferences("Remarks",MODE_PRIVATE);
        String stringFromPrefs = sharedprefs.getString("remarks", "");
        Log.i("LOOK HERE:","String from SharedPrefs is "+stringFromPrefs);
        if (!stringFromPrefs.equals(""))
            remarks = Arrays.asList(TextUtils.split(stringFromPrefs, "&"));

        TextView viewRemarksText = (TextView) findViewById(R.id.viewRemarks);
        if (!stringFromPrefs.equals("") && remarks.size()>viewSessionIndex)
            viewRemarksText.setText(remarks.get(viewSessionIndex));

/* Display the images related to the session */

        SimpleDateFormat format = new SimpleDateFormat("EEEE, MMMM d yyyy");
        String fileNameString = format.format(MainActivity.datesOfSessions.get(viewSessionIndex).getTime());
        Log.i("LOOK HERE:","Formatted value to be used for fileNameString is: datesOfSessions.get(viewSessionIndex = "+viewSessionIndex+ ").getTime() is \n" +
                format.format(MainActivity.datesOfSessions.get(viewSessionIndex).getTime()));

        ImageView viewXray = (ImageView) findViewById(R.id.viewXray);
        ImageView viewBlood = (ImageView) findViewById(R.id.viewBlood);

        File viewXrayFile = new File("/storage/emulated/0/Patient Data/X-ray from "+fileNameString+".jpg");
        File viewBloodFile = new File("/storage/emulated/0/Patient Data/Blood Report from "+fileNameString+".jpg");
        if(viewXrayFile.exists())
            viewXray.setImageURI(Uri.fromFile(viewXrayFile));
        if(viewBloodFile.exists())
            viewBlood.setImageURI(Uri.fromFile(viewBloodFile));
    }

}
