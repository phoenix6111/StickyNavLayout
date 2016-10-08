package com.wanghaisheng.stickynavlayout;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    List<TabFragment> mTabFragments = new ArrayList<>();

    String[] tabTitles = {"简介", "评价", "相关"};
    TabLayout mTabLayout;
    ViewPager mViewPager;
    FragmentPagerAdapter mPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();

        initDatas();

    }

    private void initDatas() {
        for(String str : tabTitles) {
            TabFragment tabFragment = TabFragment.newInstance(str);
            mTabFragments.add(tabFragment);
        }

        mPagerAdapter = new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                return mTabFragments.get(position);
            }

            @Override
            public int getCount() {
                return mTabFragments.size();
            }

            @Override
            public CharSequence getPageTitle(int position) {
                return tabTitles[position];
            }
        };

        mViewPager.setAdapter(mPagerAdapter);
        mTabLayout.setupWithViewPager(mViewPager);
    }

    private void initViews() {
        mTabLayout = (TabLayout) findViewById(R.id.stickynavlayout_indicator);
        mViewPager = (ViewPager) findViewById(R.id.stickynavlayout_viewpager);
    }


}
