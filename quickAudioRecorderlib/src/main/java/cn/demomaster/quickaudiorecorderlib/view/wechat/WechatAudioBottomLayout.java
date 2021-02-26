package cn.demomaster.quickaudiorecorderlib.view.wechat;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;


/**
 * 仿微信语音 按压时的底部按钮
 */
public class WechatAudioBottomLayout extends LinearLayout {
    public WechatAudioBottomLayout(Context context) {
        super(context);
        init();
    }

    public WechatAudioBottomLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public WechatAudioBottomLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public WechatAudioBottomLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        setBackgroundColor(Color.TRANSPARENT);
        //nomalColor = getResources().getColor(R.color.transparent_dark_55);
    }

    boolean isActivated;

    @Override
    public void setActivated(boolean activated) {
        isActivated = activated;
        postInvalidate();
    }

    int nomalColor = 0xff494949;
    int activateColor = Color.WHITE;
    public void setActivateColor(int activateColor) {
        this.activateColor = activateColor;
        postInvalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int color = nomalColor;
        if(isActivated){
            color = activateColor;
        }
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(color);
        paint.setAntiAlias(true);

        int r = getMeasuredWidth()*3;
        int x = getMeasuredWidth()/2;
        int y = r;

        canvas.drawCircle(x,y,r,paint);
        //canvas.drawRect(new Rect(0,0,100,100),paint);
    }
}
