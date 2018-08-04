on run {savePictures}
	set parentFolder to missing value

	tell application "Finder"
		set parentFolder to get (container of (path to me)) as text
	end tell

	set handlerLib to load script file (parentFolder & "Handlers.scpt")

	if handlerLib's isServerRunning() is equal to false then
		handlerLib's showServerNotRunningDialog()
		return
	end if

	set returnSet to handlerLib's syncContacts(savePictures)
	return returnSet
end run