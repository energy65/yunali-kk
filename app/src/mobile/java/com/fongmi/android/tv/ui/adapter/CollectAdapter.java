package com.fongmi.android.tv.ui.adapter;

import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fongmi.android.tv.bean.Collect;
import com.fongmi.android.tv.bean.Vod;
import com.fongmi.android.tv.databinding.AdapterCollectBinding;

import java.util.List;

public class CollectAdapter extends BaseDiffAdapter<Collect, CollectAdapter.ViewHolder> {

    private final OnClickListener listener;
    private long lastClickTime;
    private int lastClickPosition = -1;

    public CollectAdapter(OnClickListener listener) {
        this.listener = listener;
    }

    public interface OnClickListener {

        void onItemClick(int position, Collect item);

        void onItemDoubleClick(int position, Collect item);
    }

    public void add(List<Vod> items) {
        if (getItemCount() == 0) return;
        getItem(0).getList().addAll(items);
    }

    public int getPosition() {
        for (int i = 0; i < getItemCount(); i++) if (getItem(i).isSelected()) return i;
        return 0;
    }

    public Collect getActivated() {
        return getItems().get(getPosition());
    }

    public void setSelected(int position) {
        for (int i = 0; i < getItemCount(); i++) getItem(i).setSelected(i == position);
        notifyItemRangeChanged(0, getItemCount());
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(AdapterCollectBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Collect item = getItem(position);
        holder.binding.text.setSelected(item.isSelected());
        holder.binding.text.setText(item.getSite().getName());
        holder.binding.text.setAlpha(item.isBlocked() ? 0.4f : 1.0f);
        if (item.isBlocked()) {
            holder.binding.text.setPaintFlags(holder.binding.text.getPaintFlags() | android.graphics.Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            holder.binding.text.setPaintFlags(holder.binding.text.getPaintFlags() & ~android.graphics.Paint.STRIKE_THRU_TEXT_FLAG);
        }
        holder.binding.text.setOnClickListener(v -> {
            long now = SystemClock.elapsedRealtime();
            if (lastClickPosition == position && now - lastClickTime < 500) {
                lastClickTime = 0;
                lastClickPosition = -1;
                listener.onItemDoubleClick(position, item);
            } else {
                lastClickTime = now;
                lastClickPosition = position;
                listener.onItemClick(position, item);
            }
        });
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private final AdapterCollectBinding binding;

        ViewHolder(@NonNull AdapterCollectBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}