package com.idata.bluetoothime;

import java.util.List;
import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ConfigAdapter extends BaseAdapter implements OnClickListener {
	
	private List<Integer> mImageList;
	private List<String> mTextList;
    private Context mContext;
    private InnerItemOnclickListener mListener;
    
    public ConfigAdapter(Context mContext) {
    	this.mContext = mContext;
    }
  
    public ConfigAdapter(List<String> mTextList, List<Integer> mImageList, Context mContext) {
        this.mTextList = mTextList;
        this.mImageList = mImageList;
        this.mContext = mContext;  
    }

	@Override
	public int getCount() {
		return mTextList.size();
	}

	@Override
	public Object getItem(int position) {
		return mTextList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@SuppressLint("InflateParams") 
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(mContext).inflate(R.layout.config_item, null);
            viewHolder.barCode = (ImageView) convertView.findViewById(R.id.im_bar_code); 
            viewHolder.info = (TextView) convertView.findViewById(R.id.tv_info);  
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.barCode.setOnClickListener(this); 
        viewHolder.barCode.setImageResource(mImageList.get(position));
        viewHolder.barCode.setTag(position);
        viewHolder.info.setText(mTextList.get(position));
        return convertView;
	}
	
	public final static class ViewHolder {
        ImageView barCode;
        TextView info;  
    }

	@Override
	public void onClick(View v) {
		mListener.itemClick(v);
	}

	interface InnerItemOnclickListener {
        void itemClick(View v);  
    }
  
    public void setOnInnerItemOnClickListener(InnerItemOnclickListener listener){  
        this.mListener=listener;
    }
}
