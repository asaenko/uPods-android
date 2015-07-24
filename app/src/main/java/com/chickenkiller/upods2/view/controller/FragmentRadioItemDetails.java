package com.chickenkiller.upods2.view.controller;

import android.app.Fragment;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.chickenkiller.upods2.R;
import com.chickenkiller.upods2.interfaces.IOverlayable;
import com.chickenkiller.upods2.models.RadioItem;
import com.chickenkiller.upods2.views.ControllableScrollView;

/**
 * Created by alonzilberman on 7/8/15.
 */
public class FragmentRadioItemDetails extends Fragment implements View.OnTouchListener {
    private static final int MAGIC_NUMBER = -250; //Don't know what it does
    private static final float BOTTOM_SCROLL_BORDER_PERCENT = 0.35f;
    private static final float TOP_SCROLL_BORDER_PERCENT = 1f;
    private static int bottomScrollBorder;
    private static int topScrollBorder;
    public static String TAG = "media_details";

    private RadioItem radioItem;

    private RelativeLayout rlDetailedContent;
    private ControllableScrollView svDetails;
    private TextView tvDetailedDescription;
    private TextView tvDetailedHeader;
    private ImageView imgDetailedHeader;
    private ImageView imgDetailedTopCover;
    private int moveDeltaY;
    private int screenHeight;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_media_details, container, false);
        rlDetailedContent = (RelativeLayout) view.findViewById(R.id.rlDetailedContent);
        tvDetailedDescription = (TextView) view.findViewById(R.id.tvDetailedDescription);
        tvDetailedHeader = (TextView) view.findViewById(R.id.tvDetailedHeader);
        imgDetailedHeader = (ImageView) view.findViewById(R.id.imgDetailedHeader);
        imgDetailedTopCover = (ImageView) view.findViewById(R.id.imgDetailedCover);
        svDetails = (ControllableScrollView) view.findViewById(R.id.svDetails);
        svDetails.setEnabled(false);
        moveDeltaY = 0;

        if (radioItem != null) {
            Glide.with(getActivity()).load("http://www.linuxspace.org/wp-content/uploads/2015/examples/example_4.png").centerCrop().crossFade().into(imgDetailedHeader);
            Glide.with(getActivity()).load(radioItem.getCoverImageUrl()).centerCrop().crossFade().into(imgDetailedTopCover);
            tvDetailedHeader.setText(radioItem.getName());
            tvDetailedDescription.setText(radioItem.getDescription());
        }
        rlDetailedContent.setOnTouchListener(this);
        initFragmentScrollConstants();
        return view;
    }

    private void initFragmentScrollConstants() {
        DisplayMetrics displaymetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        screenHeight = displaymetrics.heightPixels;
        bottomScrollBorder = screenHeight - (int) (screenHeight * BOTTOM_SCROLL_BORDER_PERCENT);
        topScrollBorder = screenHeight - (int) (screenHeight * TOP_SCROLL_BORDER_PERCENT);
    }

    public void setRadioItem(RadioItem radioItem) {
        this.radioItem = radioItem;
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        final int Y = (int) event.getRawY();
        LinearLayout.LayoutParams lParams = (LinearLayout.LayoutParams) rlDetailedContent.getLayoutParams();

        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                moveDeltaY = Y - lParams.topMargin;
                break;
            case MotionEvent.ACTION_UP: {
                if (lParams.topMargin >= bottomScrollBorder) {
                    getActivity().onBackPressed();
                } else if (lParams.topMargin <= topScrollBorder) {
                    lParams.topMargin = 0;
                    lParams.bottomMargin = 0;
                    view.setLayoutParams(lParams);
                    svDetails.setEnabled(true);
                } else {
                    svDetails.setEnabled(false);
                }
                break;
            }
            case MotionEvent.ACTION_POINTER_DOWN:
                break;
            case MotionEvent.ACTION_POINTER_UP:
                break;
            case MotionEvent.ACTION_MOVE:
                int newMargin = Y - moveDeltaY < 0 ? 0 : Y - moveDeltaY;
                lParams.topMargin = newMargin;
                lParams.bottomMargin = MAGIC_NUMBER;
                view.setLayoutParams(lParams);
                correctOverlayLevel(newMargin);
                break;
        }

        return true;
    }


    private void correctOverlayLevel(int margin) {
        if (getActivity() instanceof IOverlayable) {
            // 100 - percent
            // 255 - max alpha
            int alpha = margin * 100 / screenHeight;
            alpha = 255 * alpha / 100;
            alpha = 255 - alpha;
            ((IOverlayable) getActivity()).setOverlayAlpha(alpha);
            Log.i("alpha",String.valueOf(alpha));
        }
    }

}