package com.example.kristenvondrak.dartmunch.Main;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.LoaderManager.LoaderCallbacks;
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

import com.example.kristenvondrak.dartmunch.Parse.User;
import com.example.kristenvondrak.dartmunch.R;
import com.parse.ParseException;
import com.parse.SignUpCallback;

import java.util.ArrayList;
import java.util.List;

import static android.Manifest.permission.READ_CONTACTS;

/**
 * A attemptLogin screen that offers attemptLogin via email/password.
 */
public class SignupActivity extends AppCompatActivity implements LoaderCallbacks<Cursor> {

    // Constants
    private static final String TAG = "SignupActivity";
    private static final int REQUEST_READ_CONTACTS = 0; // Id to identity READ_CONTACTS permission request.
    private static final int REQUEST_SIGNUP = 0;


    private Activity m_Activity;
    private final boolean SHOW = true;
    private final boolean HIDE = false;
    private boolean passwordView = SHOW;
    private boolean confirmView = SHOW;

    // Views
    private AutoCompleteTextView m_EmailView;
    private EditText m_PasswordView;
    private EditText m_ConfirmPasswordView;
    private Button m_SignupButton;
    private TextView m_LoginLink;
    private TextView m_ShowPasswordView;
    private TextView m_ShowConfirmPasswordView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        m_Activity = this;
        // Check if user already exists
   /*     if (ParseAPI.getCurrentParseUser() != null) {
            Intent intent = new Intent(this, MainActivity.class);
            this.startActivity(intent);
        }
    */

        initViews();
        initListeners();

        // Disable button until text is entered
        m_SignupButton.setEnabled(false);
        m_ShowPasswordView.setVisibility(View.GONE);
        m_ShowConfirmPasswordView.setVisibility(View.GONE);

        // Attempt to fill in email
        populateAutoComplete();

    }


    private void initViews() {
        m_EmailView = (AutoCompleteTextView) findViewById(R.id.input_email);
        m_PasswordView = (EditText) findViewById(R.id.input_password);
        m_ConfirmPasswordView = (EditText) findViewById(R.id.input_confirm_password);

        m_SignupButton = (Button) findViewById(R.id.btn_signup);
        m_LoginLink = (TextView) findViewById(R.id.link_login);
        m_ShowPasswordView = (TextView) findViewById(R.id.show_password);
        m_ShowConfirmPasswordView = (TextView) findViewById(R.id.show_confirm_password);
    }


    private void initListeners() {

        m_ConfirmPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.signup || id == EditorInfo.IME_NULL) {
                    attemptSignup();
                    return true;
                }
                return false;
            }
        });

        m_SignupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptSignup();
            }
        });

        m_LoginLink.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // Start the Signup activity
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
            }
        });

        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String email = m_EmailView.getText().toString();
                String password = m_PasswordView.getText().toString();
                String confirm = m_ConfirmPasswordView.getText().toString();

                if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password) && !TextUtils.isEmpty(confirm)) {
                    m_SignupButton.setEnabled(true);
                } else {
                    m_SignupButton.setEnabled(false);
                }

                int visible = password.length() > 0 ? View.VISIBLE : View.GONE;
                m_ShowPasswordView.setVisibility(visible);

                visible = confirm.length() > 0 ? View.VISIBLE : View.GONE;
                m_ShowConfirmPasswordView.setVisibility(visible);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        };

        m_ConfirmPasswordView.addTextChangedListener(textWatcher);
        m_PasswordView.addTextChangedListener(textWatcher);
        m_EmailView.addTextChangedListener(textWatcher);


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

        m_ShowConfirmPasswordView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmView = !confirmView;
                if (confirmView == SHOW) {
                    // Dots with "show" option
                    m_ConfirmPasswordView.setTransformationMethod(new PasswordTransformationMethod());
                    m_ShowConfirmPasswordView.setText(R.string.action_show_password);
                } else {
                    // Text with "hide" option
                    m_ConfirmPasswordView.setTransformationMethod(null);
                    m_ShowConfirmPasswordView.setText(R.string.action_hide_password);
                }
                m_ConfirmPasswordView.setSelection(m_ConfirmPasswordView.getText().length());
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
                new ArrayAdapter<>(m_Activity,
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



    public void attemptSignup() {
        Utils.hideKeyboard(m_Activity);

        if (!validate()) {
            return;
        }

        final ProgressDialog progressDialog = new ProgressDialog(m_Activity,
                R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Authenticating...");
        progressDialog.show();

        String email = m_EmailView.getText().toString();
        String password = m_PasswordView.getText().toString();

        createNewParseUser(email, password, progressDialog);
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

    public void onSignupSuccess() {
        Intent intent = new Intent(this, MainActivity.class);
        this.startActivity(intent);
        finish();
    }

    public boolean validate() {
        boolean valid = true;
        String title = null;
        String message = null;

        // Store values at the time of the signup attempt.
        String email = m_EmailView.getText().toString();
        String password = m_PasswordView.getText().toString();
        String confirm = m_ConfirmPasswordView.getText().toString();

        // Check for a valid email address.
        if (!Utils.isValidEmail(email)) {
            title = Constants.Validation.InvalidEmailTitle;
            message = Constants.Validation.InvalidEmailMessage;
            valid = false;

        // Check for a valid password
        } else if (!Utils.isValidPassword(password)) {
            title = Constants.Validation.InvalidPasswordTitle;
            message = Constants.Validation.InvalidPasswordMessage;
            valid = false;

        // Check that passwords match
        } else if (!password.equals(confirm)) {
            title = Constants.Validation.NoMatchPasswordsTitle;
            message = Constants.Validation.NoMatchPasswordsMessage;
            valid = false;
        }


        // There was an error; don't attempt Signup and show dialog
        if (!valid) {
            Utils.createAlertDialog(this, title, message).show();
        }

        return valid;
    }


    public void createNewParseUser(String email, String password, final ProgressDialog progressDialog) {
        User user = new User();
        user.setUsername(email);
        user.setPassword(password);
        user.setEmail(email);

        // Invoke signUpInBackground
        user.signUpInBackground(new SignUpCallback() {
            public void done(ParseException e) {
                if (e == null) {
                    onSignupSuccess();
                } else {
                    Utils.createAlertDialog(m_Activity, Constants.Validation.SignupErrorTitle,
                            Utils.toSentence(e.getMessage())).show();
                   //TODO: case ladder with specific title and message
                }
                progressDialog.dismiss();
            }
        });
    }
}


