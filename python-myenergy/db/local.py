import calendar
import logging
import sqlite3
from datetime import date, datetime


class Database:
    __connect_db = None
    __db = None

    # Database function
    def __init__(self, fileDatabase):
        self.__connect_db = sqlite3.connect(fileDatabase)
        self.__db = self.__connect_db.cursor()

        self.__create_table()

    def __create_table(self):
        self.__db.execute("""CREATE TABLE IF NOT EXISTS pv_production
                    (id INTEGER NOT NULL PRIMARY KEY,
                    date INTEGER NOT NULL, 
                    production REAL NOT NULL,
                    power INTEGER NOT NULL,
                    weather_status TEXT,
                    weather_temp REAL,
                    weather_icon TEXT,
                    sync_firestore INTEGER DEFAULT 0 
                    )""")

        self.__db.execute("""CREATE TABLE IF NOT EXISTS energy_day
                     (id INTEGER NOT NULL PRIMARY KEY , 
                     date INTEGER NOT NULL, 
                     production REAL NOT NULL DEFAULT 0,
                     avg_power REAL NOT NULL DEFAULT 0,
                     max_power REAL NOT NULL DEFAULT 0,
                     sunrise INTEGER DEFAULT 0,
                     sunset INTEGER DEFAULT 0,
                     weather_status TEXT,
                     weather_temp REAL,
                     weather_icon TEXT,
                     sync_firestore INTEGER DEFAULT 0 ) """)

        self.__db.execute("""CREATE TABLE IF NOT EXISTS energy_month 
                     (id INTEGER NOT NULL PRIMARY KEY, 
                     date INTEGER NOT NULL, 
                     production REAL NOT NULL DEFAULT 0,
                     sync_firestore INTEGER DEFAULT 0 ) """)

        self.__db.execute("""CREATE TABLE IF NOT EXISTS energy_year 
                     (id INTEGER NOT NULL PRIMARY KEY ,
                     date INTEGER NOT NULL, 
                     production REAL NOT NULL DEFAULT 0,
                     sync_firestore INTEGER DEFAULT 0 ) """)

        self.__db.execute("""CREATE TABLE IF NOT EXISTS energy_stats 
                     (id INTEGER NOT NULL PRIMARY KEY DEFAULT 0,
                     pv_production REAL NOT NULL DEFAULT 0,
                     max_power REAL NOT NULL DEFAULT 0,
                     max_power_date INTEGER NOT NULL DEFAULT 0,
                     sync_firestore INTEGER DEFAULT 0 ) """)

    def close_database(self):
        self.__connect_db.commit()
        self.__connect_db.close()

    def __save(self, sql):
        if sql is not None:
            self.__db.execute(sql)
            self.__connect_db.commit()

    def save_production(self, production, power, weather):
        _id = datetime.today().strftime('%Y%m%d%H%M%S')
        time = int(datetime.now().timestamp()*1000)
        if weather is not None:
            sql = "INSERT OR REPLACE INTO pv_production VALUES({},{},{},{},\"{}\",{},\"{}\",{})"\
                .format(_id, time, production, power,weather.current_status ,weather.current_temp, str(weather.current_icon),0)
        else:
            sql = "INSERT OR REPLACE INTO pv_production (id, date, production, power, sync_firestore) VALUES({},{},{},{},{})" \
                .format(_id, time, production, power, 0)
        self.__save(sql)

    def save_production_daily(self, production, weather):
        _id = int(datetime.today().strftime('%Y%m%d'))
        time = int(datetime(datetime.now().year, datetime.now().month, datetime.now().day).timestamp()* 1000)
        avg_power = self.get_avg_power(_id)
        max_power = self.get_max_power(_id)
        if weather is not None:
            sql = "INSERT OR REPLACE INTO energy_day VALUES({},{},{},{},{},{},{},\"{}\",{},\"{}\",{})".format(
                _id, time, production, avg_power, max_power, weather.sunrise,weather.sunset, weather.day_status,weather.day_temp,weather.day_icon, 0)
        else:
            sql = "INSERT OR REPLACE INTO energy_day (id, date, production, avg_power, max_power, sync_firestore) VALUES({},{},{}," \
                  "{},{},{})".format(_id, time, production,  avg_power, max_power, 0)

        self.__save(sql)

    def save_production_monthly(self, total):
        _id = datetime.today().strftime('%Y%m')
        time = int(datetime(datetime.now().year, datetime.now().month,1).timestamp()* 1000)
        sql = "INSERT OR REPLACE INTO energy_month VALUES({},{},{},{})"\
            .format(_id, time, total, 0)
        self.__save(sql)

    def save_production_annually(self, total):
        _id = int(datetime.today().year)
        time = int(datetime(datetime.now().year,1,1).timestamp() * 1000)
        sql = "INSERT OR REPLACE INTO energy_year VALUES({},{},{},{})"\
            .format(_id, time,total, 0)
        self.__save(sql)

    def __save_stats(self, pv_total):
        stats = self.get_max_power_total()
        max_power = stats[3]
        max_power_date = stats[1]
        sql = "INSERT OR REPLACE INTO energy_stats VALUES({},{},{},{},{})" .format(0, pv_total, max_power, max_power_date, 0)
        self.__save(sql)

    def save_pv_production(self, pv_today, pv_power, pv_total, weather=None):

        self.save_production(pv_today, pv_power, weather)
        self.save_production_daily(pv_today, weather)
        self.save_production_monthly(self.sumMonth())
        self.save_production_annually(self.sumYear())
        self.__save_stats(pv_total)


    def sumMonth(self):
        today = date.today()
        first_day_of_month = 1
        last_day_of_month = calendar.monthrange(today.year, today.month)[1]

        start_date = 10000 * today.year + 100 * today.month + first_day_of_month
        end_date = 10000 * today.year + 100 * today.month + last_day_of_month

        sum = self.__db.execute("SELECT SUM(production) FROM energy_day WHERE id>=? and id<=?",
                                (start_date, end_date)).fetchone()
        if sum is not None:
            result = round(sum[0], 2)
            logging.debug("Production this month: %s" , result )
            return result
        else:
            return 0

    def sumYear(self):
        sum = self.__db.execute("SELECT SUM(production) FROM energy_month WHERE id LIKE '"
                                + str(date.today().year) + "%'").fetchone()
        if sum is not None:
            result = round(sum[0], 2)
            logging.debug("Production this year: %s" ,result )
            return result
        else:
            return 0

    def get_avg_power(self, date):
        avg = self.__db.execute("SELECT AVG(power) FROM pv_production WHERE id LIKE '" + str(date) + "%'").fetchone()
        if avg[0] is not None:
            result = round(avg[0], 2)
            logging.debug("Average power of production today: %s" , result)
            return result
        else:
            return 0

    def get_max_power(self, date):
        max = self.__db.execute("SELECT MAX(power) FROM pv_production WHERE id LIKE '" + str(date) + "%'").fetchone()
        if max[0] is not None:
            result = round(max[0], 2)
            logging.debug("Max power of production Today: %s" , result)
            return result
        else:
            return 0

    def get_max_power_total(self):
        max = self.__db.execute("SELECT * FROM pv_production ORDER BY power DESC LIMIT 1").fetchone()
        if max is not None:
            logging.debug("Max power of production: %s" , str(max[3]))
            return max
        else:
            return 0

    def get_production_unsynchronized(self):
        return self.__db.execute("SELECT * FROM pv_production WHERE sync_firestore=0").fetchall()

    def get_day_unsynchronized(self):
        return self.__db.execute("SELECT * FROM energy_day WHERE sync_firestore=0").fetchall()

    def get_month_unsynchronized(self):
        return self.__db.execute("SELECT * FROM energy_month WHERE sync_firestore=0").fetchall()

    def get_year_unsynchronized(self):
        return self.__db.execute("SELECT * FROM energy_year WHERE sync_firestore=0").fetchall()

    def get_stats_unsynchronized(self):
        return self.__db.execute("SELECT * FROM energy_stats WHERE sync_firestore=0").fetchall()

    def set_synchronized(self):
        self.__db.execute("UPDATE pv_production SET sync_firestore=1 WHERE sync_firestore=0")
        self.__db.execute("UPDATE energy_day SET sync_firestore=1 WHERE sync_firestore=0")
        self.__db.execute("UPDATE energy_month SET sync_firestore=1 WHERE sync_firestore=0")
        self.__db.execute("UPDATE energy_year SET sync_firestore=1 WHERE sync_firestore=0")
        self.__db.execute("UPDATE energy_stats SET sync_firestore=1 WHERE sync_firestore=0")

