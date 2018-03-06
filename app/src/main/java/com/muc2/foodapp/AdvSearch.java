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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;
import java.util.Set;

public class AdvSearch extends AppCompatActivity {
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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.adv_search);

        Button btn1 = findViewById(R.id.button);
        if (btn1 != null) {
            btn1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(AdvSearch.this, SearchPage.class));
                }
            });
        }
        Button btn2 = findViewById(R.id.button2);
        if (btn2 != null) {
            btn2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(AdvSearch.this, SimpleSearch.class));
                }
            });
        }
        Button btn3 = findViewById(R.id.button3);
        if (btn3 != null) {
            btn3.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(AdvSearch.this, AdvSearch.class));
                }
            });
        }
        sharedPref = this.getSharedPreferences("AB",Context.MODE_PRIVATE);
    }

    public void submit_onClick(View view) {

        String input;
        // The textbox where the user enters in the name to search for
        EditText userInput = (EditText) findViewById(R.id.userInput);
        if (userInput != null) {
            input = userInput.getText().toString();

            // Clear any error message if it exists
            TextView error = (TextView) findViewById(R.id.error);
            if (error != null) { error.setText(""); }

            // Show the results of the query
            show_locations(input,1);
        }
    }
    public void btypes(View view) {
        int inpid = 0;
        String type;
        // The textbox where the user enters in the name to search for
        Spinner typeinp = (Spinner) findViewById(R.id.spin1);
        type = typeinp.getSelectedItem().toString();

            // Clear any error message if it exists
        TextView error = (TextView) findViewById(R.id.error);
            if (error != null) { error.setText(""); }

        switch(type){
            case "All" : inpid = -1;
                         break;
            case "Distributors/Transporters" : inpid =7 ;
                break;
            case "Farmers/growers" : inpid = 7838;
                break;
            case "Hospitals/Childcare/Caring Premises" : inpid = 5;
                break;
            case "Hotel/bed & breakfast/guest house" : inpid = 7842;
                break;
            case "Importers/Exporters" : inpid = 14;
                break;
            case "Manufacturers/packers" : inpid = 7839 ;
                break;
            case "Mobile caterer" : inpid = 7846;
                break;
            case "Other catering premises" : inpid = 7841;
                break;
            case "Pub/bar/nightclub" : inpid = 7843;
                break;
            case "Restaurant/Cafe/Canteen" : inpid = 1;
                break;
            case "Retailers - other" : inpid =4613 ;
                break;
            case "Retailers - supermarkets/hypermarkets" : inpid =7840 ;
                break;
            case "School/college/university" : inpid =7845 ;
                break;
            case "Takeaway/sandwich shop" : inpid =7844 ;
                break;
        }

        String input;
        input = Integer.toString(inpid);
        // Show the results of the query
            show_locations(input,2);

    }
    public void show_locations(String input, int chk) {

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            HttpURLConnection urlConnection = null;
            String address="";
            // Using URLEncoder to ensure that spaces are properly formatted and the entire query is passed in
            //String query = URLEncoder.encode(input);
            if(chk==1) {
                address = "http://api.ratings.food.gov.uk/Establishments?ratingkey=" + input + "&pagenumber=10&pagesize=15";
            }
            if(chk==2){
                address = "http://api.ratings.food.gov.uk/Establishments?BusinessTypeId=" + input +"&pagenumber=10&pagesize=15";
            }
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
                //((TextView) findViewById(R.id.error)).setText(responseBody);
                ins.close();
                in.close();
                // we should now have one big string with the entire fetched resource

                // If there are no results, inform the user
                if (responseBody.startsWith("[]")) {
                    TextView error = (TextView) findViewById(R.id.error);
                    if (error != null) {
                        error.setText("error");
                        error.setTextAppearance(this, R.style.Error);
                    }
                }
                // If the server returns anything other than an array, it's probably an error
                if (!responseBody.startsWith("[")) {
                    TextView error = (TextView) findViewById(R.id.error);
                    if (error != null) {
                        error.setText("Invalid response from API");
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
            table = (TableLayout) findViewById(R.id.table_location);
            if (table != null) {
                table.removeAllViews();
            }

            // Loops through all the results in the JSON array, extracts the relevant fields
            // and then invokes the table method where the rows are added
            for (int i = 0; i < data.length(); i++) {
                //id = data.getJSONObject(i).getString("id");
                BusinessName = data.getJSONObject(i).getString("BusinessName");
                //((TextView) findViewById(R.id.error)).setText("Searching by location");
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
       // tr1.setOnClickListener(tableRowOnClickListener);

        // Create a row in the table for address line 1
        TableRow tr2 = new TableRow(this);
        tr2.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT));
        // Insert the address line 1 and postcode into the table row
        TextView address1 = new TextView(this);
        address1.setText(AddressLine1);
        tr2.addView(address1);
        tr2.setClickable(true);
        tr2.setTag(id);
      //  tr2.setOnClickListener(tableRowOnClickListener);

        // Create a row in the table for address line 2
        TableRow tr3 = new TableRow(this);
        tr3.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT));
        // Insert address line 2
        TextView address2 = new TextView(this);
        address2.setText(AddressLine2);
        tr3.addView(address2);
        ImageView fav = new ImageView(this);
        try{
            InputStream stream2 = getAssets().open("favico.png");
            Drawable d2 = Drawable.createFromStream(stream2,null);
            fav.setImageDrawable(d2);
            stream2.close();
        } catch (IOException e){
            e.printStackTrace();
        }
        tr3.addView(fav);
        tr3.setTag(BusinessName);
        tr3.setOnClickListener(tableRowOnClickListener2);
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

    private View.OnClickListener tableRowOnClickListener2 = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int i;
            sharedPref = getSharedPreferences("AB",Context.MODE_PRIVATE);
            Map<String,?> entries = sharedPref.getAll();
            Set<String> keys = entries.keySet();
            i=keys.size();
            String thiskey = "fav"+Integer.toString(i+1);
            String nametofav = v.getTag().toString();
            nametofav=nametofav.replaceAll("/^[a-zA-Z ]*$/", "").toLowerCase();

            Context context = getApplicationContext();
            CharSequence text = nametofav+" added to favorites";
            int duration = Toast.LENGTH_LONG;

            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString(thiskey,nametofav);
            editor.commit();

        }
    };

    public void viewfav(View view){
        Intent intent = new Intent(AdvSearch.this, FavsActivity.class);
        startActivity(intent);
    }

}
