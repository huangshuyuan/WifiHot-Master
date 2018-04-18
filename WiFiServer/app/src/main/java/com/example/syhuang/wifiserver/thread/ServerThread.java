package com.example.syhuang.wifiserver.thread;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.example.syhuang.wifiserver.MainActivity;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * 连接线程
 * Created by syhuang on 2016/9/7.
 */
public class ServerThread extends Thread {

    private final Socket       socket;
    private       Handler      handler;
    private       InputStream  inputStream;
    private       OutputStream outputStream;

    public ServerThread(Socket socket, Handler handler){
        setName("ConnectThread");
        Log.i("ConnectThread","ConnectThread");

        this.socket = socket;
        this.handler = handler;
    }

    @Override
    public void run() {
/*        if(activeConnect){
//            socket.c
        }*/
        if(socket==null){
            return;
        }
        handler.sendEmptyMessage(MainActivity.DEVICE_CONNECTED);
        try {
            //获取数据流
            inputStream = socket.getInputStream();
            outputStream = socket.getOutputStream();

            byte[] buffer = new byte[1024];
            int bytes;
            while (true){
                //读取数据
                bytes = inputStream.read(buffer);
                if (bytes > 0) {
                    final byte[] data = new byte[bytes];
                    System.arraycopy(buffer, 0, data, 0, bytes);

                    Message message = Message.obtain();
                    message.what = MainActivity.GET_MSG;
                    Bundle bundle = new Bundle();
                    bundle.putString("MSG",new String(data));
                    message.setData(bundle);
                    handler.sendMessage(message);

                    Log.i("ConnectThread","读取到数据:"+new String(data));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 发送数据
     */
    public void sendData(String msg){
        Log.i("ConnectThread","发送数据:"+(outputStream==null));
        if(outputStream!=null){
            try {
                outputStream.write(msg.getBytes());
                Log.i("ConnectThread","发送消息："+msg);
                Message message = Message.obtain();
                message.what = MainActivity.SEND_MSG_SUCCSEE;
                Bundle bundle = new Bundle();
                bundle.putString("MSG",new String(msg));
                message.setData(bundle);
                handler.sendMessage(message);
            } catch (IOException e) {
                e.printStackTrace();
                Message message = Message.obtain();
                message.what = MainActivity.SEND_MSG_ERROR;
                Bundle bundle = new Bundle();
                bundle.putString("MSG",new String(msg));
                message.setData(bundle);
                handler.sendMessage(message);
            }
        }
    }
}
