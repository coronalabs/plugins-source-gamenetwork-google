//
//  LuaLoader.java
//  TemplateApp
//
//  Copyright (c) 2012 __MyCompanyName__. All rights reserved.
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
import com.google.android.gms.games.multiplayer.realtime.Room;

public class WaitingRoomResultHandler extends Listener implements CoronaActivity.OnActivityResultHandler {
	private GameHelper fGameHelper;

	public WaitingRoomResultHandler(CoronaRuntimeTaskDispatcher _dispatcher, int _listener, GameHelper _gameHelper) {
		super(_dispatcher, _listener);
		fGameHelper = _gameHelper;
	}

	@Override
	public void onHandleActivityResult(CoronaActivity activity, int requestCode, int resultCode, android.content.Intent data) {
		activity.unregisterActivityResultHandler(this);
		CoronaRuntimeTaskDispatcher dispatcher = activity.getRuntimeTaskDispatcher();
		Room room = ((Room)data.getExtras().get(GamesClient.EXTRA_ROOM));
		if (Activity.RESULT_OK == resultCode) {
			pushIntentResult(false, "start", room);
		} else if (Activity.RESULT_CANCELED == resultCode || GamesActivityResultCodes.RESULT_LEFT_ROOM == resultCode){ //Cancelled
			pushIntentResult(true, "cancel", room);
			if (fGameHelper != null && fGameHelper.getGamesClient() != null) {
				fGameHelper.getGamesClient().leaveRoom(RoomManager.getRoomManager(fDispatcher, fListener, fGameHelper.getGamesClient()), room.getRoomId());
			}
		} else if (GamesActivityResultCodes.RESULT_RECONNECT_REQUIRED == resultCode) {
			if (fGameHelper != null && fGameHelper.getGamesClient() != null) {
				fGameHelper.signOut();
			}
		}
	}

	public void pushIntentResult(final boolean isError, final String phase, final Room room) {
		if (fListener < 0) {
			return;
		}

		final GamesClient finalGamesClient = fGameHelper.getGamesClient();

		CoronaRuntimeTask task = new CoronaRuntimeTask() {
			@Override
			public void executeUsing(CoronaRuntime runtime) {
				LuaState L = runtime.getLuaState();
				CoronaLua.newEvent(L, "waitingRoom");

				L.pushString("waitingRoom");
				L.setField(-2, TYPE);

				L.newTable();

				L.pushBoolean(isError);
				L.setField(-2, "isError");

				L.pushString(phase);
				L.setField(-2, "phase");

				L.pushString(room.getRoomId());
				L.setField(-2, RoomManager.ROOM_ID);

				int count = 1;
				ArrayList<String> participantIds = room.getParticipantIds();
				for(int i = 0; i < participantIds.size(); i++) {
					if(participantIds.get(i) != room.getParticipantId(finalGamesClient.getCurrentPlayerId())) {
						L.pushString(participantIds.get(i));
						L.rawSet(-2, count);
						count++;
					}
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