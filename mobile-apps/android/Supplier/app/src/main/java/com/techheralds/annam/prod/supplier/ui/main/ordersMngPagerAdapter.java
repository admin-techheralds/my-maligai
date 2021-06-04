
package com.techheralds.annam.prod.supplier.ui.main;

        import android.content.Context;

        import androidx.annotation.Nullable;
        import androidx.annotation.StringRes;
        import androidx.fragment.app.Fragment;
        import androidx.fragment.app.FragmentManager;
        import androidx.fragment.app.FragmentPagerAdapter;

        import com.techheralds.annam.prod.supplier.FirstFragment;
        import com.techheralds.annam.prod.supplier.FourthFragment;
        import com.techheralds.annam.prod.supplier.R;
        import com.techheralds.annam.prod.supplier.SecondFragment;
        import com.techheralds.annam.prod.supplier.ThirdFragment;

/**
 * A [FragmentPagerAdapter] that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
public class ordersMngPagerAdapter extends FragmentPagerAdapter {

    @StringRes
    private static final int[] TAB_TITLES = new int[]{R.string.tab_text_3 ,R.string.tab_text_4 };
    private final Context mContext;

    public ordersMngPagerAdapter(Context context, FragmentManager fm) {
        super(fm);
        mContext = context;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                ThirdFragment thirdFragment = new ThirdFragment();
                return thirdFragment;
            case 1:
                FourthFragment fourthFragment = new FourthFragment();
                return fourthFragment;
            default:
                return null;
        }
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return mContext.getResources().getString(TAB_TITLES[position]);
    }

    @Override
    public int getCount() {

        return 2;
    }
}