//
//  LuaLoader.java
//  TemplateApp
//
//  Copyright (c) 2012 __MyCompanyName__. All rights reserved.
//

// This corresponds to the name of the Lua library,
// e.g. [Lua] require "plugin.library"
package CoronaProvider.gameNetwork.google;

import android.util.Log;

import com.naef.jnlua.LuaState;

import com.ansca.corona.CoronaRuntimeTaskDispatcher;

public abstract class Listener {
	public final static String DATA = "data";
	public final static String TYPE = "type";
	public final static String PLAYER_ID = "playerID";
	public final static String ALIAS = "alias";

	protected CoronaRuntimeTaskDispatcher fDispatcher;
	protected int fListener;

	public Listener(CoronaRuntimeTaskDispatcher _dispatcher, int _listener) {
		fListener = _listener;
		fDispatcher = _dispatcher;
	}
}