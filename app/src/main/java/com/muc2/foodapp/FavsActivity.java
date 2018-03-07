package com.muc2.foodapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextClock;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;
import java.util.Set;

import static android.os.Build.ID;

public class FavsActivity extends AppCompatActivity {
    private Object id1;
    private String id;
    private String BusinessName;
    private String AddressLine1;
    private String AddressLine2;
    private String AddressLine3;
    private String PostCode;
    private String RatingValue;
    private String responseBody;
    private TableLayout table;
    private SharedPreferences sharedPref;
    private String curKey;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favs);
        table = (TableLayout) findViewById(R.id.table_location2);
        if (table != null) {
            table.removeAllViews();
        }
        String addednames="";
        sharedPref = getSharedPreferences("AB",Context.MODE_PRIVATE);
        Map<String,?> entries = sharedPref.getAll();
        Set<String> keys = entries.keySet();
        int i=0;
        for (String key : keys) {
            curKey = key;
            String name = sharedPref.getString(key,"");

            if(!addednames.contains(name)) {
                addednames = addednames + name;
                get_locations(name);
            }
        }

    }
    @Override
    public void onBackPressed()
    {
        super.onBackPressed();
        startActivity(new Intent(FavsActivity.this, SearchPage.class));
        finish();

    }

    private void get_locations(String input) {

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            HttpURLConnection urlConnection = null;

            // Using URLEncoder to ensure that spaces are properly formatted and the entire query is passed in

            String query = URLEncoder.encode(input);
            //String query2 = URLEncoder.encode(input2,"UTF-8");
            String address = "http://api.ratings.food.gov.uk/Establishments?name="+query+"&pagesize=1";

            try {
                URL url = new URL(address);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestProperty("Accept","application/json");
                urlConnection.addRequestProperty("x-api-version","2");
                InputStreamReader ins = new InputStreamReader(urlConnection.getInputStream());
                BufferedReader in = new BufferedReader(ins);
                // read the input stream as for normal I/O
                String line;
                responseBody = "";
                while ((line = in.readLine()) != null) {
                    responseBody = responseBody + line;
                }
                responseBody=responseBody.replace("{\"establishments\":","");
                responseBody=responseBody.replace("FHRSID","id");
                ins.close();
                in.close();

                // we should now have one big string with the entire fetched resource

                // If there are no results, inform the user
                if (responseBody.equals("[]")) {
                    TextView error = (TextView) findViewById(R.id.error);
                    if (error != null) {
                        error.setText("The query did not return any results");
                        error.setTextAppearance(this, R.style.Error);
                    }
                }
                // If the server returns anything other than an array, it's probably an error
                if (!responseBody.startsWith("[")) {
                    TextView error = (TextView) findViewById(R.id.error);
                    if (error != null) {
                        error.setText("invalid response from API");
                        error.setTextAppearance(this, R.style.Error);
                    }
                }

                // Pass the responseBody to the parseJSON method for analysis
                parseJSON(responseBody);

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                assert urlConnection != null;
                urlConnection.disconnect();
            }
        } else {
            // display error
            TextView error = (TextView) findViewById(R.id.error);
            if (error != null) {
                error.setText("invalid or empty response from API");
                error.setTextAppearance(this, R.style.Error);
            }
        }
    }
    private void parseJSON(String responseBody) {
        try {

            // The response from the server is in JSON format
            JSONArray data = new JSONArray(responseBody);

            // Table where the results will be displayed, first remove the previous results


            // Loops through all the results in the JSON array, extracts the relevant fields
            // and then invokes the table method where the rows are added
            for (int i = 0; i < data.length(); i++) {
                id1 = data.getJSONObject(i).get("id");
                id = id1.toString();
                BusinessName = data.getJSONObject(i).getString("BusinessName");

                if (BusinessName.length() > 25) {
                    BusinessName = BusinessName.substring(0, 25) + "...";
                }
                AddressLine1 = data.getJSONObject(i).getString("AddressLine1");
                AddressLine2 = data.getJSONObject(i).getString("AddressLine2");
                AddressLine3 = data.getJSONObject(i).getString("AddressLine3");
                // If AddressLine1 from the results is empty, the other two address fields are moved up
                if (AddressLine1 == null || AddressLine1.isEmpty()) {
                    AddressLine1 = data.getJSONObject(i).getString("AddressLine2");
                    AddressLine2 = data.getJSONObject(i).getString("AddressLine3");
                    AddressLine3 = "";
                }
                // If the address field is too long, it is truncated
                if (AddressLine1.length() > 35) {
                    AddressLine1 = AddressLine1.substring(0, 25) + "...";
                }
                PostCode = data.getJSONObject(i).getString("PostCode");
                String Rating = data.getJSONObject(i).getString("RatingValue");
                if (Rating.equals("-1")) {
                    Rating = "exempt";
                }
                // Convert the Rating value to the relevant filename to fetch the right picture
                RatingValue = "rating" + Rating + ".png";

                // Insert each row into the table one by one
                createTable();
            }
        } catch (JSONException je) {
            je.printStackTrace();
        }
    }


    /**
     * Method to add the locations to the table rows and the rows to the table
     */
    private void createTable() {

        // Create a row in the table for the Business Name
        TableRow tr1 = new TableRow(this);
        tr1.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT));
        // Insert the Business Name into the table row
        TextView name = new TextView(this);
        name.setText(BusinessName);
        name.setTextAppearance(this, R.style.BusinessName);
        tr1.addView(name);
        // Insert the rating value into the table row
        ImageView rating = new ImageView(this);
        try {
            InputStream stream = getAssets().open(RatingValue);
            Drawable d = Drawable.createFromStream(stream, null);
            rating.setImageDrawable(d);
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

//        int res = getResources().getIdentifier(RatingValue, "drawable", getPackageName());
//        rating.setImageResource(res);
        rating.setPadding(30, 0, 0, 0);
        tr1.addView(rating);

        tr1.setClickable(true);
        tr1.setTag(id);
        //tr1.setOnClickListener(tableRowOnClickListener);

        // Create a row in the table for address line 1
        TableRow tr2 = new TableRow(this);
        tr2.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT));
        // Insert the address line 1 and postcode into the table row
        TextView address1 = new TextView(this);
        address1.setText(AddressLine1);
        tr2.addView(address1);
        tr2.setClickable(true);
        tr2.setTag(id);
        //tr2.setOnClickListener(tableRowOnClickListener);

        // Create a row in the table for address line 2
        TableRow tr3 = new TableRow(this);
        tr3.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT));
        // Insert address line 2
        TextView address2 = new TextView(this);
        address2.setText(AddressLine2);
        tr3.addView(address2);
        ImageView fav = new ImageView(this);
        try{
            InputStream stream2 = getAssets().open("favrem.png");
            Drawable d2 = Drawable.createFromStream(stream2,null);
            fav.setImageDrawable(d2);
            stream2.close();
        } catch (IOException e){
            e.printStackTrace();
        }
        tr3.addView(fav);
        tr3.setTag(curKey);
        tr3.setOnClickListener(tableRowOnClickListener3);
        //tr3.setOnClickListener(tableRowOnClickListener2);
        // Add the rows to the table
        table.addView(tr1);
        table.addView(tr2);
        table.addView(tr3);

        // Only add address line 3 if it exists
        if (AddressLine3 != null && !AddressLine3.isEmpty()) {
            TableRow tr4 = new TableRow(this);
            tr4.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT));
            TextView address3 = new TextView(this);
            address3.setText(AddressLine3);
            tr4.addView(address3);
            table.addView(tr4);
        }

        // Create a row in the table for the postcode
        TableRow tr5 = new TableRow(this);
        tr5.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT));
        TextView postcode = new TextView(this);
        postcode.setText(PostCode);
        tr5.addView(postcode);
        tr5.setPadding(0,0,0,20);
        table.addView(tr5);


        // Padding the left of the table to bring it away from the edge
        // Padding the bottom of the table because otherwise the last row is obscured
        table.setPadding(20, 0, 0, 50);
    }

    private View.OnClickListener tableRowOnClickListener3 = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int i;
            sharedPref = getSharedPreferences("AB",Context.MODE_PRIVATE);
            String keytorem = v.getTag().toString();
            String nm = sharedPref.getString(keytorem,"");
            Context context = getApplicationContext();
            CharSequence text = nm+" removed from favorites";
            int duration = Toast.LENGTH_LONG;

            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.remove(keytorem);
            editor.commit();
            Intent intent = new Intent(FavsActivity.this, FavsActivity.class);
            startActivity(intent);
        }
    };
}
