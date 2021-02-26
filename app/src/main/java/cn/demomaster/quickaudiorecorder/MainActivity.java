package cn.demomaster.quickaudiorecorder;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;

import cn.demomaster.quickaudiorecorderlib.AudioRecorder;
import cn.demomaster.quickaudiorecorderlib.AudioUtil;
import cn.demomaster.quickaudiorecorderlib.RecordStreamListener;
import cn.demomaster.quickaudiorecorderlib.util.DisplayUtil;
import cn.demomaster.quickaudiorecorderlib.view.WaveLineView;
import cn.demomaster.quickaudiorecorderlib.view.WaveView;
import cn.demomaster.quickaudiorecorderlib.view.wechat.WechatAudioRecordPopup;

import static cn.demomaster.quickaudiorecorderlib.AudioRecorder.TIMER_INTERVAL;

public class MainActivity extends AppCompatActivity {

    AudioRecorder audioRecorder;
    Button btn_start_record;
    Button btn_pause_record;
    Button btn_stop_record;
    WaveView waveView;
    WaveLineView waveLineView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn_start_record = findViewById(R.id.btn_start_record);
        btn_start_record.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startRecord();
            }
        });
        btn_pause_record = findViewById(R.id.btn_pause_record);
        btn_pause_record.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pauseRecord();
            }
        });
        btn_stop_record = findViewById(R.id.btn_stop_record);
        btn_stop_record.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopRecord();
            }
        });

        waveView = findViewById(R.id.waveView);
        waveLineView = findViewById(R.id.waveLineView);

        audioRecorder = AudioRecorder.getInstance();
        audioRecorder.setRecordStreamListener(mRecordStreamListener);
        requestPermissions();
        Button btn_record = findViewById(R.id.btn_record);
        btn_record.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                showPopWindow(v,event);
                return false;
            }
        });
    }

    int count = 0;
    RecordStreamListener mRecordStreamListener = new RecordStreamListener() {
        @Override
        public void onStart() {
            btn_start_record.setVisibility(View.GONE);
            btn_pause_record.setVisibility(View.VISIBLE);
            btn_stop_record.setVisibility(View.VISIBLE);
            waveLineView.startAnim();
        }

        @Override
        public void onPause() {
            btn_start_record.setVisibility(View.VISIBLE);
            btn_pause_record.setVisibility(View.GONE);
            btn_stop_record.setVisibility(View.VISIBLE);
            waveLineView.stopAnim();
        }

        @Override
        public void onStop() {
            btn_start_record.setVisibility(View.VISIBLE);
            btn_pause_record.setVisibility(View.GONE);
            btn_stop_record.setVisibility(View.GONE);
            waveLineView.stopAnim();
        }

        @Override
        public void onRelease() {
            btn_start_record.setVisibility(View.VISIBLE);
            btn_pause_record.setVisibility(View.GONE);
            btn_stop_record.setVisibility(View.GONE);
            waveLineView.stopAnim();
        }

        @Override
        public void recordOfByte(byte[] data, int begin, int end) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    short[] data1 = toShortArray(data);
                    for (int i = 0; i < data1.length; i += 60) {
                        waveView.addData(data1[i]);
                    }

                    count++;
                    long recordedTime = count * TIMER_INTERVAL;
                    int volume = 0;
                    int volumeInterval = 200;
                    if (recordedTime >= volumeInterval && recordedTime % volumeInterval == 0) {
                        volume = (AudioUtil.calculateVolume(data1));
                        double myVolume = (volume - 40) * 4;
                        waveLineView.setVolume((int) myVolume);
                        if(pop!=null) {
                            pop.setVolume(Math.max(0,volume-40));
                        }
                        Log.d("MainActivity", "current volume is " + volume);
                    }
                }
            });
        }
    };

    /**
     * byte数组转short数组
     *
     * @param src
     * @return
     */
    public short[] toShortArray(byte[] src) {
        int count = src.length >> 1;
        short[] dest = new short[count];
        for (int i = 0; i < count; i++) {
            dest[i] = (short) ((src[i * 2] & 0xff) | ((src[2 * i + 1] & 0xff) << 8));
        }
        return dest;
    }

    int REQUEST_CODE = 10306;
    private void requestPermissions() {
        if (Build.VERSION.SDK_INT >= 23) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[]
            grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE) {
            //当然权限多了，建议使用Switch，不必纠结于此
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "权限申请成功", Toast.LENGTH_SHORT).show();
            } else if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                Toast.makeText(this, "权限申请失败，用户拒绝权限", Toast.LENGTH_SHORT).show();
            }
        }
    }

    //初始化录音
    private void startRecord() {
        if (audioRecorder.getStatus()== AudioRecorder.Status.STATUS_IDLE) {
            audioRecorder.createAudio(this);
            //audioRecorder.setOutputFilePath("");
            //audioRecorder.setFileName("hahaha");
        }
        audioRecorder.startRecord();
    }

    //暂停录音
    private void stopRecord() {
        audioRecorder.stopRecord();
    }

    //暂停录音
    private void pauseRecord() {
        audioRecorder.pauseRecord();
    }

    private WechatAudioRecordPopup pop = null;
    private void showPopWindow(View v, MotionEvent event) {
        int[] location = new int[2];
        v.getLocationOnScreen(location);//获取在整个屏幕内的绝对坐标
        if(pop!=null&&pop.isShowing()){
            pop.setButtonLocation(location);
            pop.getTouchableViewGroup().dispatchTouchEvent(event);
            return;
        }
        pop = new WechatAudioRecordPopup(this);
        //pop.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        //pop.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        pop.setWidth(DisplayUtil.getScreenWidth(this));
        pop.setHeight(DisplayUtil.getScreenHeight(this));
        //pop.setClippingEnabled(true);
        //pop.setIsClippedToScreen(true);
        pop.setBackgroundDrawable(new BitmapDrawable());
        pop.setFocusable(true);
        pop.setOutsideTouchable(false);
        //pop.showAtLocation(pop.getContentView(), Gravity.BOTTOM, 0, 0);
        pop.showAtLocation(pop.getContentView(), Gravity.NO_GRAVITY,0,0);
        pop.setOnRecordListener(new WechatAudioRecordPopup.OnRecordListener() {
            @Override
            public void onFinish() {
                stopRecord();
            }

            @Override
            public void onCancel() {
                releaseRecord();
            }
        });
        pop.setButtonLocation(location);
        pop.getTouchableViewGroup().dispatchTouchEvent(event);

        //振動
        Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        long[] patter = {0,30,10,30};
        vibrator.vibrate(patter, -1);

        startRecord();
        /*startAnimation(AnimationUtils.loadAnimation(getThemeActivity(),
                R.anim.bottom_up));*/
    }

    //取消录音
    private void releaseRecord() {
        //QdToast.show(getContext(),"取消录音");
        audioRecorder.release();
    }
}