package pem.de.heroes.main;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import pem.de.heroes.profile.ProfileFragment;

/**
 * Created by Julia on 10.06.2017.
 */

public class TabsPagerAdapter extends FragmentPagerAdapter {

        public TabsPagerAdapter(FragmentManager fm)
        {
            super(fm);
        }

        @Override
        public android.support.v4.app.Fragment getItem(int index)
        {
            //depending on which index we are a different Fragment is shown
            switch (index)
            {
                case 0:
                    return new ProfileFragment();
                case 1:
                    return HelpFragment.newInstance("ask");
                case 2:
                    return HelpFragment.newInstance("offer");
            }

            return null;
        }

        @Override
        public int getCount()
        {
            return 3;
        }

}
