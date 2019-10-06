package zfani.assaf.navigationapp;

import android.graphics.Point;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;

import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapActivity extends FragmentActivity implements OnMapReadyCallback, SensorEventListener {

    private GoogleMap map;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private boolean isZoomIn;

    // record the compass picture angle turned
    private float currentDegree = 0f;

    // device sensor manager
    private SensorManager mSensorManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        createLocationRequest();
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    loadMapWithLocation(location);
                }
            }
        };
        fusedLocationClient.getLastLocation().addOnSuccessListener(this, this::loadMapWithLocation);
        // initialize your android device sensor capabilities
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());

        // for the system's orientation sensor registered listeners
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION), SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    protected void onPause() {
        super.onPause();
        fusedLocationClient.removeLocationUpdates(locationCallback);

        // to stop the listener and save battery
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.setMyLocationEnabled(true);
        int mtLocationId = 2;
        View mtLocationButton = findViewById(mtLocationId);
        mtLocationButton.setVisibility(View.GONE);
        findViewById(R.id.btnMyLocation).setOnClickListener(v -> mtLocationButton.callOnClick());
        findViewById(R.id.btnToggleZoom).setOnClickListener(v -> {
            if (isZoomIn) {
                map.animateCamera(CameraUpdateFactory.zoomOut());
                isZoomIn = false;
            } else {
                map.animateCamera(CameraUpdateFactory.zoomIn());
                isZoomIn = true;
            }
        });
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        // get the angle around the z-axis rotated
        float degree = Math.round(event.values[0]);

        // create a rotation animation (reverse turn degree degrees)
        RotateAnimation rotateAnimation = new RotateAnimation(currentDegree, -degree, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);

        // how long the animation will take place
        rotateAnimation.setDuration(210);

        // set the animation after the end of the reservation status
        rotateAnimation.setFillAfter(true);

        // Start the animation
        findViewById(R.id.imageViewCompass).startAnimation(rotateAnimation);
        currentDegree = -degree;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private void createLocationRequest() {
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setSmallestDisplacement(10);
    }

    private void loadMapWithLocation(Location location) {
        if (map != null && location != null) {
            map.clear();
            LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
            MarkerOptions markerOptions;
            map.addMarker(markerOptions = new MarkerOptions().position(currentLatLng));
            Projection projection = map.getProjection();
            LatLng markerPosition = markerOptions.getPosition();
            Point markerPoint = projection.toScreenLocation(markerPosition);
            Point targetPoint = new Point(markerPoint.x, markerPoint.y - getWindow().getDecorView().getHeight() / 3);
            LatLng targetPosition = projection.fromScreenLocation(targetPoint);
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(targetPosition, 17f));
        }
    }
}
