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
	protected boolean fLocalPlayer;

	public PlayerLoader(CoronaRuntimeTaskDispatcher _dispatcher, int _listener, GamesClient gamesClient, String eventName) {
		fPlayerSet = new HashSet<Player>();
		fDispatcher = _dispatcher;
		fListener = _listener;
		fGamesClient = gamesClient;
		fEventName = eventName;
	}

	/**
	 * @Param nameSet HashSet<String> this is the name of all the players to load
	 * @Param localPlayer boolean if its loading the localPlayer then event.data.alias will be populated instead of event.data[1].alias
	 *        this is to match the functionality on iOS
	 */
	public void loadPlayers(HashSet<String> nameSet, boolean localPlayer) {
		fNameSet = nameSet;

		HashSet<String> deepCopy = new HashSet<String>();
		Iterator<String> iter = fNameSet.iterator();
		while( iter.hasNext()) {
			String stringCopy = new String(iter.next());
			deepCopy.add(stringCopy);
		}		

		fLocalPlayer = localPlayer;

		iter = deepCopy.iterator();
		while (iter.hasNext()) {
			fGamesClient.loadPlayer(new LoadPlayerListener(), iter.next());
		}
	}

	public class LoadPlayerListener implements OnPlayersLoadedListener {
		public void onPlayersLoaded(int statusCode, PlayerBuffer buffer) {
			for (int i = 0; i<buffer.getCount(); i++) {
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

			final HashSet<Player> finalPlayerSet = fPlayerSet;
			CoronaRuntimeTask task = new CoronaRuntimeTask() {
				@Override
				public void executeUsing(CoronaRuntime runtime) {
					Iterator<Player> iter = finalPlayerSet.iterator();
					Player player;

					LuaState L = runtime.getLuaState();

					CoronaLua.newEvent(L, fEventName);

					L.newTable(finalPlayerSet.size(), 0);

					int count = 1;
					while (iter.hasNext()) {
						player = iter.next();

						//This is done because on iOS loadLocalPlayer's data is in event.data instead of event.data[1]
						if (!fLocalPlayer && finalPlayerSet.size() == 1) {
							L.newTable(0, 2);
						}
						
						L.pushString(player.getPlayerId());
						L.setField(-2, "playerID");

						L.pushString(player.getDisplayName());
						L.setField(-2, "alias");

						//This is done because on iOS loadLocalPlayer's data is in event.data instead of event.data[1]
						if (!fLocalPlayer && finalPlayerSet.size() == 1) {
							L.rawSet(-2, count);
							count++;
						}
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