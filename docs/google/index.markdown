# CoronaProvider.ads.inmobi.*

> --------------------- ------------------------------------------------------------------------------------------
> __Type__              [library][api.type.library]
> __Revision__          [REVISION_LABEL](REVISION_URL)
> __Keywords__          ads, inmobi, provider
> __Sample code__       
> __See also__          
> __Availability__      Starter, Pro, Enterprise
> --------------------- ------------------------------------------------------------------------------------------

## Overview

## Sign Up

To use the SERVICE_NAME service, please [sign up](CORONA_REFERRAL_URL) for an account.

## Platforms

The following platforms are supported:

* Android
* iOS

## Syntax

You access this plugin by passing the appropriate provider name to the [ads library][api.library.ads]:

	local ads = require "ads"
	ads.init( "inmobi", appID, adListener )

## Functions

The inmobi plugin does not provide any additional functionality beyond the core functionality provided by the [ads library][api.library.ads].

#### [ads library][api.library.ads.init]:

#### [ads library][api.library.ads.show]:

#### [ads library][api.library.ads.hide]:

## Project Settings

### SDK

When you build using the Corona Simulator, the server automatically takes care of integrating the plugin into your project. 

All you need to do is add an entry into a `plugins` table of your `build.settings`. The following is an example of a minimal `build.settings` file:

``````
settings =
{
	plugins =
	{
		-- key is the name passed to Lua's 'require()'
		["CoronaProvider.ads.inmobi"] =
		{
			-- required
			publisherId = "com.coronalabs",
		},
	},		
}

``````

### Enterprise

TBD

## Sample Code

You can access sample code [here](SAMPLE_CODE_URL).

## Support

More support is available from the InMobi team:

* [http://inmobi.com](http://inmobi.com)
