package cn.demomaster.quickaudiorecorderlib;

/**
 * 获取录音的音频流,用于拓展的处理
 * @author chenmy0709
 * @version V001R001C01B001
 */
public interface RecordStreamListener {
    void onStart();
    void onPause();
    void onStop(boolean isSuccess, String filePath);
    void onRelease();
    void recordOfByte(byte[] data, int begin, int end);
}
