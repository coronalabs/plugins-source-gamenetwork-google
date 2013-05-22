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

import com.naef.jnlua.LuaState;

import com.ansca.corona.CoronaLua;
import com.ansca.corona.CoronaRuntime;
import com.ansca.corona.CoronaRuntimeTaskDispatcher;
import com.ansca.corona.CoronaRuntimeTask;

public class SignInListener extends Listener implements GameHelper.GameHelperListener {
	public SignInListener(CoronaRuntimeTaskDispatcher _dispatcher, int _listener) {
		super(_dispatcher, _listener);
	}

	public void onSignInFailed() {
		callBackListener(true);
	}

	public void onSignInSucceeded() {
		callBackListener(false);
	}

	private void callBackListener(final boolean isError) {
		if (fListener < 0) {
			return;
		}
		CoronaRuntimeTask task = new CoronaRuntimeTask() {
			@Override
			public void executeUsing(CoronaRuntime runtime) {
				try {
					LuaState L = runtime.getLuaState();
					CoronaLua.newEvent( L, "login" );

					L.pushString( "login" );
					L.setField( -2, "type");

					if ( isError ) {
						L.pushBoolean( isError );
						L.setField( -2, "isError" );	
					}

					CoronaLua.dispatchEvent( L, fListener, 0);
					CoronaLua.deleteRef(L, fListener);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		};

		fDispatcher.send(task);
	}
}