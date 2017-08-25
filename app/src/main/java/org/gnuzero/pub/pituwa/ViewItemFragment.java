package org.gnuzero.pub.pituwa;

import android.*;
import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.gnuzero.pub.pituwa.adapter.CommentListAdapter;
import org.gnuzero.pub.pituwa.app.App;
import org.gnuzero.pub.pituwa.constants.Constants;
import org.gnuzero.pub.pituwa.dialogs.CommentActionDialog;
import org.gnuzero.pub.pituwa.dialogs.CommentDeleteDialog;
import org.gnuzero.pub.pituwa.dialogs.MyCommentActionDialog;
import org.gnuzero.pub.pituwa.model.Comment;
import org.gnuzero.pub.pituwa.model.Item;
import org.gnuzero.pub.pituwa.util.Api;
import org.gnuzero.pub.pituwa.util.CommentInterface;
import org.gnuzero.pub.pituwa.util.CustomRequest;

/*import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerFragment;
import com.google.android.youtube.player.YouTubePlayerSupportFragment;
import com.google.android.youtube.player.YouTubePlayerView;*/


public class ViewItemFragment extends Fragment implements Constants, SwipeRefreshLayout.OnRefreshListener, CommentInterface, ActivityCompat.OnRequestPermissionsResultCallback {

    private ProgressDialog pDialog;

    SwipeRefreshLayout mContentContainer;
    RelativeLayout mErrorScreen, mLoadingScreen, mEmptyScreen;
    LinearLayout mContentScreen, mCommentFormContainer, mPostTopSeparatorLine, mItemImgSeparatorLine;

    EditText mCommentText;

    ListView listView;
    Button mRetryBtn;

    RelativeLayout mVdFrame;

    View mListViewHeader;

    ImageView mItemLike, mItemShare, mItemComment, mEmojiBtn, mSendComment;
    TextView mItemCategory, mItemDate, mItemTitle, mItemLikesCount, mItemCommentsCount, mPostMessage;
    ImageView mItemImg;
    WebView mWebView;
    VideoView mVdView;

    ImageLoader imageLoader = App.getInstance().getImageLoader();

    private ArrayList<Comment> commentsList;

    private CommentListAdapter itemAdapter;

    public Item item = null;

    long itemId = 0, replyToUserId = 0;
    int arrayLength = 0;
    String commentText;

    private Boolean loading = false;
    private Boolean restore = false;
    private Boolean preload = false;



    public ViewItemFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setRetainInstance(true);

        initpDialog();

        Intent i = getActivity().getIntent();

        itemId = i.getLongExtra("itemId", 0);

        commentsList = new ArrayList<Comment>();
        itemAdapter = new CommentListAdapter(getActivity(), commentsList, this);

