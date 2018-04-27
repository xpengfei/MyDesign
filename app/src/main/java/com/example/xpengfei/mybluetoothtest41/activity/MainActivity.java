package com.example.xpengfei.mybluetoothtest41.activity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.example.xpengfei.mybluetoothtest41.R;
import com.example.xpengfei.mybluetoothtest41.database.SQLHelper;
import com.example.xpengfei.mybluetoothtest41.sound.SoundEffect;
import com.example.xpengfei.mybluetoothtest41.task.Task;
import com.example.xpengfei.mybluetoothtest41.task.TaskService;
import com.example.xpengfei.mybluetoothtest41.utils.MediaPlayerHelper;
import com.example.xpengfei.mybluetoothtest41.utils.Utils;
import com.example.xpengfei.mybluetoothtest41.utils.WifiAutoConnectManager;
import com.example.xpengfei.mybluetoothtest41.view.ChatListViewAdapter;
import com.example.xpengfei.mybluetoothtest41.view.DrawerHScrollView;

import static com.example.xpengfei.mybluetoothtest41.database.SQLHelper.COLUMN_CONTENT;
import static com.example.xpengfei.mybluetoothtest41.database.SQLHelper.COLUMN_Date;
import static com.example.xpengfei.mybluetoothtest41.database.SQLHelper.COLUMN_ID;
import static com.example.xpengfei.mybluetoothtest41.database.SQLHelper.COLUMN_NAME;
import static com.example.xpengfei.mybluetoothtest41.database.SQLHelper.COLUMN_Role;
import static com.example.xpengfei.mybluetoothtest41.database.SQLHelper.COLUMN_SHOW;
import static com.example.xpengfei.mybluetoothtest41.database.SQLHelper.DB_NAME;
import static com.example.xpengfei.mybluetoothtest41.database.SQLHelper.TABLE_NAME;

/**
 * Created by xpengfei on 2018/3/29.
 */
public class MainActivity extends Activity implements View.OnClickListener {
    private final String TAG = "ChatActivity";
    public static int sAliveCount = 0;            //未读取的消息计数器
    //	public static final String EXTRA_MESSAGER = "net.flyget.bluetoothchat.BUNDLE";
    // 蓝牙状态变量
    private static int sBTState = -1;        //蓝牙状态变化
    private final int REQUES_BT_ENABLE_CODE = 123;        //打开蓝牙的请求码
    private final int REQUES_SELECT_BT_CODE = 222;        //选择蓝牙的请求码
    private String remoteDeviceName = null;            //所连接的设备的名称
    private String myDeviceName = null;                //本机设备蓝牙名称
    private ListView mList;            //聊天内容显示List
    private EditText mInput;        //文本输入框
    private Button mSendBtn;        //发送按钮
    private ImageView mEmoButton;    //表情按钮
    private GridView mGridView;        //GridView视图
    private boolean isUpdate = false;    //是否更新
    private BluetoothDevice mRemoteDevice;        //蓝牙设备
    private SQLHelper sqlHelper;
    private int flag = 0;                        //是否断开连接,,,0为断开,1为连接
    private Boolean isHistory = false;        //判断数据信息是否为历史数据
    private LinearLayout mRootLayout, mChatLayout;        //线性布局
    private View mEmoView;                    //表情视图
    private boolean isShowEmo = false;        //是否显示表情
    private int mScrollHeight;                //滚动窗口的高度
    private DrawerHScrollView mScrollView;
    private ChatListViewAdapter mAdapter2;        //聊天列表适配器类对象
    private ArrayList<HashMap<String, Object>> mChatContent2 = new ArrayList<HashMap<String, Object>>();//聊天内容的List
    private BluetoothAdapter mBluetoothAdapter;        //蓝牙适配器
    private ArrayList<HashMap<String, Object>> mEmoList = new ArrayList<HashMap<String, Object>>();//表情包LIst
    private List<HashMap<String, Object>> chatMapList = new ArrayList<>();        //存储聊天记录
    private Boolean isAlarming = false;         // 手机是否处于响铃状态的标识
    private List<String> orderMsgList = new ArrayList();
    private String batteryInfo = null;         //电池信息

