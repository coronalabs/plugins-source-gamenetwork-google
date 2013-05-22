local gameNetwork = require "gameNetwork"
local widget = require "widget"

-- Init game network to use Google Play game services
gameNetwork.init("google")

local leaderboardId = "" -- Your leaderboard id here
local achievementId = "" -- Your achievement id here

-- Tries to automatically log in the user without displaying the login screen if the user doesn't want to login
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

-- Submits the score from the scoreTextField into the leaderboard
local function submitScoreListener(event)
	gameNetwork.request("setHighScore", 
		{
			localPlayerScore = 
			{
				category = leaderboardId, -- Id of the leaderboard to submit the score into
				value = scoreTextField.text -- The score to submit
			}
		})
end

local function unlockAchievementListener(event)
	gameNetwork.request("unlockAchievement",
		{
			achievement = 
			{
				identifier = achievementId -- The id of the achievement to unlock for the current user
			}
		})
end

local function showLeaderboardListener(event)
	gameNetwork.show("leaderboards") -- Shows all the leaderboards.
end

local function showAchievementsListener(event)
	gameNetwork.show("achievements") -- Shows the locked and unlocked achievements.
end

local loginLogoutButton
local function loginLogoutListener(event)
	local function loginListener(event1)
		-- Checks to see if there was an error with the login.
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
		-- Tries to login the user, if there is a problem then it will try to resolve it. eg. Show the log in screen.
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

--show leaderboard button
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

--show achievement button
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

-- Checks if the auto login worked and if it did then change the text on the button
if gameNetwork.request("isConnected") then
	loginLogoutButton:setLabel("Logout")
end