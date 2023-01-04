import pymysql
import pandas as pd

#작업할 CSV 파일 로드
df=pd.read_csv('C:/Users/kosmo/Desktop/최종 프로젝트/DB가공자료/자가진단 문항.csv')
# DB 연결
conn = pymysql.connect(
    host='192.168.0.52',
    user='bulcup',
    password='1234',
    database='bulcup',
)
# 연결객체 생성
cursor=conn.cursor()
# csv 파일을 읽어, 인덱스와 로우가 존재하는 경우 입력 쿼리문 전송 및 DB 입력
for index,row in df.iterrows():
    tu=(row.category_id,row.sympthom_question)
    cursor.execute("INSERT INTO sympthom_question(id,category_id,sympthom_question) VALUES (nextval(sympthom_question_id_seq),%s,%s)",tu)
# 입력 결과 커밋
conn.commit()
# 연결객체 종료
cursor.close()