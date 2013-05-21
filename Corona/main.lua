local library = require "plugin.library"
local widget = require "widget"

library.init()

local function onShowAchievements()
	library.show("achievements")
end

local function onShowLeaderBoards()
	library.show("leaderboards")
end

local function unlockAchievement( id )
	library.request("unlockAchievement", {achievement =
	{
		identifier = id
	}})
end

local function callbacker(event)
	print(event.response)
	for i, v in pairs(event.data) do
		
		if type(v) == "table" then
			print(i)
			for k, v2 in pairs(v) do
				print(k, ": ", v2)
			end
		elseif type(v) == "string" then
			print(i, ": ", v)
		end
	end
end

local function submitScore()
	library.request( 'loadScores',
    {
        leaderboard = { category="CgkI16K5tuQcEAIQBg", playerScope="Global", timeScope="AllTime", range={1, 25}},
        listener=callbacker
    })


	-- library.request("setHighScore", {localPlayerScore =
	-- {
	-- 	category = "CgkI16K5tuQcEAIQBg", value = "200000"
	-- }})
end






local achievementsTest = 
{
	"CgkI16K5tuQcEAIQAQ",
	"CgkI16K5tuQcEAIQAg",
	"CgkI16K5tuQcEAIQAw",
	"CgkI16K5tuQcEAIQBA",
	"CgkI16K5tuQcEAIQBQ"
}






local signIn
local createGSignInButton
local buttonWidth = display.viewableContentWidth - (2 * display.screenOriginX)

local function loginListener( event )
	print("type: ", event.type)
	print("isError: ", event.isError)
end

local function signInOut()
	if library.request("isConnected") then
		library.request("logout", {listener = loginListener})
		-- library.signout()
		createGSignInButton()
	else
		display.remove(signIn)
		signIn = widget.newButton
		{
			left = 0,
			top = 0,
			width = buttonWidth,
			height = 40,
			label = "Sign Out",
			onRelease = signInOut
		}
		-- library.signin()
		library.request("login", {listener = loginListener})
	end
end

createGSignInButton = function()
	display.remove(signIn)
	signIn = widget.newButton
	{
		left = 0,
		top = 0,
		width = buttonWidth,
		height = 40,
		defaultFile = "sign_in_normal.png",
		overFile = "sign_in_pressed.png",
		onRelease = signInOut 
	}
end

createGSignInButton()

local showAchBtn = widget.newButton
{ 
	left = 0,
	top = 40,
	width = buttonWidth,
	height = 40,
	label = "Show Achievements",
	onRelease = onShowAchievements 
}

local showLeaderBoardBtn = widget.newButton
{ 
	left = 0,
	top = 80,
	width = buttonWidth,
	height = 40,
	label = "Show LeaderBoards",
	onRelease = onShowLeaderBoards 
}

local createAchievement = widget.newButton
{
	left = 0,
	top = 120,
	width = buttonWidth,
	height = 40,
	label = "Submit Score",
	onRelease = submitScore 
}

local achievements = 
{
	["Answer 5"] = "CgkIu_yi1YUREAIQAQ",
	["Answer 10"] = "CgkIu_yi1YUREAIQAg",
	["Slow Poke"] = "CgkIu_yi1YUREAIQAw",
	["Just in Time"] = "CgkIu_yi1YUREAIQBA",
	["Love this Game"] = "CgkIu_yi1YUREAIQBQ",
	["Speedy Gonzales"] = "CgkIu_yi1YUREAIQCA"
}

local startTime = os.time()
local numberOfCorrectInRow = 0

-- main variables
my_player_score = 0
my_player_last_delta = 0

game_clock = 0
point_value = 0
value_a = 0; operand_1 = "+"; value_b = 0; operand_2 = "+"; value_c = 0
correct_answer = 0
answer_selection = { }
question_data = {}
round_in_session = false
get_ready = { "Get ready!", "Here it comes!", "Here we go!", "Are you ready?", "Let's do it!", "Bring it on!" }
times_up = { "Time's up!", "Not even a guess?", "Not sure, huh?", "Better luck next time!", "You'll get 'em next round!" }
correct = { "You are correct!", "Exactamundo!", "You got it!", "Alright!", "Brilliant!", "What a brain!", "Great, keep it up!", "Yes, that's it!" }
wrong = { "Sorry, wrong answer!", "Better luck next time!", "So close!", "You are incorrect.", "Oops!", "No, that's not it." }
button = {}
button_text = {}

math.randomseed (os.time())

