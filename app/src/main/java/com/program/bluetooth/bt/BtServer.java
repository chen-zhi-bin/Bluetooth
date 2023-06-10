package com.program.bluetooth.bt;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.net.Uri;

import com.program.bluetooth.util.Util;

import java.io.IOException;

/**
 * 服务器监听和连接线程，只连接一个设备
 */
public class BtServer extends BtBase{
    private static final String TAG = BtServer.class.getSimpleName();
    private BluetoothServerSocket mSSocket;

    public BtServer(Listener listener) {
        super(listener);
        listen();
    }

    /**
     * 监听客户端发起的连接
     */
    public void listen() {
        try {
            BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
//            mSSocket = adapter.listenUsingRfcommWithServiceRecord(TAG, SPP_UUID); //加密传输，Android强制执行配对，弹窗显示配对码
            mSSocket = adapter.listenUsingInsecureRfcommWithServiceRecord(TAG,SPP_UUID);  //明文传输（不安全），无需配对
            Util.EXECUTOR.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        BluetoothSocket socket = mSSocket.accept(); //监听连接
                        mSSocket.close();           //关闭监听，只连接一个设备
                        loopRead(socket);           //循环读取
                    } catch (IOException e) {
                        e.printStackTrace();
                        close();
                    }
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void close() {
        super.close();
        try {
            mSSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
