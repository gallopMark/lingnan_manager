package com.haoyu.app.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;

import com.haoyu.app.base.BaseActivity;
import com.haoyu.app.base.BaseResponseResult;
import com.haoyu.app.entity.AnnouncementEntity;
import com.haoyu.app.lingnan.manager.R;
import com.haoyu.app.utils.Common;
import com.haoyu.app.utils.Constants;
import com.haoyu.app.utils.OkHttpClientManager;
import com.haoyu.app.view.AppToolBar;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import okhttp3.Request;

/**
 * 创建日期：2017/1/19 on 9:40
 * 描述: 创建或修改培训简报
 * 作者:马飞奔 Administrator
 */
public class BriefEditActivity extends BaseActivity {
    private BriefEditActivity context = this;
    private AnnouncementEntity entity;
    private String entityId;
    @BindView(R.id.toolBar)
    AppToolBar toolBar;
    @BindView(R.id.et_content)
    EditText et_content;
    @BindView(R.id.et_title)
    EditText et_title;
    private boolean isAlter = false;

    @Override
    public int setLayoutResID() {
        return R.layout.activity_brief_edit;
    }

    @Override
    public void initView() {
        overridePendingTransition(R.anim.fade_in, 0);
        entity = (AnnouncementEntity) getIntent().getSerializableExtra("entity");
        isAlter = getIntent().getBooleanExtra("isAlter", false);
        if (entity != null) {
            entityId = entity.getId();
            et_title.setText(entity.getTitle());
            et_content.setText(entity.getContent());
        }
    }

    @Override
    public void setListener() {
        et_title.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // et.getCompoundDrawables()得到一个长度为4的数组，分别表示左右上下四张图片
                Drawable drawable = et_title.getCompoundDrawables()[2];
                //如果右边没有图片，不再处理
                if (drawable == null)
                    return false;
                //如果不是按下事件，不再处理
                if (event.getAction() != MotionEvent.ACTION_UP)
                    return false;
                if (event.getX() > et_title.getWidth()
                        - et_title.getPaddingRight()
                        - drawable.getIntrinsicWidth()) {
                    et_title.setSelection(et_title.getText().length());//将光标移至文字末尾
                }
                return false;
            }
        });
        toolBar.setOnTitleClickListener(new AppToolBar.TitleOnClickListener() {
            @Override
            public void onLeftClick(View view) {
                finish();
            }

            @Override
            public void onRightClick(View view) {
                Common.hideSoftInput(context);
                String title = et_title.getText().toString().trim();
                String content = et_content.getText().toString().trim();
                if (title.length() == 0) {
                    showMaterialDialog("提示", "请输入简报标题");
                } else if (content.length() == 0) {
                    showMaterialDialog("提示", "请输入简报内容");
                } else {
                    if (isAlter) {
                        onAlter(title, content);
                    } else {
                        onCreate(title, content);
                    }
                }
            }
        });
    }

    /*创建简报*/
    private void onCreate(String title, final String content) {
        showTipDialog();
        String url = Constants.OUTRT_NET + "/m/announcement";
        Map<String, String> map = new HashMap<>();
        map.put("title", title);
        map.put("content", content);
        map.put("type", "train_report");
        map.put("announcementRelations[0].relation.id", "system");
        addSubscription(OkHttpClientManager.postAsyn(context, url, new OkHttpClientManager.ResultCallback<BaseResponseResult<AnnouncementEntity>>() {
            @Override
            public void onError(Request request, Exception e) {
                showTipDialog();
                onNetWorkError(context);
            }

            @Override
            public void onResponse(BaseResponseResult<AnnouncementEntity> response) {
                hideTipDialog();
                if (response != null && response.getResponseData() != null) {
                    Intent intent = new Intent();
                    intent.putExtra("entity", response.getResponseData());
                    setResult(Activity.RESULT_OK, intent);
                }
                finish();
            }
        }, map));
    }

    /*修改简报*/
    private void onAlter(String title, String content) {
        showTipDialog();
        String url = Constants.OUTRT_NET + "/m/announcement/" + entityId;
        Map<String, String> map = new HashMap<>();
        map.put("_method", "put");
        map.put("title", title);
        map.put("content", content);
        addSubscription(OkHttpClientManager.postAsyn(context, url, new OkHttpClientManager.ResultCallback<BaseResponseResult<AnnouncementEntity>>() {
            @Override
            public void onError(Request request, Exception e) {
                hideTipDialog();
                onNetWorkError(context);
            }

            @Override
            public void onResponse(BaseResponseResult<AnnouncementEntity> response) {
                hideTipDialog();
                if (response != null && response.getResponseData() != null) {
                    AnnouncementEntity mEntity = response.getResponseData();
                    mEntity.setCreateTime(entity.getCreateTime());
                    Intent intent = new Intent();
                    intent.putExtra("entity", mEntity);
                    setResult(Activity.RESULT_OK, intent);
                }
                finish();
            }
        }, map));
    }

    @Override
    public void finish() {
        super.finish();
        Common.hideSoftInput(context);
        overridePendingTransition(0, R.anim.fade_out);
    }
}
