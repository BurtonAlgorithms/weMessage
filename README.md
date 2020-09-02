<h1 align="center">weMessage</h1>

[**weMessage**](https://wemessageapp.com) is a unified messaging application that brings Apple’s iMessage to Android devices, without reverse engineering or exploits. In addition to being lightning fast and incredibly easy to setup, it comes loaded with many of iMessage's coveted features and even more!

---

![Promo](https://wemessageapp.com/promotional/weMessage-Feature.png)

### Features:
* Full support for iMessage group chats and direct messaging
* SMS and MMS functionality
* The ability to send image, audio, and video attachments
* A comprehensive contact system that includes Contact Sync, Do Not Disturb, and Blocking
* Customize names and photo icons for contacts and group chats
* iMessage effects (Confetti, Fireworks, Invisible Ink)
* Encryption (AES Cryptography)
* Read Receipts

---

## Setup & Contributing

In order to load the project, run:

```
git clone https://github.com/RomanScott/weMessage.git
git submodule init
git submodule update
```

Then, open the ``android`` folder inside Android Studio (from there it will load the other modules).

The weMessage source code is separated into four modules. Compilation instructions for the Android app and weServer are included in the ``README`` files of their perspective module folders.

If you have any ideas, language translations, design or user interface changes, code cleaning or refactoring, or improvements you would like to make, please feel free to contribute by submitting a pull request! Any help is welcome and greatly appreciated. If you would like to report a bug or submit a crash report, open a <a href="https://github.com/RomanScott/weMessage/issues">new issue</a>.

Do note that the source code is largely undocumented. If you wish to add documentation to the source code, feel free to do so!

<br/>

**android** is the Android implementation of the weMessage platform. It is the client side interface of the project, and sends messages to and receives messages from the weServer instance. It controls the user interface, stores the messages, and also serves as a standalone SMS and MMS messaging app.

**server** is the messaging server implementation for the weMessage platform. It processes and forwards iMessages sent from the weMessage app to the Mac host machine, and sends messages received on the Mac to the app. It is the "bridge" between the user's Mac computer and Android device.

**commons** is a module that contains shared Java code between the weServer and weMessage application.

**firebase** is a module that contains code for some of the Firebase Cloud Functions, including version checking and sending notifications.

---

## License
[![GNU GPLv3 Image](https://www.gnu.org/graphics/agplv3-155x51.png)](https://www.gnu.org/licenses/agpl-3.0.en.html)  
weMessage is Free Software: You can use, study share and improve it at your will. Specifically you can redistribute and/or modify it under the terms of the [GNU Affero General Public License](https://www.gnu.org/licenses/agpl.html) as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.  
