package com.program.bluetooth.bt;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.program.bluetooth.APP;
import com.program.bluetooth.R;
import com.program.bluetooth.util.BtReceiver;

public class BtClientActivity extends AppCompatActivity implements BtReceiver.Listener, BtDevAdapter.Listener, BtBase.Listener {
    private TextView mTips;
    private EditText mInputMsg;
    private EditText mInputFile;
    private TextView mLogs;
    private BtDevAdapter mBtDevAdapter = new BtDevAdapter(this);
    private BtReceiver mBtReceiver;
    private final BtClient mClient = new BtClient(this);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bt_client);
        initView();
    }

    private void initView() {
        RecyclerView rv = findViewById(R.id.rv_bt);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(mBtDevAdapter);
        mTips = findViewById(R.id.tv_tips);
        mInputMsg = findViewById(R.id.input_msg);
        mInputFile = findViewById(R.id.input_file);
        mLogs = findViewById(R.id.tv_log);
        mBtReceiver = new BtReceiver(this, this);       //注册蓝牙广播

        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        Log.d("BtClientActivity","start = "+adapter);
        if (adapter.isEnabled()){
            boolean b = adapter.startDiscovery();
//            Intent discoveralbeIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
//            discoveralbeIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION,12);
//            startActivity(discoveralbeIntent);
            Log.d("BtClientActivity","start = "+b);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mBtReceiver);
        mClient.unListener();
        mClient.close();
    }

    public void sendMsg(View view){
        if (mClient.isConnected(null)) {
            String msg = mInputMsg.getText().toString();
            if (TextUtils.isEmpty(msg)) {
                APP.toast("消息不能为空",0);
            }else {
                mClient.sendMsg(msg);
            }
        }else {
            APP.toast("没有连接",0);
        }
    }

    @Override
    public void foundDev(BluetoothDevice dev) {
        mBtDevAdapter.add(dev);
    }

    // 重新扫描
    public void reScan(View view) {
        mBtDevAdapter.reScan();
    }

    @Override
    public void onItemClick(BluetoothDevice dev) {
        if (mClient.isConnected(dev)) {
            APP.toast("已经连接了："+dev.getName(),0);
            return;
        }
        mClient.connect(dev);
        APP.toast("正在连接。。。",0);
        mTips.setText("正在连接。。。");
    }

    @Override
    public void socketNotify(int state, Object obj) {
        if (isDestroyed()){
            return;
        }
        String msg = null;
        switch (state){
            case BtBase.Listener.CONNECTED:
                BluetoothDevice dev = (BluetoothDevice) obj;
                msg = String.format("与%s(%s)连接成功", dev.getName(), dev.getAddress());
                mTips.setText(msg);
                break;
            case BtBase.Listener.DISCONNECTED:
                msg="连接断开";
                mTips.setText(msg);
                break;
            case BtBase.Listener.MSG:
                msg = String.format("\n%s", obj);
                mLogs.append(msg);
                break;
        }
        APP.toast(msg,0);
    }
}