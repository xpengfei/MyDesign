package com.example.xpengfei.mybluetoothtest41.utils;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;

/**
 * Created by xpengfei on 2018/4/3.
 * void setDataSource (Context context , Uri uri)//根据Uri设置音频，当然还有其他几个重载的方法来指定特定的音频。
 * void setLooping (boolean looping)//设置是否循环播放
 * void prepare ()//让MediaPlayer真正去装载音频文件
 * void start ()//开始或恢复播放
 * void pause ()//暂停播放，调用start()可以恢复播放
 * void stop ()//停止播放
 * boolean isPlaying ()//是否正在播放
 * void release ()//释放与此MediaPlayer关联的资源
 */

public class MediaPlayerHelper {
    private static MediaPlayer mediaPlayer;              //媒体播放器对象
    private static AudioManager audioManager;            //Audio 管理器，用于控制音量
    private static int changeFlag = 1;                  //音量增加或减少的幅度
    //响铃---系统的闹钟铃声
    public static synchronized void startAlarm(Context context){
        mediaPlayer = new MediaPlayer();
        try {
//            获取系统的闹钟声音----RingtoneManager.TYPE_ALARM
            mediaPlayer.setLooping(true);
            mediaPlayer.setDataSource(context, RingtoneManager
                    .getDefaultUri(AudioManager.STREAM_ALARM));
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //停止响铃
    public static void stopAlarm(){
        mediaPlayer.stop();
    }

    //增加音量
    public static void addVoice(Context context){
        audioManager = (AudioManager) context.getSystemService(context.AUDIO_SERVICE);
        int max= audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM);
        int current=audioManager.getStreamVolume(AudioManager.STREAM_ALARM);
        if (current < max){
            audioManager.adjustStreamVolume(AudioManager.STREAM_ALARM,AudioManager.ADJUST_RAISE,AudioManager.FLAG_SHOW_UI);
        }
        //改变闹钟音量的同时，改变媒体的音量大小
        int maxMedia=audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        int currentMedia=audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        if (currentMedia < maxMedia){
            audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,AudioManager.ADJUST_RAISE,AudioManager.FLAG_SHOW_UI);
        }
    }
    //减小音量
    public static void reduceVoice(Context context){
        audioManager = (AudioManager) context.getSystemService(context.AUDIO_SERVICE);
        int current=audioManager.getStreamVolume(AudioManager.STREAM_ALARM);
        if (current > 0){
            audioManager.adjustStreamVolume(AudioManager.STREAM_ALARM,AudioManager.ADJUST_LOWER,AudioManager.FLAG_SHOW_UI);
        }
        //同时改变媒体的音量大小
        int currentMedia=audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        if (currentMedia > 0){
            audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,AudioManager.ADJUST_LOWER,AudioManager.FLAG_SHOW_UI);
        }
    }

}