        getActivity().setTitle(getString(R.string.title_activity_view_item));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_view_item, container, false);

        if (savedInstanceState != null) {

            restore = savedInstanceState.getBoolean("restore");
            loading = savedInstanceState.getBoolean("loading");
            preload = savedInstanceState.getBoolean("preload");

            replyToUserId = savedInstanceState.getLong("replyToUserId");

        } else {

            restore = false;
            loading = false;
            preload = false;

            replyToUserId = 0;
        }

        if (loading) {

            showpDialog();
        }

        mEmptyScreen = (RelativeLayout) rootView.findViewById(R.id.emptyScreen);
        mErrorScreen = (RelativeLayout) rootView.findViewById(R.id.errorScreen);
        mLoadingScreen = (RelativeLayout) rootView.findViewById(R.id.loadingScreen);
        mContentContainer = (SwipeRefreshLayout) rootView.findViewById(R.id.contentContainer);
        mContentContainer.setOnRefreshListener(this);

        mContentScreen = (LinearLayout) rootView.findViewById(R.id.contentScreen);
        mCommentFormContainer = (LinearLayout) rootView.findViewById(R.id.commentFormContainer);

        mCommentText = (EditText) rootView.findViewById(R.id.commentText);
        mSendComment = (ImageView) rootView.findViewById(R.id.sendCommentImg);
        mEmojiBtn = (ImageView) rootView.findViewById(R.id.emojiBtn);

        mSendComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                send();
            }
        });



        mRetryBtn = (Button) rootView.findViewById(R.id.retryBtn);

        mRetryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (App.getInstance().isConnected()) {

                    showLoadingScreen();

                    getItem();
                } else {
                    getOfflineItem();
                }
            }
        });

        listView = (ListView) rootView.findViewById(R.id.listView);

        mListViewHeader = getActivity().getLayoutInflater().inflate(R.layout.view_item_list_row, null);

        listView.addHeaderView(mListViewHeader);

        listView.setAdapter(itemAdapter);

        mWebView = (WebView) mListViewHeader.findViewById(R.id.webView);
        mVdView = (VideoView) mListViewHeader.findViewById(R.id.video_view);

        mItemLike = (ImageView) mListViewHeader.findViewById(R.id.itemLike);
        mItemShare = (ImageView) mListViewHeader.findViewById(R.id.itemShare);
        mItemComment = (ImageView) mListViewHeader.findViewById(R.id.itemComment);

        mPostTopSeparatorLine = (LinearLayout) mListViewHeader.findViewById(R.id.postTopSeparatorLine);
        mItemImgSeparatorLine = (LinearLayout) mListViewHeader.findViewById(R.id.itemImgSeparatorLine);
        mPostMessage = (TextView) mListViewHeader.findViewById(R.id.postMessage);

        mItemDate = (TextView) mListViewHeader.findViewById(R.id.itemDate);
        mItemTitle = (TextView) mListViewHeader.findViewById(R.id.itemTitle);
        mItemCategory = (TextView) mListViewHeader.findViewById(R.id.itemCategory);
        mItemLikesCount = (TextView) mListViewHeader.findViewById(R.id.itemLikesCount);
        mItemCommentsCount = (TextView) mListViewHeader.findViewById(R.id.itemCommentsCount);
        mItemImg = (ImageView) mListViewHeader.findViewById(R.id.itemImg);
        mVdFrame = (RelativeLayout) mListViewHeader.findViewById(R.id.video_frame);

        mVdView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent me) {

                if (mVdView.isPlaying()) {
                    mVdView.pause();
                } else {
                    mVdView.resume();
                }
                return true;
            }
        });

        if (!restore) {

            if (App.getInstance().isConnected()) {

                showLoadingScreen();
                getItem();

            } else {
                getOfflineItem();
            }

        } else {

            if (App.getInstance().isConnected()) {

                if (!preload) {

                    loadingComplete();
                    updateItem();

                } else {

                    showLoadingScreen();
                }

            } else {

                showErrorScreen();
            }
        }

        // Inflate the layout for this fragment
        return rootView;
    }

    public void getOfflineItem() {
        String id = Long.toString(itemId);

        item = App.getInstance().getItemById(id);
        updateItem();
        mContentContainer.setRefreshing(false);
    }

    public void setIconSoftKeyboard() {

        mEmojiBtn.setBackgroundResource(R.drawable.ic_keyboard);
    }

    public void onDestroyView() {

        super.onDestroyView();

        hidepDialog();
    }

    protected void initpDialog() {

        pDialog = new ProgressDialog(getActivity());
        pDialog.setMessage(getString(R.string.msg_loading));
        pDialog.setCancelable(false);
    }

    protected void showpDialog() {

        if (!pDialog.isShowing()) pDialog.show();
    }

    protected void hidepDialog() {

        if (pDialog.isShowing()) pDialog.dismiss();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

        super.onSaveInstanceState(outState);

        outState.putBoolean("restore", true);
        outState.putBoolean("loading", loading);
        outState.putBoolean("preload", preload);

        outState.putLong("replyToUserId", replyToUserId);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == ITEM_EDIT && resultCode == getActivity().RESULT_OK) {

            item.setContent(data.getStringExtra("post"));
            item.setImgUrl(data.getStringExtra("imgUrl"));

            updateItem();
        }
    }

    @Override
    public void onRefresh() {

        if (App.getInstance().isConnected()) {

            mContentContainer.setRefreshing(true);
            getItem();

        } else {
            getOfflineItem();
            //mContentContainer.setRefreshing(false);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults)
    {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Api api = new Api(getActivity());
                    api.postShare(item);
                }
        }
    }

    public void permCheck()
    {
        if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(this.getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this.getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                //handle later
            } else {
                ActivityCompat.requestPermissions(this.getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            }
        } else {
            Api api = new Api(getActivity());
            api.postShare(item);
        }
    }

    public void updateItem() {

        if (imageLoader == null) {

            imageLoader = App.getInstance().getImageLoader();
        }

        if (item == null) return;


        getActivity().setTitle(item.getTitle());

        mItemCategory.setText(item.getCategoryTitle());
        mItemDate.setText(item.getDate());

        mItemComment.setVisibility(View.GONE);
        mItemCommentsCount.setVisibility(View.GONE);

        mItemCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(getActivity(), CategoryActivity.class);
                intent.putExtra("categoryId", item.getCategoryId());
                intent.putExtra("title", item.getCategoryTitle());
                startActivity(intent);
            }
        });

        if (item.getFromUserId() == App.getInstance().getId()) {

            itemAdapter.setMyPost(true);

        } else {

            itemAdapter.setMyPost(false);
        }

        mItemLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (App.getInstance().isConnected()) {

                    if (App.getInstance().getId() != 0) {

                        CustomRequest jsonReq = new CustomRequest(Request.Method.POST, METHOD_ITEMS_LIKE, null,
                                new Response.Listener<JSONObject>() {
                                    @Override
                                    public void onResponse(JSONObject response) {

                                        try {

                                            if (!response.getBoolean("error")) {

                                                item.setLikesCount(response.getInt("likesCount"));
                                                item.setMyLike(response.getBoolean("myLike"));
                                            }

                                        } catch (JSONException e) {

                                            e.printStackTrace();

                                        } finally {

                                            if (item != null) updateItem();
                                        }
                                    }
                                }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {

                                Toast.makeText(getActivity(), getString(R.string.error_data_loading), Toast.LENGTH_LONG).show();
                            }
                        }) {

                            @Override
                            protected Map<String, String> getParams() {
                                Map<String, String> params = new HashMap<String, String>();
                                params.put("accountId", Long.toString(App.getInstance().getId()));
                                params.put("accessToken", App.getInstance().getAccessToken());
                                params.put("itemId", Long.toString(item.getId()));

                                return params;
                            }
                        };

                        App.getInstance().addToRequestQueue(jsonReq);

                    } else {
                        Toast.makeText(getActivity(), getText(R.string.msg_auth_prompt_like), Toast.LENGTH_SHORT).show();
                    }

                } else {

                    Toast.makeText(getActivity(), getText(R.string.msg_network_error), Toast.LENGTH_SHORT).show();
                }
            }
        });

        mItemShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                permCheck();
            }
        });

        if (item.isMyLike()) {

            mItemLike.setImageResource(R.drawable.perk_active);

        } else {

            mItemLike.setImageResource(R.drawable.perk);
        }

        if (item.getLikesCount() > 0) {

            mItemLikesCount.setText(Integer.toString(item.getLikesCount()));
            mItemLikesCount.setVisibility(View.VISIBLE);

        } else {

            mItemLikesCount.setText(Integer.toString(item.getLikesCount()));
            mItemLikesCount.setVisibility(View.GONE);
        }

        if (item.getTitle().length() > 0) {

            mItemTitle.setText(item.getTitle());

            mItemTitle.setVisibility(View.VISIBLE);

        } else {

            mItemTitle.setVisibility(View.GONE);
        }

        // Setup a play button to start the video
        if (item.getContent().length() != 0) {

            if (item.hasVideo()) {
                mItemImg.setVisibility(View.GONE);
                mVdFrame.setVisibility(View.VISIBLE);
                mVdView.setVisibility(View.VISIBLE);
                mVdView.setVideoURI(Uri.parse(item.getVideoUrl()));
                mVdView.start();
            } else {
                mItemImg.setVisibility(View.VISIBLE);
                mVdView.setVisibility(View.GONE);
                mVdFrame.setVisibility(View.GONE);
            }

            mWebView.setVisibility(View.VISIBLE);
            mWebView.getSettings().setJavaScriptEnabled(true);
            mWebView.getSettings().setLoadWithOverviewMode(true);
            mWebView.getSettings().setUseWideViewPort(true);

            mWebView.getSettings().setMinimumFontSize(14);

            String htmlContent = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>"
                               + "<head>"
                               + "<meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\" />"
                               + "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\"/>"
                               + "<style type=\"text/css\">body{color: #525252;} img {max-width: 100%; height: auto}</style>"
                               + "</head>"
                               + item.getContent()
                               + "";

            mWebView.loadData(htmlContent, "text/html; charset=utf-8", "UTF-8");

        }

        if (item.getImgUrl().length() > 0 && !item.hasVideo()) {

            imageLoader.get(item.getImgUrl(), ImageLoader.getImageListener(mItemImg, R.drawable.img_loading, R.drawable.img_loading));
            mItemImg.setVisibility(View.VISIBLE);
            mItemImgSeparatorLine.setVisibility(View.VISIBLE);

        } else {
            mItemImg.setVisibility(View.GONE);
            mItemImgSeparatorLine.setVisibility(View.GONE);
        }

        mPostTopSeparatorLine.setVisibility(View.GONE);
        mPostMessage.setVisibility(View.GONE);

        if (item.getAllowComments() == 0 && App.getInstance().getId() != 0) {

            mPostTopSeparatorLine.setVisibility(View.VISIBLE);
            mPostMessage.setVisibility(View.VISIBLE);

            mPostMessage.setText(getString(R.string.msg_comments_disabled));
        }

        if (App.getInstance().getId() == 0) {

            mPostTopSeparatorLine.setVisibility(View.VISIBLE);
            mPostMessage.setVisibility(View.VISIBLE);

            mPostMessage.setText(getString(R.string.msg_auth_prompt_comment));
            mPostMessage.setTypeface(App.getInstance().getFont());
        }
    }


    public void getItem() {

        CustomRequest jsonReq = new CustomRequest(Request.Method.POST, METHOD_ITEM_GET, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        try {

                            arrayLength = 0;

                            if (!response.getBoolean("error")) {

                                commentsList.clear();

                                itemId = response.getInt("itemId");

                                if (response.has("items")) {

                                    JSONArray itemsArray = response.getJSONArray("items");

                                    arrayLength = itemsArray.length();

                                    if (arrayLength > 0) {

                                        for (int i = 0; i < itemsArray.length(); i++) {

                                            JSONObject itemObj = (JSONObject) itemsArray.get(i);

                                            item = new Item(itemObj);

                                            updateItem();
                                        }
                                    }
                                }

                                if (item.getAllowComments() == COMMENTS_ENABLED) {

                                    if (response.has("comments")) {

                                        JSONObject commentsObj = response.getJSONObject("comments");

                                        if (commentsObj.has("comments")) {

                                            JSONArray commentsArray = commentsObj.getJSONArray("comments");

                                            arrayLength = commentsArray.length();

                                            if (arrayLength > 0) {

                                                for (int i = commentsArray.length() - 1; i > -1 ; i--) {

                                                    JSONObject itemObj = (JSONObject) commentsArray.get(i);

                                                    Comment comment = new Comment(itemObj);

                                                    commentsList.add(comment);
                                                }
                                            }
                                        }
                                    }
                                }

                                loadingComplete();

                            } else {

                                showErrorScreen();
                            }

                        } catch (JSONException e) {

                            showErrorScreen();

                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                showErrorScreen();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("accountId", Long.toString(App.getInstance().getId()));
                params.put("accessToken", App.getInstance().getAccessToken());
                params.put("itemId", Long.toString(itemId));
                params.put("language", "en");

                return params;
            }
        };

        App.getInstance().addToRequestQueue(jsonReq);
    }

    public void send() {

        commentText = mCommentText.getText().toString();
        commentText = commentText.trim();

        if (App.getInstance().isConnected() && App.getInstance().getId() != 0 && commentText.length() > 0) {

            loading = true;

            showpDialog();

            CustomRequest jsonReq = new CustomRequest(Request.Method.POST, METHOD_COMMENTS_NEW, null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {

                            try {

                                if (!response.getBoolean("error")) {

                                    if (response.has("comment")) {

                                        JSONObject commentObj = (JSONObject) response.getJSONObject("comment");

                                        Comment comment = new Comment(commentObj);

                                        commentsList.add(comment);

                                        itemAdapter.notifyDataSetChanged();

                                        listView.setSelection(itemAdapter.getCount() - 1);

                                        mCommentText.setText("");
                                        replyToUserId = 0;
                                    }

                                    Toast.makeText(getActivity(), getString(R.string.msg_comment_has_been_added), Toast.LENGTH_SHORT).show();

                                }

                            } catch (JSONException e) {

                                e.printStackTrace();

                            } finally {

                                loading = false;

                                hidepDialog();
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                    loading = false;

                    hidepDialog();
                }
            }) {

                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("accountId", Long.toString(App.getInstance().getId()));
                    params.put("accessToken", App.getInstance().getAccessToken());

                    params.put("itemId", Long.toString(item.getId()));
                    params.put("commentText", commentText);

                    params.put("replyToUserId", Long.toString(replyToUserId));

                    return params;
                }
            };

            int socketTimeout = 0;//0 seconds - change to what you want
            RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);

            jsonReq.setRetryPolicy(policy);

            App.getInstance().addToRequestQueue(jsonReq);
        }
    }

    public void loadingComplete() {

        itemAdapter.notifyDataSetChanged();

        if (listView.getAdapter().getCount() == 0) {

            showEmptyScreen();

        } else {

            showContentScreen();
        }

        if (mContentContainer.isRefreshing()) {

            mContentContainer.setRefreshing(false);
        }
    }

    public void showLoadingScreen() {

        preload = true;

        mContentScreen.setVisibility(View.GONE);
        mErrorScreen.setVisibility(View.GONE);
        mEmptyScreen.setVisibility(View.GONE);

        mLoadingScreen.setVisibility(View.VISIBLE);
    }

    public void showEmptyScreen() {

        mContentScreen.setVisibility(View.GONE);
        mLoadingScreen.setVisibility(View.GONE);
        mErrorScreen.setVisibility(View.GONE);

        mEmptyScreen.setVisibility(View.VISIBLE);
    }

    public void showErrorScreen() {

        mContentScreen.setVisibility(View.GONE);
        mLoadingScreen.setVisibility(View.GONE);
        mEmptyScreen.setVisibility(View.GONE);

        mErrorScreen.setVisibility(View.VISIBLE);
    }

    public void showContentScreen() {

        preload = false;

        mLoadingScreen.setVisibility(View.GONE);
        mErrorScreen.setVisibility(View.GONE);
        mEmptyScreen.setVisibility(View.GONE);

        mContentScreen.setVisibility(View.VISIBLE);

        if (item.getAllowComments() == COMMENTS_DISABLED) {

            mCommentFormContainer.setVisibility(View.GONE);
        }

        if (App.getInstance().getId() == 0) {

            mCommentFormContainer.setVisibility(View.GONE);
        }
    }

    public void commentAction(int position) {

        final Comment comment = commentsList.get(position);

        if (comment.getFromUserId() != App.getInstance().getId()) {

            /** Getting the fragment manager */
            android.app.FragmentManager fm = getActivity().getFragmentManager();

            /** Instantiating the DialogFragment class */
            CommentActionDialog alert = new CommentActionDialog();

            /** Creating a bundle object to store the selected item's index */
            Bundle b  = new Bundle();

            /** Storing the selected item's index in the bundle object */
            b.putInt("position", position);

            /** Setting the bundle object to the dialog fragment object */
            alert.setArguments(b);

            /** Creating the dialog fragment object, which will in turn open the alert dialog window */

            alert.show(fm, "alert_dialog_comment_action");

        } else {

            /** Getting the fragment manager */
            android.app.FragmentManager fm = getActivity().getFragmentManager();

            /** Instantiating the DialogFragment class */
            MyCommentActionDialog alert = new MyCommentActionDialog();

            /** Creating a bundle object to store the selected item's index */
            Bundle b  = new Bundle();

            /** Storing the selected item's index in the bundle object */
            b.putInt("position", position);

            /** Setting the bundle object to the dialog fragment object */
            alert.setArguments(b);

            /** Creating the dialog fragment object, which will in turn open the alert dialog window */

            alert.show(fm, "alert_dialog_my_comment_action");
        }
    }

    public void onCommentReply(final int position) {

        if (item.getAllowComments() == COMMENTS_ENABLED) {

            final Comment comment = commentsList.get(position);

            replyToUserId = comment.getFromUserId();

            mCommentText.setText("@" + comment.getFromUserUsername() + ", ");
            mCommentText.setSelection(mCommentText.getText().length());

            mCommentText.requestFocus();

        } else {

            Toast.makeText(getActivity(), getString(R.string.msg_comments_disabled), Toast.LENGTH_SHORT).show();
        }
    }

    public void onCommentRemove(int position) {

        /** Getting the fragment manager */
        android.app.FragmentManager fm = getActivity().getFragmentManager();

        /** Instantiating the DialogFragment class */
        CommentDeleteDialog alert = new CommentDeleteDialog();

        /** Creating a bundle object to store the selected item's index */
        Bundle b  = new Bundle();

        /** Storing the selected item's index in the bundle object */
        b.putInt("position", position);

        /** Setting the bundle object to the dialog fragment object */
        alert.setArguments(b);

        /** Creating the dialog fragment object, which will in turn open the alert dialog window */

        alert.show(fm, "alert_dialog_comment_delete");
    }

    public void onCommentDelete(final int position) {

        final Comment comment = commentsList.get(position);

        commentsList.remove(position);
        itemAdapter.notifyDataSetChanged();

        Api api = new Api(getActivity());

        api.commentDelete(comment.getId());
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    /*@Override
    public void onFullscreen(boolean b) {

    }*/
}