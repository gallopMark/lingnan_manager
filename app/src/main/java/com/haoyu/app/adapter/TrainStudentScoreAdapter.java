package com.haoyu.app.adapter;

import android.content.Context;
import android.widget.ImageView;
import android.widget.TextView;

import com.haoyu.app.basehelper.BaseArrayRecyclerAdapter;
import com.haoyu.app.entity.MTrainRegisterStat;
import com.haoyu.app.lingnan.manager.R;
import com.haoyu.app.imageloader.GlideImgManager;

import java.util.List;

/**
 * Created by acer1 on 2017/1/16.
 */
public class TrainStudentScoreAdapter extends BaseArrayRecyclerAdapter<MTrainRegisterStat> {
    private Context mContext;

    public TrainStudentScoreAdapter(Context context, List<MTrainRegisterStat> mDatas) {
        super(mDatas);
        this.mContext = context;
    }

    @Override
    public int bindView(int viewtype) {
        return R.layout.studentscore_item;
    }

    @Override
    public void onBindHoder(RecyclerHolder holder, MTrainRegisterStat entity, int position) {
        ImageView personImg = holder.obtainView(R.id.person_img);
        TextView personName = holder.obtainView(R.id.person_name);
        TextView personHours = holder.obtainView(R.id.person_hours);
        TextView personSchool = holder.obtainView(R.id.person_school);
        if (entity.getmUser() != null) {
            personName.setText(entity.getmUser().getRealName());
            personSchool.setText(entity.getmUser().getDeptName());
        } else {
            personName.setText(null);
            personSchool.setText(null);
        }
        if (entity.getmUser() != null && entity.getmUser().getAvatar() != null) {
            GlideImgManager.loadCircleImage(mContext, entity.getmUser().getAvatar(), R.drawable.user_default, R.drawable.user_default, personImg);
        } else {
            personImg.setImageResource(R.drawable.user_default);
        }
        personHours.setText(entity.getTotalStudyHours() + "学时");
    }
}
