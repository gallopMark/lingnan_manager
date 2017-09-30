package com.haoyu.app.activity;

import android.view.View;
import android.widget.TextView;

import com.haoyu.app.base.BaseActivity;
import com.haoyu.app.base.BaseResponseResult;
import com.haoyu.app.entity.AnnouncementEntity;
import com.haoyu.app.lingnan.manager.R;
import com.haoyu.app.utils.Constants;
import com.haoyu.app.utils.OkHttpClientManager;
import com.haoyu.app.utils.TimeUtil;
import com.haoyu.app.view.AppToolBar;

import org.sufficientlysecure.htmltextview.HtmlHttpImageGetter;
import org.sufficientlysecure.htmltextview.HtmlTextView;

import butterknife.BindView;
import okhttp3.Request;

/**
 * 创建日期：2017/3/23 on 11:31
 * 描述:简报详情
 * 作者:马飞奔 Administrator
 */
public class BriefDetailActivity extends BaseActivity {
    private BriefDetailActivity context = this;
    @BindView(R.id.toolBar)
    AppToolBar toolBar;
    @BindView(R.id.tv_title)
    TextView tv_title;
    @BindView(R.id.tv_createDate)
    TextView tv_createDate;
    @BindView(R.id.tv_content)
    HtmlTextView tv_content;
    private String relationId;

    @Override
    public int setLayoutResID() {
        return R.layout.activity_brief_detail;
    }

    @Override
    public void initView() {
        relationId = getIntent().getStringExtra("relationId");
    }

    /*获取通知公告详细*/
    public void initData() {
        String url = Constants.OUTRT_NET + "/m/announcement/view/" + relationId;
        addSubscription(OkHttpClientManager.getAsyn(context, url, new OkHttpClientManager.ResultCallback<BaseResponseResult<AnnouncementEntity>>() {
            @Override
            public void onBefore(Request request) {
                showTipDialog();
            }

            @Override
            public void onError(Request request, Exception e) {
                hideTipDialog();
            }

            @Override
            public void onResponse(BaseResponseResult<AnnouncementEntity> response) {
                hideTipDialog();
                if (response != null && response.getResponseData() != null) {
                    updateUI(response.getResponseData());
                }
            }
        }));
    }

    private void updateUI(AnnouncementEntity entity) {
        tv_title.setText(entity.getTitle());
        tv_createDate.setText(TimeUtil.getDateHR(entity.getCreateTime()));
        tv_content.setHtml(entity.getContent(), new HtmlHttpImageGetter(tv_content, Constants.REFERER));
    }

    @Override
    public void setListener() {
        toolBar.setOnLeftClickListener(new AppToolBar.OnLeftClickListener() {
            @Override
            public void onLeftClick(View view) {
                finish();
            }
        });
    }
}
