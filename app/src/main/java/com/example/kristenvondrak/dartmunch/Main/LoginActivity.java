package com.example.kristenvondrak.dartmunch.Main;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.LoaderManager;
import android.app.ProgressDialog;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.kristenvondrak.dartmunch.Parse.ParseAPI;
import com.example.kristenvondrak.dartmunch.R;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

import static android.Manifest.permission.READ_CONTACTS;

/**
 * A attemptLogin screen that offers attemptLogin via email/password.
 */
public class LoginActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    // Constants
    private static final String TAG = "LoginActivity";
    private static final int REQUEST_READ_CONTACTS = 0; // Id to identity READ_CONTACTS permission request.
    private static final int REQUEST_SIGNUP = 0;


    private Activity m_Activity;
    private final boolean SHOW = true;
    private final boolean HIDE = false;
    private boolean passwordView = SHOW;

    // Views
    private AutoCompleteTextView m_EmailView;
    private EditText m_PasswordView;
    private Button m_LoginButton;
    private TextView m_SignupLink;
    private TextView m_ResetPasswordLink;
    private TextView m_ShowPasswordView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        m_Activity = this;
        // Check if user already exists
        if (ParseAPI.getCurrentParseUser() != null) {
            Intent intent = new Intent(this, MainActivity.class);
            this.startActivity(intent);
        }


        initViews();
        initListeners();

        // Disable button until text is entered
        m_LoginButton.setEnabled(false);
        m_ShowPasswordView.setVisibility(View.GONE);

        // Attempt to fill in email
        populateAutoComplete();

    }


    private void initViews() {
        m_EmailView = (AutoCompleteTextView) findViewById(R.id.input_email);
        m_PasswordView = (EditText) findViewById(R.id.input_password);
        m_LoginButton = (Button) findViewById(R.id.btn_login);
        m_SignupLink = (TextView) findViewById(R.id.link_signup);
        m_ResetPasswordLink = (TextView) findViewById(R.id.link_forgot_password);
        m_ShowPasswordView = (TextView) findViewById(R.id.show_password);
    }


    private void initListeners() {

        m_PasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        m_PasswordView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0 && !TextUtils.isEmpty(m_EmailView.getText().toString())) {
                    m_LoginButton.setEnabled(true);
                } else {
                    m_LoginButton.setEnabled(false);
                }

                int visible = s.length() > 0 ? View.VISIBLE : View.GONE;
                m_ShowPasswordView.setVisibility(visible);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        m_EmailView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0 && !TextUtils.isEmpty(m_PasswordView.getText().toString())) {
                    m_LoginButton.setEnabled(true);
                } else {
                    m_LoginButton.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        m_LoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptLogin();
            }
        });

        m_SignupLink.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // Start the Signup activity
                Intent intent = new Intent(getApplicationContext(), SignupActivity.class);
                startActivityForResult(intent, REQUEST_SIGNUP);
            }
        });

        m_ShowPasswordView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                passwordView = !passwordView;
                if (passwordView == SHOW) {
                    // Dots with "show" option
                    m_PasswordView.setTransformationMethod(new PasswordTransformationMethod());
                    m_ShowPasswordView.setText(R.string.action_show_password);
                } else {
                    // Text with "hide" option
                    m_PasswordView.setTransformationMethod(null);
                    m_ShowPasswordView.setText(R.string.action_hide_password);
                }
                m_PasswordView.setSelection(m_PasswordView.getText().length());
            }
        });
    }


    private void populateAutoComplete() {
        if (!mayRequestContacts()) {
            return;
        }

        getLoaderManager().initLoader(0, null, this);
    }

    private boolean mayRequestContacts() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        if (checkSelfPermission(READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        if (shouldShowRequestPermissionRationale(READ_CONTACTS)) {
            Snackbar.make(m_EmailView, R.string.permission_rationale, Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok, new View.OnClickListener() {
                        @Override
                        @TargetApi(Build.VERSION_CODES.M)
                        public void onClick(View v) {
                            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
                        }
                    });
        } else {
            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
        }
        return false;
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_READ_CONTACTS) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                populateAutoComplete();
            }
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this,
                // Retrieve data rows for the device user's 'profile' contact.
                Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
                        ContactsContract.Contacts.Data.CONTENT_DIRECTORY), ProfileQuery.PROJECTION,

                // Select only email addresses.
                ContactsContract.Contacts.Data.MIMETYPE +
                        " = ?", new String[]{ContactsContract.CommonDataKinds.Email
                .CONTENT_ITEM_TYPE},

                // Show primary email addresses first. Note that there won't be
                // a primary email address if the user hasn't specified one.
                ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        List<String> emails = new ArrayList<>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            emails.add(cursor.getString(ProfileQuery.ADDRESS));
            cursor.moveToNext();
        }

        addEmailsToAutoComplete(emails);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }

    private void addEmailsToAutoComplete(List<String> emailAddressCollection) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(LoginActivity.this,
                        android.R.layout.simple_dropdown_item_1line, emailAddressCollection);

        m_EmailView.setAdapter(adapter);
    }


    private interface ProfileQuery {
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
        };

        int ADDRESS = 0;
        int IS_PRIMARY = 1;
    }



    public void attemptLogin() {
        Utils.hideKeyboard(m_Activity);

        if (!validate()) {
            return;
        }

        final ProgressDialog progressDialog = new ProgressDialog(LoginActivity.this,
                R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Authenticating...");
        progressDialog.show();

        String email = m_EmailView.getText().toString();
        String password = m_PasswordView.getText().toString();

        logInParseUser(email, password, progressDialog);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_SIGNUP) {
            if (resultCode == RESULT_OK) {

                // TODO: Implement successful signup logic here
                // By default we just finish the Activity and log them in automatically
                this.finish();
            }
        }
    }

    @Override
    public void onBackPressed() {
        // disable going back to the MainActivity
        moveTaskToBack(true);
    }

    public void onLoginSuccess() {
        Intent intent = new Intent(this, MainActivity.class);
        this.startActivity(intent);
        finish();
    }

    public boolean validate() {
        boolean valid = true;
        String title = null;
        String message = null;

        // Store values at the time of the attemptLogin attempt.
        String email = m_EmailView.getText().toString();
        String password = m_PasswordView.getText().toString();

        // Check for a valid email address.
        if (!Utils.isValidEmail(email)) {
            title = Constants.Validation.InvalidEmailTitle;
            message = Constants.Validation.InvalidEmailMessage;
            valid = false;
        }

        // Check for a valid password
        else if (!Utils.isValidPassword(password)) {
            title = Constants.Validation.InvalidPasswordTitle;
            message = Constants.Validation.InvalidPasswordMessage;
            valid = false;
        }

        // There was an error; don't attempt attemptLogin and show dialog
        if (!valid) {
            Utils.createAlertDialog(this, title, message).show();
        }

        return valid;
    }


    public void logInParseUser(final String email, final String password, final ProgressDialog progressDialog) {
        ParseUser.logInInBackground(email, password, new LogInCallback() {
            public void done(ParseUser user, ParseException e) {
                if (user != null) {
                    onLoginSuccess();
                } else {
                    Utils.createAlertDialog(m_Activity, Constants.Validation.SigninErrorTitle,
                            Utils.toSentence(e.getMessage())).show();
                    //m_PasswordView.setError(e.getMessage());
                    //TODO: check for name vs password error
                }
                progressDialog.dismiss();
            }
        });
    }
}
