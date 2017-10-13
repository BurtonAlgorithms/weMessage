on run {buildVersion}
	set parentFolder to missing value

	tell application "Finder"
		set parentFolder to get (container of (path to me)) as text
	end tell

	set handlerLib to load script file (parentFolder & "Handlers.scpt")

	if handlerLib's isServerRunning() is equal to false then
		handlerLib's showServerNotRunningDialog()
		return
	end if

	set init to handlerLib's init(buildVersion)

	if init is equal to handlerLib's VERSION_MISMATCH then
		return init
	end if

	return handlerLib's prerequisites()
end run