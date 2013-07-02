
# gameNetwork.show()

> --------------------- ------------------------------------------------------------------------------------------
> __Type__              [Function][api.type.Function]
> __Library__           [gameNetwork.*][api.library.gameNetwork]
> __Return value__      [TYPE][api.type.TYPE]
> __Revision__          [REVISION_LABEL](REVISION_URL)
> __Keywords__          gameNetwork, show, leaderboards, achievements
> __Sample code__       
> __See also__          [gameNetwork.init()][api.library.gameNetwork.init]<br/>[gameNetwork.request()][api.library.gameNetwork.request]
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

## Examples

**Google Play game services:**

`````lua
-- Display the leaderboard.
gameNetwork.show( "leaderboards" )

-- Display the player's achievements.
gameNetwork.show( "achievements" )


local function selectPlayersListener(event)
	print(event.data[1]) -- prints the selected players id
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

local function waitingRoomListener(event)
	print(event.type) -- "waitingRoom"
	print(event.data.isError)
	print(event.data.phase) -- "start" when the game can start, "cancel" when the user exited the waiting room screen, this will leave the room automatically
	print(event.data.roomId) -- The roomId of the waiting room
	print(event.data[1]) -- The participantIds of the room
end

-- Display the waiting room screen for a specific room
-- If the user exits the waiting room then the user will exit the room automatically
gameNetwork.show("waitingRoom", 
	{
		listener = waitingRoomListener,
		roomId = "3487324234",
		minPlayers = 2, -- The minimum number of players before the game can start
	})

local function invitationListener(event)
	print(event.data.roomId) -- The id of the room the player selected to accept the invitation to
	print(event.data.phase) -- The phase, either "selected" or "cancelled"
	print(event.data.isError)
end

-- Display the invitations management screen
gameNetwork.show("invitations", 
	{
		listener = invitationListener
	})
`````