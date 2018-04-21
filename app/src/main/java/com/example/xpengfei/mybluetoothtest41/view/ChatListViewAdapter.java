package com.example.xpengfei.mybluetoothtest41.view;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;


import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.text.Html.ImageGetter;
import android.text.Spanned;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

import com.example.xpengfei.mybluetoothtest41.R;

import static com.example.xpengfei.mybluetoothtest41.database.SQLHelper.COLUMN_CONTENT;
import static com.example.xpengfei.mybluetoothtest41.database.SQLHelper.COLUMN_Date;
import static com.example.xpengfei.mybluetoothtest41.database.SQLHelper.COLUMN_Role;
import static com.example.xpengfei.mybluetoothtest41.database.SQLHelper.COLUMN_SHOW;

/**
 * 聊天页面消息类
 * Created by xpengfei on 2018/3/29.
 */
public class ChatListViewAdapter extends BaseAdapter {
	public static final String ROLE_OWN = "own";			//自己
	public static final String ROLE_TARGET = "target";		//对方
	public static final int ROLE_OTHER = 2;			//其它
	public static final String KEY_ROLE = "role";		//角色--ROLE_OWN/ROLE_TARGET
	public static final String KEY_TEXT = "text";		//文本
	public static final String KEY_DATE = "date";		//日期时间
	public static final String KEY_NAME = "name";		//名称
	public static final String KEY_SHOW_MSG = "show_msg";	//显示消息
	
	private Context mContext;
	
	private ArrayList<HashMap<String, Object>> mDatalist;
	//View中的LayoutInflater对象
	private LayoutInflater mInflater;

	private DisplayMetrics dm;		//屏幕分辨率
	

