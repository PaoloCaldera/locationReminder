# Location Reminder

Location Reminder is the fourth project of the Android Kotlin Developer nanodegree provided by Udacity. It is a test project for the second chapter of the course, Advanced Android Apps with Kotlin. The project is focused on the location and geofencing concepts: a list of reminders is associated with a particular point in the Maps view, and whenever the user enters the surrounding area, a notification appears acting as a reminder.
The project demonstrates the ability to exploit the components already seen in the first chapter, Developing Android Apps with Kotlin (see projects [Shoe Store](https://github.com/PaoloCaldera/shoeStore) and [Asteroid Radar](https://github.com/PaoloCaldera/asteroidRadar)), and in the previous project of the second chapter of the course, [Loading Status](https://github.com/PaoloCaldera/loadingStatus). However, it demonstrates also the usage of

* [Maps](https://developer.android.com/develop/sensors-and-location/location/maps-and-places), to set the location associated with the reminder
* [Broadcast Receivers](https://developer.android.com/guide/components/broadcasts), to handle the notification click
* [Testing](https://developer.android.com/training/testing), to create methods for testing the actual application, both for unit testing and UI testing

The project consists of three screens that constitute the main app navigation and of an additional screen, opened when the user clicks the incoming notification.

Visit the [Wiki](https://github.com/PaoloCaldera/llocationReminder/wiki) to see the application screens.


## Getting Started
To clone the repository, use the command
```
$ git clone https://github.com/PaoloCaldera/locationReminder.git
```
or the `Get from VCS` option inside Android Studio by copying the link above.

Before running the application, two more steps need to be performed:
* Create a Google Cloud project and add an API key to use the Google Maps API, following the [Google](https://developers.google.com/maps/documentation/android-sdk/get-api-key) indications
* Create a Firebase project, then add the Email/Password and Google authentication methods according to the [Firebase](https://firebase.google.com/docs/auth/android/firebaseui) instructions
To retrieve the SHA-1 of the application, which is going to be used both for creating the API key and the authentication methods, execute in the Android Studio terminal the following line:
```
$ keytool -list -v -alias androiddebugkey -keystore C:\Users\paolo\.android\debug.keystore
```

Then, run the application on an Android device or emulator. The application is compiled with API 33, thus use a device or emulator supporting such API version.
For complete usage of the application, be sure that the device or emulator is connected to a Wi-Fi network.


## License

Loading Status is a public project that can be downloaded and modified under the terms and conditions of the [MIT License]().
