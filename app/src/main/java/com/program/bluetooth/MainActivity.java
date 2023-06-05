package com.program.bluetooth;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import com.program.bluetooth.bt.BtClientActivity;
import com.program.bluetooth.bt.BtServerActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //检查蓝牙开关
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter==null) {
            APP.toast("本设备没有找到蓝牙硬件或驱动",0);
            finish();
            return;
        }else {
            if (!adapter.isEnabled()) {
                //直接开启蓝牙
                adapter.enable();
            }
        }
        //检查是否支持ble蓝牙
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            APP.toast("本机不支持低功耗蓝牙",0);
            finish();
            return;
        }

        //android 6.0动态请求权限
        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.M) {
            String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE
                    , Manifest.permission.READ_EXTERNAL_STORAGE
                    , Manifest.permission.ACCESS_COARSE_LOCATION};
            for (String str : permissions) {
                if (checkSelfPermission(str) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(permissions, 111);
                    break;
                }
            }
        }
    }

    public void btClient(View view) {
        startActivity(new Intent(this, BtClientActivity.class));
    }

    public void btServer(View view) {
        startActivity(new Intent(this, BtServerActivity.class));
    }
}