local self = {}

local json = require "json"

-- class variables
self._spriteSheet = nil
self._displayGroup = nil
self._listener = nil

-- localized variables
local DW = display.contentWidth 
local DH = display.contentHeight

self.overlay = nil

local globalScale = 1
if display.contentScaleX <= 0.6 then
	globalScale = 0.5
end

self.achievedScore = 0
self.receivedTable = nil

self.event = nil

self.gemsTable = {}
self.gemsTable.moveContent = {}
self.gemsTable.moveContent.moves = {}
for i = 1, 8 do
	self.gemsTable.moveContent.moves[ i ] = {}
end


local numberOfMarkedToDestroy = 0
local gemToBeDestroyed  			-- used as a placeholder
local isGemTouchEnabled = true 		-- blocker for double touching gems

-- forward declaration
local onGemTouch

self.init = function( iSheet, displayGroup, listener )
	
	self._displayGroup = displayGroup
	self._spriteSheet = iSheet
	
	local screenWidth = display.contentWidth - (display.screenOriginX*2)
	local screenRealWidth = screenWidth / display.contentScaleX

	local screenHeight = display.contentHeight - (display.screenOriginY*2)
	local screenRealHeight = screenHeight / display.contentScaleY
	
	local bg = display.newImage( iSheet ,1)
	bg:setReferencePoint( display.CenterReferencePoint )
	bg.x = display.contentWidth * 0.5
	bg.y = display.contentHeight * 0.5
	bg.xScale, bg.yScale = globalScale, globalScale
	displayGroup:insert( bg )
	
	self.overlay = display.newRect( 0, 0, display.contentWidth, display.contentHeight )
	self.overlay.alpha = 0.01
	displayGroup:insert ( self.overlay )
	-- prevent propagation of touch events
	self.overlay:addEventListener("touch", function() return true end)
	self.overlay:addEventListener("tap", function() return true end)
	
	self._listener = listener
	
end

self.newGem = function( i, j )

	local newGem

	local R = math.random( 2,5 )

	newGem = display.newImage( self._spriteSheet ,R)
	newGem:setReferencePoint( display.CenterReferencePoint )
	newGem.x = i*40-20
	newGem.y = j*40+60
	newGem.xScale, newGem.yScale = 0.1, 0.1
	newGem.rotation = 45
	newGem.i = i
	newGem.j = j
	newGem.isMarkedToDestroy = false
	
	if R == 4 then 
		newGem.gemType = "red"
		self.gemsTable.moveContent.moves[i][j] = "red"
	elseif R == 3 then 
		newGem.gemType = "green"
		self.gemsTable.moveContent.moves[i][j] = "green"
	elseif R == 2 then 
		newGem.gemType = "blue"
		self.gemsTable.moveContent.moves[i][j] = "blue"
	elseif R == 5 then 
		newGem.gemType = "yellow"
		self.gemsTable.moveContent.moves[i][j] = "yellow"
	end

	--new gem falling animation
	transition.to( newGem, { time=100, xScale = globalScale * 0.8, yScale = globalScale * 0.8} )

	self._displayGroup:insert( newGem )

	newGem.touch = onGemTouch
	newGem:addEventListener( "touch", newGem )

return newGem
end

self.gatherTableState = function()
	for i = 1, 8, 1 do
		for j = 1, 8, 1 do
			self.gemsTable.moveContent.moves[i][j] = self.gemsTable[i][j].gemType
 		end
 	end
end

self.newMoveGem = function( i, j, color )

	local newGem
	
	local R = 0
	
	if color == "red" then
		R = 4
	elseif color == "green" then
		R = 3
	elseif color == "blue" then
		R = 2
	elseif color == "yellow" then
		R = 5
	end
	
	newGem = display.newImage( self._spriteSheet ,R)
	newGem:setReferencePoint( display.CenterReferencePoint )
	newGem.x = i*40-20
	newGem.y = j*40+60
	newGem.xScale, newGem.yScale = 0.1, 0.1
	newGem.rotation = 45
	newGem.i = i
	newGem.j = j
	newGem.isMarkedToDestroy = false
	 
	newGem.gemType = color
	self.gemsTable.moveContent.moves[i][j] = color

	--new gem falling animation
	transition.to( newGem, { time=100, xScale = globalScale * 0.8, yScale = globalScale * 0.8} )

	self._displayGroup:insert( newGem )

	newGem.touch = onGemTouch
	newGem:addEventListener( "touch", newGem )

return newGem
end

self.initTable = function( content )
	
    for i = 1, 8, 1 do

    	self.gemsTable[i] = {}
		
		for j = 1, 8, 1 do
			--print("Move: ", content.moves[i][j])
			self.gemsTable[i][j] = self.newMoveGem( i,j, content.moves[ i ][ j ] )
	
 		end
 	end

	self.overlay:toFront()
end

self.redrawTable = function()
	
    for i = 1, 8, 1 do		
		for j = 1, 8, 1 do
			--print("Move: ", self.receivedTable[i][j])
			if (self.gemsTable[i][j].gemType ~= self.receivedTable[i][j]) then
			self.gemsTable[i][j]:removeSelf()
			self.gemsTable[i][j] = self.newMoveGem( i,j, self.receivedTable[ i ][ j ] )
			end
 		end
 	end

self.receivedTable = nil

	self.overlay:toFront()
end

self.shiftGems = function()

