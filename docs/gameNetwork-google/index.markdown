
# gameNetwork.*

> --------------------- ------------------------------------------------------------------------------------------
> __Type__              [Library][api.type.Library]
> __Revision__          [REVISION_LABEL](REVISION_URL)
> __Keywords__          gameNetwork, game center, game services
> __Sample code__       
> __See also__          
> __Availability__      Starter, Basic, Pro, Enterprise
> --------------------- ------------------------------------------------------------------------------------------

## Overview

Corona's game network API allows access to social gaming features such as public leaderboards and achievements.

**NOTE:** Using the gameNetwork API will enable Corona Launchpad regardless of the setting in config.lua.

## Platforms

The following platforms are supported:

* Android

#### Google Play game services

This is currently a feature that is only supported on Android. It is not supported by the Corona Simulator.

## Functions

#### [gameNetwork.init()][plugin.gameNetwork-google.init]

#### [gameNetwork.request()][plugin.gameNetwork-google.request]

#### [gameNetwork.show()][plugin.gameNetwork-google.show]

## build.settings

The following build.settings section is required to for Google Play game services

``````lua
settings =
{
	android =
	{
		googlePlayGamesAppId = "123456789012", -- Your Google Play Games App Id
	},
	plugins =
	{
		-- key is the name passed to Lua's 'require()'
		["CoronaProvider.gameNetwork.google"] =
		{
			-- required
			publisherId = "com.coronalabs",
			supportedPlatforms = { android = true }
		},
	},
}
``````

## Sample Code

You can access sample code for leaderboards and achievements [here](https://github.com/coronalabs/plugins-sample-gameNetwork-google).
You can access sample code for real time multiplayer [here](https://github.com/coronalabs/plugins-sample-gameNetwork-google-gemwars).