    private ConnectivityManager connectivityManager;        //网络连接管理器
    private NetworkInfo networkInfo;            //网络连接状态对象
    private String reNetInfo = null;                        //要返回的当前网络状态
    private WifiManager mWifiManager;                // 定义WifiManager（WiFi管理类）对象
    private WifiInfo mWifiInfo;                      //  当前连接的WiFi对象
    WifiAutoConnectManager wifiAutoConnectManager;
    private String reWifiInfo = null;
    private String closeWifiResult = null;
    private String openWifiResult = null;
    private String curWifiInfo = null;
    private final String conFWifi = "config_Wifi";
    private Boolean isConfigWifi = false;
    private List connectedDevice;       //存放打开软件后建立过连接的Device蓝牙名称

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        initListener();
        //将指令加到orderMsgList中
        orderMsgList = Arrays.asList(getResources().getStringArray(R.array.choiceOrder));
        //创建数据库对象
        sqlHelper = new SQLHelper(this, DB_NAME, null, 2);
        // 获得蓝牙管理器
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "Your device is not support Bluetooth!");
            Toast.makeText(this, "该设备没有蓝牙设备", Toast.LENGTH_LONG).show();
            return;
        }
        mRootLayout = (LinearLayout) findViewById(R.id.root);
        mChatLayout = (LinearLayout) findViewById(R.id.topPanel);
        mList = (ListView) findViewById(R.id.listView1);
        //初始化ChatListViewAdapter-----mChatContent2-是聊天内容的List列表
        mAdapter2 = new ChatListViewAdapter(this, mChatContent2);
        //聊天内容显示列表
        mList.setAdapter(mAdapter2);
        // 初始化表情
        mEmoView = initEmoView();
        //输入框
        mInput = (EditText) findViewById(R.id.inputEdit);
        //输入框点击事件
        mInput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 点击输入框后，隐藏表情，显示输入法
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(mInput, 0);
                showEmoPanel(false);
            }
        });
        //发送按钮
        mSendBtn = (Button) findViewById(R.id.sendBtn);
        //表情按钮
        mEmoButton = (ImageView) findViewById(R.id.emotionBtn);
        //发送按钮、表情按钮点击事件
        mSendBtn.setOnClickListener(this);
        mEmoButton.setOnClickListener(this);
        // 取得WifiManager对象
        mWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        wifiAutoConnectManager = new WifiAutoConnectManager(mWifiManager);
        connectedDevice = new ArrayList();
        //---------------------------------------------------------------------
        // 打开蓝牙设备
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            //返回的请求打开蓝牙的返回码
            startActivityForResult(enableBtIntent, REQUES_BT_ENABLE_CODE);
        } else {
            // 默认设备作为服务端
            startServiceAsServer();
        }
        //---------------------------------------------------------------------
    }

    //搜索、指令、配网、删除、关于等按钮的点击监听事件
    private void initListener() {
        Button btnSearch = (Button) findViewById(R.id.btnSelect);
        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(MainActivity.this, SelectDevice.class), REQUES_SELECT_BT_CODE);
            }
        });
        Button sendOrderbtn = (Button) findViewById(R.id.btnSetName);
        sendOrderbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendOrder();
            }
        });
        Button btnAbout = (Button) findViewById(R.id.btnAbout);
        btnAbout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, AboutActivity.class));
            }
        });
        Button delBtn = findViewById(R.id.deleteBtn);
        delBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("flag值：---------", flag + "");
                if (flag != 0) {            //flag==1 标识当前已建立连接
                    deleteChatHistory();
                    Toast.makeText(MainActivity.this, "聊天记录已删除！", Toast.LENGTH_SHORT).show();
                } else
                    Toast.makeText(MainActivity.this, "尚未建立通信连接！", Toast.LENGTH_SHORT).show();
            }
        });
        Button confWifi = findViewById(R.id.configWifi);
        confWifi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 创建对话框构建器
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                // 获取布局
                View view2 = View.inflate(MainActivity.this, R.layout.config_wifi, null);
                // 获取布局中的控件
                final EditText username = (EditText) view2.findViewById(R.id.username);
                final EditText password = (EditText) view2.findViewById(R.id.password);
                final Button button = (Button) view2.findViewById(R.id.btn_login);
                final Button cancelBtn = view2.findViewById(R.id.btn_cancellogin);
                builder.setTitle("配网").setIcon(R.drawable.ic_launcher)
                        .setView(view2);
                final AlertDialog alertDialog = builder.create();
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String uname = username.getText().toString().trim();
                        String psd = password.getText().toString().trim();
                        String wifiInfo = conFWifi + "_" + uname + "_" + psd;        //将wifi信息拼接进行发送
                        TaskService.newTask(new Task(mHandler, Task.TASK_SEND_MSG, new Object[]{wifiInfo}));
                        //判断对方是否在线---在线则发送,否则发送失败
                        if (TaskService.isOnline)
                            showOwnMessage(wifiInfo);
                        else
                            flag = 0;            //断开连接,将flag置为0
                        isConfigWifi = true;
                        alertDialog.dismiss();// 对话框消失
                    }
                });
                cancelBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        alertDialog.dismiss();// 对话框消失
                    }
                });
                alertDialog.show();
            }
        });
    }

    //初始化表情视图
    private View initEmoView() {
        if (mEmoView == null) {
            LayoutInflater inflater = getLayoutInflater();
            mEmoView = inflater.inflate(R.layout.emo_layout, null);

            mScrollView = (DrawerHScrollView) mEmoView.findViewById(R.id.scrollView);
            mGridView = (GridView) mEmoView.findViewById(R.id.gridView);
            mGridView.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view,
                                        int position, long id) {
                    // 在android中要显示图片信息，必须使用Bitmap位图的对象来装载
                    Bitmap bitmap = BitmapFactory.decodeResource(getResources(), (Integer) mEmoList.get(position).get("img"));
                    ImageSpan imageSpan = new ImageSpan(MainActivity.this, bitmap);
                    SpannableString spannableString = new SpannableString((String) mEmoList.get(position).get("text"));//face就是图片的前缀名
                    spannableString.setSpan(imageSpan, 0, 8, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    mInput.append(spannableString);
                    System.out.println("mInput:" + mInput.getText());
                }
            });

            mScrollHeight = setScrollGridView(mScrollView, mGridView, 3);
            System.out.println("mScrollHeight:" + mScrollHeight);
        }
        return mEmoView;
    }

    //构造发送表情的适配器
    private SimpleAdapter getEmoAdapter() {
        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put("img", R.drawable.emo001);
        map.put("text", "<emo001>");
        mEmoList.add(map);
        map = new HashMap<String, Object>();
        map.put("img", R.drawable.emo002);
        map.put("text", "<emo002>");
        mEmoList.add(map);
        map = new HashMap<String, Object>();
        map.put("img", R.drawable.emo003);
        map.put("text", "<emo003>");
        mEmoList.add(map);
        map = new HashMap<String, Object>();
        map.put("img", R.drawable.emo004);
        map.put("text", "<emo004>");
        mEmoList.add(map);
        map = new HashMap<String, Object>();
        map.put("img", R.drawable.emo005);
        map.put("text", "<emo005>");
        mEmoList.add(map);
        map = new HashMap<String, Object>();
        map.put("img", R.drawable.emo006);
        map.put("text", "<emo006>");
        mEmoList.add(map);
        map = new HashMap<String, Object>();
        map.put("img", R.drawable.emo007);
        map.put("text", "<emo007>");
        mEmoList.add(map);
        map = new HashMap<String, Object>();
        map.put("img", R.drawable.emo008);
        map.put("text", "<emo008>");
        mEmoList.add(map);
        map = new HashMap<String, Object>();
        map.put("img", R.drawable.emo009);
        map.put("text", "<emo009>");
        mEmoList.add(map);
        map = new HashMap<String, Object>();
        map.put("img", R.drawable.emo010);
        map.put("text", "<emo010>");
        mEmoList.add(map);
        map = new HashMap<String, Object>();
        map.put("img", R.drawable.emo011);
        map.put("text", "<emo011>");
        mEmoList.add(map);
        map = new HashMap<String, Object>();
        map.put("img", R.drawable.emo012);
        map.put("text", "<emo012>");
        mEmoList.add(map);
        map = new HashMap<String, Object>();
        map.put("img", R.drawable.emo013);
        map.put("text", "<emo013>");
        mEmoList.add(map);
        map = new HashMap<String, Object>();
        map.put("img", R.drawable.emo014);
        map.put("text", "<emo014>");
        mEmoList.add(map);
        map = new HashMap<String, Object>();
        map.put("img", R.drawable.emo015);
        map.put("text", "<emo015>");
        mEmoList.add(map);
        map = new HashMap<String, Object>();
        map.put("img", R.drawable.emo016);
        map.put("text", "<emo016>");
        mEmoList.add(map);
        map = new HashMap<String, Object>();
        map.put("img", R.drawable.emo017);
        map.put("text", "<emo017>");
        mEmoList.add(map);
        map = new HashMap<String, Object>();
        map.put("img", R.drawable.emo018);
        map.put("text", "<emo018>");
        mEmoList.add(map);
        map = new HashMap<String, Object>();
        map.put("img", R.drawable.emo019);
        map.put("text", "<emo019>");
        mEmoList.add(map);
        map = new HashMap<String, Object>();
        map.put("img", R.drawable.emo020);
        map.put("text", "<emo020>");
        mEmoList.add(map);
        map = new HashMap<String, Object>();
        map.put("img", R.drawable.emo021);
        map.put("text", "<emo021>");
        mEmoList.add(map);
        map = new HashMap<String, Object>();
        map.put("img", R.drawable.emo022);
        map.put("text", "<emo022>");
        mEmoList.add(map);
        map = new HashMap<String, Object>();
        map.put("img", R.drawable.emo023);
        map.put("text", "<emo023>");
        mEmoList.add(map);
        map = new HashMap<String, Object>();
        map.put("img", R.drawable.emo024);
        map.put("text", "<emo024>");
        mEmoList.add(map);
        map = new HashMap<String, Object>();
        map.put("img", R.drawable.emo025);
        map.put("text", "<emo025>");
        mEmoList.add(map);
        map = new HashMap<String, Object>();
        map.put("img", R.drawable.emo026);
        map.put("text", "<emo026>");
        mEmoList.add(map);
        map = new HashMap<String, Object>();
        map.put("img", R.drawable.emo027);
        map.put("text", "<emo027>");
        mEmoList.add(map);
        map = new HashMap<String, Object>();
        map.put("img", R.drawable.emo028);
        map.put("text", "<emo028>");
        mEmoList.add(map);
        map = new HashMap<String, Object>();
        map.put("img", R.drawable.emo029);
        map.put("text", "<emo029>");
        mEmoList.add(map);
        map = new HashMap<String, Object>();
        map.put("img", R.drawable.emo030);
        map.put("text", "<emo030>");
        mEmoList.add(map);
        map = new HashMap<String, Object>();
        map.put("img", R.drawable.emo031);
        map.put("text", "<emo031>");
        mEmoList.add(map);
        map = new HashMap<String, Object>();
        map.put("img", R.drawable.emo032);
        map.put("text", "<emo032>");
        mEmoList.add(map);
        map = new HashMap<String, Object>();
        map.put("img", R.drawable.emo033);
        map.put("text", "<emo033>");
        mEmoList.add(map);
        map = new HashMap<String, Object>();
        map.put("img", R.drawable.emo034);
        map.put("text", "<emo034>");
        mEmoList.add(map);
        map = new HashMap<String, Object>();
        map.put("img", R.drawable.emo035);
        map.put("text", "<emo035>");
        mEmoList.add(map);
        map = new HashMap<String, Object>();
        map.put("img", R.drawable.emo036);
        map.put("text", "<emo036>");
        mEmoList.add(map);
        map = new HashMap<String, Object>();
        map.put("img", R.drawable.emo037);
        map.put("text", "<emo037>");
        mEmoList.add(map);
        map = new HashMap<String, Object>();
        map.put("img", R.drawable.emo038);
        map.put("text", "<emo038>");
        mEmoList.add(map);
        map = new HashMap<String, Object>();
        map.put("img", R.drawable.emo039);
        map.put("text", "<emo039>");
        mEmoList.add(map);
        map = new HashMap<String, Object>();
        map.put("img", R.drawable.emo040);
        map.put("text", "<emo040>");
        mEmoList.add(map);
        map = new HashMap<String, Object>();
        map.put("img", R.drawable.emo041);
        map.put("text", "<emo041>");
        mEmoList.add(map);
        map = new HashMap<String, Object>();
        map.put("img", R.drawable.emo042);
        map.put("text", "<emo042>");
        mEmoList.add(map);
        map = new HashMap<String, Object>();
        map.put("img", R.drawable.emo043);
        map.put("text", "<emo043>");
        mEmoList.add(map);
        map = new HashMap<String, Object>();
        map.put("img", R.drawable.emo044);
        map.put("text", "<emo044>");
        mEmoList.add(map);
        map = new HashMap<String, Object>();
        map.put("img", R.drawable.emo045);
        map.put("text", "<emo045>");
        mEmoList.add(map);
        map = new HashMap<String, Object>();
        map.put("img", R.drawable.emo046);
        map.put("text", "<emo046>");
        mEmoList.add(map);
        map = new HashMap<String, Object>();
        map.put("img", R.drawable.emo047);
        map.put("text", "<emo047>");
        mEmoList.add(map);
        map = new HashMap<String, Object>();
        map.put("img", R.drawable.emo048);
        map.put("text", "<emo048>");
        mEmoList.add(map);
        map = new HashMap<String, Object>();
        map.put("img", R.drawable.emo049);
        map.put("text", "<emo049>");
        mEmoList.add(map);
        map = new HashMap<String, Object>();
        map.put("img", R.drawable.emo050);
        map.put("text", "<emo050>");
        mEmoList.add(map);
        map = new HashMap<String, Object>();
        map.put("img", R.drawable.emo051);
        map.put("text", "<emo051>");
        mEmoList.add(map);
        map = new HashMap<String, Object>();
        map.put("img", R.drawable.emo052);
        map.put("text", "<emo052>");
        mEmoList.add(map);
        map = new HashMap<String, Object>();
        map.put("img", R.drawable.emo053);
        map.put("text", "<emo053>");
        mEmoList.add(map);
        map = new HashMap<String, Object>();
        map.put("img", R.drawable.emo054);
        map.put("text", "<emo054>");
        mEmoList.add(map);
        map = new HashMap<String, Object>();
        map.put("img", R.drawable.emo055);
        map.put("text", "<emo055>");
        mEmoList.add(map);
        map = new HashMap<String, Object>();
        map.put("img", R.drawable.emo056);
        map.put("text", "<emo056>");
        mEmoList.add(map);
        map = new HashMap<String, Object>();
        map.put("img", R.drawable.emo057);
        map.put("text", "<emo057>");
        mEmoList.add(map);
        map = new HashMap<String, Object>();
        map.put("img", R.drawable.emo058);
        map.put("text", "<emo058>");
        mEmoList.add(map);
        map = new HashMap<String, Object>();
        map.put("img", R.drawable.emo059);
        map.put("text", "<emo059>");
        mEmoList.add(map);
        map = new HashMap<String, Object>();
        map.put("img", R.drawable.emo060);
        map.put("text", "<emo060>");
        mEmoList.add(map);

        /**
         * 上述添加表情效率高，但是代码太冗余，下面的方式代码简单，但是效率较低
         */
           /*
		   HashMap<String, Integer> map;
		   for(int i = 0; i < 100; i++){
			   map = new HashMap<String, Integer>();
			   Field field=R.drawable.class.getDeclaredField("image"+i);  
			   int resourceId=Integer.parseInt(field.get(null).toString());
			   map.put("img", resourceId);
			   mEmoList.add(map);
		   }
		   */
        return new SimpleAdapter(this, mEmoList, R.layout.grid_view_item,
                new String[]{"img"}, new int[]{R.id.imageView});
    }

    //开启服务
    private void startServiceAsServer() {
        TaskService.start(this, mHandler);
        TaskService.newTask(new Task(mHandler, Task.TASK_START_ACCEPT, null));
        SoundEffect.getInstance(this).play(SoundEffect.SOUND_PLAY);
    }

    @Override
    protected void onResume() {
        sAliveCount++;
        super.onResume();
    }

    @Override
    protected void onPause() {
        sAliveCount--;
        super.onPause();
        // 解除注册监听
        try {
            unregisterReceiver(broadcastReceiver);
//			unregisterReceiver(wifiReceiver);
        } catch (Exception e) {

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        TaskService.stop(this);
        connectedDevice.clear();        //清空列表
    }


    @Override            //发送按钮、表情按钮的点击事件
    public void onClick(View v) {
        if (v == mSendBtn) {            //发送按钮的点击事件
            String msg = mInput.getText().toString().trim();
            TaskService.newTask(new Task(mHandler, Task.TASK_GET_REMOTE_STATE, null));
            if (msg.length() == 0) {
                //聊天内容为空时的提示音
                showToast("聊天内容为空");
                SoundEffect.getInstance(MainActivity.this).play(SoundEffect.SOUND_ERR);
                return;
            }
            //------ DEUBG ------
            TaskService.newTask(new Task(mHandler, Task.TASK_SEND_MSG, new Object[]{msg}));
            //判断对方是否在线---在线则发送,否则发送失败
            if (TaskService.isOnline)
                showOwnMessage(msg);
            else
                flag = 0;            //断开连接,将flag置为0
            mInput.setText("");
        } else if (v == mEmoButton) {            //表情按钮的点击事件
            System.out.println("Emo btn clicked");
            // 关闭输入法
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(mInput.getWindowToken(), 0);
            if (isShowEmo) {
                showEmoPanel(false);
            } else {
                showEmoPanel(true);
            }
        }
    }

    //显示表情面板
    private void showEmoPanel(boolean show) {
        if (show && !isShowEmo) {
            mEmoView.setVisibility(View.VISIBLE);
            mEmoButton.setImageResource(R.drawable.emo_collapse);
            ViewGroup.LayoutParams params = mChatLayout.getLayoutParams();
            params.height = mChatLayout.getHeight() - mScrollHeight;
            mChatLayout.setLayoutParams(params);
            isShowEmo = true;
        } else if (!show && isShowEmo) {
            mEmoView.setVisibility(View.GONE);
            mEmoButton.setImageResource(R.drawable.emo_bkg);
            ViewGroup.LayoutParams params = mChatLayout.getLayoutParams();
            params.height = mChatLayout.getHeight() + mScrollHeight;
            mChatLayout.setLayoutParams(params);
            isShowEmo = false;
        }
        if (!isUpdate && show) {
            LayoutParams para = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
            mRootLayout.addView(mEmoView, para);
            isUpdate = true;
        }
    }

    // 设置表情的多页滚动显示控件
    public int setScrollGridView(DrawerHScrollView scrollView, GridView gridView,
                                 int lines) {

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        Display display = getWindowManager().getDefaultDisplay();
        System.out.println("Width:" + display.getWidth());
        System.out.println("Height:" + display.getHeight());


        int scrollWid = display.getWidth();
        int scrollHei;
        System.out.println("scrollWid:" + scrollWid);
        if (scrollWid <= 0) {
            Log.d(TAG, "scrollWid or scrollHei is less than 0");
            return 0;
        }


        float density = dm.density;      // 屏幕密度（像素比例：0.75/1.0/1.5/2.0）

        int readlViewWidht = 56;
        // 图片都放在了Hdpi中，所以计算出图片的像素独立宽度
        int viewWidth = (int) (readlViewWidht * density / 1.5);
        int viewHeight = viewWidth;
        System.out.println("viewWidth:" + viewWidth + " viewHeight:" + viewHeight);

        int numColsPage = scrollWid / viewWidth;
        int spaceing = (scrollWid - viewWidth * numColsPage) / (numColsPage);
        System.out.println("Space:" + spaceing);


        SimpleAdapter adapter = getEmoAdapter();
        int pages = adapter.getCount() / (numColsPage * lines);

        if (pages * numColsPage * lines < adapter.getCount()) {
            pages++;
        }

        System.out.println("pages:" + pages);

        scrollHei = lines * viewHeight + spaceing * (lines + 1);

        LayoutParams params = new LayoutParams(pages * scrollWid, LayoutParams.WRAP_CONTENT);
        gridView.setLayoutParams(params);
        gridView.setColumnWidth(viewWidth);
        gridView.setHorizontalSpacing(spaceing);
        gridView.setVerticalSpacing(spaceing);
        gridView.setStretchMode(GridView.NO_STRETCH);
        gridView.setNumColumns(numColsPage * pages);

        //adapter = new DrawerListAdapter(this, colWid, colHei);
        //listener = new DrawerItemClickListener();
        gridView.setAdapter(adapter);
        //mGridView.setOnItemClickListener(listener);

        scrollView.setParameters(pages, 0, scrollWid, spaceing);
        //updateDrawerPageLayout(pageNum, 0);
        // 表情区域还要加上分布显示区
        int pageNumHei = (int) (18 * density);
        return scrollHei + pageNumHei;
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case -1:
                    showToast("没有连接其它用户，点击 搜索 扫描并选择周围用户");
                    SoundEffect.getInstance(MainActivity.this).play(SoundEffect.SOUND_ERR);
                    break;
                case Task.TASK_RECV_MSG:            //接收信息
                    if (msg.obj == null)
                        return;
                    if (msg.obj instanceof HashMap<?, ?>) {
                        Log.d("判断前reConnect：",""+TaskService.reConnect);
                        if (!connectedDevice.contains(remoteDeviceName)  && remoteDeviceName != null && TaskService.reConnect ){
                            flag=0;
                            TaskService.reConnect = false ;         //表明已经新设备建立好了连接，再次置为FALSE进行监听
                        }else if (connectedDevice.contains(remoteDeviceName)){
                            TaskService.reConnect = false ;         //表明已经与历史设备建立好了连接，再次置为FALSE进行监听
                        }
                        Log.d("判断后reConnect：",""+TaskService.reConnect);
                        if (flag == 0) {            //先收到一条上线信息提醒,获取远程设备的名称
                            showTargetMessage((HashMap<String, Object>) msg.obj);
                            isHistory = true;        //开始显示历史数据
                        }
                        flag++;
                        if (flag == 1) {        //接收到上线信息提醒,此时已经获取到远程设备的名称
                            if (remoteDeviceName != null){
                                connectedDevice.add(remoteDeviceName);          //将设备名称添加到List中
                            }
                            Log.d("flag", String.valueOf(flag));
                            if (chatMapList.size() <= 0) {    //此时还没有读取数据库
                                readData();
                                if (chatMapList.size() > 0) {    //如果有历史数据,则显示出来
                                    showChatHistory();
                                    chatMapList.clear();        //历史记录显示之后，将列表清除
                                }
                                Log.d("数据库数据条数", String.valueOf(chatMapList.size()));
                            }
                            isHistory = false;        //历史数据读取结束
                        }
                        if (flag > 1) {        //获取到聊天记录之后,开始显示新的信息
                            showTargetMessage((HashMap<String, Object>) msg.obj);
                        }
                        Log.d("测试接收数据---------", String.valueOf(((HashMap) msg.obj).get(ChatListViewAdapter.KEY_TEXT)));
                        if (flag > 1)
                            Log.d("数据库数据zong------条数", getCountNum() + "");
                    }
                    if (sAliveCount <= 0) {
                        Utils.notifyMessage(MainActivity.this, "您有未读取消息", MainActivity.this);
                    }
                    break;
                case Task.TASK_GET_REMOTE_STATE:
                    //将标题设置为远程蓝牙设备的在线状态信息
                    setTitle((String) msg.obj);
                    if (sAliveCount <= 0) {
                        if (isBTStateChanged(msg.arg1) && msg.arg1 != TaskService.BT_STAT_WAIT)
                            Utils.notifyMessage(MainActivity.this, (String) msg.obj, MainActivity.this);
                    }
                    break;
                case Task.TASK_SEND_MSG:
                    showToast(msg.obj.toString());
                    if (sAliveCount <= 0) {
                        //通知服务----通知栏信息
                        Utils.notifyMessage(MainActivity.this, msg.obj.toString(), MainActivity.this);
                    }
                    break;
            }
        }
    };

    //蓝牙状态是否变化？
    private boolean isBTStateChanged(int now) {
        if (sBTState != now) {
            sBTState = now;
            return true;
        } else {
            return false;
        }
    }

    /**
     * 显示对方信息
     *
     * @param data
     */
    private void showTargetMessage(HashMap<String, Object> data) {
        Log.d("测试TTTTT", "收到对方的信息" + ((String) data.get(ChatListViewAdapter.KEY_TEXT)));
        String reMsg = (String) data.get(ChatListViewAdapter.KEY_TEXT);
//        if (reMsg == null)
//            return;
//            Log.d("对方消息---------------",reMsg);
        if ( reMsg != null && reMsg.startsWith(conFWifi)) {
            //收到的是配置网络的指令信息
            String[] wifiInfo = ((String) data.get(ChatListViewAdapter.KEY_TEXT)).split("_");
            String userName = "", password = "";
            if (wifiInfo.length == 2)
                return;
            else if (wifiInfo.length == 3) {        //无密码
                userName = wifiInfo[2];
            } else if (wifiInfo.length == 4) {
                userName = wifiInfo[2];
                password = wifiInfo[3];
            }
            try {
                wifiAutoConnectManager.connect(userName, password, password.equals("") ? WifiAutoConnectManager.WifiCipherType.WIFICIPHER_NOPASS : WifiAutoConnectManager.WifiCipherType.WIFICIPHER_WPA);
                TaskService.newTask(new Task(mHandler, Task.TASK_SEND_MSG, new Object[]{"网络连接成功！"}));
            } catch (Exception e) {
                TaskService.newTask(new Task(mHandler, Task.TASK_SEND_MSG, new Object[]{"网络配置异常！"}));
                Log.d("配网状态", "-----------------异常");
            }
            isConfigWifi = false;
            return;
        }
        if (orderMsgList.contains((String) data.get(ChatListViewAdapter.KEY_TEXT))) {
            doOrderByMsg((String) data.get(ChatListViewAdapter.KEY_TEXT));
            if (batteryInfo != null) {        //如果指令是获取电池信息能操作,则将数据发回给对方
                TaskService.newTask(new Task(mHandler, Task.TASK_SEND_MSG, new Object[]{batteryInfo}));
                batteryInfo = null;        //数据发送之后,仍置为null
            }
            Log.d("电池信息------------", batteryInfo + "--------");
            if (reNetInfo != null) {                //将网络状态返回
                TaskService.newTask(new Task(mHandler, Task.TASK_SEND_MSG, new Object[]{reNetInfo}));
                reNetInfo = null;        //数据发送之后,仍置为null
            }
            if (reWifiInfo != null) {                //将当前连接的WIFI信息返回
                TaskService.newTask(new Task(mHandler, Task.TASK_SEND_MSG, new Object[]{reWifiInfo}));
                reWifiInfo = null;        //数据发送之后,仍置为null
            }
            if (curWifiInfo != null) {
                TaskService.newTask(new Task(mHandler, Task.TASK_SEND_MSG, new Object[]{curWifiInfo}));
                curWifiInfo = null;        //数据发送之后,仍置为null
                return;
            } else {
                if (openWifiResult != null) {
                    TaskService.newTask(new Task(mHandler, Task.TASK_SEND_MSG, new Object[]{openWifiResult}));
                    openWifiResult = null;        //数据发送之后,仍置为null.
                    return;
                }
                if (closeWifiResult != null) {
                    TaskService.newTask(new Task(mHandler, Task.TASK_SEND_MSG, new Object[]{closeWifiResult}));
                    closeWifiResult = null;        //数据发送之后,仍置为null
                    return;
                }
            }
            return;
        }
        if (!isHistory) {        //如果接收到的不是历史数据
            SimpleDateFormat df1 = new SimpleDateFormat("E MM月dd日 yy HH:mm ");
            data.put(ChatListViewAdapter.KEY_DATE, df1.format(System.currentTimeMillis()).toString());
            data.put(ChatListViewAdapter.KEY_SHOW_MSG, true);
        }
        mChatContent2.add(data);
        mAdapter2.notifyDataSetChanged();
        //收到消息的提示音
        if (!isHistory) {
            SoundEffect.getInstance(MainActivity.this).play(SoundEffect.SOUND_RECV);
            //将接收到的数据保存到数据库中
            //以保证不将读取出来的聊天记录重复的存储到数据库中chatMapList.size() <= 0 &&
            if (data.get(ChatListViewAdapter.KEY_TEXT) != null && !((String) data.get(ChatListViewAdapter.KEY_TEXT)).equals(remoteDeviceName + "已经上线"))
                saveData(data);
            if (remoteDeviceName!= null)
            Log.d("remoteDeviceName******",remoteDeviceName);
            Log.d("测试接收数据是否有设备名称??", String.valueOf(data.get(ChatListViewAdapter.KEY_NAME)));
            if (remoteDeviceName == null && String.valueOf(data.get(ChatListViewAdapter.KEY_NAME))!= null){ //用于获取再次连接时对方设备的名称
                remoteDeviceName = String.valueOf(data.get(ChatListViewAdapter.KEY_NAME));
            }else if (String.valueOf(data.get(ChatListViewAdapter.KEY_NAME))!= null &&
                    !connectedDevice.contains(String.valueOf(data.get(ChatListViewAdapter.KEY_NAME)))){
                remoteDeviceName = String.valueOf(data.get(ChatListViewAdapter.KEY_NAME));
            }
            if (myDeviceName == null) {
                myDeviceName = mBluetoothAdapter.getName();
            }
        }
    }

    /**
     * 显示自己信息data
     *
     * @param
     */
    private void showOwnMessage(String msg) {
        Log.d("测试TTTT", "自己发送的信息\t" + msg);
        //如果消息属于指令,则不显示
        if (orderMsgList.contains(msg)) {
            if (isConfigWifi)
                isConfigWifi = false;
            return;
        }
        if (msg.startsWith(conFWifi)){
            return;
        }
        HashMap<String, Object> map = new HashMap<String, Object>();
        //角色：自己  name：自己蓝牙名称  文本区：msg  时间：data   是否显示：showmsg
        map.put(ChatListViewAdapter.KEY_ROLE, ChatListViewAdapter.ROLE_OWN);
        map.put(ChatListViewAdapter.KEY_NAME, mBluetoothAdapter.getName().replaceAll("\\s", ""));
        map.put(ChatListViewAdapter.KEY_TEXT, msg);
        SimpleDateFormat df2 = new SimpleDateFormat("E MM月dd日 yy HH:mm ");
        map.put(ChatListViewAdapter.KEY_DATE, df2.format(System.currentTimeMillis()).toString());
        map.put(ChatListViewAdapter.KEY_SHOW_MSG, "true");
        if (myDeviceName == null){
            myDeviceName = mBluetoothAdapter.getName();
        }
        Log.d("isOnlineTTTTest", String.valueOf(new TaskService().getIsOnline()));
        Log.d("对方设备名称", remoteDeviceName);
        //如果isOnline值为false则说明已经断开连接,,消息发送失败,前端页面不显示
        if (TaskService.isOnline) {
            mChatContent2.add(map);
            mAdapter2.notifyDataSetChanged();        //适配器内容改变,强制调用getView刷新每个Item的内容
            SoundEffect.getInstance(MainActivity.this).play(SoundEffect.SOUND_SEND);
            //发送成功,将数据保存到数据库中
//			if (chatMapList.size() <= 0)	//以保证不将读取出来的聊天记录重复的存储到数据库中
            saveData(map);
        } else {
            SoundEffect.getInstance(MainActivity.this).play(SoundEffect.SOUND_ERR);
        }
    }

    //显示历史数据(聊天记录)
    private void showChatHistory() {
        if (myDeviceName != null && remoteDeviceName != null) {
            readData();
            if (chatMapList.size() > 0) {
                for (int i = 0; i < chatMapList.size(); i++) {
                    switch ((String) chatMapList.get(i).get(COLUMN_Role)) {
                        case ChatListViewAdapter.ROLE_OWN:
                            if (TaskService.isOnline) {
                                mChatContent2.add(chatMapList.get(i));
                                mAdapter2.notifyDataSetChanged();        //适配器内容改变,强制调用getView刷新每个Item的内容
                            }
                            break;
                        case ChatListViewAdapter.ROLE_TARGET:
                            showTargetMessage(chatMapList.get(i));
                            mAdapter2.notifyDataSetChanged();
                            break;
                    }
                }
            }
            chatMapList.clear();            //清空,,以作为后续继续保存数据的标识
        }
    }


    //发送指令
    private void sendOrder() {

        AlertDialog.Builder dlg = new AlertDialog.Builder(this);
        dlg.setTitle("请选择指令");
        dlg.setItems(R.array.choiceOrder, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String[] orderArray = getResources().getStringArray(R.array.choiceOrder);
                //将选择的指令进行传输
                TaskService.newTask(new Task(mHandler, Task.TASK_SEND_MSG, new Object[]{orderArray[i]}));
                //判断对方是否在线---在线则发送,否则发送失败
                if (TaskService.isOnline)
                    showOwnMessage(orderArray[i]);
                else
                    flag = 0;            //断开连接,将flag置为0
            }
        });
        dlg.create();
        dlg.show();
    }

    //根据接收到的指令,执行相关的操作
    private void doOrderByMsg(String order) {
        switch (order) {
            case "响铃":
                if (isAlarming == false) {
                    MediaPlayerHelper.startAlarm(MainActivity.this);
                    isAlarming = true;
                }
                break;
            case "停止响铃":
                if (isAlarming == true) {
                    MediaPlayerHelper.stopAlarm();
                    isAlarming = false;
                }
                break;
            case "音量+":
                MediaPlayerHelper.addVoice(MainActivity.this);
                break;
            case "音量-":
                MediaPlayerHelper.reduceVoice(MainActivity.this);
                break;
            case "获取电池信息":
                //注册广播接收器
                IntentFilter filter = new IntentFilter();
                filter.addAction(Intent.ACTION_BATTERY_CHANGED);
                registerReceiver(broadcastReceiver, filter);
                break;
            case "打开WiFi":
                openWifi(MainActivity.this);
                break;
            case "关闭WiFi":
                closeWifi(MainActivity.this);
                break;
            case "当前WiFi信息":
                // 取得WifiInfo对象
                mWifiInfo = mWifiManager.getConnectionInfo();
                reWifiInfo = "SSID：" + mWifiInfo.getSSID() + "\t MAC：" + mWifiInfo.getMacAddress()+
                        "\tRSSI："+mWifiInfo.getRssi()+"\t连接速度："+mWifiInfo.getLinkSpeed();
                break;
            case "获取当前网络状态":
                //获取网络连接服务
                connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                //获取代表互联网连接状态的NetWorkInfo对象
                networkInfo = connectivityManager.getActiveNetworkInfo();
                if (networkInfo != null) {
                    String netInfo = "";
                    //当前网络状态是否可用
                    if (networkInfo.isAvailable()) {
                        netInfo += "网络可用\n";
                    } else {
                        netInfo += "网络不可用\n";
                    }
                    //获取GPRS网络模式连接的描述
                    NetworkInfo.State state = connectivityManager.
                            getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState();
                    if (state == NetworkInfo.State.CONNECTED) {
                        netInfo += "当前使用数据流量...\n";
                    }
                    //获取WIFI网络模式连接的描述
                    state = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
                            .getState();
                    if (state == NetworkInfo.State.CONNECTED) {
                        netInfo += "当前网络连接为WIFI...\n";
                    }
                    reNetInfo = netInfo;
                } else {
                    reNetInfo = "当前无网络连接...";
                }
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 1:
                startActivityForResult(new Intent(this, SelectDevice.class), REQUES_SELECT_BT_CODE);
                break;
            case 2:
                sendOrder();
                break;
            case 3:
                startActivity(new Intent(this, AboutActivity.class));
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUES_BT_ENABLE_CODE && resultCode == RESULT_OK) {
            startServiceAsServer();
        } else if (requestCode == REQUES_SELECT_BT_CODE && resultCode == RESULT_OK) {
            mRemoteDevice = data.getParcelableExtra("DEVICE");
            if (mRemoteDevice == null)
                return;
            TaskService.newTask(new Task(mHandler, Task.TASK_START_CONN_THREAD, new Object[]{mRemoteDevice}));
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void showToast(String msg) {
        Toast tst = Toast.makeText(this, msg, Toast.LENGTH_LONG);
        tst.setGravity(Gravity.CENTER | Gravity.TOP, 0, 240);
        tst.show();
    }

    //聊天内容保存到数据库中
    private void saveData(HashMap<String, Object> map) {
        Log.d("保存数据saveData", "saveData执行中----");
        //获取一个用于操作数据库的SQLiteDatabase实例...如果磁盘满了,则只能读不能写
        SQLiteDatabase db = sqlHelper.getWritableDatabase();
//		如果是第一条上线信息的提醒，则不进行保存。
        Log.d("remoteDeviceName--",(String) map.get(ChatListViewAdapter.KEY_TEXT));
        Log.d("IFmyDeviceName--",((String) map.get(ChatListViewAdapter.KEY_TEXT)).startsWith(new String(myDeviceName + "已经上线"))+"");
        Log.d("IFremoteDeviceName--",((String) map.get(ChatListViewAdapter.KEY_TEXT)).startsWith(new String(remoteDeviceName + "已经上线"))+"");
        Log.d("EQUALSmyDeviceName--",myDeviceName + "已经上线");
        Log.d("EQUALSIFremoteDevice",remoteDeviceName + "已经上线");


        if (((String) map.get(ChatListViewAdapter.KEY_TEXT)).startsWith(new String(myDeviceName + "已经上线")) ||
                ((String) map.get(ChatListViewAdapter.KEY_TEXT)).startsWith(new String(remoteDeviceName + "已经上线"))){
            Log.d("---IFmyDeviceName--",((String) map.get(ChatListViewAdapter.KEY_TEXT)).equals(myDeviceName + "已经上线")+"");
            Log.d("---IFremoteDeviceName--",((String) map.get(ChatListViewAdapter.KEY_TEXT)).equals(remoteDeviceName + "已经上线")+"");
            return;
        }
        //将map对象中的数据保存到数据库中
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_ID, myDeviceName + remoteDeviceName);
        contentValues.put(COLUMN_NAME, (String) map.get(ChatListViewAdapter.KEY_NAME));
        contentValues.put(COLUMN_Role, (String) map.get(ChatListViewAdapter.KEY_ROLE));
        contentValues.put(COLUMN_Date, (String) map.get(ChatListViewAdapter.KEY_DATE));
        contentValues.put(COLUMN_CONTENT, (String) map.get(ChatListViewAdapter.KEY_TEXT));
        contentValues.put(COLUMN_SHOW, "true");
        db.insert(TABLE_NAME, null, contentValues);
        Log.d("插入到数据库中的数据信息如下", "-----------------------------------");
        Log.d(COLUMN_ID, myDeviceName + remoteDeviceName);
        Log.d(COLUMN_NAME, (String) map.get(ChatListViewAdapter.KEY_NAME));
        Log.d(COLUMN_Role, (String) map.get(ChatListViewAdapter.KEY_ROLE));
        Log.d(COLUMN_Date, (String) map.get(ChatListViewAdapter.KEY_DATE));
        Log.d(COLUMN_CONTENT, (String) map.get(ChatListViewAdapter.KEY_TEXT));
        Log.d(COLUMN_SHOW, "true");
        Log.d("测试数据库插入情况:", chatMapList.size() + "");
    }

    //根据设备名称组合,从数据库中读取聊天记录
    private void readData() {
        Log.d("读取数据库数据", "readData执行中*****");
        if (chatMapList.size() > 0) {        //如果之前获取过聊天记录,则将聊天记录清空
            chatMapList.clear();
        }
        SQLiteDatabase db = sqlHelper.getWritableDatabase();
        //COLUMN_ID  由双方的设备名称组成---myDeviceName+remoteDeviceName---便于筛选数据
        Cursor cursor = db.rawQuery("select * from " + TABLE_NAME + " where " + COLUMN_ID + " = ? ", new String[]{(myDeviceName + remoteDeviceName)});
        if (cursor.moveToFirst()) {
            do {
                //将读出的消息存到list中
                HashMap<String, Object> map = new HashMap<>();
                map.put(COLUMN_ID, cursor.getString(cursor.getColumnIndex(COLUMN_ID)));
                map.put(COLUMN_NAME, cursor.getString(cursor.getColumnIndex(COLUMN_NAME)));
                map.put(COLUMN_Role, cursor.getString(cursor.getColumnIndex(COLUMN_Role)));
                map.put(COLUMN_Date, cursor.getString(cursor.getColumnIndex(COLUMN_Date)));
                map.put(COLUMN_CONTENT, cursor.getString(cursor.getColumnIndex(COLUMN_CONTENT)));
                map.put(COLUMN_SHOW, cursor.getString(cursor.getColumnIndex(COLUMN_SHOW)));
                Log.d("数据库中读取的信息如下", "--------------------------");
                Log.d(COLUMN_ID, cursor.getString(cursor.getColumnIndex(COLUMN_ID)));
                Log.d(COLUMN_NAME, cursor.getString(cursor.getColumnIndex(COLUMN_NAME)));
                Log.d(COLUMN_Role, cursor.getString(cursor.getColumnIndex(COLUMN_Role)));
                Log.d(COLUMN_Date, cursor.getString(cursor.getColumnIndex(COLUMN_Date)));
                Log.d(COLUMN_CONTENT, cursor.getString(cursor.getColumnIndex(COLUMN_CONTENT)));
                Log.d(COLUMN_SHOW, cursor.getString(cursor.getColumnIndex(COLUMN_SHOW)));
                chatMapList.add(map);
            } while (cursor.moveToNext());
        }
    }

    //删除聊天记录
    private void deleteChatHistory() {
        SQLiteDatabase db = sqlHelper.getWritableDatabase();
//		db.execSQL("delete from "+TABLE_NAME+" where "+COLUMN_ID+" = ? ",);
        String nameId = myDeviceName + remoteDeviceName;
        db.delete(TABLE_NAME, COLUMN_ID + " = ? ", new String[]{nameId});
        Log.d("表名", TABLE_NAME);
        Log.d("COLUMN_ID", myDeviceName + remoteDeviceName);
        Log.d("myDeviceName", myDeviceName);
        Log.d("remoteDeviceName", remoteDeviceName);
        db.close();
    }

    //获取数据库中总数据条数(条件查询---本机设备蓝牙名称和远程设备蓝牙名称组合)
    private int getCountNum() {
        int count = 0;
        SQLiteDatabase db = sqlHelper.getWritableDatabase();
//		String sql = +();
//		Log.d("getCountNum-sql",sql);
        try {
            Cursor cursor = db.rawQuery("select count(*) from " + TABLE_NAME + " where " + COLUMN_ID + " = ? ", new String[]{myDeviceName + remoteDeviceName});
            cursor.moveToFirst();
            count = cursor.getInt(0);
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
            count = 0;
        }
        return count;
    }


    // 打开WIFI
    public void openWifi(Context context) {
        if (!mWifiManager.isWifiEnabled()) {
            mWifiManager.setWifiEnabled(true);
        }
        openWifiResult = "WIFI已开启...";
    }

    // 关闭WIFI
    public void closeWifi(Context context) {
        if (mWifiManager.isWifiEnabled()) {
            mWifiManager.setWifiEnabled(false);
        }
        closeWifiResult = "WIFI已关闭...";
    }

    //获取电池信息的广播接收器
    public BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Intent.ACTION_BATTERY_CHANGED)) {
                //得到电池状态
                int status = intent.getIntExtra("status", 0);
                //得到电池健康状态
                int health = intent.getIntExtra("health", 0);
                //得到电池剩余容量
                int level = intent.getIntExtra("level", 0);
                //得到电池最大值，通常为100
                int scale = intent.getIntExtra("scale", 0);
                //得到图标ID
                int icon_small = intent.getIntExtra("icon-small", 0);
                //充电方式
                int plugged = intent.getIntExtra("plugged", 0);
                //得到电池电压
                int voltage = intent.getIntExtra("voltage", 0);
                //得到电池的温度，0.1度单位
                int temperature = intent.getIntExtra("temperature", 0);
                //得到电池的类型
                String technology = intent.getStringExtra("technology");
                // 得到电池状态
                String statusString = "";
                // 根据状态id，得到状态字符串
                switch (status) {
                    case BatteryManager.BATTERY_STATUS_UNKNOWN:
                        statusString = "unknown";
                        break;
                    case BatteryManager.BATTERY_STATUS_CHARGING:
                        statusString = "charging";
                        break;
                    case BatteryManager.BATTERY_STATUS_DISCHARGING:
                        statusString = "discharging";
                        break;
                    case BatteryManager.BATTERY_STATUS_NOT_CHARGING:
                        statusString = "not charging";
                        break;
                    case BatteryManager.BATTERY_STATUS_FULL:
                        statusString = "full";
                        break;
                }
                //得到电池的寿命状态
                String healthString = "";
                //根据状态id，得到电池寿命
                switch (health) {
                    case BatteryManager.BATTERY_HEALTH_UNKNOWN:
                        healthString = "unknown";
                        break;
                    case BatteryManager.BATTERY_HEALTH_GOOD:
                        healthString = "good";
                        break;
                    case BatteryManager.BATTERY_HEALTH_OVERHEAT:
                        healthString = "overheat";
                        break;
                    case BatteryManager.BATTERY_HEALTH_DEAD:
                        healthString = "dead";
                        break;
                    case BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE:
                        healthString = "voltage";
                        break;
                    case BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE:
                        healthString = "unspecified failure";
                        break;
                }
                //得到充电模式
                String acString = "";
                //根据充电状态id，得到充电模式
                switch (plugged) {
                    case BatteryManager.BATTERY_PLUGGED_AC:
                        acString = "plugged ac";
                        break;
                    case BatteryManager.BATTERY_PLUGGED_USB:
                        acString = "plugged usb";
                        break;
                }
                //将要返回的电池信息内容
                batteryInfo = "电池状态：" + statusString + "\n健康值：" + healthString + "\n电池电量："
                        + String.valueOf(((float) level / scale) * 100 + "%") + "\n充电方式：" + acString + "\n电池电压："
                        + voltage + "\n电池温度：" + (float) temperature * 0.1 + "\n电池类型：" + technology;
            }
        }
    };
}
