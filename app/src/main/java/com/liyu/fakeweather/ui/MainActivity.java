package com.liyu.fakeweather.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.liyu.fakeweather.AppGlobal;
import com.liyu.fakeweather.R;
import com.liyu.fakeweather.event.ThemeChangedEvent;
import com.liyu.fakeweather.ui.base.BaseActivity;
import com.liyu.fakeweather.ui.bus.BusFragment;
import com.liyu.fakeweather.ui.girl.GirlsFragment;
import com.liyu.fakeweather.ui.reading.ReadingFragment;
import com.liyu.fakeweather.ui.setting.AboutActivity;
import com.liyu.fakeweather.ui.setting.SettingActivity;
import com.liyu.fakeweather.ui.weather.WeatherFragment;
import com.liyu.fakeweather.utils.DoubleClickExit;
import com.liyu.fakeweather.utils.RxDrawer;
import com.liyu.fakeweather.utils.SimpleSubscriber;
import com.liyu.fakeweather.utils.UpdateUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import rx.android.schedulers.AndroidSchedulers;

public class MainActivity extends BaseActivity {

    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private NavigationView navigationView;
    private FragmentManager fragmentManager;
    private String currentFragmentTag;

    private static final String FRAGMENT_TAG_BUS = "bus";
    private static final String FRAGMENT_TAG_WEATHER = "weeather";
    private static final String FRAGMENT_TAG_GANK = "gank";
    private static final String FRAGMENT_TAG_READING = "reading";

    @Override
    protected int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    protected int getMenuId() {
        return 0;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        //取消标题栏
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //取消状态栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void initViews(Bundle savedInstanceState) {
        fragmentManager = getSupportFragmentManager();
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        initNavigationViewHeader();
        initFragment(savedInstanceState);
    }

    private void initFragment(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            switchContent(FRAGMENT_TAG_WEATHER);
        } else {
            currentFragmentTag = savedInstanceState.getString(AppGlobal.CURRENT_INDEX);
            switchContent(currentFragmentTag);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(AppGlobal.CURRENT_INDEX, currentFragmentTag);
    }

    @Override
    protected void loadData() {
        UpdateUtil.check(MainActivity.this, true);
    }

    public void initDrawer(Toolbar toolbar) {
        if (toolbar != null) {
            mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, toolbar, R.string.open, R.string.close) {
                @Override
                public void onDrawerOpened(View drawerView) {
                    super.onDrawerOpened(drawerView);
                }

                @Override
                public void onDrawerClosed(View drawerView) {
                    super.onDrawerClosed(drawerView);
                }
            };
            mDrawerToggle.syncState();
            mDrawerLayout.addDrawerListener(mDrawerToggle);
        }
    }

    private void initNavigationViewHeader() {
        navigationView = (NavigationView) findViewById(R.id.navigation);
        navigationView.inflateHeaderView(R.layout.drawer_header);
        navigationView.setNavigationItemSelectedListener(new NavigationItemSelected());
    }

    class NavigationItemSelected implements NavigationView.OnNavigationItemSelectedListener {
        @Override
        public boolean onNavigationItemSelected(final MenuItem menuItem) {
            RxDrawer.close(mDrawerLayout).observeOn(AndroidSchedulers.mainThread()).subscribe(
                    new SimpleSubscriber<Void>() {
                        @Override
                        public void onNext(Void aVoid) {
                            switch (menuItem.getItemId()) {
                                case R.id.navigation_item_1:
                                    menuItem.setChecked(true);
                                    switchContent(FRAGMENT_TAG_BUS);
                                    break;
                                case R.id.navigation_item_2:
                                    menuItem.setChecked(true);
                                    switchContent(FRAGMENT_TAG_WEATHER);
                                    break;
                                case R.id.navigation_item_3:
                                    menuItem.setChecked(true);
                                    switchContent(FRAGMENT_TAG_GANK);
                                    break;
                                case R.id.navigation_item_4:
                                    menuItem.setChecked(true);
                                    switchContent(FRAGMENT_TAG_READING);
                                    break;
                                case R.id.navigation_item_settings:
                                    startActivity(new Intent(MainActivity.this, SettingActivity.class));
                                    break;
                                case R.id.navigation_item_about:
                                    startActivity(new Intent(MainActivity.this, AboutActivity.class));
                                    break;
                            }
                        }
                    });
            return false;
        }
    }

    public void switchContent(String name) {
        if (currentFragmentTag != null && currentFragmentTag.equals(name))
            return;

        FragmentTransaction ft = fragmentManager.beginTransaction();
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE);
        Fragment currentFragment = fragmentManager.findFragmentByTag(currentFragmentTag);
        if (currentFragment != null) {
            ft.hide(currentFragment);
        }

        Fragment foundFragment = fragmentManager.findFragmentByTag(name);

        if (foundFragment == null) {
            switch (name) {
                case FRAGMENT_TAG_BUS:
                    foundFragment = new BusFragment();
                    break;
                case FRAGMENT_TAG_WEATHER:
                    foundFragment = new WeatherFragment();
                    break;
                case FRAGMENT_TAG_GANK:
                    foundFragment = new GirlsFragment();
                    break;
                case FRAGMENT_TAG_READING:
                    foundFragment = new ReadingFragment();
                    break;
            }
        }

        if (foundFragment == null) {

        } else if (foundFragment.isAdded()) {
            ft.show(foundFragment);
        } else {
            ft.add(R.id.contentLayout, foundFragment, name);
        }
        ft.commit();
        currentFragmentTag = name;
        invalidateOptionsMenu();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onThemeChanged(ThemeChangedEvent event) {
        this.recreate();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    @Override
    protected void onDestroy() {
        if (EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            if (!DoubleClickExit.check()) {
                Snackbar.make(MainActivity.this.getWindow().getDecorView().findViewById(android.R.id.content), "再按一次退出 App!", Snackbar.LENGTH_SHORT).show();
            } else {
                super.onBackPressed();
                System.exit(0);
            }
        }
    }
}
