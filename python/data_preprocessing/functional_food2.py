import json
import urllib.request as req
import pymysql
from os import path
import re
import traceback
import api_key

base_url='http://openapi.foodsafetykorea.go.kr/api/'
key=api_key.second_key
url_last='/I0030/json/1001/2000'

def create_functional_food_file():
    url=base_url+key+url_last
    with req.urlopen(url) as response:
        data=response.read().decode("utf-8-sig")
        with open('origin_data/functional_food2.txt', 'x', encoding='utf-8-sig') as f:
            f.write(data)


def functional_food_preprocessing(data_list):
    return_list = []
    for data in data_list:
        # 섭취 주의 사항
        if data['PRDLST_NM'] != '':
            # 불필요한 기호 및 특수 기호들 제거
            data['IFTKN_ATNT_MATR_CN'] = re.sub('\s*\([^)]*\)\s*', '/', data['IFTKN_ATNT_MATR_CN'])
            data['IFTKN_ATNT_MATR_CN'] = re.sub('\s*[*]\s*', '/', data['IFTKN_ATNT_MATR_CN'])
            data['IFTKN_ATNT_MATR_CN'] = re.sub(r'\s*\d\)\s*', '/', data['IFTKN_ATNT_MATR_CN'])
            data['IFTKN_ATNT_MATR_CN'] = re.sub(r'\s*[가-힣]\)\s*', '/', data['IFTKN_ATNT_MATR_CN'])
            data['IFTKN_ATNT_MATR_CN'] = re.sub(r'\s*[\u2460-\u24FF]+\s*', '/', data['IFTKN_ATNT_MATR_CN'])
            data['IFTKN_ATNT_MATR_CN'] = re.sub(r'\s*\d+[.]\s*', '/', data['IFTKN_ATNT_MATR_CN'])
            data['IFTKN_ATNT_MATR_CN'] = re.sub(r'\s*[.]\s+', '/', data['IFTKN_ATNT_MATR_CN'])
            data['IFTKN_ATNT_MATR_CN'] = re.sub(r'\n\d+\s*', '/', data['IFTKN_ATNT_MATR_CN'])
            data['IFTKN_ATNT_MATR_CN'] = re.sub(r'- ', '/', data['IFTKN_ATNT_MATR_CN'])
            data['IFTKN_ATNT_MATR_CN'] = re.sub(r'^-\s*', '/', data['IFTKN_ATNT_MATR_CN'])
            data['IFTKN_ATNT_MATR_CN'] = re.sub(r'^1 ', '/', data['IFTKN_ATNT_MATR_CN'])
            # 복수의'/' 모두 '/' 변경 후 공백 제거 및 양끝 '/' 제거
            data['IFTKN_ATNT_MATR_CN'] = re.sub(r'\s*/+\s*', '/', data['IFTKN_ATNT_MATR_CN']).strip().strip('/').strip()
            data['IFTKN_ATNT_MATR_CN'] = re.sub(r'\s+', ' ', data['IFTKN_ATNT_MATR_CN'])
            # print(origin_data['IFTKN_ATNT_MATR_CN'])

        # 주요 기능
        if data['PRIMARY_FNCLTY'] != '':
            data['PRIMARY_FNCLTY'] = re.sub(r'\s*\(영문\)[^가-힣]*|\s*\(국문\)\s*', '', data['PRIMARY_FNCLTY'])
            data['PRIMARY_FNCLTY'] = re.sub(r'\s*\(영문\)[^A-z]*|\s*\(국문\)\s*', '', data['PRIMARY_FNCLTY'])
            data['PRIMARY_FNCLTY'] = re.sub('\s*\([^)]*\)\s*', '/', data['PRIMARY_FNCLTY'])
            data['PRIMARY_FNCLTY'] = re.sub(r'\s*[1-9]\)\s*', '/', data['PRIMARY_FNCLTY'])
            data['PRIMARY_FNCLTY'] = re.sub(r'\[', '', data['PRIMARY_FNCLTY'])
            data['PRIMARY_FNCLTY'] = re.sub(r'\]', '', data['PRIMARY_FNCLTY'])
            data['PRIMARY_FNCLTY'] = re.sub(r'\n\d+\s*', '/', data['PRIMARY_FNCLTY'])
            data['PRIMARY_FNCLTY'] = re.sub(r'\s*[\u2460-\u24FF]+\s*', '/', data['PRIMARY_FNCLTY'])
            data['PRIMARY_FNCLTY'] = re.sub(r'^[1-9]+[.] ', '/', data['PRIMARY_FNCLTY'])
            data['PRIMARY_FNCLTY'] = re.sub(r'[1-9]+[.] ', '/', data['PRIMARY_FNCLTY'])
            data['PRIMARY_FNCLTY'] = re.sub(r'^[1-9]+\s*', '/', data['PRIMARY_FNCLTY'])
            data['PRIMARY_FNCLTY'] = re.sub(r'^1+[.]', '/', data['PRIMARY_FNCLTY'])
            data['PRIMARY_FNCLTY'] = re.sub(r'^[*]\s*', '/', data['PRIMARY_FNCLTY'])
            data['PRIMARY_FNCLTY'] = re.sub(r'^-\s*', '/', data['PRIMARY_FNCLTY'])
            data['PRIMARY_FNCLTY'] = re.sub(r'\n+-', '/', data['PRIMARY_FNCLTY'])
            data['PRIMARY_FNCLTY'] = re.sub(r'\n+[*]', '/', data['PRIMARY_FNCLTY'])
            data['PRIMARY_FNCLTY'] = re.sub(r'[”]', '', data['PRIMARY_FNCLTY'])
            data['PRIMARY_FNCLTY'] = re.sub(r'^[“]', '', data['PRIMARY_FNCLTY'])
            # 복수의'/' 모두 '/' 변경 후 공백 제거 및 양끝 '/' 제거
            data['PRIMARY_FNCLTY'] = re.sub(r'\s*/+\s*', '/', data['PRIMARY_FNCLTY']).strip().strip('/').strip()
            data['PRIMARY_FNCLTY'] = re.sub(r'\s+', ' ', data['PRIMARY_FNCLTY'])
            # print(origin_data['PRIMARY_FNCLTY'])

        # 원재료
        if data['PRIMARY_FNCLTY'] != '':
            data['RAWMTRL_NM'] = re.sub(r'[,]', '/', data['RAWMTRL_NM'])
            # print(origin_data['RAWMTRL_NM'])

        # 섭취방법
        if data['PRIMARY_FNCLTY'] != '':
            data['NTK_MTHD'] = re.sub(r'^-\s*', '/', data['NTK_MTHD'])
            data['NTK_MTHD'] = re.sub(r'^\s*[\u2460-\u24FF]+\s*', '/', data['NTK_MTHD'])
            data['NTK_MTHD'] = re.sub(r'\n\s*[\u2460-\u24FF]+\s*', '/', data['NTK_MTHD'])
            data['NTK_MTHD'] = re.sub(r'\n\s*[1-9]\)\s*', '/', data['NTK_MTHD'])
            data['NTK_MTHD'] = re.sub('2. ', '/', data['NTK_MTHD'])
            # print(origin_data['NTK_MTHD'])

        # 불필요한 열 삭제
        del (data['LCNS_NO'])
        del (data['BSSH_NM'])
        del (data['PRDLST_REPORT_NO'])
        del (data['PRMS_DT'])
        del (data['POG_DAYCNT'])
        del (data['CSTDY_MTHD'])
        del (data['PRDLST_CDNM'])
        del (data['STDR_STND'])
        del (data['HIENG_LNTRT_DVS_NM'])
        del (data['PRODUCTION'])
        del (data['CHILD_CRTFC_YN'])
        del (data['DISPOS'])
        del (data['FRMLC_MTRQLT'])
        del (data['INDUTY_CD_NM'])
        del (data['LAST_UPDT_DTM'])
        del (data['INDIV_RAWMTRL_NM'])
        del (data['ETC_RAWMTRL_NM'])
        del (data['CAP_RAWMTRL_NM'])

        # 조건을 걸어 조건과 일치할 때 만, return_list에 값을 할당함.
        if data['PRIMARY_FNCLTY'].find("눈") != -1:
            data['category_id'] = 1
            return_list.append(data)
        elif data['PRIMARY_FNCLTY'].find("소화") != -1:
            data['category_id'] = 2
            return_list.append(data)
        elif data['PRIMARY_FNCLTY'].find("장") != -1:
            data['category_id'] = 2
            return_list.append(data)
        elif data['PRIMARY_FNCLTY'].find("관절") != -1:
            data['category_id'] = 3
            return_list.append(data)
        elif data['PRIMARY_FNCLTY'].find("간") != -1:
            data['category_id'] = 4
            return_list.append(data)
        elif data['PRIMARY_FNCLTY'].find("체지방") != -1:
            data['category_id'] = 5
            return_list.append(data)
        elif data['PRIMARY_FNCLTY'].find("피로") != -1:
            data['category_id'] = 6
            return_list.append(data)

    return return_list

