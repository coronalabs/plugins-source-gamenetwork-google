--
-- created with TexturePacker (http://www.codeandweb.com/texturepacker)
--
-- $TexturePacker:SmartUpdate:ceb5655d19921868929dae6efaa35799$
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
            -- bg
            x=2,
            y=2,
            width=320,
            height=480,

        },
        {
            -- blueball
            x=428,
            y=52,
            width=50,
            height=48,

            sourceX = 0,
            sourceY = 2,
            sourceWidth = 50,
            sourceHeight = 50
        },
        {
            -- greenball
            x=428,
            y=2,
            width=50,
            height=48,

            sourceX = 0,
            sourceY = 2,
            sourceWidth = 50,
            sourceHeight = 50
        },
        {
            -- redball
            x=376,
            y=2,
            width=50,
            height=50,

        },
        {
            -- yellowball
            x=324,
            y=2,
            width=50,
            height=50,

        },
        {
            -- logo
            x=2,
            y=484,
            width=279,
            height=178,

            sourceX = 8,
            sourceY = 12,
            sourceWidth = 295,
            sourceHeight = 190
        },
    },
    
    sheetContentWidth = 512,
    sheetContentHeight = 1024
}

SheetInfo.frameIndex =
{

    ["bg"] = 1,
    ["blueball"] = 2,
    ["greenball"] = 3,
    ["redball"] = 4,
    ["yellowball"] = 5,
    ["logo"] = 6,
}

function SheetInfo:getSheet()
    return self.sheet;
end

function SheetInfo:getFrameIndex(name)
    return self.frameIndex[name];
end

return SheetInfo
