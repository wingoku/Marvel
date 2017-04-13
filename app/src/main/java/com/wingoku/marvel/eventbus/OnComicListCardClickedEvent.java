package com.wingoku.marvel.eventbus;

/**
 * Created by Umer on 4/13/2017.
 */

public class OnComicListCardClickedEvent {
    private int mClickedItemPosition;

    public OnComicListCardClickedEvent(int itemPosition) {
        mClickedItemPosition = itemPosition;
    }

    public int getClickedItemPosition() {
        return mClickedItemPosition;
    }
}
