package com.haoyu.app.activity;

import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.google.gson.Gson;
import com.haoyu.app.adapter.MyTrainAdapter;
import com.haoyu.app.base.BaseActivity;
import com.haoyu.app.base.BaseResponseResult;
import com.haoyu.app.base.ExitApplication;
import com.haoyu.app.entity.CaptureResult;
import com.haoyu.app.entity.MobileUser;
import com.haoyu.app.entity.MyTrainListResult;
import com.haoyu.app.entity.MyTrainMobileEntity;
import com.haoyu.app.entity.UserInfoResult;
import com.haoyu.app.fragment.TrainBriefFragment;
import com.haoyu.app.fragment.TrainMonitorFragment;
import com.haoyu.app.fragment.TrainProcessFragment;
import com.haoyu.app.fragment.TrainStudentScoreFragment;
import com.haoyu.app.imageloader.GlideImgManager;
import com.haoyu.app.lingnan.manager.R;
import com.haoyu.app.rxBus.MessageEvent;
import com.haoyu.app.rxBus.RxBus;
import com.haoyu.app.utils.Action;
import com.haoyu.app.utils.Constants;
import com.haoyu.app.utils.OkHttpClientManager;
import com.haoyu.app.view.LoadFailView;
import com.haoyu.app.view.LoadingView;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.uuzuche.lib_zxing.activity.CodeUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import butterknife.BindView;
import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Request;

/**
 * 创建日期：2017/1/16 on 18:30
 * 描述:
 * 作者:马飞奔 Administrator
 */
public class ManagerHomePageActivity extends BaseActivity implements View.OnClickListener {
    private ManagerHomePageActivity context = this;
    @BindView(R.id.toggle)
    ImageView toggle;
    @BindView(R.id.iv_scan)
    ImageView iv_scan;
    @BindView(R.id.iv_msg)
    ImageView iv_msg;
    @BindView(R.id.loadingView)
    LoadingView loadingView;
    @BindView(R.id.loadFailView)
    LoadFailView loadFailView;
    @BindView(R.id.tv_empty)
    TextView tv_empty;
    @BindView(R.id.contentView)
    LinearLayout contentView;
    @BindView(R.id.tv_myTrain)
    TextView tv_myTrain;
    private SlidingMenu menu;
    private View MenuView;
    private ImageView iv_userIco;   //侧滑菜单用户头像
    private TextView tv_userName;   //侧滑菜单用户名
    private TextView tv_deptName;   //侧滑菜单用户部门名称
    @BindView(R.id.radioGroup)
    RadioGroup radioGroup;
    private int checkIndex = 1;
    private FragmentManager fragmentManager;
    private List<Fragment> fragments = new ArrayList<>();
    private TrainMonitorFragment mMonitorFragment;
    private TrainStudentScoreFragment mStudentScoreFragment;
    private TrainProcessFragment mProcessFragment;
    private TrainBriefFragment mBriefFragment;
    private List<MyTrainMobileEntity> myTrains = new ArrayList<>();
    private String trainId;
    private int selectItem;
    private final static int SCANNIN_GREQUEST_CODE = 1;

    @Override
    public int setLayoutResID() {
        return R.layout.activity_manager_home_page;
    }

    @Override
    public void initView() {
        menu = new SlidingMenu(this);
        menu.setMode(SlidingMenu.LEFT);
        // 设置触摸屏幕的模式
        menu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
        menu.setShadowWidthRes(R.dimen.shadow_width);
        // 设置滑动菜单视图的宽度
        menu.setBehindOffsetRes(R.dimen.slidingmenu_offset);
        // 设置渐入渐出效果的值
        menu.setFadeDegree(0.35f);
        menu.attachToActivity(this, SlidingMenu.SLIDING_CONTENT);
        //为侧滑菜单设置布局

        MenuView = getLayoutInflater().inflate(R.layout.app_homepage_menu, null);

        initMenuView(MenuView);
        menu.setMenu(MenuView);
        fragmentManager = getSupportFragmentManager();
        registRxBus();
    }

