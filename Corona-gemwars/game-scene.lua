local storyboard = require( "storyboard" )
local scene = storyboard.newScene()
local json = require("json")

local gl = require( "game-logic" )

local SCORE_LIMIT = 1000

local _W = display.contentWidth 
local _H = display.contentHeight

local gameOverLayout
local gameOverText1
local gameOverText2
local gameOverText3

storyboard.lastMove = {}

local p1score, p2score
local p1total = 0
local p2total = 0
local turnText

-- variable that checks who's player turn is it
local isMyTurn = false

-- the number of moves played
local numberOfMoves = 0

-- localize variables
local connected = false
local lastMove = nil

local processMove

local function getMoves()
	processMove(lastMove)
end

local function stopMatch()
	lastMove = nil
	storyboard.didInitTable = false

	storyboard.gn.request("leaveRoom", 
	{
		roomID = storyboard.matchId,
	})
end

local function showGameOver ()

	gameOverLayout.alpha = 0.8
	gameOverText1.alpha = 1
	gameOverText2.alpha = 1
	gameOverText3.alpha = 1
	
	if (storyboard.isHost == 1) then
		timer.performWithDelay( 2000, stopMatch )
	end
end

local function handleLogic( event )
	if event.name == "gemTapped" then
		-- gem has been tapped
		-- we push the move
		gl.gemsTable.moveContent.isFirstMove = nil
		local playerVariable = 10
		if storyboard.isHost then
			playerVariable = 20
		end

		gl.gemsTable.moveContent.params = { event.params[1], event.params[2], event.params[3], numberOfMoves }
		storyboard.lastMove = {event.params[1], event.params[2], event.params[3], numberOfMoves }
		p1total = p1total + event.params[3]
		p1score.text = string.format( "YOU: %6.0f", p1total )
		gameOverText2.text = string.format( "YOU: %6.0f", p1total )
		if p1total > SCORE_LIMIT or p2total > SCORE_LIMIT then
			showGameOver()
		end

		storyboard.gn.request("sendMessage", 
		{
			playerIDs = {storyboard.otherPlayerId},
			roomID = storyboard.matchId,
			message =  json.encode( gl.gemsTable.moveContent )
		})
		lastMove = gl.gemsTable.moveContent
		
		numberOfMoves = numberOfMoves + 1
		
		isMyTurn = false
		gl.overlay.isVisible = true
		turnText.text = "Wait for your turn"
	end
end

local function onTouchGameOverScreen ( self, event )

	if event.phase == "began" then
		storyboard.gn.request("leaveRoom", 
		{
			roomID = storyboard.matchId,
		})
		storyboard.gotoScene( "menu-scene", "fade", 400	)
		return true
	end
end 

processMove = function( moves )
	-- if it is the first move
	if moves ~= nil and storyboard.isGuest == 1 and storyboard.didInitTable == false then
		gl.initTable( moves )
		storyboard.didInitTable = true
	elseif nil ~= moves then
		-- not a first move
		if moves.params[1] ~= storyboard.lastMove[1] or moves.params[2] ~= storyboard.lastMove[2] or moves.params[3]~=storyboard.lastMove[3] or moves.params[4]~=storyboard.lastMove[4] then
			gl.receivedTable = moves.moves
			gl.redrawTable()
			p2total = p2total + moves.params[3]
			p2score.text = string.format( " P2: %6.0f", p2total )
			gameOverText3.text = string.format( " P2: %6.0f", p2total )
			if p1total > SCORE_LIMIT or p2total > SCORE_LIMIT then
				showGameOver()
			end
			numberOfMoves = numberOfMoves + 1
			isMyTurn = true
			gl.overlay.isVisible = false
			turnText.text = "It is your turn"
		end
	end
end

local function messageListener( messageEvent )
	if messageEvent.data ~= nil and messageEvent.data.message ~= nil then
		-- This is done to make sure that the messageListener is set up on both devices before we send any critical information
		if messageEvent.data.message == "connect" then
			connected = true
			storyboard.gn.request("sendMessage",
			{
				playerIDs = {storyboard.otherPlayerId},
				roomID = storyboard.matchId,
				message =  "connect"
			})
		else
			lastMove = json.decode(messageEvent.data.message)
			processMove(lastMove)
		end
	end
end

local function onKeyEvent(event)
	if event.keyName == "back" then
		Runtime:removeEventListener( "key", onKeyEvent );
		stopMatch()
		storyboard.gotoScene( "menu-scene", "fade", 400	 )
	end
	return true
