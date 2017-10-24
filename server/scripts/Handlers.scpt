property WEMESSAGE_APPLESCRIPT_VERSION : 2
property WEMESSAGE_JAVASCRIPT_VERSION : 1
property VERSION_MISMATCH : 998
property UNKNOWN_ERROR : 999
property SENT : 1000
property INVALID_NUMBER : 1004
property NUMBER_NOT_IMESSAGE : 1005
property GROUP_CHAT_NOT_FOUND : 1006
property NOT_SENT : 1008
property SERVICE_NOT_AVAILABLE : 1009
property FILE_NOT_FOUND : 1010
property NULL_MESSAGE : 1011
property ASSISTIVE_ACCESS_DISABLED : 1012
property UI_ERROR : 1013
property ACTION_PERFORMED : 1014

property INTERNAL_JAVASCRIPT_ERROR : 1015
property INTERNAL_NO_MENU : 1016
property INTERNAL_JAVASCRIPT_FILE : "JSSendMessage.js"



on init(buildVersion)
	if buildVersion as integer is not equal to WEMESSAGE_APPLESCRIPT_VERSION then
		return VERSION_MISMATCH
	else
		return ACTION_PERFORMED
	end if
end init



on sendMessage(phoneNumber, fileLocation, targetMessage)
	set returnSet to {}

	set handle to missing value

	try
		set getHandleJs to "Application('Messages').buddies.whose({ name: '" & phoneNumber & "' })[0].handle()"
		set handle to do shell script "osascript -l JavaScript -e \"" & getHandleJs & "\""

		if my isNull(handle) is equal to true then
			set handle to phoneNumber
		end if
	on error
		set handle to phoneNumber
	end try

	if my isNull(fileLocation) is equal to false then
		set end of returnSet to sendMessageFile(handle, fileLocation)
	else
		set end of returnSet to NULL_MESSAGE
	end if

	if my isNull(targetMessage) is equal to false then
		set end of returnSet to sendTextMessage(handle, targetMessage)
	else
		set end of returnSet to NULL_MESSAGE
	end if

	return returnSet
end sendMessage



on sendGroupMessage(algorithmRow, groupGuid, groupNameCheck, noNameFlag, fileLocation, targetMessage)

	set useJsReturn to null
	set jsReturn to do shell script "osascript '" & getScriptsRootPath() & INTERNAL_JAVASCRIPT_FILE & "' " & WEMESSAGE_JAVASCRIPT_VERSION & " 1 \"" & groupGuid & "\" \"" & fileLocation & "\" \"" & targetMessage & "\""

	set {textDelimiters, AppleScript's text item delimiters} to {AppleScript's text item delimiters, {", "}}
	set jsReturnSet to text items of jsReturn
	set AppleScript's text item delimiters to textDelimiters

	if (count of jsReturnSet) is less than 2 then
		set useJsReturn to false
	else
		if item 1 of jsReturnSet as integer is equal to INTERNAL_JAVASCRIPT_ERROR then
			set useJsReturn to false

		else if item 2 of jsReturnSet as integer is equal to INTERNAL_JAVASCRIPT_ERROR then

			set uiPreconditionsVal to UIPreconditions()

			if (uiPreconditionsVal is not equal to ACTION_PERFORMED) then
				set item 2 of jsReturnSet to uiPreconditionsVal

			else
				if isNull(targetMessage) is equal to false then
					delay 0.1
					set item 2 of jsReturnSet to sendGroupTextMessage(algorithmRow, groupNameCheck, noNameFlag, targetMessage)
				else
					set item 2 of jsReturnSet to NULL_MESSAGE
				end if
			end if

			set useJsReturn to true
		else
			set useJsReturn to true
		end if
	end if

	if useJsReturn is equal to true then
		return jsReturnSet
	end if

	set returnSet to {}

	set uiPreconditionsVal to UIPreconditions()
	if (uiPreconditionsVal is not equal to ACTION_PERFORMED) then return uiPreconditionsVal

	if isNull(fileLocation) is equal to false then
		set end of returnSet to sendGroupMessageFile(algorithmRow, groupNameCheck, noNameFlag, fileLocation)
	else
		set end of returnSet to NULL_MESSAGE
	end if

	if isNull(targetMessage) is equal to false then
		delay 0.1
		set end of returnSet to sendGroupTextMessage(algorithmRow, groupNameCheck, noNameFlag, targetMessage)
	else
		set end of returnSet to NULL_MESSAGE
	end if

	return returnSet
