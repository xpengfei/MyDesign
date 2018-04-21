package com.example.xpengfei.mybluetoothtest41.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by xpengfei on 2018/3/29.
 * SQLite数据库默认会自动存在一个RowID，从1开始递增，每增加一条记录数值自动加一
 * 当设置了主键，而且主键的类型为integer时，查询RowID等于主键
 */
public class SQLHelper extends SQLiteOpenHelper {
    public static final String DB_NAME = "blue_tooth_chat_db";
    public static final String TABLE_NAME = "chat_table";   //表名

    public static final String COLUMN_ID = "id";            //id标识--唯一标识有双方设备名称组成
    public static final String COLUMN_NAME = "device_name"; //设备名称
    public static final String COLUMN_Role= "key_role";     //左右侧消息标识
    public static final String COLUMN_Date="key_date";      //时间
    public static final String COLUMN_CONTENT = "content";  //内容
    public static final String COLUMN_SHOW="is_show_msg";    //是否显示文本内容
    private static final String CREATE_TABLE = "create table " + TABLE_NAME +
            "(" + COLUMN_ID + " text , " + COLUMN_NAME + " text ," + COLUMN_Role  + " varchar(20) , " +COLUMN_Date+" text ,"
            + COLUMN_CONTENT + " text ,"+COLUMN_SHOW+" varchar(20)) ";
    //构造器,用于创建数据库对象
    public SQLHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }
    //创建数据库后,对数据的相关操作
    @Override
    public void onCreate(SQLiteDatabase db) {
        //创建表
        db.execSQL(CREATE_TABLE);
    }

    //更改数据库版本的相关操作--------用不到
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }


}
