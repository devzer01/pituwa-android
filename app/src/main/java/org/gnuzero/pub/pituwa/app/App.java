package org.gnuzero.pub.pituwa.app;

import android.app.Application;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.annotation.IntegerRes;
import android.text.TextUtils;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;

import org.gnuzero.pub.pituwa.db.ItemsReaderDbHelper;
import org.gnuzero.pub.pituwa.db.ItemsSql;
import org.gnuzero.pub.pituwa.model.Item;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.gnuzero.pub.pituwa.R;
import org.gnuzero.pub.pituwa.constants.Constants;
import org.gnuzero.pub.pituwa.util.CustomRequest;
import org.gnuzero.pub.pituwa.util.LruBitmapCache;

public class App extends Application implements Constants {

	public static final String TAG = App.class.getSimpleName();

	private RequestQueue mRequestQueue;
	private ImageLoader mImageLoader;

	private static App mInstance;

    private Typeface font;

    private SharedPreferences sharedPref;

    private String username, fullname, accessToken, gcmToken = "", fb_id = "", photoUrl = "", coverUrl = "", area = "", country = "", city = "";
    private Double lat = 0.000000, lng = 0.000000;
    private long id;
    private int distance = 50, popular = 0, state, admob = 0, allowCommentReplyGCM, errorCode, notificationsCount = 0;

	@Override
	public void onCreate() {

		super.onCreate();
        mInstance = this;

        sharedPref = this.getSharedPreferences(getString(R.string.settings_file), Context.MODE_PRIVATE);

        this.readData();
	}

    public boolean isConnected() {
    	
    	ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
    	
    	NetworkInfo netInfo = cm.getActiveNetworkInfo();
    	
    	if (netInfo != null && netInfo.isConnectedOrConnecting()) {
    		return true;
    	}
    	
    	return false;
    }

