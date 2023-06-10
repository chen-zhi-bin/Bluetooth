package com.program.bluetooth.bt;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import com.program.bluetooth.APP;
import com.program.bluetooth.util.Util;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.UUID;

public class BtBase {
    static final UUID SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static final int FLAG_MSG = 0;  //消息标记
    private BluetoothSocket mSocket;
    private Listener mListener;
    private DataOutputStream mOut;
    private boolean isRead;
    private boolean isSending;

    public BtBase(Listener listener) {
        mListener = listener;
    }

    /**
     * 循环读取对方数据（若没有数据，则阻塞等待）
     * @param socket
     */
    public void loopRead(BluetoothSocket socket){
        mSocket = socket;
        try {
            if (!mSocket.isConnected()) {
                mSocket.connect();
            }
            notifyUI(Listener.CONNECTED,mSocket.getRemoteDevice());
            mOut = new DataOutputStream(mSocket.getOutputStream());
            DataInputStream in = new DataInputStream(mSocket.getInputStream());
            isRead = true;
            while (isRead){     //死循环读取
                switch (in.readInt()){
                    case FLAG_MSG:  //读取短消息
                        String msg = in.readUTF();
                        notifyUI(Listener.MSG,"接收短消息："+msg);
                        break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 释放监听
     */
    public void unListener(){
        mListener = null;
    }

    /**
     * 关闭socket连接
     */
    public void close(){
        try {
            isRead = false;
            mSocket.close();
            notifyUI(Listener.DISCONNECTED,null);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isConnected(BluetoothDevice dev){
        boolean connected = mSocket != null && mSocket.isConnected();
        if (dev==null){
            return connected;
        }
        return connected && mSocket.getRemoteDevice().equals(dev);
    }

    /**
     * 发送短消息
     */
    public void sendMsg(String msg){
        if (checkSend()) {
            return;
        }
        isSending = true;
        try {
            mOut.writeInt(FLAG_MSG);
            mOut.writeUTF(msg);
            mOut.flush();
            notifyUI(Listener.MSG,"发送短消息:" + msg);
        } catch (IOException e) {
            e.printStackTrace();
            close();
        }
        isSending = false;
    }



    //=========================================通知UI===========================================================

    public boolean checkSend(){
        if (isSending){
            APP.toast("正在发送其他数据，请稍后再发送此条数据。。。",0);
            return true;
        }
        return false;
    }

    private void notifyUI(int state, Object o) {
        APP.runUi(new Runnable() {
            @Override
            public void run() {
                try {
                    if (mListener != null) {
                        mListener.socketNotify(state,o);
                    }
                }catch (Throwable e){
                    e.printStackTrace();
                }
            }
        });
    }


    public interface Listener{
        int DISCONNECTED = 0;
        int CONNECTED = 1;
        int MSG = 2;

        void socketNotify(int state, Object obj);
    }
}
