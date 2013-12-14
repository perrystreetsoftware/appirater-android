package com.sbstrm.appirater;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Paint;
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
	
    public static void appLaunched(Context mContext, AppiraterController controller) {
    	boolean testMode = mContext.getResources().getBoolean(R.bool.appirator_test_mode);
        SharedPreferences prefs = mContext.getSharedPreferences(mContext.getPackageName()+".appirater", 0);
        if(!testMode && (prefs.getBoolean(PREF_DONT_SHOW, false) || prefs.getBoolean(PREF_RATE_CLICKED, false))) {return;}
        
        SharedPreferences.Editor editor = prefs.edit();
        
        if(testMode){
        	showRateDialog(mContext, editor, controller);
        	return;
        }
        
        // Increment launch counter
        long launch_count = prefs.getLong(PREF_LAUNCH_COUNT, 0);

        // Get date of first launch
        long date_firstLaunch = prefs.getLong(PREF_DATE_FIRST_LAUNCHED, 0);
        
		// Get reminder date pressed
		long date_reminder_pressed = prefs.getLong(PREF_DATE_REMINDER_PRESSED, 0);
        
        try{
	        int appVersionCode = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0).versionCode;
	        if(prefs.getInt(PREF_APP_VERSION_CODE, 0)  != appVersionCode){
	        	//Reset the launch count to help assure users are rating based on the latest version. 
	        	launch_count = 0;
	        }
	        editor.putInt(PREF_APP_VERSION_CODE, appVersionCode);
        }catch(Exception e){
        	//do nothing
        }
        
        launch_count++;
        editor.putLong(PREF_LAUNCH_COUNT, launch_count);

        if (date_firstLaunch == 0) {
            date_firstLaunch = System.currentTimeMillis();
            editor.putLong(PREF_DATE_FIRST_LAUNCHED, date_firstLaunch);
        }
        
        // Wait at least n days before opening
        if (launch_count >= mContext.getResources().getInteger(R.integer.appirator_launches_until_prompt)) {
			long millisecondsToWait = mContext.getResources().getInteger(R.integer.appirator_days_until_prompt) * 24 * 60 * 60 * 1000L;			
			if (System.currentTimeMillis() >= (date_firstLaunch + millisecondsToWait)) {
				if(date_reminder_pressed == 0){
					showRateDialog(mContext, editor, controller);
				}else{
					long remindMillisecondsToWait = mContext.getResources().getInteger(R.integer.appirator_days_before_reminding) * 24 * 60 * 60 * 1000L;
					if(System.currentTimeMillis() >= (remindMillisecondsToWait + date_reminder_pressed)){
						showRateDialog(mContext, editor, controller);
					}
				}
			}
        }
        
        editor.commit();
    }   
    
	@SuppressLint("NewApi")
	private static void showRateDialog(final Context mContext, 
			final SharedPreferences.Editor editor, 
			final AppiraterController controller) {
    	String appName = mContext.getString(R.string.appirator_app_title);
        
    	AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
    	builder.setNegativeButton(R.string.rate_later, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
            	if (editor != null) {
    				editor.putLong(PREF_DATE_REMINDER_PRESSED,System.currentTimeMillis());
    				editor.commit();
				}
			}
    	});
        
        LinearLayout layout = (LinearLayout)LayoutInflater.from(mContext).inflate(R.layout.appirater, null);
    	
        ImageView imageView = (ImageView) layout.findViewById(R.id.image);
        controller.setupIcon(imageView);
        

        TextView tv = (TextView) layout.findViewById(R.id.message);
        controller.setupTitle(tv);
        
        builder.setView(layout);
        final AlertDialog dialog = builder.create();
        
        Button rateButton = (Button) layout.findViewById(R.id.rate);
        controller.setupRate(rateButton);
        rateButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                mContext.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(String.format(mContext.getString(R.string.appirator_market_url), mContext.getPackageName()))));
                if (editor != null) {
                    editor.putBoolean(PREF_RATE_CLICKED, true);
                    editor.commit();
                }
                dialog.dismiss();
            }
        });

        TextView cancelView = (TextView) layout.findViewById(R.id.cancel);
        SpannableString content = new SpannableString(cancelView.getText());
        content.setSpan(new UnderlineSpan(), 0, cancelView.getText().length(), 0);
        cancelView.setText(content);

        cancelView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
	              if (editor != null) {
	            	  editor.putBoolean(PREF_DONT_SHOW, true);
	            	  editor.commit();
	              }
	              dialog.dismiss();
			}
        });
        
        TextView requestSuggestionView = (TextView) layout.findViewById(R.id.request_suggestion);
        controller.setupRequestSuggestion(requestSuggestionView);
        
        TextView contactSupportView = (TextView) layout.findViewById(R.id.contact_support);
        controller.setupContactSupport(contactSupportView);
        
        content = new SpannableString(contactSupportView.getText());
        content.setSpan(new UnderlineSpan(), 0, contactSupportView.getText().length(), 0);
        contactSupportView.setText(content);
        
        contactSupportView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
	              dialog.dismiss();
	              controller.contactSupportViewTapped();
			}
        });

        TextView contactSupportFooterView = (TextView) layout.findViewById(R.id.contact_support_footer);
        controller.setupContactSupportFooter(contactSupportFooterView);

        dialog.show();        
    }
}