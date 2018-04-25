package com.example.xpengfei.mybluetoothtest41.activity;

import java.util.ArrayList;
import java.util.Set;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.example.xpengfei.mybluetoothtest41.R;
/**
 * Created by xpengfei on 2018/3/29.
 */
public class SelectDevice extends Activity implements OnClickListener, OnItemClickListener {
	private final String TAG = "MainActivity";
	private BluetoothAdapter mBluetoothAdapter;		//蓝牙适配器
	private Button mScanBtn;			//扫描按钮
	private ListView mDevList;			//设备列表View
	
	private ArrayAdapter<String> adapter;
	private ArrayList<String> mArrayAdapter = new ArrayList<String>();
	private ArrayList<BluetoothDevice> mDeviceList = new ArrayList<BluetoothDevice>();		//设备List

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		
		setContentView(R.layout.activity_main);
		
		mDevList = (ListView) findViewById(R.id.devList);
		//设置设备列表的点击监听事件
		mDevList.setOnItemClickListener(this);
		//扫描按钮的点击事件
		mScanBtn = (Button) findViewById(R.id.scanBtn);
		mScanBtn.setOnClickListener(this);
		//适配器
		adapter = new ArrayAdapter<String>(
				this, 
				android.R.layout.simple_list_item_1, 
				mArrayAdapter);
		
		mDevList.setAdapter(adapter);
		//获取本地蓝牙适配器
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter(); 
		if (mBluetoothAdapter == null) {     
			// 设备不支持蓝牙
			Log.e(TAG, "Your device is not support Bluetooth!");
			return;
		}
		
		// 设置未打开，请求打开设备
		if (!mBluetoothAdapter.isEnabled()) {   
			// 请求打开蓝牙设备 
			Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableBtIntent, 21); 
		}else{
			//若已经打开蓝牙，则开始进行扫描
			findDevice();
		}

		// 注册广播接收器
		IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		registerReceiver(mReceiver, filter); 
	}
	
	@Override
	protected void onResume() {
		MainActivity.sAliveCount++;
		super.onResume();
	}

	@Override
	protected void onPause() {
		MainActivity.sAliveCount--;
		super.onPause();
	}
	
	//  ACTION_FOUND的广播接收器
	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) { 
			String action = intent.getAction();         
			// When discovery finds a device         
			if (BluetoothDevice.ACTION_FOUND.equals(action)) {
				// 从intent中获取BluetoothDevice对象
				BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				// 已包含该设备
				if(mDeviceList.contains(device)){
					return;
				}
				// 将设备的名称以及地址添加到数组适配器中
				mArrayAdapter.add(device.getName() +"      \t"+intent.getExtras().getShort(BluetoothDevice.EXTRA_RSSI)+ "\n" + device.getAddress());
				System.out.println(device.getName() + "\n" + device.getAddress());
				mDeviceList.add(device);
				adapter.notifyDataSetChanged();
			}else if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)){
				// 取消扫描进度显示
				setProgressBarIndeterminateVisibility(false);
			}
		} 
	}; 
	
	/**
	 * 开始扫描
	 */
	@Override
	public void onClick(View v) {
		if(!mBluetoothAdapter.isDiscovering()){
			mBluetoothAdapter.startDiscovery();
			// 开始显示进度
			setProgressBarIndeterminateVisibility(true);
		}
	}
	
	private void findDevice(){
		// 获得已经保存的配对设备
		Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices(); 
		// 若已配对过的设备不为空
		if (pairedDevices.size() > 0) {
			// 循环遍历配对过的设备
			for (BluetoothDevice device : pairedDevices) { 
				// 将配对过的设备加入到数组适配器中,用于在ListView中显示
				mArrayAdapter.add(device.getName() + "\n" + device.getAddress());
				mDeviceList.add(device);
			} 
		}

		adapter.notifyDataSetChanged();
	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(requestCode == 21){		//请求码为21
			if(resultCode ==  RESULT_OK){
				System.out.println("设备打开成功");
				findDevice();
			}else{
				System.out.println("设备打开失败");
			}
		}
	}
	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(mReceiver);
	}
	@Override	//监听点击的设备信息，进行连接通信
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
			String targetDev = mArrayAdapter.get(arg2);
			System.out.println(targetDev);
			// 将点击的设备对象保存到Intent中
			Intent data = new Intent();
			data.putExtra("DEVICE", mDeviceList.get(arg2));
			setResult(RESULT_OK, data);		//将intent对象以及返回码回传
			this.finish();
	}
}
