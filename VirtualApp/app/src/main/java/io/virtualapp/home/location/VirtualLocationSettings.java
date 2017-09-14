package io.virtualapp.home.location;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.client.ipc.VirtualLocationManager;
import com.lody.virtual.helper.utils.VLog;
import com.lody.virtual.remote.InstalledAppInfo;
import com.lody.virtual.remote.vloc.VLocation;

import java.util.ArrayList;
import java.util.List;

import io.virtualapp.R;
import io.virtualapp.abs.ui.VActivity;
import io.virtualapp.abs.ui.VUiKit;
import io.virtualapp.home.adapters.AppLocationAdapter;
import io.virtualapp.home.models.LocationData;
import io.virtualapp.home.repo.AppRepository;

import static io.virtualapp.home.location.MarkerActivity.EXTRA_LOCATION;

public class VirtualLocationSettings extends VActivity implements AdapterView.OnItemClickListener {
    private static final int REQUSET_CODE = 1001;
    private AppRepository mRepo;
    private ListView mListView;
    private AppLocationAdapter mAppLocationAdapter;
    private LocationData mSelectData;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_settings);
        mListView = (ListView) findViewById(R.id.appdata_list);
        mRepo = new AppRepository(this);
        mAppLocationAdapter = new AppLocationAdapter(this);
        mListView.setAdapter(mAppLocationAdapter);
        mListView.setOnItemClickListener(this);
        loadData();
    }

    private void readLocation(LocationData locationData) {
        locationData.mode = VirtualLocationManager.get().getMode(locationData.userId, locationData.packageName);
        locationData.location = VirtualLocationManager.get().getLocation(locationData.userId, locationData.packageName);
    }

    private void saveLocation(LocationData locationData) {
        if(locationData.location == null||locationData.location.isEmpty()){
            VirtualLocationManager.get().setMode(locationData.userId, locationData.packageName, 0);
        }else if(locationData.mode != 2){
            VirtualLocationManager.get().setMode(locationData.userId, locationData.packageName, 2);
        }
        VirtualLocationManager.get().setLocation(locationData.userId, locationData.packageName, locationData.location);
    }

    private void loadData() {
        ProgressDialog dialog = ProgressDialog.show(this, null, "loading");
        VUiKit.defer().when(() -> {
            List<InstalledAppInfo> infos = VirtualCore.get().getInstalledApps(0);
            List<LocationData> models = new ArrayList<>();
            for (InstalledAppInfo info : infos) {
                if (!VirtualCore.get().isPackageLaunchable(info.packageName)) {
                    continue;
                }
                int[] userIds = info.getInstalledUsers();
                for (int userId : userIds) {
                    LocationData data = new LocationData(this, info, userId);
                    readLocation(data);
                    models.add(data);
                }
            }
            return models;
        }).done((list) -> {
            dialog.dismiss();
            mAppLocationAdapter.set(list);
            mAppLocationAdapter.notifyDataSetChanged();
        }).fail((e) -> {
            dialog.dismiss();
        });
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        mSelectData = mAppLocationAdapter.getItem(position);
        Intent intent = new Intent(this, MarkerActivity.class);
        if (mSelectData.location != null) {
            intent.putExtra(EXTRA_LOCATION, mSelectData.location);
        }
        startActivityForResult(intent, REQUSET_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUSET_CODE) {
            if (resultCode == RESULT_OK) {
                VLocation location = data.getParcelableExtra(EXTRA_LOCATION);
                if (mSelectData != null) {
                    mSelectData.location = location;
                    VLog.i("kk", "set" + mSelectData);
                    saveLocation(mSelectData);
                    mSelectData = null;
                    loadData();
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

}
