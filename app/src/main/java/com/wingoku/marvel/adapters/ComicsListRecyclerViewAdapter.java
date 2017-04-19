package com.wingoku.marvel.adapters;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.wingoku.marvel.R;
import com.wingoku.marvel.eventbus.OnComicListCardClickedEvent;
import com.wingoku.marvel.models.MarvelComic;
import com.wingoku.marvel.utils.Constants;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import timber.log.Timber;

import static android.R.attr.onClick;

/**
 * Created by Umer on 4/9/2017.
 */

public class ComicsListRecyclerViewAdapter extends RecyclerView.Adapter<ComicsListRecyclerViewAdapter.ComicsListRecyclerViewHolder>{

    private Context mContext;
    private int mLayoutFileID;
    private int mListSize;

    private List<MarvelComic> mMarvelComicList;

    Picasso mPicasso;

    /**
     * {@link ComicsListRecyclerViewAdapter} constructor
     * @param context Application/Activity Context
     * @param marvelComicList List of all marvel comics
     * @param cellLayoutFileID Resource ID of the layout that will be used by the recyclerView
     */
    public ComicsListRecyclerViewAdapter(Context context, List<MarvelComic> marvelComicList, int cellLayoutFileID, Picasso picasso){
        mContext = context;
        mMarvelComicList = marvelComicList;
        mLayoutFileID = cellLayoutFileID;
        mPicasso = picasso;
    }

    @Override
    public ComicsListRecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.
                from(parent.getContext()).
                inflate(mLayoutFileID, parent, false);

        return new ComicsListRecyclerViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ComicsListRecyclerViewHolder holder, int position) {
        String comicName = mMarvelComicList.get(position).getTitle();
        holder.mComicNameTextView.setText(comicName);
        String price = mContext.getString(R.string.string_dollar)+mMarvelComicList.get(position).getPrice();
        holder.mComicPriceTextView.setText(price);
        mPicasso.load(mMarvelComicList.get(position).getThumbnailUrl()+Constants.PORTAIT_FANTASTIC)
                .placeholder(R.drawable.placeholder)
                .into(holder.mComicThumbnailImageView);
    }

    @Override
    public int getItemCount() {
        return mListSize;
    }

    static class ComicsListRecyclerViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.imageView_comicPic)
        ImageView mComicThumbnailImageView;

        @BindView(R.id.tv_comicName)
        TextView mComicNameTextView;

        @BindView(R.id.tv_comicPrice)
        TextView mComicPriceTextView;

        @BindView(R.id.card_view)
        CardView mCardView;

        public ComicsListRecyclerViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        // in case the user taps on image, we should be able to recieve that click event
        @OnClick({R.id.card_view, R.id.imageView_comicPic})
        public void onClick(View v) {
            /**
             * The event bus will relay this event to {@link com.wingoku.marvel.fragments.ComicListFragment} that will in turn send this to MainActivity
             */
            EventBus.getDefault().post(new OnComicListCardClickedEvent(this.getAdapterPosition()));
        }
    }

    public void setComicListSize(int size) {
        mListSize = size;
    }
}