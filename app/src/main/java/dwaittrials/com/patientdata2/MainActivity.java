package dwaittrials.com.patientdata2;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.ListView;

import android.view.ContextMenu;
import android.widget.AdapterView.AdapterContextMenuInfo;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

/* The main screen code that handles the list of sessions, adding new sessions, and the switching to edit or view screen */

public class MainActivity extends AppCompatActivity {


    final static List<Calendar> datesOfSessions = new ArrayList<>();
    int sessionCount = 0;
    List<String> sessions_list = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.showOverflowMenu();
        setSupportActionBar(toolbar);

/* Retrieving the session list from phone's memory (SharedPreferences) */

        SharedPreferences sharedprefs = getSharedPreferences("Remarks", MODE_PRIVATE);
        String serialized = sharedprefs.getString("remarks", null);
        if (serialized != null) {
            EditActivity.remarks = Arrays.asList(TextUtils.split(serialized, "&"));
            EditActivity.remarks = new ArrayList<>(EditActivity.remarks);
        }


        SimpleDateFormat format = new SimpleDateFormat("'On' EEEE,'\n'MMMM d, yyyy");
        int i = 0;
        SharedPreferences sharedPref = getSharedPreferences("sharedPref",MODE_PRIVATE);
        while(sharedPref.getLong("calendar"+i,0)!=0){                                               // get datesOfSessions from Shared Preferences
            Calendar cal = new GregorianCalendar();
            cal.setTimeInMillis(sharedPref.getLong("calendar"+i,0));
            datesOfSessions.add(cal);
            i++;
        }

        sessionCount = sharedPref.getInt("sessionCount",0);                                         // get sessionCount from Shared Preferences

        Log.i("LOOK HERE: Method name","onCreate");
        Log.i("LOOK HERE:","Now (formatted) datesOfSessions is ");
        for(int indexVar=0;indexVar<sessionCount;indexVar++)
            Log.i("LOOK HERE:","At index "+indexVar+" - \n"+format.format(MainActivity.datesOfSessions.get(indexVar).getTime()));


        final ListView sessionList = (ListView) findViewById(R.id.sessionList);

        String[] sessions = new String[sessionCount];
        for(int temp=0;temp<sessionCount;temp++)
            sessions[temp] = format.format(datesOfSessions.get(temp).getTime());


        sessions_list = new ArrayList<>(Arrays.asList(sessions));
        final ArrayAdapter<String> myAdapter = new ArrayAdapter<>
                (this, android.R.layout.simple_list_item_1, sessions_list);
        sessionList.setAdapter(myAdapter);

/* Choosing session date from the date picker dialog, as setting it as the sesion name */

        final DatePickerDialog datePickerDialog = new DatePickerDialog(this,new DatePickerDialog.OnDateSetListener(){
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {

                Log.i("LOOK HERE: Method name","onDateSet");

                Log.i("LOOK HERE:","Date received to be set is:\nyear - "+year+",\nmonth - "+month+",\ndayOfMonth - "+dayOfMonth);

                Calendar sessionDate = Calendar.getInstance();
                sessionDate.set(year,month,dayOfMonth);
                // Toast.makeText(MainActivity.this,"Date changed",Toast.LENGTH_SHORT).show();
                SimpleDateFormat format = new SimpleDateFormat("'On' EEEE,'\n'MMMM d, yyyy");
				myAdapter.add(format.format(sessionDate.getTime()));
				Log.i("LOOK HERE:","Date added to list is (formatted sessionDate): \n"+format.format(sessionDate.getTime()));
                // Toast.makeText(MainActivity.this, "New session added", Toast.LENGTH_SHORT).show();

                datesOfSessions.add(sessionCount,sessionDate);
                sessionCount++;

                Log.i("LOOK HERE","Now count is " + sessionCount);

                Log.i("LOOK HERE:","After datesOfSessions.add(sessionCount = " + sessionCount + ",sessionDate) on date set, contents of datesOfSessions are ");
                for(int indexVar=0;indexVar<sessionCount;indexVar++)
                    Log.i("LOOK HERE:","At index "+indexVar+" - "+format.format(MainActivity.datesOfSessions.get(indexVar).getTime()));

                switchToEdit(sessionCount-1);
            }
        }, Calendar.getInstance().get(Calendar.YEAR),
                Calendar.getInstance().get(Calendar.MONTH),
                Calendar.getInstance().get(Calendar.DAY_OF_MONTH));



        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_add);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i("LOOK HERE: Method","Floating action button (to add entry) onClick");
                datePickerDialog.show();
            }
        });


        registerForContextMenu(sessionList);