    private void initMenuView(View menuView) {
        View ll_userInfo = getView(menuView, R.id.ll_userInfo);
        ll_userInfo.setOnClickListener(context);
        iv_userIco = getView(menuView, R.id.iv_userIco);
        GlideImgManager.loadCircleImage(context, getAvatar(), R.drawable.user_default,
                R.drawable.user_default, iv_userIco);
        tv_userName = getView(menuView, R.id.tv_userName);
        tv_deptName = getView(menuView, R.id.tv_deptName);
        if (TextUtils.isEmpty(getRealName()))
            tv_userName.setText("请填写用户名");
        else
            tv_userName.setText(getRealName());
        if (TextUtils.isEmpty(getDeptName()))
            tv_deptName.setText("请选择单位");
        else
            tv_deptName.setText(getDeptName());
        TextView tv_monitor = getView(menuView, R.id.tv_monitor);
        tv_monitor.setOnClickListener(context);
        TextView tv_teaching = getView(menuView, R.id.tv_teaching);
        tv_teaching.setOnClickListener(context);
        TextView tv_message = getView(menuView, R.id.tv_message);
        tv_message.setOnClickListener(context);
        TextView tv_consulting = getView(menuView, R.id.tv_consulting);
        tv_consulting.setOnClickListener(context);
        TextView tv_settings = getView(menuView, R.id.tv_settings);
        tv_settings.setOnClickListener(context);
        getUserInfo();
    }

    private void getUserInfo() {
        String url = Constants.OUTRT_NET + "/m/user/" + getUserId();
        addSubscription(OkHttpClientManager.getAsyn(context, url, new OkHttpClientManager.ResultCallback<UserInfoResult>() {

            @Override
            public void onError(Request request, Exception e) {
            }

            @Override
            public void onResponse(UserInfoResult response) {
                if (response != null && response.getResponseData() != null) {
                    updateUI(response.getResponseData());
                }
            }
        }));
    }

    private void updateUI(MobileUser user) {
        GlideImgManager.loadCircleImage(context.getApplicationContext(), user.getAvatar(), R.drawable.user_default,
                R.drawable.user_default, iv_userIco);
        if (TextUtils.isEmpty(user.getRealName()))
            tv_userName.setText("请填写用户名");
        else
            tv_userName.setText(user.getRealName());
        if (user.getmDepartment() != null && user.getmDepartment().getDeptName() != null)
            tv_deptName.setText(user.getmDepartment().getDeptName());
        else
            tv_deptName.setText("请选择单位");
    }

    public void initData() {
        String url = Constants.OUTRT_NET + "/m/manage/listProject";
        addSubscription(OkHttpClientManager.getAsyn(context, url, new OkHttpClientManager.ResultCallback<MyTrainListResult>() {
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
            public void onResponse(MyTrainListResult response) {
                loadingView.setVisibility(View.GONE);
                if (response != null && response.getResponseData() != null && response.getResponseData().size() > 0) {
                    contentView.setVisibility(View.VISIBLE);
                    updateTrainListUI(response.getResponseData());
                } else {
                    tv_empty.setVisibility(View.VISIBLE);
                }
            }
        }));
    }


