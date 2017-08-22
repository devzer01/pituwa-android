package org.gnuzero.pub.pituwa;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.gnuzero.pub.pituwa.app.App;
import org.gnuzero.pub.pituwa.constants.Constants;
import org.gnuzero.pub.pituwa.util.CustomRequest;
import org.gnuzero.pub.pituwa.util.Helper;

public class SignupFragment extends Fragment implements Constants {

    CallbackManager callbackManager;

    LoginButton loginButton;

    private ProgressDialog pDialog;

    LinearLayout mActionContainer, actionContainerLogin;

    EditText signupUsername, signupFullname, signupPassword, signupEmail;
    Button signupJoinHowBtn;
    TextView mLabelTerms, mRegularSignup, mLabelAuthorizationViaFacebook, mSigninButton;

    private String username = "", password = "", email = "", language = "", fullname = "";
    String facebookId = "", facebookName = "", facebookEmail = "", facebookPicture = "", facebookCover = "";

    private Boolean restore = false;
    private Boolean loading = false;



    public SignupFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setRetainInstance(true);

        if (AccessToken.getCurrentAccessToken()!= null) LoginManager.getInstance().logOut();

        callbackManager = CallbackManager.Factory.create();

        Intent i = getActivity().getIntent();
        facebookId = i.getStringExtra("facebookId");
        facebookName = i.getStringExtra("facebookName");
        facebookEmail = i.getStringExtra("facebookEmail");

        initpDialog();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_signup, container, false);

        if (loading) {

            showpDialog();
        }

        loginButton = (LoginButton) rootView.findViewById(R.id.login_button);
        loginButton.setTypeface(App.getInstance().getFont());
        loginButton.setReadPermissions("user_friends, email, public_profile");

        if (!FACEBOOK_AUTHORIZATION) {

            loginButton.setVisibility(View.GONE);
        }

        //mActionContainer = (LinearLayout) rootView.findViewById(R.id.actionContainer);
        //actionContainerLogin = (LinearLayout) rootView.findViewById(R.id.actionContainerLogin);
        TextView mHeader1 = (TextView) rootView.findViewById(R.id.header1);
        mHeader1.setTypeface(App.getInstance().getFont());

        TextView mHeader2 = (TextView) rootView.findViewById(R.id.header2);
        mHeader2.setTypeface(App.getInstance().getFont());

        TextView mHeader3 = (TextView) rootView.findViewById(R.id.header3);
        mHeader3.setTypeface(App.getInstance().getFont());

        TextView mHeader5 = (TextView) rootView.findViewById(R.id.header5);
        mHeader5.setTypeface(App.getInstance().getFont());


        signupUsername = (EditText) rootView.findViewById(R.id.signupUsername);
        signupUsername.setTypeface(App.getInstance().getFont());
        signupFullname = (EditText) rootView.findViewById(R.id.signupFullname);
        signupFullname.setTypeface(App.getInstance().getFont());
        signupPassword = (EditText) rootView.findViewById(R.id.signupPassword);
        signupPassword.setTypeface(App.getInstance().getFont());
        signupEmail = (EditText) rootView.findViewById(R.id.signupEmail);
        signupEmail.setTypeface(App.getInstance().getFont());

        mLabelTerms = (TextView) rootView.findViewById(R.id.SignupLabelTerms);
        mLabelTerms.setTypeface(App.getInstance().getFont());

        mLabelTerms.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                Intent i = new Intent(getActivity(), WebViewActivity.class);
                i.putExtra("url", METHOD_APP_TERMS);
                i.putExtra("title", getText(R.string.signup_label_terms_and_policies));
                startActivity(i);
            }
        });

        //mLabelAuthorizationViaFacebook = (TextView) rootView.findViewById(R.id.labelAuthorizationViaFacebook);

