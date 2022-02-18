package com.dq.drawgiftdemo.dialogsheet;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ImageView;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import com.dq.drawgiftdemo.R;
import com.dq.drawgiftdemo.model.GiftBean;
import java.util.ArrayList;
import java.util.List;

public class GiftPagerView extends ViewPager {

    private int currentSelectGiftTag = -1;// indexInManager
    private View currentSelectGiftView ;

    private final static int bigEmojiconRows = 2;
    private final static int bigEmojiconColumns = 4;

    private GiftSelectListener onGiftSelectListener;

    public GiftPagerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public GiftPagerView(Context context) {
        this(context, null);
    }

    public int init(Context context, List<GiftBean> giftList){
        List<View> gridViews = new ArrayList<View>();
        List<GridView> tempGridViews = getGroupGridViews(context, giftList);
        gridViews.addAll(tempGridViews);

        PagerAdapter pagerAdapter = new EmojiconPagerAdapter(gridViews);
        setAdapter(pagerAdapter);
        return tempGridViews.size();
    }

    /**
     * 获取一类表情的gridviews，有几个面板list里就有几个GridView
     * @return
     */
    private List<GridView> getGroupGridViews(Context context, List<GiftBean> giftList){
        //每一页有几个表情（-1表示不算删除按钮）
        int itemSize = bigEmojiconColumns * bigEmojiconRows;
        int totalSize = giftList.size();
        int pageSize = totalSize % itemSize == 0 ? totalSize/itemSize : totalSize/itemSize + 1;
        List<GridView> views = new ArrayList<GridView>();
        for(int i = 0; i < pageSize; i++){
        	GridView gv = new GridView(context);
    		gv.setSelector(new ColorDrawable(Color.TRANSPARENT));// 屏蔽GridView默认点击效果
    		gv.setCacheColorHint(Color.TRANSPARENT);
    		int item_space = (int)dpToPx(9);
    		gv.setVerticalSpacing(item_space);
    		gv.setGravity(Gravity.CENTER);
    		gv.setNumColumns(bigEmojiconColumns);
    		gv.setPadding(item_space, item_space, item_space, 0);
            gv.setTag(i);
            //一页的表情数据
            List<GiftBean> list = new ArrayList<GiftBean>();
            if(i != pageSize -1){
                //从当前类型的表情总数，切出每一页
                list.addAll(giftList.subList(i * itemSize, (i+1) * itemSize));
            }else{
                list.addAll(giftList.subList(i * itemSize, totalSize));
            }

            GiftGridAdapter gridAdapter = new GiftGridAdapter(context, 1, list);
            gv.setAdapter(gridAdapter);
            gv.setOnItemClickListener(new OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    GiftGridAdapter gridAdapter = (GiftGridAdapter)parent.getAdapter();
                    int gridViewTag = (int)parent.getTag();
                    int indexInManager = gridViewTag*(bigEmojiconColumns * bigEmojiconRows)+position;
                    if (currentSelectGiftTag != -1) {
                        view.setBackgroundResource(R.drawable.white_fill_blue_border_rect);
                        currentSelectGiftView.setBackgroundColor(getResources().getColor(android.R.color.transparent));
                    }
                    if (indexInManager == currentSelectGiftTag) {
                        //点选了当前的
                        currentSelectGiftTag = -1;

                        if (onGiftSelectListener != null){
                            onGiftSelectListener.onGiftSelect(GiftPagerView.this, currentSelectGiftTag, null);
                        }
                    } else {
                        //点选了新的
                        view.setBackgroundResource(R.drawable.gift_item_round);
                        currentSelectGiftTag = indexInManager;
                        currentSelectGiftView = view;

                        if (onGiftSelectListener != null){

                            ImageView imageView = currentSelectGiftView.findViewById(R.id.iv_expression);
                            imageView.setDrawingCacheEnabled(true);
                            Bitmap bitmap = Bitmap.createBitmap(imageView.getDrawingCache());
                            imageView.setDrawingCacheEnabled(false);

                            onGiftSelectListener.onGiftSelect(GiftPagerView.this, currentSelectGiftTag, bitmap);
                        }
                    }
                }
            });
            
            views.add(gv);
        }
        return views;
    }

    public static final float DENSITY = Resources.getSystem()
            .getDisplayMetrics().density;

    public static int dpToPx(int dpValue) {
        return (int) (dpValue * DENSITY + 0.5f);
    }

    public int getCurrentSelectGiftTag(){
        return currentSelectGiftTag;
    }

    public Drawable findCurrentImage(){
        ImageView imageView = currentSelectGiftView.findViewById(R.id.iv_expression);
        return imageView.getDrawable();
    }

    public void setOnGiftSelectListener(GiftSelectListener onGiftSelectListener) {
        this.onGiftSelectListener = onGiftSelectListener;
    }

    public interface GiftSelectListener {
        void onGiftSelect(GiftPagerView viewPager, int position, Bitmap giftBitmap);
    }
}
