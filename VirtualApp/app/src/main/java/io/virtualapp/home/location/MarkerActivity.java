package io.virtualapp.home.location;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.lody.virtual.helper.utils.VLog;
import com.tencent.lbssearch.TencentSearch;
import com.tencent.lbssearch.httpresponse.BaseObject;
import com.tencent.lbssearch.httpresponse.HttpResponseListener;
import com.tencent.lbssearch.object.Location;
import com.tencent.lbssearch.object.param.Geo2AddressParam;
import com.tencent.lbssearch.object.result.Geo2AddressResultObject;
import com.tencent.map.geolocation.TencentLocation;
import com.tencent.map.geolocation.TencentLocationListener;
import com.tencent.map.geolocation.TencentLocationManager;
import com.tencent.map.geolocation.TencentLocationRequest;
import com.tencent.mapsdk.raster.model.BitmapDescriptorFactory;
import com.tencent.mapsdk.raster.model.CameraPosition;
import com.tencent.mapsdk.raster.model.LatLng;
import com.tencent.mapsdk.raster.model.MarkerOptions;
import com.tencent.tencentmap.mapsdk.map.CameraUpdateFactory;
import com.tencent.tencentmap.mapsdk.map.MapView;
import com.tencent.tencentmap.mapsdk.map.TencentMap;

import io.virtualapp.abs.ui.VActivity;
import io.virtualapp.R;

/**
 * AMapV2地图中简单介绍一些Marker的用法.
 */
