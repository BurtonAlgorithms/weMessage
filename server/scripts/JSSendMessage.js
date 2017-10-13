const WEMESSAGE_JAVASCRIPT_VERSION = 1;
const VERSION_MISMATCH = 998;
const SENT = 1000;
const NULL_MESSAGE = 1011;
const ACTION_PERFORMED = 1014;
const INTERNAL_JAVASCRIPT_ERROR = 1015;

const messages = Application('Messages');



ObjC.import('Foundation');

var argv = [];
var args = $.NSProcessInfo.processInfo.arguments;
var argc = args.count;

for (var i = 2; i < argc; i++) {
    argv.push(ObjC.unwrap(args.objectAtIndex(i)));
}

sendMessage(argv[0], argv[1], argv[2], argv[3], argv[4]);


var toType = function(obj) {
  return ({}).toString.call(obj).match(/\s([a-zA-Z]+)/)[1].toLowerCase()
}


function init(jsVersion) {
	if (jsVersion != WEMESSAGE_JAVASCRIPT_VERSION) {
		return VERSION_MISMATCH;
	} else {
		return ACTION_PERFORMED;
	}
}


function sendMessage(jsVersion, isGroup, targetIdentifier, fileLocation, targetMessage) {

	if (init(jsVersion) == VERSION_MISMATCH) {
		return INTERNAL_JAVASCRIPT_ERROR;
	}

	var returnSet = [];
	var target;

	try {
		if (isGroup == 1){

			target = messages.textChats.byId(targetIdentifier);

		} else {

			target = messages.buddies.whose({ handle: targetIdentifier })[0];
		}

		if (fileLocation != "") {

			var file = new Path(fileLocation);

			messages.send(file, { to: target });
			returnSet.push(SENT);

		} else {
			returnSet.push(NULL_MESSAGE);
		}

		if (targetMessage != "") {

			messages.send(targetMessage, { to: target });
			returnSet.push(SENT);

		} else {
			returnSet.push(NULL_MESSAGE);
		}

	} catch(ex) {
		returnSet.push(INTERNAL_JAVASCRIPT_ERROR);
	}

	return returnSet;
}