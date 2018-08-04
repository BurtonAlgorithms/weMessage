# weMessage

[**weMessage**](https://wemessageapp.com) is a unified messaging application that brings Apple’s iMessage to Android devices. weMessage is a software solution that is comprised of two main pieces of software: the Android app itself and a messaging server called the weServer. In order to actually use iMessage, the weServer needs to be installed on a Mac machine. weMessage can also function as a standalone SMS and MMS messaging app.

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

**android** is the Android implementation of the weMessage platform. It is the client side interface of the project, and sends messages to and receives messages from the weServer instance. It controls the user interface, stores the messages, and also serves as a standalone SMS and MMS messaging app.

**server** is the messaging server implementation for the weMessage platform. It processes and forwards iMessages sent from the weMessage app to the Mac host machine, and sends messages received on the Mac to the app. It is the "bridge" between the user's Mac computer and Android device.

**commons** is a module that contains shared Java code between the weServer and weMessage application.

---

### Information

weMessage was created by Roman Scott for Communitext LLC.

    Copyright © 2018 Roman Scott