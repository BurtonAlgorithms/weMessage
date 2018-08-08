# weMessage Android

### Compilation and Debugging

weMessage is compiled like a standard Android application. weMessage depends on two projects: [ChatKit](https://github.com/RomanScott/ChatKit) and [SMSKit](https://github.com/RomanScott/SMSKit), both of which are separate repositories on my GitHub. These two are both forks of their parent repositories, with a few modifications that were made specifically for weMessage's functionality. If you find a bug or want to update either of these repositories, submit an issue or pull request there.

For debugging, you can use the standard debugging tools that are included in Android Studio. By default, all crash reports are uploaded to our Firebase's Crashlytics solution, if you want to change your Firebase instance you can do this through the ``google-services.json`` file.