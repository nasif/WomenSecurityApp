
package com.tavant.droid.security.activities;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Window;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import com.tavant.droid.security.R;
import com.tavant.droid.security.utils.WSConstants;

public class TermsAbout extends ActionBarActivity{

	

	private TextView mAboutText1 = null;
	private TextView mAboutText2 = null;
	private TextView mAboutText3 = null;
	private TextView mAboutText4 = null;

	private WebView mTermsWeb = null;
	private TextView mTermsTitle = null;
	private static String mVersion = null;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		int isTer = getIntent().getExtras().getInt(WSConstants.TYPE);
		switch (isTer) {
		case WSConstants.TYPE_HELP:
			//setContentView(R.layout.about);
			break;
        case WSConstants.TYPE_LOGIN:
        	//setContentView(isTerms ? R.layout.terms : R.layout.about);
			break;
        case WSConstants.TYPE_TERMS:
        	setContentView(R.layout.terms);
        	initTerms();
	    break;
		default:
			break;
		}
		mTermsWeb.setWebViewClient(new WebViewClient(){
				@Override
				public void onPageFinished(WebView view, String url) {
					super.onPageFinished(view, url);
					//mProgress.setVisibility(View.GONE);
				}
				@Override
				public void onPageStarted(WebView view, String url,Bitmap favicon) {
					super.onPageStarted(view, url, favicon);
				}
			});
			mTermsWeb.loadUrl("http://code.vivox.com/bobsled/policy/Vivox_T-Mobile_EULA.html");
	}

	private void initTerms(){
		mTermsTitle =(TextView)findViewById(R.id.TermsTitle);
		//mTermsTitle.setTypeface(mTypeFace);
		mTermsWeb =(WebView)findViewById(R.id.termsWebview);
		//mProgress =(ProgressBar)findViewById(R.id.TemrsProgress);
	}
	
}