end sendGroupMessage



on renameGroup(algorithmRow, groupNameCheck, noNameFlag, newGroupTitle)
	set uiPreconditionsVal to UIPreconditions()
	if (uiPreconditionsVal is not equal to ACTION_PERFORMED) then return uiPreconditionsVal

	set groupChat to findGroupRow(algorithmRow, groupNameCheck, noNameFlag)

	if groupChat is equal to UI_ERROR then
		return UI_ERROR
	else if groupChat is equal to missing value then
		return GROUP_CHAT_NOT_FOUND
	end if

	tell application "System Events"
		tell process "Messages"
			select groupChat
			try
				tell window 1 to tell splitter group 1 to tell button "Details"
					click
					tell pop over 1 to tell scroll area 1 to tell text field 1
						set value to newGroupTitle
						confirm
					end tell
					click
				end tell
			on error errorMessage
				my logError("RenameGroup.scpt", errorMessage)
				key code 53
				return UI_ERROR
			end try
		end tell
	end tell
	return ACTION_PERFORMED
end renameGroup



on addParticipantToGroup(algorithmRow, groupNameCheck, noNameFlag, phoneNumber)
	if isNull(phoneNumber) is equal to true then
		return INVALID_NUMBER
	end if

	set uiPreconditionsVal to UIPreconditions()
	if (uiPreconditionsVal is not equal to ACTION_PERFORMED) then return uiPreconditionsVal

	set isNumberIMessageReturn to isNumberIMessage(phoneNumber)

	if isNumberIMessageReturn is equal to UI_ERROR then
		return UI_ERROR

	else if isNumberIMessageReturn is equal to INTERNAL_NO_MENU then
		set groupChat to findGroupRow(algorithmRow, groupNameCheck, noNameFlag)

		if groupChat is equal to UI_ERROR then
			return UI_ERROR
		else if groupChat is equal to missing value then
			return GROUP_CHAT_NOT_FOUND
		end if

		tell application "System Events"
			tell process "Messages"
				select groupChat
				try
					tell window 1 to tell splitter group 1 to tell button "Details"
						click

						tell pop over 1 to tell scroll area 1 to tell text field 2
							set value to phoneNumber
							set focused to true
							key code 124
							keystroke space
							keystroke (ASCII character 8)
							delay 0.2
							keystroke return
							key code 53
						end tell
					end tell

					delay 1
					set totalWindows to count windows

					if totalWindows is greater than 1 then
						repeat (totalWindows - 1) times
							try
								tell button 1 of window 1 to perform action "AXPress"
							on error
								exit repeat
							end try
						end repeat

						return NUMBER_NOT_IMESSAGE
					else
						try
							tell button 1 of sheet 1 of window 1 to perform action "AXPress"

							return NUMBER_NOT_IMESSAGE
						on error
							return ACTION_PERFORMED
						end try
					end if
				on error errorMessage
					key code 53
					my logError("AddParticipant.scpt", errorMessage)
					return UI_ERROR
				end try
			end tell
		end tell

	else if isNumberIMessageReturn is not equal to true then
		return NUMBER_NOT_IMESSAGE
	end if

	set groupChat to findGroupRow(algorithmRow, groupNameCheck, noNameFlag)

	if groupChat is equal to UI_ERROR then
		return UI_ERROR
	else if groupChat is equal to missing value then
		return GROUP_CHAT_NOT_FOUND
	end if

	tell application "System Events"
		tell process "Messages"
			select groupChat
			try
				tell window 1 to tell splitter group 1 to tell button "Details"
					click

					tell pop over 1 to tell scroll area 1 to tell text field 2
						set value to phoneNumber
						set focused to true
						key code 124
						keystroke space
						keystroke (ASCII character 8)
						delay 0.2
						keystroke return
						key code 53
						return ACTION_PERFORMED
					end tell
				end tell
			on error errorMessage
				key code 53
				my logError("AddParticipant.scpt", errorMessage)
				return UI_ERROR
			end try
		end tell
	end tell
