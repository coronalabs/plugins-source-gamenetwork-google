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

import com.naef.jnlua.LuaState;

import com.ansca.corona.CoronaLua;
import com.ansca.corona.CoronaRuntime;
import com.ansca.corona.CoronaRuntimeTaskDispatcher;
import com.ansca.corona.CoronaRuntimeTask;

import com.google.android.gms.games.multiplayer.Invitation;
import com.google.android.gms.games.multiplayer.OnInvitationReceivedListener;

public class InvitationReceivedListener extends Listener implements OnInvitationReceivedListener {
	public InvitationReceivedListener(CoronaRuntimeTaskDispatcher _dispatcher, int _listener) {
		super(_dispatcher, _listener);
	}

	public void onInvitationReceived(Invitation invitation) {
		if (fListener < 0) {
			return;
		}

		final Invitation finalInvitation = invitation;
		CoronaRuntimeTask task = new CoronaRuntimeTask() {
			@Override
			public void executeUsing(CoronaRuntime runtime) {
				LuaState L = runtime.getLuaState();
				CoronaLua.newEvent(L, "invitationReceived");

				L.pushString("invitationReceived");
				L.setField(-2, TYPE);
				
				L.newTable();

				L.pushString(finalInvitation.getInvitationId());
				L.setField(-2, RoomManager.ROOM_ID);

				L.pushString(finalInvitation.getInviter().getDisplayName());
				L.setField(-2, ALIAS);

				L.pushString(finalInvitation.getInviter().getPlayer().getPlayerId());
				L.setField(-2, PLAYER_ID);

				L.setField(-2, DATA);

				try {
					CoronaLua.dispatchEvent(L, fListener, 0);
				} catch(Exception ex) {
					ex.printStackTrace();
				}
				
			}
		};
		fDispatcher.send(task);
	}
}
