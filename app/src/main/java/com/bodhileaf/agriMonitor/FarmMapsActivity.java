package com.bodhileaf.agriMonitor;

import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v13.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.PlaceLikelihoodBufferResponse;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.PlaceDetectionClient;
import com.google.android.gms.location.places.PlaceLikelihood;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;


public class FarmMapsActivity extends FragmentActivity implements OnMapReadyCallback,MarkerDialogFragment.MarkerDialogListener  {
    private class marker_data {
        private Integer node_id;
        private Integer node_type;
        private Integer click_count;

        marker_data(Integer nodeid, Integer nodetype){
            node_id=nodeid;
            node_type=nodetype;
            click_count = 0;
        }
        marker_data() {
            click_count = 0;
            node_id= 0;
            node_type = 0;
        }

        public Integer getNode_id() {
            return node_id;
        }
        public Integer getNode_type() {
            return node_type;
        }
        public Integer getClick_count() {
            return click_count;
        }
        public void incrClick_count() {
            click_count++;
        }
        public void setNode_id(Integer nodeId) {
            node_id = nodeId;
        }
        public void setNode_type(Integer nodetype) {
            node_type = nodetype;
        }

    }

    private static final String TAG = FarmMapsActivity.class.getSimpleName();
    private GoogleMap mMap;
    private CameraPosition mCameraPosition;

    // The entry points to the Places API.
    private GeoDataClient mGeoDataClient;
    private PlaceDetectionClient mPlaceDetectionClient;

    // The entry point to the Fused Location Provider.
    private FusedLocationProviderClient mFusedLocationProviderClient;

    // A default location (bengaluru, India) and default zoom to use when location permission is
    // not granted.

    private final LatLng mDefaultLocation = new LatLng(12.9716, 77.5946);
    private static final int DEFAULT_ZOOM = 15;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private boolean mLocationPermissionGranted;

    // The geographical location where the device is currently located. That is, the last-known
    // location retrieved by the Fused Location Provider.
    private Location  mLastKnownLocation;

    // Keys for storing activity state.
    private static final String KEY_CAMERA_POSITION = "camera_position";
    private static final String KEY_LOCATION = "location";

    // Used for selecting the current place.
    private static final int M_MAX_ENTRIES = 5;
    private String[] mLikelyPlaceNames;
    private String[] mLikelyPlaceAddresses;
    private String[] mLikelyPlaceAttributions;
    private LatLng[] mLikelyPlaceLatLngs;
    private String dbFileName;
    private LatLng last_position;
    private Marker cur_marker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle extras = getIntent().getExtras();
        if (extras == null) {
            return;
        }
// get data via the key
        dbFileName  = extras.getString("filename");
        if (dbFileName == null) {
            Log.d(TAG, "onreate: db filename missing" );
            return;
            // do something with the data
        }
        Log.d(TAG, "onreate: db filename "+dbFileName );