end addParticipantToGroup



on removeParticipantFromGroup(algorithmRow, groupNameCheck, noNameFlag, phoneNumber)
	if isNull(phoneNumber) is equal to true then
		return INVALID_NUMBER
	end if

	set uiPreconditionsVal to UIPreconditions()
	if (uiPreconditionsVal is not equal to ACTION_PERFORMED) then return uiPreconditionsVal

	set groupChat to findGroupRow(algorithmRow, groupNameCheck, noNameFlag)

	if groupChat is equal to UI_ERROR then
		return UI_ERROR
	else if groupChat is equal to missing value then
		return GROUP_CHAT_NOT_FOUND
	end if

	set contactName to missing value
	set contactRow to missing value

	try
		set getNameJs to "Application('Messages').buddies.whose({ handle: '" & phoneNumber & "' })[0].name()"
		set contactName to do shell script "osascript -l JavaScript -e \"" & getNameJs & "\""

		if isNull(contactName) then
			set contactName to phoneNumber
		end if
	on error
		set contactName to phoneNumber
	end try

	tell application "System Events"
		tell process "Messages"
			select groupChat
			try
				tell window 1 to tell splitter group 1 to tell button "Details"
					click
					tell pop over 1 to tell scroll area 1
						repeat with theRow in (table 1's entire contents) as list
							if theRow's class is row then
								if name of theRow's UI element 1 is equal to contactName then
									set contactRow to theRow
									exit repeat
								end if
							end if
						end repeat

						if contactRow is equal to missing value then
							key code 53
							return INVALID_NUMBER
						end if

						select contactRow
						delay 0.1
						keystroke (ASCII character 8)
						delay 0.1
						key code 53
						return ACTION_PERFORMED
					end tell
				end tell
			on error errorMessage
				key code 53
				my logError("RemoveParticipant.scpt", errorMessage)
				return UI_ERROR
			end try
		end tell
	end tell
end removeParticipantFromGroup



on createGroup(groupName, participants, targetMessage)
	set uiPreconditionsVal to UIPreconditions()
	if (uiPreconditionsVal is not equal to ACTION_PERFORMED) then return uiPreconditionsVal

	try
		tell application "System Events" to tell process "Messages"
			set theTextField to missing value

			tell window 1 to tell splitter group 1 to tell button 1 to click

			try
				set theTextField to text field 1 of scroll area 3 of splitter group 1 of window 1
			on error
				set theTextField to text field 1 of scroll area 4 of splitter group 1 of window 1
			end try

			tell window 1
				tell theTextField
					set value to participants
					keystroke ","
				end tell

				set theTextArea to missing value

				try
					set theTextArea to text area 1 of scroll area 4 of splitter group 1
				on error
					set theTextArea to text area 1 of scroll area 3 of splitter group 1
				end try

				tell theTextArea
					select
					set value to targetMessage
					keystroke return
					keystroke return
				end tell
				delay 0.2
				try
					tell splitter group 1 to tell button "Details"
						click
						tell pop over 1 to tell scroll area 1 to tell text field 1
							set value to groupName
							confirm
						end tell
						click
					end tell
					return ACTION_PERFORMED
				on error errorMessage
					delay 1

					if my createGroupErrorHelper() is equal to true then
						return NUMBER_NOT_IMESSAGE
					else
						key code 53
						my logError("CreateGroup.scpt", errorMessage)
						return UI_ERROR
					end if
				end try
			end tell
		end tell
	on error errorMessage
		my logError("CreateGroup.scpt", errorMessage)
		return UI_ERROR
	end try
end createGroup



on leaveGroup(algorithmRow, groupNameCheck, noNameFlag)
	set uiPreconditionsVal to UIPreconditions()
	if (uiPreconditionsVal is not equal to ACTION_PERFORMED) then return uiPreconditionsVal

	set groupChat to findGroupRow(algorithmRow, groupNameCheck, noNameFlag)

	if groupChat is equal to UI_ERROR then
		return UI_ERROR
	else if groupChat is equal to missing value then
		return GROUP_CHAT_NOT_FOUND
	end if

	tell application "System Events"
		tell process "Messages"
			select groupChat
			try
				tell window 1 to tell splitter group 1 to tell button "Details"
					click
					tell pop over 1 to tell scroll area 1
						tell button 1
							click
						end tell
						key code 53
						return ACTION_PERFORMED
					end tell
				end tell
			on error errorMessage
				key code 53
				my logError("LeaveGroup.scpt", errorMessage)
				return UI_ERROR
			end try
		end tell
	end tell
end leaveGroup



on sendTextMessage(phoneNumber, targetMessage)
	tell application "Messages"
		try
			set targetService to 1st service whose service type = iMessage
		on error errorMessage
			my logError("SendMessage.scpt", errorMessage)
			return SERVICE_NOT_AVAILABLE
		end try
		try
			set targetPerson to buddy phoneNumber of targetService
		on error errorMessage
			my logError("SendMessage.scpt", errorMessage)
			return INVALID_NUMBER
		end try
		try
			send targetMessage to targetPerson
			return SENT
		on error errorMessage
			if errorMessage contains "get buddy id" then
				return INVALID_NUMBER
			else
				my logError("SendMessage.scpt", errorMessage)
				return NOT_SENT
			end if
		end try
	end tell
end sendTextMessage



on sendGroupTextMessage(algorithmRow, groupNameCheck, noNameFlag, targetMessage)
	set groupChat to findGroupRow(algorithmRow, groupNameCheck, noNameFlag)
	set classNames to {}

	if groupChat is equal to UI_ERROR then
		return UI_ERROR
	else if groupChat is equal to missing value then
		return GROUP_CHAT_NOT_FOUND
	end if

	try
		tell application "System Events"
			tell process "Messages"
				select groupChat

				set theTextArea to missing value

				try
					set theTextArea to text area 1 of scroll area 4 of splitter group 1 of window 1
				on error
					set theTextArea to text area 1 of scroll area 3 of splitter group 1 of window 1
				end try

				tell theTextArea
					set value to targetMessage
					keystroke return
					return SENT
				end tell
			end tell
		end tell
	on error errorMessage
		my logError("SendGroupMessage.scpt", errorMessage)
		return UNKNOWN_ERROR
	end try
end sendGroupTextMessage



on sendMessageFile(phoneNumber, fileLocation)
	tell application "Messages"
		try
			set targetService to 1st service whose service type = iMessage
		on error errorMessage
			my logError("SendMessage.scpt", errorMessage)
			return SERVICE_NOT_AVAILABLE
		end try
		try
			set targetPerson to buddy phoneNumber of targetService
		on error errorMessage
			my logError("SendMessage.scpt", errorMessage)
			return INVALID_NUMBER
		end try
		try
			set targetAttachment to POSIX file (fileLocation) as alias
		on error errorMessage
			my logError("SendMessage.scpt", errorMessage)
			return FILE_NOT_FOUND
		end try
		try
			send targetAttachment to targetPerson
			return SENT
		on error errorMessage
			if errorMessage contains "get buddy id" then
				return INVALID_NUMBER
			else
				my logError("SendMessage.scpt", errorMessage)
				return NOT_SENT
			end if
		end try
	end tell
end sendMessageFile



on sendGroupMessageFile(algorithmRow, groupNameCheck, noNameFlag, fileLocation)
	set groupChat to findGroupRow(algorithmRow, groupNameCheck, noNameFlag)
	set classNames to {}

	if groupChat is equal to UI_ERROR then
		return UI_ERROR
	else if groupChat is equal to missing value then
		return GROUP_CHAT_NOT_FOUND
	end if

	try
		tell application "Finder"
			if exists fileLocation as POSIX file then
				set the clipboard to ((fileLocation) as POSIX file)
			else
				my logError("SendGroupMessage.scpt", "The file " & fileLocation & " could not be found on this system.")
				return FILE_NOT_FOUND
			end if
		end tell
	on error errorMessage
		my logError("SendGroupMessage.scpt", errorMessage)
		return FILE_NOT_FOUND
	end try
	delay 0.1
	try
		tell application "System Events"
			my foregroundApp("Messages", "Messages", true)
			tell process "Messages"
				select groupChat

				set theTextArea to missing value

				try
					set theTextArea to text area 1 of scroll area 4 of splitter group 1 of window 1
				on error
					set theTextArea to text area 1 of scroll area 3 of splitter group 1 of window 1
				end try

				tell theTextArea
					keystroke "v" using command down
					keystroke return
					return SENT
				end tell
			end tell
		end tell
	on error errorMessage
		my logError("SendGroupMessage.scpt", errorMessage)
		return UNKNOWN_ERROR
	end try
end sendGroupMessageFile



on createGroupErrorHelper()
	tell application "System Events" to tell process "Messages"
		set totalWindows to count windows

		if totalWindows is greater than 1 then
			repeat (totalWindows - 1) times
				try
					tell button 1 of window 1 to perform action "AXPress"
				on error
					exit repeat
				end try
			end repeat

			return true
		else
			try
				tell button 1 of sheet 1 of window 1 to perform action "AXPress"
				return true
			on error
				return false
			end try
		end if
	end tell
end createGroupErrorHelper



on findGroupRow(algorithmRow, groupNameCheck, noNameFlag)
	try
		tell application "System Events"
			tell process "Messages"
				set newMessageCounter to 0
				repeat with theRow in ((table 1 of scroll area 1 of splitter group 1 of window 1)'s entire contents as list)
					if theRow's class is row then
						set fullName to (theRow's UI element 1)'s description

						if fullName contains "New message" then
							if fullName does not contain ". Last message: " then
								set newMessageCounter to newMessageCounter + 1
							else
								exit repeat
							end if
						else
							exit repeat
						end if
					end if
				end repeat

				repeat newMessageCounter times
					select row 1 in table 1 of scroll area 1 of splitter group 1 of window 1
					key code 51 using {command down}
					delay 0.1
				end repeat

				set theRow to (algorithmRow + 1)

				if noNameFlag as boolean is equal to true then
					set groupParticipantList to {}
					set rowParticipantList to {}

					tell table 1 of scroll area 1 of splitter group 1 of window 1 to select row theRow
					tell window 1 to tell splitter group 1 to tell button "Details"
						click

						tell pop over 1 to tell scroll area 1
							repeat with contactRow in (table 1's entire contents) as list
								if contactRow's class is row then
									set getHandleJs to "Application('Messages').buddies.whose({ name: '" & (name of contactRow's UI element 1) & "' })[0].handle()"
									set contactHandle to do shell script "osascript -l JavaScript -e \"" & getHandleJs & "\""

									set end of rowParticipantList to contactHandle
								end if
							end repeat

							key code 53
						end tell
					end tell

					set {textDelimiters, AppleScript's text item delimiters} to {AppleScript's text item delimiters, {","}}
					set groupParticipantList to text items of groupNameCheck
					set AppleScript's text item delimiters to textDelimiters

					delay 0.5
					if my checkListEquivalence(groupParticipantList, rowParticipantList) is equal to true then
						return row theRow of table 1 of scroll area 1 of splitter group 1 of window 1
					else
						return missing value
					end if
				end if

				tell table 1 of scroll area 1 of splitter group 1 of window 1
					if (row theRow's UI element 1)'s description contains ". Has unread messages. " then
						select row theRow
					end if

					set fullName to (row theRow's UI element 1)'s description
					set finalName to (item 1 of my split(fullName, ". Last message: "))

					if finalName is equal to groupNameCheck then
						return row theRow
					end if
				end tell

				set returnRow to missing value
				repeat with theRow in ((table 1 of scroll area 1 of splitter group 1 of window 1)'s entire contents as list)
					if theRow's class is row then
						if (theRow's UI element 1)'s description contains ". Has unread messages. " then
							select theRow
						end if

						set fullName to (theRow's UI element 1)'s description
						set finalName to (item 1 of my split(fullName, ". Last message: "))

						if finalName is equal to groupNameCheck then
							set returnRow to theRow
							exit repeat
						end if
					end if
				end repeat

				return returnRow
			end tell
		end tell
	on error errorMessage
		my logError("Handlers.scpt", errorMessage)
		return UI_ERROR
	end try
end findGroupRow



on isNumberIMessage(phoneNumber)
	try
		set theTextField to missing value

		tell application "System Events" to tell process "Messages" to tell window 1 to tell splitter group 1 to tell button 1 to click

		tell application "System Events" to tell process "Messages"
			try
				set theTextField to text field 1 of scroll area 3 of splitter group 1 of window 1
			on error
				set theTextField to text field 1 of scroll area 4 of splitter group 1 of window 1
			end try
		end tell

		tell application "System Events" to tell process "Messages"
			tell window 1
				tell theTextField
					set value to phoneNumber
					keystroke ","
					keystroke (ASCII character 8)
				end tell
			end tell
		end tell

		ignoring application responses
			tell application "System Events" to tell process "Messages" to tell theTextField
				perform action "AXShowMenu" of menu button 1
			end tell
		end ignoring

		delay 0.05
		do shell script "killall System\\ Events"
		delay 0.05

		tell application "System Events" to tell process "Messages"
			tell theTextField
				try
					set menuItemTitle to title of menu item 2 of menu 1
				on error
					keystroke return
					return INTERNAL_NO_MENU
				end try
				try
					set finalString to my combine((text -5 thru -1 of my split(menuItemTitle, " ")), " ")
				on error
					set finalString to " "
				end try
				keystroke return
				keystroke (ASCII character 8)
				if finalString is equal to "is not registered with iMessage." then
					return false
				else
					return true
				end if
			end tell
		end tell
	on error errorMessage
		my logError("Handlers.scpt", errorMessage)
		return UI_ERROR
	end try
end isNumberIMessage



on isServerRunning()
	set jpsOutput to do shell script "jps -v"

	if jpsOutput contains "-Dname=weServer" then
		return true
	else
		return false
	end if
end isServerRunning



on prerequisites()
	if hasAssistiveAccess() is equal to false then
		return ASSISTIVE_ACCESS_DISABLED
	end if

	return ACTION_PERFORMED
end prerequisites



on UIPreconditions()
	set value to my prerequisites()
	if (value is not equal to ACTION_PERFORMED) then return value

	set windowCount to 0

	tell application "System Events"
		tell process "Messages"
			set windowCount to count windows
		end tell

		if windowCount is equal to 0 then my respringMessages()
		if windowCount is greater than 1 then my respringMessages()
	end tell

	return ACTION_PERFORMED
end UIPreconditions



on hasAssistiveAccess()
	tell application "System Events"
		set uiEnabled to UI elements enabled
	end tell
	if uiEnabled is false then
		tell application "System Preferences"
			reopen
			set securityPane to pane id "com.apple.preference.security"
			tell securityPane to reveal anchor "Privacy_Accessibility"
			activate
			display dialog "In order to send group messages, weServer uses macOS' GUI scripting feature (which is controlled by assistive access), which is currently disabled." & return & return & "You can enable assistive access by pressing \"Click the lock to make changes\", adding, and checking the app that launched the weServer (often the Terminal app.)" with icon file (my getProjectRoot() & "assets:AppLogo.png") buttons {"Okay"} giving up after 12
			return false
		end tell
	else
		return true
	end if
end hasAssistiveAccess



on showServerNotRunningDialog()
	display dialog "This script cannot run without the weMessage Server. Please turn on the server before running message scripts." with icon file (getProjectRoot() & "assets:AppLogo.png") buttons {"Okay"} giving up after 20
end showServerNotRunningDialog



on logError(callScript, theError)
	set outputFile to ((POSIX path of getProjectRoot()) & "scriptError-" & generateRandom(12))
	set sanitizedError to do shell script "echo " & quoted form of theError & " | tr -d '\"' "
	set theString to "{\"callScript\":\"" & callScript & "\", \"error\":\"" & sanitizedError & "\"}"
	do shell script "echo " & quoted form of theString & " > " & quoted form of outputFile
end logError



on activateApp(theApp)
	if isAppRunning(theApp) is not equal to true then
		tell application theApp to activate
	end if
end activateApp



on respringMessages()
	delay 0.1

	if isAppRunning("Messages") is equal to true then
		tell application "Messages"
			close windows
			quit
		end tell
	end if

	delay 0.1

	activateApp("Messages")
end respringMessages



on foregroundApp(theApp, theProcess, theBoolean)
	if theBoolean is equal to true then
		activateApp(theApp)
	end if

	if theApp is equal to "Finder" then
		tell application "Finder"
			if theBoolean is equal to true then
				set collapsed of windows to false
			else
				set collapsed of windows to true
			end if
		end tell
	else
		tell application theApp
			if theBoolean is equal to true then
				set miniaturized of windows to false
				delay 0.2
			else
				delay 0.5
				set miniaturized of windows to true
			end if
		end tell
		if theBoolean is equal to true then
			tell application "System Events"
				tell process theProcess
					set frontmost to true
				end tell
			end tell
		end if
	end if
end foregroundApp



on isAppRunning(targetApp)
	tell application "System Events"
		set processExists to exists process targetApp
	end tell
end isAppRunning



on getProjectRoot()
	tell application "Finder"
		set parentFolder to get (container of (container of (path to me))) as text
	end tell
end getProjectRoot



on getScriptsRoot()
	tell application "Finder"
		set parentFolder to get (container of (path to me)) as text
	end tell
end getScriptsRoot



on getScriptsRootPath()
	tell application "Finder"
		return (POSIX path of (parent of (path to me) as string))
	end tell
end getScriptsRootPath



on split(theString, theDelimiter)
	set oldDelimiters to AppleScript's text item delimiters
	set AppleScript's text item delimiters to theDelimiter
	set theArray to (every text item in theString) as list
	set AppleScript's text item delimiters to oldDelimiters

	return theArray
end split



on combine(theList, theDelimiter)
	set oldDelimiters to AppleScript's text item delimiters
	set AppleScript's text item delimiters to theDelimiter
	set theListAsString to theList as text
	set AppleScript's text item delimiters to oldDelimiters

	return theListAsString
end combine



on isNull(theString)
	if theString is equal to missing value then
		return true
	end if
	if theString is equal to null then
		return true
	end if
	if theString as text is equal to "null" then
		return true
	end if
	if theString as text is equal to "" then
		return true
	end if
	return false
end isNull



on checkListEquivalence(listOne, listTwo)
	if (count of listOne) is not equal to (count of listTwo) then return false

	repeat with theItem in listOne
		if theItem is not in listTwo then
			return false
		end if
	end repeat

	return true
end checkListEquivalence



on generateRandom(theLength)
	set chars to {"1", "2", "3", "4", "5", "6", "7", "8", "9", "0", "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z"}

	set returnString to ""

	repeat theLength times
		set returnString to returnString & some item of chars
	end repeat

	return returnString
end generateRandom