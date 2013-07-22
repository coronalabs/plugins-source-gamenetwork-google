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
import java.util.ArrayList;
import java.util.HashSet;

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

import com.google.android.gms.games.leaderboard.LeaderboardVariant;
import com.google.android.gms.games.multiplayer.realtime.Room;
import com.google.android.gms.games.multiplayer.realtime.RoomConfig;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.ConnectionResult;

public class LuaLoader implements JavaFunction {

	private int fListener;

	private CoronaRuntimeTaskDispatcher fDispatcher;

	private GameHelper helper;

	// This corresponds to the event name, e.g. [Lua] event.name
	private static final String EVENT_NAME = "gameNetwork";

	/**
	 * Creates a new object for displaying banner ads on the CoronaActivity
	 */
	public LuaLoader() {
		CoronaActivity activity = CoronaEnvironment.getCoronaActivity();
		// Validate.
		if (activity == null) {
			throw new IllegalArgumentException("Activity cannot be null.");
		}
		
		// Initialize member variables.
		fListener = CoronaLua.REFNIL;
	}

	/**
	 * Warning! This method is not called on the main UI thread.
	 */
	@Override
	public int invoke(LuaState L) {
		fDispatcher = new CoronaRuntimeTaskDispatcher( L );

		// Add functions to library
		NamedJavaFunction[] luaFunctions = new NamedJavaFunction[] {
			new InitWrapper(),
			new ShowWrapper(),
			new RequestWrapper(),
		};

		String libName = L.toString( 1 );
		L.register(libName, luaFunctions);

		return 1;
	}

