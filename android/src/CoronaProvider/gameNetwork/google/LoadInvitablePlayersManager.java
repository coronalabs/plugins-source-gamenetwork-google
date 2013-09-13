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

import CoronaProvider.gameNetwork.google.GameHelper;

import android.util.Log;
import java.util.HashMap;
import java.util.Iterator;

import com.naef.jnlua.LuaState;

import com.ansca.corona.CoronaLua;
import com.ansca.corona.CoronaRuntime;
import com.ansca.corona.CoronaRuntimeTaskDispatcher;
import com.ansca.corona.CoronaRuntimeTask;

import com.google.android.gms.games.Player;
import com.google.android.gms.games.PlayerBuffer;
import com.google.android.gms.games.GamesClient;
import com.google.android.gms.games.OnPlayersLoadedListener;

public class LoadInvitablePlayersManager {
	private CoronaRuntimeTaskDispatcher fDispatcher;
	private int fListener;
	private GamesClient fGamesClient;
	
	public LoadInvitablePlayersManager(CoronaRuntimeTaskDispatcher _dispatcher, int _listener,  GamesClient _gamesClient) {
		fDispatcher = _dispatcher;
		fListener = _listener;
		fGamesClient = _gamesClient;
	}

	public void load() {
		if (fDispatcher != null && fListener >0 && fGamesClient != null) {
			//The max page size per load is 25
			fGamesClient.loadMoreInvitablePlayers(new LoadPlayerListener(), 25);
		}
	}

	public class LoadPlayerListener implements OnPlayersLoadedListener {
		private HashMap<String, Player> fPlayers;

		public LoadPlayerListener() {
			fPlayers = new HashMap<String, Player>();
		}

		public void onPlayersLoaded(int statusCode, PlayerBuffer buffer) {
			if (buffer.getCount() > 1) {
				for (int i = 0; i<buffer.getCount(); i++) {
					String playerId = buffer.get(i).getPlayerId();
					if (fPlayers.containsKey(playerId)) {
						callback();
						return;
					} else {
						fPlayers.put(playerId, buffer.get(i));
					}
				}
				fGamesClient.loadMoreInvitablePlayers(this, 25);
			} else {
				callback();
			}
		}

		private void callback() {
			if (fListener < 0) {
				return;
			}

			CoronaRuntimeTask task = new CoronaRuntimeTask() {
				@Override
				public void executeUsing(CoronaRuntime runtime) {
					Iterator<Player> iter = fPlayers.values().iterator();
					Player player;

					LuaState L = runtime.getLuaState();

					CoronaLua.newEvent(L, "loadFriends");

					L.pushString("loadFriends");
					L.setField(-2, Listener.TYPE);

					L.newTable();

					int count = 1;
					while (iter.hasNext()) {
						player = iter.next();

						Listener.pushPlayerToLua(L, player);

						L.rawSet(-2, count);
						count++;
					}

					L.setField(-2, Listener.DATA);

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