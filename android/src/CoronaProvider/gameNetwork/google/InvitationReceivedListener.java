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

	public void onInvitationRemoved(String invitationId) {
		
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
				
				Listener.pushInvitationToLua(L, finalInvitation);

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