end

-- Called when the scene's view does not exist:
function scene:createScene( event )
	print("createScene")

	p1total = 0
	p2total = 0
	p1score = 0
	p2score = 0

	if storyboard.isGuest == 1 then
		isMyTurn = true
	end 

	Runtime:addEventListener( "key", onKeyEvent );
	-- Sets the listener that will be called whenever a message has been received. 
	storyboard.gn.request("setMessageReceivedListener", 
	{
		listener = messageListener
	})

	local screenGroup = self.view

	groupGameLayer = display.newGroup()
	groupEndGameLayer = display.newGroup()

	--score text
	

	if storyboard.isHost == 1 then
		gl.init( storyboard.iSheet, groupGameLayer, handleLogic )
		-- reseed
		math.randomseed( os.time() )
		for i = 1, 8, 1 do
			gl.gemsTable[i] = {}
			for j = 1, 8, 1 do
				gl.gemsTable[i][j] = gl.newGem( i,j )
			end
		end
		
		gl.overlay:toFront()
	else
		-- init, but don't show content
		gl.init( storyboard.iSheet, groupGameLayer, handleLogic )
	end

	-- if we host the game, we send the first move.
	if storyboard.isHost == 1 then
		local checkConnection
		local function sendMove()
			local params = {}
			gl.gemsTable.moveContent.isFirstMove = 1
			storyboard.gn.request("sendMessage", 
			{
				playerIDs = {storyboard.otherPlayerId},
				roomID = storyboard.matchId,
				message =  json.encode( gl.gemsTable.moveContent )
			})
			lastMove = gl.gemsTable.moveContent
		end

		-- This is done to make sure that the messageListener is set up on both devices before we send any critical information
		checkConnection = function()
			if connected == false then
				storyboard.gn.request("sendMessage",
				{
					playerIDs = {storyboard.otherPlayerId},
					roomID = storyboard.matchId,
					message =  "connect"
				})
			else
				sendMove()
				timer.cancel(checkConnection)
			end
		end

		timer.performWithDelay(500, checkConnection, 0)
	end 
	
	p1score = display.newText( "P1:" , 40, 20, "Futura-CondensedExtraBold", 25 * storyboard.globalScale )
	p1score.text = string.format( "YOU: %6.0f", 0 )
	p1score:setReferencePoint(display.TopLeftReferencePoint)
	p1score.x = 10
	p1score:setTextColor(255, 255, 255, 255)
		
	groupGameLayer:insert ( p1score )
	
	p2score = display.newText( "P2:" , 40, 20, "Futura-CondensedExtraBold", 25 * storyboard.globalScale )
	p2score.text = string.format( " P2: %6.0f", 0 )
	p2score:setReferencePoint(display.TopLeftReferencePoint)
	p2score.x = 240
	p2score:setTextColor(255, 255, 255, 255)
		
	groupGameLayer:insert ( p2score )

	gameOverLayout = display.newRect( 0, 0, 320, 480)
	gameOverLayout:setFillColor( 0, 0, 0 )
	gameOverLayout.alpha = 0
	
	gameOverText1 = display.newText( "GAME OVER", 0, 0, "Futura-CondensedExtraBold", 60 * storyboard.globalScale )
	gameOverText1:setTextColor( 255 )
	gameOverText1:setReferencePoint( display.CenterReferencePoint )
	gameOverText1.x, gameOverText1.y = _W * 0.5, _H * 0.5 -150
	gameOverText1.alpha = 0

	gameOverText2 = display.newText( "P1: ", 0, 0, "Futura-CondensedExtraBold", 48 * storyboard.globalScale )
	gameOverText2.text = string.format( "YOU: %6.0f", p1total )
	gameOverText2:setTextColor( 255 )
	gameOverText2:setReferencePoint( display.CenterReferencePoint )
	gameOverText2.x, gameOverText2.y = _W * 0.5, _H * 0.5 - 50
	gameOverText2.alpha = 0

	gameOverText3 = display.newText( "P1: ", 0, 0, "Futura-CondensedExtraBold", 48 * storyboard.globalScale )
	gameOverText3.text = string.format( " P2: %6.0f", p2total )
	gameOverText3:setTextColor( 255 )
	gameOverText3:setReferencePoint( display.CenterReferencePoint )
	gameOverText3.x, gameOverText3.y = _W * 0.5, _H * 0.5 + 10
	gameOverText3.alpha = 0
	
	gameOverLayout.touch = onTouchGameOverScreen
	gameOverLayout:addEventListener( "touch", gameOverLayout )


	groupEndGameLayer:insert ( gameOverLayout )
	groupEndGameLayer:insert ( gameOverText1 )
	groupEndGameLayer:insert ( gameOverText2 )
	groupEndGameLayer:insert ( gameOverText3 )

	turnText = display.newText( "It is your turn" , 40, display.contentHeight - 50, "Futura-CondensedExtraBold", 25 * storyboard.globalScale )
	turnText.text = "It is your turn"
	turnText:setReferencePoint(display.CenterReferencePoint)
	turnText.x = display.contentWidth * 0.5
	turnText:setTextColor(255, 255, 255, 255)
		
	groupGameLayer:insert ( turnText )

	-- insterting display groups to the screen group (storyboard group)
	screenGroup:insert ( groupGameLayer )
	screenGroup:insert ( groupEndGameLayer )
	

	if isMyTurn then
		gl.overlay.isVisible = false
	else
		gl.overlay.isVisible = true
		turnText.text = "Wait for your turn"
	end