        // Retrieve location and camera position from saved instance state.
        if (savedInstanceState != null) {
            mLastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION);
            mCameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION);
        }
        // Retrieve the content view that renders the map.
        setContentView(R.layout.activity_farm_maps);


        // Construct a GeoDataClient.
        mGeoDataClient = Places.getGeoDataClient(this, null);

        // Construct a PlaceDetectionClient.
        mPlaceDetectionClient = Places.getPlaceDetectionClient(this, null);

        // Construct a FusedLocationProviderClient.
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        // Build the map.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


    }

    /**
     * Saves the state of the map when the activity is paused.
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (mMap != null) {
            outState.putParcelable(KEY_CAMERA_POSITION, mMap.getCameraPosition());
            outState.putParcelable(KEY_LOCATION, mLastKnownLocation);
            super.onSaveInstanceState(outState);
        }
    }

    /**
     * Sets up the options menu.
     * @param menu The options menu.
     * @return Boolean.
     */
    /*
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.current_place_menu, menu);
        return true;
    }

    /**
     * Handles a click on the menu option to get a place.
     * @param item The menu item to handle.
     * @return Boolean.
     */
    /*
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.option_get_place) {
            showCurrentPlace();
        }
        return true;
    }
*/
    /**
     * Manipulates the map when it's available.
     * This callback is triggered when the map is ready to be used.
     */
    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;

        // Prompt the user for permission.
        getLocationPermission();

        // Turn on the My Location layer and the related control on the map.
        updateLocationUI();

        // Get the current location of the device and set the position of the map.
        getDeviceLocation();
        if (dbFileName == null) {
            Log.d(TAG, "onMapReady: ERROR :no db file found" );
        }
        Log.d(TAG, "onMapReady: db filename "+dbFileName );
        SQLiteDatabase farmDb = openOrCreateDatabase(dbFileName,MODE_PRIVATE ,null) ;
        Cursor nodeListResults = farmDb.rawQuery("SELECT * FROM nodesInfo",null);
        nodeListResults.moveToFirst();
        if(nodeListResults.getCount() >0) {
            do {
                Double latLocation = nodeListResults.getDouble(2);
                Double lonLocation = nodeListResults.getDouble(3);
                Integer nodeID = nodeListResults.getInt(0);
                Integer nodeType = nodeListResults.getInt(1);
                String sensorType = null;
                Log.d(TAG, "onMapReady: node: lat: " + Double.toString(latLocation) + " long :" + Double.toString(lonLocation));
                LatLng addLoc = new LatLng(latLocation, lonLocation);
                marker_data marker_obj = new marker_data(nodeID, nodeType);
                switch (nodeType) {
                    case 0:
                        sensorType = "Air Soil Sensor Node";
                        Marker new_marker = mMap.addMarker(new MarkerOptions().position(addLoc).title(sensorType + "\n #" + Integer.toString(nodeID)).icon(BitmapDescriptorFactory.fromResource(R.drawable.sensor)));
                        new_marker.setTag(marker_obj);
                        break;
                    case 1:
                        sensorType = "Water level Sensor Node";
                        new_marker = mMap.addMarker(new MarkerOptions().position(addLoc).title(sensorType + "\n #" + Integer.toString(nodeID)).icon(BitmapDescriptorFactory.fromResource(R.drawable.water_level)));
                        new_marker.setTag(marker_obj);
                        break;
                    case 2:
                        sensorType = "water flow & Actuator Node";
                        new_marker = mMap.addMarker(new MarkerOptions().position(addLoc).title(sensorType + "\n #" + Integer.toString(nodeID)).icon(BitmapDescriptorFactory.fromResource(R.drawable.water_tap)));
                        new_marker.setTag(marker_obj);
                        break;
                    default:
                        break;
                }
            } while (nodeListResults.moveToNext());
        }
        // Set a listener for marker click.
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            /** Called when the user clicks a marker. */
            @Override
            public boolean onMarkerClick(final Marker marker) {
                cur_marker = marker;
                last_position = marker.getPosition();
                marker_data marker_info = (marker_data) marker.getTag();
                Log.d(TAG, "onMarkerClick: " );
                Integer clickCount = (Integer) marker_info.getClick_count();

                Bundle bundle = new Bundle();
                bundle.putInt("node_id", marker_info.getNode_id());
                bundle.putInt("node_type",marker_info.getNode_type());
                bundle.putString("filename",dbFileName);
                // Check if a click count was set, then display the click count.

                DialogFragment newMarkerFragment = new MarkerDialogFragment();
                newMarkerFragment.setArguments(bundle);
                newMarkerFragment.show(getFragmentManager(), "MarkerDetails");

                clickCount = clickCount + 1;
                marker_info.incrClick_count();
                marker.setTag(marker_info);
                Log.d(TAG, "onMarkerClick: "+clickCount.toString() );
                    // Toast.makeText(marker.getTitle() + " has been clicked " + clickCount + " times.",
                    //        Toast.LENGTH_SHORT).show();


                // Return false to indicate that we have not consumed the event and that we wish
                // for the default behavior to occur (which is for the camera to move such that the
                // marker is centered and for the marker's info window to open, if it has one).
                return false;
            }
        });

        // Use a custom info window adapter to handle multiple lines of text in the
        // info window contents.
        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

            @Override
            // Return null here, so that getInfoContents() is called next.
            public View  getInfoWindow(Marker arg0) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {
                // Inflate the layouts for the info window, title and snippet.
                View infoWindow = getLayoutInflater().inflate(R.layout.custom_info_contents,
                        (FrameLayout ) findViewById(R.id.map), false);

                //TextView  title = ((TextView) infoWindow.findViewById(R.id.title));
                //title.setText(marker.getTitle());

                TextView snippet = ((TextView) infoWindow.findViewById(R.id.snippet));
                snippet.setText(marker.getTitle()+"\n"+marker.getPosition().toString());



                return infoWindow;
            }
        });


        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                last_position = latLng;
                Marker new_marker = mMap.addMarker(new MarkerOptions()
                        .position(latLng)
                        .title("Your marker title")
                        .snippet("Your marker snippet"));
                new_marker.setTag(new marker_data());
                cur_marker = new_marker;
            }

        });
    }



    /**
     * Gets the current location of the device, and positions the map's camera.
     */
    private void getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            if (mLocationPermissionGranted) {
                Task<Location> locationResult = mFusedLocationProviderClient.getLastLocation();
                //Task<Location> locationResult = mDefaultLocation;
                locationResult.addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull  Task<Location> task) {
                        if (task.isSuccessful()) {
                            // Set the map's camera position to the current location of the device.
                            mLastKnownLocation = task.getResult();
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                    new LatLng(mLastKnownLocation.getLatitude(),
                                            mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                            //mMap.addMarker(new MarkerOptions().position(new LatLng(mLastKnownLocation.getLatitude(),mLastKnownLocation.getLongitude())).title("Marker in Bengaluru"));
                        } else {
                            Log.d(TAG, "Current location is null. Using defaults.");
                            Log.e(TAG, "Exception: %s", task.getException());
                            mMap.moveCamera(CameraUpdateFactory
                                    .newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM));
                            mMap.getUiSettings().setMyLocationButtonEnabled(false);
                        }
                    }
                });
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }


    /**
     * Prompts the user for permission to use the device location.
     */
    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    /**
     * Handles the result of the request for location permissions.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                }
            }
        }
        updateLocationUI();
    }

    /**
     * Prompts the user to select the current place from a list of likely places, and shows the
     * current place on the map - provided the user has granted location permission.
     */
    private void showCurrentPlace() {
        if (mMap == null) {
            return;
        }

        if (mLocationPermissionGranted) {
            // Get the likely places - that is, the businesses and other points of interest that
            // are the best match for the device's current location.
            @SuppressWarnings("MissingPermission") final
            Task<PlaceLikelihoodBufferResponse> placeResult =
                    mPlaceDetectionClient.getCurrentPlace(null);
            placeResult.addOnCompleteListener
                    (new OnCompleteListener<PlaceLikelihoodBufferResponse>() {
                        @Override
                        public void onComplete(@NonNull Task<PlaceLikelihoodBufferResponse> task) {
                            if (task.isSuccessful() && task.getResult() != null) {
                                PlaceLikelihoodBufferResponse likelyPlaces = task.getResult();

                                // Set the count, handling cases where less than 5 entries are returned.
                                int count;
                                if (likelyPlaces.getCount() < M_MAX_ENTRIES) {
                                    count = likelyPlaces.getCount();
                                } else {
                                    count = M_MAX_ENTRIES;
                                }

                                int i = 0;
                                mLikelyPlaceNames = new String[count];
                                mLikelyPlaceAddresses = new String[count];
                                mLikelyPlaceAttributions = new String[count];
                                mLikelyPlaceLatLngs = new LatLng[count];

                                for (PlaceLikelihood placeLikelihood : likelyPlaces) {
                                    // Build a list of likely places to show the user.
                                    mLikelyPlaceNames[i] = (String) placeLikelihood.getPlace().getName();
                                    mLikelyPlaceAddresses[i] = (String) placeLikelihood.getPlace()
                                            .getAddress();
                                    mLikelyPlaceAttributions[i] = (String) placeLikelihood.getPlace()
                                            .getAttributions();
                                    mLikelyPlaceLatLngs[i] = placeLikelihood.getPlace().getLatLng();

                                    i++;
                                    if (i > (count - 1)) {
                                        break;
                                    }
                                }

                                // Release the place likelihood buffer, to avoid memory leaks.
                                likelyPlaces.release();

                                // Show a dialog offering the user the list of likely places, and add a
                                // marker at the selected place.
                                openPlacesDialog();

                            } else {
                                Log.e(TAG, "Exception: %s", task.getException());
                            }
                        }
                    });
        } else {
            // The user has not granted permission.
            Log.i(TAG, "The user did not grant location permission.");

            // Add a default marker, because the user hasn't selected a place.
            mMap.addMarker(new MarkerOptions()
                    .title(getString(R.string.default_info_title))
                    .position(mDefaultLocation)
                    .snippet(getString(R.string.default_info_snippet)));

            // Prompt the user for permission.
            getLocationPermission();
        }
    }

    /**
     * Displays a form allowing the user to select a place from a list of likely places.
     */
    private void openPlacesDialog() {
        // Ask the user to choose the place where they are now.
        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // The "which" argument contains the position of the selected item.
                LatLng markerLatLng = mLikelyPlaceLatLngs[which];
                String markerSnippet = mLikelyPlaceAddresses[which];
                if (mLikelyPlaceAttributions[which] != null) {
                    markerSnippet = markerSnippet + "\n" + mLikelyPlaceAttributions[which];
                }

                // Add a marker for the selected place, with an info window
                // showing information about that place.
                mMap.addMarker(new MarkerOptions()
                        .title(mLikelyPlaceNames[which])
                        .position(markerLatLng)
                        .snippet(markerSnippet));

                // Position the map's camera at the location of the marker.
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(markerLatLng,
                        DEFAULT_ZOOM));
            }
        };

        // Display the dialog.
        AlertDialog  dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.pick_place)
                .setItems(mLikelyPlaceNames, listener)
                .show();
    }

    /**
     * Updates the map's UI settings based on whether the user has granted location permission.
     */
    private void updateLocationUI() {
        if (mMap == null) {
            return;
        }
        try {
            if (mLocationPermissionGranted) {
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                mMap.setMyLocationEnabled(false);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                mLastKnownLocation = null;
                getLocationPermission();
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        EditText mkTitle = (EditText) dialog.getDialog().getWindow().findViewById(R.id.markerTitle);
        Log.d(TAG, "onDialogPositiveClick: marker title :" + mkTitle.getText());
        EditText mkDescription = (EditText) dialog.getDialog().getWindow().findViewById(R.id.markerDescription);
        Log.d(TAG, "onDialogPositiveClick: marker description :" + mkTitle.getText());
        EditText mkNodeid = (EditText) dialog.getDialog().getWindow().findViewById(R.id.markerNodeId);
        Log.d(TAG, "onDialogPositiveClick: marker node id :" + mkNodeid.getText());
        Spinner mkNodetype = (Spinner) dialog.getDialog().getWindow().findViewById(R.id.spinner_node_type);

        //Log.d(TAG, "onDialogPositiveClick: marker node type :" + mkNodetype.getText());

        // user clicked OK

        Intent configScreen = new Intent(FarmMapsActivity.this, com.bodhileaf.agriMonitor.config.class);
        configScreen.putExtra("nodeId",Integer.valueOf(mkNodeid.getText().toString()));
        configScreen.putExtra("nodeType",mkNodetype.getSelectedItemPosition());
        configScreen.putExtra("dbFileName",dbFileName);
        SQLiteDatabase farmDb = openOrCreateDatabase(dbFileName, MODE_PRIVATE ,null) ;
        String nodeCheckQuery = String.format("SELECT * from nodesInfo where nodeID is %d ",Integer.valueOf(mkNodeid.getText().toString()));
        Cursor nodeCheckResult= farmDb.rawQuery(nodeCheckQuery,null);
        if (nodeCheckResult.getCount() != 0) {
            //DELETE the node entry is a match is found
            String nodeDeleteQuery = String.format("DELETE from nodesInfo where nodeID is %d ",Integer.valueOf(mkNodeid.getText().toString()));
            farmDb.execSQL(nodeDeleteQuery);
            nodeCheckResult.close();
        }
        String insertRowQuery = String.format("INSERT INTO nodesInfo(nodeID,nodeType,latitude,longitude) VALUES(%s,%d,'%f','%f')", mkNodeid.getText().toString(), mkNodetype.getSelectedItemPosition(), last_position.latitude, last_position.longitude);
        farmDb.execSQL(insertRowQuery);
        farmDb.close();
        marker_data marker_info = (marker_data) cur_marker.getTag();
        marker_info.setNode_id(Integer.valueOf(mkNodeid.getText().toString()));
        marker_info.setNode_type(mkNodetype.getSelectedItemPosition());
        cur_marker.setTag(marker_info);
        switch(mkNodetype.getSelectedItemPosition()) {
            case 0:
                cur_marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.sensor));
                break;
            case 1:
                cur_marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.water_level));
                break;
            case 2:
                cur_marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.water_tap));
                break;
        }

        //startActivity(configScreen);
        //setContentView(R.layout.activity_config);
    }


    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {

    }


    @Override
    public void onBackPressed() {

        // TODO Auto-generated method stub
        getFragmentManager().popBackStack();
    }
}



























