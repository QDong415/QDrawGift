package com.dq.drawgiftdemo;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.FrameLayout;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.FutureTarget;
import com.dq.drawgift.model.DrawGiftModel;
import com.dq.drawgift.view.DrawGiftPlayView;
import com.dq.drawgift.view.DrawGiftView;
import com.dq.drawgiftdemo.model.GiftBean;
import com.dq.drawgiftdemo.dialogsheet.BottomGiftSheetBuilder;
import com.dq.drawgiftdemo.dialogsheet.QBottomSheet;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;

public class LiveActivity extends AppCompatActivity {

    private List<GiftBean> giftBeanList;

    //底部的礼物弹框
    private BottomGiftSheetBuilder giftSheetBuilder;

    //画礼物的背景View（透明的，并不是灰底）
    private DrawGiftView drawGiftView;

    //播放礼物动画的层
    private DrawGiftPlayView playView;

    //为了节省内存。通过giftId当做Key来缓存Bitmap（如果你的giftId是String，那么这里要修改成HashMap<String,Bitmap>）
    //其实这个东西你可以做成单例。我现在没用单例，就需要每次进入直播间都重新缓存
    private SparseArray<Bitmap> cacheBitmapByGiftIdMap;

    private MyHandler handler = new MyHandler(this);
    private static class MyHandler extends Handler {

        private WeakReference<Context> reference;

        public MyHandler(Context context) {
            reference = new WeakReference<Context>(context);
        }