end
 
-- Called BEFORE scene has moved onscreen:
function scene:willEnterScene( event )
		local screenGroup = self.view
		
		-----------------------------------------------------------------------------
				
		--		This event requires build 2012.782 or later.
		
		-----------------------------------------------------------------------------
		
end
 
-- Called immediately after scene has moved onscreen:
function scene:enterScene( event )
		local screenGroup = self.view
		
		-----------------------------------------------------------------------------
				
		--		INSERT code here (e.g. start timers, load audio, start listeners, etc.)
		
		-----------------------------------------------------------------------------
	
	-- remove previous scene's view
		
	storyboard.purgeScene( "menu-scene" )

	print( "1: enterScene event" )

end
 
 
-- Called when scene is about to move offscreen:
function scene:exitScene( event )
		local screenGroup = self.view

end
 
-- Called AFTER scene has finished moving offscreen:
function scene:didExitScene( event )
		local screenGroup = self.view
		
		-----------------------------------------------------------------------------
				
		--		This event requires build 2012.782 or later.
		
		-----------------------------------------------------------------------------
		
end
 
 
-- Called prior to the removal of scene's "view" (display group)
function scene:destroyScene( event )
		local screenGroup = self.view
		
		-----------------------------------------------------------------------------
		
		--		INSERT code here (e.g. remove listeners, widgets, save state, etc.)
		
		-----------------------------------------------------------------------------
		
end
 
-- Called if/when overlay scene is displayed via storyboard.showOverlay()
function scene:overlayBegan( event )
		local screenGroup = self.view
		local overlay_scene = event.sceneName  -- overlay scene name
		
		-----------------------------------------------------------------------------
				
		--		This event requires build 2012.797 or later.
		
		-----------------------------------------------------------------------------
		
end
 
-- Called if/when overlay scene is hidden/removed via storyboard.hideOverlay()
function scene:overlayEnded( event )
		local screenGroup = self.view
		local overlay_scene = event.sceneName  -- overlay scene name
 
		-----------------------------------------------------------------------------
				
		--		This event requires build 2012.797 or later.
		
		-----------------------------------------------------------------------------
		
end
 
 
 
---------------------------------------------------------------------------------
-- END OF YOUR IMPLEMENTATION
---------------------------------------------------------------------------------
 
-- "createScene" event is dispatched if scene's view does not exist
scene:addEventListener( "createScene", scene )
 
-- "willEnterScene" event is dispatched before scene transition begins
scene:addEventListener( "willEnterScene", scene )
 
-- "enterScene" event is dispatched whenever scene transition has finished
scene:addEventListener( "enterScene", scene )
 
-- "exitScene" event is dispatched before next scene's transition begins
scene:addEventListener( "exitScene", scene )
 
-- "didExitScene" event is dispatched after scene has finished transitioning out
scene:addEventListener( "didExitScene", scene )
 
-- "destroyScene" event is dispatched before view is unloaded, which can be
-- automatically unloaded in low memory situations, or explicitly via a call to
-- storyboard.purgeScene() or storyboard.removeScene().
scene:addEventListener( "destroyScene", scene )
 
-- "overlayBegan" event is dispatched when an overlay scene is shown
scene:addEventListener( "overlayBegan", scene )
 
-- "overlayEnded" event is dispatched when an overlay scene is hidden/removed
scene:addEventListener( "overlayEnded", scene )
 
---------------------------------------------------------------------------------
 
return scene