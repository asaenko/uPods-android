package com.chickenkiller.upods2.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.chickenkiller.upods2.R;
import com.chickenkiller.upods2.controllers.internet.BackendManager;
import com.chickenkiller.upods2.views.GridSpacingItemDecoration;
import com.chickenkiller.upods2.controllers.adaperts.MediaItemsAdapter;
import com.chickenkiller.upods2.controllers.player.SmallPlayer;
import com.chickenkiller.upods2.interfaces.IContentLoadListener;
import com.chickenkiller.upods2.interfaces.IFragmentsManager;
import com.chickenkiller.upods2.interfaces.IRequestCallback;
import com.chickenkiller.upods2.interfaces.ISlidingMenuHolder;
import com.chickenkiller.upods2.interfaces.IToolbarHolder;
import com.chickenkiller.upods2.models.MediaItem;
import com.chickenkiller.upods2.models.MediaItemTitle;
import com.chickenkiller.upods2.models.RadioItem;
import com.chickenkiller.upods2.utils.ServerApi;
import com.chickenkiller.upods2.views.AutofitRecyclerView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by alonzilberman on 7/10/15.
 */
public class FragmentMainFeatured extends Fragment implements IContentLoadListener {

    public static final String TAG = "main_featured";
    public static final int MEDIA_ITEMS_CARDS_MARGIN = 25;
    public static final int MEDIA_ITEMS_TYPES_COUNT = 3;


    private AutofitRecyclerView rvMain;
    private SmallPlayer smallPlayer;
    private MediaItemsAdapter mediaItemsAdapter;
    private ProgressBar pbLoadingFeatured;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Init fragments views
        View view = inflater.inflate(R.layout.fragment_main_featured, container, false);
        pbLoadingFeatured = (ProgressBar) view.findViewById(R.id.pbLoadingFeatured);
        rvMain = (AutofitRecyclerView) view.findViewById(R.id.rvMain);
        smallPlayer = new SmallPlayer(view, getActivity());

        //Toolbar
        if (getActivity() instanceof IToolbarHolder) {
            MenuItem searchMenuItem = ((IToolbarHolder) getActivity()).getToolbar().getMenu().findItem(R.id.action_search);
            searchMenuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem menuItem) {
                    FragmentSearch fragmentSearch = new FragmentSearch();
                    fragmentSearch.setSearchType(FragmentSearch.SearchType.RADIO);
                    ((IFragmentsManager) getActivity()).showFragment(R.id.fl_content, fragmentSearch, FragmentSearch.TAG);
                    return false;
                }
            });
        }
        ((IToolbarHolder) getActivity()).getToolbar().setTitle(R.string.radio_main);
        ((ISlidingMenuHolder) getActivity()).setSlidingMenuHeader(getString(R.string.radio_main));

        //Featured adapter
        mediaItemsAdapter = new MediaItemsAdapter(getActivity(), R.layout.card_media_item_vertical,
                R.layout.media_item_title, RadioItem.withOnlyBannersHeader());
        if (getActivity() instanceof IFragmentsManager) {
            mediaItemsAdapter.setFragmentsManager((IFragmentsManager) getActivity());
        }
        mediaItemsAdapter.setContentLoadListener(this);

        //Featured recycle view
        rvMain.setHasFixedSize(true);
        rvMain.setAdapter(mediaItemsAdapter);
        rvMain.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                int viewType = mediaItemsAdapter.getItemViewType(position);
                return (viewType != MediaItemsAdapter.HEADER && viewType != MediaItemsAdapter.BANNERS_LAYOUT) ?
                        1 : rvMain.getSpanCount();
            }
        });
        rvMain.setVisibility(View.INVISIBLE);

        //Load tops from remote server
        showTops();

        return view;
    }

    private void showTops() {
        BackendManager.getInstance().loadTops(BackendManager.TopType.MAIN_FEATURED, ServerApi.RADIO_TOP, new IRequestCallback() {
                    @Override
                    public void onRequestSuccessed(final JSONObject jResponse) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    ArrayList<MediaItem> topRadioStations = new ArrayList<MediaItem>();
                                    MediaItemTitle mediaItemTitle = new MediaItemTitle(getString(R.string.top40_chanels), getString(R.string.top40_chanels_subheader));
                                    mediaItemTitle.showButton = true;
                                    topRadioStations.add(mediaItemTitle);
                                    topRadioStations.addAll(RadioItem.withJsonArray(jResponse.getJSONArray("result"), getActivity()));
                                    mediaItemsAdapter.addItems(topRadioStations);
                                    GridSpacingItemDecoration gridSpacingItemDecoration = new GridSpacingItemDecoration(rvMain.getSpanCount(), MEDIA_ITEMS_CARDS_MARGIN, true);
                                    gridSpacingItemDecoration.setGridItemType(MediaItemsAdapter.ITEM);
                                    gridSpacingItemDecoration.setItemsTypesCount(MEDIA_ITEMS_TYPES_COUNT);
                                    rvMain.addItemDecoration(gridSpacingItemDecoration);
                                    mediaItemsAdapter.notifyContentLoadingStatus();
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }

                    @Override
                    public void onRequestFailed() {

                    }

                }
        );
    }

    @Override
    public void onDestroy() {
        mediaItemsAdapter.destroy();
        smallPlayer.destroy();
        super.onDestroy();
    }

    @Override
    public void onContentLoaded() {
        pbLoadingFeatured.setVisibility(View.GONE);
        rvMain.setVisibility(View.VISIBLE);
    }

}