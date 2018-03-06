package com.muc2.foodapp;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.StrictMode;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
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
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.Set;

public class SearchPage extends AppCompatActivity {
    private final int FINE_LOCATION_PERMISSION = 1;
    private double lon, lat;
    private String latitude, longitude, responseBody;
    private TableLayout table;
    private String id;
    private String BusinessName;
    private String AddressLine1;
    private String AddressLine2;
    private String AddressLine3;
    private String PostCode;
    private String RatingValue;
    private String Distance;
    private SharedPreferences sharedPref;

    LocationListener loclis;
    LocationManager locMan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_page);
        Button btn1 = (Button) findViewById(R.id.button);
        if (btn1 != null) {
            btn1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(SearchPage.this, SearchPage.class));
                }
            });
        }
        Button btn2 =  findViewById(R.id.button2);
        if (btn2 != null) {
            btn2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(SearchPage.this, SimpleSearch.class));
                }
            });
        }
        Button btn3 = findViewById(R.id.button3);
        if (btn3 != null) {
            btn3.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(SearchPage.this, AdvSearch.class));
                }
            });
        }
        sharedPref = this.getSharedPreferences("AB",Context.MODE_PRIVATE);
        ((TextView) findViewById(R.id.textView)).setText("click local Search, then search button");
        Context context = this.getApplicationContext();
        locMan = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        //get coordinates
        loclis =new LocationListener() {

            @Override
            public void onLocationChanged(Location location) {

                // Get the values from the GPS and call the other program methods
                lat = location.getLatitude();
                lon = location.getLongitude();

                // Convert the latitude and longitude values to String
                latitude = String.valueOf(lat);
                longitude = String.valueOf(lon);




            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {
//                ((TextView) findViewById(R.id.textView)).setText(provider);
//                ((TextView) findViewById(R.id.textView2)).setText("enabled");
            }

            @Override
            public void onProviderDisabled(String provider) {
//                ((TextView) findViewById(R.id.textView)).setText(provider);
//                ((TextView) findViewById(R.id.textView2)).setText("disabled");
            }
        };
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                new AlertDialog.Builder(this)
                        .setMessage("The application is about to request access to your location.")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                requestLocPerms();
                            }
                        })
                        .create()
                        .show();
            } else {
                requestLocPerms();
            }
        } else {
            attachLocManager();
        }


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case FINE_LOCATION_PERMISSION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    attachLocManager();
                } else {
                }
                return;
            }
        }
    }

    public void attachLocManager() {
        try {
            locMan.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, loclis);
            locMan.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, loclis);
        } catch (SecurityException err) {
        }
    }

    public void requestLocPerms() {
        ActivityCompat.requestPermissions(SearchPage.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, FINE_LOCATION_PERMISSION);
    }

    public void localsearch(View view) {

        //use coordinates and call request api results
        Context context = getApplicationContext();
        CharSequence text = "sending request for establishments";
        int duration = Toast.LENGTH_SHORT;

        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
        get_locations();
    }



    private void get_locations() {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            HttpURLConnection urlConnection = null;
//            if (latitude.isEmpty() && longitude.isEmpty()){
//                Context context = getApplicationContext();
//                CharSequence text = "sorry! please click the local search button and then click the search icon again";
//                int duration = Toast.LENGTH_SHORT;
//
//                Toast toast = Toast.makeText(context, text, duration);
//                toast.show();
//            }
            String address = "http://sandbox.kriswelsh.com/hygieneapi/hygiene.php?op=s_loc&lat=" + latitude + "&long=" + longitude;
//            Context context = getApplicationContext();
//            CharSequence text = "fetching results from FSA API";
//            int duration = Toast.LENGTH_SHORT;
//
//            Toast toast = Toast.makeText(context, text, duration);
//            toast.show();

            try {
                URL url = new URL(address);
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStreamReader ins = new InputStreamReader(urlConnection.getInputStream());
                BufferedReader in = new BufferedReader(ins);
                // read the input stream as for normal I/O
                String line;
                responseBody = "";
                while ((line = in.readLine()) != null) {
                    responseBody = responseBody + line;
                }
                ins.close();
                in.close();
                // we should now have one big string with the entire fetched resource

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
                id = data.getJSONObject(i).getString("id");
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
                Distance = data.getJSONObject(i).getString("DistanceKM");
                if (Float.parseFloat(Distance) < 0.1) {
                    Distance = "< 0.1";
                } else {
                    Distance = round(Distance);
                }

                // Insert each row into the table one by one
                createTable();
            }
        } catch (JSONException je) {
            je.printStackTrace();
        }
        Context context = getApplicationContext();
        CharSequence text = "Click on the establishment name for map view";
        int duration = Toast.LENGTH_SHORT;

        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
    }

    private static String round(String value) {
        double doubleValue = Double.parseDouble(value);
        BigDecimal bd = new BigDecimal(doubleValue);
        bd = bd.setScale(2, RoundingMode.HALF_UP);
        return String.valueOf(bd.doubleValue());
    }

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
        tr1.setOnClickListener(tableRowOnClickListener);

        // Create a row in the table for address line 1
        TableRow tr2 = new TableRow(this);
        tr2.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT));
        // Insert the address line 1 and postcode into the table row
        TextView address1 = new TextView(this);
        address1.setText(AddressLine1);
        tr2.addView(address1);
        // Insert the distance from the user
        TextView distance = new TextView(this);
        String dist = "dist: " + Distance + "km";
        distance.setText(dist);
        distance.setTextAppearance(this, R.style.Distance);
        distance.setPadding(30, 0, 0, 0);
        tr2.addView(distance);
        tr2.setClickable(true);
        tr2.setTag(id);
        tr2.setOnClickListener(tableRowOnClickListener);

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

    private View.OnClickListener tableRowOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(SearchPage.this, MapsActivity.class);

            // Put the JSON array as a string into the intent
            intent.putExtra("JSON", responseBody);

            // Pass the id of the row so the selected location can be identified later
            String id = v.getTag().toString();
            intent.putExtra("id", id);
            startActivity(intent);
        }
    };

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

        Intent intent = new Intent(SearchPage.this, FavsActivity.class);
        startActivity(intent);
    }
}
