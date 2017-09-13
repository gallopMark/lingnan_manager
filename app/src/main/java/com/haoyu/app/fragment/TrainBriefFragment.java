package com.haoyu.app.fragment;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.haoyu.app.activity.BriefDetailActivity;
import com.haoyu.app.activity.BriefEditActivity;
import com.haoyu.app.adapter.TrainBriefAdapter;
import com.haoyu.app.base.BaseFragment;
import com.haoyu.app.base.BaseResponseResult;
import com.haoyu.app.entity.AnnouncementEntity;
import com.haoyu.app.entity.AnnouncementListResult;
import com.haoyu.app.entity.Paginator;
import com.haoyu.app.lingnan.manager.R;
import com.haoyu.app.utils.Constants;
import com.haoyu.app.utils.OkHttpClientManager;
import com.haoyu.app.view.LoadFailView;
import com.haoyu.app.view.LoadingView;
import com.haoyu.app.xrecyclerview.XRecyclerView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Request;


/**
 * 创建日期：2017/1/16 on 19:29
 * 描述:项目简报
 * 作者:马飞奔 Administrator
 */
public class TrainBriefFragment extends BaseFragment implements XRecyclerView.LoadingListener {
    private TextView tv_empty;
    private LoadingView loadingView;
    private LoadFailView loadFailView;
    private XRecyclerView xRecyclerView;
    private Button bt_addBrief;
    private int page = 1;
    private int limit = 20;
    private boolean isRefresh, isLoadMore;
    private List<AnnouncementEntity> entityList = new ArrayList<>();
    private TrainBriefAdapter adapter;
    private int REQUEST_CREATE = 1, REQUEST_ALTER = 2;

    @Override
    public int createView() {
        return R.layout.fragment_train_brief;
    }

    @Override
    public void initData() {
        String url = Constants.OUTRT_NET + "/m/announcement?announcementRelations[0].relation.id=system"
                + "&type=train_report" + "&getContent=true" + "&page=" + page + "&limit=" + limit + "&orders=CREATE_TIME.DESC";
        addSubscription(OkHttpClientManager.getAsyn(context, url, new OkHttpClientManager.ResultCallback<AnnouncementListResult>() {
            @Override
            public void onBefore(Request request) {
                super.onBefore(request);
                if (!isRefresh) {
                    loadingView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onError(Request request, Exception e) {
                xRecyclerView.refreshComplete(false);
                if (isRefresh) {
                    xRecyclerView.refreshComplete(false);
                } else if (isLoadMore) {
                    xRecyclerView.loadMoreComplete(false);
                    page -= 1;
                } else {
                    loadingView.setVisibility(View.GONE);
                    loadFailView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onResponse(AnnouncementListResult response) {
                loadingView.setVisibility(View.GONE);
                if (response != null && response.getResponseData() != null && response.getResponseData().getAnnouncements() != null && response.getResponseData().getAnnouncements().size() > 0) {
                    UpdateUI(response.getResponseData().getAnnouncements(), response.getResponseData().getPaginator());
                } else {
                    if (isRefresh) {
                        xRecyclerView.refreshComplete(true);
                    } else if (isLoadMore) {
                        xRecyclerView.loadMoreComplete(true);
                    } else {
                        tv_empty.setVisibility(View.VISIBLE);
                        xRecyclerView.setVisibility(View.GONE);
                    }
                    xRecyclerView.setLoadingMoreEnabled(false);
                }
            }
        }));
    }

    private void UpdateUI(List<AnnouncementEntity> mDatas, Paginator paginator) {
        if (isRefresh) {
            entityList.clear();
            xRecyclerView.refreshComplete(true);
        } else if (isLoadMore) {
            xRecyclerView.loadMoreComplete(true);
        }
        entityList.addAll(mDatas);
        adapter.notifyDataSetChanged();
        if (paginator != null && paginator.getHasNextPage()) {
            xRecyclerView.setLoadingMoreEnabled(true);
        } else {
            xRecyclerView.setLoadingMoreEnabled(false);
        }
    }

    @Override
    public void initView(View view) {
        tv_empty = getView(view, R.id.tv_empty);
        xRecyclerView = getView(view, R.id.xRecyclerView);
        loadingView = getView(view, R.id.loadView);
        loadFailView = getView(view, R.id.loadFailView);
        bt_addBrief = getView(view, R.id.bt_addBrief);
        LinearLayoutManager manager = new LinearLayoutManager(context);
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        xRecyclerView.setLayoutManager(manager);
        xRecyclerView.setArrowImageView(R.drawable.refresh_arrow);
        adapter = new TrainBriefAdapter(entityList);
        xRecyclerView.setAdapter(adapter);
        xRecyclerView.setLoadingListener(this);
    }

    @Override
    public void setListener() {
        bt_addBrief.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, BriefEditActivity.class);
                startActivityForResult(intent, REQUEST_CREATE);
            }
        });
        adapter.setItemClickCallBack(new TrainBriefAdapter.onItemClickCallBack() {
            @Override
            public void onItemCallBack(int position, AnnouncementEntity entity) {
                Intent intent = new Intent(context, BriefDetailActivity.class);
                intent.putExtra("relationId", entityList.get(position).getId());
                startActivity(intent);
            }
        });

        adapter.setDisposeCallBack(new TrainBriefAdapter.onDisposeCallBack() {
            @Override
            public void onAlter(int position, AnnouncementEntity entity) {
                Intent intent = new Intent(context, BriefEditActivity.class);
                intent.putExtra("entity", entity);
                intent.putExtra("isAlter", true);
                startActivityForResult(intent, REQUEST_ALTER);
            }

            @Override
            public void onDelete(final int position, AnnouncementEntity entity) {
                showTipDialog();
                delete(position, entity);
            }
        });
        loadFailView.setOnRetryListener(new LoadFailView.OnRetryListener() {
            @Override
            public void onRetry(View v) {
                initData();
            }
        });
    }

