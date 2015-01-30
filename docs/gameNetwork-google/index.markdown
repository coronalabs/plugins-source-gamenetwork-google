# Google Play Game Services

> --------------------- ------------------------------------------------------------------------------------------
> __Type__              [Library][api.type.Library]
> __Revision__          [REVISION_LABEL](REVISION_URL)
> __Keywords__          gameNetwork, Google Play Game Services
> __Availability__      Starter, Basic, Pro, Enterprise
> __Platforms__			Android
> --------------------- ------------------------------------------------------------------------------------------

## Overview

[Google Play Game Services](http://developer.android.com/google/play-services/games.html) makes your games more social with capabilities for achievements, leaderboards, and other popular features using the Google Play game services SDK. Let players sign in using their Google+ identities and share their gaming experience with friends.


## Syntax

	local gameNetwork = require( "gameNetwork" )


## Functions

#### [gameNetwork.init()][plugin.gameNetwork-google.init]

#### [gameNetwork.request()][plugin.gameNetwork-google.request]

#### [gameNetwork.show()][plugin.gameNetwork-google.show]


## Project Settings

To use this plugin, add an entry into the `plugins` table of `build.settings`. When added, the build server will integrate the plugin during the build phase.

``````lua
settings =
{
	plugins =
	{
		["CoronaProvider.gameNetwork.google"] =
		{
			publisherId = "com.coronalabs",
			supportedPlatforms = { android=true },
		},
	},
}
``````

In addition, you must specify the Google Play Games App ID within the `android` table of `build.settings`:

``````lua
settings =
{
	android =
	{
		googlePlayGamesAppId = "123456789012",
	},
}
``````


## Sample Code

* Leaderboards and achievements &mdash; [https://github.com/coronalabs/plugins-sample-gameNetwork-google/](https://github.com/coronalabs/plugins-sample-gameNetwork-google)

* Real-time multiplayer &mdash; [https://github.com/coronalabs/plugins-sample-gameNetwork-google-gemwars/](https://github.com/coronalabs/plugins-sample-gameNetwork-google-gemwars)


## Support

* [http://developer.android.com/google/play-services/games.html](http://developer.android.com/google/play-services/games.html)
* [Corona Forums](http://forums.coronalabs.com/forum/621-game-networking/)
