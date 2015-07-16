package com.sfo.games.puzzlebobble;

import android.os.Bundle;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;

public class AndroidActivity extends AndroidApplication
{
	public void onCreate(Bundle paramBundle)
	{
		super.onCreate(paramBundle);
		AndroidApplicationConfiguration localAndroidApplicationConfiguration = new AndroidApplicationConfiguration();
		localAndroidApplicationConfiguration.useAccelerometer = false;
		localAndroidApplicationConfiguration.useCompass = false;
		localAndroidApplicationConfiguration.useWakelock = true;
		localAndroidApplicationConfiguration.useGL20 = true;
		localAndroidApplicationConfiguration.hideStatusBar = true;
		Game.i = new Game();
		initialize(Game.i, true);
	}
}