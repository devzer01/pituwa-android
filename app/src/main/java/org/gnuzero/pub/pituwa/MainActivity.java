package org.gnuzero.pub.pituwa;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import org.gnuzero.pub.pituwa.app.App;
import org.gnuzero.pub.pituwa.common.ActivityBase;
import org.gnuzero.pub.pituwa.dialogs.ImageChooseDialog;
import org.gnuzero.pub.pituwa.dialogs.PopularSettingsDialog;
import org.gnuzero.pub.pituwa.dialogs.ProfileReportDialog;
import org.gnuzero.pub.pituwa.util.CustomRequest;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends ActivityBase implements FragmentDrawer.FragmentDrawerListener, ImageChooseDialog.AlertPositiveListener, ProfileReportDialog.AlertPositiveListener, PopularSettingsDialog.AlertPositiveListener {

    Toolbar mToolbar;
    TextView mToolbarTitle;

    private FragmentDrawer drawerFragment;

    // used to store app title
    private CharSequence mTitle;

    LinearLayout mContainerAdmob;

    Fragment fragment;
    Boolean action = false;
    int page = 0;

    private Boolean restore = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState != null) {

            //Restore the fragment's instance
            fragment = getSupportFragmentManager().getFragment(savedInstanceState, "currentFragment");

            restore = savedInstanceState.getBoolean("restore");
            mTitle = savedInstanceState.getString("mTitle");

        } else {

            fragment = new StreamFragment();

            restore = false;
            mTitle = getString(R.string.app_name);
        }

        if (fragment != null) {

            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.container_body, fragment).commit();
        }

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbarTitle = (TextView) findViewById(R.id.toolbarTitle);
        mToolbarTitle.setTypeface(App.getInstance().getFont());

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        //getSupportActionBar().setTitle(mTitle);

        drawerFragment = (FragmentDrawer) getFragmentManager().findFragmentById(R.id.fragment_navigation_drawer);
        drawerFragment.setUp(R.id.fragment_navigation_drawer, (DrawerLayout) findViewById(R.id.drawer_layout), mToolbar);
        drawerFragment.setDrawerListener(this);

        mContainerAdmob = (LinearLayout) findViewById(R.id.container_admob);

        if (App.getInstance().getAdmob() == ADMOB_ENABLED) {

            mContainerAdmob.setVisibility(View.VISIBLE);
            AdView mAdView = (AdView) findViewById(R.id.adView);
            AdRequest adRequest = new AdRequest.Builder().build();
            mAdView.loadAd(adRequest);
        } else {
            mContainerAdmob.setVisibility(View.GONE);
        }

        if (!restore) {

            // Show default section "Stream"

            displayView(1);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {

        super.onSaveInstanceState(outState);

        outState.putBoolean("restore", true);
        //outState.putString("mTitle", getSupportActionBar().getTitle().toString());
        getSupportFragmentManager().putFragment(outState, "currentFragment", fragment);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        fragment.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == ACTION_LOGIN && resultCode == RESULT_OK && null != data) {

            String pageId = data.getStringExtra("pageId");

            switch (pageId) {

                case "favorites": {

                    displayView(4);

                    break;
                }

                case "notifications": {

                    displayView(6);

                    break;
                }

                case "profile": {

                    displayView(5);

                    break;
                }

                case "settings": {

                    Intent i = new Intent(MainActivity.this, SettingsActivity.class);
                    startActivity(i);

                    break;
                }

                default: {

                    break;
                }
            }
        }
    }

    @Override
    public void onChangePopularCategory(int position) {

        PopularFragment p = (PopularFragment) fragment;
        p.onChangeCategory(position);
    }

    @Override
    public void onImageFromGallery() {

        ProfileFragment p = (ProfileFragment) fragment;
        p.imageFromGallery();
    }

    @Override
    public void onImageFromCamera() {

        ProfileFragment p = (ProfileFragment) fragment;
        p.imageFromCamera();
    }

    @Override
    public void onProfileReport(int position) {

        ProfileFragment p = (ProfileFragment) fragment;
        p.onProfileReport(position);
    }


    @Override
    public void onDrawerItemSelected(View view, int position) {

        //RelativeLayout v = (RelativeLayout) view;
        //v.setBackgroundResource(R.drawable.ic_background);
        displayView(position);
    }

    private void displayView(int position) {

        action = false;

        switch (position) {

            case 0: {

                break;
            }

            case 1: {

                page = 1;

                fragment = new StreamFragment();
                //getSupportActionBar().setTitle(R.string.page_1);
                mToolbarTitle.setText(R.string.page_1);

                action = true;

                break;
            }

            case 2: {

                page = 2;

                fragment = new CategoriesFragment();
                //getSupportActionBar().setTitle(R.string.page_2);
                mToolbarTitle.setText(R.string.page_2);

                action = true;

                break;
            }


            case 35: {

                page = 35;

                fragment = new SearchFragment();
                getSupportActionBar().setTitle("පිටු සෙවීම");

                action = true;

                break;
            }

            case 3: {

                page = 3;

                fragment = new PopularFragment();
                //getSupportActionBar().setTitle(R.string.page_1);
                mToolbarTitle.setText(R.string.page_4);
                //getSupportActionBar().setTitle(R.string.page_4);

                action = true;

                break;
            }

            case 4: {

                if (App.getInstance().getId() != 0){

                    page = 4;

                    fragment = new FavoritesFragment();
                    //getSupportActionBar().setTitle(R.string.page_1);
                    mToolbarTitle.setText(R.string.page_5);
                    //getSupportActionBar().setTitle(R.string.page_5);

                    action = true;

                } else {

                    Intent i = new Intent(MainActivity.this, LoginActivity.class);
                    i.putExtra("pageId", "favorites");
                    startActivityForResult(i, ACTION_LOGIN);
                }

                break;
            }

            case 51: {

                if (App.getInstance().getId() != 0){

                    page = 51;

                    fragment = new NotificationsFragment();
                    //getSupportActionBar().setTitle(R.string.page_6);

                    action = true;

                } else {

                    Intent i = new Intent(MainActivity.this, LoginActivity.class);
                    i.putExtra("pageId", "notifications");
                    startActivityForResult(i, ACTION_LOGIN);
                }

                break;
            }

            case 5: {

                if (App.getInstance().getId() != 0){

                    page = 5;

                    fragment = new ProfileFragment();
                    //getSupportActionBar().setTitle(R.string.page_1);
                    mToolbarTitle.setText(R.string.page_7);
                    //getSupportActionBar().setTitle(R.string.page_7);

                    action = true;
                    /*Intent i = new Intent(MainActivity.this, LoginActivity.class);
                    i.putExtra("pageId", "profile");
                    startActivityForResult(i, ACTION_LOGIN);*/

                } else {

                    Intent i = new Intent(MainActivity.this, LoginActivity.class);
                    i.putExtra("pageId", "profile");
                    startActivityForResult(i, ACTION_LOGIN);
                }

                break;
            }

            case 6: {
                if (App.getInstance().getId() != 0){

                    if (App.getInstance().isConnected() && App.getInstance().getId() != 0) {

                        showpDialog();

                        CustomRequest jsonReq = new CustomRequest(Request.Method.POST, METHOD_ACCOUNT_LOGOUT, null,
                                new Response.Listener<JSONObject>() {
                                    @Override
                                    public void onResponse(JSONObject response) {

                                        try {

                                            if (!response.getBoolean("error")) {

                                                App.getInstance().removeData();
                                                App.getInstance().readData();

                                                App.getInstance().setNotificationsCount(0);
                                                App.getInstance().setId(0);
                                                App.getInstance().setUsername("");
                                                App.getInstance().setFullname("");

                                                Intent i = new Intent(MainActivity.this, MainActivity.class);
                                                startActivity(i);
                                            }

                                        } catch (JSONException e) {

                                            e.printStackTrace();

                                        } finally {

                                            hidepDialog();
                                        }
                                    }
                                }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                hidepDialog();
                            }
                        }) {

                            @Override
                            protected Map<String, String> getParams() {
                                Map<String, String> params = new HashMap<String, String>();
                                params.put("clientId", CLIENT_ID);
                                params.put("accountId", Long.toString(App.getInstance().getId()));
                                params.put("accessToken", App.getInstance().getAccessToken());

                                return params;
                            }
                        };

                        App.getInstance().addToRequestQueue(jsonReq);
                    }

                } else {

                }
            }
            break;

            default: {

                if (App.getInstance().getId() != 0) {

                    Intent i = new Intent(MainActivity.this, SettingsActivity.class);
                    startActivity(i);

                } else {

                    Intent i = new Intent(MainActivity.this, LoginActivity.class);
                    i.putExtra("pageId", "settings");
                    startActivityForResult(i, ACTION_LOGIN);
                }

                break;
            }
        }

        if (action) {
            //FrameLayout frm = (FrameLayout) findViewById(R.id.container_body);
            //frm.setBackgroundResource(R.drawable.ic_background);
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.container_body, fragment)
                    .commit();
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // If the nav drawer is open, hide action items related to the content view
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {

            case android.R.id.home: {

                return true;
            }

            default: {

                return super.onOptionsItemSelected(item);
            }
        }
    }

    @Override
    public void onBackPressed() {

        if (drawerFragment.isDrawerOpen()) {

            drawerFragment.closeDrawer();

        } else {

            super.onBackPressed();
        }
    }

    @Override
    public void setTitle(CharSequence title) {

        mTitle = title;
        //getSupportActionBar().setTitle(R.string.page_1);
        mToolbarTitle.setText(mTitle);
        //getSupportActionBar().setTitle(mTitle);
    }

    public void hideAds() {

        if (App.getInstance().getAdmob() == ADMOB_DISABLED) {

            mContainerAdmob.setVisibility(View.GONE);
        }
    }
}
