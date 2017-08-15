
property WEMESSAGE_VERSION : "Alpha 0.1"
property WEMESSAGE_NUMERIC_VERSION : 1
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



on sendMessage(phoneNumber, fileLocation, targetMessage)
	set returnSet to {}

	if my isNull(fileLocation) is equal to false then
		set end of returnSet to sendMessageFile(phoneNumber, fileLocation)
	else
		set end of returnSet to NULL_MESSAGE
	end if

	if my isNull(targetMessage) is equal to false then
		set end of returnSet to sendTextMessage(phoneNumber, targetMessage)
	else
		set end of returnSet to NULL_MESSAGE
	end if

	return returnSet
end sendMessage



on sendGroupMessage(groupName, lastUpdated, lastMessage, fileLocation, targetMessage)
	set returnSet to {}
	set value to prerequisites()
	if (value is not equal to ACTION_PERFORMED) then return value

	set readMessagesVal to readMessages(groupName)
	if (readMessagesVal is not equal to ACTION_PERFORMED) then return readMessagesVal

	if isNull(fileLocation) is equal to false then
		set end of returnSet to sendGroupMessageFile(groupName, lastUpdated, lastMessage, fileLocation)
	else
		set end of returnSet to NULL_MESSAGE
	end if

	if isNull(targetMessage) is equal to false then
		delay 0.1
		set end of returnSet to sendGroupTextMessage(groupName, lastUpdated, lastMessage, targetMessage)
	else
		set end of returnSet to NULL_MESSAGE
	end if

	return returnSet
end sendGroupMessage



on renameGroup(groupName, lastUpdated, lastMessage, newGroupTitle)
	set value to prerequisites()
	if (value is not equal to ACTION_PERFORMED) then return value

	set readMessagesVal to readMessages(groupName)
	if (readMessagesVal is not equal to ACTION_PERFORMED) then return readMessagesVal

	set groupChat to findGroupRow(groupName, lastUpdated, lastMessage)

	if groupChat is equal to UI_ERROR then
		return UI_ERROR
	else if groupChat is equal to missing value then
		return GROUP_CHAT_NOT_FOUND
	end if
	tell application "System Events"
		tell process "Messages"
			select groupChat
			try
				tell window "Messages" to tell splitter group 1 to tell button 2
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



on addParticipantToGroup(groupName, lastUpdated, lastMessage, phoneNumber)
	set value to prerequisites()
	if (value is not equal to ACTION_PERFORMED) then return value

	if isNull(phoneNumber) is equal to true then
		return INVALID_NUMBER
	end if

	set readMessagesVal to readMessages(groupName)
	if (readMessagesVal is not equal to ACTION_PERFORMED) then return readMessagesVal

	set isNumberIMessageReturn to isNumberIMessage(phoneNumber)

	if isNumberIMessageReturn is equal to UI_ERROR then
		return UI_ERROR
	else if isNumberIMessageReturn is not equal to true then
		return NUMBER_NOT_IMESSAGE
	end if

	set groupChat to findGroupRow(groupName, lastUpdated, lastMessage)

	if groupChat is equal to UI_ERROR then
		return UI_ERROR
	else if groupChat is equal to missing value then
		return GROUP_CHAT_NOT_FOUND
	end if

	tell application "System Events"
		tell process "Messages"
			select groupChat
			try
				tell window "Messages" to tell splitter group 1 to tell button 2
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