	// library.init( listener )
	public int init(LuaState L) {
		int listener = -1;
		
		int top = L.getTop();

		if (CoronaLua.isListener( L, -1, "" )) {
			listener = CoronaLua.newRef( L, -1 );
		}

		L.setTop(top);

		if (listener > 0) {
			final int finalListener = listener;

			CoronaRuntimeTask task = new CoronaRuntimeTask() {
				@Override
				public void executeUsing(CoronaRuntime runtime) {
					try {

						CoronaActivity activity = CoronaEnvironment.getCoronaActivity();

						int result = GooglePlayServicesUtil.isGooglePlayServicesAvailable(activity);

						boolean isError = false;
						String errorMessage = "";

						//TODO: modifiy this to integrate with GameHelper
						if (result == ConnectionResult.SERVICE_MISSING) {
							isError = true;
							errorMessage = "Service Missing";
						} else if (result == ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED) {
							isError = true;
							errorMessage = "Service Version Update Required";
						} else if (result == ConnectionResult.SERVICE_DISABLED) {
							isError = true;
							errorMessage = "Service Disabled";
						} else if (result == ConnectionResult.SERVICE_INVALID) {
							isError = true;
							errorMessage = "Service Invalid";
						}

						LuaState L = runtime.getLuaState();
						CoronaLua.newEvent( L, "init" );

						L.pushString( "init" );
						L.setField( -2, Listener.TYPE);

						L.pushBoolean( !isError );
						L.setField( -2, Listener.DATA);

						if ( isError ) {
							L.pushBoolean( isError );
							L.setField( -2, "isError" );

							L.pushString( errorMessage );
							L.setField( -2, "errorMessage" );

							L.pushNumber( result );
							L.setField( -2, "errorCode" );
						}

						if (finalListener > 0) {
							CoronaLua.dispatchEvent( L, finalListener, 0);
						}
						
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
			};

			fDispatcher.send(task);

		}		
		
		return 0;
	}

	public int login(int _listener, boolean userInitiated) {
		CoronaActivity activity = CoronaEnvironment.getCoronaActivity();
		SignInListener signInListener = new SignInListener(fDispatcher, _listener);
		if (isConnected() && activity != null) {
			signInListener.onSignInSucceeded();
			return 0;
		}

		helper = new GameHelper(activity);
		helper.setup(signInListener, GameHelper.CLIENT_GAMES | GameHelper.CLIENT_PLUS);

		if (!userInitiated) {
			helper.getGamesClient().connect();
			helper.getPlusClient().connect();
			return 0;
		}

		int requestCode = activity.registerActivityResultHandler(new CoronaActivity.OnActivityResultHandler() {
			@Override
			public void onHandleActivityResult(CoronaActivity activity, int requestCode, int resultCode, android.content.Intent data) {
				helper.onActivityResult(requestCode, resultCode, data);
			}
		});

		helper.setRequestCode(requestCode);

		if (!helper.getGamesClient().isConnected()) {
			final GameHelper finalHelper = helper;
			activity.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					finalHelper.beginUserInitiatedSignIn();
				}
			});
		}
		return 0;
	}

	public int logout(int _listener) {
		if (helper != null) {
			helper.signOut();
		}

		return 0;
	}

	public int show(LuaState L) {
		int listener = -1;
		int index = -1;
		String leaderBoardId = "";

		int top = L.getTop();

		if (L.isTable(index)) {
			L.getField(-1, "listener");
			if (CoronaLua.isListener( L, -1, "googlePlayGames")) {
				listener = CoronaLua.newRef( L, -1 );
			}
			index--;
		}

		L.setTop(top);

		String whatToShow = "";
		whatToShow = L.toString(index);
		CoronaActivity activity = CoronaEnvironment.getCoronaActivity();

		boolean checks = isConnected() && activity != null;

		if (isConnected() && activity != null) {
			final GameHelper finalHelper = helper;
			if (whatToShow.equals("achievements")) {
				activity.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						if (CoronaEnvironment.getCoronaActivity() != null) {
							CoronaEnvironment.getCoronaActivity().startActivityForResult(finalHelper.getGamesClient().getAchievementsIntent(), 123);
						}
					}
				});
			} else if (whatToShow.equals("leaderboards")) {
				activity.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						if (CoronaEnvironment.getCoronaActivity() != null) {
							CoronaEnvironment.getCoronaActivity().startActivityForResult(finalHelper.getGamesClient().getAllLeaderboardsIntent(), 123);
						}
						
					}
				});
			} else if (whatToShow.equals("selectPlayers")) {
				int min = -1;
				int max = -1;
					
				if (L.isTable(-1)) {
					L.getField(-1, "minPlayers");
					if(L.isNumber(-1)) {
						min = (int)L.toNumber(-1);
						L.pop(1);
					}

					L.getField(-1, "maxPlayers");
					if(L.isNumber(-1)) {
						max = (int)L.toNumber(-1);
						L.pop(1);
					}
				}

				if (min > -1 && max > -1) {
					final int finalRequestCode = activity.registerActivityResultHandler(new SelectPlayersResultHandler(fDispatcher, listener));
					final int finalMin = min;
					final int finalMax = max;
					activity.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							if (CoronaEnvironment.getCoronaActivity() != null) {
								CoronaEnvironment.getCoronaActivity().startActivityForResult(finalHelper.getGamesClient().getSelectPlayersIntent(finalMin, finalMax), finalRequestCode);
							}
						}
					});	
				}

			} else if (whatToShow.equals("waitingRoom")) {
				String roomId = null;
				int minToStart = 0;
				if (L.isTable(-1)) {
					L.getField(-1, RoomManager.ROOM_ID);
					roomId = L.toString(-1);

					L.pop(1);

					L.getField(-1, "minPlayers");
					if (L.isNumber(-1)) {
						minToStart = (int)L.toNumber(-1);
					}

				}

				if (roomId != null) {
					final int finalRequestCode = activity.registerActivityResultHandler(new WaitingRoomResultHandler(fDispatcher, listener, finalHelper.getGamesClient()));
					final Room room = RoomManager.getRoom(roomId);
					final int finalMinToStart = minToStart;
					activity.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							if (CoronaEnvironment.getCoronaActivity() != null && room != null) {
								CoronaEnvironment.getCoronaActivity().startActivityForResult(finalHelper.getGamesClient().getRealTimeWaitingRoomIntent(room, finalMinToStart), finalRequestCode);
							}
						}
					});
				}

			} else if (whatToShow.equals("invitations")) {
				final int finalRequestCode = activity.registerActivityResultHandler(new InvitationResultHandler(fDispatcher, listener));
				activity.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						if (CoronaEnvironment.getCoronaActivity() != null) {
							CoronaEnvironment.getCoronaActivity().startActivityForResult(finalHelper.getGamesClient().getInvitationInboxIntent(), finalRequestCode);
						}
					}
				});
			}
		}
		
		return 0;
	}

	public int request(LuaState L) {
		int returnValues = 0;
		int index = -1;
		int listener = -1;

		if (L.isTable( index )) {
			L.getField(-1, "listener");
			if (CoronaLua.isListener( L, -1, "" )) {
				listener = CoronaLua.newRef( L, -1 );
			}
			L.pop(1);

			index--;
		}

		String requestedAction = "";
		requestedAction = L.toString(index);

		if (requestedAction.equals("unlockAchievement")) {
			String achievement = "";
			int top = L.getTop();
			if (L.isTable(-1)) {
				L.getField(-1, "achievement");
				if (L.isTable(-1)) {
					L.getField(-1, "identifier");
					if (L.isString(-1)) {
						achievement = L.toString(-1);
					}
				}
			}
			L.setTop(top);
			if (isConnected()) {
				helper.getGamesClient().unlockAchievementImmediate(new UnlockAchievementListener(fDispatcher, listener), achievement);
			}
			
		} else if (requestedAction.equals("setHighScore")) {
			long score = 0;
			int top = L.getTop();
			String leaderBoardId = "";
			if (L.isTable(-1)) {
				L.getField(-1, "localPlayerScore");
				if (L.isTable(-1)) {
					L.getField(-1, "category");
					if (L.isString(-1)) {
						leaderBoardId = L.toString(-1);
					}
					L.getField(-2, "value");
					if (L.isNumber(-1)) {
						score = (long)L.toNumber(-1);
					}
				}
			}
			L.setTop(top);
			if (isConnected()) {
				helper.getGamesClient().submitScoreImmediate(new SetHighScoreListener(fDispatcher, listener), leaderBoardId, score);
			}
			
		} else if (requestedAction.equals("isConnected")) {
			returnValues++;
			L.pushBoolean(isConnected());			
		} else if (requestedAction.equals("login")) {
			boolean userInitiated = true;
			if (L.isTable(-1)) {
				L.getField(-1, "userInitiated");
				if (L.isBoolean(-1)) {
					userInitiated = L.toBoolean(-1);
				}
			}
			login(listener, userInitiated);
		} else if (requestedAction.equals("logout")) {
			logout(listener);
		} else if (requestedAction.equals("loadPlayers")) {
			HashSet<String> nameSet = new HashSet<String>();
			
			int top = L.getTop();
			if (L.isTable(-1)) {
				L.getField(-1, "playerIDs");
				if (L.isTable(-1)) {
					//TODO: iterate as table
					int arrayLength = L.length(-1);
					if (arrayLength>0) {
						for (int i = 1; i <= arrayLength; i++) {
							L.rawGet(-1, i);
							nameSet.add(L.toString(-1));
							L.pop(1);
						}
					}
				}
			}
			L.setTop(top);

			if (isConnected()) {
				PlayerLoaderManager playerLoader = new PlayerLoaderManager(fDispatcher, listener, helper.getGamesClient(), "loadPlayers");
				playerLoader.loadPlayers(nameSet, false);
			}

		} else if (requestedAction.equals("loadLocalPlayer")) {
			if (isConnected()) {
				HashSet<String> nameSet = new HashSet<String>();
				nameSet.add(helper.getGamesClient().getCurrentPlayerId());
				PlayerLoaderManager playerLoader = new PlayerLoaderManager(fDispatcher, listener, helper.getGamesClient(), "loadPlayers");
				playerLoader.loadPlayers(nameSet, true);
			}
		} else if (requestedAction.equals("loadScores")) {
			String leaderBoardId = "";
			String playerScope = "Global";
			String timeScope = "AllTime";
			int numToGet = 25;
			boolean playerCentered = false;

			int top = L.getTop();
			if (L.isTable(-1)) {
				L.getField(-1, "leaderboard");
				if (L.isTable(-1)) {
					L.getField(-1, "category");
					if (L.isString(-1)) {
						leaderBoardId = L.toString(-1);
					}
					L.pop(1);

					L.getField(-1, "playerScope");
					if (L.isString(-1)) {
						playerScope = L.toString(-1);
					}
					L.pop(1);

					L.getField(-1, "timeScope");
					if (L.isString(-1)) {
						timeScope = L.toString(-1);
					}
					L.pop(1);

					L.getField(-1, "playerCentered");
					if (L.isBoolean(-1)) {
						playerCentered = L.toBoolean(-1);
					}
					L.pop(1);

					L.getField(-1, "range");
					if (L.isTable(-1)) {
						L.rawGet(-1, 2);
						if (L.isNumber(-1)) {
							Double d = Double.valueOf(L.toNumber(-1));
							numToGet = d.intValue();
							if (numToGet>25) {
								numToGet = 25;
							}
						}
					}
 				}
 				
 				if (!leaderBoardId.equals("")) {
 					int span;
 					int leaderboardCollection;

					if (timeScope.equals("Week")) {
						span = LeaderboardVariant.TIME_SPAN_WEEKLY;
 					} else if (timeScope.equals("Today")) {
 						span = LeaderboardVariant.TIME_SPAN_DAILY;
 					} else {
 						span = LeaderboardVariant.TIME_SPAN_ALL_TIME;
 					}

 					if (playerScope.equals("FriendsOnly")) {
 						leaderboardCollection = LeaderboardVariant.COLLECTION_SOCIAL;
 					} else {
 						leaderboardCollection = LeaderboardVariant.COLLECTION_PUBLIC;
 					}
					
					if (isConnected()) {
						if (playerCentered) {
	 						helper.getGamesClient().loadPlayerCenteredScores(new LoadTopScoresListener(fDispatcher, listener, leaderBoardId), leaderBoardId, span, leaderboardCollection, numToGet, true);
	 					} else {
	 						helper.getGamesClient().loadTopScores(new LoadTopScoresListener(fDispatcher, listener, leaderBoardId), leaderBoardId, span, leaderboardCollection, numToGet, true);	
	 					}	
					}
 				}

			}
			L.setTop(top);

		} else if (requestedAction.equals("loadAchievements") || requestedAction.equals("loadAchievementDescriptions")) {
			if (isConnected()) {
				helper.getGamesClient().loadAchievements(new LoadAchievementsListener(fDispatcher, listener));
			}
		} else if (requestedAction.equals("loadLeaderboardCategories")) {
			if (isConnected()) {
				helper.getGamesClient().loadLeaderboardMetadata(new LoadLeaderboardCategoriesListener(fDispatcher, listener));
			}
		} else if (requestedAction.equals("createRoom")) {
			if (isConnected()) {
				ArrayList<String> playerIds = new ArrayList<String>();
				int maxAutoMatchPlayers = 0;
				int minAutoMatchPlayers = 0;

				int top = L.getTop();
				if (L.isTable(-1)) {
					L.getField(-1, "playerIDs");
					if (L.isTable(-1)) {
						//TODO: iterate as table
						int arrayLength = L.length(-1);
						if (arrayLength>0) {
							for (int i = 1; i <= arrayLength; i++) {
								L.rawGet(-1, i);
								playerIds.add(L.toString(-1));
								L.pop(1);
							}
						}
					}
					L.pop(1);

					L.getField(-1, "maxAutoMatchPlayers");
					maxAutoMatchPlayers = (int)L.toNumber(-1);
					L.pop(1);

					L.getField(-1, "minAutoMatchPlayers");
					minAutoMatchPlayers = (int)L.toNumber(-1);
				}
				L.setTop(top);			

				RoomManager roomManager = RoomManager.getRoomManager(fDispatcher, listener, helper.getGamesClient());
				MessageManager messageManager = MessageManager.getMessageManager(fDispatcher, listener, helper.getGamesClient());
				RoomConfig.Builder roomConfig = RoomConfig.builder(roomManager);
				if (playerIds.size() > 0) {
					roomConfig.addPlayersToInvite(playerIds);
				}

				if (minAutoMatchPlayers > 0 || maxAutoMatchPlayers > 0) {
					roomConfig.setAutoMatchCriteria(RoomConfig.createAutoMatchCriteria(minAutoMatchPlayers, maxAutoMatchPlayers, 0));
				}

				roomConfig.setMessageReceivedListener(messageManager);
				roomConfig.setRoomStatusUpdateListener(roomManager);
				roomConfig.setSocketCommunicationEnabled(false);
				helper.getGamesClient().createRoom(roomConfig.build());
			}
		} else if (requestedAction.equals("joinRoom")) {
			if (isConnected()) {
				String roomId = null;
				int top = L.getTop();
				if (L.isTable(-1)) {
					L.getField(-1, RoomManager.ROOM_ID);
					if (L.isString(-1)) {
						roomId = L.toString(-1);
					}
				}
				L.setTop(top);

				if (roomId != null) {
					RoomManager roomManager = RoomManager.getRoomManager(fDispatcher, listener, helper.getGamesClient());
					MessageManager messageManager = MessageManager.getMessageManager(fDispatcher, listener, helper.getGamesClient());
					RoomConfig.Builder roomConfig = RoomConfig.builder(roomManager);
					roomConfig.setInvitationIdToAccept(roomId);
					roomConfig.setSocketCommunicationEnabled(false);
					roomConfig.setMessageReceivedListener(messageManager);
					roomConfig.setRoomStatusUpdateListener(roomManager);
					helper.getGamesClient().joinRoom(roomConfig.build());
				}
			}
		} else if (requestedAction.equals("sendMessage")) {
			if (isConnected()) {
				ArrayList<String> playerIds = new ArrayList<String>();
				String message = null;
				String roomId = null;
				boolean isReliable = true;
				int top = L.getTop();
				if (L.isTable(-1)) {
					L.getField(-1, "playerIDs");
					if (L.isTable(-1)) {
						//TODO: iterate as table
						int arrayLength = L.length(-1);
						if (arrayLength>0) {
							for (int i = 1; i <= arrayLength; i++) {
								L.rawGet(-1, i);
								playerIds.add(L.toString(-1));
								L.pop(1);
							}
						}
					}
					L.pop(1);

					L.getField(-1, RoomManager.ROOM_ID);
					if (L.isString(-1)) {
						roomId = L.toString(-1);
					}
					L.pop(1);

					L.getField(-1, "message");
					if (L.isString(-1)) {
						message = L.toString(-1);
					}
					L.pop(1);

					L.getField(-1, "reliable");
					if(L.isBoolean(-1)) {
						isReliable = L.toBoolean(-1);
					}
					L.pop(1);
				}

				L.setTop(top);
				if (playerIds != null && message != null && roomId != null) {
					MessageManager messageManager = MessageManager.getMessageManager(fDispatcher, listener, helper.getGamesClient());
					messageManager.sendMessage(playerIds, message, roomId, isReliable);
				}
			}
		} else if (requestedAction.equals("leaveRoom")) {
			if (isConnected()) {
				String roomId = null;
				int top = L.getTop();
				if (L.isTable(-1)) {
					L.getField(-1, RoomManager.ROOM_ID);
					if (L.isString(-1)) {
						roomId = L.toString(-1);
					}
				}
				L.setTop(top);

				if (roomId != null) {
					RoomManager roomManager = RoomManager.getRoomManager(fDispatcher, listener, helper.getGamesClient());
					helper.getGamesClient().leaveRoom(roomManager, roomId);
				}
			}
		} else if (requestedAction.equals("setMessageReceivedListener")) {
			MessageManager.setMessageListener(listener);
		} else if (requestedAction.equals("setRoomListener")) {
			RoomManager.setRoomListener(listener);
		} else if (requestedAction.equals("setInvitationReceivedListener")) {
			if (isConnected()) {
				helper.getGamesClient().registerInvitationListener(new InvitationReceivedListener(fDispatcher, listener));
			}
		} else if (requestedAction.equals("loadFriends")) {
			(new LoadInvitablePlayersManager(fDispatcher, listener, helper.getGamesClient())).load();
		}

		return returnValues;
	}

	private boolean isConnected() {
		return helper != null && helper.getGamesClient().isConnected() && helper.getPlusClient().isConnected();
	}

	private class InitWrapper implements NamedJavaFunction {
		@Override
		public String getName() {
			return "init";
		}
		
		/**
		 * Warning! This method is not called on the main UI thread.
		 */
		@Override
		public int invoke(LuaState L) {
			return init(L);
		}
	}

	private class ShowWrapper implements NamedJavaFunction {
		@Override
		public String getName() {
			return "show";
		}

		@Override
		public int invoke(LuaState L) {
			return show(L);
		}
	}

	private class RequestWrapper implements NamedJavaFunction {
		@Override
		public String getName() {
			return "request";
		}

		@Override
		public int invoke(LuaState L) {
			return request(L);
		}
	}
}