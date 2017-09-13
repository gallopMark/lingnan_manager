package com.haoyu.app.adapter;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.haoyu.app.basehelper.BaseArrayRecyclerAdapter;
import com.haoyu.app.entity.AnnouncementEntity;
import com.haoyu.app.lingnan.manager.R;
import com.haoyu.app.swipe.SwipeMenuLayout;
import com.haoyu.app.utils.TimeUtil;

import java.util.List;

/**
 * 创建日期：2017/1/18 on 18:02
 * 描述:
 * 作者:马飞奔 Administrator
 */
public class TrainBriefAdapter extends BaseArrayRecyclerAdapter<AnnouncementEntity> {
    private onItemClickCallBack itemClickCallBack;
    private onDisposeCallBack disposeCallBack;

    public TrainBriefAdapter(List<AnnouncementEntity> mDatas) {
        super(mDatas);
    }

    public void setItemClickCallBack(onItemClickCallBack itemClickCallBack) {
        this.itemClickCallBack = itemClickCallBack;
    }

    public void setDisposeCallBack(onDisposeCallBack disposeCallBack) {
        this.disposeCallBack = disposeCallBack;
    }

    @Override
    public void onBindHoder(RecyclerHolder holder, final AnnouncementEntity entity, final int position) {
        final SwipeMenuLayout swipeLayout = holder.obtainView(R.id.swipeLayout);
        View contentView = holder.obtainView(R.id.contentView);
        TextView briefTitle = holder.obtainView(R.id.briefTitle);
        TextView briefCreateTime = holder.obtainView(R.id.briefCreateTime);
        Button bt_alter = holder.obtainView(R.id.bt_alter);
        Button bt_delete = holder.obtainView(R.id.bt_delete);
        briefTitle.setText(entity.getTitle());
        briefCreateTime.setText(TimeUtil.getSlashDate(entity.getCreateTime()));
        swipeLayout.setIos(true);
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.contentView:
                        if (swipeLayout.isExpand()) {
                            swipeLayout.smoothClose();
                        } else {
                            if (itemClickCallBack != null) {
                                itemClickCallBack.onItemCallBack(position, entity);
                            }
                        }
                        break;
                    case R.id.bt_alter:
                        swipeLayout.smoothClose();
                        if (disposeCallBack != null) {
                            disposeCallBack.onAlter(position, entity);
                        }
                        break;
                    case R.id.bt_delete:
                        swipeLayout.smoothClose();
                        if (disposeCallBack != null) {
                            disposeCallBack.onDelete(position, entity);
                        }
                        break;
                }
            }
        };
        contentView.setOnClickListener(listener);
        bt_alter.setOnClickListener(listener);
        bt_delete.setOnClickListener(listener);
    }

    @Override
    public int bindView(int viewtype) {
        return R.layout.train_brief_item;
    }

    public interface onItemClickCallBack {
        void onItemCallBack(int position, AnnouncementEntity entity);
    }

    public interface onDisposeCallBack {
        void onAlter(int position, AnnouncementEntity entity);

        void onDelete(int position, AnnouncementEntity entity);
    }
}
