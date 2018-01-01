package dwaittrials.com.patientdata2;

import android.annotation.TargetApi;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class DataReceiver extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_receiver);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        new receiveData().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void[]) null);
        Button refreshButton = (Button) findViewById(R.id.refresh_button);

        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new receiveData().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void[]) null);
            }
        });

    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    class receiveData extends AsyncTask<Object, Object, Void> {

        String url = "http://192.168.43.171:8080/data-server/";
        TextView receivedTempView = (TextView) findViewById(R.id.received_temp);
        TextView receivedHumidityView = (TextView) findViewById(R.id.received_humidity);

        ArrayList<Double> temp = new ArrayList<>();
        ArrayList<Double> humidity = new ArrayList<>();

        String piUrl = "http://mlceeri.pythonanywhere.com/data-session/";

        @Override
        protected Void doInBackground(Object... params) {

            /* Receive data from pi3 server */

            OkHttpClient client = new OkHttpClient();

            Request request = new Request.Builder()
                    .url(url)
                    .build();

            try (Response response = client.newCall(request).execute()) {

                JSONArray responseArray = new JSONArray(response.body().string());

                for (int i = 0; i < responseArray.length(); i++) {
                    JSONObject responseObject = responseArray.getJSONObject(i);
                    temp.add(responseObject.getDouble("temp"));
                    humidity.add(responseObject.getDouble("humidity"));
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String tempText = "", humidText = "";
                        for (int i = 0; i < temp.size(); i++) {
                            tempText = tempText + "\n" + temp.get(i);
                            humidText = humidText + "\n" + humidity.get(i);
                        }
                        receivedTempView.setText(tempText);
                        receivedHumidityView.setText(humidText);
                    }
                });


                /* Send data to main server */

                OkHttpClient piClient = new OkHttpClient();
                RequestBody piFormBody = new MultipartBody.Builder()
                        .addFormDataPart("data", responseArray.toString())
                        .build();

                Request piRequest = new Request.Builder().url(piUrl).post(piFormBody).build();
                try (Response piResponse = piClient.newCall(piRequest).execute()) {
                    Log.i("LOOK HERE", "Response body for pi data is\n" + piResponse.body().string());
                } catch (IOException e) {
                    Log.e("LOOK HERE", "Error Response when sending to main server is: ", e);
                }


            } catch (IOException e) {
                Log.e("LOOK HERE", "IOException: ", e);
            } catch (JSONException e) {
                Log.e("LOOK HERE", "JSONException: ", e);
            }


            return null;
        }

    }

}
