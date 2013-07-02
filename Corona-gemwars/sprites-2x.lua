--
-- created with TexturePacker (http://www.codeandweb.com/texturepacker)
--
-- $TexturePacker:SmartUpdate:b717ddb2b907b6e95bd986a88688fe57$
--
-- local sheetInfo = require("mysheet")
-- local myImageSheet = graphics.newImageSheet( "mysheet.png", sheetInfo:getSheet() )
-- local sprite = display.newSprite( myImageSheet , {frames={sheetInfo:getFrameIndex("sprite")}} )
--

local SheetInfo = {}

SheetInfo.sheet =
{
    frames = {
    
        {
            -- bg@2x
            x=2,
            y=2,
            width=640,
            height=960,

        },
        {
            -- blueball@2x
            x=848,
            y=102,
            width=100,
            height=96,

            sourceX = 0,
            sourceY = 4,
            sourceWidth = 100,
            sourceHeight = 100
        },
        {
            -- greenball@2x
            x=848,
            y=2,
            width=100,
            height=98,

            sourceX = 0,
            sourceY = 2,
            sourceWidth = 100,
            sourceHeight = 100
        },
        {
            -- redball@2x
            x=746,
            y=2,
            width=100,
            height=100,

        },
        {
            -- yellowball@2x
            x=644,
            y=2,
            width=100,
            height=100,

        },
        {
            -- logo@2x
            x=2,
            y=964,
            width=558,
            height=354,

            sourceX = 16,
            sourceY = 26,
            sourceWidth = 590,
            sourceHeight = 380
        },
    },
    
    sheetContentWidth = 1024,
    sheetContentHeight = 2048
}

SheetInfo.frameIndex =
{

    ["bg@2x"] = 1,
    ["blueball@2x"] = 2,
    ["greenball@2x"] = 3,
    ["redball@2x"] = 4,
    ["yellowball@2x"] = 5,
    ["logo@2x"] = 6,
}

function SheetInfo:getSheet()
    return self.sheet;
end

function SheetInfo:getFrameIndex(name)
    return self.frameIndex[name];
end

return SheetInfo
