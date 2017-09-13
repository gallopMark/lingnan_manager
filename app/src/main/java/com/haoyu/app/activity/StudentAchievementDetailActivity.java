package com.haoyu.app.activity;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.haoyu.app.base.BaseActivity;
import com.haoyu.app.basehelper.BaseArrayRecyclerAdapter;
import com.haoyu.app.entity.MTrainRegisterStat;
import com.haoyu.app.entity.MobileUser;
import com.haoyu.app.entity.TrainScoreSingleResult;
import com.haoyu.app.lingnan.manager.R;
import com.haoyu.app.utils.Constants;
import com.haoyu.app.utils.OkHttpClientManager;
import com.haoyu.app.utils.TimeUtil;
import com.haoyu.app.view.FullyLinearLayoutManager;
import com.haoyu.app.view.LoadFailView;
import com.haoyu.app.view.LoadingView;

import java.util.List;

import butterknife.BindView;
import okhttp3.Request;


/**
 * 创建日期：2017/1/19 on 19:52
 * 描述:
 * 作者:马飞奔 Administrator
 */
public class StudentAchievementDetailActivity extends BaseActivity implements View.OnClickListener {
    private StudentAchievementDetailActivity context = this;
    @BindView(R.id.loadingView)
    LoadingView loadingView;
    @BindView(R.id.contentView)
    View contentView;
    @BindView(R.id.loadFailView)
    LoadFailView loadFailView;
    @BindView(R.id.person_name)
    TextView person_name;
    @BindView(R.id.person_unit)
    TextView person_unit;//所在单位
    @BindView(R.id.id_card)
    TextView id_card;//身份证号
    @BindView(R.id.join_project)
    TextView join_project;//参与项目
    @BindView(R.id.train_semester)
    TextView train_semester;//培训期次
    @BindView(R.id.train_time)
    TextView train_time;//培训时间
    @BindView(R.id.course_num)
    TextView course_num;//课程学习数量
    @BindView(R.id.iv_course_state)
    ImageView iv_course_state;  //课程结果类型标记
    @BindView(R.id.course_learn)
    TextView course_learn;//课程学习结果
    @BindView(R.id.workgroup_reaearch)
    TextView workgroup_reaearch;//工作坊研修数量
    @BindView(R.id.iv_workgroup_state)
    ImageView iv_workgroup_state;  //工作坊结果类型标记
    @BindView(R.id.workgroup_score)
    TextView workgroup_score;//工作坊研修成绩
    @BindView(R.id.society)
    TextView society;//社区拓展数量
    @BindView(R.id.iv_society_state)
    ImageView iv_society_state;  //社区结果类型标记
    @BindView(R.id.society_state)
    TextView society_state;//社区拓展成绩
    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    @BindView(R.id.re_back)
    ImageView re_back;//返回
    @BindView(R.id.course_hours)
    TextView course_hours;//学时
    @BindView(R.id.tv_warn)
    TextView tv_warn;
    private String trainId;
    private String trainRegisterId;

    private MobileUser mUser;

    @Override
    public int setLayoutResID() {
        return R.layout.activity_student_achievement;
    }

    @Override
    public void initView() {
        trainId = getIntent().getStringExtra("trainId");
        trainRegisterId = getIntent().getStringExtra("trainRegisterId");
        mUser = (MobileUser) getIntent().getSerializableExtra("mUser");
        recyclerView.setNestedScrollingEnabled(false);
    }

