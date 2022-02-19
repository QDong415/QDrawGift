package com.dq.drawgift.view;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.dq.drawgift.model.DrawGiftModel;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class DrawGiftPlayView extends View {

    //需要被画上的全部的礼物，采用链表的方式，每次取第0个
    private LinkedList<List<DrawGiftModel>> allDrawGiftsLinkedList;

    //当前这幅画播放到第几个礼物
    private int currentGiftShowIndex;

    private DrawAnimationListener onDrawAnimationListener;

    //为了体现最后的消失动画效果，才引入了这个Paint
    private Paint mPaint;

    private static final int DRAW_ONE_GIFT = 0;

    private Handler mHandler;

    private Handler.Callback mCallback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case DRAW_ONE_GIFT:
                    //画一个礼物
                    currentGiftShowIndex = msg.arg1;
                    invalidate();

                    if (currentGiftShowIndex == allDrawGiftsLinkedList.getFirst().size()){
                        //进入这里，说明已经播放完最后一个礼物了
                        //播放扩大动画，然后消失
                        ValueAnimator valueAnimator = ValueAnimator.ofFloat(1, 2);
                        valueAnimator.setDuration(500);
                        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                            @Override
                            public void onAnimationUpdate(ValueAnimator animation) {
                                float p = (float) animation.getAnimatedValue();
                                //透明效果（所有礼物都做这个动作）
                                mPaint.setAlpha((int)((2.0F - p) * 255));
                                //放大效果（所有礼物都做这个动作）
                                for (DrawGiftModel giftModel : allDrawGiftsLinkedList.getFirst()) {

                                    giftModel.getMatrix().reset();
                                    giftModel.getMatrix().preTranslate(giftModel.getX() - giftModel.getGiftBitmap().getWidth() / 2F, giftModel.getY() - giftModel.getGiftBitmap().getHeight() / 2F);
                                    giftModel.getMatrix().postScale((float) (1.0F + (p - 1) * 0.4), (float) (1.0F + (p - 1) * 0.4), giftModel.getX(), giftModel.getY());
                                }

                                invalidate();
                            }
                        });
                        valueAnimator.addListener(new Animator.AnimatorListener(){

                            @Override
                            public void onAnimationStart(Animator animation) {

                            }

                            @Override
                            public void onAnimationEnd(Animator animation) {
                                //放大动画结束
                                mPaint.reset();

                                currentGiftShowIndex = 0;
                                allDrawGiftsLinkedList.removeFirst();

                                if (onDrawAnimationListener != null) {
                                    //当前的这一幅画结束
                                    onDrawAnimationListener.onAnimationNodeEnd(DrawGiftPlayView.this);

                                    if (allDrawGiftsLinkedList.isEmpty()){
                                        //所有画全部结束
                                        onDrawAnimationListener.onAnimationAllOver(DrawGiftPlayView.this);
                                    } else {
                                        //还有画没播完，继续画下一幅画
                                        //要先清理掉画布上的上一幅画
                                        invalidate();

                                        Message message = Message.obtain();
                                        message.arg1 = 0;
                                        message.what = DRAW_ONE_GIFT;
                                        mHandler.sendMessageDelayed(message, 100);
                                    }
                                }
                            }

                            @Override
                            public void onAnimationCancel(Animator animation) {

                            }

                            @Override
                            public void onAnimationRepeat(Animator animation) {

                            }
                        });
                        valueAnimator.start();
                    } else {
                        //继续画下一个礼物
                        Message message = Message.obtain();
                        message.arg1 = currentGiftShowIndex + 1;
                        message.what = DRAW_ONE_GIFT;
                        mHandler.sendMessageDelayed(message, 100);
                    }

                    return true;
            }
            return true;
        }
    };

    public DrawGiftPlayView(Context context) {
        this(context, null);
    }

    public DrawGiftPlayView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DrawGiftPlayView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mHandler = new Handler(mCallback);
        allDrawGiftsLinkedList = new LinkedList<List<DrawGiftModel>>();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int size = allDrawGiftsLinkedList.getFirst().size();
        for(int i = 0; i <= currentGiftShowIndex && i < size; i++) {
            DrawGiftModel giftModel = allDrawGiftsLinkedList.getFirst().get(i);
            canvas.drawBitmap(giftModel.getGiftBitmap(), giftModel.getMatrix(), mPaint);
        }
    }

    public void addDrawGifts(List<DrawGiftModel> currentDrawGiftArray, boolean insertToFirst){

        //处理每个小bitmap的大小和位置
        for(int i = 0; i < currentDrawGiftArray.size(); i++) {
            DrawGiftModel giftModel = currentDrawGiftArray.get(i);

            giftModel.getMatrix().reset();
            giftModel.getMatrix().postTranslate(giftModel.getX() - giftModel.getGiftBitmap().getWidth() / 2F, giftModel.getY() - giftModel.getGiftBitmap().getHeight() / 2F);
        }

        //添加到队列
        if (insertToFirst){
            if (allDrawGiftsLinkedList.size() > 0){
                //说明队列里已经有画在draw了。
                //由于我目前的逻辑是：取first节点，画完了再remove；而不是先remove再设置为全局变量。所以要插入到第1个元素
                allDrawGiftsLinkedList.add(1, currentDrawGiftArray);
            } else {
                //说明当前列表是空的，直接插入到表头
                allDrawGiftsLinkedList.offerFirst(currentDrawGiftArray);
            }
        } else {
            //插入到表尾
            allDrawGiftsLinkedList.offerLast(currentDrawGiftArray);
        }

        if (allDrawGiftsLinkedList.size() == 1){
            //刚插入完，size==1，说明是刚开始，这时候要开始播放
            List<DrawGiftModel> firstDrawGiftArray = this.allDrawGiftsLinkedList.getFirst();

            if (firstDrawGiftArray != null){
                //说明有需要播放的，开始draw
                Message message = Message.obtain();
                message.arg1 = 0;
                message.what = DRAW_ONE_GIFT;
                mHandler.sendMessage(message);
            }
        }
    }

    @Override
    public void onAttachedToWindow(){
        super.onAttachedToWindow();
        mPaint = new Paint();
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mHandler.removeCallbacksAndMessages(null);
        mPaint.reset();
        currentGiftShowIndex = 0;
    }

    public void setOnDrawAnimationListener(DrawAnimationListener onDrawAnimationListener) {
        this.onDrawAnimationListener = onDrawAnimationListener;
    }

    public interface DrawAnimationListener{
        //礼物动画全部结束
        public void onAnimationNodeEnd(DrawGiftPlayView drawGiftPlayView);
        //礼物动画当前的这一幅画结束
        public void onAnimationAllOver(DrawGiftPlayView drawGiftPlayView);
    }
}