/*        mRegularSignup = (TextView) rootView.findViewById(R.id.regularSignup);

        mRegularSignup.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                facebookId = "";
                facebookName = "";
                facebookEmail = "";

                loginButton.setVisibility(View.VISIBLE);

                mActionContainer.setVisibility(View.GONE);
            }
        });*/

/*        mSigninButton = (TextView) rootView.findViewById(R.id.signinButton);

        mSigninButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent i = new Intent(getActivity(), LoginActivity.class);
                startActivity(i);

                getActivity().finish();
            }
        });*/

        if (facebookId != null && !facebookId.equals("")) {

            loginButton.setVisibility(View.GONE);

            //mActionContainer.setVisibility(View.VISIBLE);

        } else {

            //mActionContainer.setVisibility(View.GONE);
        }

        if (facebookId == null) {

            facebookId = "";
        }

        if (facebookEmail != null && !facebookEmail.equals("")) {

            signupEmail.setText(facebookEmail);
        }

        signupUsername.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {

                if (App.getInstance().isConnected() && checkUsername()) {

                    CustomRequest jsonReq = new CustomRequest(Request.Method.POST, METHOD_APP_CHECKUSERNAME, null,
                            new Response.Listener<JSONObject>() {
                                @Override
                                public void onResponse(JSONObject response) {

                                    try {

                                        if (response.getBoolean("error")) {

                                            signupUsername.setError(getString(R.string.error_login_taken));
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
                            params.put("username", username);

                            return params;
                        }
                    };

                    App.getInstance().addToRequestQueue(jsonReq);
                }
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });

        signupJoinHowBtn = (Button) rootView.findViewById(R.id.signupJoinHowBtn);
        signupJoinHowBtn.setTypeface(App.getInstance().getFont());
        signupJoinHowBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                username = signupUsername.getText().toString();
                fullname = signupFullname.getText().toString();
                password = signupPassword.getText().toString();
                email = signupEmail.getText().toString();
                language = Locale.getDefault().getLanguage();

                if (verifyRegForm()) {

                    if (App.getInstance().isConnected()) {

                        loading = true;

                        showpDialog();

                        CustomRequest jsonReq = new CustomRequest(Request.Method.POST, METHOD_ACCOUNT_SIGNUP, null,
                                new Response.Listener<JSONObject>() {
                                    @Override
                                    public void onResponse(JSONObject response) {

                                        ////Log.e("Profile", "Malformed JSON: \"" + response.toString() + "\"");

                                        if (App.getInstance().authorize(response)) {

                                            ////Log.e("Profile", "Malformed JSON: \"" + response.toString() + "\"");

                                            App.getInstance().updateGeoLocation();

                                            Intent intent = new Intent(getActivity(), MainActivity.class);
                                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                            startActivity(intent);

                                        } else {

                                            switch (App.getInstance().getErrorCode()) {

                                                case 300 : {

                                                    signupUsername.setError(getString(R.string.error_login_taken));
                                                    break;
                                                }

                                                case 301 : {

                                                    signupEmail.setError(getString(R.string.error_email_taken));
                                                    break;
                                                }

                                                default: {

                                                    //Log.e("Profile", "Could not parse malformed JSON: \"" + response.toString() + "\"");
                                                    break;
                                                }
                                            }
                                        }

                                        loading = false;

                                        hidepDialog();
                                    }
                                }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {

                                //Log.e("Error", "Malformed JSON: \"" + error.toString() + "\"");
                                //Log.e("Error", "Malformed JSON: \"" + error.getMessage() + "\"");

                                Toast.makeText(getActivity(), getText(R.string.error_data_loading), Toast.LENGTH_LONG).show();

                                loading = false;

                                hidepDialog();
                            }
                        }) {

                            @Override
                            protected Map<String, String> getParams() {
                                Map<String, String> params = new HashMap<String, String>();
                                params.put("username", username);
                                params.put("fullname", fullname);
                                params.put("password", password);
                                params.put("email", email);
                                params.put("language", getStrOrEmpty(language));
                                params.put("facebookId", getStrOrEmpty(facebookId));
                                params.put("clientId", CLIENT_ID);
                                params.put("profilePicture", getStrOrEmpty(facebookPicture));
                                params.put("coverPicture", getStrOrEmpty(facebookCover));
                                params.put("gcm_regId", getStrOrEmpty(App.getInstance().getGcmToken()));

                                return params;
                            }
                        };

                        App.getInstance().addToRequestQueue(jsonReq);

                    } else {

                        Toast.makeText(getActivity(), R.string.msg_network_error, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        LoginManager.getInstance().registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {

                        // App code

                        if (App.getInstance().isConnected()) {

                            loading = true;

                            showpDialog();

                            GraphRequest request = GraphRequest.newMeRequest(
                                    AccessToken.getCurrentAccessToken(),
                                    new GraphRequest.GraphJSONObjectCallback() {
                                        @Override
                                        public void onCompleted(JSONObject object, GraphResponse response) {

                                            // Application code

                                            try {

                                                //actionContainerLogin.setVisibility(View.VISIBLE);

                                                facebookId = object.getString("id");
                                                facebookName = object.getString("name");
                                                facebookPicture = object.getJSONObject("picture").getJSONObject("data").getString("url");
                                                facebookCover = object.getJSONObject("cover").getString("source");

                                                if (object.has("email")) {

                                                    facebookEmail = object.getString("email");
                                                }

                                            } catch (Throwable t) {

                                                //Log.e("Profile", "Could not parse malformed JSON: \"" + object.toString() + "\"");

                                            } finally {

                                                if (AccessToken.getCurrentAccessToken() != null) LoginManager.getInstance().logOut();

                                                //Log.d("Profile", object.toString());

                                                if (App.getInstance().isConnected()) {

                                                    if (!facebookId.equals("")) {

                                                        signinByFacebookId();

                                                    } else {

                                                        loading = false;

                                                        hidepDialog();
                                                    }

                                                } else {

                                                    loading = false;

                                                    hidepDialog();
                                                }
                                            }
                                        }
                                    });
                            Bundle parameters = new Bundle();
                            parameters.putString("fields", "id,name,link,email,cover,picture.type(large)");
                            request.setParameters(parameters);
                            request.executeAsync();
                        }
                    }

                    @Override
                    public void onCancel() {

                        // App code
                        // Cancel
                    }

                    @Override
                    public void onError(FacebookException exception) {

                        // App code
                        // Error
                    }
                });

        if (!restore) {

//            getNotifications();
        }


        // Inflate the layout for this fragment
        return rootView;
    }

    public String getStrOrEmpty(String str) {
        if (str == null) return "";
        else return str;
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
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    public void signinByFacebookId() {

        CustomRequest jsonReq = new CustomRequest(Request.Method.POST, METHOD_ACCOUNT_LOGINBYFACEBOOK, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        if (App.getInstance().authorize(response)) {

                            if (App.getInstance().getState() == ACCOUNT_STATE_ENABLED) {

                                App.getInstance().updateGeoLocation();

                                Intent intent = new Intent(getActivity(), MainActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);

                            } else {

                                if (App.getInstance().getState() == ACCOUNT_STATE_BLOCKED) {

                                    App.getInstance().logout();
                                    Toast.makeText(getActivity(), getText(R.string.msg_account_blocked), Toast.LENGTH_SHORT).show();

                                } else {

                                    App.getInstance().updateGeoLocation();

                                    Intent intent = new Intent(getActivity(), MainActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);
                                }
                            }

                        } else {

                            if (facebookId != "") {

                                loginButton.setVisibility(View.GONE);

                                //mActionContainer.setVisibility(View.VISIBLE);

                                if (facebookEmail != null && !facebookEmail.equals("")) {

                                    signupEmail.setText(facebookEmail);
                                    signupFullname.setText(facebookName);
                                    signupUsername.setText(facebookName.split(" ")[0]);
                                }
                            }
                        }

                        loading = false;

                        hidepDialog();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                Toast.makeText(getActivity(), getText(R.string.error_data_loading), Toast.LENGTH_LONG).show();

                loading = false;

                hidepDialog();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("facebookId", facebookId);
                params.put("clientId", CLIENT_ID);
                params.put("gcm_regId", App.getInstance().getGcmToken());

                return params;
            }
        };

        App.getInstance().addToRequestQueue(jsonReq);
    }

    public Boolean checkUsername() {

        username = signupUsername.getText().toString();

        Helper helper = new Helper();

        if (username.length() == 0) {

            signupUsername.setError(getString(R.string.error_field_empty));

            return false;
        }

        if (username.length() < 5) {

            signupUsername.setError(getString(R.string.error_small_username));

            return false;
        }

        if (!helper.isValidLogin(username)) {

            signupUsername.setError(getString(R.string.error_wrong_format));

            return false;
        }

        signupUsername.setError(null);

        return  true;
    }

    public Boolean checkFullname() {

        fullname = signupFullname.getText().toString();

        if (fullname.length() == 0) {

            signupFullname.setError(getString(R.string.error_field_empty));

            return false;
        }

        if (fullname.length() < 2) {

            signupFullname.setError(getString(R.string.error_small_fullname));

            return false;
        }

        signupFullname.setError(null);

        return  true;
    }

    public Boolean checkPassword() {

        password = signupPassword.getText().toString();

        Helper helper = new Helper();

        if (password.length() == 0) {

            signupPassword.setError(getString(R.string.error_field_empty));

            return false;
        }

        if (password.length() < 6) {

            signupPassword.setError(getString(R.string.error_small_password));

            return false;
        }

        if (!helper.isValidPassword(password)) {

            signupPassword.setError(getString(R.string.error_wrong_format));

            return false;
        }

        signupPassword.setError(null);

        return true;
    }

    public Boolean checkEmail() {

        email = signupEmail.getText().toString();

        Helper helper = new Helper();

        if (email.length() == 0) {

            signupEmail.setError(getString(R.string.error_field_empty));


            return false;
        }

        if (!helper.isValidEmail(email)) {

            signupEmail.setError(getString(R.string.error_wrong_format));

            return false;
        }

        signupEmail.setError(null);

        return true;
    }

    public Boolean verifyRegForm() {

        signupUsername.setError(null);
        signupFullname.setError(null);
        signupPassword.setError(null);
        signupEmail.setError(null);

        Helper helper = new Helper();

        if (username.length() == 0) {

            signupUsername.setError(getString(R.string.error_field_empty));

            return false;
        }

        if (username.length() < 5) {

            signupUsername.setError(getString(R.string.error_small_username));

            return false;
        }

        if (!helper.isValidLogin(username)) {

            signupUsername.setError(getString(R.string.error_wrong_format));

            return false;
        }

        if (fullname.length() == 0) {

            signupFullname.setError(getString(R.string.error_field_empty));

            return false;
        }

        if (fullname.length() < 2) {

            signupFullname.setError(getString(R.string.error_small_fullname));

            return false;
        }

        if (password.length() == 0) {

            signupPassword.setError(getString(R.string.error_field_empty));

            return false;
        }

        if (password.length() < 6) {

            signupPassword.setError(getString(R.string.error_small_password));

            return false;
        }

        if (!helper.isValidPassword(password)) {

            signupPassword.setError(getString(R.string.error_wrong_format));

            return false;
        }

        if (email.length() == 0) {

            signupEmail.setError(getString(R.string.error_field_empty));

            return false;
        }

        if (!helper.isValidEmail(email)) {

            signupEmail.setError(getString(R.string.error_wrong_format));

            return false;
        }

        return true;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
}