package com.dq.drawgiftdemo;

import android.util.Log;

import com.dq.drawgift.model.DrawGiftModel;
import com.dq.drawgiftdemo.model.GiftBean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TestData {

    private static float randomIndex = (float) 0.05;

    public static void createGiftBean(List<GiftBean> giftBeanList){
        GiftBean giftBean = new GiftBean();
        giftBean.setGiftId(1);
        giftBean.setPrice(1);//礼物价格
        giftBean.setType(1);//0普通礼物，1可以涂鸦的礼物，2全屏动画礼物
        giftBean.setPicture("https://upload-images.jianshu.io/upload_images/26002059-c705bfa08e77fd88.png");
        giftBean.setTitle("红玫瑰");
        giftBeanList.add(giftBean);

        giftBean = new GiftBean();
        giftBean.setGiftId(2);
        giftBean.setPrice(3);//礼物价格
        giftBean.setType(1);//0普通礼物，1可以涂鸦的礼物，2全屏动画礼物
        giftBean.setPicture("https://upload-images.jianshu.io/upload_images/26002059-7cb943e8df8861d1.png");
        giftBean.setTitle("蓝玫瑰");
        giftBeanList.add(giftBean);

        giftBean = new GiftBean();
        giftBean.setGiftId(3);
        giftBean.setPrice(10);//礼物价格
        giftBean.setType(1);//0普通礼物，1可以涂鸦的礼物，2全屏动画礼物
        giftBean.setPicture("https://upload-images.jianshu.io/upload_images/26002059-ab0005c2d44626ce.png");
        giftBean.setTitle("蓝玫瑰");
        giftBeanList.add(giftBean);

        giftBean = new GiftBean();
        giftBean.setGiftId(4);
        giftBean.setPrice(1000);//礼物价格
        giftBean.setType(2);//0普通礼物，1可以涂鸦的礼物，2全屏动画礼物
        giftBean.setPicture("https://upload-images.jianshu.io/upload_images/26002059-8151fe05b36980e9.png");
        giftBean.setTitle("贵重礼物1");
        giftBeanList.add(giftBean);

        giftBean = new GiftBean();
        giftBean.setGiftId(5);
        giftBean.setPrice(1);//礼物价格
        giftBean.setType(1);//0普通礼物，1可以涂鸦的礼物，2全屏动画礼物
        giftBean.setPicture("https://upload-images.jianshu.io/upload_images/26002059-c705bfa08e77fd88.png");
        giftBean.setTitle("红玫瑰");
        giftBeanList.add(giftBean);

        giftBean = new GiftBean();
        giftBean.setGiftId(6);
        giftBean.setPrice(3);//礼物价格
        giftBean.setType(1);//0普通礼物，1可以涂鸦的礼物，2全屏动画礼物
        giftBean.setPicture("https://upload-images.jianshu.io/upload_images/26002059-7cb943e8df8861d1.png");
        giftBean.setTitle("蓝玫瑰");
        giftBeanList.add(giftBean);

        giftBean = new GiftBean();
        giftBean.setGiftId(7);
        giftBean.setPrice(10);//礼物价格
        giftBean.setType(1);//0普通礼物，1可以涂鸦的礼物，2全屏动画礼物
        giftBean.setPicture("https://upload-images.jianshu.io/upload_images/26002059-ab0005c2d44626ce.png");
        giftBean.setTitle("蓝玫瑰");
        giftBeanList.add(giftBean);

        giftBean = new GiftBean();
        giftBean.setGiftId(8);
        giftBean.setPrice(1000);//礼物价格
        giftBean.setType(2);//0普通礼物，1可以涂鸦的礼物，2全屏动画礼物
        giftBean.setPicture("https://upload-images.jianshu.io/upload_images/26002059-8151fe05b36980e9.png");
        giftBean.setTitle("贵重礼物2");
        giftBeanList.add(giftBean);

        giftBean = new GiftBean();
        giftBean.setGiftId(9);
        giftBean.setPrice(1000);//礼物价格
        giftBean.setType(2);//0普通礼物，1可以涂鸦的礼物，2全屏动画礼物
        giftBean.setPicture("https://upload-images.jianshu.io/upload_images/26002059-8151fe05b36980e9.png");
        giftBean.setTitle("贵重礼物3");
        giftBeanList.add(giftBean);
    }

    public static List<HashMap<String, String>> createRandomGifts(boolean isVipGift){
        //fixedArray是需要转成json发送给服务器的
        List<HashMap<String, String>> fixedArray = new ArrayList<>();
        HashMap<String, String> param = null;
        for (int i = 0; i < 15; i++) {
            param = new HashMap<>();
            param.put("x",String.valueOf(0.05 + i / 16.0f));
            param.put("y",String.valueOf(0.05 + randomIndex));
            param.put("giftid",String.valueOf(isVipGift ? 3 : 1));
            fixedArray.add(param);
        }
        randomIndex += 0.07 ;
        if (randomIndex >= 0.5 ){
            randomIndex = 0.04f;
        }
        return fixedArray;
    }
}
