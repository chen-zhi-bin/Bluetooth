package com.program.bluetooth.bt;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Environment;

import com.program.bluetooth.APP;
import com.program.bluetooth.util.Util;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

public class BtBase {
    static final UUID SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static final String FILE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/bluetooth/";
    private static final int FLAG_MSG = 0;  //消息标记
    private static final int FLAG_FILE = 1; //文件标记
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
                    case FLAG_FILE:
                        Util.mkdirs(FILE_PATH);
                        String fileName = in.readUTF();//文件名
                        long fileLen = in.readLong();//文件长度
                        //读取文件内容
                        long len = 0;
                        int r;
                        byte[] b = new byte[4 * 1024];
                        FileOutputStream out = new FileOutputStream(FILE_PATH + fileName);
                        notifyUI(Listener.MSG,"正在接收文件（"+fileName+"），请稍后。。。");
                        while ((r=in.read(b))!=-1){
                            out.write(b,0,r);
                            len+=r;
                            if (len >= fileLen) {
                                break;
                            }
                        }
                        notifyUI(Listener.MSG,"文件接收完成（存放在："+FILE_PATH+"）");
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

    /**
     * 发送文件
     */
    public void sendFile(String filePath){
        if (checkSend()) {
            return;
        }
        isSending = true;
        Util.EXECUTOR.execute(new Runnable() {
            @Override
            public void run() {
                try {

                    FileInputStream in = new FileInputStream(filePath);
                    File file = new File(filePath);
                    mOut.writeInt(FLAG_FILE);   //文件标记
                    mOut.writeUTF(file.getName());//文件名
                    mOut.writeLong(file.length());//文件长度
                    int r;
                    byte[] b = new byte[4 * 1024];
                    notifyUI(Listener.MSG,"正在发送文件（"+filePath+"），请稍等。。。");
                    while ((r=in.read(b))!=-1){
                        mOut.write(b,0,r);
                    }
                    mOut.flush();
                    notifyUI(Listener.MSG,"文件发送完成.");
                }catch (Throwable e){
                    close();
                }
                isSending =false;
            }
        });
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
