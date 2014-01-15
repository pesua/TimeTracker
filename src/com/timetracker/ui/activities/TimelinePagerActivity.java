/*
 * Copyright 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.timetracker.ui.activities;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import com.timetracker.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class TimelinePagerActivity extends FragmentActivity {

    private static final int DISPLAY_DAYS = 14;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.timeline_pager);

        TimelinePagerAdapter mTimelinePagerAdapter = new TimelinePagerAdapter(getSupportFragmentManager());

        ViewPager pager = (ViewPager) findViewById(R.id.pager);
        pager.setAdapter(mTimelinePagerAdapter);
        pager.setCurrentItem(DISPLAY_DAYS - 1);

    }

    public static class TimelinePagerAdapter extends FragmentStatePagerAdapter {

        public TimelinePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            Fragment fragment = new TimelineReportFragment();
            Bundle args = new Bundle();
            Calendar calendar = getDay(i);
            args.putLong(TimelineReportFragment.ARG_OBJECT, calendar.getTimeInMillis());
            fragment.setArguments(args);
            return fragment;
        }

        private Calendar getDay(int i) {
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DATE, -DISPLAY_DAYS + 1 + i);
            return calendar;
        }

        @Override
        public int getCount() {
            return DISPLAY_DAYS;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Date date = getDay(position).getTime();
            return new SimpleDateFormat("dd.mm.yyyy").format(date);//todo get format from resources
        }
    }
}