/* Switching to view screen when a session is clicked */

        sessionList.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Intent switchToView = new Intent(MainActivity.this,ViewActivity.class);
                        switchToView.putExtra("viewSessionIndex",position);
                        startActivity(switchToView);
                    }
                }
        );

    }


    public void switchToEdit(int pos)
    {
        Intent goToEdit = new Intent (this,EditActivity.class);
        goToEdit.putExtra("editSessionIndex",pos);
        goToEdit.putExtra("sessionCount",sessionCount);
        startActivity(goToEdit);
    }

/* Save session list in memory (SharedPreferences) whenever screen goes in background, ie whenever activity is paused */

    @Override
    protected void onPause() {
        super.onPause();

        Log.i("LOOK HERE: Method name","onPause");
        SimpleDateFormat format = new SimpleDateFormat("'On' EEEE,'\n'MMMM d, yyyy");
        Log.i("LOOK HERE:"," datesOfSessions.size() = " + datesOfSessions.size() + "\nand those " + datesOfSessions.size() + " entries from datesOfSessions put into SharedPreferences are :");
        for(int indexVar=0;indexVar<datesOfSessions.size();indexVar++)
            Log.i("LOOK HERE:","At index "+indexVar+" - \n"+format.format(MainActivity.datesOfSessions.get(indexVar).getTime()));

        SharedPreferences sharedPref = getSharedPreferences("sharedPref",MODE_PRIVATE);
        SharedPreferences.Editor prefEditor = sharedPref.edit();
        for(int i = 0; i<datesOfSessions.size();i++){
            long millis = datesOfSessions.get(i).getTimeInMillis();
            prefEditor.putLong("calendar"+i,millis);
            prefEditor.apply();
        }
        prefEditor.putInt("sessionCount",sessionCount);
        prefEditor.apply();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i("LOOK HERE: Method name","onDestroy");
        Log.i("LOOK HERE","Main activity destroyed");
       // Toast.makeText(this,"Main activity destroyed", Toast.LENGTH_LONG).show();
    }

/* Context menu (popup) created on long press on a session name for editing and deleting a session */

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        if (v.getId()==R.id.sessionList) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.menu_list, menu);
        }
    }


    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
      //  Toast.makeText(this, "position is "+info.position, Toast.LENGTH_SHORT).show();
        switch(item.getItemId()) {

            case R.id.menu_edit:
                switchToEdit(info.position);
                return true;
            case R.id.menu_delete:

                SimpleDateFormat format = new SimpleDateFormat("EEEE, MMMM d yyyy");
                final String imageNameString = format.format(MainActivity.datesOfSessions.get(info.position).getTime());
                File xrayFile = new File("/storage/emulated/0/Patient Data/X-ray from "+imageNameString+".jpg");
                File bloodFile = new File("/storage/emulated/0/Patient Data/Blood Report from "+imageNameString+".jpg");
                xrayFile.delete();
                bloodFile.delete();

                datesOfSessions.remove(info.position);

                SharedPreferences sharedPref = getSharedPreferences("sharedPref",MODE_PRIVATE);
                SharedPreferences.Editor prefEditor = sharedPref.edit();
                for(int i = 0; i<datesOfSessions.size();i++){
                    long millis = datesOfSessions.get(i).getTimeInMillis();
                    prefEditor.putLong("calendar"+i,millis);
                    prefEditor.apply();
                }

                ListView sessionList = (ListView) findViewById(R.id.sessionList);
                ArrayAdapter<String> myAdapter = (ArrayAdapter<String>)sessionList.getAdapter();
                myAdapter.remove(myAdapter.getItem(info.position));
                myAdapter.notifyDataSetChanged();

                EditActivity.remarks.remove(info.position);
                SharedPreferences sharedprefs = getSharedPreferences("Remarks",MODE_PRIVATE);
                SharedPreferences.Editor prefsEditor = sharedprefs.edit();
                prefsEditor.putString("remarks", TextUtils.join("&", EditActivity.remarks));
                prefsEditor.apply();

                sessionCount--;
                prefEditor.putInt("sessionCount",sessionCount);

                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

/* Overflow menu option to send file via filesharer */

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.send_file:
                startActivity(new Intent(this, io.github.karuppiah7890.filesharer.SenderActivity.class));
                return true;

            default:
                return super.onOptionsItemSelected(item);

        }
    }

}
