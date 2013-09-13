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

import com.naef.jnlua.LuaState;

import com.ansca.corona.CoronaLua;
import com.ansca.corona.CoronaRuntime;
import com.ansca.corona.CoronaRuntimeTaskDispatcher;
import com.ansca.corona.CoronaRuntimeTask;

import com.google.android.gms.games.leaderboard.OnLeaderboardMetadataLoadedListener;
import com.google.android.gms.games.leaderboard.LeaderboardBuffer;
import com.google.android.gms.games.leaderboard.Leaderboard;

public class LoadLeaderboardCategoriesListener extends Listener implements OnLeaderboardMetadataLoadedListener {
	public LoadLeaderboardCategoriesListener(CoronaRuntimeTaskDispatcher _dispatcher, int _listener) {
		super(_dispatcher, _listener);
	}

	public void onLeaderboardMetadataLoaded(int _statusCode, LeaderboardBuffer _buffer) {
		if (fListener < 0) {
			return;
		}

		final LeaderboardBuffer buffer = _buffer;

		CoronaRuntimeTask task = new CoronaRuntimeTask() {
			@Override
			public void executeUsing(CoronaRuntime runtime) {
				try {
					Leaderboard leaderboard;
					
					LuaState L = runtime.getLuaState();
					CoronaLua.newEvent( L, "loadLeaderboardCategories" );

					L.pushString("loadLeaderboardCategories");
					L.setField(-2, TYPE);

					L.newTable(buffer.getCount() + 1, 0);

					for(int i = 0; i<buffer.getCount(); i++) {
						leaderboard = buffer.get(i);
						Listener.pushLeaderboardToLua(L, leaderboard);
						L.rawSet(-2, i+1);
					}

					L.setField(-2, DATA);

					CoronaLua.dispatchEvent( L, fListener, 0);
					CoronaLua.deleteRef(L, fListener);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		};

		fDispatcher.send(task);
	}
}