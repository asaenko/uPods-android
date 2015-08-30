package com.chickenkiller.upods2.controllers;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.chickenkiller.upods2.R;
import com.chickenkiller.upods2.interfaces.IFragmentsManager;
import com.chickenkiller.upods2.interfaces.IPlayableMediaItem;
import com.chickenkiller.upods2.interfaces.IToolbarHolder;
import com.chickenkiller.upods2.models.MediaItem;
import com.chickenkiller.upods2.models.Podcast;
import com.chickenkiller.upods2.models.RadioItem;
import com.chickenkiller.upods2.view.controller.FragmentRadioItemDetails;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by alonzilberman on 7/2/15.
 */
public class SearchResultsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {


    private int itemLayout;

    private List<MediaItem> items;
    private Context mContext;
    private IFragmentsManager fragmentsManager;


    private class ViewHolderRadioResult extends RecyclerView.ViewHolder implements View.OnClickListener {
        public ImageView imgCover;
        public TextView tvTitle;
        public TextView tvCountry;

        public ViewHolderRadioResult(View view) {
            super(view);
            this.imgCover = (ImageView) view.findViewById(R.id.imgSearchRadioCover);
            this.tvTitle = (TextView) view.findViewById(R.id.tvSearchRadioTitle);
            this.tvCountry = (TextView) view.findViewById(R.id.tvSearchCountry);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            FragmentRadioItemDetails fragmentRadioItemDetails = new FragmentRadioItemDetails();
            if (items.get(getAdapterPosition()) instanceof IPlayableMediaItem) {
                fragmentRadioItemDetails.setPlayableItem((IPlayableMediaItem) items.get(getAdapterPosition()));
            }
            if (!fragmentsManager.hasFragment(FragmentRadioItemDetails.TAG)) {
                SearchView searchView = (SearchView) ((IToolbarHolder) mContext).getToolbar().getMenu().findItem(R.id.action_search).getActionView();
                searchView.clearFocus();
                fragmentsManager.showFragment(R.id.fl_window, fragmentRadioItemDetails, FragmentRadioItemDetails.TAG,
                        IFragmentsManager.FragmentOpenType.OVERLAY, IFragmentsManager.FragmentAnimationType.BOTTOM_TOP);
            }
        }

    }


    public SearchResultsAdapter(Context mContext, int itemLayout) {
        super();
        this.items = new ArrayList<>();
        this.itemLayout = itemLayout;
        this.mContext = mContext;
    }


    public void setFragmentsManager(IFragmentsManager fragmentsManager) {
        this.fragmentsManager = fragmentsManager;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(itemLayout, parent, false);
        RecyclerView.ViewHolder viewHolder = new ViewHolderRadioResult(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ViewHolderRadioResult) {
            MediaItem currentItem = items.get(position);
            ;
            if (currentItem instanceof RadioItem) {
                Glide.with(mContext).load(((RadioItem) currentItem).getCoverImageUrl()).centerCrop()
                        .crossFade().into(((ViewHolderRadioResult) holder).imgCover);
                ((ViewHolderRadioResult) holder).tvTitle.setText(((RadioItem) currentItem).getName());
                ((ViewHolderRadioResult) holder).tvCountry.setText(((RadioItem) currentItem).getCountry());
            } else {
                Glide.with(mContext).load(((Podcast) currentItem).getCoverImageUrl()).centerCrop()
                        .crossFade().into(((ViewHolderRadioResult) holder).imgCover);
                ((ViewHolderRadioResult) holder).tvTitle.setText(((Podcast) currentItem).getName());
                ((ViewHolderRadioResult) holder).tvCountry.setText(((Podcast) currentItem).getGenre());
            }
            holder.itemView.setTag(currentItem);
        }
    }


    @Override
    public int getItemCount() {
        return items.size();
    }

    public void addItems(ArrayList<MediaItem> items) {
        this.items.addAll(items);
        this.notifyDataSetChanged();
    }

    public void clear() {
        items.clear();
    }

}