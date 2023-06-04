package com.program.bluetooth;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.os.Bundle;

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
            
        }
    }
}