def insert_into_functional_food(functional_food_list):
    #DB에 데이터 입력
    conn=pymysql.connect(
        host='192.168.0.52',
        user='bulcup',
        password='1234',
        database='bulcup'
    )
    #전송 객체 생성
    cur=conn.cursor()
    try:
        query="INSERT INTO FUNCTIONAL_FOOD(id,category_id,functional_food_name,functionalities,raw_materials,caution,formulation,intake_method)" \
              " VALUES(nextval(functional_food_id_seq),%(category_id)s,%(PRDLST_NM)s,%(PRIMARY_FNCLTY)s,%(RAWMTRL_NM)s,%(IFTKN_ATNT_MATR_CN)s,%(PRDT_SHAP_CD_NM)s,%(NTK_MTHD)s) "
        cur.executemany(query,functional_food_list)
    except Exception as e:
        print('예외 발생:', e)
        print(traceback.format_exc())
    else:
        conn.commit()
    finally:
        cur.close()
        conn.close()






if not path.isfile('origin_data/functional_food2.txt'):
    create_functional_food_file()

with open('origin_data/functional_food2.txt', 'r', encoding='utf-8-sig') as f:
    str_data=f.read()
    json_data=json.loads(str_data)
    temp_list=json_data['I0030']['row']


#key_list=[1,2,3,4,5,6]

preprocessing_data=functional_food_preprocessing(temp_list)
#print(preprocessing_data)

insert_into_functional_food(preprocessing_data)




