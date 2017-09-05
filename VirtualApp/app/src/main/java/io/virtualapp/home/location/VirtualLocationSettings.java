package io.virtualapp.home.location;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.Toast;

import io.virtualapp.abs.ui.VActivity;

import static io.virtualapp.home.location.MarkerActivity.EXTRA_ADDTESS;
import static io.virtualapp.home.location.MarkerActivity.EXTRA_LATITUDE;
import static io.virtualapp.home.location.MarkerActivity.EXTRA_LONGITUDE;

public class VirtualLocationSettings extends VActivity {
    private static final int REQUSET_CODE = 1001;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //TODO list
        startActivityForResult(new Intent(this, MarkerActivity.class), REQUSET_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUSET_CODE) {
            if (resultCode == RESULT_OK) {
                double lat = data.getDoubleExtra(EXTRA_LATITUDE, 0);
                double lon = data.getDoubleExtra(EXTRA_LONGITUDE, 0);
                String address = data.getStringExtra(EXTRA_ADDTESS);
                Toast.makeText(this,
                        "lat=" + lat + ",lon=" + lon + "\n" + address,
                        Toast.LENGTH_SHORT).show();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
