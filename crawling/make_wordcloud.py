from wordcloud import WordCloud, STOPWORDS
from PIL import Image
import matplotlib.pyplot as plt
import numpy as np
import pandas as pd

# count.csv 파일 읽기
df = pd.read_csv("count.csv", encoding='cp949')
# 생성된 데이터 프레임을 딕셔너리로 변환
wc = df.set_index("title").to_dict()["count"]

wordcloud = WordCloud(
    font_path="malgun",         # 폰트
    width=600,                  # 너비
    height=400,                 # 높이
    max_font_size=100,          # 빈도수 높은 단어 폰트 사이즈
    background_color='white',   # 배경색
    colormap='Set2'             # 폰트 색상

).generate_from_frequencies(wc)

plt.figure()
plt.imshow(wordcloud)
# 워드클라우드 축 라벨 없애기
plt.axis('off')
# 워드클라우드 이미지 저장 
plt.savefig("wc.png")
# 출력 (이미지 저장 후 출력해야 함)
plt.show()
