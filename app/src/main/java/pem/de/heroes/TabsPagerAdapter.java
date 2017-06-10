package pem.de.heroes;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

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
            switch (index)
            {
                case 0:
                    return HelpFragment.newInstance("my");
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
