//
//  LuaLoader.java
//  TemplateApp
//
//  Copyright (c) 2012 __MyCompanyName__. All rights reserved.
//

// This corresponds to the name of the Lua library,
// e.g. [Lua] require "plugin.library"
package CoronaProvider.gameNetwork.google;

import android.util.Log;

import java.util.Date;

import com.naef.jnlua.LuaState;

import com.ansca.corona.CoronaRuntimeTaskDispatcher;
import com.google.android.gms.games.leaderboard.SubmitScoreResult;
import com.google.android.gms.games.leaderboard.LeaderboardVariant;
import com.google.android.gms.games.leaderboard.LeaderboardScore;
import com.google.android.gms.games.leaderboard.Leaderboard;

import com.google.android.gms.games.achievement.Achievement;

import com.google.android.gms.games.multiplayer.Invitation;

import com.google.android.gms.games.Player;

public abstract class Listener {
	public final static String DATA = "data";
	public final static String TYPE = "type";
	public final static String PLAYER_ID = "playerID";
	public final static String ALIAS = "alias";

	protected CoronaRuntimeTaskDispatcher fDispatcher;
	protected int fListener;

	public Listener(CoronaRuntimeTaskDispatcher _dispatcher, int _listener) {
		fListener = _listener;
		fDispatcher = _dispatcher;
	}

	static protected void pushSubmitScoreResultToLua(LuaState L, SubmitScoreResult scoreResult) {
		L.newTable(4, 0);

		L.pushString(scoreResult.getLeaderboardId());
		L.setField(-2, "category");

		SubmitScoreResult.Result result = scoreResult.getScoreResult(LeaderboardVariant.TIME_SPAN_ALL_TIME);
		if (result != null) {
			L.pushNumber((double)result.rawScore);
			L.setField(-2, "value");

			L.pushString(result.formattedScore);
			L.setField(-2, "formattedValue");
		}
		
		L.pushString(scoreResult.getPlayerId());
		L.setField(-2, PLAYER_ID);
	}

	static protected void pushLeaderboardScoreToLua(LuaState L, LeaderboardScore score, String category) {
		Player player = score.getScoreHolder();
		L.newTable(0, 7);

		L.pushString(player.getPlayerId());
		L.setField(-2, PLAYER_ID);

		L.pushString(category);
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
	}

	static protected void pushLeaderboardToLua(LuaState L, Leaderboard leaderboard) {
		L.newTable(0, 2);

		L.pushString(leaderboard.getLeaderboardId());
		L.setField(-2, "category");

		L.pushString(leaderboard.getDisplayName());
		L.setField(-2, "title");
	}

	static protected void pushAchievementToLua(LuaState L, Achievement achievement) {
		L.newTable(0, 6);

		L.pushString(achievement.getAchievementId());
		L.setField(-2, "identifier");

		L.pushString(achievement.getName());
		L.setField(-2, "title");

		L.pushString(achievement.getDescription());
		L.setField(-2, "description");					

		L.pushBoolean(achievement.getState() == Achievement.STATE_UNLOCKED);
		L.setField(-2, "isCompleted");

		L.pushBoolean(achievement.getState() == Achievement.STATE_HIDDEN);
		L.setField(-2, "isHidden");

		L.pushNumber(achievement.getLastUpdatedTimestamp());
		L.setField(-2, "lastReportedDate");
	}

	static protected void pushInvitationToLua(LuaState L, Invitation invitation) {
		L.newTable(0, 3);

		L.pushString(invitation.getInvitationId());
		L.setField(-2, RoomManager.ROOM_ID);

		L.pushString(invitation.getInviter().getDisplayName());
		L.setField(-2, ALIAS);

		L.pushString(invitation.getInviter().getPlayer().getPlayerId());
		L.setField(-2, PLAYER_ID);
	}

	static protected void pushPlayerToLua(LuaState L, Player player) {
		L.newTable(0, 2);
						
		L.pushString(player.getPlayerId());
		L.setField(-2, PLAYER_ID);

		L.pushString(player.getDisplayName());
		L.setField(-2, ALIAS);
	}
}