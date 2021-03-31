import logging
import requests
import json

from model.weather import Weather


class Open_Weather_Map:
    lat = None
    lon = None
    api_key = None

    def __init__(self, lat, lon, api_key):
        self.lat = lat
        self.lon = lon
        self.api_key = api_key

    def __getJson(self):
        url = "https://api.openweathermap.org/data/2.5/onecall?lat=%s&lon=%s&appid=%s&exclude=minutely,hourly,alerts&units=metric" % (
            self.lat, self.lon, self.api_key)
        try:
            req = requests.get(url)
            data = json.loads(req.text)
            return data
        except requests.ConnectionError as err:
            logging.debug("Cannot connect to Weather api")
            logging.debug(err)
            return None

    def get_weather(self):
        weather = None
        try:
            data = self.__getJson()

            weather = Weather()
            weather.sunrise = int(data["current"]["sunrise"]) * 1000  # milliseconds
            weather.sunset = int(data["current"]["sunset"]) * 1000  # milliseconds

            # current weather
            weather.current_status = str(data['current']["weather"][0]["main"])
            weather.current_temp = int(data["current"]["temp"])
            icon = str(data['current']["weather"][0]["icon"]).replace("n", "d")
            weather.current_icon = icon
            # day weather
            weather.day_status = str(data['daily'][0]["weather"][0]["main"])
            weather.day_temp = int(data["daily"][0]["temp"]["day"])
            icon = str(data['daily'][0]["weather"][0]["icon"]).replace("n", "d")
            weather.day_icon = icon
        except:
            logging.debug("Cannot get weather")

        return weather
