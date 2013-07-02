local storyboard = require( "storyboard" )
local json = require( "json" )

local scene = storyboard.newScene()
 
local bg, text1, text2, text3;
local _W = display.contentWidth 
local _H = display.contentHeight

-- the match id, as a storyboard variable
storyboard.matchId = nil
-- is the player hosting the game
storyboard.isHost = 0
-- is the player joining the game
storyboard.isGuest = 0
-- the player id
storyboard.myPlayerId = nil
-- the other player id
storyboard.otherPlayerId = nil


local function onKeyEvent(event)
	if event.keyName == "back" then
		native.requestExit()
	end
	return true
end	

local function beginGame(playerId, roomID)
	Runtime:removeEventListener( "key", onKeyEvent );
	local options =
	{
		effect = "fade",
		time = 400
	}

	storyboard.didInitTable = false
	storyboard.otherPlayerId = playerId
	storyboard.matchId = roomID

	storyboard.gotoScene( "game-scene", options )
end

local function waitingRoomListener(waitingRoomEvent)
	if waitingRoomEvent.data.phase == "start" then
		-- We only need the first player because its a 2 player game
		beginGame(waitingRoomEvent.data[1], waitingRoomEvent.data.roomID)
	end
end

-- Called when the scene's view does not exist
function scene:createScene( event )
	local screenGroup = self.view
end

-- Called when the scene's view is about to appear
function scene:willEnterScene( event )
    local screenGroup = self.view
end

local function roomListener(event)
	if event.type == "joinRoom" or event.type == "createRoom" then
		if event.data.isError then 
			native.showAlert("Room Error", "Sorry there was an error when trying to create/join a room")
		else
			storyboard.gn.show("waitingRoom", {
				listener = waitingRoomListener,
				roomID = event.data.roomID,
				minPlayers = 0
			})
		end
	end
end

