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

import android.app.Activity;
import android.util.Log;

import java.util.ArrayList;
import java.util.Iterator;

import com.naef.jnlua.LuaState;

import com.ansca.corona.CoronaActivity;
import com.ansca.corona.CoronaLua;
import com.ansca.corona.CoronaRuntime;
import com.ansca.corona.CoronaRuntimeTaskDispatcher;
import com.ansca.corona.CoronaRuntimeTask;

import com.google.android.gms.games.GamesClient;
import com.google.android.gms.games.GamesActivityResultCodes;

public class SelectPlayersResultHandler extends Listener implements CoronaActivity.OnActivityResultHandler {
	private GameHelper fGameHelper;

	public SelectPlayersResultHandler(CoronaRuntimeTaskDispatcher _dispatcher, int _listener, GameHelper _gameHelper) {
		super(_dispatcher, _listener);
		fGameHelper = _gameHelper;
	}

	@Override
	public void onHandleActivityResult(CoronaActivity activity, int requestCode, int resultCode, android.content.Intent data) {
		activity.unregisterActivityResultHandler(this);
		CoronaRuntimeTaskDispatcher dispatcher = activity.getRuntimeTaskDispatcher();
		ArrayList<String> invitees = null;
		int minAutoMatchPlayers = 0;
    	int maxAutoMatchPlayers = 0;
    	String phase = null;
		if (Activity.RESULT_OK == resultCode) {
			invitees = data.getStringArrayListExtra(GamesClient.EXTRA_PLAYERS);
			minAutoMatchPlayers = data.getIntExtra(GamesClient.EXTRA_MIN_AUTOMATCH_PLAYERS, 0);
        	maxAutoMatchPlayers = data.getIntExtra(GamesClient.EXTRA_MAX_AUTOMATCH_PLAYERS, 0);
        	phase = "selected";
        } else if (GamesActivityResultCodes.RESULT_RECONNECT_REQUIRED == resultCode) {
        	phase = "logout";
        	if (fGameHelper != null && fGameHelper.getGamesClient() != null) {
				fGameHelper.signOut();
			}
        } else { //Cancelled
			invitees = new ArrayList<String>();
			phase = "cancelled";
		}
		pushSelectedPlayersToLua(invitees, minAutoMatchPlayers, maxAutoMatchPlayers, phase);
	}

	private void pushSelectedPlayersToLua(final ArrayList<String> invitees, final int minAutoMatch, final int maxAutoMatch, final String phase) {
		CoronaRuntimeTask task = new CoronaRuntimeTask() {
			@Override
			public void executeUsing(CoronaRuntime runtime) {
				LuaState L = runtime.getLuaState();
				
				CoronaLua.newEvent(L, "selectPlayers");

				L.pushString("selectPlayers");
				L.setField(-2, TYPE);
				
				L.newTable();

				int count = 1;
				Iterator<String> iter = invitees.iterator();

				while(iter.hasNext()) {
					L.pushString(iter.next());
					L.rawSet(-2, count);
					count++;
				}

				L.pushNumber(minAutoMatch);
				L.setField(-2, "minAutoMatchPlayers");

				L.pushNumber(maxAutoMatch);
				L.setField(-2, "maxAutoMatchPlayers");

				L.pushString(phase);
				L.setField(-2, "phase");

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