on removeParticipantFromGroup(groupName, lastUpdated, lastMessage, phoneNumber)
	set value to prerequisites()
	if (value is not equal to ACTION_PERFORMED) then return value

	if isNull(phoneNumber) is equal to true then
		return INVALID_NUMBER
	end if

	set readMessagesVal to readMessages(groupName)
	if (readMessagesVal is not equal to ACTION_PERFORMED) then return readMessagesVal

	set groupChat to findGroupRow(groupName, lastUpdated, lastMessage)

	if groupChat is equal to UI_ERROR then
		return UI_ERROR
	else if groupChat is equal to missing value then
		return GROUP_CHAT_NOT_FOUND
	end if

	set contactRow to missing value
	tell application "System Events"
		tell process "Messages"
			select groupChat
			try
				tell window "Messages" to tell splitter group 1 to tell button 2
					click
					tell pop over 1 to tell scroll area 1
						repeat with theRow in (table 1's entire contents) as list
							if theRow's class is row then
								if name of theRow's UI element 1 is equal to phoneNumber then
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
	set value to prerequisites()
	if (value is not equal to ACTION_PERFORMED) then return value

	set readMessagesVal to readMessages(groupName)
	if (readMessagesVal is not equal to ACTION_PERFORMED) then return readMessagesVal

	try
		tell application "System Events" to tell process "Messages"
			tell window "Messages"
				tell splitter group 1 to tell button 1 to click
				tell splitter group 1 to tell scroll area 3 to tell text field 1
					set value to participants
					keystroke ","
				end tell
				tell text area 1 of scroll area 4 of splitter group 1
					select
					set value to targetMessage
					keystroke return
					keystroke return
				end tell
				delay 0.2
				try
					tell splitter group 1 to tell button 2
						click
						tell pop over 1 to tell scroll area 1 to tell text field 1
							set value to groupName
							confirm
						end tell
						click
					end tell
					return ACTION_PERFORMED
				on error errorMessage
					key code 53
					my logError("CreateGroup.scpt", errorMessage)
					return UI_ERROR
				end try
			end tell
		end tell
	on error errorMessage
		my logError("CreateGroup.scpt", errorMessage)
		return UI_ERROR
	end try
end createGroup



on leaveGroup(groupName, lastUpdated, lastMessage)
	set value to prerequisites()
	if (value is not equal to ACTION_PERFORMED) then return value

	set readMessagesVal to readMessages(groupName)
	if (readMessagesVal is not equal to ACTION_PERFORMED) then return readMessagesVal

	set groupChat to findGroupRow(groupName, lastUpdated, lastMessage)

	if groupChat is equal to UI_ERROR then
		return UI_ERROR
	else if groupChat is equal to missing value then
		return GROUP_CHAT_NOT_FOUND
	end if

	tell application "System Events"
		tell process "Messages"
			select groupChat
			try
				tell window "Messages" to tell splitter group 1 to tell button 2
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
			my logError("SendMessage.scpt", errorMessage)
			return NOT_SENT
		end try
	end tell
end sendTextMessage



on sendGroupTextMessage(groupName, lastUpdated, lastMessage, targetMessage)
	set groupChat to findGroupRow(groupName, lastUpdated, lastMessage)
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
				tell text area 1 of scroll area 4 of splitter group 1 of window "Messages"
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
			my logError("SendMessage.scpt", errorMessage)
			return NOT_SENT
		end try
	end tell
end sendMessageFile



on sendGroupMessageFile(groupName, lastUpdated, lastMessage, fileLocation)
	set groupChat to findGroupRow(groupName, lastUpdated, lastMessage)
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
				tell text area 1 of scroll area 4 of splitter group 1 of window "Messages"
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



on readMessages(groupNameStarter)
	try
		tell application "System Events"
			tell process "Messages"
				set counter to 0
				repeat with theRow in ((table 1 of scroll area 1 of splitter group 1 of window "Messages")'s entire contents as list)
					if theRow's class is row then
						set fullName to (theRow's UI element 1)'s description

						if (groupNameStarter is in fullName) then
							if (fullName contains "Has unread messages.") then
								set counter to counter + 1
								select theRow
							end if
						end if
					end if
				end repeat

				if counter is equal to 0 then
					repeat with theRow in ((table 1 of scroll area 1 of splitter group 1 of window "Messages")'s entire contents as list)
						if theRow's class is row then
							set fullName to (theRow's UI element 1)'s description

							if (fullName contains "Has unread messages.") then
								set counter to counter + 1
								select theRow
							end if
						end if
					end repeat
				end if

				return ACTION_PERFORMED
			end tell
		end tell
	on error errorMessage
		my logError("Handlers.scpt", errorMessage)
		return UI_ERROR
	end try
end readMessages



on findGroupRow(groupName, lastUpdated, lastMessage)
	try
		tell application "System Events"
			tell process "Messages"
				set rowList to {}
				repeat with theRow in ((table 1 of scroll area 1 of splitter group 1 of window "Messages")'s entire contents as list)
					if theRow's class is row then
						set fullName to (theRow's UI element 1)'s description
						set finalName to (item 1 of my split(fullName, ". Last message: "))

						if finalName is equal to groupName then
							set end of rowList to theRow
						end if
					end if
				end repeat

				if (count of rowList) is equal to 0 then
					return missing value
				end if

				if (count of rowList) is equal to 1 then
					return (item 1 of rowList)
				end if

				if (count of rowList) is greater than 1 then
					set rowDateList to {}

					repeat with convoMatchRow in rowList
						set stringList to my split(convoMatchRow's UI element 1's description, " ")
						set itemCount to count of stringList
						set theDate to text 1 thru -2 of ((item (itemCount - 1) of stringList) & " " & (item itemCount of stringList))

						if theDate is equal to lastUpdated then
							set end of rowDateList to convoMatchRow
						end if
					end repeat

					if (count of rowDateList) is equal to 0 then
						set rowLastMessageList to {}

						repeat with convoMatchMessageRow in rowList
							set lastMessageList to my split(convoMatchMessageRow's UI element 1's description, ". Last message: ")
							set lastMessageSplit to my split((item 2 of lastMessageList), " ")
							set countOfList to count of lastMessageSplit
							set lastMessageFinal to words 1 thru (countOfList - 2) of (item 2 of lastMessageList)
							set lastMessageFinalString to my combine(lastMessageFinal, " ")

							if lastMessageFinalString is equal to lastMessage then
								set end of rowLastMessageList to convoMatchMessageRow
							end if
						end repeat

						if (count of rowLastMessageList) is equal to 0 then
							return (item 1 of rowList)
						end if

						if (count of rowLastMessageList) is greater than 0 then
							return (item 1 of rowLastMessageList)
						end if
					end if

					if (count of rowDateList) is equal to 1 then
						return (item 1 of rowDateList)
					end if

					if (count of rowDateList) is greater than 1 then
						set rowDateLastMessageList to {}

						repeat with convoMatchDateRow in rowDateList
							set lastMessageList to my split(convoMatchDateRow's UI element 1's description, ". Last message: ")
							set lastMessageSplit to my split((item 2 of lastMessageList), " ")
							set countOfList to count of lastMessageSplit
							set lastMessageFinal to words 1 thru (countOfList - 2) of (item 2 of lastMessageList)
							set lastMessageFinalString to my combine(lastMessageFinal, " ")

							if lastMessageFinalString is equal to lastMessage then
								set end of rowDateLastMessageList to convoMatchDateRow
							end if
						end repeat

						if (count of rowDateLastMessageList) is equal to 0 then
							return (item 1 of rowDateList)
						end if

						if (count of rowDateLastMessageList) is greater than 0 then
							return (item 1 of rowDateLastMessageList)
						end if
					end if
				end if
			end tell
		end tell
	on error errorMessage
		my logError("Handlers.scpt", errorMessage)
		return UI_ERROR
	end try
end findGroupRow



on isNumberIMessage(phoneNumber)
	try
		tell application "System Events" to tell process "Messages"
			tell window "Messages"
				tell splitter group 1 to tell button 1 to click
				tell splitter group 1 to tell scroll area 3 to tell text field 1
					set value to phoneNumber
					keystroke ","
					keystroke (ASCII character 8)
				end tell
			end tell
		end tell

		ignoring application responses
			tell application "System Events" to tell process "Messages" to tell window "Messages" to tell splitter group 1 to tell scroll area 3 to tell text field 1
				perform action "AXShowMenu" of menu button 1
			end tell
		end ignoring

		delay 0.05
		do shell script "killall System\\ Events"
		delay 0.05

		tell application "System Events" to tell process "Messages"
			tell window "Messages" to tell splitter group 1 to tell scroll area 3 to tell text field 1
				set menuItemTitle to title of menu item 2 of menu 1
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



on foregroundApp(theApp, theProcess, theBoolean)
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


on closeMessagesApp()
	delay 0.2
	tell application "System Events"
		tell application process "Messages"
			keystroke "m" using {command down}
		end tell
	end tell
end closeMessagesApp



on isServerRunning()
	tell application "Terminal"
		set textHistory to ""
		repeat with theTab in every tab of every window
			set textHistory to textHistory & (history of theTab as text)
		end repeat

		if ("[INFO] [weServer] Starting weServer on port" is in textHistory) then
			return true
		else
			return false
		end if
	end tell
end isServerRunning



on getProjectRoot()
	tell application "Finder"
		set parentFolder to get (container of (container of (path to me))) as text
	end tell
end getProjectRoot



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



on getCoordinates(theElement)
	tell application "System Events"
		tell theElement
			set p to position
			set s to size
		end tell
	end tell

	set xCoordinate to (item 1 of p) + (item 1 of s) / 2
	set yCoordinate to (item 2 of p) + (item 2 of s) / 2

	return {xCoordinate, yCoordinate}
end getCoordinates



on prerequisites()
	if hasAssistiveAccess() is equal to false then
		return ASSISTIVE_ACCESS_DISABLED
	end if

	return ACTION_PERFORMED
end prerequisites



on logError(callScript, theError)
	set weMessageDb to space & (POSIX path of getProjectRoot()) & "weserver.db" & space
	set head to "sqlite3 -column " & weMessageDb & quote
	set query to "insert into errors(script, errormessage) VALUES('" & callScript & "', '" & theError & "');"
	do shell script head & query & quote
end logError



on findGroupById(groupId)
	tell application "Messages"
		set groupChatList to every text chat
		repeat with i from 1 to count groupChatList
			try
				if {groupChatList's item i}'s cookie is equal to groupId then
				end if
			on error errorMessage
				if errorMessage contains groupId then
					return {groupChatList's item i}
				end if
			end try
		end repeat
		return missing value
	end tell
end findGroupById