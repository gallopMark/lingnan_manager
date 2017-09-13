package com.haoyu.app.fragment;

import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.haoyu.app.activity.StudentAchievementDetailActivity;
import com.haoyu.app.adapter.MyTrainAdapter;
import com.haoyu.app.adapter.TrainStudentScoreAdapter;
import com.haoyu.app.base.BaseFragment;
import com.haoyu.app.base.BaseResponseResult;
import com.haoyu.app.basehelper.BaseRecyclerAdapter;
import com.haoyu.app.entity.MTrainRegisterStat;
import com.haoyu.app.entity.MyTrainMobileEntity;
import com.haoyu.app.entity.Paginator;
import com.haoyu.app.entity.TrainScoreListResult;
import com.haoyu.app.lingnan.manager.R;
import com.haoyu.app.rxBus.MessageEvent;
import com.haoyu.app.utils.Action;
import com.haoyu.app.utils.Constants;
import com.haoyu.app.utils.OkHttpClientManager;
import com.haoyu.app.view.LoadFailView;
import com.haoyu.app.view.LoadingView;
import com.haoyu.app.xrecyclerview.XRecyclerView;

import java.util.ArrayList;
import java.util.List;

import okhttp3.Request;

/**
 * 创建日期：2017/1/16 on 19:28
 * 描述: 学员成绩
 */
public class TrainStudentScoreFragment extends BaseFragment implements XRecyclerView.LoadingListener {
    private String projectId, trainId;
    private List<MyTrainMobileEntity> myTrains = new ArrayList<>();
    private LoadingView loadingView;
    private LoadFailView loadFailView, loadFailView1;
    private LinearLayout ll_qici;
    private TextView semester;
    private RelativeLayout contentLayout;
    private XRecyclerView xRecyclerView;
    private List<MTrainRegisterStat> mDatas = new ArrayList<>();
    private TrainStudentScoreAdapter adapter;
    private boolean isRefresh, isLoadMore, needDialog, fromEvent;
    private int page = 1;
    private TextView tv_warn;

    @Override
    public void obBusEvent(MessageEvent event) {
        if (event.getAction().equals(Action.UPDATE_TRAIN) && event.obj != null && event.obj instanceof String) {
            projectId = (String) event.obj;
            fromEvent = true;
            page = 1;
            initData();
        }
    }

    @Override
    public int createView() {
        return R.layout.fragment_student_score;
    }

    @Override
    public void initView(View view) {
        Bundle bundle = getArguments();
        if (bundle != null) {
            projectId = bundle.getString("trainId");
        }
        loadingView = getView(view, R.id.loadingView);
        loadFailView = getView(view, R.id.loadFailView);
        loadFailView1 = getView(view, R.id.loadFailView1);
        ll_qici = getView(view, R.id.ll_qici);
        semester = getView(view, R.id.semester);
        contentLayout = getView(view, R.id.contentLayout);
        tv_warn = getView(view, R.id.tv_warn);
        xRecyclerView = getView(view, R.id.xRecyclerView);
        LinearLayoutManager manager = new LinearLayoutManager(context);
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        xRecyclerView.setLayoutManager(manager);
        adapter = new TrainStudentScoreAdapter(context, mDatas);
        xRecyclerView.setAdapter(adapter);
        xRecyclerView.setLoadingListener(this);
    }

