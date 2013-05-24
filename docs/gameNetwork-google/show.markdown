
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

## Examples

**Google Play game services:**

`````lua
-- Display the leaderboard.
gameNetwork.show( "leaderboards" )

-- Display the player's achievements.
gameNetwork.show( "achievements" )

`````