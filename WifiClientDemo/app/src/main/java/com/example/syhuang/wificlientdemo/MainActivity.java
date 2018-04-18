package com.example.syhuang.wificlientdemo;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.example.syhuang.wificlientdemo.thread.ConnectThread;
import com.example.syhuang.wificlientdemo.thread.ListenerThread;
import com.nononsenseapps.filepicker.FilePickerActivity;
import com.nononsenseapps.filepicker.Utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    public static final int DEVICE_CONNECTING = 1;//有设备正在连接热点
    public static final int DEVICE_CONNECTED  = 2;//有设备连上热点
    public static final int SEND_MSG_SUCCSEE  = 3;//发送消息成功
    public static final int SEND_MSG_ERROR    = 4;//发送消息失败
    public static final int GET_MSG           = 6;//获取新消息

    private TextView      text_state;
    /**
     * 连接线程
     */
    private ConnectThread connectThread;


    /**
     * 监听线程
     */
    private ListenerThread listenerThread;

    /**
     * 热点名称
     */
    private static final String WIFI_HOTSPOT_SSID = "TEST";
    /**
     * 端口号
     */
    private static final int    PORT              = 54321;
    private WifiManager wifiManager;

    private TextView status_init;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.send).setOnClickListener(this);
        findViewById(R.id.connect).setOnClickListener(this);
        findViewById(R.id.fileButton).setOnClickListener(this);
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        //检查Wifi状态
        if (!wifiManager.isWifiEnabled())
            wifiManager.setWifiEnabled(true);
        text_state = (TextView) findViewById(R.id.status_info);
        status_init = (TextView) findViewById(R.id.status_init);


        status_init.setText("已连接到：" + wifiManager.getConnectionInfo().getSSID() +
                "\nIP:" + getIp()
                + "\n路由：" + getWifiRouteIPAddress(MainActivity.this));

        //        initBroadcastReceiver();
        //        开启连接线程
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Socket socket = new Socket(getWifiRouteIPAddress(MainActivity.this), PORT);
                    connectThread = new ConnectThread(socket, handler);
                    connectThread.start();
                } catch (IOException e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            text_state.setText("通信连接失败");
                        }
                    });

                }
            }
        }).start();


        listenerThread = new ListenerThread(PORT, handler);
        listenerThread.start();
    }

    /**
     * 获取已连接的热点路由
     *
     * @return
     */
    private String getIp() {
        //检查Wifi状态
        if (!wifiManager.isWifiEnabled())
            wifiManager.setWifiEnabled(true);
        WifiInfo wi = wifiManager.getConnectionInfo();
        //获取32位整型IP地址
        int ipAdd = wi.getIpAddress();
        //把整型地址转换成“*.*.*.*”地址
        String ip = intToIp(ipAdd);
        return ip;
    }

    /**
     * 获取路由
     *
     * @return
     */

    private String getRouterIp() {
        //检查Wifi状态
        if (!wifiManager.isWifiEnabled())
            wifiManager.setWifiEnabled(true);
        WifiInfo wi = wifiManager.getConnectionInfo();
        //获取32位整型IP地址
        int ipAdd = wi.getIpAddress();
        //把整型地址转换成“*.*.*.*”地址
        String ip = intToRouterIp(ipAdd);
        return ip;
    }

    private String intToIp(int i) {
        return (i & 0xFF) + "." +
                ((i >> 8) & 0xFF) + "." +
                ((i >> 16) & 0xFF) + "." +
                (i >> 24 & 0xFF);
    }

    private String intToRouterIp(int i) {
        return (i & 0xFF) + "." +
                ((i >> 8) & 0xFF) + "." +
                ((i >> 16) & 0xFF) + "." +
                1;
    }

    /**
     * wifi获取 已连接网络路由  路由ip地址---方法同上
     *
     * @param context
     * @return
     */
    private static String getWifiRouteIPAddress(Context context) {
        WifiManager wifi_service = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        DhcpInfo dhcpInfo = wifi_service.getDhcpInfo();
        //        WifiInfo wifiinfo = wifi_service.getConnectionInfo();
        //        System.out.println("Wifi info----->" + wifiinfo.getIpAddress());
        //        System.out.println("DHCP info gateway----->" + Formatter.formatIpAddress(dhcpInfo.gateway));
        //        System.out.println("DHCP info netmask----->" + Formatter.formatIpAddress(dhcpInfo.netmask));
        //DhcpInfo中的ipAddress是一个int型的变量，通过Formatter将其转化为字符串IP地址
        String routeIp = Formatter.formatIpAddress(dhcpInfo.gateway);
        Log.i("route ip", "wifi route ip：" + routeIp);

        return routeIp;
    }

    int CHOOSE_FILE_RESULT_CODE = 1001;

    int FILE_CODE = 1002;

    @Override
    public void onClick(View view) {
        switch (view.getId()) {

            case R.id.fileButton:
                /**
                 * 相册
                 */
                //                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                //                intent.setType("image/*");
                //                startActivityForResult(intent, CHOOSE_FILE_RESULT_CODE);
                /**文件库*/
                // This always works
                Intent i = new Intent(MainActivity.this, FilePickerActivity.class);
                // This works if you defined the intent filter
                // Intent i = new Intent(Intent.ACTION_GET_CONTENT);

                // Set these depending on your use case. These are the defaults.
                i.putExtra(FilePickerActivity.EXTRA_ALLOW_MULTIPLE, false);
                i.putExtra(FilePickerActivity.EXTRA_ALLOW_CREATE_DIR, false);
                i.putExtra(FilePickerActivity.EXTRA_MODE, FilePickerActivity.MODE_FILE);

                // Configure initial directory by specifying a String.
                // You could specify a String like "/storage/emulated/0/", but that can
                // dangerous. Always use Android's API calls to get paths to the SD-card or
                // internal memory.
                i.putExtra(FilePickerActivity.EXTRA_START_PATH, Environment.getExternalStorageDirectory().getPath());

                startActivityForResult(i, FILE_CODE);

                break;
            case R.id.send:
                if (connectThread != null) {
                    connectThread.sendData("这是来自Wifi-client热点的消息");
                } else {
                    Log.i("AAA", "connectThread == null");
                }
                break;
            case R.id.connect:
                status_init.setText("已连接到：" + wifiManager.getConnectionInfo().getSSID() +
                        "\nIP:" + getIp()
                        + "\n路由：" + getWifiRouteIPAddress(MainActivity.this));


                //                //        initBroadcastReceiver();
                //                //        开启连接线程
                //                new Thread(new Runnable() {
                //                    @Override
                //                    public void run() {
                //                        try {
                //                            Socket socket = new Socket(getRouterIp(), PORT);
                //                            connectThread = new ConnectThread(socket, handler);
                //                            connectThread.start();
                //                        } catch (IOException e) {
                //                            e.printStackTrace();
                //                            runOnUiThread(new Runnable() {
                //                                @Override
                //                                public void run() {
                //                                    text_state.setText("通信连接失败");
                //                                }
                //                            });
                //
                //                        }
                //                    }
                //                }).start();
                //                listenerThread = new ListenerThread(PORT, handler);
                //                listenerThread.start();
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        //        if (resultCode == RESULT_OK) {
        //            Uri uri = data.getData();
        //            //            Intent serviceIntent = new Intent(this, FileTransferService.class);
        //            //            serviceIntent.setAction(FileTransferService.ACTION_SEND_FILE);
        //            //            serviceIntent.putExtra(FileTransferService.EXTRAS_FILE_PATH, uri.toString());
        //            //            serviceIntent.putExtra(FileTransferService.EXTRAS_GROUP_OWNER_ADDRESS,
        //            //                    getWifiRouteIPAddress(MainActivity.this));
        //            //            serviceIntent.putExtra(FileTransferService.EXTRAS_GROUP_OWNER_PORT, PORT);
        //            //            startService(serviceIntent);
        //
        //            if (connectThread != null) {
        //                connectThread.sendData(MainActivity.this, uri);
        //            }
        //        }

        if (requestCode == FILE_CODE && resultCode == Activity.RESULT_OK) {
            // Use the provided utility method to parse the result
            List<Uri> files = Utils.getSelectedFilesFromResult(data);
            for (Uri uri : files) {
                File file = Utils.getFileForUri(uri);
                // Do something with the result...

                if (connectThread != null) {
                    connectThread.sendData(file);
                }
            }
        }


    }
    //文件传输
    //    https://blog.csdn.net/yuankundong/article/details/51489823

    /**
     * 查找当前连接状态
     */
    private void initBroadcastReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        //        intentFilter.addAction(WifiManager.RSSI_CHANGED_ACTION);

        registerReceiver(receiver, intentFilter);
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (action.equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
                Log.i("BBB", "SCAN_RESULTS_AVAILABLE_ACTION");
                // wifi已成功扫描到可用wifi。
                //                List<ScanResult> scanResults = wifiManager.getScanResults();
                //                wifiListAdapter.clear();
                //                wifiListAdapter.addAll(scanResults);
            } else if (action.equals(WifiManager.WIFI_STATE_CHANGED_ACTION)) {
                Log.i("BBB", "WifiManager.WIFI_STATE_CHANGED_ACTION");
                int wifiState = intent.getIntExtra(
                        WifiManager.EXTRA_WIFI_STATE, 0);
                switch (wifiState) {
                    case WifiManager.WIFI_STATE_ENABLED:
                        //获取到wifi开启的广播时，开始扫描
                        //                        wifiManager.startScan();
                        break;
                    case WifiManager.WIFI_STATE_DISABLED:
                        //wifi关闭发出的广播
                        break;
                }
            } else if (action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
                Log.i("BBB", "WifiManager.NETWORK_STATE_CHANGED_ACTION");
                NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                if (info.getState().equals(NetworkInfo.State.DISCONNECTED)) {
                    text_state.setText("连接已断开");
                } else if (info.getState().equals(NetworkInfo.State.CONNECTED)) {
                    WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                    final WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                    text_state.setText("已连接到网络:" + wifiInfo.getSSID()
                            + "\n" + wifiInfo.getIpAddress()
                            + "\n" + wifiInfo.getNetworkId()
                            + "\n" + wifiInfo.getMacAddress());
                    Log.i("AAA", "wifiInfo.getSSID():" + wifiInfo.getSSID() +
                            "  WIFI_HOTSPOT_SSID:" + WIFI_HOTSPOT_SSID);
                    if (wifiInfo.getSSID().equals(WIFI_HOTSPOT_SSID)) {
                        //如果当前连接到的wifi是热点,则开启连接线程


                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    ArrayList<String> connectedIP = getConnectedIP();
                                    for (String ip : connectedIP) {
                                        if (ip.contains(".")) {
                                            Log.i("AAA", "IP:" + ip);
                                            Socket socket = new Socket(ip, PORT);
                                            connectThread = new ConnectThread(socket, handler);
                                            connectThread.start();
                                        }
                                    }

                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }).start();
                    }
                } else {
                    NetworkInfo.DetailedState state = info.getDetailedState();
                    if (state == state.CONNECTING) {
                        text_state.setText("连接中...");
                    } else if (state == state.AUTHENTICATING) {
                        text_state.setText("正在验证身份信息...");
                    } else if (state == state.OBTAINING_IPADDR) {
                        text_state.setText("正在获取IP地址...");
                    } else if (state == state.FAILED) {
                        text_state.setText("连接失败");
                    }
                }

            }
           /* else if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                if (intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false)) {
                    text_state.setText("连接已断开");
                    wifiManager.removeNetwork(wcgID);
                } else {
                    WifiManager wifiManager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
                    WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                    text_state.setText("已连接到网络:" + wifiInfo.getSSID());
                }
            }*/
        }
    };

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case DEVICE_CONNECTING:
                    connectThread = new ConnectThread(listenerThread.getSocket(), handler);
                    connectThread.start();
                    break;
                case DEVICE_CONNECTED:
                    text_state.setText("设备连接成功");
                    break;
                case SEND_MSG_SUCCSEE:
                    text_state.setText("发送消息成功:" + msg.getData().getString("MSG"));
                    break;
                case SEND_MSG_ERROR:
                    text_state.setText("发送消息失败:" + msg.getData().getString("MSG"));
                    break;
                case GET_MSG:
                    text_state.setText("收到消息:" + msg.getData().getString("MSG"));
                    break;
            }
        }
    };

    /**
     * 获取连接到热点上的手机ip
     *
     * @return
     */
    private ArrayList<String> getConnectedIP() {
        ArrayList<String> connectedIP = new ArrayList<String>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(
                    "/proc/net/arp"));
            String line;
            while ((line = br.readLine()) != null) {
                String[] splitted = line.split(" +");
                if (splitted != null && splitted.length >= 4) {
                    String ip = splitted[0];
                    connectedIP.add(ip);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        //        Log.i("connectIp:", connectedIP);
        return connectedIP;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        System.exit(0);
    }
}