        @Override
        public void handleMessage(Message msg) {
            final LiveActivity activity = (LiveActivity) reference.get();
            if(activity == null){
                return;
            }
            switch (msg.what) {
                case 1:
                    List<DrawGiftModel> allDrawGiftArray = (List<DrawGiftModel>) msg.obj;
                    activity.playDrawGift(allDrawGiftArray, msg.arg1 == 1);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live);

        //本地的 giftBeanList，你肯定是需要从缓存里取出来的，至于什么时候刷新，看你们的逻辑
        giftBeanList = new ArrayList<GiftBean>();

        //key是giftId，value是Bitmap。为了不重复生成Bitmap
        cacheBitmapByGiftIdMap = new SparseArray<Bitmap>();

        TestData.createGiftBean(giftBeanList);

        findViewById(R.id.show_gift_sheet_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //底部礼物弹框，本Demo只是提供这样一种底部弹框的方法，你可以按你自己的想法实现底部弹框
                lazyGiftSheetBuilder();

                //显示底部礼物弹框。
                giftSheetBuilder.mDialog.show();

                giftSheetBuilder.mDialog.getContentView().getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        //正常会连续回调两次。remove防止回调两次
                        giftSheetBuilder.mDialog.getContentView().getViewTreeObserver().removeOnGlobalLayoutListener(this);

                        //懒加载DrawView层
//                        lazyDrawGiftView();

                        //正式显示DrawView层
                        drawGiftView.showInActivityWindow(LiveActivity.this, giftSheetBuilder.mDialog.getContentView().getHeight());
                    }
                });
            }
        });

        findViewById(R.id.receive_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //模拟收到普通礼物（队列）
                prepareShowDrawGift(TestData.createRandomGifts(false), false);
            }
        });

        findViewById(R.id.receive_vip_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //模拟收到普通礼物（队列）
                prepareShowDrawGift(TestData.createRandomGifts(true) , true);
            }
        });
    }

    private DrawGiftView lazyDrawGiftView(){
        if (drawGiftView == null){
            drawGiftView = new DrawGiftView(LiveActivity.this);
            drawGiftView.setOnDrawGiftListener(new DrawGiftView.DrawGiftListener() {
                @Override
                public void onGiftPainted(DrawGiftView drawGiftView, int giftId) {
                    //有礼物被画上
                    //计算需要的金币并setText
                    resetNeedPriceDisplay();
                }

                @Override
                public void onTouchEventUpWhenDrawDisable(DrawGiftView drawGiftView) {
                    giftSheetBuilder.mDialog.dismiss();
                }
            });

            GiftBean selectedDrawGiftBean = giftSheetBuilder.getSelectedDrawGiftBean();
            if (selectedDrawGiftBean != null){
                //有默认选中的礼物
                drawGiftView.setCurrentGift(selectedDrawGiftBean.getGiftId() ,
                        obtainThumbBitmap(selectedDrawGiftBean.getGiftId() ,giftSheetBuilder.getSelectedDrawGiftLargeBitmap()),
                        selectedDrawGiftBean.getPrice());

                drawGiftView.setDrawEnable(selectedDrawGiftBean.drawEnable());
            }
        }
        return drawGiftView;
    }

    private BottomGiftSheetBuilder lazyGiftSheetBuilder(){
        if (giftSheetBuilder == null){
            giftSheetBuilder = new BottomGiftSheetBuilder(LiveActivity.this);

            QBottomSheet bottomSheet = giftSheetBuilder.build();

            giftSheetBuilder.setGiftList(LiveActivity.this, giftBeanList
                    , new BottomGiftSheetBuilder.BottomGiftSheetListener() {
                        @Override
                        public void onGiftSheetShow(BottomGiftSheetBuilder bottomGiftSheetBuilder) {
                            lazyDrawGiftView();
                            //问：为啥不在底部弹框的onShow回调里就addView(drawGiftView)？而是要在onGlobalLayout里addView？
                            //答：因为onShow里还不知道dialog的contentView的高度

                            //问：为啥要知道dialog的contentView的高度？
                            //答：因为要设置drawGiftView的高度 = screenHeight - contentView.height

                            //问：为啥不能把drawGiftView的高度无脑设置为MATCH_PARENT？
                            //答：因为onTouchEvent无法从drawGiftView的Window层 分发到dialog的Window层

                            //问：为啥不把drawGiftView添加到decorView层 而是要添加到window层？
                            //答：因为系统的dialog和popupView都是添加到window层，window层高于decorView层，dialog把touch事件消费了
                        }

                        @Override
                        public void onGiftSheetDismiss(BottomGiftSheetBuilder bottomGiftSheetBuilder) {
                            //底部弹框消失
                            //移除掉draw礼物View层。
//                                FrameLayout contentParent = (FrameLayout) getWindow().getDecorView().findViewById(android.R.id.content);
                            WindowManager mWindowManager = getWindowManager();
                            mWindowManager.removeView(drawGiftView);
                            //如果不 = null，leakCanary会报内存泄漏，但其实是误报
                            drawGiftView = null;
                        }

                        @Override
                        public void onGiftSelect(BottomGiftSheetBuilder bottomGiftSheetBuilder, GiftBean giftBean, int position, Bitmap giftBitmap) {
                            if (giftBean == null){
                                //没选中任何礼物
                                drawGiftView.setCurrentGift(0, null, 0);
                                drawGiftView.setDrawEnable(false);
                                bottomGiftSheetBuilder.costCoinTv.setText("消耗金币：0.0");
                            } else {
                                drawGiftView.setDrawEnable(giftBean.drawEnable());
                                //点选了一个礼物
                                if (giftBean.drawEnable()) {
                                    //点选了可画的礼物

                                    //bitmap缩放到指定大小
                                    drawGiftView.setCurrentGift(giftBean.getGiftId(), obtainThumbBitmap(giftBean.getGiftId(), giftBitmap), giftBean.getPrice());

                                    bottomGiftSheetBuilder.revokeIv.setVisibility(View.VISIBLE);
                                    bottomGiftSheetBuilder.deleteIv.setVisibility(View.VISIBLE);
                                    //计算需要的金币并setText
                                    resetNeedPriceDisplay();
                                } else {
                                    //点选了普通礼物
                                    bottomGiftSheetBuilder.revokeIv.setVisibility(View.INVISIBLE);
                                    bottomGiftSheetBuilder.deleteIv.setVisibility(View.INVISIBLE);
                                    bottomGiftSheetBuilder.costCoinTv.setText("消耗金币："+giftBean.getPrice());
                                }
                            }
                        }

                        @Override
                        public void onDrawRevokeClick(BottomGiftSheetBuilder bottomGiftSheetBuilder) {
                            // 点击撤回按钮
                            drawGiftView.removeLastStroke();
                            //计算需要的金币并setText
                            resetNeedPriceDisplay();
                        }

                        @Override
                        public void onDrawDeleteClick(BottomGiftSheetBuilder bottomGiftSheetBuilder) {
                            // 点击清空按钮
                            drawGiftView.removeAll();
                            //计算需要的金币并setText
                            resetNeedPriceDisplay();
                        }

                        @Override
                        public void onGiftSend(BottomGiftSheetBuilder bottomGiftSheetBuilder, GiftBean giftBean) {
                            //点赠送按钮
                            if (giftBean == null) return;

                            if (giftBean.drawEnable()){
                                //是那种画的礼物

                                //这样获取每个礼物对应屏幕的x y位置比例（0到1，比如0.5就是正中间），转成json，发给你的服务器
                                List<HashMap<String, String>> drawFixedArray = drawGiftView.transformGiftArrayFitScreen(LiveActivity.this);
                                //TODO 这里你需要把json传给你的服务器

                                //模拟1秒后收到服务器的推送，显示礼物动画
                                handler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        prepareShowDrawGift(drawFixedArray , false);
                                    }
                                }, 700);
                            } else {
                                //非手绘礼物
                            }

                            //隐藏底部礼物弹框
                            bottomGiftSheetBuilder.mDialog.dismiss();
                        }
                    });
        }
        return giftSheetBuilder;
    }

    //从缓存里取出bitmap
    private Bitmap obtainThumbBitmap(int giftId , Bitmap largeBitmap){
        Bitmap thumbGiftBitmap = cacheBitmapByGiftIdMap.get(giftId);
        if (thumbGiftBitmap == null){
            int newSize = dip2px(LiveActivity.this, 20);
            thumbGiftBitmap = Bitmap.createScaledBitmap(largeBitmap, newSize, newSize, true);
            cacheBitmapByGiftIdMap.put(giftId, thumbGiftBitmap);
        }
        return thumbGiftBitmap;
    }

    //计算需要的金币并setText
    private void resetNeedPriceDisplay(){
        float totalPrice = 0;
        List<DrawGiftModel> allDrawGiftArray = drawGiftView.getAllDrawGiftArray();
        for (DrawGiftModel giftModel : allDrawGiftArray) {
            totalPrice += giftModel.getGiftPrice();
        }
        giftSheetBuilder.costCoinTv.setText("消耗金币："+totalPrice);
    }

    //子线程处理播放礼物的数据
    private void prepareShowDrawGift(List<HashMap<String, String>> fixedArray, boolean insertToFirst){
        //把服务器推送来的"礼物位置json" 和 本地的 giftBeanList 一一对应上，找到礼物的bitmap
        final List<DrawGiftModel> allDrawGiftArray = new ArrayList<>();

        //经过测试，这个子线程耗时仅为30ms左右(前提是bitmap已经被是从本地取的)
        Thread thread = new Thread(){
            @Override
            public void run() {
                Log.e("dz","prepareShowDrawGift start");

                //本机屏幕宽高
                DisplayMetrics displayMetrics = new DisplayMetrics();
                ((WindowManager) getApplicationContext().getSystemService(Context.WINDOW_SERVICE))
                        .getDefaultDisplay().getMetrics(displayMetrics);
                float viewWidth = displayMetrics.widthPixels;
                float viewHeight = displayMetrics.heightPixels;

                //fixedArray 是服务器推送过来的礼物json
                for (HashMap<String, String> fixedMap : fixedArray) {
                    //giftBeanList 是手机本地缓存的全部礼物列表
                    for (GiftBean giftBean : giftBeanList) {
                        //我们要做的就是两个列表相互匹配，根据礼物id，找出礼物的bitmap（通过glide）
                        int giftId = Integer.parseInt(fixedMap.get("giftid"));
                        if (giftBean.getGiftId() == giftId){

                            //将服务器推送来的x，y转成绝对像素坐标
                            DrawGiftModel drawGiftModel = new DrawGiftModel();
                            drawGiftModel.setX(Float.valueOf(fixedMap.get("x")) * viewWidth);
                            drawGiftModel.setY(Float.valueOf(fixedMap.get("y")) * viewHeight);

                            Bitmap thumbGiftBitmap = cacheBitmapByGiftIdMap.get(giftId);
                            if (thumbGiftBitmap != null){
                                //缓存中就有bitmap
                                drawGiftModel.setGiftBitmap(thumbGiftBitmap);

                            } else {
                                //缓存没有bitmap
                                //从Glide里找出礼物的bitmap
                                FutureTarget<Bitmap> futureBitmap = Glide.with(LiveActivity.this).asBitmap()
                                        .load(giftBean.getPicture())
                                        .submit();

                                try {
                                    Bitmap bitmap = futureBitmap.get();
                                    thumbGiftBitmap = obtainThumbBitmap(giftId, bitmap);
                                    drawGiftModel.setGiftBitmap(thumbGiftBitmap);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    //万一下载失败了，取本地的图片占位
                                    Bitmap errorBitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.error_draw_gift);
                                    drawGiftModel.setGiftBitmap(errorBitmap);
                                }
                            }
                            allDrawGiftArray.add(drawGiftModel);
                            break;
                        }
                    }
                }

                Message message = Message.obtain(handler, 1 ,insertToFirst ? 1 : 0 , 0, allDrawGiftArray);
                handler.sendMessage(message);
            }
        };
        thread.start();
    }

    //播放礼物draw动画，insertToFirst = 是否插入到队列前面
    private void playDrawGift(List<DrawGiftModel> allDrawGiftArray, boolean insertToFirst){
        if (playView == null) {
            playView = new DrawGiftPlayView(this);
            playView.setOnDrawAnimationListener(new DrawGiftPlayView.DrawAnimationListener(){

                @Override
                public void onAnimationNodeEnd(DrawGiftPlayView drawGiftPlayView) {

                }

                @Override
                public void onAnimationAllOver(DrawGiftPlayView drawGiftPlayView) {
                    //动画放完了，移除掉播放礼物View层。当然这里你也可以不移除，一直保留
                    FrameLayout contentParent = (FrameLayout) getWindow().getDecorView().findViewById(android.R.id.content);
                    contentParent.removeView(drawGiftPlayView);
                }
            });
        }

        if (playView.getParent() == null){
            //添加到decorView
            FrameLayout contentParent = (FrameLayout) getWindow().getDecorView().findViewById(android.R.id.content);
            playView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            contentParent.addView(playView);
        }

        playView.addDrawGifts(allDrawGiftArray, insertToFirst);
    }

    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    private int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }
}