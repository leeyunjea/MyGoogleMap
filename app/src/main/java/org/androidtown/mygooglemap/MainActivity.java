package org.androidtown.mygooglemap;

import android.Manifest;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

import static org.androidtown.mygooglemap.R.id.map;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    protected static final String TAG = "MainActivity";
    private EditText editText;
    private Button find;
    private TextView result;
    private GoogleMap mMap;
    private Marker mPerth;
    private FusedLocationProviderClient mFusedLocationClient;
    private static final int RC_LOCATION = 1;
    protected Location mLastLocation;
    protected double mLastLatitude;
    protected double mLastLognitude;
    protected String lastLocationAddress;
    protected String edit_string;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editText = (EditText)findViewById(R.id.editText);
        find = (Button)findViewById(R.id.find);
        result = (TextView)findViewById(R.id.textView);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        SupportMapFragment mapFragment = (SupportMapFragment)getSupportFragmentManager().findFragmentById(map);
        mapFragment.getMapAsync(this);

        getLastLocation();

        Log.i("yunjae", "onCreate(): " + mLastLatitude + " " + mLastLognitude);

        find.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //카메라뷰이동?
                edit_string = editText.getText().toString();
                toAddress();
                updateCamera();
                updateTextView();
            }
        });
    }

    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        LatLng location = new LatLng(37, 127);
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(location));
        Log.i("yunjae", "onMapReady(): " + mLastLatitude + " " + mLastLognitude);

    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    @Override
    public boolean onMarkerClick(final Marker marker) {
        if(marker.equals(mPerth)) {

        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @SuppressWarnings("MissingPermission")
    @AfterPermissionGranted(RC_LOCATION)
    public void getLastLocation() {
        String[] perms = {Manifest.permission.ACCESS_FINE_LOCATION};
        if(EasyPermissions.hasPermissions(this, perms)) {
            mFusedLocationClient.getLastLocation().addOnCompleteListener(this,
                    new OnCompleteListener<Location>() {
                        @Override
                        public void onComplete(@NonNull Task<Location> task) {
                            if(task.isSuccessful() && task.getResult() != null) {
                                mLastLocation = task.getResult();
                                try {
                                    Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.KOREA);
                                    List<Address> addresses = geocoder.getFromLocation(mLastLocation.getLatitude(), mLastLocation.getLongitude(), 1);
                                    if (addresses.size() > 0) {
                                        mLastLatitude = mLastLocation.getLatitude();
                                        mLastLognitude = mLastLocation.getLongitude();
                                        Address bestResult = (Address) addresses.get(0);
                                        lastLocationAddress = bestResult.getFeatureName();
                                        Log.i("yunjae", "getLastLocation(): " + mLastLatitude + " " + mLastLognitude);
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                updateCamera();
                                updateTextView();
                            }else {
                                Log.w(TAG, "getLastLocation:exception", task.getException());
                            }
                        }
                    });
        }else {
            EasyPermissions.requestPermissions(this, "This app needs access to your location to know where you are.", RC_LOCATION, perms);
        }
    }

    public void toAddress(){
        try {
            Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.KOREA);
            List<Address> addresses = geocoder.getFromLocationName(edit_string, 1);
            if (addresses.size() > 0) {
                Address bestResult = (Address) addresses.get(0);
                mLastLatitude = bestResult.getLatitude();
                mLastLognitude = bestResult.getLongitude();
                lastLocationAddress = bestResult.getFeatureName();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void updateCamera() {
        Log.i("yunjae", "updateCamera(): " +  + mLastLatitude + " " + mLastLognitude);
        LatLng location = new LatLng(mLastLatitude, mLastLognitude);
        mMap.clear();
        mMap.addMarker(new MarkerOptions().position(location).title(lastLocationAddress));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15));
        mMap.setOnMarkerClickListener(this);
    }
    public void updateTextView() {
        result.setText("[" + mLastLatitude + ", " + mLastLognitude + "]");
    }

}
