package zfani.assaf.navigationapp;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActvity extends AppCompatActivity {

    private static final int KEY_REQUEST_LOCATION_PERMISSION = 10001;
    private static final int KEY_ACTION_APPLICATION_DETAILS_SETTINGS = 10002;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkLocationPermission();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == KEY_ACTION_APPLICATION_DETAILS_SETTINGS) {
            checkLocationPermission();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == KEY_REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startMapActivity();
            } else {
                showPermissionRequestAlert(this);
            }
        }
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, KEY_REQUEST_LOCATION_PERMISSION);
            return;
        }
        startMapActivity();
    }

    private void startMapActivity() {
        startActivity(new Intent(this, MapActivity.class));
        finish();
    }

    private void showPermissionRequestAlert(Activity activity) {
        new AlertDialog.Builder(activity).setCancelable(false)
                .setTitle(activity.getString(R.string.dialog_location_permission_title))
                .setMessage(activity.getString(R.string.dialog_location_permission_message))
                .setPositiveButton(activity.getString(R.string.dialog_location_permission_confirm), (dialog, which) -> {
                    Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    intent.setData(Uri.parse("package:" + activity.getPackageName()));
                    activity.startActivityForResult(intent, KEY_ACTION_APPLICATION_DETAILS_SETTINGS);
                }).create().show();
    }
}