print ("Shifting Gems")

	--print("generated table: ", json.encode( self.gemsTable.moveContent.moves ) ) 

	-- first roww
	for i = 1, 8, 1 do
			if self.gemsTable[i][1].isMarkedToDestroy then
					-- current gem must go to a 'gemToBeDestroyed' variable holder to prevent memory leaks
					-- cannot destroy it now as gemsTable will be sorted and elements moved down
					gemToBeDestroyed = self.gemsTable[i][1]
						self.gemsTable[i][1] = self.newGem(i,1, self._spriteSheet, self._displayGroup)
					-- destroy old gem
					gemToBeDestroyed:removeSelf()
					gemToBeDestroyed = nil
			end
	end

	-- rest of the rows
	for j = 2, 8, 1 do  -- j = row number - need to do like this it needs to be checked row by row
		for i = 1, 8, 1 do
			if self.gemsTable[i][j].isMarkedToDestroy then --if you find and empty hole then shift down all gems in column
					gemToBeDestroyed = self.gemsTable[i][j]
					-- shiftin whole column down, element by element in one column
					for k = j, 2, -1 do -- starting from bottom - finishing at the second row
						-- curent markedToDestroy Gem is replaced by the one above in the gemsTable
						self.gemsTable[i][k] = self.gemsTable[i][k-1]
						self.gemsTable[i][k].y = self.gemsTable[i][k].y +40
						transition.to( self.gemsTable[i][k], { time=0, y= self.gemsTable[i][k].y} )
						-- we change its j value as it has been 'moved down' in the gemsTable
						self.gemsTable[i][k].j = self.gemsTable[i][k].j + 1
					end
					-- create a new gem at the first row as there is en ampty space due to gems
					-- that have been moved in the column
						self.gemsTable[i][1] = self.newGem(i,1, self._spriteSheet, self._displayGroup)
					-- destroy the old gem (the one that was invisible and placed in gemToBeDestroyed holder)
					gemToBeDestroyed:removeSelf()
					gemToBeDestroyed = nil
			end 
		end
	end
	
	self.gatherTableState()
	--print("generated table after switching: ", json.encode( self.gemsTable.moveContent.moves ) ) 
	

	self.overlay:toFront()

	
end --shiftGems()

self.markToDestroy = function( gem )

	gem.isMarkedToDestroy = true
	numberOfMarkedToDestroy = numberOfMarkedToDestroy + 1
	
	-- check on the left
	if gem.i>1 then
		if (self.gemsTable[gem.i-1][gem.j]).isMarkedToDestroy == false then

			if (self.gemsTable[gem.i-1][gem.j]).gemType == gem.gemType then

			   self.markToDestroy( self.gemsTable[gem.i-1][gem.j] )
			end	 
		end
	end

	-- check on the right
	if gem.i<8 then
		if (self.gemsTable[gem.i+1][gem.j]).isMarkedToDestroy == false then

			if (self.gemsTable[gem.i+1][gem.j]).gemType == gem.gemType then

			    self.markToDestroy( self.gemsTable[gem.i+1][gem.j] )
			end	 
		end
	end

	-- check above
	if gem.j>1 then
		if (self.gemsTable[gem.i][gem.j-1]).isMarkedToDestroy == false then

			if (self.gemsTable[gem.i][gem.j-1]).gemType == gem.gemType then

			   self.markToDestroy( self.gemsTable[gem.i][gem.j-1] )
			end	 
		end
	end

	-- check below
	if gem.j<8 then
		if (self.gemsTable[gem.i][gem.j+1]).isMarkedToDestroy== false then

			if (self.gemsTable[gem.i][gem.j+1]).gemType == gem.gemType then

			   self.markToDestroy( self.gemsTable[gem.i][gem.j+1] )
			end	 
		end
	end
end

local function sendData()
	self._listener( self.event )
	self.event = nil
	print("triggered")
end

local function sendEvent()
	print("start event")
	timer.performWithDelay(600, sendData)
end

function onGemTouch( gem, event )	-- was pre-declared
	if event.phase == "began" and isGemTouchEnabled then
		print("Gem touched i= "..gem.i.." j= "..gem.j)
		self.markToDestroy(gem)
		if numberOfMarkedToDestroy >= 3 then
			self.destroyGems()
			self.event = {}
			self.event.name = "gemTapped"
			self.event.params = { gem.i, gem.j, self.achievedScore }
			sendEvent()
			self.achievedScore = 0
			
		else 
			self.cleanUpGems()
		end
	end

return true
end

self.enableGemTouch = function()
	isGemTouchEnabled = true
end

self.destroyGems = function()
	print ("Destroying Gems. Marked to Destroy = "..numberOfMarkedToDestroy)


	for i = 1, 8, 1 do
		for j = 1, 8, 1 do
			isGemTouchEnabled = false
			if self.gemsTable[i][j].isMarkedToDestroy then

				
				transition.to( self.gemsTable[i][j], { time=250, alpha=0, xScale=0.1, onComplete=self.enableGemTouch } )
				
				-- update score
				self.achievedScore = self.achievedScore + 50
				
			end
		end
	end

	numberOfMarkedToDestroy = 0
	timer.performWithDelay( 300, self.shiftGems )
end

self.cleanUpGems = function()
	print("Cleaning Up Gems")
		
	numberOfMarkedToDestroy = 0
	
	for i = 1, 8, 1 do
		for j = 1, 8, 1 do
			
			-- show that there is not enough
			if self.gemsTable[i][j].isMarkedToDestroy then
				transition.to( self.gemsTable[i][j], { time=100, xScale=globalScale, yScale = globalScale } )
				transition.to( self.gemsTable[i][j], { time=100, delay=100, xScale=globalScale * 0.8, yScale = globalScale * 0.8} )
			end

			self.gemsTable[i][j].isMarkedToDestroy = false
			

		end
	end
end

return self
