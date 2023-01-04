from selenium import webdriver
from bs4 import BeautifulSoup

#리스트에 담기위해 선언
news = []
# 크롬 드라이버 설정
driver = webdriver.Chrome('../cWebConn/4_selenium_class/webdriver/chromedriver.exe')

# 만들어 놓은 파일에 설정해놓은 단어가 있을시 해당 단어를 설정해준다
eye = open("fileEye.txt", "r", encoding='UTF-8').read().split(', ')
diet = open("fileDiet.txt", "r", encoding='UTF-8').read().split(', ')
liver = open("fileLiver.txt", "r", encoding='UTF-8').read().split(', ')
digestive = open("fileDigestive.txt", "r", encoding='UTF-8').read().split(', ')
bone = open("fileBone.txt", "r", encoding='UTF-8').read().split(', ')
energy = open("fileEnergy.txt", "r", encoding='UTF-8').read().split(', ')

# 크롤링할 사이트 url(경향신문)
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
        print("TITLE :: ", soup.select_one('div h1').text)
        print("IMG :: " ,soup.select_one('div.art_body img')['src'])
        print("TIME :: ",' '.join(s for s in soup.select_one('div.byline').text.strip().replace("\n", "").split(' ')[2:4])[:-2])
        content = soup.select_one('div.art_body').text.strip().replace("\n", "")
        print("CONTENT :: ",content)
        boo = True
        for x in bone:
                if x in content and boo:
                    print("CLASSIFY:: ", "bone", " ", x)
                    boo = False
        for x in diet:
                if x in content and boo:
                    print("CLASSIFY:: ", "diet", " ", x)
                    boo = False
        for x in energy:
                if x in content and boo:
                    print("CLASSIFY:: ", "energy", " ", x)
                    boo = False
        for x in digestive:
                if x in content and boo:
                    print("CLASSIFY:: ", "digestive", " ", x)
                    boo = False
        for x in eye:
                if x in content and boo:
                    print("CLASSIFY:: ", "eye", " ", x)
                    boo = False
        for x in liver:
                if x in content and boo:
                    print("CLASSIFY:: ", "liver", " ", x)
                    boo = False
        # 아무런 항목에도 걸리지 않는경우 extra를 준다
        if(boo):
            print("CLASSIFY:: ", "extra")
            print("\n")

    # 예외처리
    except:
        print("NO CONTENT")





