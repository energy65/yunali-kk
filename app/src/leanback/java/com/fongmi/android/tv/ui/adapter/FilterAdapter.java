package com.fongmi.android.tv.ui.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fongmi.android.tv.bean.Filter;
import com.fongmi.android.tv.bean.Value;
import com.fongmi.android.tv.databinding.AdapterFilterBinding;

import java.util.ArrayList;
import java.util.List;

public class FilterAdapter extends RecyclerView.Adapter<FilterAdapter.ViewHolder> {

    private final OnClickListener mListener;
    private final String mKey;
    private final List<Value> mItems;

    public static FilterAdapter create(Filter filter, OnClickListener listener) {
        return new FilterAdapter(filter, listener);
    }

    public FilterAdapter(Filter filter, OnClickListener listener) {
        mListener = listener;
        mKey = filter.getKey();
        mItems = new ArrayList<>(filter.getValue());
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
    }

    private void select(Value item) {
        for (Value value : mItems) value.setSelected(item);
        notifyDataSetChanged();
        mListener.onItemClick(mKey, item);
    }

    public interface OnClickListener {

        void onItemClick(String key, Value item);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private final AdapterFilterBinding binding;

        ViewHolder(@NonNull AdapterFilterBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