-- Called when the scene's view appeared 
function scene:enterScene( event )

	local loginText

    local screenGroup = self.view

    storyboard.purgeScene( "game-scene" )

	-- touch listener for the push button
	local function buttonPressed( event )
		local target = event.target
		
		if event.phase == "began" then
			target:setTextColor( 245, 127, 32 )
		elseif event.phase == "ended" then
			-- action
			if target.tag == 1 then
				if not storyboard.gn.request("isConnected") then
					native.showAlert("Login", "Please login before trying to join a match")
				else
					local function invitationsListener(invitationsEvent)
						-- This will let the user join a room 
						storyboard.gn.request("joinRoom",
						{
							roomID = invitationsEvent.data.roomID,
							listener = roomListener
						})
					end

					-- This will show the invitations screen which shows a list of all the invitations the user has
					storyboard.gn.show("invitations", {
						listener = invitationsListener
					})
					storyboard.isGuest = 1
				end
			elseif target.tag == 2 then
				if not storyboard.gn.request("isConnected") then
					native.showAlert("Login", "Please login before trying to start a match")
				else
					local function selectPlayersListener(selectPlayerEvent)
						-- Create a room with only the first selection
						local array = {selectPlayerEvent.data[1]}
						storyboard.gn.request("createRoom", {
							listener = roomListener,
							playerIDs = array
						})
					end
					storyboard.gn.show("selectPlayers", {
						listener = selectPlayersListener,
						minPlayers = 1,
						maxPlayers = 1
					})

					-- The following can also be used to get a list of players that can be invited and then shown in some other way
					--[[
						local function loadFriendsListener(event)
							for nameCount = 1, #event.data do
								print(event.data[nameCount].alias, " ", event.data[nameCount].playerID)
							end
						end
						storyboard.gn.request("loadFriends", {listener = loadFriendsListener})
					--]]
					

					-- This is to mark who created the game
					storyboard.isHost = 1
				end
			elseif target.tag == 3 then
				local function loginListener(loginEvent)
					-- Checks to see if there was an error with the login.
					if loginEvent.isError then
						loginText.text = "LOGIN"
					else
						loginText.text = "LOGOUT"
					end
				end

				if storyboard.gn.request("isConnected") then
					storyboard.gn.request("logout")
					loginText.text = "LOGIN"
				else
					storyboard.gn.request("login",
					{
						listener = loginListener,
						userInitiated = true
					})
				end
			end
			-- reset the button color
			target:setTextColor( 255, 255, 255 )
		end
	end
	
	local screenWidth = display.contentWidth - (display.screenOriginX*2)
	local screenRealWidth = screenWidth / display.contentScaleX

	local screenHeight = display.contentHeight - (display.screenOriginY*2)
	local screenRealHeight = screenHeight / display.contentScaleY

	local bg = display.newImage( storyboard.iSheet ,1)
	bg:setReferencePoint( display.CenterReferencePoint )
	bg.x = display.contentWidth * 0.5
	bg.y = display.contentHeight * 0.5
	bg.xScale, bg.yScale = storyboard.globalScale, storyboard.globalScale
	screenGroup:insert( bg )
	
	local logo = display.newImage( storyboard.iSheet, 6)
	logo:setReferencePoint( display.CenterReferencePoint )
	logo.xScale, logo.yScale = storyboard.globalScale, storyboard.globalScale
	logo.x = display.contentWidth * 0.5
	logo.y = 120
	screenGroup:insert( logo )
	
	local joinText = display.newText( screenGroup, "JOIN MATCH", display.contentWidth * 0.5, display.contentHeight - 270, "Futura-CondensedExtraBold", 30 )
	joinText:setReferencePoint( display.CenterReferencePoint )
	joinText.x = display.contentWidth * 0.5
	joinText:setTextColor( 255, 255, 255 )
	joinText:addEventListener( "touch", buttonPressed )
	joinText.tag = 1
	
	local hostText = display.newText( screenGroup, "CREATE MATCH", display.contentWidth * 0.5, display.contentHeight - 200, "Futura-CondensedExtraBold", 30 )
	hostText:setReferencePoint( display.CenterReferencePoint )
	hostText.x = display.contentWidth * 0.5
	hostText:setTextColor( 255, 255, 255 )
	hostText:addEventListener( "touch", buttonPressed )
	hostText.tag = 2

	-- This will check to see if the player is logged in so we can show the appropriate text
	local text = "LOGIN"
	if storyboard.gn.request("isConnected") then
		text = "LOGOUT"
	end

	loginText = display.newText( screenGroup, text, display.contentWidth * 0.5, display.contentHeight - 130, "Futura-CondensedExtraBold", 30 )
	loginText:setReferencePoint( display.CenterReferencePoint )
	loginText.x = display.contentWidth * 0.5
	loginText:setTextColor( 255, 255, 255 )
	loginText:addEventListener( "touch", buttonPressed )
	loginText.tag = 3
	

	local function receivedInvitationListener(event)
		native.showAlert("Invitation Received", "You received an invitation from " .. event.data.alias, {"OK"})
	end

	if storyboard.gn.request("isConnected") then
		-- This will call the listener whenever the user receives an invitation
		storyboard.gn.request("setInvitationReceivedListener", 
		{
			listener = receivedInvitationListener,
		})

		--[[
			This will listen for any modifications to the room, for example
			* createRoom -- The logged in player created a room
			* joinRoom -- The logged in player joined a room
			* leaveRoom -- The logged in player left the room
			* connectedRoom -- All the players that were invited have accepted and all the auto match making has been completed
			* peerAcceptedInvitation  -- The list of players who accepted an invitation to join a room
			* peerDeclinedInvitation -- The list of players who declined an invitation to join a room
			* peerLeftRoom  -- The list of players who left a room
			* peerDisconnectedFromRoom  -- The list of players who disconnected from a room
		--]]
		storyboard.gn.request("setRoomListener",
		{
			listener = roomListener,
		})
	end

	Runtime:addEventListener( "key", onKeyEvent );
end

function scene:exitScene( event )
    local screenGroup = self.view
end
 
-- "createScene" event is dispatched if scene's view does not exist
scene:addEventListener( "createScene", scene )
 
-- "willEnterScene" event is dispatched before scene transition begins
scene:addEventListener( "willEnterScene", scene )
 
-- "enterScene" event is dispatched whenever scene transition has finished
scene:addEventListener( "enterScene", scene )
 
-- "exitScene" event is dispatched before next scene's transition begins
scene:addEventListener( "exitScene", scene )
 
---------------------------------------------------------------------------------
 
return scene