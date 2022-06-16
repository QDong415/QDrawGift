package com.dq.drawgiftdemo.dialogsheet;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.dq.drawgiftdemo.R;
import com.dq.drawgiftdemo.model.GiftBean;

import java.util.LinkedList;
import java.util.List;


public class GiftGridAdapter extends ArrayAdapter<GiftBean>{

    private Context context;

    //为了预加载，里面存的是adapter的itemView，你可以不用
    public LinkedList<View> cachedItemViewList;

    public GiftGridAdapter(Context context, int textViewResourceId, List<GiftBean> objects) {
        super(context, textViewResourceId, objects);
        this.context = context;
    }
    
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null){
            convertView = cachedItemViewList.poll();
            if (convertView == null) {
                convertView = View.inflate(getContext(), R.layout.griditem_gift, null);
                Log.e("dq","getView = 没用上缓存");
            } else {
                Log.e("dq","getView = 用上缓存");
            }
        }
        GiftBean emojicon = getItem(position);
        ImageView imageView = (ImageView) convertView.findViewById(R.id.iv_expression);
        TextView textView = (TextView) convertView.findViewById(R.id.title_tv);
        textView.setText(emojicon.getTitle());

        TextView coin_tv = (TextView) convertView.findViewById(R.id.coin_tv);
        coin_tv.setText(emojicon.getPrice()+"金币");

        Glide.with(context).load(emojicon.getPicture()).diskCacheStrategy(DiskCacheStrategy.RESOURCE).into(imageView);

        return convertView;
    }
}