    @Override
    public void setListener() {
        re_back.setOnClickListener(this);
        loadFailView.setOnRetryListener(new LoadFailView.OnRetryListener() {
            @Override
            public void onRetry(View v) {
                initData();
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.re_back:
                this.finish();
                break;
        }
    }

    @Override
    public void initData() {
        String url = Constants.OUTRT_NET + "/m/manage/getTrainRegisterStat?trainId=" + trainId + "&trainRegisterId=" + trainRegisterId;
        addSubscription(OkHttpClientManager.getAsyn(context, url, new OkHttpClientManager.ResultCallback<TrainScoreSingleResult>() {
            @Override
            public void onBefore(Request request) {
                loadingView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onError(Request request, Exception e) {
                loadingView.setVisibility(View.GONE);
                loadFailView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onResponse(TrainScoreSingleResult response) {
                loadingView.setVisibility(View.GONE);
                if (response != null && response.getResponseData() != null) {
                    contentView.setVisibility(View.VISIBLE);
                    updateUI(response.getResponseData());
                }
            }
        }));
    }

    private void updateUI(MTrainRegisterStat responseData) {
        String msg = "暂无";
        if (responseData.getmTrain().getName() != null) {
            train_semester.setText(responseData.getmTrain().getName());
        } else {
            train_semester.setText(msg);
        }
        if (responseData.getmTrain() != null && responseData.getmTrain().getTrainingTime() != null) {
            long startTime = responseData.getmTrain().getTrainingTime().getStartTime();
            long endTime = responseData.getmTrain().getTrainingTime().getEndTime();
            train_time.setText(TimeUtil.convertDayOfMinute(startTime, endTime));
        } else {
            train_time.setText("未确定");
        }
        course_hours.setText(responseData.getTotalStudyHours() + "学时");
        if (mUser != null) {
            person_name.setText(mUser.getRealName());
            person_unit.setText(mUser.getDeptName());
        }
        if (responseData.getProjectName() != null) {
            join_project.setText(responseData.getProjectName());
        } else {
            join_project.setText(msg);
        }
        course_num.setText(String.valueOf(responseData.getRegistedCourseNum()));
        if (responseData.getCourseEvaluate() != null
                && responseData.getCourseEvaluate().equals("合格")) {
            iv_course_state.setImageResource(R.drawable.state_qualified);
            course_learn.setText(responseData.getCourseEvaluate());
        } else if (responseData.getCourseEvaluate() != null
                && responseData.getCourseEvaluate().equals("未达标")) {
            iv_course_state.setImageResource(R.drawable.state_nostandards);
        } else if (responseData.getCourseEvaluate() != null
                && responseData.getCourseEvaluate().equals("优秀")) {
            iv_course_state.setImageResource(R.drawable.state_excellent);
        } else if (responseData.getCourseEvaluate() != null
                && responseData.getCourseEvaluate().equals("待评价")) {
            iv_course_state.setImageResource(R.drawable.state_evaluate);
        } else {
            iv_course_state.setImageResource(R.drawable.state_noparticipation);
        }
        if (responseData.getCourseEvaluate() != null && responseData.getCourseEvaluate().equals("-"))
            course_learn.setText("不限学时");
        else
            course_learn.setText(responseData.getCourseEvaluate());
        workgroup_reaearch.setText(String.valueOf(responseData.getWorkshopNum()));
        if (responseData.getWorkshopEvaluate() != null
                && responseData.getWorkshopEvaluate().equals("待评价")) {
            iv_workgroup_state.setImageResource(R.drawable.state_evaluate);
        } else if (responseData.getWorkshopEvaluate() != null
                && responseData.getWorkshopEvaluate().equals("优秀")) {
            iv_workgroup_state.setImageResource(R.drawable.state_excellent);
        } else if (responseData.getWorkshopEvaluate() != null
                && responseData.getWorkshopEvaluate().equals("合格")) {
            iv_workgroup_state.setImageResource(R.drawable.state_qualified);
        } else if (responseData.getWorkshopEvaluate() != null
                && responseData.getWorkshopEvaluate().equals("未达标")) {
            iv_workgroup_state.setImageResource(R.drawable.state_nostandards);
        } else {
            iv_workgroup_state.setImageResource(R.drawable.state_noparticipation);
        }
        if (responseData.getWorkshopEvaluate() != null && responseData.getWorkshopEvaluate().equals("-"))
            workgroup_score.setText("未参与");
        else
            workgroup_score.setText(responseData.getWorkshopEvaluate());
        if (responseData.getCommunityEvaluate() != null
                && responseData.getCommunityEvaluate().equals("待评价")) {
            iv_society_state.setImageResource(R.drawable.state_evaluate);
        } else if (responseData.getCommunityEvaluate() != null
                && responseData.getCommunityEvaluate().equals("优秀")) {
            iv_society_state.setImageResource(R.drawable.state_excellent);
        } else if (responseData.getCommunityEvaluate() != null
                && responseData.getCommunityEvaluate().equals("合格")) {
            iv_society_state.setImageResource(R.drawable.state_qualified);
        } else if (responseData.getCommunityEvaluate() != null
                && responseData.getCommunityEvaluate().equals("未达标")) {
            iv_society_state.setImageResource(R.drawable.state_nostandards);
        } else {
            iv_society_state.setImageResource(R.drawable.state_noparticipation);
        }

        if (responseData.getCommunityEvaluate() != null
                && responseData.getCommunityEvaluate().equals("-"))
            society_state.setText("未参与");
        else
            society_state.setText(responseData.getCommunityEvaluate());
        if (responseData.getmCourseRegisters() != null && responseData.getmCourseRegisters().size() > 0) {
            CourseAdapter mAdapter = new CourseAdapter(responseData.getmCourseRegisters());
            FullyLinearLayoutManager manager = new FullyLinearLayoutManager(context);
            manager.setOrientation(FullyLinearLayoutManager.VERTICAL);
            recyclerView.setLayoutManager(manager);
            recyclerView.setAdapter(mAdapter);
        } else {
            recyclerView.setVisibility(View.GONE);
            tv_warn.setVisibility(View.VISIBLE);
        }
    }

    class CourseAdapter extends BaseArrayRecyclerAdapter<MTrainRegisterStat.CourseRegisters> {

        public CourseAdapter(List<MTrainRegisterStat.CourseRegisters> mDatas) {
            super(mDatas);
        }

        @Override
        public void onBindHoder(RecyclerHolder holder, MTrainRegisterStat.CourseRegisters entity, int position) {
            TextView tv_title = holder.obtainView(R.id.tv_title);
            TextView tv_score = holder.obtainView(R.id.tv_score);
            View divider = holder.obtainView(R.id.divider);
            if (position == getItemCount() - 1)
                divider.setVisibility(View.GONE);
            else
                divider.setVisibility(View.VISIBLE);
            if (entity.getmCourse() != null && entity.getmCourse().getTitle() != null)
                tv_title.setText(entity.getmCourse().getTitle());
            else
                tv_title.setText("");
            tv_score.setText(String.valueOf(entity.getScore()));
        }

        @Override
        public int bindView(int viewtype) {
            return R.layout.student_choose_achievement;
        }
    }
}
