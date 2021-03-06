package com.chickenkiller.upods2.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.chickenkiller.upods2.R;
import com.chickenkiller.upods2.controllers.adaperts.MediaItemsAdapter;
import com.chickenkiller.upods2.controllers.app.ProfileManager;
import com.chickenkiller.upods2.interfaces.IFragmentsManager;
import com.chickenkiller.upods2.interfaces.IMediaItemView;
import com.chickenkiller.upods2.models.MediaItem;
import com.chickenkiller.upods2.models.MediaItemTitle;
import com.chickenkiller.upods2.models.MediaListItem;
import com.chickenkiller.upods2.models.Podcast;
import com.chickenkiller.upods2.models.RadioItem;
import com.chickenkiller.upods2.utils.enums.MediaItemType;

import java.util.ArrayList;
import java.util.Calendar;


/**
 * Created by Alon Zilberman on 8/8/15.
 */
public class FragmentMediaItemsList extends Fragment {

    public static final String TAG;
    private RecyclerView rvMediaItems;
    private LinearLayout lnEmptyScreen;
    private MediaItemsAdapter mediaItemsAdapter;
    private MediaItemType mediaItemType;
    private View rootView;

    static {
        long time = Calendar.getInstance().get(Calendar.MILLISECOND);
        TAG = "f_favorites_downloaded_" + String.valueOf(time);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_horizontal_media_items, container, false);
        rvMediaItems = (RecyclerView) rootView.findViewById(R.id.rvMediaItems);
        lnEmptyScreen = (LinearLayout) rootView.findViewById(R.id.lnEmptyScreen);

        setEmptyScreenText();

