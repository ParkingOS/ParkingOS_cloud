package com.zhenlaidian.util;

import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.util.Log;

/**
 * 播放一段音频;
 */
public class PlayerVoiceUtil {

    public Context context;
    private MediaPlayer player;
    public int raw;

    public PlayerVoiceUtil(Context context, int raw) {
        this.context = context;
        this.raw = raw;
        player = MediaPlayer.create(context, raw);
    }

    public void play() {
        try {
            player.setOnCompletionListener(new OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    Log.e("ShareBroadCast", "播放完毕释放资源");
                    mp.release();
                }
            });
            player.start();
        } catch (Exception e) {

        }
    }
}
