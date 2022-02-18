package com.dq.drawgiftdemo.model;

public class GiftBean {

    private String title;
    private String picture;
    private int giftId;
    private float price;

    private int type;//0普通礼物，1可以涂鸦的礼物，2全屏动画礼物

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    public float getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public boolean drawEnable() {
        return type == 1;
    }

    public int getGiftId() {
        return giftId;
    }

    public void setGiftId(int giftId) {
        this.giftId = giftId;
    }
}
