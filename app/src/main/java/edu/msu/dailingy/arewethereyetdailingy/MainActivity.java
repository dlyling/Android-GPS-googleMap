package edu.msu.dailingy.arewethereyetdailingy;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private LocationManager locationManager = null;

    private double latitude = 0;
    private double longitude = 0;
    private boolean valid = false;

    private double toLatitude = 0;
    private double toLongitude = 0;
    private String destination = toLatitude + ", " + toLongitude;
    private String to = "";

    private SharedPreferences settings = null;

    private final static String TO = "to";
    private final static String TOLAT = "tolat";
    private final static String TOLONG = "tolong";

    public static final int DRIVING = 0;
    public static final int WALKING = 1;
    public static final int BICYCLING = 2;

    private String favoriate;

    private int option = 0;

    private ActiveListener activeListener = new ActiveListener();

    /**
     * The hat choice spinner
     */
    private Spinner getSpinner() {
        return (Spinner) findViewById(R.id.spinner);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        settings = PreferenceManager.getDefaultSharedPreferences(this);
        to = settings.getString(TO, "2250 Engineering");

        toLatitude = Double.parseDouble(settings.getString(TOLAT, "42.724303"));
        toLongitude = Double.parseDouble(settings.getString(TOLONG, "-84.480507"));

        // Get the location manager
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Also, dont forget to add overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //   int[] grantResults)
            // to handle the case where the user grants the permission.
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }

        // Force the screen to say on and bright
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        /*
         * Set up the spinner
         */
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.spinner, android.R.layout.simple_spinner_item);

        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Apply the adapter to the spinner
        getSpinner().setAdapter(adapter);

        getSpinner().setOnItemSelectedListener(new SpinnerListener());
    }

    private class SpinnerListener implements Spinner.OnItemSelectedListener {

        @Override
        public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
                                   long arg3) {
            option = arg2;
        }

        @Override
        public void onNothingSelected(AdapterView<?> arg0) {
            // TODO Auto-generated method stub

        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    /**
     * Handle an options menu selection
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.itemSpartan:
                newTo("Sparty", 42.731138, -84.487508);
                return true;

            case R.id.itemHome:
                newTo("My Home", 42.760477, -84.486381);
                return true;

            case R.id.item2250:
                newTo("2250 Engineering", 42.724303, -84.480507);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Handle setting a new "to" location.
     * @param address Address to display
     * @param lat latitude
     * @param lon longitude
     */
    private void newTo(String address, double lat, double lon) {
        to = address;
        toLatitude = lat;
        toLongitude = lon;

        setUI();
    }


    public void onNew(View view) {
        EditText location = (EditText)findViewById(R.id.editLocation);
        final String address = location.getText().toString().trim();
        newAddress(address);
    }

    public void onNavigate(View view) {
        String choice = "d";
        destination = toLatitude + ", " + toLongitude;
        switch (option) {
                case DRIVING:
                    choice = "d";
                    break;
                case WALKING:
                    choice = "w";
                    break;
                case BICYCLING:
                    choice = "b";
                    break;
        }

        // Create a Uri from an intent string. Use the result to create an Intent.
        Uri gmmIntentUri = Uri.parse("google.navigation:q=" + Uri.encode(destination) + "&mode=" + Uri.encode(choice));

        // Create an Intent from gmmIntentUri. Set the action to ACTION_VIEW
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        // Make the Intent explicit by setting the Google Maps package
        mapIntent.setPackage("com.google.android.apps.maps");

        if (mapIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(mapIntent);
        }
    }

    public void onSave(View view) {

        // Bring up the picture selection dialog box
        SaveDlg dialog = new SaveDlg();
        dialog.show(getSupportFragmentManager(), null);
    }

    public void onLoad(View view){
        // Bring up the picture selection dialog box
        LoadDlg dialog = new LoadDlg();
        dialog.show(getSupportFragmentManager(), null);
    }

    private void newAddress(final String address) {
        if(address.equals("")) {
            // Don't do anything if the address is blank
            return;
        }

        new Thread(new Runnable() {

            @Override
            public void run() {
                lookupAddress(address);

            }

        }).start();
    }

    /**
     * Look up the provided address. This works in a thread!
     * @param address Address we are looking up
     */
    private void lookupAddress(final String address) {
        Geocoder geocoder = new Geocoder(MainActivity.this, Locale.US);
        boolean exception = false;
        List<Address> locations;
        try {
            locations = geocoder.getFromLocationName(address, 1);
        } catch(IOException ex) {
            // Failed due to I/O exception
            locations = null;
            exception = true;
        }

        final boolean tempException = exception;
        final List<Address> tempAddress = locations;

        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                newLocation(address,tempException, tempAddress);
            }
        });
    }



    private void registerListeners() {
        unregisterListeners();

        // Create a Criteria object
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setPowerRequirement(Criteria.POWER_HIGH);
        criteria.setAltitudeRequired(true);
        criteria.setBearingRequired(false);
        criteria.setSpeedRequired(false);
        criteria.setCostAllowed(false);

        String bestAvailable = locationManager.getBestProvider(criteria, true);

        if (bestAvailable != null) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    Activity#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for Activity#requestPermissions for more details.
                return;
            }
            locationManager.requestLocationUpdates(bestAvailable, 500, 1, activeListener);
            TextView viewProvider = (TextView)findViewById(R.id.textProvider);
            viewProvider.setText(bestAvailable);
            Location location = locationManager.getLastKnownLocation(bestAvailable);
            onLocation(location);
        }
    }

    private void unregisterListeners() {
        locationManager.removeUpdates(activeListener);
    }

    private void onLocation(Location location) {
        if(location == null) {
            return;
        }

        latitude = location.getLatitude();
        longitude = location.getLongitude();
        valid = true;

        setUI();
    }

    private void newLocation(String address, boolean exception, List<Address> locations) {

        if(exception) {
            Toast.makeText(MainActivity.this, R.string.exception, Toast.LENGTH_SHORT).show();
        } else {
            if(locations == null || locations.size() == 0) {
                Toast.makeText(this, R.string.couldnotfind, Toast.LENGTH_SHORT).show();
                return;
            }

            EditText location = (EditText)findViewById(R.id.editLocation);
            location.setText("");

            // We have a valid new location
            Address a = locations.get(0);
            newTo(address, a.getLatitude(), a.getLongitude());

        }
    }


    /**
     * Set all user interface components to the current state
     */
    @SuppressLint("DefaultLocale")
    private void setUI() {

        TextView temp = (TextView) findViewById(R.id.textTo);
        temp.setText(to);
        if (!valid) {
            TextView temp1 = (TextView) findViewById(R.id.textLatitude);
            temp1.setText(" ");
            @SuppressLint("CutPasteId") TextView temp2 = (TextView) findViewById(R.id.textDistance);
            temp2.setText(" ");
            @SuppressLint("CutPasteId") TextView temp3 = (TextView) findViewById(R.id.textLongitude);
            temp3.setText(" ");
        }
        if (valid) {
            TextView temp1 = (TextView) findViewById(R.id.textLatitude);
            temp1.setText(String.valueOf(latitude));
            @SuppressLint("CutPasteId") TextView temp2 = (TextView) findViewById(R.id.textLongitude);
            temp2.setText(String.valueOf(longitude));


            Location locationA = new Location("A");

            locationA.setLatitude(latitude);
            locationA.setLongitude(longitude);

            Location locationB = new Location("B");

            locationB.setLatitude(toLatitude);
            locationB.setLongitude(toLongitude);

            float distance = locationA.distanceTo(locationB);

            @SuppressLint("CutPasteId") TextView temp3 = (TextView) findViewById(R.id.textDistance);
            temp3.setText(String.format("%1$6.1fm", distance));

        }
    }

    /**
     * Called when this application becomes foreground again.
     */
    @Override
    protected void onResume() {
        super.onResume();

        TextView viewProvider = (TextView)findViewById(R.id.textProvider);
        viewProvider.setText("");

        setUI();
        registerListeners();
    }

    /**
     * Called when this application is no longer the foreground application.
     */
    @Override
    protected void onPause() {
        unregisterListeners();
        super.onPause();
    }

    private class ActiveListener implements LocationListener {

        @Override
        public void onLocationChanged(Location location) {
            onLocation(location);
        }

        @Override
        public void onStatusChanged(String s, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String s) {

        }

        @Override
        public void onProviderDisabled(String s) {
            registerListeners();
        }
    };
}
