package com.stiggpwnz.vibes.fragments;

import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.stiggpwnz.vibes.R;
import com.stiggpwnz.vibes.fragments.base.BaseFragment;

import butterknife.InjectView;

/**
 * Created by adelnizamutdinov on 03/03/2014
 */
public class MainFragment extends BaseFragment {
    @InjectView(R.id.drawer_layout) DrawerLayout drawerLayout;

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (getActivity() != null && getActivity().getActionBar() != null) {
            getActivity().getActionBar().setHomeButtonEnabled(true);
            getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
            getActivity().getActionBar().setTitle(R.string.app_name);
        }
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (getChildFragmentManager().findFragmentById(R.id.left_drawer) == null) {
            getChildFragmentManager().beginTransaction()
                    .replace(R.id.left_drawer, new NavigationFragment())
                    .replace(R.id.content_frame, FeedFragmentBuilder.newFeedFragment(0))
                    .commit();
        }
    }
}