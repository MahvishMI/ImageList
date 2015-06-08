package com.oliverbud.android.imagelist;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;


import com.oliverbud.android.imagelist.Application.App;
import com.oliverbud.android.imagelist.EventBus.AddItemsEvent;
import com.oliverbud.android.imagelist.EventBus.GenericEvent;
import com.oliverbud.android.imagelist.EventBus.NavItemSelectedEvent;
import com.oliverbud.android.imagelist.EventBus.SearchEvent;

import java.lang.reflect.Array;
import java.util.ArrayList;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.Optional;
import dagger.ObjectGraph;
import de.greenrobot.event.EventBus;
import de.greenrobot.event.util.ErrorDialogManager;
import icepick.Icepick;


public class MainActivity extends AppCompatActivity{

    @InjectView(R.id.searchInput)Toolbar searchInput;
    @Optional @InjectView(R.id.drawerLayout) DrawerLayout drawerLayout;
    @Optional @InjectView(R.id.linearLayout) LinearLayout linearLayout;
    @InjectView(R.id.tabLayout) TabLayout tabLayout;
    @InjectView(R.id.coordinatorLayout)CoordinatorLayout coordinatorLayout;
    @InjectView(R.id.spinner)ProgressBar spinner;



    String currentSearch = null;
    ArrayList<String> searchStrings;

    ActionBarDrawerToggle abdt;

    @Inject
    ImageListPresenter presenter;

    private ObjectGraph activityGraph;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("itemListApp", "onCreate Activity");

        super.onCreate(savedInstanceState);
        Icepick.restoreInstanceState(this, savedInstanceState);

        if (isTablet(this)){
//            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            setContentView(R.layout.activity_main_tablet_landscape);
            ButterKnife.inject(this);
            setSupportActionBar(searchInput);
            tabLayout.setupWithViewPager(((ListsDisplayFragment)getSupportFragmentManager().findFragmentByTag("listsFragment")).listsViewPager);


        }
        else {
//            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            setContentView(R.layout.activity_main_phone_protrait);
            ButterKnife.inject(this);
            setSupportActionBar(searchInput);
            abdt = new ActionBarDrawerToggle(this, drawerLayout, 0, 0);
            drawerLayout.setDrawerListener(abdt);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
            abdt.syncState();
            tabLayout.setupWithViewPager(((ListsDisplayFragment)getSupportFragmentManager().findFragmentByTag("listsFragment")).listsViewPager);
        }

        spinner.setIndeterminate(true);
        spinner.setVisibility(View.GONE);

        searchStrings = new ArrayList();



        if (savedInstanceState != null){
            currentSearch = savedInstanceState.getString("currentSearchString");
            searchStrings = savedInstanceState.getStringArrayList("searchStrings");

            EventBus.getDefault().post(new AddItemsEvent(searchStrings));

        }

        handleIntent(getIntent());

    }

    public static boolean isTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK)
                >= Configuration.SCREENLAYOUT_SIZE_LARGE;
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



            MenuItem menuItem = this.menu.findItem(R.id.search);
            menuItem.collapseActionView();
            searchInput.setTitle(query);

            ArrayList<String> addItems = new ArrayList<String>();
            addItems.add(query);
            EventBus.getDefault().post(new AddItemsEvent(addItems));
            EventBus.getDefault().post(new SearchEvent(query));


            currentSearch = query;
            if (!searchStrings.contains(currentSearch)) {
                searchStrings.add(currentSearch);
            }
        }
    }



    @Override
    protected void onResume() {
        super.onResume();
        Log.d("itemListApp", "onResume Activity");
        if (currentSearch != null){
            getSupportActionBar().setTitle(currentSearch);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        Log.d("itemListApp", "onSaveInstanceState Activity");

        outState.putString("currentSearchString", currentSearch);
        outState.putStringArrayList("searchStrings", searchStrings);

        super.onSaveInstanceState(outState);
        Icepick.saveInstanceState(this, outState);

    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);

    }

    @Override
    protected void onStart() {
        super.onStart();

        EventBus.getDefault().register(this);
    }


    public void onEvent(NavItemSelectedEvent event) {



        String searchParam = (String)event.item.getTitle();
        if (drawerLayout != null) {
            drawerLayout.closeDrawer(GravityCompat.START);
        }
        if (!searchParam.equals(currentSearch)) {

            searchInput.setTitle(searchParam);
            currentSearch = searchParam;
            if (!searchStrings.contains(currentSearch)) {
                searchStrings.add(currentSearch);
            }
        }
        MenuItem searchItem = menu.findItem(R.id.search);
        searchItem.collapseActionView();
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
        if (id == android.R.id.home) {
            drawerLayout.openDrawer(GravityCompat.START);
        }

        return super.onOptionsItemSelected(item);
    }





}
