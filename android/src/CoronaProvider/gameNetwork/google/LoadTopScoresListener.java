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
				L.setField(-2, "type");

				L.newTable(scores.getCount(), 0);

				for(int i = 0; i<scores.getCount(); i++) {
					leaderBoardScore = scores.get(i);
					dumpOnTable(L, leaderBoardScore, i+1);
				}

				L.setField(-2, "data");

				try {
					CoronaLua.dispatchEvent(L, fListener, 0);
					CoronaLua.deleteRef(L, fListener);
				} catch(Exception ex) {
					ex.printStackTrace();
				}
			}

			private void dumpOnTable(LuaState L, LeaderboardScore score, int index) {
				Player player = score.getScoreHolder();
				L.newTable(0, 7);

				L.pushString(player.getPlayerId());
				L.setField(-2, "playerID");

				L.pushString(fCategory);
				L.setField(-2, "category");

				L.pushNumber((double)score.getRawScore());
				L.setField(-2, "value");					

				Date date = new Date(score.getTimestampMillis());
				L.pushString(date.toString());
				L.setField(-2, "date");

				L.pushString(score.getDisplayScore());
				L.setField(-2, "formattedValue");

				L.pushNumber((double)score.getRank());
				L.setField(-2, "rank");

				L.pushNumber(score.getTimestampMillis());
				L.setField(-2, "unixTime");

				L.rawSet(-2, index);
			}
		};
		fDispatcher.send(task);
	}
}