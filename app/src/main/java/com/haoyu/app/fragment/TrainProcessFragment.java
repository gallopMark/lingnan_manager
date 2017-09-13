package com.haoyu.app.fragment;

import android.view.View;
import android.widget.TextView;

import com.haoyu.app.base.BaseFragment;
import com.haoyu.app.entity.TrainProcessResult;
import com.haoyu.app.lingnan.manager.R;
import com.haoyu.app.utils.Common;
import com.haoyu.app.utils.Constants;
import com.haoyu.app.utils.OkHttpClientManager;
import com.haoyu.app.view.LoadFailView;
import com.haoyu.app.view.LoadingView;

import java.text.DecimalFormat;

import okhttp3.Request;

/**
 * 创建日期：2017/1/16 on 19:29
 * 描述: 过程监测
 * 作者:马飞奔 Administrator
 */
public class TrainProcessFragment extends BaseFragment {
    private LoadingView loadView;
    private LoadFailView loadFailView;
    private View content;
    private TextView tv_trainCount; //项目开展总数
    private TextView tv_trainPeriod; //项目培训期次
    private TextView tv_teacherCount; //师资总人数
    private TextView tv_studentCount; //学员总人数
    private TextView tv_joineNum;  //培训参与人数
    private TextView tv_Registerlv; //参训率
    private TextView tv_registCount; //平台培训人次
    private TextView tv_participation; //人均参训次数

    @Override
    public int createView() {
        return R.layout.fragment_train_process;
    }

    @Override
    public void initData() {
        String url = Constants.OUTRT_NET + "/m/manage/getSystemStat";
        addSubscription(OkHttpClientManager.getAsyn(context, url, new OkHttpClientManager.ResultCallback<TrainProcessResult>() {
            @Override
            public void onBefore(Request request) {
                loadView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onError(Request request, Exception e) {
                loadView.setVisibility(View.GONE);
                loadFailView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onResponse(TrainProcessResult response) {
                loadView.setVisibility(View.GONE);
                content.setVisibility(View.VISIBLE);
                if (response != null && response.getResponseData() != null) {
                    updateUI(response.getResponseData());
                }
            }
        }));
    }

    private void updateUI(TrainProcessResult.TrainProcessData responseData) {
        String trainCount = responseData.getProjectNum() >= 10 ? format(responseData.getProjectNum())
                : "0" + responseData.getProjectNum();
        tv_trainCount.setText(trainCount);
        String trainPeriod = responseData.getTrainNum() >= 10 ? format(responseData.getTrainNum())
                : "0" + responseData.getTrainNum();
        tv_trainPeriod.setText(trainPeriod);
        String teacherCount = responseData.getUserTeacherNum() >= 10 ? format(responseData.getUserTeacherNum())
                : "0" + responseData.getUserTeacherNum();
        tv_teacherCount.setText(teacherCount);
        String studentCount = responseData.getStudentNum() >= 10 ? format(responseData.getStudentNum())
                : "0" + responseData.getStudentNum();
        tv_studentCount.setText(studentCount);
        String joinNum = responseData.getJoinedTrainStudentNum() >= 10 ? format(responseData.getJoinedTrainStudentNum())
                : "0" + responseData.getJoinedTrainStudentNum();
        tv_joineNum.setText(joinNum);
        if (responseData.getStudentNum() > 0) {
            tv_Registerlv.setText(Common.accuracy(responseData.getJoinedTrainStudentNum(), responseData.getStudentNum(), 2));
        } else {
            tv_Registerlv.setText(0 + "%");
        }
        String registNum = responseData.getTrainRegisterNum() >= 10 ? format(responseData.getTrainRegisterNum()) :
                "0" + responseData.getTrainRegisterNum();
        tv_registCount.setText(registNum);
        if (responseData.getStudentNum() > 0) {
            tv_participation.setText(getRegistLV(responseData.getTrainRegisterNum(), responseData.getStudentNum()));
        }
    }

    private String format(int num) {
        DecimalFormat f = new DecimalFormat(",###");
        return f.format(num);
    }

    private String getRegistLV(int registNum, int totalNum) {
        float num = (float) registNum / totalNum;
        DecimalFormat df = new DecimalFormat("0.00");
        return df.format(num);
    }

    @Override
    public void initView(View view) {
        loadView = getView(view, R.id.loadView);
        loadFailView = getView(view, R.id.loadFailView);
        content = view.findViewById(R.id.content);
        tv_trainCount = getView(view, R.id.tv_trainCount);
        tv_trainPeriod = getView(view, R.id.tv_trainPeriod);
        tv_teacherCount = getView(view, R.id.tv_teacherCount);
        tv_studentCount = getView(view, R.id.tv_studentCount);
        tv_joineNum = getView(view, R.id.tv_joineNum);
        tv_Registerlv = getView(view, R.id.tv_Registerlv);
        tv_registCount = getView(view, R.id.tv_registCount);
        tv_participation = getView(view, R.id.tv_participation);
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
