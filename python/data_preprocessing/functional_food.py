import json
import urllib.request as req
import pymysql
import re
import traceback
import api_key
import csv

base_url = 'http://openapi.foodsafetykorea.go.kr/api/'
key = api_key.second_key
url_last = '/I0030/json/1/1000'


def create_functional_food_file():
    url = base_url + key + url_last
    with req.urlopen(url) as response:
        cf_str_data = response.read().decode("utf-8-sig")
        with open('origin_data/functional_food.txt', 'x', encoding='utf-8-sig') as f:
            f.write(cf_str_data)


def create_crawling_need_file(preprocessing_data2):
    for data2 in preprocessing_data2:
        search_condition_list = [data2['PRDLST_NM'], data2['BSSH_NM']]
        data2['PRDLST_NM'] = re.sub(r'\s*\W\s*', '&', data2['PRDLST_NM'])
        data2['PRDLST_NM'] = re.sub(r'\s*&\w&\s*', '&', data2['PRDLST_NM'])
        data2['PRDLST_NM'] = re.sub(r'&+', '&', data2['PRDLST_NM'])
        data2['PRDLST_NM'] = re.sub(r'\s*&\w&\s*', '&', data2['PRDLST_NM'])
        data2['PRDLST_NM'] = re.sub(r'^\w&\s*', '', data2['PRDLST_NM'])
        data2['PRDLST_NM'] = re.sub(r'&\w\s*$', '', data2['PRDLST_NM'])
        data2['PRDLST_NM'] = data2['PRDLST_NM'].strip().strip('&')
        data2['PRDLST_NM'] = re.sub(r' ', '&', data2['PRDLST_NM'])
        search_condition_list.append(data2['PRDLST_NM'])
        # print(data2['PRDLST_NM'])

        with open('../crawling/search_condition.csv', 'a+', encoding='utf-8-sig', newline='') as f:
            writer = csv.writer(f)
            writer.writerow(search_condition_list)


