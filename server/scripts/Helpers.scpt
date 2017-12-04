on run {handlerCode}
	set parentFolder to missing value

	tell application "Finder"
		set parentFolder to get (container of (path to me)) as text
	end tell

	set handlerLib to load script file (parentFolder & "Handlers.scpt")

	if handlerLib's isServerRunning() is equal to false then
		handlerLib's showServerNotRunningDialog()
		return
	end if

	if handlerCode as integer is equal to 1 then
		startMessagesApp()
	else if handlerCode as integer is equal to 2 then
		killMessagesApp()
	end if
end run


on startMessagesApp()
	if isAppRunning("Messages") is not equal to true then
		tell application "Messages" to activate
	end if
end startMessagesApp


on killMessagesApp()
	if isAppRunning("Messages") then
		tell application "Messages" to quit
	end if
end killMessagesApp


on isAppRunning(targetApp)
	tell application "System Events"
		set processExists to exists process targetApp
	end tell
end isAppRunning