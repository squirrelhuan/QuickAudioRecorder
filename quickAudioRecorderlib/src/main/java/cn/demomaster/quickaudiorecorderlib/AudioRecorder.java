package cn.demomaster.quickaudiorecorderlib;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 实现录音
 *
 * @author chenmy0709
 * @version V001R001C01B001
 */
public class AudioRecorder {
    //音频输入-麦克风
    private final static int AUDIO_INPUT = MediaRecorder.AudioSource.MIC;
    //采用频率
    //44100是目前的标准，但是某些设备仍然支持22050，16000，11025
    //采样频率一般共分为22.05KHz、44.1KHz、48KHz三个等级
    private final static int AUDIO_SAMPLE_RATE = 16000;
    //声道 单声道
    private final static int AUDIO_CHANNEL = AudioFormat.CHANNEL_IN_MONO;
    //编码
    private final static int AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    // 缓冲区字节大小
    private int bufferSizeInBytes = 0;

    //录音对象
    private AudioRecord audioRecord;

    //录音状态
    private Status status = Status.STATUS_IDLE;

    //文件名
    private String fileName;

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    //录音文件
    private List<String> filesName = new ArrayList<>();
    String fileOutputPath;

    /**
     * 设置输出文件路径
     *
     * @param path
     */
    public void setOutputFilePath(String path) {
        this.fileOutputPath = path;
    }

    /**
     * 类级的内部类，也就是静态类的成员式内部类，该内部类的实例与外部类的实例
     * 没有绑定关系，而且只有被调用时才会装载，从而实现了延迟加载
     */
    private static class AudioRecorderHolder {
        /**
         * 静态初始化器，由JVM来保证线程安全
         */
        private static AudioRecorder instance = new AudioRecorder();
    }

    private AudioRecorder() {
    }

    public static AudioRecorder getInstance() {
        return AudioRecorderHolder.instance;
    }

    private Context mContext;
    public static final int TIMER_INTERVAL = 100;

    /**
     * 创建录音对象
     */
    public void createAudio(Context context, int audioSource, int sampleRateInHz, int channelConfig, int audioFormat) {
        // 获得缓冲区字节大小
        bufferSizeInBytes = AudioRecord.getMinBufferSize(sampleRateInHz,
                channelConfig, audioFormat);
        audioRecord = new AudioRecord(audioSource, sampleRateInHz, channelConfig, audioFormat, bufferSizeInBytes);
        status = Status.STATUS_IDLE;
        mContext = context;
        fileOutputPath = mContext.getFilesDir().getAbsolutePath();

        int bSamples = 8;
        if (audioFormat == AudioFormat.ENCODING_PCM_16BIT) {
            bSamples = 16;
        } else {
            bSamples = 8;
        }

        int nChannels = 1;
        if (channelConfig == AudioFormat.CHANNEL_IN_MONO) {
            nChannels = 1;
        } else {
            nChannels = 2;
        }
        int framePeriod = sampleRateInHz * TIMER_INTERVAL / 1000;
        int bufferSize = framePeriod * 2 * bSamples * nChannels / 8;
    }

    /**
     * 创建默认的录音对象
     */
    public void createAudio(Context context) {
        createAudio(context, AUDIO_INPUT, AUDIO_SAMPLE_RATE, AUDIO_CHANNEL, AUDIO_ENCODING);
    }

    RecordStreamListener mRecordStreamListener;

    public void setRecordStreamListener(RecordStreamListener recordStreamListener) {
        this.mRecordStreamListener = recordStreamListener;
    }

