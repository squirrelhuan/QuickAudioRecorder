package cn.demomaster.quickaudiorecorderlib.view.wechat;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.widget.TextView;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import cn.demomaster.quickaudiorecorderlib.R;
import cn.demomaster.quickaudiorecorderlib.util.DisplayUtil;

/**
 * 仿微信语音 波形图
 */
public class WechatAudioWaveView extends TextView {
    public WechatAudioWaveView(Context context) {
        super(context);
        init();
    }

    public WechatAudioWaveView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public WechatAudioWaveView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public WechatAudioWaveView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        data = new ArrayList<>();
        for(int i=0;i<count;i++){
            data.add(0.d);
        }
    }

    boolean showText;

    public void setShowText(boolean showText) {
        this.showText = showText;
        postInvalidate();
    }

    int maxHeight = 0;
    @Override
    protected void onDraw(Canvas canvas) {
        RectF rectF1 = new RectF(0, 0, getMeasuredWidth(), getMeasuredHeight());
        Paint paint1 = new Paint();
        paint1.setColor(getResources().getColor(R.color.wave_view_background));
        int radiu = DisplayUtil.dip2px(getContext(), 15);
        canvas.drawRoundRect(rectF1, radiu, radiu, paint1);

        if(showText) {
            super.onDraw(canvas);
        }else {
            int w = 5;
            int h = 15;
            int dx = 4;
            int left = (getMeasuredWidth() - (count * w + (count - 1) * dx)) / 2;
            maxHeight = h * 3;
            for (int i = 0; i < count; i++) {
                float value = (float) (getData(i) / max);
            /*if(value>1){
                value = (float) (Math.min(value,1));
            }else if(value<-1){
                value = (float) (Math.max(value,-1));
            }*/
                int h1 = (int) (h + maxHeight * value);
                int l = left + i * (w + dx);
                int t = getMeasuredHeight() / 2 - h1 / 2;
                int r = l + w;
                int b = t + h1;
                RectF rectF = new RectF(l, t, r, b);
                Paint paint = new Paint();
                paint.setColor(Color.WHITE);
                paint.setAntiAlias(true);
                canvas.drawRoundRect(rectF, w / 2, w / 2, paint);
            }
        }
    }

    private double getMaxValueData() {
        double max = 0;
        for(double value:data){
            if(value>max){
                max = value;
            }
        }
        return max;
    }

    private double getMinValueData() {
        double min = 0;
        for(double value:data){
            if(value<min){
                min = value;
            }
        }
        return min;
    }
    private double getAvgValueData() {
        double min = 0;
        for(double value:data){
            min += value;
        }
        return min/data.size();
    }

    private double getData(int i) {
        if(i<data.size()){
            return data.get(i);
        }
        return 0;
    }

    int count = 25;
    List<Double> data;
    double max = 100;
    public void addData(double value) {
        if(data.size()>count){
            data.remove(0);
        }
        data.add(value);
        /*if (d > max) {
            max = getAvgValueData();
        }*/
        max = Math.max(10, getAvgValueData())*4;
        postInvalidate();
    }
}
