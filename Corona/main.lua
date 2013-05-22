local gameNetwork = require "gameNetwork"
local widget = require "widget"

gameNetwork.init("google")

local leaderboardId = "" -- Your leaderboard id here
local achievementId = "" -- Your achievement id here

gameNetwork.request("login",
{
	userInitiated = false
})

local left = display.screenOriginX + display.viewableContentWidth/100
local top = display.screenOriginY + display.viewableContentHeight/100
local width = display.viewableContentWidth - display.viewableContentWidth/100
local size = display.viewableContentHeight/15
local buttonTextSize = display.viewableContentWidth/20

local scoreText = display.newText("Score: ", left, top, native.systemFont, size)

local scoreTextField = native.newTextField( scoreText.x + scoreText.width/2, top , width - scoreText.width * 1.1, size)
scoreTextField.inputType = "number"

local function submitScoreListener(event)
	gameNetwork.request("setHighScore", 
		{
			localPlayerScore = 
			{
				category = leaderboardId,
				value = scoreTextField.text
			}
		})
end

local function unlockAchievementListener(event)
	gameNetwork.request("unlockAchievement",
		{
			achievement = 
			{
				identifier = achievementId
			}
		})
end

local function showLeaderboardListener(event)
	gameNetwork.show("leaderboards")
end

local function showAchievementsListener(event)
	gameNetwork.show("achievements")
end

local loginLogoutButton
local function loginLogoutListener(event)
	local function loginListener(event1)
		if event1.isError then
			loginLogoutButton:setLabel("Login")
		else
			loginLogoutButton:setLabel("Logout")
		end
	end

	if gameNetwork.request("isConnected") then
		gameNetwork.request("logout")
		loginLogoutButton:setLabel("Login")
	else
		gameNetwork.request("login",
			{
				listener = loginListener,
				userInitiated = true
			})
	end
end

local scoreSubmitButton = widget.newButton
{
	top = top + scoreText.height,
	left = left,
	width = width,
	height = size,
	label = "Submit Score",
	fontSize = buttonTextSize,
	onRelease = submitScoreListener,
}


local achievementSubmitButton = widget.newButton
{
	top = scoreSubmitButton.y + scoreSubmitButton.height/2,
	left = left,
	width = width,
	height = size,
	label = "Unlock Achievement",
	fontSize = buttonTextSize,
	onRelease = unlockAchievementListener,
}

--show leaderboard
local showLeaderboardButton = widget.newButton
{
	top = display.screenOriginY + display.viewableContentHeight/2,
	left = left,
	width = width,
	height = size,
	label = "Show Leaderboard",
	fontSize = buttonTextSize,
	onRelease = showLeaderboardListener,
}

--show achievement
local showAchievementButton = widget.newButton
{
	top = showLeaderboardButton.y + showLeaderboardButton.height/2,
	left = left,
	width = width,
	height = size,
	label = "Show Achievements",
	fontSize = buttonTextSize,
	onRelease = showAchievementsListener,
}

--login button
loginLogoutButton = widget.newButton
{
	top = display.screenOriginY + display.viewableContentHeight - size,
	left = left,
	width = width,
	height = size,
	label = "Login",
	fontSize = buttonTextSize,
	onRelease = loginLogoutListener,
}

if gameNetwork.request("isConnected") then
	loginLogoutButton:setLabel("Logout")
end