/*

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_farm_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    private void getLocationPermission() {
    /*
     * Request location permission, so that we can get the location of the
     * device. The result of the permission request is handled by a callback,
     * onRequestPermissionsResult.
     */
/*
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }
    */
/*
    public double CalculationByDistance(LatLng StartP, LatLng EndP) {
        int Radius = 6371;// radius of earth in Km
        double lat1 = StartP.latitude;
        double lat2 = EndP.latitude;
        double lon1 = StartP.longitude;
        double lon2 = EndP.longitude;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2)) * Math.sin(dLon / 2)
                * Math.sin(dLon / 2);
        double c = 2 * Math.asin(Math.sqrt(a));
        double valueResult = Radius * c;
        double km = valueResult / 1;
        DecimalFormat newFormat = new DecimalFormat("####");
        int kmInDec = Integer.valueOf(newFormat.format(km));
        double meter = valueResult % 1000;
        int meterInDec = Integer.valueOf(newFormat.format(meter));
        Log.i("Radius Value", "" + valueResult + "   KM  " + kmInDec
                + " Meter   " + meterInDec);

        return Radius * c;
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
/*
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;



        MarkerOptions  test1 = new MarkerOptions();
        test1.getTitle();
        double area = 100;
        double latIncr = (1/110.574) * 0.045 * Math.sqrt(area);
        double longIncr = (1/(111.32*Math.cos(mDefaultLocation.latitude)))* 0.045 * Math.sqrt(area);
        LatLngBounds bounds = new LatLngBounds(new com.google.android.gms.maps.model.LatLng(mDefaultLocation.latitude-latIncr ,
                mDefaultLocation.longitude-longIncr),new LatLng(mDefaultLocation.latitude+latIncr ,mDefaultLocation.longitude+longIncr));

        mMap.addMarker(new MarkerOptions().position(mDefaultLocation).title("Marker in Bengaluru"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(mDefaultLocation));
        //restrict view only to the 100 acres boundary with default location being the center
        mMap.setLatLngBoundsForCameraTarget(bounds);
        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds,50));



    }
    */


