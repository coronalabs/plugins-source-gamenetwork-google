//
//  LuaLoader.java
//  TemplateApp
//
//  Copyright (c) 2012 __MyCompanyName__. All rights reserved.
//

// This corresponds to the name of the Lua library,
// e.g. [Lua] require "plugin.library"


//TODO map participantid to playerid
package CoronaProvider.gameNetwork.google;

import CoronaProvider.gameNetwork.google.Listener;

import java.util.ArrayList;

import android.util.Log;

import com.naef.jnlua.LuaState;

import com.ansca.corona.CoronaLua;
import com.ansca.corona.CoronaRuntime;
import com.ansca.corona.CoronaRuntimeTaskDispatcher;
import com.ansca.corona.CoronaRuntimeTask;

import com.google.android.gms.games.multiplayer.realtime.RealTimeReliableMessageSentListener;
import com.google.android.gms.games.multiplayer.realtime.RealTimeMessageReceivedListener;
import com.google.android.gms.games.multiplayer.realtime.RealTimeMessage;
import com.google.android.gms.games.GamesClient;

public class MessageManager implements RealTimeMessageReceivedListener {
	static int fMessageListener;
	static GamesClient fGamesClient;
	static MessageManager fMessageManager;
	static CoronaRuntimeTaskDispatcher fDispatcher;

	//Made this a singleton so that we only send one message back to the lua side.
	public static MessageManager getMessageManager(CoronaRuntimeTaskDispatcher _dispatcher, int _listener, GamesClient _gamesClient) {
		if (fMessageManager == null) {
			fMessageManager = new MessageManager(_dispatcher, _listener, _gamesClient);
		}
		MessageManager.setDispatcher(_dispatcher);

		//Basic check to see if this is a valid listener memory address
		if (_listener > 0) {
			MessageManager.setMessageListener(_listener);
		}
		
		return fMessageManager;
	}

	private MessageManager(CoronaRuntimeTaskDispatcher _dispatcher, int _listener, GamesClient _gamesClient) {
		fGamesClient = _gamesClient;
	}

	public void sendMessage(ArrayList<String> playerIds, String message, String roomId, boolean isReliable) {
		if (isReliable) {
			for(String playerId : playerIds) {
				fGamesClient.sendReliableRealTimeMessage(new RealTimeReliableMessageSentListener() {
					@Override
					public void onRealTimeMessageSent(int statusCode, int tokenId, String recipientId) {
					}
				}, message.getBytes(), roomId, playerId);
			}
		} else {
			fGamesClient.sendUnreliableRealTimeMessage(message.getBytes(), roomId, playerIds);
		}
	}

	public void onRealTimeMessageReceived(RealTimeMessage message) {
		final RealTimeMessage finalMessage = message;
		CoronaRuntimeTask task = new CoronaRuntimeTask() {
			@Override
			public void executeUsing(CoronaRuntime runtime) {
				LuaState L = runtime.getLuaState();
				
				CoronaLua.newEvent(L, "messageReceived");

				L.pushString("messageReceived");
				L.setField(-2, Listener.TYPE);
				
				L.newTable();
		
				L.pushString(new String(finalMessage.getMessageData()));
				L.setField(-2, "message");

				L.pushString(finalMessage.getSenderParticipantId());
				L.setField(-2, "participantId");
		
				L.setField(-2, Listener.DATA);

				try {
					CoronaLua.dispatchEvent(L, fMessageListener, 0);
					// CoronaLua.deleteRef(L, fListener);
				} catch(Exception ex) {
					ex.printStackTrace();
				}
				
			}
		};
		fDispatcher.send(task);
	}

	private static void setDispatcher(CoronaRuntimeTaskDispatcher _dispatcher) {
		fDispatcher = _dispatcher;
	}

	public static void setMessageListener(int listener) {
		fMessageListener = listener;
	}
}
