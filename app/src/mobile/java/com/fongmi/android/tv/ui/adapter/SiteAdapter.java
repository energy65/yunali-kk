package com.fongmi.android.tv.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fongmi.android.tv.R;
import com.fongmi.android.tv.api.config.VodConfig;
import com.fongmi.android.tv.bean.Site;
import com.fongmi.android.tv.databinding.AdapterSiteBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SiteAdapter extends RecyclerView.Adapter<SiteAdapter.ViewHolder> {

    private final OnClickListener listener;
    private final List<Site> mItems;
    private final List<Site> mAllItems;
    private boolean search;
    private boolean change;

    public SiteAdapter(OnClickListener listener) {
        this.listener = listener;
        this.mItems = new ArrayList<>();
        this.mAllItems = new ArrayList<>();
        addAll();
    }

    public interface OnClickListener {

        void onTextClick(Site item);

        void onSearchClick(int position, Site item);

        void onChangeClick(int position, Site item);

        boolean onSearchLongClick(Site item);

        boolean onChangeLongClick(Site item);
    }

    public SiteAdapter search(boolean search) {
        this.search = search;
        return this;
    }

    public SiteAdapter change(boolean change) {
        this.change = change;
        return this;
    }

    private void addAll() {
        mAllItems.clear();
        for (Site site : VodConfig.get().getSites()) if (!site.isHide()) mAllItems.add(site);
        mItems.clear();
        mItems.addAll(mAllItems);
    }

    public void filter(String searchText, String selectedTag) {
        mItems.clear();
        for (Site site : mAllItems) {
            boolean matches = true;
            if (searchText != null && !searchText.isEmpty()) {
                if (!site.getName().toLowerCase().contains(searchText)) {
                    matches = false;
                }
            }
            if (selectedTag != null && !selectedTag.isEmpty()) {
                Pattern pattern = Pattern.compile("\\[([^\\]]+)]");
                Matcher matcher = pattern.matcher(site.getName());
                boolean hasTag = false;
                while (matcher.find()) {
                    if (matcher.group(1).equals(selectedTag)) {
                        hasTag = true;
                        break;
                    }
                }
                if (!hasTag) matches = false;
            }
            if (matches) mItems.add(site);
        }
        notifyDataSetChanged();
    }

    public List<Site> getItems() {
        return mItems;
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(AdapterSiteBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Site item = mItems.get(position);
        boolean on = !search || change;
        holder.binding.text.setText(item.getName());
        holder.binding.text.setEnabled(on);
        holder.binding.text.setFocusable(on);
        holder.binding.text.setSelected(on && item.isSelected());
        holder.binding.search.setImageResource(getSearchIcon(item));
        holder.binding.change.setImageResource(getChangeIcon(item));
        holder.binding.search.setVisibility(search ? View.VISIBLE : View.GONE);
        holder.binding.change.setVisibility(change ? View.VISIBLE : View.GONE);
        holder.binding.text.setOnClickListener(v -> listener.onTextClick(item));
        holder.binding.search.setOnClickListener(v -> listener.onSearchClick(position, item));
        holder.binding.change.setOnClickListener(v -> listener.onChangeClick(position, item));
        holder.binding.search.setOnLongClickListener(v -> listener.onSearchLongClick(item));
        holder.binding.change.setOnLongClickListener(v -> listener.onChangeLongClick(item));
    }

    private int getSearchIcon(Site item) {
        return item.isSearchable() ? R.drawable.ic_site_search : R.drawable.ic_site_block;
    }

    private int getChangeIcon(Site item) {
        return item.isChangeable() ? R.drawable.ic_site_change : R.drawable.ic_site_block;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private final AdapterSiteBinding binding;

        ViewHolder(@NonNull AdapterSiteBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}