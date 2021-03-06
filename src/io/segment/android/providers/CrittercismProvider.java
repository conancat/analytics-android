package io.segment.android.providers;

import io.segment.android.errors.InvalidSettingsException;
import io.segment.android.models.EasyJSONObject;
import io.segment.android.models.Identify;
import io.segment.android.models.Screen;
import io.segment.android.models.Track;
import io.segment.android.models.Traits;
import io.segment.android.provider.SimpleProvider;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.text.TextUtils;

import com.crittercism.app.Crittercism;

public class CrittercismProvider extends SimpleProvider {

	private static class SettingKey { 

		private static final String APP_ID = "appId";
		
		private static final String DELAY_SENDING_APP_LOAD = "delaySendingAppLoad";
		private static final String INCLUDE_VERSION_CODE = "includeVersionCode";
		private static final String SHOULD_INCLUDE_LOGCAT = "shouldCollectLogcat";
	}
	
	@Override
	public String getKey() {
		return "Crittercism";
	}

	@Override
	public void validate(EasyJSONObject settings)
			throws InvalidSettingsException {

		if (TextUtils.isEmpty(settings.getString(SettingKey.APP_ID))) {
			throw new InvalidSettingsException(SettingKey.APP_ID, "Crittercism requires the appId setting.");
		}
	}

	@Override
	public void onCreate(Context context) {
		
		EasyJSONObject settings = this.getSettings();
		String appId = settings.getString(SettingKey.APP_ID);
		
		// docs: https://app.crittercism.com/developers/docs-optional-android
		
		JSONObject crittercismConfig = new JSONObject();
		try
		{
			// send app load data with Crittercism.sendAppLoadData()
			crittercismConfig.put("delaySendingAppLoad", 
					settings.getBoolean(SettingKey.DELAY_SENDING_APP_LOAD, false));
		    
			// necessary for collecting logcat data on Android Jelly Bean devices.
		    crittercismConfig.put("shouldCollectLogcat", 
		    		settings.getBoolean(SettingKey.SHOULD_INCLUDE_LOGCAT, false)); 
		    
		    // include version code in version name.
		    crittercismConfig.put("includeVersionCode", 
		    		settings.getBoolean(SettingKey.INCLUDE_VERSION_CODE, false));
		}
		catch (JSONException je){}
		
		Crittercism.init(context, appId, crittercismConfig);
		
		ready();
	}
	
	@Override
	public void identify(Identify identify) {
		String userId = identify.getUserId();
		Traits traits = identify.getTraits();
		
		Crittercism.setUsername(userId);
		if (traits != null) {
			
			if (traits.has("name"))
				Crittercism.setUsername(traits.getString("name"));
			
			Crittercism.setMetadata(traits);
		}
	}
	
	@Override
	public void screen(Screen screen) {
		// track a "Viewed SCREEN" event
		track(screen);
	}
	
	@Override
	public void track(Track track) {
		String event = track.getEvent();
		Crittercism.leaveBreadcrumb(event);
	}
	
	@Override
	public void flush() {
		Crittercism.sendAppLoadData();
	}
}
