package com.example.xpengfei.mybluetoothtest41.task;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;


import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.example.xpengfei.mybluetoothtest41.R;
import com.example.xpengfei.mybluetoothtest41.sound.SoundEffect;
import com.example.xpengfei.mybluetoothtest41.view.ChatListViewAdapter;

/**
 * 任务处理服务
 * Created by xpengfei on 2018/3/29.
 */
public class TaskService extends Service {
    public static final int BT_STAT_WAIT = 0;        //等待连接
    public static final int BT_STAT_CONN = 1;        //连接
    public static final int BT_STAT_ONLINE = 2;        //在线
    public static final int BT_STAT_UNKNOWN = 3;    //未知
    public static boolean isOnline = false;        //标注连接
    public static boolean reConnect = false ;      //标注是否重新建立了连接
    private final String TAG = "TaskService";
    private TaskThread mThread;                //任务线程
    private List<String> orderMsgList;
    private BluetoothAdapter mBluetoothAdapter;        //本地蓝牙适配器
    private AcceptThread mAcceptThread;            //监听线程
    private ConnectThread mConnectThread;        //连接线程

    private boolean isServerMode = true;

    private static Handler mActivityHandler;

    // 任务队列
    private static ArrayList<Task> mTaskList = new ArrayList<Task>();

