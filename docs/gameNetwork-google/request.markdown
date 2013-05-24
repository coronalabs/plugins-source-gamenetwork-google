# gameNetwork.request()

> --------------------- ------------------------------------------------------------------------------------------
> __Type__              [Function][api.type.Function]
> __Library__           [gameNetwork.*][api.library.gameNetwork]
> __Return value__      none
> __Revision__          [REVISION_LABEL](REVISION_URL)
> __Keywords__          gameNetwork, request, gamecenter
> __Sample code__       */CoronaSDK/SampleCode/GameNetwork/GameCenter*
> __See also__          [gameNetwork.init()][plugin.gameNetwork-google.init]<br/>[gameNetwork.show()][plugin.gameNetwork-google.show]
> --------------------- ------------------------------------------------------------------------------------------


## Overview

Send or request information to/from the game network provider.

## Syntax

	gameNetwork.request( command [, params ...] )

##### command ~^(required)^~
_[String][api.type.String]._ The following commands are supported for the corresponding game networks:

**Google Play game services:**

* setHighScore
* loadScores
* loadLocalPlayer
* loadPlayers
* loadFriends
* loadAchievements
* unlockAchievement
* loadAchievementDescriptions
* loadLeaderboardCategories
* isConnected
* login
* logout

##### params ~^(optional)^~
The following parameters are supported for the corresponding game networks:

#### Google Play game services:

**setHighScore:** Sets a high score for the currently logged in user for the specified leaderboard (category). If the high score is not higher than the one currently on the server, the server will keep the highest value.

This function corresponds to Apple's reportScoreWithCompletionHandler. The nomenclature deviates in this case to provide consistency with our existing gameNetwork APIs.

Example "setHighScore" request:

	gameNetwork.request( "setHighScore",
	{
        localPlayerScore = { category="Cy_sd893DEewf3", value=25 },
        listener=requestCallback
	})

'localPlayerScore' is a required table..

In the localPlayerScore table:

'category' must be a string that matches the ID of the leaderboard you want to register the score with as entered on iTunes Connect. (The ID you pick need not follow the fully qualified reverse domain style shown here.) '

'value' must be a number representing your score.

**loadScores:** event.data in callback listener is an array of items (tables) that have
the following properties:

* playerID (string)
* category (string)
* value (number)
* date (string)
* formattedValue (string)
* rank (integer)
* unixTime (integer)

example:
event.data[5].formattedValue	-- #event.data == 2nd value specified in range table

Example "loadScores" request:

	gameNetwork.request( "loadScores",
	{
        leaderboard =
        {
            category="Cy_SLDWING4334h",
            playerScope="Global",   -- Global, FriendsOnly
            timeScope="AllTime",    -- AllTime, Week, Today
            range={1,5},
            playerCentered=true,
        },
        listener=requestCallback
	})

'leaderboard' is a required table.

In the leaderboard table:

'category' (required) must be a string that matches the name of the board you want to fetch the scores with generated on the Google Play developers console.

'playerScope' (optional) is a string of either "Global" or "FriendsOnly". The latter setting will restrict the fetched scores to only people in the player's circles.

'timeScope' (optional) is a string of either "AllTime", "Week", "Today" which limits the fetched scores to the specified time range.

'range' (optional) is an array of two values. The first value is ignored. The second value is the number of players to retrieve. Google says this number must be less than 25.  The default number is 25.

'playerCentered' (optional) is a boolean value.  If true this then value will return the scores that are centered around the currently logged in player.  If false then this will return the top scores.

**loadLocalPlayer:** Requests the GKPlayer object for the currently logged-in user.

event.data in callback listener includes the following properties:

* playerID (string)
* alias (string)

example:
event.data.playerID

Example "loadLocalPlayer" request:

	gameNetwork.request( "loadLocalPlayer", { listener=requestCallback } )

**loadPlayers:** Requests a list of players with the specified player IDs, and returns an array of items (tables) for each requested player in the callback listener.

event.data in callback listener is an array of items that
have the following properties:

* playerID (string)
* alias (string)

Each item (table) in the array corresponds to Apple's GKPlayer class

example:
event.data[3].alias

Example "loadPlayers" request:

	gameNetwork.request( "loadPlayers",
	{
        playerIDs =
        {
            "G:123456789",
            "G:1234567890",
            "G:0123456789"
        },
        listener=requestCallback
	})

**loadAchievements:** Loads a list of the user's available achievements for the app and returns an array of items (tables) representing each achievement in the callback listener.  This call returns the same thing as loadAchievementDescriptions.

event.data in callback listener is an array of items that have the
following properties (each representing an achievement):

* identifier (string)
* title (string)
* description (string)
* isCompleted (boolean)
* isHidden (boolean)
* lastReportedDate (number)

example:
event.data[4].identifier

Example "loadAchievements" request:

	gameNetwork.request( "loadAchievements", { listener=requestCallback } )

**unlockAchievement:** Unlocks the specified achievement (identifier).

Example "unlockAchievement" request.

	gameNetwork.request( "unlockAchievement",
	{
        achievement =
        {
            identifier="com.appletest.one_tap",
        },
        listener=requestCallback
	})
    
In the "achievement" table:

'identifier' (required) must be a string that matches the name of the achievement you want to unlock/report as generated on the Google Play developers console.

The listener callback will fill event.data with a table that contains the following information:

* achievementId (string)

**loadAchievementDescriptions:** Requests a list of all descriptions associated with the achievements for the app and returns an array of items (tables) representing each achievement description object in the callback listener.  This call returns the same thing as loadAchievements.

event.data in callback listener is an array of items which are the
descriptions of your achievements.

* identifier (string)
* title (string)
* description (string)
* isCompleted (boolean)
* isHidden (boolean)
* lastReportedDate (number)

Example "loadAchievementDescriptions" request:

	gameNetwork.request( "loadAchievementDescriptions", { listener=requestCallback } )

**loadLeaderboardCategories:** Requests a list of leaderboard categories for the app and returns an array of tables with each table containing description information of a leaderboard in the callback listener.

event.data in callback listener is an array of items (tables) where each table contains the keys 'category' and 'title', both of which are strings.

Example "loadLeaderboardCategories" request:

	gameNetwork.request( "loadLeaderboardCategories", { listener=requestCallback } )
 
	-- example of what an event.data table returned via callback listener looks like
	event.data =
	{
        [1] = {
            category = "com.appledts.EasyTapList",
            title = "Easy"
        },
        [2] = {
            category = "com.appledts.HardTapList",
            title = "Hard"
        },
        [3] = {
            category = "com.appledts.AwesomeTapList",
            title = "Awesome"
        },
	}

**isConnected:** Checks to see if the user is currently logged into Google Play game services.  This function returns immediately with the result so there is no need to supply it with a callback.  Returns true if the current user is currently logged in and false if the user isn't.

**login:** Tries to log the user into Google Play game services.  The table takes a parameter called userInitated.  This parameter defaults to true.  If this parameter is set to true then it will try to resolve any problems with the login process so for example it will show the login popup.  If it is set to false then it will just try to connect without trying to resolve any errors.  Setting this to false is useful for automatically logging in at the start of the app without annoying the user to log in every time.
Example "login" request:

    gameNetwork.request( "login",
    {
        userInitiated=true,
        listener=requestCallback
    })

event in callback listener is a boolean flag that states if there with an error with the sign in process.

* isError(boolean)

**logout:** Logs the user out of Google Play game services.  This function does not accept a callback.