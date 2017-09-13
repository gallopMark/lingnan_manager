package com.haoyu.app.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.utils.ViewPortHandler;
import com.haoyu.app.base.BaseFragment;
import com.haoyu.app.entity.TrainMonitorResult;
import com.haoyu.app.lingnan.manager.R;
import com.haoyu.app.rxBus.MessageEvent;
import com.haoyu.app.utils.Action;
import com.haoyu.app.utils.Common;
import com.haoyu.app.utils.Constants;
import com.haoyu.app.utils.OkHttpClientManager;
import com.haoyu.app.view.ColorArcProgressBar;
import com.haoyu.app.view.LoadFailView;
import com.haoyu.app.view.LoadingView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Request;

/**
 * 创建日期：2017/1/16 on 19:27
 * 描述: 培训监控
 * 作者:马飞奔 Administrator
 */
public class TrainMonitorFragment extends BaseFragment {
    private LoadFailView loadFailView;
    private LoadingView loadView;
    private TextView tv_empty;
    private LinearLayout content;
    private BarChart mChart;
    private View tv_apply;
    private TextView tv_percentPass;
    private ColorArcProgressBar progressBar;
    private String trainId;
    private Map<String, TrainMonitorResult> resultMap = new HashMap<>();

    @Override
    public void obBusEvent(MessageEvent event) {
        if (event.getAction().equals(Action.UPDATE_TRAIN) && event.obj != null && event.obj instanceof String) {
            trainId = (String) event.obj;
            initData();
        }
    }

    @Override
    public int createView() {
        return R.layout.fragment_train_monitor;
    }

    @Override
    public void initData() {
        if (resultMap.get(trainId) != null && resultMap.get(trainId) instanceof TrainMonitorResult) {
            TrainMonitorResult response = resultMap.get(trainId);
            if (response != null && response.getResponseData() != null) {
                content.setVisibility(View.VISIBLE);
                updateUI(response.getResponseData());
            } else {
                tv_empty.setVisibility(View.GONE);
            }
        } else {
            String url = Constants.OUTRT_NET + "/m/manage/getProjectStatistics?projectId=" + trainId;
            OkHttpClientManager.getAsyn(context, url, new OkHttpClientManager.ResultCallback<TrainMonitorResult>() {
                @Override
                public void onBefore(Request request) {
                    super.onBefore(request);
                    loadView.setVisibility(View.VISIBLE);
                    content.setVisibility(View.GONE);
                }

                @Override
                public void onError(Request request, Exception e) {
                    loadView.setVisibility(View.GONE);
                    loadFailView.setVisibility(View.VISIBLE);
                }

                @Override
                public void onResponse(TrainMonitorResult response) {
                    resultMap.put(trainId, response);
                    loadView.setVisibility(View.GONE);
                    if (response != null && response.getResponseData() != null) {
                        content.setVisibility(View.VISIBLE);
                        updateUI(response.getResponseData());
                    } else {
                        tv_empty.setVisibility(View.GONE);
                    }
                }
            });
        }
    }

