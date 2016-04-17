package com.sbstrm.appirater;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/*	
 * @source https://github.com/sbstrm/appirater-android
 * @license MIT/X11
 * 
 * Copyright (c) 2011-2013 sbstrm Y.K.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

public class Appirater {
    private static final String PREF_LAUNCH_COUNT = "launch_count";
    private static final String PREF_RATE_CLICKED = "rateclicked";
    private static final String PREF_DONT_SHOW = "dontshow";
    private static final String PREF_DATE_REMINDER_PRESSED = "date_reminder_pressed";
    private static final String PREF_DATE_FIRST_LAUNCHED = "date_firstlaunch";
    private static final String PREF_APP_VERSION_CODE = "versioncode";

    public interface AppiraterControllerListener {
        void showDialog(Appirater appirater);
    }

    private SharedPreferences getSharedPreferences(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(context.getPackageName() + ".appirater", 0);

        return prefs;
    }

    public void appLaunched(Context context, AppiraterControllerListener listener) {
        boolean testMode = context.getResources().getBoolean(R.bool.appirator_test_mode);

        SharedPreferences prefs = getSharedPreferences(context);

        if (!testMode && (prefs.getBoolean(PREF_DONT_SHOW, false) || prefs.getBoolean(PREF_RATE_CLICKED, false))) {
            return;
        }

        SharedPreferences.Editor editor = prefs.edit();

        if (testMode) {
            // showRateDialog(mContext, editor, controller);
            listener.showDialog(this);
            return;
        }

        // Increment launch counter
        long launch_count = prefs.getLong(PREF_LAUNCH_COUNT, 0);

        // Get date of first launch
        long date_firstLaunch = prefs.getLong(PREF_DATE_FIRST_LAUNCHED, 0);

        // Get reminder date pressed
        long date_reminder_pressed = prefs.getLong(PREF_DATE_REMINDER_PRESSED, 0);

        try {
            int appVersionCode = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
            if (prefs.getInt(PREF_APP_VERSION_CODE, 0) != appVersionCode) {
                //Reset the launch count to help assure users are rating based on the latest version.
                launch_count = 0;
            }
            editor.putInt(PREF_APP_VERSION_CODE, appVersionCode);
        } catch (Exception e) {
            //do nothing
        }

        launch_count++;
        editor.putLong(PREF_LAUNCH_COUNT, launch_count);

        if (date_firstLaunch == 0) {
            date_firstLaunch = System.currentTimeMillis();
            editor.putLong(PREF_DATE_FIRST_LAUNCHED, date_firstLaunch);
        }

        // Wait at least n days before opening
        if (launch_count >= context.getResources().getInteger(R.integer.appirator_launches_until_prompt)) {
            long millisecondsToWait = context.getResources().getInteger(R.integer.appirator_days_until_prompt) * 24 * 60 * 60 * 1000L;
            if (System.currentTimeMillis() >= (date_firstLaunch + millisecondsToWait)) {
                if (date_reminder_pressed == 0) {
                    listener.showDialog(this);
                } else {
                    long remindMillisecondsToWait = context.getResources().getInteger(R.integer.appirator_days_before_reminding) * 24 * 60 * 60 * 1000L;
                    if (System.currentTimeMillis() >= (remindMillisecondsToWait + date_reminder_pressed)) {
                        listener.showDialog(this);
                    }
                }
            }
        }

        editor.commit();
    }

    public void remindMeLaterSelected(Context context) {
        SharedPreferences prefs = this.getSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();

        if (editor != null) {
            editor.putLong(PREF_DATE_REMINDER_PRESSED, System.currentTimeMillis());
            editor.commit();
        }
    }

    public void rateSelected(Context context) {
        SharedPreferences prefs = this.getSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();

        if (editor != null) {
            editor.putBoolean(PREF_RATE_CLICKED, true);
            editor.commit();
        }
    }

    public void doNotShowSelected(Context context) {
        SharedPreferences prefs = this.getSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();

        if (editor != null) {
            editor.putBoolean(PREF_DONT_SHOW, true);
            editor.commit();
        }
    }
}