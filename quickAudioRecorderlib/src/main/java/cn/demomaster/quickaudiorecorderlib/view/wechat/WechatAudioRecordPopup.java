package cn.demomaster.quickaudiorecorderlib.view.wechat;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import java.lang.reflect.Field;

import cn.demomaster.quickaudiorecorderlib.R;


public class WechatAudioRecordPopup extends PopupWindow {
    private Activity mContext;

    public WechatAudioRecordPopup(Context context) {
        super(context);
        mContext = (Activity) context;
        fitPopupWindowOverStatusBar(this,true);
        init();
    }

    public WechatAudioRecordPopup(View contentView, int width, int height) {
        super(contentView, width, height);
        mContext = (Activity) contentView.getContext();
        init();
    }

    WechatAudioRecordViewGroup audioRecordViewGroup;

    /**
     * 获取要触摸的view
     *
     * @return
     */
    public View getTouchableViewGroup() {
        return audioRecordViewGroup;
    }

    //TextView tv_cancel_tip;
    WechatAudioWaveView wechatAudioWaveView;

    private void init() {
        View view = mContext.getLayoutInflater().inflate(R.layout.item_wechat_popup_audio_record,
                null);
        audioRecordViewGroup = view.findViewById(R.id.audioRecordViewGroup);

        LinearLayout wechatAudioBottomLayout = view.findViewById(R.id.wechatAudioBottomLayout);
        wechatAudioBottomLayout.setActivated(true);
        //tv_cancel_tip = view.findViewById(R.id.tv_cancel_tip);
        //tv_cancel_tip.setVisibility(View.INVISIBLE);
        TextView tv_tip_bottom = view.findViewById(R.id.tv_tip_bottom);
        wechatAudioWaveView = view.findViewById(R.id.wechatAudioWaveView);
        audioRecordViewGroup.setOnTouchCanelListener(new WechatAudioRecordViewGroup.OnTouchListener() {
            @Override
            public void onHover(float x, float y, View view, boolean isActivated) {
                if (view != null && view.getId() == R.id.wechatAudioBottomLayout) {
                    view.setActivated(isActivated);
                    if (isActivated) {
                        tv_tip_bottom.setVisibility(View.VISIBLE);
                        wechatAudioWaveView.setShowText(false);
                        wechatAudioWaveView.setVisibility(View.VISIBLE);
                        //tv_cancel_tip.setVisibility(View.INVISIBLE);
                        //tv_cancel_tip.setTextColor(Color.WHITE);
                        //tv_cancel_tip.setBackground(view.getContext().getResources().getDrawable(R.drawable.rect_round_transparent_bg));
                    } else {
                        tv_tip_bottom.setVisibility(View.GONE);
                        wechatAudioWaveView.setShowText(true);
                        //tv_cancel_tip.setVisibility(View.VISIBLE);
                        //tv_cancel_tip.setTextColor(Color.BLACK);
                        //tv_cancel_tip.setBackground(view.getContext().getResources().getDrawable(R.drawable.rect_round_white_bg));
                    }
                }
            }

            @Override
            public void onCancel(float x, float y, View view) {
                if (onRecordListener != null) {
                    if (view != null && view.getId() == R.id.wechatAudioBottomLayout) {
                        //合成语音并准备发送
                        onRecordListener.onFinish();
                    } else {
                        onRecordListener.onCancel();
                    }
                }
                dismiss();
            }
        });
        audioRecordViewGroup.setFocusableInTouchMode(true);
        audioRecordViewGroup.requestFocus();
        setContentView(view);
    }

    View mContentView;

    public View getContentView() {
        return mContentView;
    }

    @Override
    public void setContentView(View contentView) {
        mContentView = contentView;
        super.setContentView(contentView);
    }

    /**
     * 设置代理按钮的坐标信息
     *
     * @param location
     */
    public void setButtonLocation(int[] location) {
        audioRecordViewGroup.setButtonLocation(location);
    }

    public void setVolume(double myVolume) {
        wechatAudioWaveView.addData(myVolume);
    }

    OnRecordListener onRecordListener;

    public void setOnRecordListener(OnRecordListener onRecordListener) {
        this.onRecordListener = onRecordListener;
    }

    public static interface OnRecordListener {
        //录音结束
        void onFinish();

        //录音取消
        void onCancel();
    }
    public static void fitPopupWindowOverStatusBar(PopupWindow mPopupWindow, boolean needFullScreen) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            try {
                Field mLayoutInScreen = PopupWindow.class.getDeclaredField("mLayoutInScreen");
                mLayoutInScreen.setAccessible(needFullScreen);
                mLayoutInScreen.set(mPopupWindow, needFullScreen);
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }
}
