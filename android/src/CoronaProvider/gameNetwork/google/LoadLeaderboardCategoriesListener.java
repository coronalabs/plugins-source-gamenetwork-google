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
					LuaState L = runtime.getLuaState();
					CoronaLua.newEvent( L, "loadLeaderboardCategories" );

					Leaderboard leaderboard;

					L.newTable(buffer.getCount(), 0);

					for(int i = 0; i<buffer.getCount(); i++) {
						leaderboard = buffer.get(i);
						dumpOnTable(L, leaderboard, i+1);
					}

					L.setField(-2, "data");

					CoronaLua.dispatchEvent( L, fListener, 0);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}

			private void dumpOnTable(LuaState L, Leaderboard leaderboard, int index) {
				L.newTable(0, 4);

				L.pushString(leaderboard.getLeaderboardId());
				L.setField(-2, "category");

				L.pushString(leaderboard.getDisplayName());
				L.setField(-2, "title");

				L.rawSet(-2, index);
			}
		};

		fDispatcher.send(task);
	}
}