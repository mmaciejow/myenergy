# My Energy

This folder contains tools for monitoring pv system from Ginlong Data Logging Stick Wi-Fi for Solis Inverters. Currently tested with a Solis 4G Three Phase Inverter.
There is a possibility it also works with the following inverters: Omnik Solar, Solarman and Trannergy Inverters

The repository has two folders:
- python-myenergy - app for data collection
- android-myenergy - app for preview data

The Python script gets PV statistics from an internal website and stores in the database. To view the data, you need to submit your statistics to Firestore.

The following data are collected:
- current production 
- total production
- power
- (optional) weather / sunrise, sunset via ([openweathermap.org](https://openweathermap.org/ "openweathermap.org"))

## Setup

### Firebase
[Set up your Android app for Firestore]( https://firebase.google.com/docs/firestore/client/setup-android)

1. Go to the Firebase console.
2. In the center of the project overview page, click the Android icon or Add app
	- Use the package name **net.myenv.myenergy**
3. Register app.
4. Download "google-services.json" to obtain your Firebase

### Python App

1. Download **python-myenergy**

	`git clone https://github.com/mmaciejow/myenergy.git `

2. Create python virtual environments and install all modules required by this project

	```bash
	cd myenergy/python-myenergy/
	pip install virtualenv
	virtualenv venv
	source venv/bin/activate
	pip install -r requirements.txt 
	```
3. Put your firebase credentials (google-services.json) into app folder
4. Copy config.ini.org to config.ini and edit 

	`cp config.ini.org config.ini `

 
	- set IP address, username, password to your system pv web
	- set path to database file
	- set path to Firebase credential
	- set api key and geographical coordinates (latitude, longitude) for openweathermap if you want to get information about the day


### Android App
1. Download **android-myenergy** and open in Android Studio
2. Put your firebase credentials (google-services.json) into module app folder`android-myenergy\app\`
3. Compile app
4. Run the app on an Android device or emulator.

## Usage

`path/to/venv/bin/python app.py config.ini`

You can add to crontab

	`*/5 5-21 * * * /path/to/venv/bin/python /path/to/myenergy/app.py /path/to/myenergy/config.ini`

## Screenshots
<table width="100%">
	<tr>
	  <th><img src="https://github.com/mmaciejow/myenergy/blob/main/screenshots/myenergy-real-time.jpg?raw=true" width="100%"></th>
	  <th><img src="https://github.com/mmaciejow/myenergy/blob/main/screenshots/myenergy-history.jpg?raw=true" width="100%"></th>
	</tr>
	<tr>
	  <th><img src="https://github.com/mmaciejow/myenergy/blob/main/screenshots/myenergy-day.jpg?raw=true" width="100%"></th>
	</tr>
</table>