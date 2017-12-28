package com.bodhileaf.agriMonitor;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.DialogFragment;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.provider.OpenableColumns;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.app.LoaderManager.LoaderCallbacks;

import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


//import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveClient;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.OpenFileActivityOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static android.Manifest.permission.READ_CONTACTS;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity implements LoaderCallbacks<Cursor> , FarmListFragment.FarmSourceListener{

    /**
     * Id to identity READ_CONTACTS permission request.
     */
    private static final int REQUEST_READ_CONTACTS = 0;
    private static final String TAG = "LoginActivity";
    private static final int RC_SIGN_IN = 100;

    private static final int FILE_SELECT_CODE = 99;
    private String dbfilename;

    /**
     * Request code for the file opener activity.
     */
    private static final int REQUEST_CODE_OPENER = 1;
    /**
     * A dummy authentication store containing known user names and passwords.
     * TODO: remove after connecting to a real authentication system.
     */
    private static final String[] DUMMY_CREDENTIALS = new String[]{
            "foo@example.com:hello", "bar@example.com:world"
    };
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;

    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;
    private GoogleSignInClient mGoogleSignInClient;


    private void showFileChooser() {
        Intent  intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        try {
            startActivityForResult(
                    Intent.createChooser(intent, "Select a File to Upload"),
                    FILE_SELECT_CODE);
        } catch (android.content.ActivityNotFoundException ex) {
            // Potentially direct the user to the Market with a Dialog
            Toast.makeText(this, "Please install a File Manager.",
                    Toast.LENGTH_SHORT).show();
        }
        Log.d("main activity", "showFileChooser: exit ");

    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case RC_SIGN_IN:
                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                handleSignInResult(task);
                break;

            case FILE_SELECT_CODE:
                if (resultCode == RESULT_OK) {
                    // Get the Uri of the selected file
                    Uri  uri = data.getData();
                    Log.d("onActivity", "File Uri: " + uri.toString());
                    // Get the path
                    String path = null;
                    String filename = null;

                    path = FileUtils.getPath(LoginActivity.this, uri);
                    Log.d(TAG, "Actual File Path: " + path);
                    // Get the file instance

                    Cursor  cursor = null;

                    try {
                        cursor = getContentResolver().query(uri, null, null, null, null);
                        int column_index = cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME);
                        if (cursor.moveToFirst()) {
                            filename = cursor.getString(column_index);
                        }
                        dbfilename = path;
                        Log.d("onActivity", "File path: " + path+ "Name:" + filename);
                        SQLiteDatabase agridb = openOrCreateDatabase(path,MODE_PRIVATE ,null ) ;
                        if (!qualifyDB(agridb)) {
                            Toast.makeText(getApplicationContext(), "INCORRECT TYPE OF DATABASE. OPEN ANOTHER" , Toast.LENGTH_LONG).show();
                            // TODO: return to open db activity;
                            Log.d("database tables", "qualifyDB: FAILED");
                            return;
                        }
                        agridb.close();
                        //agridb.execSQL("insert into nodesInfo(nodeID,nodeType) values(\"104\",\"122\") ");
                        //Cursor  resultSet = agridb.rawQuery("Select nodeID from nodesInfo",null);
                        //resultSet.moveToFirst();
                        //String username = resultSet.getString(0);
                        //String password = resultSet.getString(1);
                        //Toast.makeText(getApplicationContext(), username , Toast.LENGTH_LONG).show();



                    } catch (Exception e) {
                        // Eat it
                    }
                }

                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG , "on activityresult: exit ");
        if (dbfilename != null) {
            Intent optionsScreen = new Intent(LoginActivity.this, com.bodhileaf.agriMonitor.OptionsActivity.class);
            optionsScreen.putExtra("filename",dbfilename );
            startActivity(optionsScreen);
            //Intent mapsScreen  = new Intent(LoginActivity.this, com.bodhileaf.agriMonitor.FarmMapsActivity.class);
            //mapsScreen.putExtra("filename",dbfilename );
            //startActivity(mapsScreen);
        }
    }
    // Sanity check if the opened db is not corrupted and matches the required table schemas
    private boolean qualifyDB(SQLiteDatabase selectDB) {

        MyDatabase myDB = new MyDatabase(getApplicationContext());
        SQLiteDatabase compareDB = myDB.getDatabase();
        boolean result = true;


        //SQLiteDatabase compareDB = openOrCreateDatabase("golden_new.db",MODE_PRIVATE,null);
        Cursor results = compareDB.rawQuery("SELECT name FROM sqlite_master WHERE type='table'",null);
        Cursor resultsSelect = selectDB.rawQuery("SELECT name FROM sqlite_master WHERE type='table'",null);
        results.moveToFirst();
        resultsSelect.moveToFirst();
        do {
            Log.d("database tables", "qualifyDB: "+results.getString(0));
            if (results.getString(0) != "android_metadata") {
                if (results.getString(0) == resultsSelect.getString(0)) {
                    String query = "SELECT sql FROM sqlite_master WHERE name ='" + results.getString(0) + "'";
                    Cursor schemaResults = compareDB.rawQuery(query, null);
                    Cursor schemaSelectResults = selectDB.rawQuery(query, null);
                    schemaResults.moveToFirst();
                    schemaSelectResults.moveToFirst();
                    if (schemaResults.getString(0) != schemaSelectResults.getString(0)) {
                        compareDB.close();
                        return false;
                    }
                }
            } else {
                compareDB.close();
                return false;
            }
            //Log.d("database tables", "schema query result: " + schemaResults.getString(0));

        }while (results.moveToNext());
        compareDB.close();
        return result;
    }

    // Create new DB which copies table schemas from golden db
    private void createNewDB(String path) {
        SQLiteDatabase baseDB = openOrCreateDatabase("databases/golden.db",MODE_PRIVATE,null);
        //File fileMetadata = new File();
        //fileMetadata.setName("Project plan");
        //fileMetadata.setMimeType("application/vnd.google-apps.drive-sdk");

        //File file = driveService.files().create(fileMetadata)
        //       .setFields("id")
        //      .execute();
        //System.out.println("File ID: " + file.getId());





    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        //getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        //getActionBar().hide();
        // Set up the login form.
        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);
        populateAutoComplete();

        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                //attemptLogin();
                signIn();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
        /*
        Configure sign-in to request offline access to the user's ID, basic
        profile, and Google Drive. The first time you request a code you will
        be able to exchange it for an access token and refresh token, which
        you should store. In subsequent calls, the code will only result in
        an access token. By asking for profile access (through
        DEFAULT_SIGN_IN) you will also get an ID Token as a result of the
        code exchange.
        */

        String serverClientId = getString(R.string.server_client_id);

        // Configure sign-in to request the user's ID, email address, and basic
// profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestScopes(new Scope(Scopes.DRIVE_APPFOLDER))
                .requestServerAuthCode(serverClientId)
                .requestEmail()
                .build();

        // Build a GoogleSignInClient with the options specified by gso.
         mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

    }
    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);

            // Signed in successfully, show authenticated UI.
            updateUI(account);
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
            updateUI(null);
        }
    }

    private void updateUI(@Nullable GoogleSignInAccount account) {
        if(account != null) {
            //Intent optionsScreen = new Intent(LoginActivity.this, com.bodhileaf.agriMonitor.OptionsActivity.class);
            //startActivity(optionsScreen);
            DialogFragment newFarmListFragment = new FarmListFragment();
            newFarmListFragment.show(getFragmentManager(), "FarmSelect");

        } else {

        }
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
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
            Snackbar.make(mEmailView, R.string.permission_rationale, Snackbar.LENGTH_INDEFINITE)
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


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            mAuthTask = new UserLoginTask(email, password);
            mAuthTask.execute((Void) null);
        }
    }

    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 4;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
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

        mEmailView.setAdapter(adapter);
    }


    private interface ProfileQuery {
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
        };

        int ADDRESS = 0;
        int IS_PRIMARY = 1;
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mEmail;
        private final String mPassword;

        UserLoginTask(String email, String password) {
            mEmail = email;
            mPassword = password;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.

            try {
                // Simulate network access.
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                return false;
            }

            for (String credential : DUMMY_CREDENTIALS) {
                String[] pieces = credential.split(":");
                if (pieces[0].equals(mEmail)) {
                    // Account exists, return true if the password matches.
                    return pieces[1].equals(mPassword);
                }
            }

            // TODO: register the new account here.
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);

            if (success) {
                finish();
            } else {
                mPasswordView.setError(getString(R.string.error_incorrect_password));
                mPasswordView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }

    @Override
    public void onFarmSourceSelect(int result) {
        Intent optionsScreen = new Intent(LoginActivity.this, com.bodhileaf.agriMonitor.OptionsActivity.class);
        Log.d("Main Activity", "onFarmSourceSelect: result: "+Integer.toString(result));
        switch (result) {
            case 0:



                //TODO: Open create new file dialog box
                DriveClient mDriveClient = Drive.getDriveClient(this, GoogleSignIn.getLastSignedInAccount(this));
                // Create the initial metadata - MIME type and title.
                // Note that the user will be able to change the title later.
                MetadataChangeSet metadataChangeSet =
                        new MetadataChangeSet.Builder()
                                .setMimeType("*/*")
                                .setTitle("Android .db")
                                .build();
                // Set up options to configure and display the create file activity.
                OpenFileActivityOptions openFileActivityOptions =
                        new OpenFileActivityOptions.Builder()
                                .setMimeType(Collections.singletonList("*/*"))
                                .build();

                mDriveClient.newOpenFileActivityIntentSender(openFileActivityOptions)
                        .addOnSuccessListener(new OnSuccessListener<IntentSender>() {
                            @Override
                            public void onSuccess(IntentSender intentSender) {
                                try {
                                    startIntentSenderForResult(
                                            intentSender,
                                            REQUEST_CODE_OPENER,
                            /* fillInIntent= */ null,
                            /* flagsMask= */ 0,
                            /* flagsValues= */ 0,
                            /* extraFlags= */ 0);
                                } catch (IntentSender.SendIntentException e) {
                                    Log.w(TAG, "Unable to send intent.", e);
                                }
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Unable to create OpenFileActivityIntent.", e);
                    }
                });


                //startActivity(optionsScreen);

                break;


            case 1:
                if(dbfilename == null) {
                    showFileChooser();
                }
                //startActivity(optionsScreen);

                Log.d("main Activity", "onFarmSourceSelect: file chooser done");


                break;

            case 2:
                //startActivity(optionsScreen);
                break;
            default:
                break;

        }
    }
}



