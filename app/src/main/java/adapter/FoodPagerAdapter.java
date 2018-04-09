package adapter;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;



import java.util.ArrayList;

import constants.QuanLyConstants;
import fragment.MenuFragment;


/**
 * Created by LieuDucManh on 4/8/2018.
 */
public class FoodPagerAdapter extends android.support.v13.app.FragmentStatePagerAdapter {

    private Context context;
    private ArrayList<String> listData = new ArrayList<>();
    private ArrayList<android.app.Fragment> mFragmentList = new ArrayList<>();

    public FoodPagerAdapter(android.app.FragmentManager fm, Context context) {
        super(fm);
        this.context = context;
    }


    @Override
    public int getCount() {
        return mFragmentList.size();
    }

    public void addFragment(android.app.Fragment fragment, String title){
        mFragmentList.add(fragment);
        listData.add(title);
    }


    @Override
    public CharSequence getPageTitle(int position) {
        return listData.get(position);
    }

    @Override
    public android.app.Fragment getItem(int position) {
        android.app.Fragment fragment = mFragmentList.get(position);
        Bundle bundle = new Bundle();
        bundle.putCharSequence(QuanLyConstants.FOOD_TYPE,getPageTitle(position));
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }
}
