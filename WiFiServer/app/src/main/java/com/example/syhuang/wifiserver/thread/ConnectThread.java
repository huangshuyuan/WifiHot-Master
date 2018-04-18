package com.example.syhuang.wifiserver.thread;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.example.syhuang.wifiserver.MainActivity;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.text.DecimalFormat;

/**
 * 连接线程
 * Created by syhuang on 2016/9/7.
 */
public class ConnectThread extends Thread {

    private final Socket       socket;
    private       Handler      handler;
    private       InputStream  inputStream;
    private       OutputStream outputStream;
    Context context;

    public ConnectThread(Context context, Socket socket, Handler handler) {
        setName("ConnectThread");
        Log.i("ConnectThread", "ConnectThread");
        this.socket = socket;
        this.handler = handler;
        this.context = context;
    }

    @Override
    public void run() {
/*        if(activeConnect){
//            socket.c
        }*/
        if (socket == null) {
            return;
        }
        handler.sendEmptyMessage(MainActivity.DEVICE_CONNECTED);
        try {
            //获取数据流
            inputStream = socket.getInputStream();
            outputStream = socket.getOutputStream();

            byte[] buffer = new byte[1024];
            int bytes;
            while (true) {

                //读取数据
                bytes = inputStream.read(buffer);
                if (bytes > 0) {
                    final byte[] data = new byte[bytes];
                    System.arraycopy(buffer, 0, data, 0, bytes);

                    Message message = Message.obtain();
                    message.what = MainActivity.GET_MSG;
                    Bundle bundle = new Bundle();
                    bundle.putString("MSG", new String(data));
                    message.setData(bundle);
                    handler.sendMessage(message);
                }
                //                DataInputStream dis = null;
                //                FileOutputStream fos = null;
                //                try {
                //                    dis = new DataInputStream(inputStream);
                //
                //                    // 文件名和长度
                //                    String fileName = dis.readUTF();
                //                    if (!fileName.equals("")) {
                //                        long fileLength = dis.readLong();
                //                        Log.i("ConnectThread", "======== 文件接收 [File Name：" + fileName + "] " +
                //                                "[Size：" + getFormatFileSize(fileLength) + "] ========");
                //                        File directory = new File(Environment.getExternalStorageDirectory() + "/");
                //                        if (!directory.exists()) {
                //                            directory.mkdir();
                //                        } else {
                //                        }
                //                        File file = new File(directory.getAbsolutePath() + File.separatorChar + fileName);
                //                        fos = new FileOutputStream(file);
                //
                //                        // 开始接收文件
                //                        byte[] bytesA = new byte[1024];
                //                        int length = 0;
                //                        int progress = 0;
                //                        while ((length = dis.read(bytesA, 0, bytesA.length)) != -1) {
                //                            Log.i("ConnectThread", length + "...");
                //                            fos.write(bytesA, 0, length);
                //                            fos.flush();
                //                            progress += length;
                //                            Log.i("ConnectThread", "| " + (100 * progress / file.length()) + "% |");
                //                        }
                //                        Log.i("ConnectThread", "文件传输完成");
                //
                //                        Message message = Message.obtain();
                //                        message.what = MainActivity.GET_MSG;
                //                        Bundle bundle = new Bundle();
                //                        bundle.putString("MSG", new String("接收到文件：" + file.getAbsolutePath()));
                //                        message.setData(bundle);
                //                        handler.sendMessage(message);
                //                    } else {
                //                        //读取数据
                //                        bytes = inputStream.read(buffer);
                //                        if (bytes > 0) {
                //                            final byte[] data = new byte[bytes];
                //                            System.arraycopy(buffer, 0, data, 0, bytes);
                //
                //
                //                            Message message = Message.obtain();
                //                            message.what = MainActivity.GET_MSG;
                //                            Bundle bundle = new Bundle();
                //                            bundle.putString("MSG", new String(data));
                //                            message.setData(bundle);
                //                            handler.sendMessage(message);
                //
                //                            Log.i("ConnectThread", "读取到数据:" + new String(data));
                //                        }
                //                    }


            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 格式化文件大小
     *
     * @param length
     * @return
     */
    private String getFormatFileSize(long length) {
        DecimalFormat df = new DecimalFormat("#0.0");
        double size = ((double) length) / (1 << 30);
        if (size >= 1) {
            return df.format(size) + "GB";
        }
        size = ((double) length) / (1 << 20);
        if (size >= 1) {
            return df.format(size) + "MB";
        }
        size = ((double) length) / (1 << 10);
        if (size >= 1) {
            return df.format(size) + "KB";
        }
        return length + "B";
    }

    public static boolean copyFile(InputStream inputStream, OutputStream out) {
        byte buf[] = new byte[1024];
        int len;
        try {
            while ((len = inputStream.read(buf)) != -1) {
                out.write(buf, 0, len);

            }
            out.close();
            //            inputStream.close();
        } catch (IOException e) {

            return false;
        }
        return true;
    }

    /**
     * 发送数据
     */
    public void sendData(String msg) {
        Log.i("ConnectThread", "发送数据:" + (outputStream == null));
        if (outputStream != null) {
            try {
                outputStream.write(msg.getBytes());
                Log.i("ConnectThread", "发送消息：" + msg);
                Message message = Message.obtain();
                message.what = MainActivity.SEND_MSG_SUCCSEE;
                Bundle bundle = new Bundle();
                bundle.putString("MSG", new String(msg));
                message.setData(bundle);
                handler.sendMessage(message);
            } catch (IOException e) {
                e.printStackTrace();
                Message message = Message.obtain();
                message.what = MainActivity.SEND_MSG_ERROR;
                Bundle bundle = new Bundle();
                bundle.putString("MSG", new String(msg));
                message.setData(bundle);
                handler.sendMessage(message);
            }
        }
    }

}
