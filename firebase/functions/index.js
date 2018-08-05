const functions = require('firebase-functions');
const admin = require('firebase-admin');

const USE_FIRESTORE = true;
const GENERIC_ERROR = 500;
const UNSUPPORTED_NOTIFICATION_VERSION = 501;

admin.initializeApp(functions.config().firebase);

var database = admin.database();
var firestore = admin.firestore();

exports.sendNotification= functions.https.onRequest((request, response) => {

	const httpNotificationVersion = parseInt(request.body.notificationVersion, 10);
	
	const options = {
		priority: "high"
	};
	
	if (httpNotificationVersion == 1){	  
		const token = request.body.registrationToken;
		const payload = {
			data: {
				notificationVersion: request.body.notificationVersion,
				encryptedText: request.body.encryptedText,
				key: request.body.key,
				handleId: request.body.handleId,
				chatId: request.body.chatId,
				chatName: request.body.chatName,
				attachmentNumber: request.body.attachmentNumber
			}
		};
		
		//Notification Priority Fix is in Version 1.2 and Higher
		
		admin.messaging().sendToDevice(token, payload)
			.then(function(res){
				response.status(200).send("Done");
			})
			.catch(function(error) {
				console.log("An error occurred while sending a notification. Error: ", error);
				response.status(GENERIC_ERROR).send("Error");
			});
	  
	} else if (httpNotificationVersion == 2){
		const token = request.body.registrationToken;
		const payload = {
			data: {
				notificationVersion: request.body.notificationVersion,
				encryptedText: request.body.encryptedText,
				key: request.body.key,
				handleId: request.body.handleId,
				chatId: request.body.chatId,
				chatName: request.body.chatName,
				attachmentNumber: request.body.attachmentNumber,
				accountLogin: request.body.accountLogin
			}
		};
	
		admin.messaging().sendToDevice(token, payload, options)
			.then(function(res){
				response.status(200).send("Done");
			})
			.catch(function(error) {
				console.log("An error occurred while sending a notification. Error: ", error);
				response.status(GENERIC_ERROR).send("Error");
			});
		
	} else if (httpNotificationVersion == 3){
		response.status(UNSUPPORTED_NOTIFICATION_VERSION).send("Unsupported Notification Version");
	} else {
		response.status(UNSUPPORTED_NOTIFICATION_VERSION).send("Unsupported Notification Version");
	}
});
 
exports.getVersion= functions.https.onRequest((request, response) => {
	var dbLatestBuildVersion = "";
	var dbStringLatestVersion = "";
  
	if (USE_FIRESTORE){
		var getVersionDoc = firestore.collection('metadata').doc('version').get()
			.then(doc => {
				dbLatestBuildVersion = doc.data().latestBuildVersion;
				dbStringLatestVersion = doc.data().latestVersion;
				
				var payload = {
					latestVersion: dbStringLatestVersion,
					latestBuildVersion: dbLatestBuildVersion
				};
		
				response.writeHead(200, {"Content-Type": "application/json"});
				response.end(JSON.stringify(payload));
			}).catch(err => {
				console.log('An error occurred while reading version metadata', err);
				
				var payload = {
					latestVersion: null,
					latestBuildVersion: null
				};
				
				response.writeHead(200, {"Content-Type": "application/json"});
				response.end(JSON.stringify(payload));
			});
	} else {
		var latestBuildVersionFunc = database.ref('/version/latestBuildVersion').once("value", function(data) {
			dbLatestBuildVersion = data.val();
		});
  
		var latestVersionStringFunc = database.ref('/version/latestVersion').once("value", function(data) {
			dbStringLatestVersion = data.val();
		});
   
		Promise.all([latestBuildVersionFunc, latestVersionStringFunc]).then(function(result){
			var payload = {
				latestVersion: dbStringLatestVersion,
				latestBuildVersion: dbLatestBuildVersion
			};
		
			response.writeHead(200, {"Content-Type": "application/json"});
			response.end(JSON.stringify(payload));
		});
	}
});