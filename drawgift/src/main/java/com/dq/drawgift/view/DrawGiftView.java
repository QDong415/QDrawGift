package com.dq.drawgift.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import com.dq.drawgift.R;
import com.dq.drawgift.model.DrawGiftModel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

//手绘礼物的View
public class DrawGiftView extends View {

    //当前被画上去的全部的DrawGiftModel
    private List<DrawGiftModel> allDrawGiftArray;
    private float mLastX;
    private float mLastY;

    //每一笔的最后一个图的index，所以：这个List.size = 一共几笔
    private List<Integer> strokeFirstPositionArray;

    private DrawGiftListener onDrawGiftListener;

    //当前选中的礼物的id和bitmap
    private int currentGiftId;
    private Bitmap currentGiftBitmap;
    private float currentGiftPrice;

    //连续两个礼物之间的间距（像素），0表示用当前礼物的宽
    private int drawStrokeInterval;

    //是否可以画
    private boolean drawEnable = false;

    //没画的时候，显示的默认文本
    private String placeHolderText = "涂鸦模式，绘制你的图案";

    public DrawGiftView(Context context) {
        this(context, null);
    }

    public DrawGiftView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DrawGiftView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (!drawEnable) {
            //因为选了普通礼物，所以当前View不能画
            //这时候要清空之前画的内容（一定要draw一些东西，只调用invalidate的话无法清空）
            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
            return;
        }

        if (allDrawGiftArray.isEmpty()){
            float placeHolderViewHeight = 500;

            Bitmap mBitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.drawgift_placeholder);
            canvas.drawBitmap(mBitmap, (getWidth() - mBitmap.getWidth())/2, (getHeight() - placeHolderViewHeight) / 2, null);
            mBitmap.recycle();

            Paint textPaint = new Paint();
            textPaint.setStyle(Paint.Style.FILL);
            textPaint.setColor(0xffffffff);
            textPaint.setStrokeWidth(8);
            textPaint.setTextSize(42);
            textPaint.setTextAlign(Paint.Align.CENTER);

