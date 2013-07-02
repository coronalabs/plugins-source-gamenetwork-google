-- hide the status bar
display.setStatusBar( display.HiddenStatusBar )

-- require the storyboard module
local storyboard = require "storyboard"

-- the asset loading suffix
local assetSuffix = "1"
local fileSuffix = ""
storyboard.globalScale = 1

-- for most devices with scaling values between 0.6 and 0.25, we're talking about a retina device, non-tablet
if display.contentScaleX <= 0.6 then
	assetSuffix = "2"
	fileSuffix = "@2x"
	storyboard.globalScale = 0.5
end

-- the spritesheet
-- image sheets / theme
local theme = "sprites"
local sheetInfo = require( theme .. "-" .. assetSuffix .. "x" )
storyboard.iSheet = graphics.newImageSheet( theme .. "-" ..assetSuffix.. "x.png", sheetInfo:getSheet() )

-- assign the gameNetwork plugin to the gn variable of the storyboard
storyboard.gn = require( "gameNetwork" )

-- init the gameNetwork plugin
storyboard.gn.init( "google" )

local function setInvitationListener(event)
	storyboard.gotoScene( "menu-scene" )
end


storyboard.gn.request("login",
{
	userInitiated = false,
	listener = setInvitationListener
})