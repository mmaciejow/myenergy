import firebase_admin
from firebase_admin import credentials
from firebase_admin import firestore


class Firestore:
    __firestore_db = None

    __COLLECTION_PV_PRODUCTION = "pv_production"
    __COLLECTION_ENERGY_DAY = "energy_day"
    __COLLECTION_ENERGY_MONTH = "energy_month"
    __COLLECTION_ENERGY_YEAR = "energy_year"
    __COLLECTION_ENEGRY_STATS = "energy_stats"

    def __init__(self, key):
        self.key = key
        # initialize sdk
        cred = credentials.Certificate(key)
        firebase_admin.initialize_app(cred)
        self.__firestore_db = firestore.client()

    def __sync(self, items):
        try:
            batch = self.__firestore_db.batch()
            count = 0
            for item in items:
                collection = item['collection']
                doc = item['doc']
                batch.set(collection, doc)
                count = count + 1
                if count == 500:
                    batch.commit()
                    batch = self.__firestore_db.batch()
                    count = 0

            batch.commit()
            return True
        except:
            return False

    def sync_firestore(self, production=[], production_daily=[], production_monthly=[], production_annually=[],
                       energy_stats=[]):

        if not production and not production_daily and not production_monthly and not production_annually and not energy_stats:
            return True

        items = []

        for item in production:
            collection = self.__firestore_db.collection(self.__COLLECTION_PV_PRODUCTION).document(str(item[0]))
            doc = {'date': item[1], 'production': item[2], 'power': item[3],
                   'weather_status': item[4], 'weather_temp': item[5], 'weather_icon': item[6]}
            items.append({"collection": collection, "doc": doc})

        for item in production_daily:
            collection = self.__firestore_db.collection(self.__COLLECTION_ENERGY_DAY).document(str(item[0]))
            doc = {'date': item[1], 'production': item[2], 'avg_power': item[3], 'max_power': item[4],
                   'sunrise': item[5], 'sunset': item[6],
                   'weather_status': item[7], 'weather_temp': item[8], 'weather_icon': item[9]}
            items.append({"collection": collection, "doc": doc})

        for item in production_monthly:
            collection = self.__firestore_db.collection(self.__COLLECTION_ENERGY_MONTH).document(str(item[0]))
            doc = {'date': item[1], 'production': item[2]}
            items.append({"collection": collection, "doc": doc})

        for item in production_annually:
            collection = self.__firestore_db.collection(self.__COLLECTION_ENERGY_YEAR).document(str(item[0]))
            doc = {'date': item[1], 'production': item[2]}
            items.append({"collection": collection, "doc": doc})

        for item in energy_stats:
            collection = self.__firestore_db.collection(self.__COLLECTION_ENEGRY_STATS).document("stats")
            doc = {'pv_production': item[1], 'max_power': item[2], 'max_power_date': item[3]}
            items.append({"collection": collection, "doc": doc})

        return self.__sync(items)
