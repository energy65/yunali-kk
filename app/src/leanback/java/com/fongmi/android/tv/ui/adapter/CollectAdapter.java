package com.fongmi.android.tv.ui.adapter;

import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fongmi.android.tv.bean.Collect;
import com.fongmi.android.tv.databinding.AdapterTypeBinding;

import java.util.ArrayList;
import java.util.List;

public class CollectAdapter extends RecyclerView.Adapter<CollectAdapter.ViewHolder> {

    private final List<Collect> mItems;
    private OnDoubleClickListener doubleClickListener;
    private long lastClickTime;
    private int lastClickPosition = -1;

    public interface OnDoubleClickListener {
        void onItemDoubleClick(int position, Collect item);
    }

    public CollectAdapter() {
        mItems = new ArrayList<>();
    }

    public void setOnDoubleClickListener(OnDoubleClickListener listener) {
        this.doubleClickListener = listener;
    }

    public void add(Collect item) {
        mItems.add(item);
        notifyItemInserted(mItems.size() - 1);
    }

    public void clear() {
        mItems.clear();
        notifyDataSetChanged();
    }

    public Collect get(int position) {
        return mItems.get(position);
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(AdapterTypeBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Collect item = mItems.get(position);
        holder.binding.text.setText(item.getSite().getName());
        holder.binding.text.setAlpha(item.isBlocked() ? 0.4f : 1.0f);
        if (item.isBlocked()) {
            holder.binding.text.setPaintFlags(holder.binding.text.getPaintFlags() | android.graphics.Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            holder.binding.text.setPaintFlags(holder.binding.text.getPaintFlags() & ~android.graphics.Paint.STRIKE_THRU_TEXT_FLAG);
        }
        holder.binding.getRoot().setOnClickListener(v -> {
            long now = SystemClock.elapsedRealtime();
            if (lastClickPosition == position && now - lastClickTime < 500) {
                lastClickTime = 0;
                lastClickPosition = -1;
                if (doubleClickListener != null) {
                    doubleClickListener.onItemDoubleClick(position, item);
                }
            } else {
                lastClickTime = now;
                lastClickPosition = position;
            }
        });
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private final AdapterTypeBinding binding;

        ViewHolder(@NonNull AdapterTypeBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}