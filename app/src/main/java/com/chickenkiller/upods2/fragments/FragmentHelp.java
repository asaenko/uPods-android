package com.chickenkiller.upods2.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.chickenkiller.upods2.R;
import com.chickenkiller.upods2.controllers.adaperts.HelpPagesAdapter;
import com.chickenkiller.upods2.interfaces.IControlStackHistory;
import com.chickenkiller.upods2.interfaces.ICustumziedBackPress;
import com.chickenkiller.upods2.interfaces.IToolbarHolder;
import com.chickenkiller.upods2.views.CircleIndicator;
import com.pixplicity.easyprefs.library.Prefs;

/**
 * Created by Alon Zilberman on 8/8/15.
 */
public class FragmentHelp extends Fragment implements ICustumziedBackPress, IControlStackHistory {

    public static final String TAG = "fragment_help";
    public static final String PREF_HELP_SHOWN = "help_shown";

    private HelpPagesAdapter helpPagesAdapter;
    private ViewPager vpHelp;
    private CircleIndicator indicatorHelp;
    private View.OnClickListener closeClickListener;

    public boolean isFromSlidingMenu;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_help, container, false);

        if (((IToolbarHolder) getActivity()).getToolbar() != null) {
            ((IToolbarHolder) getActivity()).getToolbar().setVisibility(View.GONE);
        }

        vpHelp = (ViewPager) view.findViewById(R.id.vpHelp);
        indicatorHelp = (CircleIndicator) view.findViewById(R.id.indicatorHelp);
        helpPagesAdapter = new HelpPagesAdapter(getChildFragmentManager());

        initCloseListener();
        helpPagesAdapter.setCloseClickListener(closeClickListener);
        vpHelp.setAdapter(helpPagesAdapter);
        indicatorHelp.setViewPager(vpHelp);
        Prefs.putBoolean(PREF_HELP_SHOWN, true);
        return view;
    }

    private void initCloseListener() {
        if (closeClickListener == null) {
            closeClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getFragmentManager().popBackStack();
                }
            };
        }
    }


    public void setCloseClickListener(View.OnClickListener closeClickListener) {
        this.closeClickListener = closeClickListener;
    }

    @Override
    public boolean onBackPressed() {
        closeClickListener.onClick(null);
        return false;
    }

    @Override
    public boolean shouldBeAddedToStack() {
        if (isFromSlidingMenu) {
            return true;
        }
        return !Prefs.getBoolean(FragmentHelp.PREF_HELP_SHOWN, true); //don't add to history if first time screen
    }
}