def functional_food_preprocessing(orig_list):
    return_list = []
    for data in orig_list:
        # 불필요한 제품 제거
        if data['PRDLST_NM'].find('수출') != -1:
            continue
        if re.search(r'\d+종', data['PRDLST_NM']) is not None:
            continue
        if data['BSSH_NM'].find('메디오젠') != -1:
            continue
        if re.search(r'^\+\s*\w+\s*', data['PRDLST_NM']) is not None:
            continue

        # 제품명
        if re.search(r'\(구\)', data['PRDLST_NM']) is not None:
            continue
        data['PRDLST_NM'] = re.sub(r'\s*\([^)]*\)\s*', ' ', data['PRDLST_NM'])
        data['PRDLST_NM'] = re.sub(r'\s*\[[^]]*]\s*', ' ', data['PRDLST_NM'])
        if re.search(r'[가-힣]', data['PRDLST_NM']) is None:
            continue
        data['PRDLST_NM'] = data['PRDLST_NM'].replace('\"', '')
        data['PRDLST_NM'] = data['PRDLST_NM'].strip()
        # print(data['PRDLST_NM'])

        # 섭취 주의 사항
        if data['IFTKN_ATNT_MATR_CN'] != '':
            # 불필요한 기호 및 특수 기호들 제거
            data['IFTKN_ATNT_MATR_CN'] = re.sub(r'\s*\([^)]*\)\s*', '/', data['IFTKN_ATNT_MATR_CN'])
            data['IFTKN_ATNT_MATR_CN'] = re.sub(r'\s*[*]\s*', '/', data['IFTKN_ATNT_MATR_CN'])
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
            data['PRIMARY_FNCLTY'] = re.sub(r'\s*\([^)]*\)\s*', '/', data['PRIMARY_FNCLTY'])
            data['PRIMARY_FNCLTY'] = re.sub(r'\s*[1-9]\)\s*', '/', data['PRIMARY_FNCLTY'])
            data['PRIMARY_FNCLTY'] = re.sub(r'\[', '', data['PRIMARY_FNCLTY'])
            data['PRIMARY_FNCLTY'] = re.sub(r']', '', data['PRIMARY_FNCLTY'])
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
            data['PRIMARY_FNCLTY'] = re.sub(r'”', '', data['PRIMARY_FNCLTY'])
            data['PRIMARY_FNCLTY'] = re.sub(r'^“', '', data['PRIMARY_FNCLTY'])
            # 복수의'/' 모두 '/' 변경 후 공백 제거 및 양끝 '/' 제거
            data['PRIMARY_FNCLTY'] = re.sub(r'\s*/+\s*', '/', data['PRIMARY_FNCLTY']).strip().strip('/').strip()
            data['PRIMARY_FNCLTY'] = re.sub(r'\s+', ' ', data['PRIMARY_FNCLTY'])
            # print(origin_data['PRIMARY_FNCLTY'])

        # 원재료
        if data['PRIMARY_FNCLTY'] != '':
            data['RAWMTRL_NM'] = re.sub(r'\s*,\s*', '/', data['RAWMTRL_NM'])
            # print(origin_data['RAWMTRL_NM'])

        # 섭취방법
        if data['PRIMARY_FNCLTY'] != '':
            data['NTK_MTHD'] = re.sub(r'^-\s*', '/', data['NTK_MTHD'])
            data['NTK_MTHD'] = re.sub(r'^\s*[\u2460-\u24FF]+\s*', '/', data['NTK_MTHD'])
            data['NTK_MTHD'] = re.sub(r'\n\s*[\u2460-\u24FF]+\s*', '/', data['NTK_MTHD'])
            data['NTK_MTHD'] = re.sub(r'\n\s*[1-9]\)\s*', '/', data['NTK_MTHD'])
            data['NTK_MTHD'] = re.sub('2. ', '/', data['NTK_MTHD'])
            # print(origin_data['NTK_MTHD'])

        # 제조사
        if data['BSSH_NM'] != '':
            data['BSSH_NM'] = re.sub(r'\([^)]*\)', '', data['BSSH_NM'])
            data['BSSH_NM'] = re.sub(r'주\)', '', data['BSSH_NM'])
            data['BSSH_NM'] = re.sub(r'\s*주식회사\s*', '', data['BSSH_NM'])
            data['BSSH_NM'] = data['BSSH_NM'].strip()

        # 불필요한 열 삭제
        del (data['LCNS_NO'])
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

        # print(data)

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
    # DB에 데이터 입력
    conn = pymysql.connect(
        host='192.168.0.52',
        user='bulcup',
        password='1234',
        database='bulcup'
    )
    # 전송 객체 생성
    cur = conn.cursor()
    try:
        query = " INSERT INTO FUNCTIONAL_FOOD(id,category_id,functional_food_name,functionalities," \
                " raw_materials,caution,formulation,intake_method,manufacturer)" \
                " VALUES(nextval(functional_food_id_seq),%(category_id)s,%(PRDLST_NM)s,%(PRIMARY_FNCLTY)s," \
                " %(RAWMTRL_NM)s,%(IFTKN_ATNT_MATR_CN)s,%(PRDT_SHAP_CD_NM)s,%(NTK_MTHD)s,%(BSSH_NM)s) "
        cur.executemany(query, functional_food_list)
    except Exception as e:
        conn.rollback()
        print('예외 발생:', e)
        print(traceback.format_exc())
    else:
        conn.commit()
    finally:
        cur.close()
        conn.close()


# if not path.isfile('./origin_data/functional_food.txt'):
#     create_functional_food_file()


with open('origin_data/functional_food25.txt', 'r', encoding='utf-8-sig') as file:
    ff_str_data = file.read()
    json_data = json.loads(ff_str_data)
    temp_list = json_data['I0030']['row']

preprocessing_data = functional_food_preprocessing(temp_list)
# for data in preprocessing_data:
#     print(data)
# insert_into_functional_food(preprocessing_data)
create_crawling_need_file(preprocessing_data)
