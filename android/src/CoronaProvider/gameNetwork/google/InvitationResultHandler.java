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
import com.google.android.gms.games.multiplayer.Invitation;

public class InvitationResultHandler extends Listener implements CoronaActivity.OnActivityResultHandler {
	private GameHelper fGameHelper;

	public InvitationResultHandler(CoronaRuntimeTaskDispatcher _dispatcher, int _listener, GameHelper _gameHelper) {
		super(_dispatcher, _listener);
		fGameHelper = _gameHelper;
	}

	@Override
	public void onHandleActivityResult(CoronaActivity activity, int requestCode, int resultCode, android.content.Intent data) {
		activity.unregisterActivityResultHandler(this);
		CoronaRuntimeTaskDispatcher dispatcher = activity.getRuntimeTaskDispatcher();

		if (Activity.RESULT_OK == resultCode) { //return the room id that the user chose to join
			Invitation inv = data.getExtras().getParcelable(GamesClient.EXTRA_INVITATION);
			pushInvitationsToLua(inv.getInvitationId(), false, "selected");
		} else if(GamesActivityResultCodes.RESULT_RECONNECT_REQUIRED == resultCode) {
			if (fGameHelper != null && fGameHelper.getGamesClient() != null) {
				fGameHelper.signOut();
			}
			pushInvitationsToLua("", true, "logout");
		} else {
			pushInvitationsToLua("", true, "cancelled");
		}
	}

	private void pushInvitationsToLua(final String invitationId, final boolean isError, final String phase) {
		CoronaRuntimeTask task = new CoronaRuntimeTask() {
			@Override
			public void executeUsing(CoronaRuntime runtime) {
				LuaState L = runtime.getLuaState();
				
				CoronaLua.newEvent(L, "invitations");

				L.pushString("invitations");
				L.setField(-2, "type");
				
				L.newTable();

				L.pushString(invitationId);
				L.setField(-2, RoomManager.ROOM_ID);

				L.pushString(phase);
				L.setField(-2, "phase");

				L.pushBoolean(isError);
				L.setField(-2, "isError");

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