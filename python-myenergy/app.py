import configparser
import sys

from data.pv.ginlong_stick_wifi_web_scraper import PV_Production
from db.local import *
from db.firestore import *

file_config = str(sys.argv[1])
config = configparser.ConfigParser()
config.read(file_config)

file_database = config.get("DATABASE", "file_database")
database = Database(file_database)

debug = config.getboolean('DEBUG', 'debug')
if debug:
    logging.basicConfig(level=logging.DEBUG, datefmt='%Y-%m-%d %H:%M:%S', format='%(asctime)s [%(levelname)s] %(message)s')

use_weather = config.getboolean("WEATHER", "use_weather")
if use_weather:
    from data.weather.openweathermap import Open_Weather_Map


def get_production_pv():
    PV_IP = config.get('PV_GINLONG_STICK_WIFI_WEB', 'ip')
    PV_USERNAME = config.get('PV_GINLONG_STICK_WIFI_WEB', 'user')
    PV_PASSWORD = config.get('PV_GINLONG_STICK_WIFI_WEB', 'password')
    production = PV_Production(PV_IP, PV_USERNAME, PV_PASSWORD)
    return production.getData()


def get_weather():
    api_key = config.get("WEATHER", "api_key")
    lat = config.get("WEATHER", "lat")
    lon = config.get("WEATHER", "lon")
    weather = Open_Weather_Map(lat, lon, api_key)
    return weather.get_weather()


def sync_firestore():
    key = config.get("FIRESTORE", "file_firestore_key")
    firestore = Firestore(key)
    production_unsynchronized = database.get_production_unsynchronized()
    day_unsynchronized = database.get_day_unsynchronized()
    month_unsynchronized = database.get_month_unsynchronized()
    year_unsynchronized = database.get_year_unsynchronized()
    stats_unsynchronized = database.get_stats_unsynchronized()

    return firestore.sync_firestore(production_unsynchronized, day_unsynchronized, month_unsynchronized,
                                    year_unsynchronized, stats_unsynchronized)


def main():
    logging.debug('Start MyEnergy')

    production = get_production_pv()
    if production is not None:
        today = production.today
        power = production.power
        total = production.total

        weather = None
        if use_weather:
            weather = get_weather()

        database.save_pv_production(today, power, total, weather)

    sync = sync_firestore()
    if sync is True:
        database.set_synchronized()
        logging.debug("Synchronization with firebase successful")
    else:
        logging.debug("Synchronization with firebase failed")

    database.close_database()

    logging.debug('End MyEnergy')


if __name__ == '__main__':
    main()