        new Thread(new Runnable() {
            @Override
            public void run() {
                final ArrayList<IMediaItemView> allItems = new ArrayList<>();
                MediaItemTitle mediaItemTitle = null;

                if (mediaItemType == MediaItemType.PODCAST_DOWNLOADED || mediaItemType == null) {
                    mediaItemTitle = new MediaItemTitle(getString(R.string.recently_downloaded));
                    mediaItemTitle.showButton = false;
                    allItems.add(mediaItemTitle);
                    ArrayList<Podcast> downloadedPodcasts = ProfileManager.getInstance().getDownloadedPodcasts();
                    allItems.addAll(downloadedPodcasts);
                } else if (mediaItemType == MediaItemType.PODCAST_FAVORITE) {
                    mediaItemTitle = new MediaItemTitle(getString(R.string.recently_subscribed));
                    mediaItemTitle.showButton = false;
                    allItems.add(mediaItemTitle);
                    ArrayList<Podcast> favoritePodcasts = ProfileManager.getInstance().getSubscribedPodcasts();
                    allItems.addAll(favoritePodcasts);
                } else if (mediaItemType == MediaItemType.RADIO_SUBSCRIBED) {
                    mediaItemTitle = new MediaItemTitle(getString(R.string.recently_subscribed));
                    mediaItemTitle.showButton = false;
                    allItems.add(mediaItemTitle);
                    ArrayList<RadioItem> subscribedRadioItems = ProfileManager.getInstance().getSubscribedRadioItems();
                    allItems.addAll(subscribedRadioItems);
                } else if (mediaItemType == MediaItemType.RADIO_RECENT) {
                    mediaItemTitle = new MediaItemTitle(getString(R.string.recently_played));
                    mediaItemTitle.showButton = false;
                    allItems.add(mediaItemTitle);
                    ArrayList<RadioItem> recentRadioItems = ProfileManager.getInstance().getRecentRadioItems();
                    allItems.addAll(recentRadioItems);
                }

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        FragmentMediaItemsList.this.mediaItemsAdapter = new MediaItemsAdapter(getActivity(), R.layout.card_media_item_horizontal, R.layout.media_item_title, allItems);
                        FragmentMediaItemsList.this.mediaItemsAdapter.setFragmentsManager((IFragmentsManager) getActivity());
                        FragmentMediaItemsList.this.mediaItemsAdapter.setMediaItemType(mediaItemType);

                        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
                        layoutManager.setOrientation(OrientationHelper.VERTICAL);
                        rvMediaItems.setLayoutManager(layoutManager);
                        rvMediaItems.setAdapter(FragmentMediaItemsList.this.mediaItemsAdapter);

                        notifyPlaceHolder();
                    }
                });
            }
        }).run();

        return rootView;
    }

    public void setMediaItemType(MediaItemType mediaItemType) {
        this.mediaItemType = mediaItemType;
    }

    public void notifyMediaItemChanges(ProfileManager.ProfileUpdateEvent profileUpdateEvent) {
        if (mediaItemsAdapter != null) {
            MediaItem mediaItem = profileUpdateEvent.mediaItem;

            if (mediaItemType == MediaItemType.PODCAST_DOWNLOADED && mediaItem instanceof Podcast
                    && profileUpdateEvent.updateListType.equals(MediaListItem.DOWNLOADED)) {
                if (profileUpdateEvent.isRemoved) {
                    mediaItemsAdapter.removeMediaItem(mediaItem);
                } else {
                    mediaItemsAdapter.addItem(mediaItem);
                }
                mediaItemsAdapter.notifyDataSetChanged();
                notifyPlaceHolder();
            } else if (mediaItemType == MediaItemType.PODCAST_FAVORITE && mediaItem instanceof Podcast
                    && profileUpdateEvent.updateListType.equals(MediaListItem.SUBSCRIBED)) {
                if (profileUpdateEvent.isRemoved) {
                    mediaItemsAdapter.removeMediaItem(mediaItem);
                } else {
                    mediaItemsAdapter.addItem(mediaItem);
                }
                mediaItemsAdapter.notifyDataSetChanged();
                notifyPlaceHolder();
            } else if (mediaItemType == MediaItemType.PODCAST_FAVORITE && mediaItem instanceof Podcast
                    && profileUpdateEvent.updateListType.equals(MediaListItem.NEW)) {
                mediaItemsAdapter.notifyDataSetChanged();
            } else if (mediaItemType == MediaItemType.RADIO_SUBSCRIBED && mediaItem instanceof RadioItem
                    && profileUpdateEvent.updateListType.equals(MediaListItem.SUBSCRIBED)) {
                if (profileUpdateEvent.isRemoved) {
                    mediaItemsAdapter.removeMediaItem(mediaItem);
                } else {
                    mediaItemsAdapter.addItem(mediaItem);
                }
                mediaItemsAdapter.notifyDataSetChanged();
                notifyPlaceHolder();
            } else if (mediaItemType == MediaItemType.RADIO_RECENT && mediaItem instanceof RadioItem
                    && profileUpdateEvent.updateListType.equals(MediaListItem.RECENT)) {
                if (profileUpdateEvent.isRemoved) {
                    mediaItemsAdapter.removeMediaItem(mediaItem);
                } else {
                    mediaItemsAdapter.addItem(mediaItem);
                }
                mediaItemsAdapter.notifyDataSetChanged();
                notifyPlaceHolder();
            } else {
                //Check if current list contains updatedMediaItem -> if yes sync it
                mediaItemsAdapter.updateMediaItem(mediaItem);
            }
        }
    }

    public void reloadAllData() {
        ArrayList<IMediaItemView> allItems = new ArrayList<>();
        allItems.add(mediaItemsAdapter.getItemAt(0));
        if (mediaItemType == MediaItemType.PODCAST_DOWNLOADED || mediaItemType == null) {
            ArrayList<Podcast> downloadedPodcasts = ProfileManager.getInstance().getDownloadedPodcasts();
            allItems.addAll(downloadedPodcasts);
        } else if (mediaItemType == MediaItemType.PODCAST_FAVORITE) {
            ArrayList<Podcast> favoritePodcasts = ProfileManager.getInstance().getSubscribedPodcasts();
            allItems.addAll(favoritePodcasts);
        } else if (mediaItemType == MediaItemType.RADIO_SUBSCRIBED) {
            ArrayList<RadioItem> subscribedRadioItems = ProfileManager.getInstance().getSubscribedRadioItems();
            allItems.addAll(subscribedRadioItems);
        } else if (mediaItemType == MediaItemType.RADIO_RECENT) {
            ArrayList<RadioItem> recentRadioItems = ProfileManager.getInstance().getRecentRadioItems();
            allItems.addAll(recentRadioItems);
        }
        mediaItemsAdapter.clearItems();
        mediaItemsAdapter.addItems(allItems);
        mediaItemsAdapter.notifyDataSetChanged();
        notifyPlaceHolder();
    }

    private void notifyPlaceHolder() {
        if (mediaItemsAdapter.getItemCount() < 2) {
            rvMediaItems.setVisibility(View.GONE);
            lnEmptyScreen.setVisibility(View.VISIBLE);
        } else {
            rvMediaItems.setVisibility(View.VISIBLE);
            lnEmptyScreen.setVisibility(View.GONE);
        }
    }

    private void setEmptyScreenText() {
        TextView tvEmptyScreenText = (TextView) rootView.findViewById(R.id.tvEmptyScreenText);
        ImageView imgHelpTip = (ImageView) rootView.findViewById(R.id.imgHelpTip);

        if (mediaItemType == MediaItemType.PODCAST_DOWNLOADED || mediaItemType == null) {
            tvEmptyScreenText.setText(R.string.empty_dowloaded_podcasts);
            imgHelpTip.setImageResource(R.drawable.downloaded_empty_sreen);
        } else if (mediaItemType == MediaItemType.PODCAST_FAVORITE) {
            tvEmptyScreenText.setText(R.string.empty_favorite_podcasts);
            imgHelpTip.setImageResource(R.drawable.favorites_empty_sreen);
        } else if (mediaItemType == MediaItemType.RADIO_SUBSCRIBED) {
            imgHelpTip.setImageResource(R.drawable.subscribed_empty_sreen);
            tvEmptyScreenText.setText(R.string.empty_radio_subscribed);
        } else if (mediaItemType == MediaItemType.RADIO_RECENT) {
            imgHelpTip.setImageResource(R.drawable.recently_empty_sreen);
            tvEmptyScreenText.setText(R.string.empty_radio_recent);
        }
    }
}
