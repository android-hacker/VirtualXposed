package io.virtualapp.widgets;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

import java.util.List;

public abstract class PagerAdapter<DataItem> {

    protected List<DataItem> mList;

    protected OnDataChangeListener mDataChangeListener;

    protected LayoutInflater mInflater;


    public PagerAdapter(Context context, List<DataItem> list) {
        this.mInflater = LayoutInflater.from(context);
        this.mList = list;
    }


    public View getView(int position) {
        DataItem item = mList.get(position);
        View view = mInflater.inflate(getItemLayoutId(position, item), null);
        onBindView(view, item);
        return view;
    }

    public abstract int getItemLayoutId(int position, DataItem item);

    public abstract void onBindView(View view, DataItem item);


    public int getCount() {
        return mList.size();
    }

    public Context getContext() {
        return mInflater.getContext();
    }

    public void exchange(int oldPosition, int newPosition) {
        DataItem item = mList.get(oldPosition);
        mList.remove(oldPosition);
        mList.add(newPosition, item);
    }


    public void setOnDataChangeListener(OnDataChangeListener dataChangeListener) {
        this.mDataChangeListener = dataChangeListener;
    }

    public void delete(int position) {
        if (position < getCount()) {
            mList.remove(position);
            dispatchDataChange();
        }
    }

    private void dispatchDataChange() {
        mDataChangeListener.onDataChange();
    }

    public void add(DataItem item) {
        if (mList.add(item)) {
            dispatchDataChange();
        }
    }

    public DataItem getItem(int position) {
        return mList.get(position);
    }

    public interface OnDataChangeListener {
        void onDataChange();
    }
}