	public ChatListViewAdapter(Context context, ArrayList<HashMap<String, Object>> data) {
		super();
		mContext = context;
		mDatalist = data;
		mInflater = LayoutInflater.from(context);
		//获取屏幕分辨率信息
		dm = new DisplayMetrics();
		((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(dm); 
	}

	@Override
	public int getCount() {
		return mDatalist.size();
	}

	@Override
	public Object getItem(int position) {
		return null;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override			//这里用于构造前端页面所能看到的布局数据
	public View getView(int position, View convertView, ViewGroup parent) {
		TextView text;				//文本信息
		TextView date;				//时间信息
		if(convertView == null){
			//加载布局文件--聊天列表-chat_list_item_layout
			View layout = mInflater.inflate(R.layout.chat_list_item_layout, null);
			if(layout == null)
				return null;
			// 聊天内容TextView
			text = (TextView) layout.findViewById(R.id.tvText);
			ClickListener listener = new ClickListener(text);		//点击的监听事件
			text.setOnClickListener(listener);
			text.setTag(listener);				//存储的View的额外数据
			
			// 时间日期TextView
			date = (TextView) layout.findViewById(R.id.tvDate);
			ViewHolder holder = new ViewHolder(null, text, date);
			holder.setPosition(position);
			layout.setTag(holder);
			convertView = layout;
		}else{
			ViewHolder holder = (ViewHolder) convertView.getTag();
			text = holder.mText;
			date = holder.mDate;
			holder.setPosition(position);
		}
		if(text == null || date == null)
			return null;
		//获取角色信息，实时消息中标志为  KEY_ROLE=role，历史记录中为COLUMN_Role= "key_role"
		String role = (String) mDatalist.get(position).get(KEY_ROLE);
		if (role == null)
			role = (String) mDatalist.get(position).get(COLUMN_Role);
		RelativeLayout rLayout = (RelativeLayout) convertView;
		LayoutParams param;
		Log.d("位置--------------", String.valueOf(position));
		switch (role) {
		case ROLE_OWN:	// 自己发送的消息显示在右边
			rLayout.removeAllViews();
			param = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
			param.addRule(RelativeLayout.ALIGN_PARENT_TOP);
			//设置日期值
			String datea=(String) mDatalist.get(position).get(KEY_DATE);
			if (datea == null)
				datea = (String) mDatalist.get(position).get(COLUMN_Date);
			date.setText(datea);
			//将其加载到View视图中显示
			rLayout.addView(date, param);
			//设置文本区颜色
			text.setTextColor(Color.WHITE);
			text.setBackgroundResource(R.drawable.chart_list_item_right_selector);
			// 获取内容
			String content = (String) mDatalist.get(position).get(KEY_TEXT);
			if (content == null)
				content = (String) mDatalist.get(position).get(COLUMN_CONTENT);
			Spanned spann = makeChatContent(content);
			text.setText(spann);
			//消息的点击事件
			ClickListener listener = (ClickListener) text.getTag();
			if(listener != null){
				//隐藏消息内容  &  显示消息内容
				String isShowMsg = (String)mDatalist.get(position).get(KEY_SHOW_MSG);
				if (isShowMsg == null)
					isShowMsg = (String)mDatalist.get(position).get(COLUMN_SHOW);
				if(isShowMsg.equals("true")){
					listener.hideMessage();
				}else{
					listener.showMessage();
				}
			}
				
			param = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			param.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
			param.addRule(RelativeLayout.BELOW, date.getId());
			rLayout.addView(text, param);
			break;

		case ROLE_TARGET:	//接收到的对方的消息 显示在左边
			rLayout.removeAllViews();
			param = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
			param.addRule(RelativeLayout.ALIGN_PARENT_TOP);
			String datea1=(String) mDatalist.get(position).get(KEY_DATE);
			if (datea1 == null)
				datea1 = (String) mDatalist.get(position).get(COLUMN_Date);
			date.setText(datea1);
			rLayout.addView(date, param);

			text.setTextColor(Color.BLACK);
			text.setBackgroundResource(R.drawable.chart_list_item_left_selector);
			// 获取内容
			String content1 = (String) mDatalist.get(position).get(KEY_TEXT);
			if (content1 == null)
				content1 = (String) mDatalist.get(position).get(COLUMN_CONTENT);
			Spanned spann2 = makeChatContent(content1);
			text.setText(spann2);
			ClickListener listener2 = (ClickListener) text.getTag();
			if(listener2 != null){
				Boolean isShow=(Boolean)mDatalist.get(position).get(KEY_SHOW_MSG);
				if (isShow == null){
					String show = (String)mDatalist.get(position).get(COLUMN_SHOW);
					if (show.equals("true")){
						isShow = true;
					}else {
						isShow = false;
					}
				}
				if( isShow ){
					listener2.hideMessage();
				}else{
					listener2.showMessage();
				}
			}
			param = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			param.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
			param.addRule(RelativeLayout.BELOW, date.getId());
			rLayout.addView(text, param);
			break;
		default:
			return null;
		}
		return rLayout;
	}
	//触摸监听事件
	private class TouchListener implements OnTouchListener{
		private TextView mView;
		public TouchListener(TextView v){
			mView = v;
		}
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			if(event.getAction() == MotionEvent.ACTION_DOWN ){
				mView.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
			}else if(event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_OUTSIDE){
				mView.setTransformationMethod(PasswordTransformationMethod.getInstance());
			}
			return true;
		}
	}
	//点击监听事件
	private class ClickListener implements OnClickListener{
		private TextView mView;
		public ClickListener(TextView v){
			mView = v;
		}
		
		public void showMessage(){
			mView.setTransformationMethod(PasswordTransformationMethod.getInstance());
		}
		public void hideMessage(){
			mView.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
		}
		@Override
		public void onClick(View v) {
			RelativeLayout rLayout = (RelativeLayout) mView.getParent();
			ViewHolder holder = (ViewHolder) rLayout.getTag();
			int pos = holder.getPosition();
			boolean isShow = (Boolean) mDatalist.get(pos).get(KEY_SHOW_MSG);
			if(isShow){
				mView.setTransformationMethod(PasswordTransformationMethod.getInstance());
				try {
					mDatalist.get(pos).put(KEY_SHOW_MSG, false);
				}catch (Exception e){
					mDatalist.get(pos).put(COLUMN_SHOW, false);
				}

			}else {
				mView.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
				try {
					mDatalist.get(pos).put(KEY_SHOW_MSG, true);
				}catch (Exception e){
					mDatalist.get(pos).put(COLUMN_SHOW, true);
				}
			}
		}
	}
	
	//<emo001]
	private Spanned makeChatContent(String msg){
		String htmlStr = msg;
		while(true){
			int start = htmlStr.indexOf("<emo", 0);
			if(start != -1){		//文字消息
				String resIdStr = htmlStr.substring(start+1,start + 7);
				htmlStr = htmlStr.replaceFirst("<emo...>", "<img src='" + resIdStr +"'/>");
			}else{					//表情消息
				return Html.fromHtml(htmlStr, imgGetter, null);
			}
		}
	}
	
	private ImageGetter imgGetter = new ImageGetter() {
		@Override
		public Drawable getDrawable(String source) {
			int resID =  mContext.getResources().getIdentifier(source, "drawable", mContext.getPackageName());
			Drawable drawable = mContext.getResources().getDrawable(resID);
			int w = (int) (drawable.getIntrinsicWidth() * dm.density / 2);
			int h = (int) (drawable.getIntrinsicHeight() * dm.density / 2);
			drawable.setBounds(0, 0, w , h);
			return drawable;
		}
	};
	
	class ViewHolder {
		public TextView mName, mText, mDate; 
		public int position;
		public ViewHolder(TextView name, TextView text, TextView date){
			this.mName = name;
			this.mText = text;
			this.mDate = date;
		}
		public int getPosition() {
			return position;
		}
		public void setPosition(int position) {
			this.position = position;
		}
	}
}