    public void logout() {

        if (App.getInstance().isConnected() && App.getInstance().getId() != 0) {

            CustomRequest jsonReq = new CustomRequest(Request.Method.POST, METHOD_ACCOUNT_LOGOUT, null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {

                            try {

                                if (!response.getBoolean("error")) {



                                }

                            } catch (JSONException e) {

                                e.printStackTrace();

                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                    App.getInstance().removeData();
                    App.getInstance().readData();
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

        App.getInstance().removeData();
        App.getInstance().readData();
    }

    public String gPackageName() {
        return getApplicationContext().getPackageName();
    }

    public boolean setConfig(JSONObject config) {
        try {
            int admob = config.getInt("admob");
            this.admob = admob;
            return true;
        } catch (JSONException e) {
            return false;
        }
    }

    public void getConfig() {
        if (App.getInstance().isConnected()) {

            Map<String, String> params = new HashMap<String, String>();
            params.put("r", String.valueOf(Math.random()));

            CustomRequest jsonReq = new CustomRequest(Request.Method.POST, METHOD_AD_CONFIG, params,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {

                            if (!App.getInstance().setConfig(response)) {

                                Toast.makeText(getApplicationContext(), getString(R.string.msg_network_error), Toast.LENGTH_SHORT).show();
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                    Toast.makeText(getApplicationContext(), getString(R.string.msg_network_error), Toast.LENGTH_SHORT).show();
                }
            }) {

                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("clientId", CLIENT_ID);
                    return params;
                }
            };
            jsonReq.setShouldCache(false);
            App.getInstance().addToRequestQueue(jsonReq);
        }
    }

    public void reload() {

        if (App.getInstance().isConnected() && App.getInstance().getId() != 0) {

            CustomRequest jsonReq = new CustomRequest(Request.Method.POST, METHOD_ACCOUNT_AUTHORIZE, null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {

                            if (!App.getInstance().authorize(response)) {

                                Toast.makeText(getApplicationContext(), getString(R.string.msg_network_error), Toast.LENGTH_SHORT).show();
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                    Toast.makeText(getApplicationContext(), getString(R.string.msg_network_error), Toast.LENGTH_SHORT).show();
                }
            }) {

                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("clientId", CLIENT_ID);
                    params.put("accountId", Long.toString(App.getInstance().getId()));
                    params.put("accessToken", App.getInstance().getAccessToken());
                    params.put("gcm_regId", App.getInstance().getGcmToken());

                    return params;
                }
            };

            App.getInstance().addToRequestQueue(jsonReq);
        }
    }

    public void updateGeoLocation()
    {

        // empty here ;)
    }

    public Typeface getFont() {
        if (font == null) font = Typeface.createFromAsset(getAssets(), "fonts/WARNA.ttf");
        return font;
    }

    public Boolean authorize(JSONObject authObj) {

        try {

            if (authObj.has("error_code")) {

                this.setErrorCode(authObj.getInt("error_code"));
            }

            if (!authObj.has("error")) {

                return false;
            }

            if (authObj.getBoolean("error")) {

                return false;
            }

            if (!authObj.has("account")) {

                return false;
            }

            JSONArray accountArray = authObj.getJSONArray("account");

            if (accountArray.length() > 0) {

                JSONObject accountObj = (JSONObject) accountArray.get(0);

                this.setUsername(accountObj.getString("username"));
                this.setFullname(accountObj.getString("fullname"));
                this.setState(accountObj.getInt("state"));
                this.setAdmob(accountObj.getInt("admob"));
                this.setFacebookId(accountObj.getString("fb_id"));
                this.setAllowCommentReplyGCM(accountObj.getInt("allowCommentReplyGCM"));

                this.setPhotoUrl(accountObj.getString("lowPhotoUrl"));
                this.setCoverUrl(accountObj.getString("coverUrl"));

                this.setNotificationsCount(accountObj.getInt("notificationsCount"));
            }

            this.setId(authObj.getLong("accountId"));
            this.setAccessToken(authObj.getString("accessToken"));

            this.saveData();

            if (getGcmToken().length() != 0) {

                setGcmToken(getGcmToken());
            }

            return true;

        } catch (JSONException e) {

            e.printStackTrace();
            return false;
        }
    }

    public void setDistance(int distance) {

        this.distance = distance;
    }

    public int getDistance() {

        return this.distance;
    }

    public void setPopular(int category) {

        this.popular = category;
    }

    public int getPopular() {

        return this.popular;
    }

    public long getId() {

        return this.id;
    }

    public void setId(long id) {

        this.id = id;
    }

    public void setGcmToken(final String gcmToken) {

        CustomRequest jsonReq = new CustomRequest(Request.Method.POST, METHOD_ACCOUNT_SET_GCM_TOKEN, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        try {

                            if (!response.getBoolean("error")) {

                            }

                        } catch (JSONException e) {

                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("accountId", Long.toString(App.getInstance().getId()));
                params.put("accessToken", App.getInstance().getAccessToken());
                params.put("gcm_regId", gcmToken);

                return params;
            }
        };

        int socketTimeout = 0;//0 seconds - change to what you want
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);

        jsonReq.setRetryPolicy(policy);

        App.getInstance().addToRequestQueue(jsonReq);

        this.gcmToken = gcmToken;
    }

    public String getGcmToken() {

        return this.gcmToken;
    }

    public void setFacebookId(String fb_id) {

        this.fb_id = fb_id;
    }

    public String getFacebookId() {

        return this.fb_id;
    }

    public void setState(int state) {

        this.state = state;
    }

    public int getState() {

        return this.state;
    }

    public void setNotificationsCount(int notificationsCount) {

        this.notificationsCount = notificationsCount;
    }

    public int getNotificationsCount() {

        return this.notificationsCount;
    }


    public void setAllowCommentReplyGCM(int allowCommentReplyGCM) {

        this.allowCommentReplyGCM = allowCommentReplyGCM;
    }

    public int getAllowCommentReplyGCM() {

        return this.allowCommentReplyGCM;
    }

    public void setAdmob(int admob) {

        this.admob = admob;
    }

    public int getAdmob() {

        return this.admob;
    }

    public void setErrorCode(int errorCode) {

        this.errorCode = errorCode;
    }

    public int getErrorCode() {

        return this.errorCode;
    }

    public String getUsername() {

        if (this.username == null) {

            this.username = "";
        }

        return this.username;
    }

    public void setUsername(String username) {

        this.username = username;
    }

    public String getAccessToken() {

        return this.accessToken;
    }

    public void setAccessToken(String accessToken) {

        this.accessToken = accessToken;
    }

    public void setFullname(String fullname) {

        this.fullname = fullname;
    }

    public String getFullname() {

        if (this.fullname == null) {

            this.fullname = "";
        }

        return this.fullname;
    }

    public void setPhotoUrl(String photoUrl) {

        this.photoUrl = photoUrl;
    }

    public String getPhotoUrl() {

        if (this.photoUrl == null) {

            this.photoUrl = "";
        }

        return this.photoUrl;
    }

    public void setCoverUrl(String coverUrl) {

        this.coverUrl = coverUrl;
    }

    public String getCoverUrl() {

        if (coverUrl == null) {

            this.coverUrl = "";
        }

        return this.coverUrl;
    }

    public void setCountry(String country) {

        this.country = country;
    }

    public String getCountry() {

        if (this.country == null) {

            this.setCountry("");
        }

        return this.country;
    }

    public void setCity(String city) {

        this.city = city;
    }

    public String getCity() {

        if (this.city == null) {

            this.setCity("");
        }

        return this.city;
    }

    public void setArea(String area) {

        this.area = area;
    }

    public String getArea() {

        if (this.area == null) {

            this.setArea("");
        }

        return this.area;
    }

    public void setLat(Double lat) {

        if (this.lat == null) {

            this.lat = 0.000000;
        }

        this.lat = lat;
    }

    public Double getLat() {

        if (this.lat == null) {

            this.lat = 0.000000;
        }

        return this.lat;
    }

    public void setLng(Double lng) {

        if (this.lng == null) {

            this.lng = 0.000000;
        }

        this.lng = lng;
    }

    public Double getLng() {

        return this.lng;
    }

    public void readData() {

        this.setId(sharedPref.getLong(getString(R.string.settings_account_id), 0));
        this.setUsername(sharedPref.getString(getString(R.string.settings_account_username), ""));
        this.setAccessToken(sharedPref.getString(getString(R.string.settings_account_access_token), ""));
    }

    public void saveData() {

        sharedPref.edit().putLong(getString(R.string.settings_account_id), this.getId()).apply();
        sharedPref.edit().putString(getString(R.string.settings_account_username), this.getUsername()).apply();
        sharedPref.edit().putString(getString(R.string.settings_account_access_token), this.getAccessToken()).apply();
    }

    public void removeData() {

        sharedPref.edit().putLong(getString(R.string.settings_account_id), 0).apply();
        sharedPref.edit().putString(getString(R.string.settings_account_username), "").apply();
        sharedPref.edit().putString(getString(R.string.settings_account_access_token), "").apply();
    }

    public static synchronized App getInstance() {
		return mInstance;
	}

	public Item getItemById(String id) {
        ItemsReaderDbHelper mDbHelper = new ItemsReaderDbHelper(getApplicationContext());
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        String[] projection = getColumns();
        String selection = ItemsSql.ItemEntry.COLUMN_ID + " = ?";
        String[] selectionArgs = { id };

        Cursor cursor = db.query(ItemsSql.ItemEntry.TABLE_NAME, projection, selection,
                                        selectionArgs, null, null, null);

        ArrayList<Item> offlineItems = mapCursorToItemModel(cursor);

        cursor.close();
        db.close();
        return offlineItems.get(0);
    }

	public ArrayList<Item> getOfflineItems() {
        ItemsReaderDbHelper mDbHelper = new ItemsReaderDbHelper(getApplicationContext());
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        String[] projection = getColumns();
        String sortOrder = ItemsSql.ItemEntry.COLUMN_ID + " DESC";

        Cursor cursor = db.query(ItemsSql.ItemEntry.TABLE_NAME, projection, null,  null,  null, null, sortOrder);
        ArrayList<Item> items = mapCursorToItemModel(cursor);
        cursor.close();
        db.close();

        return items;
    }

    public String[] getColumns() {
        String[] projection = {
             ItemsSql.ItemEntry.COLUMN_ID,
             ItemsSql.ItemEntry.COLUMN_TITLE,
             ItemsSql.ItemEntry.COLUMN_IMAGE_URL,
             ItemsSql.ItemEntry.COLUMN_CONTENT,
             ItemsSql.ItemEntry.COLUMN_DATE,
             ItemsSql.ItemEntry.COLUMN_CATEGORY_TITLE,
             ItemsSql.ItemEntry.COLUMN_ITEM_DESCRIPTION,
             ItemsSql.ItemEntry.COLUMN_MY_LIKE,
             ItemsSql.ItemEntry.COLUMN_CREATE_AT,
             ItemsSql.ItemEntry.COLUMN_LIKES_COUNT,
             ItemsSql.ItemEntry.COLUMN_CATEGORY_ID,
             ItemsSql.ItemEntry.COLUMN_VIDEO_URL
        };
        
        return projection;
    }

    public ArrayList<Item> mapCursorToItemModel(Cursor cursor) {
        ArrayList<Item> offlineItems = new ArrayList<>();
        while(cursor.moveToNext()) {
            Boolean myLike = false;
            long itemId =      cursor.getLong(cursor.getColumnIndexOrThrow(ItemsSql.ItemEntry.COLUMN_ID));
            String itemTitle = cursor.getString(cursor.getColumnIndexOrThrow(ItemsSql.ItemEntry.COLUMN_TITLE));
            String imageUrl =  cursor.getString(cursor.getColumnIndexOrThrow(ItemsSql.ItemEntry.COLUMN_IMAGE_URL));
            String content =   cursor.getString(cursor.getColumnIndexOrThrow(ItemsSql.ItemEntry.COLUMN_CONTENT));
            String date      = cursor.getString(cursor.getColumnIndexOrThrow(ItemsSql.ItemEntry.COLUMN_DATE));
            String categoryTitle = cursor.getString(cursor.getColumnIndexOrThrow(ItemsSql.ItemEntry.COLUMN_CATEGORY_TITLE));
            String itemDescription = cursor.getString(cursor.getColumnIndexOrThrow(ItemsSql.ItemEntry.COLUMN_ITEM_DESCRIPTION));
            if(cursor.getInt(cursor.getColumnIndexOrThrow(ItemsSql.ItemEntry.COLUMN_MY_LIKE)) == 1) myLike = true;
            Integer createAt = cursor.getInt(cursor.getColumnIndexOrThrow(ItemsSql.ItemEntry.COLUMN_CREATE_AT));
            Integer likeCount = cursor.getInt(cursor.getColumnIndexOrThrow(ItemsSql.ItemEntry.COLUMN_LIKES_COUNT));
            Integer categoryId = cursor.getInt(cursor.getColumnIndexOrThrow(ItemsSql.ItemEntry.COLUMN_CATEGORY_ID));
            String videoUrl = cursor.getString(cursor.getColumnIndexOrThrow(ItemsSql.ItemEntry.COLUMN_VIDEO_URL));

            offlineItems.add(new Item(itemId, itemTitle, imageUrl, content, date, categoryTitle,
                    itemDescription, myLike, createAt, likeCount, categoryId, videoUrl));
        }
        cursor.close();
        return offlineItems;
    }

    public void storeForOfflineUse(ArrayList<Item> items) {

        new SaveItemsTask().execute(items.toArray(new Item[items.size()]));

    }

	public RequestQueue getRequestQueue() {

		if (mRequestQueue == null) {
			mRequestQueue = Volley.newRequestQueue(getApplicationContext());
		}

		return mRequestQueue;
	}

	public ImageLoader getImageLoader() {
		getRequestQueue();
		if (mImageLoader == null) {
			mImageLoader = new ImageLoader(this.mRequestQueue,
					new LruBitmapCache());
		}
		return this.mImageLoader;
	}

	public <T> void addToRequestQueue(Request<T> req, String tag) {
		// set the default tag if tag is empty
		req.setTag(TextUtils.isEmpty(tag) ? TAG : tag);
		getRequestQueue().add(req);
	}

	public <T> void addToRequestQueue(Request<T> req) {
		req.setTag(TAG);
		getRequestQueue().add(req);
	}

	public void cancelPendingRequests(Object tag) {
		if (mRequestQueue != null) {
			mRequestQueue.cancelAll(tag);
		}
	}

	public void setUpHeadlines(final TextView tv) {
        CustomRequest jsonReq = new CustomRequest(METHOD_HEADLINES, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        ArrayList<String>  sHeadlines = new ArrayList<>();
                        try {
                            JSONArray headlines = response.getJSONArray("headlines");

                            for (Integer i = 0; i < headlines.length(); i++) {
                                sHeadlines.add(headlines.getString(i));
                            }

                            String s1 = new String(sHeadlines.get(0).getBytes("ISO-8859-1"));
                            tv.setText(s1);

                        } catch (JSONException e) {
                            //e.printStackTrace();
                        } catch (UnsupportedEncodingException e) {
                        } finally {

                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        });

        int socketTimeout = 0;//0 seconds - change to what you want
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);

        jsonReq.setRetryPolicy(policy);

        App.getInstance().addToRequestQueue(jsonReq);

        this.gcmToken = gcmToken;

        ///////////
    }

