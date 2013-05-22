
# gameNetwork.init()

> --------------------- ------------------------------------------------------------------------------------------
> __Type__              [Function][api.type.Function]
> __Library__           [gameNetwork.*][api.library.gameNetwork]
> __Return value__      none
> __Revision__          [REVISION_LABEL](REVISION_URL)
> __Keywords__          gameNetwork, game center
> __Sample code__       */CoronaSDK/SampleCode/GameNetwork/GameCenter*, */CoronaSDK/SampleCode/GameNetwork/CoronaCloud*
> __See also__          [gameNetwork.request()][plugin.gameNetwork-google.request]<br/>[gameNetwork.show()][plugin.gameNetwork-google.show]
> --------------------- ------------------------------------------------------------------------------------------


## Overview

Initializes an app required by the game network provider.

**NOTE:** Using the gameNetwork API will enable Corona Launchpad regardless of the setting in config.lua.

## Syntax

	gameNetwork.init( providerName [, ...] )
	
##### providerName ~^(required)^~
_[String][api.type.String]._ Name of the gameNetwork provider to initialize. The following string is valid:

* `"google"`: Only available on Android.

## Providers

### Google Play game services

Google Play game services is currently only avaliable on Android.

	gameNetwork.init( "google" [, initCallback] )

##### initCallback ~^(optional)^~
_[Listener][api.type.Listener]._ If "google" is specified as `providerName`, this is a callback function. On successful login, `event.data` will be 'true'. On unsuccessful init, `event.data` will be 'false'. When problems such as network errors occur, `event.errorCode` (integer) and `event.errorMessage` (string) will be defined. 

`````lua
local gameNetwork = require "gameNetwork"

local function initCallback( event )
	if event.data then
		loggedIntoGC = true
        native.showAlert( "Success!", "", { "OK" } )
	end
end

gameNetwork.init( "google", initCallback )
`````