package com.program.bluetooth.bt;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import com.program.bluetooth.R;
import com.program.bluetooth.util.Util;

import java.io.IOException;

/**
 * 客户机，与服务器建立长连接
 */
public class BtClient extends BtBase{
    public BtClient(Listener listener) {
        super(listener);
    }

    /**
     * 与远程设备建立长连接
     * @param dev 远程设备
     */
    public void connect(BluetoothDevice dev){
        close();
//        BluetoothSocket socket = dev.createRfcommSocketToServiceRecord(SPP_UUID); //加密传输，Android系统强制配对，弹窗显示配对码
        try {
            BluetoothSocket socket = dev.createInsecureRfcommSocketToServiceRecord(SPP_UUID);//明文传输（不安全），无需配对
            //开启子线程
            Util.EXECUTOR.execute(new Runnable() {
                @Override
                public void run() {
                    loopRead(socket);   //循环读取
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
            close();
        }

    }
}
