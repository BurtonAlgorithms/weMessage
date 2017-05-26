on run {groupName, lastUpdated, lastMessage, newTitle}
	set parentFolder to missing value

	tell application "Finder"
		set parentFolder to get (container of (path to me)) as text
	end tell

	set handlerLib to load script file (parentFolder & "Handlers.scpt")
	set isServerRunning to handlerLib's isServerRunning()

	if isServerRunning is equal to false then
		display dialog "This script cannot run without the weMessage Server. Please turn on the server before running message scripts." with icon file (handlerLib's getProjectRoot() & "assets:AppLogo.png") buttons {"Okay"} giving up after 20
		return
	end if

	tell handlerLib to foregroundApp("Messages", "Messages", true)
	set returnSet to handlerLib's renameGroup(groupName, lastUpdated, lastMessage, newTitle)
	tell application "System Events"
		tell application process "Messages"
			keystroke "m" using {command down}
		end tell
	end tell

	return returnSet
end run