//
//  LuaLoader.java
//  TemplateApp
//
//  Copyright (c) 2012 __MyCompanyName__. All rights reserved.
//

// This corresponds to the name of the Lua library,
// e.g. [Lua] require "plugin.library"
package CoronaProvider.gameNetwork.google;

import CoronaProvider.gameNetwork.google.Listener;

import android.util.Log;

import com.naef.jnlua.LuaState;

import com.ansca.corona.CoronaLua;
import com.ansca.corona.CoronaRuntime;
import com.ansca.corona.CoronaRuntimeTaskDispatcher;
import com.ansca.corona.CoronaRuntimeTask;

import com.google.android.gms.games.achievement.OnAchievementUpdatedListener;


public class UnlockAchievementListener extends Listener implements OnAchievementUpdatedListener {
	public UnlockAchievementListener(CoronaRuntimeTaskDispatcher _dispatcher, int _listener) {
		super(_dispatcher, _listener);
	}

	public void onAchievementUpdated(int _statusCode, String _achievementId) {
		if (fListener < 0) {
			return;
		}

		final String achievementId = _achievementId;

		CoronaRuntimeTask task = new CoronaRuntimeTask() {
			@Override
			public void executeUsing(CoronaRuntime runtime) {
				LuaState L = runtime.getLuaState();
				CoronaLua.newEvent(L, "unlockAchievement");
				
				L.pushString(achievementId);
				L.setField(-2, "achievementId");

				try {
					CoronaLua.dispatchEvent(L, fListener, 0);
				} catch(Exception ex) {
					ex.printStackTrace();
				}
			}
		};
		fDispatcher.send(task);
	}
}