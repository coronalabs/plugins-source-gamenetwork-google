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

import com.google.android.gms.games.achievement.Achievement;
import com.google.android.gms.games.achievement.AchievementBuffer;
import com.google.android.gms.games.achievement.OnAchievementsLoadedListener;

public class LoadAchievementsListener extends Listener implements OnAchievementsLoadedListener {
	public LoadAchievementsListener(CoronaRuntimeTaskDispatcher _dispatcher, int _listener) {
		super(_dispatcher, _listener);
	}

	public void onAchievementsLoaded(int _statusCode, AchievementBuffer _buffer) {
		if (fListener < 0) {
			return;
		}

		final AchievementBuffer buffer = _buffer;
		CoronaRuntimeTask task = new CoronaRuntimeTask() {
			@Override
			public void executeUsing(CoronaRuntime runtime) {
				LuaState L = runtime.getLuaState();
				Achievement achievement = null;
				CoronaLua.newEvent(L, "loadAchievements");

				L.pushString("loadAchievements");
				L.setField(-2, TYPE);
				
				L.newTable(buffer.getCount(), 0);

				for(int i = 0; i<buffer.getCount(); i++) {
					achievement = buffer.get(i);
					Listener.pushAchievementToLua(L, achievement);
					L.rawSet(-2, i+1);
				}

				L.setField(-2, DATA);

				try {
					CoronaLua.dispatchEvent(L, fListener, 0);
					CoronaLua.deleteRef(L, fListener);
				} catch(Exception ex) {
					ex.printStackTrace();
				}
				
			}
		};
		fDispatcher.send(task);
	}
}
