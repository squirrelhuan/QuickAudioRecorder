package cn.demomaster.quickaudiorecorderlib.view.wechat;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

/**
 * 仿微信语音消息控件
 */
public class WechatAudioRecordViewGroup extends RelativeLayout {

    public WechatAudioRecordViewGroup(Context context) {
        super(context);
        init();
    }

    public WechatAudioRecordViewGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public WechatAudioRecordViewGroup(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public WechatAudioRecordViewGroup(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        setBackgroundColor(Color.TRANSPARENT);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Paint paint = new Paint();
        Shader mShader = new LinearGradient(0,0,0,getMeasuredHeight(),new int[] {0x55000000,0xff000000},null,Shader.TileMode.REPEAT);
//新建一个线性渐变，前两个参数是渐变开始的点坐标，第三四个参数是渐变结束的点的坐标。连接这2个点就拉出一条渐变线了，玩过PS的都懂。然后那个数组是渐变的颜色。下一个参数是渐变颜色的分布，如果为空，每个颜色就是均匀分布的。最后是模式，这里设置的是循环渐变
        //mShader = new RadialGradient(0,0,0,new int[] {0xccffffff,0x11ffffff},new float[]{0.3f,1f},Shader.TileMode.REPEAT);
        paint.setShader(mShader);
        canvas.drawRect(new Rect(0,0,getMeasuredWidth(),getMeasuredHeight()),paint);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed,l,t,r,b);
    }

    int[] location_button = new int[2];

    /**
     * 设置触发按钮绝对位置
     * @param location_button
     */
    public void setButtonLocation(int[] location_button) {
        this.location_button = location_button;
    }

    boolean isPressed;
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int[] myLocation = new int[2];
        getLocationOnScreen(myLocation);//获取在整个屏幕内的绝对坐标
        //计算相对位置
        int dx = location_button[0]-myLocation[0];
        int dy = location_button[1]-myLocation[1];
        float X = event.getX()+dx;
        float Y = event.getY()+dy;

        //QDLogger.e("WechatAudioRecordViewGroup onTouchEvent="+X+","+Y+",index="+event.getActionIndex());
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                //QDLogger.e("WechatAudioRecordViewGroup onTouchEvent index="+event.getActionIndex());
                if(isPressed){
                    return super.onTouchEvent(event);
                }
                isPressed = true;
                return true;
            case MotionEvent.ACTION_MOVE:
                activiteChildView(X,Y);
                break;
            case MotionEvent.ACTION_POINTER_2_UP://第二个手指抬起的时候
                //needToHandle=true;
                break;
            default:
                break;
        }
        if(event.getAction()==MotionEvent.ACTION_UP||event.getAction()==MotionEvent.ACTION_CANCEL){
            onCancel(X,Y);
        }
        return super.onTouchEvent(event);
    }

    /**
     * 手指释放
     * @param
     */
    private void onCancel(float x, float y) {
        isPressed = false;
        View view = null;
        W:for(int i=0;i<getChildCount();i++){
            int[] location = new int[2];
            View child = getChildAt(i);
            child.getLocationInWindow(location);//获取在整个屏幕内的绝对坐标
            if(location[0]<x&&location[0]+child.getMeasuredWidth()>x){
                boolean b = (location[1]<y&&location[1]+child.getMeasuredHeight()>y);
                if(b){
                    view = child;
                    break W;
                }
            }
        }
        if(onTouchListener!=null){
            onTouchListener.onCancel(x, y, view);
        }
        setButtonLocation(new int[2]);
    }

    /**
     * 激活当前选中的子控件
     * @param x
     * @param y
     */
    private void activiteChildView(float x, float y) {
        for(int i=0;i<getChildCount();i++){
            int[] location = new int[2];
            View child = getChildAt(i);
            child.getLocationInWindow(location);//获取在整个屏幕内的绝对坐标
            if(location[0]<x&&location[0]+child.getMeasuredWidth()>x){
                boolean isSelected = (location[1]<y&&location[1]+child.getMeasuredHeight()>y);
                if(onTouchListener!=null){
                    onTouchListener.onHover(x,y,child,isSelected);
                }
            }
        }
    }
    OnTouchListener onTouchListener;
    /**
     * 设置取消触摸事件
     * @param onTouchListener
     */
    public void setOnTouchCanelListener(OnTouchListener onTouchListener) {
        this.onTouchListener = onTouchListener;
    }

    public static interface OnTouchListener{
        //触摸移动,悬停在某个子空间上
        void onHover(float x, float y,View view,boolean isSelected);
        //手指释放
        void onCancel(float x, float y,View view);
    }
}