    @Override
    public void onCreate() {
        super.onCreate();
        orderMsgList= Arrays.asList(getResources().getStringArray(R.array.choiceOrder));
        //获取本地蓝牙适配器
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "Your device is not support Bluetooth!");
            return;
        }
        mThread = new TaskThread();
        mThread.start();
    }

    //在线状态、连接状态服务handler
    private Handler mServiceHandler = new Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case Task.TASK_GET_REMOTE_STATE:
                    android.os.Message activityMsg = mActivityHandler
                            .obtainMessage();
                    activityMsg.what = msg.what;
                    if (mAcceptThread != null && mAcceptThread.isAlive()) {
                        //设备打开软件后默认为 服务器  处于监听连接请求的状态
                        activityMsg.obj = "等待连接...";
                        activityMsg.arg1 = BT_STAT_WAIT;        //等待连接
                    } else if (mCommThread != null && mCommThread.isAlive()) {
                        //	已经建立连接
                        activityMsg.obj = mCommThread.getRemoteName() + "[在线]";
                        activityMsg.arg1 = BT_STAT_ONLINE;
                    } else if (mConnectThread != null && mConnectThread.isAlive()) {
                        //成功建立连接后,播放音乐提醒---QQ消息音乐
                        SoundEffect.getInstance(TaskService.this).play(3);
                        activityMsg.obj = "正在连接："
                                + mConnectThread.getDevice().getName();
                        activityMsg.arg1 = BT_STAT_CONN;
                    } else {
                        activityMsg.obj = "未知状态";
                        activityMsg.arg1 = BT_STAT_UNKNOWN;
                        //播放对方下线消息提醒
                        SoundEffect.getInstance(TaskService.this).play(2);
                        // 重新等待连接
                        mAcceptThread = new AcceptThread();
                        mAcceptThread.start();
                        isServerMode = true;            //处于服务器状态,,监听连接
                        isOnline = false;               //消息发送失败的标识
                    }
                    mActivityHandler.sendMessage(activityMsg);
                    break;
                default:
                    break;
            }
            super.handleMessage(msg);
        }
    };

    //Context是上下文、handler用于处理多线程之间的通信
    public static void start(Context c, Handler handler) {
        mActivityHandler = handler;
        Intent intent = new Intent(c, TaskService.class);
        c.startService(intent);
    }

    public static void stop(Context c) {
        Intent intent = new Intent(c, TaskService.class);
        c.stopService(intent);
    }


    public static void newTask(Task target) {
        synchronized (mTaskList) {
            mTaskList.add(target);
        }
    }

    private class TaskThread extends Thread {
        private boolean isRun = true;       //正在执行
        private int mCount = 0;

        public void cancel() {
            isRun = false;
        }

        @Override
        public void run() {
            Task task;
            while (isRun) {
                // 有任务
                if (mTaskList.size() > 0) {
                    synchronized (mTaskList) {
                        // 获得任务
                        task = mTaskList.get(0);
                        doTask(task);
                    }
                } else {
                    try {
                        //线程等待0.2秒
                        Thread.sleep(200);
                        mCount++;
                    } catch (InterruptedException e) {
                    }
                    // 每过2秒钟进行一次状态检查
                    if (mCount >= 10) {
                        mCount = 0;
                        // 检查远程设备状态
                        android.os.Message handlerMsg = mServiceHandler
                                .obtainMessage();
                        handlerMsg.what = Task.TASK_GET_REMOTE_STATE;
                        mServiceHandler.sendMessage(handlerMsg);
                    }
                }
            }
        }

    }

    private void doTask(Task task) {
        //根据任务的类型，开始分配不同的执行过程
        switch (task.getTaskID()) {
            case Task.TASK_START_ACCEPT:    //开启监听
                mAcceptThread = new AcceptThread();
                mAcceptThread.start();
                isServerMode = true;
                break;
            case Task.TASK_START_CONN_THREAD:   //建立连接
                if (task.mParams == null || task.mParams.length == 0) {
                    break;
                }
                BluetoothDevice remote = (BluetoothDevice) task.mParams[0];
                mConnectThread = new ConnectThread(remote);
                mConnectThread.start();
                isServerMode = false;
                Log.d("状态","成功建立连接......");
                break;
            case Task.TASK_SEND_MSG:    //发送消息给对方的任务
                boolean sucess = false;
                if (mCommThread == null || !mCommThread.isAlive()
                        || task.mParams == null || task.mParams.length == 0) {
                    Log.e(TAG, "mCommThread or task.mParams null");
                } else {
                    byte[] msg = null;
                    msg = ((String)task.mParams[0]).getBytes();
                    try {
                        Log.d("TASK_SEND_MSG",new String(msg,"UTF-8"));
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    sucess = mCommThread.write(msg);
                }
                if (!sucess) {
                    android.os.Message returnMsg = mActivityHandler.obtainMessage();
                    returnMsg.what = Task.TASK_SEND_MSG;
                    String msg = ((String)task.mParams[0]).trim();
                    if (orderMsgList.contains(msg)){
                        returnMsg.obj = "通信连接异常，指令操作失败！";
                    }else {
                        returnMsg.obj = "消息发送失败";
                    }
                    mActivityHandler.sendMessage(returnMsg);
                }else{      //消息发送成功,可将数据存到数据库中

                }
                break;
        }
        // 移除任务
        mTaskList.remove(task);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mThread.cancel();
    }

    //唯一标识、UUID类似端口，用于与其它蓝牙软件作区分
    private final String UUID_STR = "00001101-0000-1000-8000-00805F9B34FB";

    /**
     * 等待客户端连接线程
     */
    private class AcceptThread extends Thread {
        private final BluetoothServerSocket mmServerSocket;
        private boolean isCancel = false;
        public AcceptThread() {
            Log.d(TAG, "AcceptThread");
            BluetoothServerSocket tmp = null;
            try {
                tmp = mBluetoothAdapter.listenUsingRfcommWithServiceRecord(
                        "MT_Chat_Room", UUID.fromString(UUID_STR));
            } catch (IOException e) {
            }
            mmServerSocket = tmp;
        }

        public void run() {
            BluetoothSocket socket = null;
            while (true) {
                try {
                    // 阻塞等待、其中accept方法返回的是BluetoothSocket对象
                    //根据socket对象中的输入输出流进行相关数据的通信
                    socket = mmServerSocket.accept();
                } catch (IOException e) {
                    if (!isCancel) {
                        try {
                            mmServerSocket.close();
                        } catch (IOException e1) {
                        }
                        //没有收到请求，一直处于监听状态
                        mAcceptThread = new AcceptThread();
                        mAcceptThread.start();
                        isServerMode = true;
                    }
                    break;
                }
                if (socket != null) {
                    manageConnectedSocket(socket);
                    try {
                        mmServerSocket.close();
                    } catch (IOException e) {
                    }
                    mAcceptThread = null;
                    break;
                }
            }
        }

        public void cancel() {
            try {
                //取消监听
                Log.d(TAG, "AcceptThread canceled");
                isCancel = true;
                isServerMode = false;
                mmServerSocket.close();
                mAcceptThread = null;
                if (mCommThread != null && mCommThread.isAlive()) {
                    mCommThread.cancel();
                }
            } catch (IOException e) {
            }
        }
    }

    /**
     * 作为客户端连接指定的蓝牙设备线程
     */
    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;     //远程设备对象
        public ConnectThread(BluetoothDevice device) {
            Log.d(TAG, "ConnectThread");
            //正在建立连接，取消监听线程
            if (mAcceptThread != null && mAcceptThread.isAlive()) {
                mAcceptThread.cancel();
            }
            //-------------这里应该改为保持当前连接
            if (mCommThread != null && mCommThread.isAlive()) {
                mCommThread.cancel();
            }
            // Use a temporary object that is later assigned to mmSocket,
            // because mmSocket is final
            BluetoothSocket tmp = null;
            mmDevice = device;
            try {
                tmp = device.createRfcommSocketToServiceRecord(UUID
                        .fromString(UUID_STR));
            } catch (IOException e) {
                Log.d(TAG, "createRfcommSocketToServiceRecord error!");
            }
            mmSocket = tmp;
        }

        public BluetoothDevice getDevice() {
            return mmDevice;
        }

        public void run() {
            // 取消搜索,否则会延缓连接的建立
            mBluetoothAdapter.cancelDiscovery();
            try {
                // 通过socket与device建立连接,直至连接成功或者抛出异常
                mmSocket.connect();
                isOnline = true;
            } catch (IOException connectException) {
                //连接失败,关闭socket并退出
                Log.e(TAG, "Connect server failed");
                try {
                    mmSocket.close();
                } catch (IOException closeException) {
                }
                //将程序回退到 监听连接请求的状态,开启监听线程
                mAcceptThread = new AcceptThread();
                mAcceptThread.start();
                isServerMode = true;
                isOnline = false;
                reConnect = true ;
                return;
            }
            //连接成功后管理连接socket
            manageConnectedSocket(mmSocket);
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
            }
            mConnectThread = null;
        }
    }
    private ConnectedThread mCommThread;

    //管理连接的socket
    private void manageConnectedSocket(BluetoothSocket socket) {
        // 启动子线程来维持连接
        mCommThread = new ConnectedThread(socket);
        mCommThread.start();
    }

    //作为服务器端的线程
    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;            //用于通信的socket
        private final InputStream mmInStream;            //输入流
        private final OutputStream mmOutStream;            //输出流
        private BufferedOutputStream mmBos;                //输出字节流
        private byte[] buffer;

        public ConnectedThread(BluetoothSocket socket) {
            Log.d(TAG, "ConnectedThread");
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
            }
            mmInStream = tmpIn;
            mmOutStream = tmpOut;
            mmBos = new BufferedOutputStream(mmOutStream);
        }

        public OutputStream getOutputStream() {
            return mmOutStream;
        }
        //用于Task任务中判断是否发送成功!
        public boolean write(byte[] msg) {
            if (msg == null)
                return false;
            try {
                //用于将字节输入流输出
                mmBos.write(msg);
                mmBos.flush();
                System.out.println("Task任务中使用的方法--写入的数据用于发送:" + new String(msg,"UTF-8"));
            } catch (IOException e) {
                return false;
            }
            return true;
        }

        //获取远程设备的名称
        public String getRemoteName() {
            return mmSocket.getRemoteDevice().getName();
        }

        //取消连接
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
            }
            mCommThread = null;
        }

        public void run() {
            try {
                //这里标注已经上线的标识,用于获取聊天记录
                isOnline = true;                //表示目前已经建立好了连接
                Log.d("run()方法中的isOnline？", String.valueOf(isOnline));
                write((mBluetoothAdapter.getName()
                        + "已经上线\n").getBytes("UTF-8"));
//                write("测试读取数据库聊天记录信息：".getBytes("UTF-8"));
                Log.d("上线信息：",mBluetoothAdapter.getName());
            } catch (UnsupportedEncodingException e2) {
            }
            int size = 0;
            android.os.Message handlerMsg;
            buffer = new byte[1024];
            BufferedInputStream bis = new BufferedInputStream(mmInStream);
            // BufferedReader br = new BufferedReader(new
            // InputStreamReader(mmInStream));
            HashMap<String, Object> data;
            while (true) {          //这里是接收到的数据显示到聊天页面
                try {
                    size = mmInStream.read(buffer);
                    //解压文字信息时有误
                    String msg = new String(buffer,0,size);
                    if (mActivityHandler == null) {
                        return;
                    }
                   String remoteDevName = mmSocket.getRemoteDevice().getName();
                    //文字消息
                    data = new HashMap<String, Object>();
                    System.out.println("Read data.");
                    data.put(ChatListViewAdapter.KEY_ROLE,
                            ChatListViewAdapter.ROLE_TARGET);
                    data.put(ChatListViewAdapter.KEY_NAME,
                            remoteDevName);
                    data.put(ChatListViewAdapter.KEY_TEXT, msg);
                    Log.d("接收到的内容：",msg);
                    // 通过Activity更新到UI上
                    handlerMsg = mActivityHandler.obtainMessage();
                    handlerMsg.what = Task.TASK_RECV_MSG;
                    handlerMsg.obj = data;
                    mActivityHandler.sendMessage(handlerMsg);

                } catch (IOException e) {
                    try {
                        mmSocket.close();
                    } catch (IOException e1) {
                    }
                    mCommThread = null;
                    if (isServerMode) {
                        // 检查远程设备状态
                        handlerMsg = mServiceHandler.obtainMessage();
                        handlerMsg.what = Task.TASK_GET_REMOTE_STATE;
                        mServiceHandler.sendMessage(handlerMsg);
                        SoundEffect.getInstance(TaskService.this).play(2);
                        mAcceptThread = new AcceptThread();
                        mAcceptThread.start();
                        isOnline = false;       //表示当前连接断开
                        reConnect = true ;
                        Log.d("isOnline？", String.valueOf(isOnline));
                    }
                    break;
                }
            }
        }
    }
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public boolean getIsOnline() {
        return isOnline;
    }

    public void setIsOnline(boolean online) {
        isOnline = online;
    }
}
