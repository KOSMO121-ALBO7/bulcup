import re
import csv
import time
import traceback
import pymysql
from urllib import request
import selenium.common.exceptions
from bs4 import BeautifulSoup
from selenium import webdriver
from selenium.webdriver.chrome.service import Service
from selenium.webdriver.common.by import By
from selenium.webdriver.common.keys import Keys
from webdriver_manager.chrome import ChromeDriverManager


# def set_chrome_driver():
#     chrome_options = webdriver.ChromeOptions()
#     driver = webdriver.Chrome(service_args=Service(ChromeDriverManager().install()), options=chrome_options)
#     return driver


def set_chrome_driver():
    driver = selenium.webdriver.Chrome('D:/Download/chromedriver.exe')
    return driver


def update_image_path(imgurl):
    global status

    # DB에 업데이트
    conn = pymysql.connect(
        host='192.168.0.52',
        user='bulcup',
        password='1234',
        database='bulcup'
    )

    cursor = conn.cursor()

    try:
        query = 'UPDATE functional_food' \
                ' SET image_path = %s ' \
                ' WHERE functional_food_name = %s AND manufacturer = %s'
        cursor.execute(query, (imgurl, product_name, manufacturer))
    except Exception as ex:
        conn.rollback()
        print('예외발생:', ex)
        # print(traceback.format_exc())
        return
    else:
        conn.commit()
        status = False
    finally:
        cursor.close()
        conn.close()


def search(rows):
    global index
    for row in rows:
        title = row.find_element(By.CSS_SELECTOR, 'div.title').text
        company = row.find_element(By.CSS_SELECTOR, 'div.company').text
        # print(title, company)

        if title.find(product_name) == -1:
            continue
        elif company.find(manufacturer) != -1:
            index = 2
            url = row.find_element(By.CSS_SELECTOR, 'div.thumbnail').get_attribute('style')
            imgurl = re.findall(r'\("([^)]+)"', url)
            if imgurl[0] == '/assets/image_search_sample.svg':
                imgurl = '/user/images/none.png'
            else:
                imgurl = imgurl[0]
            print(imgurl, product_name, manufacturer)
            update_image_path(imgurl)


with open('search_condition.csv', 'r', encoding='utf-8-sig') as f:
    reader = csv.reader(f)
    search_word_list = [row for row in reader if row]
    # print(search_condition)

driver = set_chrome_driver()
driver.implicitly_wait(3)
driver.get('https://www.hsinportal.com/search/product')
html = driver.page_source
soup = BeautifulSoup(html, 'html.parser')

search_bar = driver.find_element(By.XPATH, '//*[@id="__next"]/div[2]/div/dl[1]/dd/div/input')
search_button = driver.find_element(By.CLASS_NAME, 'find')
search_condition = driver.find_element(By.XPATH, '//*[@id="__next"]/div[2]/div/dl[1]/dd/div/div[1]/div/div/div[1]/div')

search_dropbox = driver.find_element(By.XPATH, '//*[@id="__next"]/div[2]/div/dl[1]/dd/div/div[1]/div/div')
search_dropbox.click()
search_dropbox_item = driver.find_element(By.XPATH,
                                          '//*[@id="__next"]/div[2]/div/dl[1]/dd/div/div[1]/div/div[2]/div/div[2]')
search_dropbox_item.click()
checkbox1 = driver.find_element(By.XPATH, '//*[@id="__next"]/div[2]/div/div[2]/dl[1]/dd/label[2]')
checkbox2 = driver.find_element(By.XPATH, '//*[@id="__next"]/div[2]/div/div[2]/dl[2]/dd/label[2]')
checkbox1.click()
checkbox2.click()

status = True
index = 2
for data in search_word_list:
    product_name = data[0]
    manufacturer = data[1]
    keyword = data[2]

    search_bar.send_keys(keyword)
    search_button.click()
    time.sleep(1)

    print('검색어:', product_name, '제조사:', manufacturer)

    board_list = driver.find_element(By.XPATH, '//*[@id="__next"]/div[3]/div[3]')
    while status:
        try:
            driver.find_element(By.XPATH, '//*[@id="__next"]/div[3]/div[5]/a[' + str(index) + ']').click()
            table_rows = board_list.find_elements(By.CSS_SELECTOR, 'div.items div.row')
            search(table_rows)
            index += 1
            if index == 13:
                index = 2
        except selenium.common.exceptions.NoSuchElementException as ex:
            index = 2
            update_image_path('/user/images/none.png')
            status = False
            print('예외발생:', ex)

    search_bar.clear()
    status = True
