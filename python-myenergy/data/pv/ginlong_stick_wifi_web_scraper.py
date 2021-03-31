import logging
import re
import requests
from model.production import Production


class PV_Production:
    __ip = None
    __username = None
    __password = None

    def __init__(self, ip, username, password):
        self.__ip = ip
        self.__username = username
        self.__password = password

    def __getHtmlSource(self):
        url = "http://" + self.__ip + "/status.html"
        try:
            req = requests.get(url, auth=(self.__username, self.__password), timeout=20)
            return req.text
        except requests.ConnectionError as err:
            logging.debug("Cannot connect to PV site")
            logging.debug(err)
            return None

    def getData(self):
        text_html = self.__getHtmlSource()
        if text_html is not None:
            try:
                today = float(re.search('webdata_today_e\s+=\s+"([^"]+)', text_html).group(1))
                total = float(re.search('webdata_total_e\s+=\s+"([^"]+)', text_html).group(1))
                power = int(re.search('webdata_now_p\s+=\s+"([^"]+)', text_html).group(1))
            except:
                today = None
                total = None
                power = None
                logging.debug("Cannot find stats")

            if power is not None:
                logging.debug("Production today: %s", today)
                logging.debug('Current Power: %s', power)
                logging.debug('Production total: %s', total)
                production = Production(today, power, total)
                return production
        else:
            return None