    @Override
    public void initData() {
        String url = Constants.OUTRT_NET + "/m/manage/listTrain?projectId=" + projectId;
        addSubscription(OkHttpClientManager.getAsyn(context, url, new OkHttpClientManager.ResultCallback<BaseResponseResult<List<MyTrainMobileEntity>>>() {
            @Override
            public void onBefore(Request request) {
                loadingView.setVisibility(View.VISIBLE);
                ll_qici.setVisibility(View.GONE);
                loadFailView.setVisibility(View.GONE);
                contentLayout.setVisibility(View.GONE);
            }

            @Override
            public void onError(Request request, Exception e) {
                loadingView.setVisibility(View.GONE);
                loadFailView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onResponse(BaseResponseResult<List<MyTrainMobileEntity>> response) {
                loadingView.setVisibility(View.GONE);
                if (response != null && response.getResponseData() != null) {
                    updateTrainListUI(response.getResponseData());
                }
            }
        }));
    }

    /*更新我的培训列表*/
    private void updateTrainListUI(List<MyTrainMobileEntity> responseData) {
        ll_qici.setVisibility(View.VISIBLE);
        myTrains.clear();
        myTrains.addAll(responseData);
        if (myTrains.size() > 0) {
            semester.setEnabled(true);
            semester.setText(myTrains.get(0).getName());
            selectItem = 0;
            trainId = myTrains.get(0).getId();
            getUsers();
        } else {
            semester.setHint("暂无可选择的期次");
            semester.setEnabled(false);
        }
    }

    private void updateScoreUI(List<MTrainRegisterStat> datas, Paginator paginator) {
        xRecyclerView.setVisibility(View.VISIBLE);
        if (isRefresh) {
            mDatas.clear();
            xRecyclerView.refreshComplete(true);
        } else if (needDialog) {
            mDatas.clear();
        } else if (fromEvent) {
            mDatas.clear();
        } else if (isLoadMore) {
            xRecyclerView.loadMoreComplete(true);
        }
        mDatas.addAll(datas);
        adapter.notifyDataSetChanged();
        if (paginator != null && paginator.getHasNextPage()) {
            xRecyclerView.setLoadingMoreEnabled(true);
        } else {
            xRecyclerView.setLoadingMoreEnabled(false);
        }
        isRefresh = false;
        isLoadMore = false;
        needDialog = false;
        fromEvent = false;
    }

    @Override
    public void setListener() {
        loadFailView.setOnRetryListener(new LoadFailView.OnRetryListener() {
            @Override
            public void onRetry(View v) {
                initData();
            }
        });
        loadFailView1.setOnRetryListener(new LoadFailView.OnRetryListener() {
            @Override
            public void onRetry(View v) {
                getUsers();
            }
        });
        semester.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setPopupView(semester, myTrains);
            }
        });
        adapter.setOnItemClickListener(new BaseRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseRecyclerAdapter adapter, BaseRecyclerAdapter.RecyclerHolder holder, View view, int position) {
                String trainRegisterId = mDatas.get(position - 1).getTrainRegisterId();
                Intent intent = new Intent(context, StudentAchievementDetailActivity.class);
                intent.putExtra("mUser", mDatas.get(position - 1).getmUser());
                intent.putExtra("trainRegisterId", trainRegisterId);
                intent.putExtra("trainId", trainId);
                startActivity(intent);
            }
        });
    }

    private int selectItem;

    private void setPopupView(final TextView tv, final List<MyTrainMobileEntity> list) {
        Drawable shouqi = ContextCompat.getDrawable(context,
                R.drawable.course_dictionary_shouqi);
        shouqi.setBounds(0, 0, shouqi.getMinimumWidth(),
                shouqi.getMinimumHeight());
        final Drawable zhankai = ContextCompat.getDrawable(context,
                R.drawable.course_dictionary_xiala);
        zhankai.setBounds(0, 0, zhankai.getMinimumWidth(),
                zhankai.getMinimumHeight());
        tv.setCompoundDrawables(null, null, shouqi, null);
        View view = getActivity().getLayoutInflater().inflate(R.layout.popupwindow_listview,
                null);
        ListView lv = view.findViewById(R.id.listView);
        final MyTrainAdapter adapter = new MyTrainAdapter(context, list, selectItem);
        lv.setAdapter(adapter);
        final PopupWindow popupWindow = new PopupWindow(view, LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        popupWindow.setFocusable(true);
        popupWindow.setBackgroundDrawable(new BitmapDrawable());
        popupWindow.setOutsideTouchable(true);
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                tv.setCompoundDrawables(null, null, zhankai, null);
                WindowManager.LayoutParams lp = getActivity().getWindow().getAttributes();
                lp.alpha = 1f;
                getActivity().getWindow().setAttributes(lp);
            }
        });
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
            }
        });
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                popupWindow.dismiss();
                needDialog = true;
                selectItem = position;
                adapter.setSelectItem(selectItem);
                trainId = list.get(position).getId();
                tv.setText(list.get(position).getName());
                page = 1;
                getUsers();
            }
        });
        popupWindow.showAsDropDown(tv);
        // 设置背景颜色变暗
        WindowManager.LayoutParams lp = getActivity().getWindow().getAttributes();
        lp.alpha = 0.7f;
        getActivity().getWindow().setAttributes(lp);
    }

    private void getUsers() {
        String url = Constants.OUTRT_NET + "/m/manage/listTrainRegisterStat?trainId=" + trainId + "&page=" + page;
        addSubscription(OkHttpClientManager.getAsyn(context, url, new OkHttpClientManager.ResultCallback<TrainScoreListResult>() {
            @Override
            public void onBefore(Request request) {
                contentLayout.setVisibility(View.VISIBLE);
                tv_warn.setVisibility(View.GONE);
                loadFailView1.setVisibility(View.GONE);
                if (isRefresh || isLoadMore) {
                    xRecyclerView.setVisibility(View.VISIBLE);
                } else {
                    xRecyclerView.setVisibility(View.GONE);
                }
                if (needDialog) {
                    showTipDialog();
                }
            }

            @Override
            public void onError(Request request, Exception e) {
                hideTipDialog();
                tv_warn.setVisibility(View.GONE);
                loadFailView1.setVisibility(View.VISIBLE);
            }

            @Override
            public void onResponse(TrainScoreListResult response) {
                hideTipDialog();
                if (response != null && response.getResponseData() != null &&
                        response.getResponseData().getmTrainRegisterStat() != null
                        && response.getResponseData().getmTrainRegisterStat().size() > 0) {
                    updateScoreUI(response.getResponseData().getmTrainRegisterStat(), response.getResponseData().getPaginator());
                } else {
                    if (isRefresh) {
                        xRecyclerView.refreshComplete(true);
                    } else if (isLoadMore) {
                        xRecyclerView.loadMoreComplete(true);
                    } else {
                        tv_warn.setVisibility(View.VISIBLE);
                    }
                }
            }
        }));
    }

    @Override
    public void onRefresh() {
        isRefresh = true;
        page = 1;
        getUsers();
    }

    @Override
    public void onLoadMore() {
        isLoadMore = true;
        page += 1;
        getUsers();
    }
}
