package com.tavant.droid.security.activities;


import group.pals.android.lib.ui.lockpattern.LockPatternActivity;
import group.pals.android.lib.ui.lockpattern.prefs.SecurityPrefs;

import org.apache.http.client.methods.HttpRequestBase;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

import com.tavant.droid.security.BaseActivity;
import com.tavant.droid.security.HomeActivity;
import com.tavant.droid.security.R;
import com.tavant.droid.security.adapters.SettingsAdapter;
import com.tavant.droid.security.data.BaseData;
import com.tavant.droid.security.database.ContentDescriptor;
import com.tavant.droid.security.http.HttpRequestCreater;
import com.tavant.droid.security.lock.LPEncrypter;
import com.tavant.droid.security.lock.LockScreenActivity;
import com.tavant.droid.security.prefs.CommonPreferences;
import com.tavant.droid.security.utils.Utils;
import com.tavant.droid.security.utils.VolunteerStatus;
import com.tavant.droid.security.utils.WSConstants;

public class SettingsActivity extends BaseActivity implements VolunteerStatus{
	Preference facebookPref=null;
	Preference pattrenpref=null;
	Preference buzzerPref=null;
	Preference friendsPref=null;
	SharedPreferences pref=null;

	public static final int REQ_CREATE_PATTERN = 0;
	public static final int REQ_ENTER_PATTERN = 1;
	private ContentResolver resolver;
	private Cursor fbCursor=null;
	private Cursor friendCursor=null;
	private ListView listview=null;
	private boolean isVolunteer=false;

	private boolean issettings=false;

	private  int title[]={R.string.str_facebook,
			R.string.str_contacts,R.string.str_buzzer,
			R.string.str_friends,R.string.str_volunteer,
			R.string.str_pattern};
	private int desc[]={R.string.str_facebook_desc,
			R.string.str_contacts_desc,R.string.str_buzzer_desc,
			R.string.str_friends_desc,R.string.str_volunteer_desc,
			R.string.str_pattern_desc};
	private int desc_long[]={R.string.str_facebook_desc_long,
			R.string.str_contacts_desc_long,R.string.str_buzzer_desc_long,
			R.string.str_friends_desc_long,R.string.str_volunteer_desc_long,
			R.string.str_pattern_desc_long};




	private SettingsAdapter adapter=null;
	private CommonPreferences prefs=null;
	private boolean showMenu=false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.settingslist);
		getSupportActionBar().setDisplayHomeAsUpEnabled(false);
		listview=(ListView) findViewById(R.id.friendslist);
		prefs=CommonPreferences.getInstance();
		adapter=new SettingsAdapter(this,title,desc,desc_long);
		listview.setAdapter(adapter);
		SecurityPrefs.setAutoSavePattern(this, true);
		SecurityPrefs.setEncrypterClass(this, LPEncrypter.class);
		resolver=getContentResolver();
		fbCursor =resolver.query(ContentDescriptor.WSFacebook.CONTENT_URI, null, null, null, null);
		friendCursor=resolver.query(ContentDescriptor.WSContact.CONTENT_URI, null, null, null, null);
		issettings=getIntent().getBooleanExtra("issetting", false);
		if(fbCursor!=null&&fbCursor.getCount()>0&&friendCursor!=null&&friendCursor.getCount()>0&&!issettings){
			fbCursor.close();
			friendCursor.close();
			startActivity(new Intent(this, HomeActivity.class));	
			finish();
			return;
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		fbCursor =resolver.query(ContentDescriptor.WSFacebook.CONTENT_URI, null, null, null, null);
		friendCursor=resolver.query(ContentDescriptor.WSContact.CONTENT_URI, null, null, null, null);
		if(fbCursor!=null&&fbCursor.getCount()>0&&friendCursor!=null&&friendCursor.getCount()>0&&!issettings){
			showMenu=true;
	    	if(Utils.hasHoneycomb())
	    	  invalidateOptionsMenu();
	    	else
	    		ActivityCompat.invalidateOptionsMenu(SettingsActivity.this);
		}
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if(item.getItemId()==R.id.action_done)
			finish();
		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.settings_menu, menu);
		MenuItem item=menu.findItem(R.id.action_done);
		if(showMenu){
			item.setVisible(showMenu);
		}
		return true;
	}

	@Override
	public void startpatternActivity() {
		loadSettings();
	}

	private void loadSettings() {

		SharedPreferences prfs = getSharedPreferences(
				"AUTHENTICATION_FILE_NAME", Context.MODE_PRIVATE);
		String pattaren = prfs.getString("drawpattern", null);
		System.out.println("pattern code >>>>>>>>>>>>>>>>>> " + pattaren);
		if (pattaren != null) {
			Intent intentActivity = new Intent(
					LockPatternActivity.ACTION_COMPARE_PATTERN, null, this,
					LockPatternActivity.class);
			intentActivity.putExtra(LockPatternActivity.EXTRA_PATTERN,
					pattaren.toCharArray());
			startActivityForResult(intentActivity,
					LockScreenActivity.REQ_ENTER_PATTERN);
		} else {
			Intent intentActivity = new Intent(
					LockPatternActivity.ACTION_CREATE_PATTERN,
					null, this,
					LockPatternActivity.class);
			startActivityForResult(intentActivity,
					REQ_CREATE_PATTERN);
		}

	}


	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case REQ_CREATE_PATTERN: 
			if (resultCode == RESULT_OK){
				char array[]=data.getCharArrayExtra(LockPatternActivity.EXTRA_PATTERN); 
				Log.d("drawpattern",new String(array)); 
				// Save this prefrnce and read it from prefrence when we need to validate
				SharedPreferences preferences = getSharedPreferences("AUTHENTICATION_FILE_NAME", Context.MODE_PRIVATE);
				SharedPreferences.Editor editor = preferences.edit();
				editor.putString("drawpattern",new String(array));
				editor.commit();
			} 
			break;
		case REQ_ENTER_PATTERN: {
			int msgId = 0;
			switch (resultCode) {
			case RESULT_OK:
				msgId = android.R.string.ok;
				Intent intentActivity = new Intent(
						LockPatternActivity.ACTION_CREATE_PATTERN,
						null, this,
						LockPatternActivity.class);
				startActivityForResult(intentActivity,
						REQ_CREATE_PATTERN);
				break;
			case RESULT_CANCELED:
				msgId = android.R.string.cancel;
				break;
			case LockPatternActivity.RESULT_FAILED:
				msgId = 0;
				break;
			default:
				return;
			}
			break;
		}
		}
	}

	@Override
	public void changetoVolunteer(boolean status) {
		isVolunteer=status;
		HttpRequestBase post=HttpRequestCreater.editUser(prefs.getFbId(), null,null,null,null,(isVolunteer ? 1:0),null);
		onExecute(WSConstants.CODE_USER_API,post, false);
	}

	@Override
	protected void onComplete(int reqCode, BaseData data) {
		prefs.setIsvolunteer(isVolunteer);
	}

	@Override
	protected void onError(int reqCode, int errorCode, String errorMessage) {
		Toast.makeText(this, "server error", Toast.LENGTH_SHORT).show();
	}

	

	

}