    private void delete(final int position, AnnouncementEntity entity) {
        String url = Constants.OUTRT_NET + "/m/announcement/" + entity.getId();
        Map<String, String> map = new HashMap<>();
        map.put("_method", "delete");
        addSubscription(OkHttpClientManager.postAsyn(context, url, new OkHttpClientManager.ResultCallback<BaseResponseResult>() {
            @Override
            public void onError(Request request, Exception e) {
                hideTipDialog();
                onNetWorkError();
            }

            @Override
            public void onResponse(BaseResponseResult response) {
                hideTipDialog();
                if (response != null && response.getResponseCode() != null && response.getResponseCode().equals("00")) {
                    entityList.remove(position);
                    adapter.notifyDataSetChanged();
                    if (entityList.size() <= 0) {
                        xRecyclerView.setVisibility(View.GONE);
                        tv_empty.setVisibility(View.VISIBLE);
                    }
                }
            }
        }, map));
    }

    @Override
    public void onRefresh() {
        isRefresh = true;
        isLoadMore = false;
        page = 1;
        initData();
    }

    @Override
    public void onLoadMore() {
        isRefresh = false;
        isLoadMore = true;
        page += 1;
        initData();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, requestCode, data);
        if (requestCode == REQUEST_CREATE && resultCode == Activity.RESULT_OK && data != null) {
            AnnouncementEntity entity = (AnnouncementEntity) data.getSerializableExtra("entity");
            if (!xRecyclerView.isLoadingMoreEnabled()) {
                entityList.add(entity);
                adapter.notifyDataSetChanged();
            }
            if (entityList.size() > 0) {
                xRecyclerView.setVisibility(View.VISIBLE);
                tv_empty.setVisibility(View.GONE);
            }
        } else if (requestCode == REQUEST_ALTER && resultCode == Activity.RESULT_OK && data != null) {
            AnnouncementEntity entity = (AnnouncementEntity) data.getSerializableExtra("entity");
            if (entityList.contains(entity)) {
                int index = entityList.indexOf(entity);
                entityList.set(index, entity);
                adapter.notifyDataSetChanged();
            }
        }
    }
}