-- shuffle answers
function shuffle( array )
	array2 = {}
	for i=1, #array do
		index = math.random(#array)
		grabbed_value = array[index]
		table.remove(array, index)
		array2[i] = grabbed_value
	end
	return array2
end


-- generate new math question
function generate_new_question()
	value_a = math.random(1,99)
	value_b = math.random(1,99)
	if math.random(2) ==1 then value_c = 0 else value_c = math.random(1,99) end
	if math.random(2) == 1 then operand_1 = "+" else operand_1 = "-" end
	if math.random(2) == 1 then operand_2 = "+" else operand_2 = "-" end
	-- compute the equation
	correct_answer = value_a
	if operand_1 == "+" then correct_answer = correct_answer + value_b else correct_answer = correct_answer - value_b end
	if operand_2 == "+" then correct_answer = correct_answer + value_c else correct_answer = correct_answer - value_c end		
	-- create five different choices
	answer_selection[1] = correct_answer - math.random(1,25)
	answer_selection[2] = answer_selection[1] - math.random(1,25)
	answer_selection[3] = correct_answer + math.random(1,25)
	answer_selection[4] = answer_selection[3] + math.random(1,25)
	answer_selection[5] = correct_answer

	answer_selection = shuffle (answer_selection)
	question_data = value_a .. " " .. operand_1 .. " " .. value_b
	if value_c > 0 then question_data = question_data .. " " .. operand_2 .. " " .. value_c end
	question_data = question_data .. "|" .. correct_answer .. ">"
	for i=1, 5 do
		question_data = question_data .. answer_selection[i] .. " "
	end
	return question_data
end

-- parse question string
function parse_question( text )
	math_question = string.sub ( text, 1, string.find ( text, "|" ) - 1 )
	print ( "Math question received" )
	answers = string.sub ( text, string.find ( text, ">" ) + 1)
	answer_selection = {}
	for value in string.gmatch(answers, "%S+") do
        answer_selection[ #answer_selection + 1 ] = value
	end
	correct_answer = string.sub ( text, string.find ( text, "|" ) + 1, string.find ( text, ">" ) - 1 ) 
end


-- show correct answer
function show_correct_answer()
	for i=1, 5 do
		if answer_selection[i] ~= correct_answer then
			button_text[i].alpha = 0.15
		end
	end
	old_text = equation_text.text
	equation_text.text = old_text .. " = " .. correct_answer
	transition.to (equation_text, { time = 1000, xScale = 0.45, yScale = 0.9 } )
end


-- reduce point value
function reduce_point_value()
	if round_in_session then
		point_value = point_value - point_reduction
		if point_value <= 0 then
			point_value = 0
			point_value_text.text = times_up[math.random(#times_up)]
			-- unlockAchievement(achievements["Slow Poke"])
			numberOfCorrectInRow = 0
			round_in_session = false
			show_correct_answer()
		else
			point_value_text.text = "Value: " .. comma_value(math.ceil(point_value))
		end
	end
end
	
		
-- start new round
function start_new_round()
	if value_c == 0 then point_value = 1000 else point_value = 3000 end
	point_reduction = point_value / 125
	round_in_session = true
	equation_text.text = math_question
	for i=1, 5 do
		button_text[i].text = answer_selection[i]
		button_text[i].alpha = 1
	end
	reset_equation_graphics()
	-- begin value countdown
	timer.performWithDelay (50, reduce_point_value, 125)
end


-- game clock loop
function game_clock_loop()
		if os.time() > startTime + 600 then
			-- unlockAchievement(achievements["Love this Game"])
		end

		game_clock = game_clock % 20 + 1
		game_clock_text.text = game_clock
 		if game_clock == 1 then
    		local questionData = generate_new_question()
    		parse_question(questionData)
    		start_new_round()
		elseif game_clock == 17 then
	 	   	point_value_text.text = get_ready[math.random(#get_ready)]
	 	   	for i=1, 5 do
	 	   		button_text[i].text = ""
	 	   	end
 	   end
	
	timer.performWithDelay ( 1000, game_clock_loop)
end


-- format number with commas
function comma_value(n) -- credit http://richard.warburton.it
	local left,num,right = string.match(n,'^([^%d]*%d)(%d*)(.-)$')
	return left..(num:reverse():gsub('(%d%d%d)','%1,'):reverse())..right
end


-- reset equation graphics
function reset_equation_graphics()
	equation_text.rotation = math.random(-60,60)
	equation_text.xScale = 1
	equation_text.yScale = 2
	equation_text.alpha = 1
	transition.to (equation_text, {time = 2000, rotation = math.random(-20,20), xScale = 0.5, yScale = 1, transition = easing.outExpo})
end


-- flash delta
function flash_delta()
    	temp_delta = comma_value(my_player_last_delta)
    	if my_player_last_delta > 0 then
    		temp_delta = "+" .. temp_delta
    	end
	delta_text = display.newText ( temp_delta, 0, 0, "Helvetica-Bold", 48)
	delta_text.x = 160
	function remove_delta()
		delta_text:removeSelf()
		delta_text = nil
	end
	if my_player_last_delta > 0 then
		delta_text:setTextColor (0,255,0)
		delta_text.y = point_value_text.y
		timer.performWithDelay ( 1000, function() your_score_text.text = "Your Score: " .. comma_value( my_player_score ) end)
		transition.to (delta_text, { time = 3000, alpha = 0, y = your_score_text.y, transition = easing.outExpo, onComplete = remove_delta })
	else
		delta_text:setTextColor (255,0,0)
		delta_text.y = your_score_text.y
		your_score_text.text = "Your Score: " .. comma_value( my_player_score )
		transition.to (delta_text, { time = 3000, alpha = 0, y = point_value_text.y, transition = easing.outExpo, onComplete = remove_delta })
	end
end


-- flash correct or wrong symbol
function flash_symbol( symbol_type, x, y )
	symbol = display.newImageRect ( symbol_type, 64, 64 )
	symbol.x = x
	symbol.y = y
	symbol.alpha = 1
	function delete_symbol()
		symbol:removeSelf()
		symbol = nil
	end
	transition.to ( symbol, { delay = 500, time = 1000, xScale = 3, yScale = 3, alpha = 0, onComplete = delete_symbol, transition = easing.inExpo } )
end


-- button handler
function button_handler( event )
	if round_in_session then
		if event.phase == "ended" then
			round_in_session = false
			show_correct_answer()

			if answer_selection[ event.target.id ] == correct_answer then
				my_player_score = my_player_score + point_value
				point_value_text.text = correct[math.random(#correct)]
				flash_symbol( "correct.png", button[event.target.id].x, button[event.target.id].y )

				numberOfCorrectInRow = numberOfCorrectInRow + 1
				
				if game_clock<2 then
					-- unlockAchievement(achievements["Speedy Gonzales"])
				end

				if numberOfCorrectInRow == 5 then
					-- unlockAchievement(achievements["Answer 5"])
				end

				if numberOfCorrectInRow == 10 then
					-- unlockAchievement(achievements["Answer 10"])
				end

				if point_value<150 then
					-- unlockAchievement(achievements["Just in Time"])
				end
			else
				point_value = -point_value
				my_player_score = my_player_score + point_value
				point_value_text.text = wrong[math.random(#wrong)]
				flash_symbol( "wrong.png", button[event.target.id].x, button[event.target.id].y )
				numberOfCorrectInRow = 0
			end
			my_player_last_delta = point_value
			timer.performWithDelay (2000, flash_delta)
		end
	end
	
	return true
end


-- set up the interface

point_value_text = display.newText ( "Connecting...", 0, 0, "Helvetica-Bold", 20)
point_value_text:setTextColor (255,255,0)
point_value_text.x = 160
point_value_text.y = 300

equation_text = display.newText ( "MATHEMANIA", 0, 0, "Helvetica-Bold",80)
equation_text:setTextColor (255,255,255)
equation_text.x = 160
equation_text.y = 230
reset_equation_graphics()

your_score_text = display.newText ( "Your Score: 0", 0, 0, "Helvetica-Bold", 24)
your_score_text:setTextColor (0,255,255)
your_score_text.x = 160
your_score_text.y = 460

game_clock_text = display.newText ( 0, 0, 0, "Helvetica-Bold", 12)
game_clock_text:setTextColor (64, 64, 64)
game_clock_text.x = 20
game_clock_text.y = 460

for i=1, 5 do
	button[i] = widget.newButton 
	{
		id = i,
        defaultFile = "button.png",
        overFile = "button_lit.png",
        onEvent = button_handler,
	}
	button[i].x = -32 + (64 * i)
	button[i].y = 380
	button_text[i] = display.newText ( "", 0, 0, "Helvetica-Bold", 20)
	button_text[i].x = button[i].x
	button_text[i].y = button[i].y	
end

-- start the loop
game_clock_loop()
