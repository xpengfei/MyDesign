package com.example.xpengfei.mybluetoothtest41.task;

import android.os.Handler;
/**
 * 任务类
 * Created by xpengfei on 2018/3/29.
 */
public class Task {
	public static final int TASK_START_ACCEPT = 1;		//开启监听线程,用于接收连接请求
	public static final int TASK_START_CONN_THREAD = 2;	//开启连接线程
	public static final int TASK_SEND_MSG = 3;			//发送消息
	public static final int TASK_GET_REMOTE_STATE = 4;	//获取远程设备的状态
	public static final int TASK_RECV_MSG = 5;			//接收文本消息
	public static final int TASK_PROGRESS = 8;			//进度

	
	// 任务ID
	private int mTaskID;
	// 任务参数列表
	public Object[] mParams;
	
	private Handler mH;
	
	
	public Task(Handler handler, int taskID, Object[] params){
		this.mH = handler;			//用于多线程之间传输消息
		this.mTaskID = taskID;		//任务ID编号
		this.mParams = params;		//任务参数列表
	}
	
	public Handler getHandler(){
		return this.mH;
	}
	
	public int getTaskID(){
		return mTaskID;
	}
}
