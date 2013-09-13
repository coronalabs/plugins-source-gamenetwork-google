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

import java.util.Date;

import com.naef.jnlua.LuaState;

import com.ansca.corona.CoronaLua;
import com.ansca.corona.CoronaRuntime;
import com.ansca.corona.CoronaRuntimeTaskDispatcher;
import com.ansca.corona.CoronaRuntimeTask;

import com.google.android.gms.games.leaderboard.OnLeaderboardScoresLoadedListener;
import com.google.android.gms.games.leaderboard.LeaderboardScore;
import com.google.android.gms.games.leaderboard.LeaderboardBuffer;
import com.google.android.gms.games.leaderboard.LeaderboardScoreBuffer;
import com.google.android.gms.games.Player;

public class LoadTopScoresListener extends Listener implements OnLeaderboardScoresLoadedListener {
	private String fCategory;

	public LoadTopScoresListener(CoronaRuntimeTaskDispatcher _dispatcher, int _listener, String _category) {
		super(_dispatcher, _listener);
		fCategory = _category;
	}

	public void onLeaderboardScoresLoaded(int statusCode, LeaderboardBuffer _leaderboard, LeaderboardScoreBuffer _scores) {
		if (fListener < 0) {
			return;
		}

		final LeaderboardBuffer leaderboards = _leaderboard;
		final LeaderboardScoreBuffer scores = _scores;
		CoronaRuntimeTask task = new CoronaRuntimeTask() {
			@Override
			public void executeUsing(CoronaRuntime runtime) {
				LuaState L = runtime.getLuaState();
				LeaderboardScore leaderBoardScore;
				CoronaLua.newEvent(L, "loadScores");
				
				L.pushString("loadScores");
				L.setField(-2, TYPE);

				L.newTable(scores.getCount(), 0);

				for(int i = 0; i<scores.getCount(); i++) {
					leaderBoardScore = scores.get(i);
					Listener.pushLeaderboardScoreToLua(L, leaderBoardScore, fCategory);
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