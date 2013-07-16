
# gameNetwork.show()

> --------------------- ------------------------------------------------------------------------------------------
> __Type__              [Function][api.type.Function]
> __Library__           [gameNetwork.*][api.library.gameNetwork]
> __Revision__          [REVISION_LABEL](REVISION_URL)
> __Keywords__          gameNetwork, show, leaderboards, achievements
> __Sample code__       
> __See also__          [gameNetwork.init()][plugin.gameNetwork-google.init]<br/>[gameNetwork.request()][plugin.gameNetwork-google.request]
> --------------------- ------------------------------------------------------------------------------------------


## Overview

Displays the requested game network information to the user.

## Syntax

	gameNetwork.show( name [, data ] )

##### name ~^(required)^~
_[String][api.type.String]._ Supports the following strings:

**Google Play game services:**

* `"leaderboards"`
* `"achievements"`
* `"selectPlayers"`
* `"waitingRoom"`
* `"invitations"`

##### params ~^(optional)^~
The following parameters are supported for the corresponding game networks:

#### Google Play game services:

**leaderboards:** View the leaderboard screen.  From this screen you can navigate to all the different types of leaderboards.  This function does not take a callback

Example "leaderboards" request:

`````lua
-- Display the leaderboard.
gameNetwork.show( "leaderboards" )

`````

**achievements:** View the achievements screen.  It shows the achievements that the player has and has not obtained yet.  This function does not take a callback

Example "achievements" request:

````lua
-- Display the player's achievements.
gameNetwork.show( "achievements" )

````

**selectPlayers:** Shows a screen where the player can select which players to invite to a game or choose to auto match players.

event.data in callback listener is an array of items that have the following properties:
* maxAutoMatchPlayers (number)
* minAutoMatchPlayers (number)
* phase (string)
* array

````lua
local function selectPlayersListener(event)
	print(event.data[1], event.data[2], event.data[3]) -- prints the selected player ids
	print(event.data.maxAutoMatchPlayers) -- prints the maximum number of auto match players
	print(event.data.minAutoMatchPlayers) -- prints the minimum number of auto match players
	print(event.data.phase) -- prints the phase, either "selected" or "cancelled"
end

-- Display the screen to select players for multiplayer
gameNetwork.show("selectPlayers", 
	{
		listener = selectPlayersListener,
		minPlayers = 1, -- This does NOT include the current player
		maxPlayers = 3  -- This does NOT include the current player
	})

````

**waitingRoom:** Shows the waiting room screen

event.data in callback listener is an array of items that have the following properties:
* isError (boolean)
* phase (string)
* roomID (string)
* array

````lua
local function waitingRoomListener(event)
	print(event.type) -- "waitingRoom"
	print(event.data.isError)
	print(event.data.phase) -- "start" when the game can start, "cancel" when the user exited the waiting room screen, this will leave the room automatically
	print(event.data.roomID) -- The roomId of the room the waiting room is for
	print(event.data[1], event.data[2], event.data[3]) -- The participantIds of the room
end

-- Display the waiting room screen for a specific room
-- If the user exits the waiting room then the user will exit the room automatically
gameNetwork.show("waitingRoom", 
	{
		listener = waitingRoomListener,
		roomID = "3487324234",
		minPlayers = 2, -- The minimum number of players before the game can start
	})

````

**invitations:** Shows the current invitations for the user

event.data in callback listener is an array of items that have the following properties:
* roomID (string)
* phase (string)
* isError (boolean)

````lua
local function invitationListener(event)
	print(event.data.roomID) -- The id of the room the player selected to accept the invitation to
	print(event.data.phase) -- The phase, either "selected" or "cancelled"
	print(event.data.isError)
end

-- Display the invitations management screen
gameNetwork.show("invitations", 
	{
		listener = invitationListener
	})

`````