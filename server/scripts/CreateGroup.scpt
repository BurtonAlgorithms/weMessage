on run {groupName, participants, message}
	set parentFolder to missing value

	tell application "Finder"
		set parentFolder to get (container of (path to me)) as text
	end tell

	set handlerLib to load script file (parentFolder & "Handlers.scpt")

	if handlerLib's isServerRunning() is equal to false then
		handlerLib's showServerNotRunningDialog()
		return
	end if

	tell handlerLib to foregroundApp("Messages", "Messages", true)
	set returnSet to handlerLib's createGroup(groupName, participants, message)

	return returnSet
end run