    private void updateUI(TrainMonitorResult.TrainMonitorData responseData) {
        ArrayList<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(0, responseData.getRegisterHeadcount()));
        entries.add(new BarEntry(1, responseData.getParticipateHeadcount()));
        entries.add(new BarEntry(2, responseData.getPassHeadcount()));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams
                (0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        int maxValue = getMax(responseData.getRegisterHeadcount(), responseData.getParticipateHeadcount(), responseData.getPassHeadcount());
        if (maxValue < 100) {
            params.leftMargin = (int) getResources().getDimension(R.dimen.margin_size_6);
        } else if (100 < maxValue && maxValue < 1000) {
            params.leftMargin = (int) getResources().getDimension(R.dimen.margin_size_18);
        } else if (1000 < maxValue && maxValue < 10000) {
            params.leftMargin = (int) getResources().getDimension(R.dimen.margin_size_24);
        } else {
            params.leftMargin = (int) getResources().getDimension(R.dimen.margin_size_30);
        }
        tv_apply.setLayoutParams(params);
        int[] colors = {Color.rgb(98, 181, 67), Color.rgb(255, 200, 35), Color.rgb(13, 131, 254)};
        List<Integer> valueColors = new ArrayList<>();
        valueColors.add(Color.rgb(98, 181, 67));
        valueColors.add(Color.rgb(255, 200, 35));
        valueColors.add(Color.rgb(13, 131, 254));
        BarDataSet dataSet = new BarDataSet(entries, null);
        dataSet.setColors(colors);
        dataSet.setValueTextSize(16);
        dataSet.setValueTextColors(valueColors);
        dataSet.setBarShadowColor(Color.rgb(203, 203, 203));
        dataSet.setValueFormatter(new MyValueFormatter());
        dataSet.setDrawValues(true);
        ArrayList<IBarDataSet> sets = new ArrayList<>();
        sets.add(dataSet);
        BarData cd = new BarData(sets);
        cd.setBarWidth(0.5f);
        mChart.setData(cd);
        mChart.invalidate();
        progressBar.setMaxValues(responseData.getParticipateHeadcount());
        progressBar.setCurrentValues(responseData.getPassHeadcount());
        if (responseData.getParticipateHeadcount() > 0)
            tv_percentPass.setText(Common.accuracy(responseData.getPassHeadcount(), responseData.getParticipateHeadcount(), 0) + "\n合格率");
        else
            tv_percentPass.setText(0 + "%\n合格率");
    }

    private int getMax(int a, int b, int c) {
        int max;
        if (a > b && a > c) {
            max = a;
        } else if (c > a && c > b) {
            max = c;
        } else
            max = b;
        return max;
    }

    private class MyValueFormatter implements IValueFormatter {

        @Override
        public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
            return String.valueOf((int) value);
        }
    }

    @Override
    public void initView(View view) {
        Bundle bundle = getArguments();
        if (bundle != null) {
            trainId = bundle.getString("trainId");
        }
        tv_empty = getView(view, R.id.tv_empty);
        content = getView(view, R.id.content);
        loadView = getView(view, R.id.loadView);
        loadFailView = getView(view, R.id.loadFailView);
        mChart = getView(view, R.id.barChart);
        tv_apply = getView(view, R.id.tv_apply);
        progressBar = getView(view, R.id.progressBar);
        tv_percentPass = getView(view, R.id.tv_percentPass);
        // 1.隐藏网格线,保留水平线
        XAxis xAxis = mChart.getXAxis();
        xAxis.setDrawAxisLine(true);
        xAxis.setAxisLineColor(ContextCompat.getColor(context, R.color.spaceColor));
        xAxis.setDrawGridLines(false);
        xAxis.setXOffset(0);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);//X轴在下边
        xAxis.setDrawLabels(false);
        mChart.getAxisLeft().setDrawAxisLine(false);
        //设置比例图标的显示隐藏
        Legend legend = mChart.getLegend();
        legend.setEnabled(false);
        mChart.getAxisRight().setEnabled(false);
        mChart.setDescription(null);
        mChart.getAxisLeft().setLabelCount(5, false);
        mChart.getAxisLeft().setAxisMinimum(0f);
        mChart.getAxisLeft().setTextSize(12f);
        //设置支持触控
        mChart.setTouchEnabled(false);
        //设置是否支持拖拽
        mChart.setDragEnabled(false);
        //设置能否缩放
        mChart.setScaleEnabled(false);
        //设置true支持两个指头向X、Y轴的缩放，如果为false，只能支持X或者Y轴的当方向缩放
        mChart.setPinchZoom(false);
    }

    @Override
    public void setListener() {
        loadFailView.setOnRetryListener(new LoadFailView.OnRetryListener() {
            @Override
            public void onRetry(View v) {
                initData();
            }
        });
    }
}
