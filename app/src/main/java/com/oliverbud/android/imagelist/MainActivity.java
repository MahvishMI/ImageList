package com.oliverbud.android.imagelist;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.EditorInfo;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;
import icepick.Icepick;


public class MainActivity extends AppCompatActivity implements ImageListView{

    @InjectView(R.id.searchInput)Toolbar searchInput;
    @InjectView(R.id.imageList) ListView imageList;

    ImageListPresenter listPresenter;
    ImageListAdapter adapter;

    String currentSearch = null;

    EndlessScrollListener scrollListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("itemListApp", "onCreate Activity");

        super.onCreate(savedInstanceState);
        Icepick.restoreInstanceState(this, savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);

        setSupportActionBar(searchInput);
        searchInput.setTitle("");

        if (listPresenter == null) {
            listPresenter = new ImageListPresenter(this);
        }

        if (savedInstanceState != null) {
            listPresenter.list = savedInstanceState.getParcelableArrayList("listData");
            listPresenter.page = savedInstanceState.getInt("page");
            currentSearch = savedInstanceState.getString("currentSearchString");
        }

        handleIntent(getIntent());
//        searchInput.setOnEditorActionListener(new EditText.OnEditorActionListener() {
//            @Override
//            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
//
//                if (actionId == EditorInfo.IME_ACTION_SEARCH){
//                    listPresenter.searchFor(v.getText().toString());
//                }
//
//                return false;
//            }
//        });



    }

    @Override
    public void onBackPressed() {

        MenuItem menuItem = this.menu.findItem(R.id.search);
        if (!menuItem.collapseActionView()){
            super.onBackPressed();

        }

    }

    @Override
    protected void onNewIntent(Intent intent) {
        Log.d("itemListApp", "onNewIntent");

        handleIntent(intent);

    }

    private void handleIntent(Intent intent) {

        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            Log.d("itemListApp", "handling intent: " + query);
            listPresenter.searchFor(query);
            MenuItem menuItem = this.menu.findItem(R.id.search);
            menuItem.collapseActionView();
            searchInput.setTitle(query);
            currentSearch = query;
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        Log.d("itemListApp", "onResume Activity");
        if (listPresenter != null) {
            listPresenter.recouple(this);
            listPresenter.onResume();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        Log.d("itemListApp", "onSaveInstanceState Activity");
        outState.putParcelableArrayList("listData", listPresenter.list);
        outState.putInt("page", listPresenter.page);
        outState.putString("currentSearchString", currentSearch);

        super.onSaveInstanceState(outState);
        Icepick.saveInstanceState(this, outState);

    }

    @Override
    protected void onStop() {
        super.onStop();
        listPresenter.decouple();
        imageList.setOnScrollListener(null);
    }

    @Override
    public void setItems(ArrayList<ImageDataItem> listData) {
        if (adapter == null || adapter.getData() != listData) {

            imageList.setAdapter(this.adapter = new ImageListAdapter(this, listData));
            imageList.setOnScrollListener(this.scrollListener = new EndlessScrollListener() {

                @Override
                public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                    super.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);

                }
                @Override
                public void onLoadMore(int page, int totalItemsCount) {
                    Log.d("itemListApp", "onLoadMore Activity");

                    listPresenter.loadMore(currentSearch);
                }

                @Override
                public void onLoadFail() {
                    Log.d("itemListApp", "onLoadFail scrollListener");

                    setLoading(false);
                }
            });
        }
        else{
            imageList.setAdapter(this.adapter);
        }
    }

    @Override
    public void addItems(ArrayList<ImageDataItem> listData) {
//        if (adapter == null || adapter.getData() != listData) {
//            imageList.setAdapter(this.adapter = new ImageListAdapter(this, listData));
//        }
//        else{
//            this.adapter.addAll(listData);
//        }
        this.adapter.notifyDataSetChanged();

    }

    @Override
    public void displayLoading() {

    }

    @Override
    public void displayError() {
        Log.d("itemListApp", "displayError Activity");
        this.scrollListener.onLoadFail();
    }


    Menu menu;
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView =
                (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(getComponentName()));

        this.menu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}