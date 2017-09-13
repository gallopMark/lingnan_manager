package com.haoyu.app.adapter;

import android.app.Activity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.haoyu.app.basehelper.BaseArrayRecyclerAdapter;
import com.haoyu.app.entity.MFileInfo;
import com.haoyu.app.lingnan.manager.R;
import com.haoyu.app.utils.Common;

import java.util.List;

/**
 * Created by acer1 on 2017/2/6.
 * 研讨文件列表
 */
public class DiscussFileAdapter2 extends BaseArrayRecyclerAdapter<MFileInfo> {
    private Activity mContext;
    public OpenResourceCallBack callBack;

    public DiscussFileAdapter2(Activity context, List<MFileInfo> mDatas) {
        super(mDatas);
        this.mContext = context;

    }

    @Override
    public void onBindHoder(RecyclerHolder holder, final MFileInfo mFileInfo, int position) {
        ImageView iv_type = holder.obtainView(R.id.resourcesType);
        TextView resourcesName = holder.obtainView(R.id.resourcesName);
        TextView resourcesSize = holder.obtainView(R.id.resourcesSize);
        LinearLayout fileClick = holder.obtainView(R.id.file_click);

        fileClick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (callBack != null) {
                    callBack.open(mFileInfo);
                }

            }
        });
        resourcesName.setText(mFileInfo.getFileName());
        Common.setFileType(mFileInfo.getUrl(), iv_type);
        resourcesSize.setText(Common.FormetFileSize(mFileInfo.getFileSize()));

    }

    @Override
    public int bindView(int viewtype) {
        return R.layout.resources_item4;
    }

    public interface OpenResourceCallBack {
        void open(MFileInfo mFileInfo);

    }

    public void setCallBack(OpenResourceCallBack callBack) {
        this.callBack = callBack;
    }
}