    private class SaveItemsTask extends AsyncTask<Item, Integer, Integer> {

        protected Integer doInBackground(Item... items) {
            ItemsReaderDbHelper mDbHelper = new ItemsReaderDbHelper(App.mInstance.getApplicationContext());
            SQLiteDatabase db = mDbHelper.getWritableDatabase();

            for (Integer i = 0; i < items.length; i++) {

                String[] projection = {ItemsSql.ItemEntry.COLUMN_ID};
                String selection = ItemsSql.ItemEntry.COLUMN_ID + " = ?";
                String[] selectionArgs = { Long.toString(items[i].getId()) };
                Cursor cursor = db.query(ItemsSql.ItemEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, null);

                if (cursor.getCount() == 0) {
                    ContentValues values = new ContentValues();

                    Integer myLike = 0;
                    if (items[i].isMyLike()) myLike = 1;

                    values.put(ItemsSql.ItemEntry.COLUMN_ID, items[i].getId());
                    values.put(ItemsSql.ItemEntry.COLUMN_TITLE, items[i].getTitle());
                    values.put(ItemsSql.ItemEntry.COLUMN_IMAGE_URL, items[i].getImgUrl());
                    values.put(ItemsSql.ItemEntry.COLUMN_CONTENT, items[i].getContent());

                    values.put(ItemsSql.ItemEntry.COLUMN_DATE, items[i].getDate());
                    values.put(ItemsSql.ItemEntry.COLUMN_CATEGORY_TITLE, items[i].getCategoryTitle());
                    values.put(ItemsSql.ItemEntry.COLUMN_ITEM_DESCRIPTION, items[i].getDescription());
                    values.put(ItemsSql.ItemEntry.COLUMN_MY_LIKE, myLike);
                    values.put(ItemsSql.ItemEntry.COLUMN_CREATE_AT, items[i].getCreateAt());
                    values.put(ItemsSql.ItemEntry.COLUMN_LIKES_COUNT, items[i].getLikesCount());
                    values.put(ItemsSql.ItemEntry.COLUMN_CATEGORY_ID, items[i].getCategoryId());
                    values.put(ItemsSql.ItemEntry.COLUMN_VIDEO_URL, items[i].getVideoUrl());
                    Long id = db.insert(ItemsSql.ItemEntry.TABLE_NAME, null, values);
                }
                cursor.close();
            }
            db.close();
            return 0;
        }

        protected void onProgressUpdate(Integer... progress) {
        }

        protected void onPostExecute(Long result) {

        }
    }
}