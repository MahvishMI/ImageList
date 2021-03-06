package com.oliverbud.android.imagelist.UI;

import com.oliverbud.android.imagelist.UI.Util.ImageDataItem;

import java.util.ArrayList;

/**
 * Created by oliverbud on 5/26/15.
 */
public interface ImageListView {

    public void displayLoading();

    public void setItems(ArrayList<ImageDataItem> listData);

    public void updateItems(int position);


    public void displayError();

}
