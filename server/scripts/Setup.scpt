on run
	set parentFolder to missing value

	tell application "Finder"
		set parentFolder to get (container of (path to me)) as text
	end tell

	set handlerLib to load script file (parentFolder & "Handlers.scpt")
	set isServerRunning to handlerLib's isServerRunning()

	return handlerLib's prerequisites()
end run