    /*更新我的培训列表*/
    private void updateTrainListUI(List<MyTrainMobileEntity> responseData) {
        myTrains.addAll(responseData);
        tv_myTrain.setText(myTrains.get(0).getName());
        trainId = myTrains.get(0).getId();
        selectItem = 0;
        setTabIndex(checkIndex);
    }

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
        View view = getLayoutInflater().inflate(R.layout.popupwindow_listview,
                null);
        final PopupWindow popupWindow = new PopupWindow(view, LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        popupWindow.setFocusable(true);
        popupWindow.setBackgroundDrawable(new BitmapDrawable());
        popupWindow.setOutsideTouchable(true);
        ListView lv = view.findViewById(R.id.listView);
        final MyTrainAdapter adapter = new MyTrainAdapter(context, list, selectItem);
        lv.setAdapter(adapter);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                if (popupWindow != null) {
                    popupWindow.dismiss();
                }
                selectItem = position;
                adapter.setSelectItem(selectItem);
                trainId = list.get(position).getId();
                tv.setText(list.get(position).getName());
                MessageEvent event = new MessageEvent();
                event.action = Action.UPDATE_TRAIN;
                event.obj = list.get(position).getId();
                RxBus.getDefault().post(event);
            }
        });
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                tv.setCompoundDrawables(null, null, zhankai, null);
                WindowManager.LayoutParams lp = getWindow().getAttributes();
                lp.alpha = 1f;
                getWindow().setAttributes(lp);
            }
        });
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
            }
        });
        popupWindow.showAsDropDown(tv);
        // 设置背景颜色变暗
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.alpha = 0.7f;
        getWindow().setAttributes(lp);
    }

    @Override
    public void setListener() {
        loadFailView.setOnRetryListener(new LoadFailView.OnRetryListener() {
            @Override
            public void onRetry(View v) {
                initData();
            }
        });
        tv_myTrain.setOnClickListener(context);
        iv_msg.setOnClickListener(context);
        iv_scan.setOnClickListener(context);
        toggle.setOnClickListener(context);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.rb_train_monitor:
                        checkIndex = 1;
                        break;
                    case R.id.rb_train_student_score:
                        checkIndex = 2;
                        break;
                    case R.id.rb_train_train_process:
                        checkIndex = 3;
                        break;
                    case R.id.rb_train_projectbrief:
                        checkIndex = 4;
                        break;
                }
                setTabIndex(checkIndex);
            }
        });
    }

    private void setTabIndex(int checkIndex) {
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        hideFragments(transaction);
        Bundle bundle = new Bundle();
        bundle.putString("trainId", trainId);
        switch (checkIndex) {
            case 1:
                if (mMonitorFragment == null) {
                    mMonitorFragment = new TrainMonitorFragment();
                    mMonitorFragment.setArguments(bundle);
                    transaction.add(R.id.content, mMonitorFragment);
                    fragments.add(mMonitorFragment);
                } else {
                    transaction.show(mMonitorFragment);
                }
                break;
            case 2:
                if (mStudentScoreFragment == null) {
                    mStudentScoreFragment = new TrainStudentScoreFragment();
                    mStudentScoreFragment.setArguments(bundle);
                    transaction.add(R.id.content, mStudentScoreFragment);
                    fragments.add(mStudentScoreFragment);
                } else {
                    transaction.show(mStudentScoreFragment);
                }
                break;
            case 3:
                if (mProcessFragment == null) {
                    mProcessFragment = new TrainProcessFragment();
                    mProcessFragment.setArguments(bundle);
                    transaction.add(R.id.content, mProcessFragment);
                    fragments.add(mProcessFragment);
                } else {
                    transaction.show(mProcessFragment);
                }
                break;
            case 4:
                if (mBriefFragment == null) {
                    mBriefFragment = new TrainBriefFragment();
                    mBriefFragment.setArguments(bundle);
                    transaction.add(R.id.content, mBriefFragment);
                    fragments.add(mBriefFragment);
                } else {
                    transaction.show(mBriefFragment);
                }
                break;
        }
        transaction.commitAllowingStateLoss();
    }

    private void hideFragments(FragmentTransaction transaction) {
        for (Fragment fragment : fragments) {
            if (fragment != null) {
                transaction.hide(fragment);
            }
        }
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent();
        switch (v.getId()) {
            case R.id.iv_scan:
                Intent cameraIntent = new Intent(context, AppCaptureActivity.class);
                startActivityForResult(cameraIntent, SCANNIN_GREQUEST_CODE);
                break;
            case R.id.iv_msg:
                StringBuilder sb = new StringBuilder();
                if (myTrains.size() > 0) {
                    for (int i = 0; i < myTrains.size(); i++) {
                        sb.append(myTrains.get(i).getId());
                        sb.append(",");
                    }
                    sb.deleteCharAt(sb.lastIndexOf(","));
                }
                String trainId = sb.toString();
                intent.setClass(context, AnnouncementActivity.class);
                intent.putExtra("relationId", trainId);
                startActivity(intent);
                break;
            case R.id.toggle:
                menu.toggle(true);
                break;
            case R.id.tv_myTrain:
                setPopupView(tv_myTrain, myTrains);
                break;
            case R.id.ll_userInfo:   //侧滑菜单个人信息
                startActivity(new Intent(context, AppUserInfoActivity.class));
                break;
            case R.id.tv_monitor:  //侧滑菜单监测
                menu.toggle(true);
                break;
            case R.id.tv_teaching:  //侧滑菜单教研
                startActivity(new Intent(context, TeachingResearchActivity.class));
                break;
            case R.id.tv_message:  //侧滑菜单消息
                intent.setClass(context, MessageActivity.class);
                startActivity(intent);
                break;
            case R.id.tv_consulting:  //侧滑菜单教务咨询
                startActivity(new Intent(context, EducationConsultActivity.class));
                break;
            case R.id.tv_settings:  //侧滑菜单设置
                intent.setClass(context, SettingActivity.class);
                startActivity(intent);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        /**
         * 处理二维码扫描结果
         */
        if (requestCode == SCANNIN_GREQUEST_CODE) {
            //处理扫描结果（在界面上显示）
            if (null != data) {
                Bundle bundle = data.getExtras();
                if (bundle == null) {
                    return;
                }
                if (bundle.getInt(CodeUtils.RESULT_TYPE) == CodeUtils.RESULT_SUCCESS) {
                    String result = bundle.getString(CodeUtils.RESULT_STRING);
                    parseCaptureResult(result);
                } else if (bundle.getInt(CodeUtils.RESULT_TYPE) == CodeUtils.RESULT_FAILED) {
                    toast(context, "解析二维码失败");
                }
            }
        }
    }

    private void parseCaptureResult(String result) {
        if (result.contains("qtId") && result.contains("service")) {   //扫一扫登录
            try {
                Gson gson = new Gson();
                CaptureResult mCaptureResult = gson.fromJson(result, CaptureResult.class);
                String qtId = mCaptureResult.getQtId();
                String service = mCaptureResult.getService();
                String url = Constants.LOGIN_URL;
                login(url, qtId, service);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if ((result.startsWith("http") || result.startsWith("https"))) {  //扫一扫签到
            if (result.contains(Constants.REFERER))
                signedOn(result);
            else {
                Intent intent = new Intent(context, WebActivity.class);
                intent.putExtra("url", result);
                startActivity(intent);
            }
        } else {
            showMaterialDialog("扫描结果", result);
        }
    }

    private void login(final String url, final String qtId, final String service) {
        showLoadingDialog("登录验证");
        Flowable.fromCallable(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return OkHttpClientManager.getInstance().scanLogin(context, url, qtId, service);
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean isSuccessful) throws Exception {
                        hideLoadingDialog();
                        if (isSuccessful) {
                            toast(context, "验证成功");
                        } else {
                            toast(context, "验证失败");
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        hideLoadingDialog();
                        toast(context, "验证失败");
                    }
                });
    }

    private void signedOn(String url) {
        addSubscription(OkHttpClientManager.getAsyn(context, url, new OkHttpClientManager.ResultCallback<BaseResponseResult>() {
            @Override
            public void onBefore(Request request) {
                showTipDialog();
            }

            @Override
            public void onError(Request request, Exception e) {
                hideTipDialog();
                onNetWorkError(context);
            }

            @Override
            public void onResponse(BaseResponseResult response) {
                hideTipDialog();
                if (response != null && response.getResponseCode() != null && response.getResponseCode().equals("00")) {
                    toast(context, "签到成功");
                } else {
                    if (response != null && response.getResponseMsg() != null)
                        toast(context, response.getResponseMsg());
                    else
                        toast(context, "签到失败");
                }
            }
        }));
    }

    private long mExitTime = 0;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && menu.isMenuShowing()) {
            menu.toggle(true);
            return false;
        } else if (keyCode == KeyEvent.KEYCODE_BACK && !menu.isMenuShowing()) {
            if ((System.currentTimeMillis() - mExitTime) > 2000) {
                toast(context, "再按一次退出" + getResources().getString(R.string.app_name));
                mExitTime = System.currentTimeMillis();
            } else {
                ExitApplication.getInstance().exit();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void obBusEvent(MessageEvent event) {
        if (event.action.equals(Action.CHANGE_USER_ICO) && event.obj != null && event.obj instanceof String) {
            String avatar = (String) event.obj;
            GlideImgManager.loadCircleImage(context, avatar, R.drawable.user_default, R.drawable.user_default, iv_userIco);
        } else if (event.action.equals(Action.CHANGE_USER_NAME) && event.obj != null && event.obj instanceof String) {
            String realName = (String) event.obj;
            tv_userName.setText(realName);
        } else if (event.action.equals(Action.CHANGE_DEPT_NAME) && event.obj != null && event.obj instanceof String) {
            String deptName = (String) event.obj;
            tv_deptName.setText(deptName);
        }
    }
}
