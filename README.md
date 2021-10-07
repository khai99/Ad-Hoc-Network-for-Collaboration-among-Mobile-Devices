# BlueKa - Android Application
This Android Application allows users to connect to nearby smartphones using the Bluetooth Low Energy (BLE) technology and thereby perform an action in unison. This action involves playing the "Merdeka!" sound snippet in coordination.

## Project Description
The idea of this project is that, as the user moves around, their phone will connect to other phones nearby that have the same app installed. Under certain circumstances an agreement will be reached, and the phones will perform an action together. In this case, the action is to play a sound snippet simultaneously. For example, given that a user has the app installed on their phone, and has it configured to connect to a minimum of four other phones within the range of their bluetooth radio. Once the network of five phones is established, the phones will negotiate a time within a few seconds from the connection and play the Merdeka! chime simultaneously at full volume.

## Aims and Objectives
* Creating an Android App that implements Bluetooth Low Energy teachnology as well as Sound Synchronization feature.
* Developing the mobile application in such a way that it runs without major user intervention. This indicates that the application should primarily run in background.
* Implementation of scan filter that allows a device to detect other smartphones which have the Android application installed.
* Configuration and connection of nearby smartphones which have active Bluetooth.
* Leveraging a technology that allows smartphones within a certain location to band together and perform an action synchronously.
* Handling audio outputs in order to play the sound snippet at maximum volume and reset it back to the original volume.

## User Manual
**Pre-requisite for code development : Android Studio**

### Code Development
1. Clone this repository and import into Android Studio.
2. Before running the application on a device, USB debugging must be enabled. On the device, open Settings page, select Developer Options, and then enable USB debugging.
3. Build and run the application on Android Studio.

### Using BlueKa App
1. Download APK file onto your Android device. The APK file can be found [here](https://github.com/Group-10b-SE-GP/BlueKa/tree/master/APK%20file).
2. Once the application is launched on the device, the user shall be requested to enable Bluetooth.
<p align="center">
    <img src=https://github.com/Group-10b-SE-GP/BlueKa/blob/master/BlueKaScreenshots/HomePage.jpg width="230" height="490"/>
    <img src=https://github.com/Group-10b-SE-GP/BlueKa/blob/master/BlueKaScreenshots/EnableBluetooth.jpg width="230" height="490"/>
</p>

3. The user shall then be able to set the number of devices that can participate in the concert.
<p align="center">
    <img src=https://github.com/Group-10b-SE-GP/BlueKa/blob/master/BlueKaScreenshots/SetNumber.jpg width="230" height="490"/>
</p>

4. This is followed by pressing on the connect button. If location permission is switched off, then the user shall be requested to enable location.
<p align="center">
    <img src=https://github.com/Group-10b-SE-GP/BlueKa/blob/master/BlueKaScreenshots/EnableLocation.jpg width="230" height="490"/>
</p>

5. Once the device successfully connects based on the set conditions, the snippet shall be heard and a toast message shall be seen. This operation does not necessarily require the application to be open. This is because once the user presses the connect button,  the device can either scan or advertise, and form a network to play the snippet even when the application is running in background.
<p align="center">
    <img src=https://github.com/Group-10b-SE-GP/BlueKa/blob/master/BlueKaScreenshots/PhonesConnected.jpg width="230" height="490"/>
    <img src=https://github.com/Group-10b-SE-GP/BlueKa/blob/master/BlueKaScreenshots/PlayedToastForeground.jpg width="230" height="490"/>
    <img src=https://github.com/Group-10b-SE-GP/BlueKa/blob/master/BlueKaScreenshots/PlayedToastBackground.jpg width="230" height="490"/>
</p>

6. The user shall also be able to access the settings page. On Settings page, the user shall be able to control the permissions that the application requires as well as gain more information as to why each permission is required..
<p align="center">
    <img src=https://github.com/Group-10b-SE-GP/BlueKa/blob/master/BlueKaScreenshots/SettingsPage.jpg width="230" height="490"/>
</p>

7. The user can further switch between light and dark mode for the application.
<p align="center">
    <img src=https://github.com/Group-10b-SE-GP/BlueKa/blob/master/BlueKaScreenshots/HomePage.jpg width="230" height="490"/>
    <img src=https://github.com/Group-10b-SE-GP/BlueKa/blob/master/BlueKaScreenshots/DarkModeHomePage.jpg width="230" height="490"/>
</p>

<p align="center">
    <img src=https://github.com/Group-10b-SE-GP/BlueKa/blob/master/BlueKaScreenshots/SettingsPage.jpg width="230" height="490"/>
    <img src=https://github.com/Group-10b-SE-GP/BlueKa/blob/master/BlueKaScreenshots/DarkModeSettingsPage.jpg width="230" height="490"/>
</p>

## Summary
This project was managed and achieved through different stages. A total of four sprints were implemented which started with a planning meeting and ended with a retrospective meeting. In each sprint several other meetings were held whereby the work of each member was verified by the team members and the supervisor. The goal of the first sprint was to gather requirements from the client, and intiate a thorough research procedure. This continnued by intial implementation of codes. In the second sprint, BLE advertiser and scanner codes were completed and tested. Moreover, audio handling codes were also implemented and successfullly tested. During the third sprint, more research was carried out on synchronization methods. This lead to the implementation of synchronization by NTP codes. In the fourth sprint, the queue and disconnect operations were completed and all the codes were merged with the user interface to build up the final BlueKa application. Several testing methodologies were adopted and the application was thoroughly tested. Eventually, all of the requirements requested by the client were achieved through the implementation of this project. 

