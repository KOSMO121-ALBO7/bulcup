from selenium import webdriver
from bs4 import BeautifulSoup
from time import time

option = webdriver.ChromeOptions()
option.add_experimental_option("useAutomationExtension", False)
option.add_experimental_option("excludeSwitches", ['enable-automation'])

driver = webdriver.Chrome('../chromedriver/chromedriver.exe')

eye = open("fileEye.txt", "r", encoding='UTF-8').read().split(', ')
diet = open("fileDiet.txt", "r", encoding='UTF-8').read().split(', ')
liver = open("fileLiver.txt", "r", encoding='UTF-8').read().split(', ')
digestive = open("fileDigestive.txt", "r", encoding='UTF-8').read().split(', ')
bone = open("fileBone.txt", "r", encoding='UTF-8').read().split(', ')
energy = open("fileEnergy.txt", "r", encoding='UTF-8').read().split(', ')
file = open("text.txt", "w", encoding='UTF-8')
file.write("URL:: TITLE:: IMG:: WRITER:: TIME:: CONTENT:: CLASSIFY\n")

# 뉴스들의 url 가져오기
news = []
t1 = time()
for i in range(1000):
    if(time() - t1) > 3:  # 3초동안 크롤링~!
        break
    url = "https://news.naver.com/main/list.naver?mode=LS2D&sid2=241&sid1=103&mid=shm&date=20230103&page="+str(i)
    driver.get(url)
    html = driver.page_source
    soup = BeautifulSoup(html, 'html.parser')
    for url in soup.select('td.content a'):
        print(url)
        news.append(url['href'])


for url in news[::2]:
    try:
        driver.get(url)
        html = driver.page_source
        soup = BeautifulSoup(html, 'html.parser')
        print("URL :: ", url)
        title = soup.select_one('div.media_end_head_title span').text
        print("TITLE :: ", title)

        img = soup.select_one("div.newsct_article img")['src']
        print("IMG:: ", img)
        writer = "No Writer"
        try:
            writer = soup.select_one("div.byline span.byline_s").text
            print("WRITER:: ", writer)
        except BaseException as e:
            print("NO WRITER" + str(e))

        writeTime = soup.select_one("div.media_end_head_info_datestamp_bunch span").text
        print("TIME:: ", writeTime)
        content = soup.select_one("div#dic_area").text.strip().replace("\n", "")
        print("CONTENT:: ", content)

        file.write(url + "::")
        file.write(title + "::")
        file.write(img + "::")
        file.write(writer + "::")
        file.write(writeTime + "::")
        file.write(content + "::")

        boo = True
        # 제일 많이 언급 안되는 단어 순서대로 classify를 나눔.
        # https://wordrow.kr
        # 뼈를 포함하는 모든 글자의 단어: 1,677개
        # 간을 포함하는 모든 글자의 단어: 13,128개
        # 눈을 포함하는 모든 글자의 단어: 3,278개
        for x in bone:
            if x in content and boo:
                print("CLASSIFY:: ", "bone", " ", x)
                file.write("뼈\n")
                boo = False
        for x in diet:
            if x in content and boo:
                print("CLASSIFY:: ", "diet", " ", x)
                file.write("다이어트\n")
                boo = False
        for x in energy:
            if x in content and boo:
                print("CLASSIFY:: ", "energy", " ", x)
                file.write("활력\n")
                boo = False
        for x in digestive:
            if x in content and boo:
                print("CLASSIFY:: ", "digestive", " ", x)
                file.write("위\n")
                boo = False
        for x in eye:
            if x in content and boo:
                print("CLASSIFY:: ", "eye", " ", x)
                file.write("눈\n")
                boo = False
        for x in liver:
            if x in content and boo:
                print("CLASSIFY:: ", "liver", " ", x)
                file.write("간\n")
                boo = False
        if boo:
            print("CLASSIFY:: ", "extra")
            file.write("기타\n")
        print("\n")

    #    print("CLASSIFY:: ", soup.select_one(""))
    except:
        print("NO CONTENT")


# 크롤링할 사이트 url(경향신문)
news = []
url = "https://www.khan.co.kr/life/health/articles"
driver.get(url)
html = driver.page_source
soup = BeautifulSoup(html,'html.parser')

# news에 각 기사의 링크들을 추출하여 넣는다
for url in soup.select('div ul li h2 a'):
    news.append(url['href'])
print(news)

# 각 기사들의 제목, 이미지, 작성시간, 기사를 추출하여 각 항목에 맞게 분류
for url in news:
    try:
        driver.get(url)
        html = driver.page_source
        soup = BeautifulSoup(html, 'html.parser')
        print("URL :: ", url)
        title = soup.select_one('div h1').text
        print("TITLE :: ", soup.select_one('div h1').text)
        img = soup.select_one('div.art_body img')['src']
        print("IMG :: ", soup.select_one('div.art_body img')['src'])
        writer = soup.select_one('span.author a').text
        print("WRITER ::", writer)
        writeTime = ' '.join(s for s in soup.select_one('div.byline').text.strip().replace("\n", "").split(' ')[2:4])[:-2]
        print("TIME :: ", ' '.join(s for s in soup.select_one('div.byline').text.strip().replace("\n", "").split(' ')[2:4])[:-2])
        content = soup.select_one('div.art_body').text.strip().replace("\n", "")
        print("CONTENT :: ", content)

        file.write(url + "::")
        file.write(title + "::")
        file.write(img + "::")
        file.write(writer + "::")
        file.write(writeTime + "::")
        file.write(content + "::")

        boo = True
        for x in bone:
            if x in content and boo:
                print("CLASSIFY:: ", "bone", " ", x)
                file.write("뼈\n")
                boo = False
        for x in diet:
            if x in content and boo:
                print("CLASSIFY:: ", "diet", " ", x)
                file.write("다이어트\n")
                boo = False
        for x in energy:
            if x in content and boo:
                print("CLASSIFY:: ", "energy", " ", x)
                file.write("활력\n")
                boo = False
        for x in digestive:
            if x in content and boo:
                print("CLASSIFY:: ", "digestive", " ", x)
                file.write("위\n")
                boo = False
        for x in eye:
            if x in content and boo:
                print("CLASSIFY:: ", "eye", " ", x)
                file.write("눈\n")
                boo = False
        for x in liver:
            if x in content and boo:
                print("CLASSIFY:: ", "liver", " ", x)
                file.write("간\n")
                boo = False
        # 아무런 항목에도 걸리지 않는경우 extra를 준다
        if boo:
            print("CLASSIFY:: ", "extra")
            file.write("기타\n")
            print("\n")

    # 예외처리
    except BaseException as b:
        print("NO CONTENT" + str(b))
