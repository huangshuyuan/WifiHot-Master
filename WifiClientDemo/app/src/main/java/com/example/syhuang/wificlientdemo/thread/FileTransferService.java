package com.example.syhuang.wificlientdemo.thread;

import android.app.IntentService;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Author: syhuang
 * Date:  2018/4/8
 */

public class FileTransferService extends IntentService {
    private static final int    SOCKET_TIMEOUT             = 3600;
    public static        String ACTION_SEND_FILE           = "android.intent.action.SEND_FILE";
    public static        String EXTRAS_FILE_PATH           = "extras_file_path";
    public static        String EXTRAS_GROUP_OWNER_ADDRESS = "extras_group_owner_address";
    public static        String EXTRAS_GROUP_OWNER_PORT    = "extras_group_owner_port";


    public FileTransferService(String name) {
        super(name);
    }

    public FileTransferService() {
        super("FileTransferService");

    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Context context = getApplicationContext();
        if (intent.getAction().equals(ACTION_SEND_FILE)) {
            String fileUri = intent.getExtras().getString(EXTRAS_FILE_PATH);
            String host = intent.getExtras().getString(EXTRAS_GROUP_OWNER_ADDRESS);
            Socket socket = new Socket();
            int port = intent.getExtras().getInt(EXTRAS_GROUP_OWNER_PORT);

            try {
                socket.bind(null);
                socket.connect((new InetSocketAddress(host, port)), SOCKET_TIMEOUT);
                OutputStream stream = socket.getOutputStream();
                ContentResolver cr = context.getContentResolver();
                InputStream is = null;
                try {
                    is = cr.openInputStream(Uri.parse(fileUri));
                } catch (FileNotFoundException e) {
                    Log.i("FileService", e.toString());
                }
                copyFile(is, stream);
            } catch (IOException e) {
            } finally {
                if (socket != null) {
                    if (socket.isConnected()) {
                        try {
                            socket.close();
                        } catch (IOException e) {
                            // Give up
                            e.printStackTrace();
                        }
                    }
                }
            }

        }
    }

    public static boolean copyFile(InputStream inputStream, OutputStream out) {
        byte buf[] = new byte[1024];
        int len;
        try {
            while ((len = inputStream.read(buf)) != -1) {
                out.write(buf, 0, len);

            }
            out.close();
            inputStream.close();
        } catch (IOException e) {

            return false;
        }
        return true;
    }

    //    链接：https://www.jianshu.com/p/14ec886bb624
}

