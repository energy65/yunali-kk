package com.fongmi.android.tv.ui.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.leanback.widget.ArrayObjectAdapter;
import androidx.leanback.widget.ItemBridgeAdapter;
import androidx.leanback.widget.ListRow;
import androidx.lifecycle.ViewModelProvider;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewbinding.ViewBinding;

import com.fongmi.android.tv.Product;
import com.fongmi.android.tv.R;
import com.fongmi.android.tv.api.config.VodConfig;
import com.fongmi.android.tv.bean.Cache;
import com.fongmi.android.tv.bean.Filter;
import com.fongmi.android.tv.bean.Result;
import com.fongmi.android.tv.bean.Site;
import com.fongmi.android.tv.bean.Style;
import com.fongmi.android.tv.bean.Value;
import com.fongmi.android.tv.bean.Vod;
import com.fongmi.android.tv.databinding.FragmentTypeBinding;
import com.fongmi.android.tv.databinding.ViewFilterBinding;
import com.fongmi.android.tv.model.SiteViewModel;
import com.fongmi.android.tv.ui.activity.CollectActivity;
import com.fongmi.android.tv.ui.activity.HomeActivity;
import com.fongmi.android.tv.ui.activity.VodActivity;
import com.fongmi.android.tv.ui.activity.VideoActivity;
import com.fongmi.android.tv.ui.adapter.FilterAdapter;
import com.fongmi.android.tv.setting.Setting;
import com.fongmi.android.tv.ui.base.BaseFragment;
import com.fongmi.android.tv.ui.custom.CustomRowPresenter;
import com.fongmi.android.tv.ui.custom.CustomScroller;
import com.fongmi.android.tv.ui.custom.CustomSelector;
import com.fongmi.android.tv.ui.presenter.VodPresenter;
import com.fongmi.android.tv.utils.Notify;
import com.fongmi.android.tv.utils.ResUtil;
import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TypeFragment extends BaseFragment implements CustomScroller.Callback, VodPresenter.OnClickListener, SwipeRefreshLayout.OnRefreshListener {

    private HashMap<String, String> mExtends;
    private FragmentTypeBinding mBinding;
    private ArrayObjectAdapter mAdapter;
    private ArrayObjectAdapter mLast;
    private CustomScroller mScroller;
    private SiteViewModel mViewModel;
    private List<Filter> mFilters;
    private boolean headerVisible;

    public static TypeFragment newInstance(String key, String typeId, Style style, HashMap<String, String> extend, boolean folder) {
        Bundle args = new Bundle();
        args.putString("key", key);
        args.putString("typeId", typeId);
        args.putBoolean("folder", folder);
        args.putParcelable("style", style);
        args.putSerializable("extend", extend);
        TypeFragment fragment = new TypeFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private String getKey() {
        return getArguments().getString("key");
    }

    private String getTypeId() {
        return getArguments().getString("typeId");
    }

    private boolean isFolder() {
        return getArguments().getBoolean("folder");
    }

    private boolean isHome() {
        return "home".equals(getTypeId());
    }

    private Style getStyle() {
        return isFolder() ? Style.list() : getSite().getStyle(getArguments().getParcelable("style"));
    }

    private HashMap<String, String> getExtend() {
        return (HashMap<String, String>) getArguments().getSerializable("extend");
    }

    private List<Filter> getFilter() {
        return Cache.copy(getTypeId());
    }

    private Site getSite() {
        return VodConfig.get().getSite(getKey());
    }

    private FolderFragment getParent() {
        return ((FolderFragment) getParentFragment());
    }

    @Override
    protected ViewBinding getBinding(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return mBinding = FragmentTypeBinding.inflate(inflater, container, false);
    }

    @Override
    protected void initView() {
        mScroller = new CustomScroller(this);
        mExtends = getExtend();
        mFilters = getFilter();
        setRecyclerView();
        setViewModel();
        setFilters();
        getVideo();
    }

    @Override
    protected void initEvent() {
        mBinding.swipeLayout.setOnRefreshListener(this);
        mBinding.recycler.addOnScrollListener(mScroller);
    }

    @SuppressLint("RestrictedApi")
    private void setRecyclerView() {
        CustomSelector selector = new CustomSelector();
        selector.addPresenter(Vod.class, new VodPresenter(this, Style.list()));
        selector.addPresenter(ListRow.class, new CustomRowPresenter(16), VodPresenter.class);
        mBinding.recycler.setAdapter(new ItemBridgeAdapter(mAdapter = new ArrayObjectAdapter(selector)));
        mBinding.recycler.setHeader(getActivity(), R.id.recycler);
        mBinding.recycler.setVerticalSpacing(ResUtil.dp2px(16));
    }

    private void setViewModel() {
        mViewModel = new ViewModelProvider(this).get(SiteViewModel.class);
        mViewModel.getResult().observe(getViewLifecycleOwner(), this::setAdapter);
        mViewModel.getAction().observe(getViewLifecycleOwner(), result -> Notify.show(result.getMsg()));
    }

    private void setFilters() {
        for (Filter filter : mFilters) {
            if (mExtends.containsKey(filter.getKey())) filter.setSelected(mExtends.get(filter.getKey()));
            mBinding.filter.addView(getFilterView(filter));
        }
        mBinding.filter.setVisibility(mFilters.isEmpty() ? View.GONE : View.VISIBLE);
    }

    private View getFilterView(Filter filter) {
        ViewFilterBinding binding = ViewFilterBinding.inflate(getLayoutInflater(), mBinding.filter, false);
        binding.name.setText(filter.getName());
        binding.recycler.setHasFixedSize(true);
        binding.recycler.setItemAnimator(null);
        binding.recycler.setNumRows(1);
        binding.recycler.setHorizontalSpacing(ResUtil.dp2px(10));
        binding.recycler.setRowHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        binding.recycler.setAdapter(new FilterAdapter(filter, this::setClick).control(new FilterAdapter.OnControlListener() {
            @Override
            public void onBack() {
                focusTop();
            }

            @Override
            public void onUp() {
                mBinding.recycler.showHeader();
            }
        }));
        return binding.getRoot();
    }

    private void focusTop() {
        mBinding.recycler.showHeader();
        mBinding.recycler.scrollToPosition(0);
        View tabs = requireActivity().findViewById(R.id.recycler);
        if (tabs != null) tabs.post(tabs::requestFocus);
    }

    private void setClick(String key, Value item) {
        if (item.isSelected()) mExtends.put(key, item.getV());
        else mExtends.remove(key);
        Setting.putSiteFilter(getKey(), getTypeId(), mExtends);
        onRefresh();
    }

    private void getVideo() {
        mLast = null;
        clearVideo();
        mScroller.reset();
        if (isHome()) setAdapter(getHomeResult());
        else getVideo(getTypeId(), "1");
    }

    private Result getHomeResult() {
        if (getActivity() instanceof HomeActivity) return ((HomeActivity) getActivity()).getResult();
        if (getActivity() instanceof VodActivity) return ((VodActivity) getActivity()).getResult();
        return Result.empty();
    }

    private void getVideo(String typeId, String page) {
        mViewModel.categoryContent(getKey(), typeId, page, true, mExtends);
    }

    private void setAdapter(Result result) {
        boolean first = mScroller.first();
        boolean flag = mExtends.isEmpty();
        int size = result.getList().size();
        mBinding.progressLayout.showContent(first & flag, size);
        mBinding.swipeLayout.setRefreshing(false);
        mScroller.endLoading(result);
        if (size > 0) addVideo(result);
    }

    private void addVideo(Result result) {
        Style style = result.getStyle(getStyle());
        if (style.isList()) mAdapter.addAll(mAdapter.size(), result.getList());
        else addGrid(result.getList(), style);
        checkMore();
    }

    private void checkMore() {
        if (isHome() || mScroller.isDisable() || mAdapter.size() >= 5) return;
        mScroller.checkMore();
    }

    private boolean checkLastSize(List<Vod> items, Style style) {
        if (mLast == null || items.isEmpty()) return false;
        int size = Product.getColumn(style) - mLast.size();
        if (size == 0) return false;
        size = Math.min(size, items.size());
        mLast.addAll(mLast.size(), items.subList(0, size));
        addGrid(items.subList(size, items.size()), style);
        return true;
    }

    private void addGrid(List<Vod> items, Style style) {
        if (checkLastSize(items, style)) return;
        List<ListRow> rows = new ArrayList<>();
        VodPresenter presenter = new VodPresenter(this, style);
        for (List<Vod> part : Lists.partition(items, Product.getColumn(style))) {
            mLast = new ArrayObjectAdapter(presenter);
            mLast.addAll(0, part);
            rows.add(new ListRow(mLast));
        }
        mAdapter.addAll(mAdapter.size(), rows);
    }

    private void clearVideo() {
        boolean hasVideo = mAdapter.size() > 0;
        if (hasVideo) mAdapter.removeItems(0, mAdapter.size());
        if (hasVideo) mBinding.swipeLayout.setRefreshing(true);
        else mBinding.progressLayout.showProgress();
    }

    public void onRefresh() {
        if (isHome()) setAdapter(getHomeResult());
        else getVideo();
    }

    @Override
    public void onItemClick(Vod item) {
        if (item.isAction()) {
            mViewModel.action(getKey(), item.getAction());
        } else if (item.isFolder()) {
            getParent().openFolder(item.getId(), mExtends);
            headerVisible = mBinding.recycler.isHeaderVisible();
        } else {
            if (getSite().isIndex()) CollectActivity.start(requireActivity(), item.getName());
            else VideoActivity.start(requireActivity(), getKey(), item.getId(), item.getName(), item.getPic(), isFolder() ? item.getName() : null);
        }
    }

    @Override
    public boolean onLongClick(Vod item) {
        if (item.isAction() || item.isFolder()) return false;
        CollectActivity.start(requireActivity(), item.getName());
        return true;
    }

    @Override
    public boolean onLoadMore(String page) {
        if (isHome()) return false;
        getVideo(getTypeId(), page);
        return true;
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (hidden) {
            mBinding.recycler.showHeader();
        } else {
            if (headerVisible) mBinding.recycler.showHeader();
            else mBinding.recycler.hideHeader();
            mBinding.recycler.requestFocus();
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (mBinding != null) mBinding.recycler.moveToTop();
    }
}
