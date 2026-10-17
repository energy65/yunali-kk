package com.fongmi.android.tv.ui.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fongmi.android.tv.bean.Filter;
import com.fongmi.android.tv.bean.Value;
import com.fongmi.android.tv.databinding.AdapterFilterBinding;
import com.fongmi.android.tv.utils.KeyUtil;

import java.util.ArrayList;
import java.util.List;

public class FilterAdapter extends RecyclerView.Adapter<FilterAdapter.ViewHolder> {

    private final OnClickListener mListener;
    private final String mKey;
    private final List<Value> mItems;
    private OnControlListener mControl;

    public static FilterAdapter create(Filter filter, OnClickListener listener) {
        return new FilterAdapter(filter, listener);
    }

    public FilterAdapter(Filter filter, OnClickListener listener) {
        mListener = listener;
        mKey = filter.getKey();
        mItems = new ArrayList<>(filter.getValue());
    }

    public FilterAdapter control(OnControlListener control) {
        mControl = control;
        return this;
    }

    public String getKey() {
        return mKey;
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(AdapterFilterBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Value item = mItems.get(position);
        holder.binding.text.setText(item.getN());
        holder.binding.text.setSelected(item.isSelected());
        holder.binding.text.setOnClickListener(v -> select(item));
        holder.binding.text.setOnKeyListener((v, event) -> {
            if (mControl == null || !KeyUtil.isActionDown(event)) return false;
            if (KeyUtil.isBackKey(event)) {
                mControl.onBack();
                return true;
            }
            if (KeyUtil.isUpKey(event)) mControl.onUp();
            return false;
        });
    }

    private void select(Value item) {
        for (Value value : mItems) value.setSelected(item);
        notifyItemRangeChanged(0, mItems.size());
        mListener.onItemClick(mKey, item);
    }

    public interface OnClickListener {

        void onItemClick(String key, Value item);
    }

    public interface OnControlListener {

        void onBack();

        void onUp();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private final AdapterFilterBinding binding;

        ViewHolder(@NonNull AdapterFilterBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
