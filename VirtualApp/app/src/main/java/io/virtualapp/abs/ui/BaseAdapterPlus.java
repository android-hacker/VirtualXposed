package io.virtualapp.abs.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.SpinnerAdapter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class BaseAdapterPlus<T> extends BaseAdapter implements SpinnerAdapter {
    protected Context context;
    private LayoutInflater mLayoutInflater;
    protected final List<T> mItems = new ArrayList<T>();

    public BaseAdapterPlus(Context context) {
        this.context = context;
        mLayoutInflater = LayoutInflater.from(context);
    }

    public Context getContext() {
        return context;
    }

    public boolean add(T item) {
        return add(-1, item, false);
    }

    public boolean add(int pos, T item, boolean onlyone) {
        if (item != null) {
            if (onlyone) {
                if (exist(item)) {
                    return false;
                }
            }
            if (pos >= 0) {
                mItems.add(pos, item);
            } else {
                mItems.add(item);
            }
            return true;
        }
        return true;
    }

    public T remove(int pos) {
        return mItems.remove(pos);
    }

    public List<T> getItems() {
        return mItems;
    }

    protected <VW extends View> VW  inflate(int resource, ViewGroup root) {
        return (VW) mLayoutInflater.inflate(resource, root);
    }

    protected <VW extends View> VW inflate(int resource, ViewGroup root, boolean attachToRoot) {
        return (VW) mLayoutInflater.inflate(resource, root, attachToRoot);
    }

    public void clear() {
        mItems.clear();
    }

    public void set(Collection<T> items) {
        clear();
        addAll(items);
    }

    public void addAll(Collection<T> items) {
        if (items != null) {
            mItems.addAll(items);
        }
    }

    public int findItem(T item) {
        return mItems.indexOf(item);
    }

    public boolean exist(T item) {
        if (item == null) return false;
        return mItems.contains(item);
    }

    @Override
    public final int getCount() {
        return mItems.size();
    }

    public final T getDataItem(int position) {
        return mItems.get(position);
    }

    @Override
    public final T getItem(int position) {
        if (position >= 0 && position < getCount()) {
            return mItems.get(position);
        }
        return null;
    }

    public final T getItemById(long id) {
        return getItem((int) id);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public final View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = createView(position, parent);
        }
        T t = getItem(position);
        attach(convertView, t, position);
        return convertView;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = createView(position, parent);
        }
        T t = getItem(position);
        attach(convertView, t, position);
        return convertView;
    }

    protected abstract View createView(int position, ViewGroup parent);

    protected abstract void attach(View view, T item, int position);

    public static class BaseViewHolder {
        protected View view;
        protected Context context;

        public BaseViewHolder(View view) {
            this.view = view;
            this.context = view.getContext();
        }

        protected <T extends View> T $(int id) {
            return (T) view.findViewById(id);
        }
    }
}
