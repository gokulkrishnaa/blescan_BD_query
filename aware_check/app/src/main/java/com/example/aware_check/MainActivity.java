package com.example.aware_check;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.aware.Aware;
import com.aware.Aware_Preferences;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class MainActivity extends Activity {

    private ProgressDialog pDialog;
    public String url="http://buildingdepot.wv.cc.cmu.edu:82/service/api/v1/Room=Test/tags";
    public String sens_type=null;
    public String sens_uuid=null;
    public String sens_val = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        GetJSONList gj = new GetJSONList();

        /* STARTS HERE */
        ContentValues new_data = new ContentValues();

        String jsonStr;

        String jsonVal;
        try {
            jsonStr = gj.execute().get();
            try {

                // Find number of Objects in data
                JSONObject jsonObj = new JSONObject(jsonStr);
                int len = jsonObj.getJSONObject("data").length();
                Log.d("Length: ", "> " + len);

                //Run through the JSON Objects in data : "sensors"
                for(int i=1;i<=len;i++){

                    JSONObject sensor = jsonObj.getJSONObject("data").getJSONObject("sensor_"+i);
                    sens_uuid = sensor.getString("name");
                    sens_type = sensor.getJSONObject("metadata").getString("Type");

                    url="http://buildingdepot.wv.cc.cmu.edu:82/service/api/v1/data/id="+sens_uuid+"/interval=100s/";
                    GetJSONList gk = new GetJSONList();
                    jsonVal = gk.execute().get();
                    JSONObject val_json = new JSONObject(jsonVal);
                    int lens = val_json.getJSONObject("data").length();
                    Log.d("Value_JSON",jsonVal);
                    Log.d("Value_Length",">" + lens);



                    Log.d(sens_type , "\t" + sens_uuid);

                    new_data.put(Provider.Example_Data.DEVICE_ID, Aware.getSetting(getApplicationContext(), Aware_Preferences.DEVICE_ID));
                    new_data.put(Provider.Example_Data.TIMESTAMP, System.currentTimeMillis());

                    new_data.put(Provider.Example_Data.SENS_TYPE, sens_type);
                    getContentResolver().insert(Provider.Example_Data.CONTENT_URI, new_data);
                    Toast.makeText(getApplicationContext(), "You have been saved!", Toast.LENGTH_LONG).show();


                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    private class GetJSONList extends AsyncTask<Void, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog
            pDialog = new ProgressDialog(MainActivity.this);
            pDialog.setMessage("Please wait...");
            pDialog.setCancelable(false);
            pDialog.show();

        }
        @Override
        protected String doInBackground(Void... params) {
            ServiceHandler sh = new ServiceHandler();

            // Making a request to url and getting response
            String jsonStr = null;
            try {
                jsonStr = sh.makeServiceCall(url, ServiceHandler.GET);
            } catch (IOException e) {
                e.printStackTrace();
            }

            return jsonStr;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            // Dismiss the progress dialog
            if (pDialog.isShowing())
                pDialog.dismiss();
        }

    }


}



