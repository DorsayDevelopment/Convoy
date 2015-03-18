package com.dorsaydevelopment.convoy;

import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseUser;
import com.parse.SaveCallback;


public class SettingsActivity extends ActionBarActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }

    public static class SettingsFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_general);

            final ParseUser currentUser = ParseUser.getCurrentUser();
            final String name = currentUser.getString("firstName") + " " + currentUser.getString("lastName");

            Preference logoutBtn = (Preference) findPreference("logout_pref_btn");
            logoutBtn.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {

                    Log.i("Logout", "Logging out of application");
                    ParseUser.logOut();
                    Intent intent = new Intent(getActivity().getApplicationContext(), DispatchActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    getActivity().finish();
                    return true;
                }
            });

            final Preference linkFacebookBtn = (Preference) findPreference("link_facebook_pref_btn");
            // Check if user has already linked facebook
            if(!ParseFacebookUtils.isLinked(currentUser)) {
                linkFacebookBtn.setTitle("Link Facebook account");
                linkFacebookBtn.setSummary("Connect a Facebook account to " + getResources().getString(R.string.app_name));
            } else {
                linkFacebookBtn.setTitle("Unlink Facebook account");
                linkFacebookBtn.setSummary("Currently logged in as " + name);
            }

            linkFacebookBtn.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    // Check if user has already linked facebook
                    if (!ParseFacebookUtils.isLinked(currentUser)) {
                        ParseFacebookUtils.link(currentUser, getActivity(), new SaveCallback() {
                            @Override
                            public void done(ParseException ex) {
                                if (ParseFacebookUtils.isLinked(currentUser)) {
                                    Log.d("MyApp", "Woohoo, user logged in with Facebook!");
                                    linkFacebookBtn.setTitle("Unlink Facebook account");
                                    linkFacebookBtn.setSummary("Currently logged in as " + name);
                                    Toast.makeText(getActivity().getApplicationContext(), "Successfully linked Facebook account", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    } else {
                        ParseFacebookUtils.unlinkInBackground(currentUser, new SaveCallback() {
                            @Override
                            public void done(ParseException ex) {
                                if (ex == null) {
                                    Log.d("MyApp", "The user is no longer associated with their Facebook account.");
                                    linkFacebookBtn.setTitle("Link Facebook account");
                                    linkFacebookBtn.setSummary("Connect a Facebook account to " + getResources().getString(R.string.app_name));
                                }
                            }
                        });
                    }
                    return true;
                }
            });
        }
    }
}