            if (placeHolderText != null){
                float baseline = (getHeight() - placeHolderViewHeight) / 2 + 340;
                canvas.drawText(placeHolderText, getWidth() / 2, baseline, textPaint);
            }

        } else {
            for (DrawGiftModel giftModel : allDrawGiftArray) {
                canvas.drawBitmap(giftModel.getGiftBitmap(), giftModel.getX() - giftModel.getGiftBitmap().getWidth() / 2, giftModel.getY() - giftModel.getGiftBitmap().getHeight() / 2, null);
                //这里肯定是不能recycle的
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                if (!drawEnable) {
                    //不允许画
                    return super.onTouchEvent(event);
                }

                if (currentGiftBitmap == null) {
                    //当前没选中任何礼物
                    return super.onTouchEvent(event);
                }

                //点下
                mLastX = checkIfBelongHorizontalEdge(event.getX());
                mLastY = checkIfBelongVerticalEdge(event.getY());
                addDrawGiftModel(mLastX, mLastY, currentGiftBitmap);
                invalidate();

                //记录这一笔的开头
                strokeFirstPositionArray.add(allDrawGiftArray.size() - 1);

                return true;
            case MotionEvent.ACTION_MOVE:
                float moveX = checkIfBelongHorizontalEdge(event.getX());
                float moveY = checkIfBelongVerticalEdge(event.getY());

                //距离上一个礼物的移动距离
                float distance = (float) (Math.pow(moveX - mLastX, 2) + Math.pow(moveY - mLastY, 2));
                //理应移动多少才需要画新礼物
                float reference = (float) Math.pow(drawStrokeInterval == 0 ? currentGiftBitmap.getWidth() : drawStrokeInterval, 2);

                if (distance >= reference) {
                    //距离拉开了，应该画新礼物
                    if (moveX >= getLeft() && moveX < getRight() && moveY >= getTop() && moveY < getBottom()){
                        //看看新礼物是否在View内
                        addDrawGiftModel(moveX, moveY, currentGiftBitmap);
                        mLastX = moveX;
                        mLastY = moveY;
                        invalidate();

                        if (onDrawGiftListener != null) {
                            onDrawGiftListener.onGiftPainted(this, currentGiftId);
                        }
                    } else {
                        //越界
                    }
                }
                break;
            case MotionEvent.ACTION_CANCEL:
                break;
            case MotionEvent.ACTION_UP:
                if (!drawEnable && onDrawGiftListener != null) {
                    onDrawGiftListener.onTouchEventUpWhenDrawDisable(this);
                }

                break;
        }
        return super.onTouchEvent(event);
    }

    private void addDrawGiftModel(float x, float y ,Bitmap bitmap){
        DrawGiftModel mDIYGiftModel = new DrawGiftModel();
        mDIYGiftModel.setGiftBitmap(bitmap);
        mDIYGiftModel.setgiftId(currentGiftId);
        mDIYGiftModel.setGiftPrice(currentGiftPrice);
        mDIYGiftModel.setX(x);
        mDIYGiftModel.setY(y);
        allDrawGiftArray.add(mDIYGiftModel);
    }

    //检查是否是屏幕边缘
    private float checkIfBelongHorizontalEdge(float intrinsicX){
        if (intrinsicX > getWidth() - (currentGiftBitmap.getWidth() / 2)){
            //超过右边界
            return getWidth() - (currentGiftBitmap.getWidth() / 2);
        } else if (intrinsicX < currentGiftBitmap.getWidth() / 2) {
            //超过左边界
            return currentGiftBitmap.getWidth() / 2;
        } else {
            return intrinsicX;
        }
    }

    //检查是否是屏幕边缘
    private float checkIfBelongVerticalEdge(float intrinsicY){
        if (intrinsicY > getHeight() - (currentGiftBitmap.getHeight() / 2)){
            //超过右边界
            return getHeight() - (currentGiftBitmap.getHeight() / 2);
        } else if (intrinsicY < currentGiftBitmap.getHeight() / 2) {
            //超过左边界
            return currentGiftBitmap.getHeight() / 2;
        } else {
            return intrinsicY;
        }
    }

    //清空当前画的所有礼物
    public void removeAll(){
        strokeFirstPositionArray.clear();
        allDrawGiftArray.clear();
        invalidate();
    }

    //移除最后一笔
    public void removeLastStroke(){
        if (strokeFirstPositionArray.isEmpty()){
            return;
        }

        int lastStrokeIndex = strokeFirstPositionArray.get(strokeFirstPositionArray.size() - 1);

        //先从全部礼物里删除最后一笔礼物
        for (int i = allDrawGiftArray.size() - 1; i >= lastStrokeIndex ; i--){
            allDrawGiftArray.remove(i);
        }

        //移除最后一笔的记录点
        strokeFirstPositionArray.remove(strokeFirstPositionArray.size() - 1);

        invalidate();
    }

    //设置当前这笔礼物
    public void setCurrentGift(int giftId, Bitmap giftBitmap, float giftPrice){
        this.currentGiftId = giftId;
        this.currentGiftBitmap = giftBitmap;
        this.currentGiftPrice = giftPrice;
    }

    //bottomSheetHeight是底部不能画的区域的高度
    public void showInActivityWindow(Activity activity, int bottomSheetHeight){
        WindowManager mWindowManager = activity.getWindowManager();
        WindowManager.LayoutParams wl = new WindowManager.LayoutParams();

        //用TYPE_APPLICATION 是本View所在的层级，如果太高了（超过了应用）还要申请权限
        wl.type = WindowManager.LayoutParams.TYPE_APPLICATION;

        //据说这个可以让触摸事件穿透到view层，但是我们的sheet是dialog，所以无用
//                      wl.flags = WindowManager.LayoutParams.FLAG_LOCAL_FOCUS_MODE;

        wl.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        wl.gravity = Gravity.TOP;//这个top会盖住titleBar，但是盖不住状态栏
        wl.width = WindowManager.LayoutParams.MATCH_PARENT;
        wl.height = activity.getWindow().getDecorView().findViewById(android.R.id.content).getHeight() - bottomSheetHeight;
        wl.token = activity.getWindow().getAttributes().token;
        wl.format = PixelFormat.TRANSLUCENT;//Toast就是用这个实现的透明
        mWindowManager.addView(this, wl);
    }

    @Override
    public void onAttachedToWindow(){
        super.onAttachedToWindow();
        allDrawGiftArray = new ArrayList<>();
        strokeFirstPositionArray = new ArrayList<>();
    }

    @Override
    public void onDetachedFromWindow(){
        super.onDetachedFromWindow();
        allDrawGiftArray.clear();
        strokeFirstPositionArray.clear();
    }

    @Override
    public void onFinishInflate() {
        super.onFinishInflate();
    }

    public List<DrawGiftModel> getAllDrawGiftArray(){
        return allDrawGiftArray;
    }

    //由于不同手机屏幕尺寸不同，如果礼物画在大屏幕的最右边（X很大）在小屏幕上绘制礼物的时候，X会超出屏幕。因此在这里转成百分比再上传给服务器
    public List<HashMap<String, String>> transformGiftArrayFitScreen(Context context){

        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((WindowManager) context.getApplicationContext().getSystemService(Context.WINDOW_SERVICE))
                .getDefaultDisplay().getMetrics(displayMetrics);

        //fixedArray是需要转成json发送给服务器的
        List<HashMap<String, String>> fixedArray = new ArrayList<>();
        float viewWidth = displayMetrics.widthPixels;
        float viewHeight = displayMetrics.heightPixels;
        HashMap<String, String> param = null;
        for (DrawGiftModel giftModel : allDrawGiftArray) {
            //x 和 y转为屏幕比例
            param = new HashMap<>();
            param.put("x",String.valueOf(giftModel.getX() / viewWidth));
            param.put("y",String.valueOf(giftModel.getY() / viewHeight));
            param.put("giftid",String.valueOf(giftModel.getgiftId()));
            fixedArray.add(param);
        }
        return fixedArray;
    }

    //连续两个礼物之间的间距（像素），0表示用当前礼物的宽
    public void setDrawStrokeInterval(int drawStrokeInterval) {
        this.drawStrokeInterval = drawStrokeInterval;
    }

    //还没画的时候，提示语
    public void setPlaceHolderText(String placeHolderText) {
        this.placeHolderText = placeHolderText;
        invalidate();
    }

    //是否可以画
    public void setDrawEnable(boolean drawEnable) {
        if (drawEnable != this.drawEnable){
            this.drawEnable = drawEnable;
            if (!drawEnable){
                allDrawGiftArray.clear();
                strokeFirstPositionArray.clear();
            }
            invalidate();
        }
    }

    public void setOnDrawGiftListener(DrawGiftListener onDrawGiftListener) {
        this.onDrawGiftListener = onDrawGiftListener;
    }

    public interface DrawGiftListener{
        //新的礼物节点被画上
        public void onGiftPainted(DrawGiftView drawGiftView, int giftId);
        //当drawDisable的情况下，触发的touchUp回调
        public void onTouchEventUpWhenDrawDisable(DrawGiftView drawGiftView);
    }
}