    /**
     * 开始录音
     */
    public void startRecord() {
        if (status == Status.STATUS_PAUSE) {
            throw new IllegalStateException("录音尚未初始化,请检查是否禁止了录音权限~");
        }
        if (status == Status.STATUS_START) {
            throw new IllegalStateException("正在录音");
        }
        if (TextUtils.isEmpty(fileName)) {
            fileName = new SimpleDateFormat("yyyyMMddhhmmss").format(new Date());
        }
        
        Log.d("AudioRecorder", "startRecord：" + audioRecord.getState());
        audioRecord.startRecording();
        
        if (mRecordStreamListener != null) {
            mRecordStreamListener.onStart();
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                readData();
            }
        }).start();
    }

    /**
     * 暂停录音
     */
    public void pauseRecord() {
        Log.d("AudioRecorder", "===pauseRecord===");
        if (isStarted()) {
            audioRecord.stop();
            status = Status.STATUS_PAUSE;
            if (mRecordStreamListener != null) {
                mRecordStreamListener.onPause();
            }
        }
    }

    /**
     * 停止录音
     */
    public void stopRecord() {
        Log.d("AudioRecorder", "===stopRecord===");
        if (isStarted()) {
            audioRecord.stop();
            status = Status.STATUS_STOP;
            //假如有暂停录音
            List<String> filePaths = new ArrayList<>();
            try {
                if (filesName.size() > 0) {
                    for (String fileName : filesName) {
                        filePaths.add(fileOutputPath+"/pcm/"+fileName);
                    }
                    //清除
                    filesName.clear();
                }
            } catch (IllegalStateException e) {
                throw new IllegalStateException(e.getMessage());
            }

            //将多个pcm文件转化为wav文件
            mergePCMFilesToWAVFile(filePaths, new OnGenerateFileListener() {
                @Override
                public void onFinish(boolean isSuccess, String filePath) {
                    if (mRecordStreamListener != null) {
                        mRecordStreamListener.onStop(isSuccess,filePath);
                    }
                }
            });
        }
    }

    /**
     * 释放资源
     */
    public void release() {
        Log.d("AudioRecorder", "===release===");

        if (audioRecord != null) {
            audioRecord.release();
            audioRecord = null;
        }

        if (mRecordStreamListener != null) {
            mRecordStreamListener.onRelease();
        }
        status = Status.STATUS_IDLE;
    }

    /**
     * 取消录音
     */
    public void canel() {
        filesName.clear();
        fileName = null;
        if (audioRecord != null) {
            audioRecord.release();
            audioRecord = null;
        }
        status = Status.STATUS_IDLE;
    }

    /**
     * byte[]转int
     *
     * @param bytes 需要转换成int的数组
     * @return int值
     */
    public static int byteArrayToInt(byte[] bytes) {
        int value = 0;
        for (int i = 0; i < bytes.length; i++) {
            int shift = (bytes.length - 1 - i) * 8;
            value += (bytes[i] & 0xFF) << shift;
        }
        return value;
    }

    /**
     * 将音频信息写入文件
     */
    private void readData() {
        // new一个byte数组用来存一些字节数据，大小为缓冲区大小
        byte[] audiodata = new byte[bufferSizeInBytes];
        FileOutputStream fos = null;
        int readsize = 0;
        try {
            String currentFileName = fileName;
            if (status == Status.STATUS_PAUSE) {
                //假如是暂停录音 将文件名后面加个数字,防止重名文件内容被覆盖
                currentFileName += filesName.size();
            }
            filesName.add(currentFileName);
            File filePath = new File(fileOutputPath+"/pcm/");
            if (!filePath.exists()) {
                filePath.mkdirs();
            }
            File file = new File(fileOutputPath+"/pcm/", currentFileName);
            if (file.exists()) {
                file.delete();
            }
            fos = new FileOutputStream(file);// 建立一个可存取字节的文件
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        //将录音状态设置成正在录音状态
        status = Status.STATUS_START;
        while (status == Status.STATUS_START&&audioRecord!=null) {
            readsize = audioRecord.read(audiodata, 0, bufferSizeInBytes);
            // byte[] firstBytes = new byte[2];
            //System.arraycopy(audiodata, 0, firstBytes, 0, firstBytes.length);
            //int a =byteArrayToInt(firstBytes);
            //Log.e("AudioRecorder", "len="+audiodata.length+",a="+a);
            if (AudioRecord.ERROR_INVALID_OPERATION != readsize && fos != null) {
                try {
                    fos.write(audiodata);
                    if (mRecordStreamListener != null) {
                        //用于拓展业务
                        mRecordStreamListener.recordOfByte(audiodata, 0, audiodata.length);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        try {
            if (fos != null) {
                fos.close();// 关闭写入流
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 将pcm合并成wav
     *
     * @param filePaths
     */
    private void mergePCMFilesToWAVFile(final List<String> filePaths,OnGenerateFileListener listener) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String save = fileOutputPath + "/wav/" + fileName + ".wav";
                boolean result = AudioUtil.mergePCMFilesToWAVFile(filePaths, save);
                fileName = null;
                if(listener!=null){
                    Log.i("AudioRecorder", "录音结果 :" + result + "," + save);
                    listener.onFinish(result,save);
                }
            }
        }).start();
    }

    public interface OnGenerateFileListener{
        void onFinish(boolean isSuccess,String filePath);
    }


    /**
     * 获取录音对象的状态
     *
     * @return
     */
    public Status getStatus() {
        return status;
    }

    /**
     * 获取本次录音文件的个数
     *
     * @return
     */
    public int getPcmFilesCount() {
        return filesName.size();
    }

    /**
     * 录音对象的状态
     */
    public enum Status {
        //未开始
        STATUS_IDLE(0),
        //录音
        STATUS_START(1),
        //暂停
        STATUS_PAUSE(2),
        //停止
        STATUS_STOP(3);

        private int value = 0;

        Status(int value) {//必须是private的，否则编译错误
            this.value = value;
        }

        public int value() {
            return this.value;
        }
    }

    public boolean isStarted() {
        return getStatus() == AudioRecorder.Status.STATUS_START;
    }

}