public class MarkerActivity extends VActivity implements TencentMap.OnMapClickListener, TencentLocationListener {
    private TencentMap mMap;
    private MapView mapView;
    private LatLng mLatLng = new LatLng(39.9182645956, 116.3970032689);
    //    private OnLocationChangedListener mListener;
//    private AMapLocationClient mlocationClient;
//    private AMapLocationClientOption mLocationOption;
    private TextView pathText;
    //    private Thread mThread;
    private TencentSearch geocoderSearch;
    private String mAddress;
    private boolean isNoPoint = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_marker);
        setResult(Activity.RESULT_CANCELED);
        Toolbar toolbar = bind(R.id.task_top_toolbar);
        setSupportActionBar(toolbar);
        //地址显示，暂时不用
        pathText = bind(R.id.address);
        pathText.setVisibility(View.VISIBLE);
        enableBackHome();
        mapView = (MapView) findViewById(R.id.map);
        mapView.onCreate(savedInstanceState); // 此方法必须重写
        mMap = mapView.getMap();
        mMap.setOnMapClickListener(this);
        geocoderSearch = new TencentSearch(this);
        setUpMap();
        //从intent里面传过来
        Intent data = getIntent();
        if (data != null) {
            double lat = data.getDoubleExtra(MarkerActivity.EXTRA_LATITUDE, 0);
            double lon = data.getDoubleExtra(MarkerActivity.EXTRA_LONGITUDE, 0);
            String address = data.getStringExtra(MarkerActivity.EXTRA_ADDTESS);
            if (lat != 0 && lon != 0) {
                mLatLng = new LatLng(lat, lon);
                isNoPoint = false;
            }
            if (!TextUtils.isEmpty(address)) {
                pathText.setText(address);
            } else {
                pathText.setText("Unknown");
            }
        }

        if (isNoPoint) {
            startLocation();
        } else {
            onMapClick(mLatLng);
        }
    }

    @SuppressWarnings("unchecked")
    protected <T extends View> T bind(int id) {
        return (T) findViewById(id);
    }

    public void enableBackHome() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    /**
     * 设置一些amap的属性
     */
    private void setUpMap() {
        // 自定义系统定位小蓝点
//        MyLocationStyle myLocationStyle = new MyLocationStyle();
//        myLocationStyle.myLocationIcon(BitmapDescriptorFactory
//                .fromResource(R.drawable.location_marker));// 设置小蓝点的图标
//        myLocationStyle.strokeColor(Color.BLACK);// 设置圆形的边框颜色
//        myLocationStyle.radiusFillColor(Color.argb(100, 0, 0, 180));// 设置圆形的填充颜色
//        // myLocationStyle.anchor(int,int)//设置小蓝点的锚点
//        myLocationStyle.strokeWidth(1.0f);// 设置圆形的边框粗细
//        mMap.setMyLocationStyle(myLocationStyle);
//        mMap.setLocationSource(this);// 设置定位监听
//        mMap.getUiSettings().setMyLocationButtonEnabled(true);// 设置默认定位按钮是否显示
//        mMap.setMyLocationEnabled(true);// 设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false
//        // mMap.setMyLocationType()
    }

    private void startLocation() {
        Toast.makeText(this, "start location", Toast.LENGTH_SHORT).show();
        TencentLocationRequest request = TencentLocationRequest.create()
                .setRequestLevel(TencentLocationRequest.REQUEST_LEVEL_GEO)
                .setAllowGPS(true);
        int error = TencentLocationManager.getInstance(this)
                .requestLocationUpdates(request, this);
        if(error != 0) {
            VLog.w("TMap", "startLocation:error=" + error);
        }
    }

    @Override
    public void onLocationChanged(TencentLocation location, int error, String msg) {
        if (location != null) {
            TencentLocationManager.getInstance(this).removeUpdates(this);
            onMapClick(new LatLng(location.getLatitude(), location.getLongitude()));
        } else {
            String errText = "定位失败," + error + ": " + msg;
            VLog.e("TMap", errText);
        }
    }

    @Override
    public void onStatusUpdate(String s, int i, String s1) {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.marktet_map, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.action_clear:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Question");
                builder.setMessage("Clear virtual location");
                builder.setNegativeButton(android.R.string.ok, (d, s) -> {
                    if (mMap != null) {
                        mMap.clearAllOverlays();
                    }
                    setResult(0, 0, null);
                    finish();
                    d.dismiss();
                });
                builder.setNeutralButton(android.R.string.cancel, (d, s) -> {
                    d.dismiss();
                });
                builder.show();
                break;
            case R.id.action_ok:
                if (mLatLng != null) {
                    setResult(mLatLng.getLatitude(), mLatLng.getLongitude(), mAddress);
                    finish();
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onMapClick(LatLng latLng) {
        mLatLng = latLng;
        MarkerOptions markerOption = new MarkerOptions()
                .icon(BitmapDescriptorFactory.defaultMarker())
                .position(mLatLng)
                .draggable(true);
        mMap.clearAllOverlays();
        mMap.addMarker(markerOption);
        int level = Math.min(mMap.getZoomLevel(), mMap.getMaxZoomLevel()/3*2);
        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition(latLng, level)));
        //查询地理位置
//        pathText.setText(R.string.find_location_address);
        ProgressDialog dialog = ProgressDialog.show(this, null, "get address of location");
        Geo2AddressParam param = new Geo2AddressParam()
                .location(new Location()
                        .lat((float) latLng.getLatitude())
                        .lng((float) latLng.getLongitude()));
//            param.get_poi(true);
        geocoderSearch.geo2address(param, new HttpResponseListener() {
            @Override
            public void onSuccess(int i, BaseObject object) {
                Geo2AddressResultObject oj = (Geo2AddressResultObject) object;
                if (oj.result != null) {
                    pathText.setText(oj.result.address);
                    mAddress = oj.result.address;
                }
                dialog.dismiss();
            }

            @Override
            public void onFailure(int i, String s, Throwable throwable) {
                dialog.dismiss();
                pathText.setText("error:" + s);
            }
        });
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }


    private void setResult(double lat, double lon, String address) {
        Intent intent = new Intent();
        intent.putExtra(EXTRA_LATITUDE, lat);
        intent.putExtra(EXTRA_LONGITUDE, lon);
        intent.putExtra(EXTRA_ADDTESS, address);
        setResult(Activity.RESULT_OK, intent);
    }

    public static final String EXTRA_LATITUDE = "amap.latitude";
    public static final String EXTRA_LONGITUDE = "amap.longitude";
    public static final String EXTRA_ADDTESS = "amap.address";
}
