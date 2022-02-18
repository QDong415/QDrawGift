package com.dq.drawgift.model;

import android.graphics.Bitmap;
import android.graphics.Matrix;

public class DrawGiftModel {
    
    private float x;
    private float y;
    private int giftId;
    private float giftPrice;//礼物价格，为了方便画的过程中，快速的计算出当前一共画了多少钱的礼物，默认0
    private Bitmap giftBitmap;

    //在收到礼物显示动画的时候，本demo为了体现出礼物放大消失的动画，引入了这个Matrix参数，如果没有放大动画，就不需要这两个
    private Matrix matrix;

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public int getgiftId() {
        return giftId;
    }

    public void setgiftId(int giftId) {
        this.giftId = giftId;
    }

    public Bitmap getGiftBitmap() {
        return giftBitmap;
    }

    public void setGiftBitmap(Bitmap giftBitmap) {
        this.giftBitmap = giftBitmap;
    }

    public Matrix getMatrix() {
        if (matrix == null){
            matrix = new Matrix();
        }
        return matrix;
    }

    public void setMatrix(Matrix matrix) {
        this.matrix = matrix;
    }

    public float getGiftPrice() {
        return giftPrice;
    }

    public void setGiftPrice(float giftPrice) {
        this.giftPrice = giftPrice;
    }
}
