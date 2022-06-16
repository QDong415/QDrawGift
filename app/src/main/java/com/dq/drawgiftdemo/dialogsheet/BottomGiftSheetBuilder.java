package com.dq.drawgiftdemo.dialogsheet;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.viewpager.widget.ViewPager;
import com.dq.drawgiftdemo.R;
import com.dq.drawgiftdemo.model.GiftBean;

import java.util.LinkedList;
import java.util.List;

public class BottomGiftSheetBuilder implements ViewPager.OnPageChangeListener, QBottomSheet.OnBottomSheetShowListener, DialogInterface.OnDismissListener {

    private Context mContext;
    public QBottomSheet mDialog;

    private TextView myCoinTv;
    private GiftIndicatorView indicatorView;
    private GiftPagerView pagerView;

    public TextView costCoinTv;
    public ImageView revokeIv;
    public ImageView deleteIv;

    //当前选中的礼物
    private GiftBean selectedDrawGiftBean;
    //当前选中的礼物的Bitmap
    private Bitmap selectedDrawGiftLargeBitmap;

    private BottomGiftSheetListener onBottomGiftSheetListener;

    public BottomGiftSheetBuilder(Context context) {
        mContext = context;
    }

    public QBottomSheet build(View cachedBottomGiftSheetView) {
        mDialog = new QBottomSheet(mContext);

        if (cachedBottomGiftSheetView != null) {
            mDialog.setContentView(cachedBottomGiftSheetView,
                    new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        } else {
            View contentView = (LinearLayout) View.inflate(mContext, R.layout.bottom_sheet_gift, null);;
            mDialog.setContentView(contentView,
                    new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        }
        mDialog.setOnBottomSheetShowListener(this);
        mDialog.setOnDismissListener(this);
        return mDialog;
    }

    public void setGiftList(final Context context, final List<GiftBean> giftList ,LinkedList<View> cachedItemViewList
            , final BottomGiftSheetListener onBottomGiftSheetListener){

        this.onBottomGiftSheetListener = onBottomGiftSheetListener;

        pagerView = (GiftPagerView) mDialog.findViewById(R.id.pager_view);
        indicatorView = (GiftIndicatorView) mDialog.findViewById(R.id.indicator_view);
        myCoinTv = (TextView) mDialog.findViewById(R.id.my_coin_tv);
        costCoinTv = (TextView) mDialog.findViewById(R.id.cost_coin_tv);
        revokeIv = (ImageView) mDialog.findViewById(R.id.revoke_iv);
        deleteIv = (ImageView) mDialog.findViewById(R.id.delete_iv);

        pagerView.cachedItemViewList = cachedItemViewList;
        int pagesize = pagerView.init(context,giftList);
        indicatorView.init(pagesize);
        pagerView.addOnPageChangeListener(this);

        pagerView.setOnGiftSelectListener(new GiftPagerView.GiftSelectListener() {
            @Override
            public void onGiftSelect(GiftPagerView viewPager, int position, Bitmap giftBitmap) {
                if (position == -1){
                    //没选任何礼物
                    selectedDrawGiftBean = null;
                    selectedDrawGiftLargeBitmap = null;

                    if (onBottomGiftSheetListener != null) {
                        onBottomGiftSheetListener.onGiftSelect(BottomGiftSheetBuilder.this, null, -1, null);
                    }
                } else {
                    //选中了某个礼物
                    GiftBean giftBean = giftList.get(position);

                    if (giftBean.drawEnable()){
                        selectedDrawGiftBean = giftBean;
                        selectedDrawGiftLargeBitmap = giftBitmap;
                    } else {
                        selectedDrawGiftBean = null;
                        selectedDrawGiftLargeBitmap = null;
                    }

                    if (onBottomGiftSheetListener != null) {
                        onBottomGiftSheetListener.onGiftSelect(BottomGiftSheetBuilder.this, giftBean, position, giftBitmap);
                    }
                }
            }
        });

        mDialog.findViewById(R.id.send_tv).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // 点击赠送按钮
                if (pagerView.getCurrentSelectGiftTag() == -1) {
                    return;
                }

                GiftBean giftBean = giftList.get(pagerView.getCurrentSelectGiftTag());

                if (onBottomGiftSheetListener != null) {
                    onBottomGiftSheetListener.onGiftSend(BottomGiftSheetBuilder.this, giftBean);
                }
            }
        });

        revokeIv.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // 点击撤回按钮
                if (onBottomGiftSheetListener != null) {
                    onBottomGiftSheetListener.onDrawRevokeClick(BottomGiftSheetBuilder.this);
                }
            }
        });

        deleteIv.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // 点击删除按钮
                if (onBottomGiftSheetListener != null) {
                    onBottomGiftSheetListener.onDrawDeleteClick(BottomGiftSheetBuilder.this);
                }
            }
        });
    }

    public GiftBean getSelectedDrawGiftBean() {
        return selectedDrawGiftBean;
    }

    public Bitmap getSelectedDrawGiftLargeBitmap() {
        return selectedDrawGiftLargeBitmap;
    }

    public interface BottomGiftSheetListener {
        //底部弹框弹出
        public void onGiftSheetShow(BottomGiftSheetBuilder bottomGiftSheetBuilder);
        //底部弹框消失
        public void onGiftSheetDismiss(BottomGiftSheetBuilder bottomGiftSheetBuilder);
        //点选了一个礼物
        public void onGiftSelect(BottomGiftSheetBuilder bottomGiftSheetBuilder, GiftBean giftBean, int position, Bitmap giftBitmap);
        //在画图模式下点击了撤回按钮
        public void onDrawRevokeClick(BottomGiftSheetBuilder bottomGiftSheetBuilder);
        //在画图模式下点击了清空按钮
        public void onDrawDeleteClick(BottomGiftSheetBuilder bottomGiftSheetBuilder);
        //点赠送按钮
        public void onGiftSend(BottomGiftSheetBuilder bottomGiftSheetBuilder, GiftBean giftBean);
    }

    @Override
    public void onShow() {
        if (onBottomGiftSheetListener != null) {
            onBottomGiftSheetListener.onGiftSheetShow(this);
        }
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        if (onBottomGiftSheetListener != null) {
            onBottomGiftSheetListener.onGiftSheetDismiss(this);
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        indicatorView.selectTo(position);
    }

    @Override
    public void onPageSelected(int position) {

    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

}
