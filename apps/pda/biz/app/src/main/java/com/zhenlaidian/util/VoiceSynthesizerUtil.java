package com.zhenlaidian.util;


import android.content.Context;
import android.os.Bundle;

import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SynthesizerListener;

/**
 * 科大讯飞语音合成播放;
 */
public class VoiceSynthesizerUtil {

    public Context context;

    public VoiceSynthesizerUtil(Context context) {
        super();
        this.context = context;
    }

    public void playText(String text) {
        //1.创建SpeechSynthesizer对象, 第二个参数：本地合成时传InitListener
        SpeechSynthesizer mTts = SpeechSynthesizer.createSynthesizer(context, null);
        //2.合成参数设置，详见《科大讯飞MSC API手册(Android)》SpeechSynthesizer 类
        mTts.setParameter(SpeechConstant.VOICE_NAME, "vixy");//设置发音人
        mTts.setParameter(SpeechConstant.SPEED, "50");//设置语速
        mTts.setParameter(SpeechConstant.VOLUME, "100");//设置音量，范围0~100
        mTts.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD); //设置云端
        //设置合成音频保存位置（可自定义保存位置），保存在“./sdcard/iflytek.pcm”
        //保存在SD卡需要在AndroidManifest.xml添加写SD卡权限
        //如果不需要保存合成音频，注释该行代码
//	    mTts.setParameter(SpeechConstant.TTS_AUDIO_PATH, "./sdcard/iflytek.pcm");  
        //3.开始合成
        mTts.startSpeaking(text, mSynListener);
    }

    //合成监听器  
    private SynthesizerListener mSynListener = new SynthesizerListener() {

        //percent为缓冲进度0~100，beginPos为缓冲音频在文本中开始位置，endPos表示缓冲音频在文本中结束位置，info为附加信息。
        @Override
        public void onBufferProgress(int arg0, int arg1, int arg2, String arg3) {
            // TODO Auto-generated method stub

        }

        //会话结束回调接口，没有错误时，error为null
        @Override
        public void onCompleted(SpeechError arg0) {
            // TODO Auto-generated method stub

        }

        //会话事件回调接口
        @Override
        public void onEvent(int arg0, int arg1, int arg2, Bundle arg3) {
            // TODO Auto-generated method stub

        }

        //开始播放
        @Override
        public void onSpeakBegin() {
            // TODO Auto-generated method stub

        }

        //暂停播放
        @Override
        public void onSpeakPaused() {
            // TODO Auto-generated method stub

        }

        //percent为播放进度0~100,beginPos为播放音频在文本中开始位置，endPos表示播放音频在文本中结束位置.
        @Override
        public void onSpeakProgress(int arg0, int arg1, int arg2) {
            // TODO Auto-generated method stub

        }

        //恢复播放回调接口
        @Override
        public void onSpeakResumed() {
            // TODO Auto-generated method stub

        }

    };


}
