//
//  LuaLoader.java
//  TemplateApp
//
//  Copyright (c) 2012 __MyCompanyName__. All rights reserved.
//

// This corresponds to the name of the Lua library,
// e.g. [Lua] require "plugin.library"
package CoronaProvider.gameNetwork.google;

import java.lang.Double;
import java.util.HashSet;
import java.util.Iterator;

import android.app.Activity;
import android.util.Log;

import com.naef.jnlua.LuaState;
import com.naef.jnlua.JavaFunction;
import com.naef.jnlua.NamedJavaFunction;

import com.ansca.corona.CoronaActivity;
import com.ansca.corona.CoronaEnvironment;
import com.ansca.corona.CoronaLua;
import com.ansca.corona.CoronaRuntime;
import com.ansca.corona.CoronaRuntimeListener;
import com.ansca.corona.CoronaRuntimeTaskDispatcher;
import com.ansca.corona.CoronaRuntimeTask;

import com.google.android.gms.games.Player;
import com.google.android.gms.games.OnPlayersLoadedListener;
import com.google.android.gms.games.PlayerBuffer;
import com.google.android.gms.games.leaderboard.LeaderboardVariant;
import com.google.android.gms.games.GamesClient;

public class PlayerLoader {

	protected HashSet<String> fNameSet;
	protected HashSet<Player> fPlayerSet;
	protected CoronaRuntimeTaskDispatcher fDispatcher;
	protected int fListener;
	protected GamesClient fGamesClient;
	protected String fEventName;

	public PlayerLoader(CoronaRuntimeTaskDispatcher _dispatcher, int _listener, GamesClient gamesClient, String eventName) {
		fPlayerSet = new HashSet<Player>();
		fDispatcher = _dispatcher;
		fListener = _listener;
		fGamesClient = gamesClient;
		fEventName = eventName;
	}

	public void loadPlayers(HashSet<String> nameSet) {
		fNameSet = nameSet;

		HashSet<String> deepCopy = new HashSet<String>();
		Iterator<String> iter = fNameSet.iterator();
		while(iter.hasNext()) {
			String stringCopy = new String(iter.next());
			deepCopy.add(stringCopy);
		}		

		iter = deepCopy.iterator();
		while(iter.hasNext()) {
			fGamesClient.loadPlayer(new LoadPlayerListener(), iter.next());
		}
	}

	public class LoadPlayerListener implements OnPlayersLoadedListener {
		public void onPlayersLoaded(int statusCode, PlayerBuffer buffer) {
			for(int i = 0; i<buffer.getCount(); i++) {
				fPlayerSet.add(buffer.get(i));
				fNameSet.remove(buffer.get(i).getPlayerId());
			}

			if (fNameSet.size()<1) {
				callback();
			}
		}

		private void callback() {
			if (fListener < 0) {
				return;
			}

			final HashSet<Player> playerSet = fPlayerSet;
			CoronaRuntimeTask task = new CoronaRuntimeTask() {
				@Override
				public void executeUsing(CoronaRuntime runtime) {
					Iterator<Player> iter = playerSet.iterator();
					Player player;

					LuaState L = runtime.getLuaState();

					CoronaLua.newEvent(L, fEventName);

					L.newTable(playerSet.size(), 0);

					int count = 1;
					while(iter.hasNext()) {
						player = iter.next();

						L.newTable(0, 2);

						L.pushString(player.getPlayerId());
						L.setField(-2, "playerID");

						L.pushString(player.getDisplayName());
						L.setField(-2, "alias");

						L.rawSet(-2, count);
						count++;
					}

					L.setField(-2, "data");

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
}