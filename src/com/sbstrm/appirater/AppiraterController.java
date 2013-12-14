package com.sbstrm.appirater;

import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public abstract class AppiraterController {

	public abstract void setupTitle(TextView view);
	public abstract void setupIcon(ImageView imageView);
	public abstract void setupRate(Button view);
	public abstract void setupRequestSuggestion(TextView view);
	public abstract void setupContactSupport(TextView view);
	public abstract void setupContactSupportFooter(TextView view);
	public abstract void contactSupportViewTapped();

}
