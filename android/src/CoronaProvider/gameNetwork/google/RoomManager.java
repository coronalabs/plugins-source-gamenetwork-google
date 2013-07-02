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

import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import android.util.Log;

import com.naef.jnlua.LuaState;

import com.ansca.corona.CoronaLua;
import com.ansca.corona.CoronaRuntime;
import com.ansca.corona.CoronaRuntimeTaskDispatcher;
import com.ansca.corona.CoronaRuntimeTask;

import com.google.android.gms.games.multiplayer.realtime.RoomUpdateListener;
import com.google.android.gms.games.multiplayer.realtime.RoomStatusUpdateListener;
import com.google.android.gms.games.multiplayer.realtime.RealTimeReliableMessageSentListener;
import com.google.android.gms.games.multiplayer.realtime.RealTimeMessageReceivedListener;
import com.google.android.gms.games.multiplayer.realtime.Room;
import com.google.android.gms.games.multiplayer.realtime.RealTimeMessage;
import com.google.android.gms.games.GamesClient;

public class RoomManager implements RoomUpdateListener, RoomStatusUpdateListener {
	public final static String ROOM_ID = "roomID";

	static int fRoomListener;
	static HashMap<String, Room> fRooms;
	static GamesClient fGamesClient;
	static CoronaRuntimeTaskDispatcher fDispatcher;

	static RoomManager fRoomManager;

	static { 
		fRooms = new HashMap<String, Room>(); 
	}
	
	public static Room getRoom(String roomId) {
		return fRooms.get(roomId);
	}

	//Made this a singleton so that we only send 1 set of room events back to the lua side
	public static RoomManager getRoomManager(CoronaRuntimeTaskDispatcher _dispatcher, int _listener, GamesClient _gamesClient) {
		if (fRoomManager == null) {
			fRoomManager = new RoomManager(_dispatcher, _listener, _gamesClient);
		}
		fDispatcher = _dispatcher;
		RoomManager.setRoomListener(_listener);
		return fRoomManager;
	}

	private RoomManager(CoronaRuntimeTaskDispatcher _dispatcher, int _listener, GamesClient _gamesClient) {
		fGamesClient = _gamesClient;
		fRoomListener = _listener;
	}

	@Override
	public void onJoinedRoom(int statusCode, Room room) {
		boolean isError = false;
		if (room == null) {
			isError = true;
		} else {
			fRooms.put(room.getRoomId(), room);
		}
		
		pushToLua("joinRoom", room, null, isError);
	}

	@Override
	public void onLeftRoom(int statusCode, String roomId) {
		final String finalRoomId = roomId;
		CoronaRuntimeTask task = new CoronaRuntimeTask() {
			@Override
			public void executeUsing(CoronaRuntime runtime) {
				LuaState L = runtime.getLuaState();
				
				CoronaLua.newEvent(L, "leaveRoom");

				L.pushString("leaveRoom");
				L.setField(-2, Listener.TYPE);
				
				L.newTable();

				L.pushString(finalRoomId);
				L.setField(-2, ROOM_ID);

				L.setField(-2, Listener.DATA);

				try {
					CoronaLua.dispatchEvent(L, fRoomListener, 0);
				} catch(Exception ex) {
					ex.printStackTrace();
				}
				
			}
		};
		fDispatcher.send(task);
	}

	//This is called when all the players are connected and the game can begin
	@Override
	public void onRoomConnected(int statusCode, Room room) {
		ArrayList<String> participantIds = room.getParticipantIds();
		pushToLua("connectedRoom", room, participantIds, false);
	}

	@Override
	public void onRoomCreated(int statusCode, Room room) {
		boolean isError = false;
		if (room == null) {
			isError = true;
		} else {
			fRooms.put(room.getRoomId(), room);
		}
		pushToLua("createRoom", room, null, isError);
	}

	@Override
	public void onConnectedToRoom(Room room) {

	}

	@Override
	public void onDisconnectedFromRoom(Room room) {
		
	}

	@Override
	public void onPeerDeclined(Room room, List<String> participantIds) {
		pushToLua("peerDeclinedInvitation", room, participantIds, false);
	}

	@Override
	public void onPeerInvitedToRoom(Room room, List<String> participantIds) {
		
	}

	@Override
	public void onPeerJoined(Room room, List<String> participantIds) {
		pushToLua("peerAcceptedInvitation", room, participantIds, false);
	}

	@Override
	public void onPeerLeft(Room room, List<String> participantIds) {
		pushToLua("peerLeftRoom", room, participantIds, false);
	}

	@Override
	public void onPeersConnected(Room room, List<String> participantIds) {
		
	}

	@Override
	public void onPeersDisconnected(Room room, List<String> participantIds) {
		pushToLua("peerDisconnectedFromRoom", room, participantIds, false);
	}

	@Override
	public void onRoomAutoMatching(Room room) {
		
	}

	@Override
	public void onRoomConnecting(Room room) {
		
	}

	private void pushToLua(final String type, final Room room, final List<String> participantIds, final boolean isError) {
		CoronaRuntimeTask task = new CoronaRuntimeTask() {
			@Override
			public void executeUsing(CoronaRuntime runtime) {
				LuaState L = runtime.getLuaState();
				
				CoronaLua.newEvent(L, type);

				L.pushString(type);
				L.setField(-2, Listener.TYPE);
				
				L.newTable();

				int count = 1;

				if (participantIds != null) {
					ListIterator<String> iter = participantIds.listIterator();
					while(iter.hasNext()) {
						String participantId = iter.next();
						if(room != null && participantId != room.getParticipantId(fGamesClient.getCurrentPlayerId())) {
							L.pushString(participantId);
							L.rawSet(-2, count);
							count++;
						}
					}
				}

				if (room != null) {
					L.pushString(room.getRoomId());
					L.setField(-2, ROOM_ID);
				}

				L.pushBoolean(isError);
				L.setField(-2, "isError");

				L.setField(-2, Listener.DATA);

				try {
					CoronaLua.dispatchEvent(L, fRoomListener, 0);
				} catch(Exception ex) {
					ex.printStackTrace();
				}
			}
		};
		fDispatcher.send(task);
	}

	public static void setRoomListener(int listener) {
		fRoomListener = listener;
	}
}
