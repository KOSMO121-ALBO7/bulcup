import json
import urllib.request as req
import pymysql
import api_key as key
from os import path
import re
import traceback


def raw_material_preprocessing(data_list):
    return_list = []
    for data in data_list:
        if data['PRDCT_NM'] != '':
            # 제품명 전처리
            # 인정번호 제거
            data['PRDCT_NM'] = re.sub(r'\(제\d*-\d*호\)', '', data['PRDCT_NM'])
            # 불필요한 문장 제거
            data['PRDCT_NM'] = re.sub(r'\[.*]', '', data['PRDCT_NM'])
            data['PRDCT_NM'] = re.sub(r'<.*>', '', data['PRDCT_NM'])

            # 주의 사항 전처리
            # 문장 변경
            data['IFTKN_ATNT_MATR_CN'] = re.sub(r'영·유아|영유아|영-유아|영・유아|영‧유아', '영아, 유아', data['IFTKN_ATNT_MATR_CN'])
            data['IFTKN_ATNT_MATR_CN'] = re.sub(r'유아·어린이', '유아, 어린이', data['IFTKN_ATNT_MATR_CN'])
            data['IFTKN_ATNT_MATR_CN'] = re.sub(r'임산부-수유부', '임산부, 수유부', data['IFTKN_ATNT_MATR_CN'])
            data['IFTKN_ATNT_MATR_CN'] = re.sub(r'임신', '임산부', data['IFTKN_ATNT_MATR_CN'])
            data['IFTKN_ATNT_MATR_CN'] = re.sub(r'다\.', '다', data['IFTKN_ATNT_MATR_CN'])
            data['IFTKN_ATNT_MATR_CN'] = re.sub(r'오\.', '오', data['IFTKN_ATNT_MATR_CN'])
            data['IFTKN_ATNT_MATR_CN'] = re.sub(r'음\.', '음', data['IFTKN_ATNT_MATR_CN'])
            data['IFTKN_ATNT_MATR_CN'] = re.sub(r'\.', ' 및 ', data['IFTKN_ATNT_MATR_CN'])
            # 개행 ==> '/'
            data['IFTKN_ATNT_MATR_CN'] = re.sub(r'\s*\n\s*', '/', data['IFTKN_ATNT_MATR_CN'])
            # (1,가,a) ==> '/'
            data['IFTKN_ATNT_MATR_CN'] = re.sub(r'\s*\(\w\)\s*', '/', data['IFTKN_ATNT_MATR_CN'])
            # 1) ==> '/'
            data['IFTKN_ATNT_MATR_CN'] = re.sub(r'\s*\d\)\s*', '', data['IFTKN_ATNT_MATR_CN'])
            # 특수문자 처리
            data['IFTKN_ATNT_MATR_CN'] = re.sub(r'\s*[\u2460-\u24FF]+\s*', '/', data['IFTKN_ATNT_MATR_CN'])
            data['IFTKN_ATNT_MATR_CN'] = data['IFTKN_ATNT_MATR_CN'].strip('-')
            data['IFTKN_ATNT_MATR_CN'] = re.sub(r'\s*/-\s*', '/', data['IFTKN_ATNT_MATR_CN'])
            data['IFTKN_ATNT_MATR_CN'] = re.sub(r'\s*․\s*', ',', data['IFTKN_ATNT_MATR_CN'])
            data['IFTKN_ATNT_MATR_CN'] = re.sub(r'\s*·\s*', ', ', data['IFTKN_ATNT_MATR_CN'])
            data['IFTKN_ATNT_MATR_CN'] = re.sub(r'\s*※\s*', '', data['IFTKN_ATNT_MATR_CN'])
            # 복수의'/' 모두 '/' 변경 후 공백 제거 및 양끝 '/' 제거
            data['IFTKN_ATNT_MATR_CN'] = re.sub(r'\s*/+\s*', '/', data['IFTKN_ATNT_MATR_CN']).strip().strip('/').strip()
            data['IFTKN_ATNT_MATR_CN'] = re.sub(r'\s+', ' ', data['IFTKN_ATNT_MATR_CN'])

            # 기능성 전처리
            # (국문) 제거 및 '(영문)'이후 모두 제거
            data['PRIMARY_FNCLTY'] = re.sub(r'\s*\(영문\)[^가-힣]*|\s*\(국문\)\s*', '', data['PRIMARY_FNCLTY'])
            # (1,가,a) ==> '/'
            data['PRIMARY_FNCLTY'] = re.sub(r'\s*\(\w\)\s*', '/', data['PRIMARY_FNCLTY'])
            # 1. ==> '/'
            data['PRIMARY_FNCLTY'] = re.sub(r'\s*\d[.]\s*', '/', data['PRIMARY_FNCLTY'])
            # 괄호() 모두 제거
            data['PRIMARY_FNCLTY'] = re.sub(r'\s*\([^)]*\)\s*', '', data['PRIMARY_FNCLTY'])
            # 1,가,a) ==> '/'
            data['PRIMARY_FNCLTY'] = re.sub(r'\s*\w\)\s*', '', data['PRIMARY_FNCLTY'])
            # 개행 ==> '/'
            data['PRIMARY_FNCLTY'] = re.sub(r'\s*\n\s*', '/', data['PRIMARY_FNCLTY'])
            # 불필요한 문장 제거 및 변경
            data['PRIMARY_FNCLTY'] = re.sub(r'을 줄 수 있음|을 줄 수 있습니다|을 줌|을 줍니다', '', data['PRIMARY_FNCLTY'])
            data['PRIMARY_FNCLTY'] = re.sub(r'에 줄 수 있음|에 줄 수 있습니다', '', data['PRIMARY_FNCLTY'])
            data['PRIMARY_FNCLTY'] = re.sub(r'도움\s*,\s*', '도움/', data['PRIMARY_FNCLTY'])
            data['PRIMARY_FNCLTY'] = re.sub(r'도움', '도움/', data['PRIMARY_FNCLTY'])
            data['PRIMARY_FNCLTY'] = re.sub(r'/을', '을', data['PRIMARY_FNCLTY'])
            data['PRIMARY_FNCLTY'] = re.sub(r'개선에 도움', '개선/', data['PRIMARY_FNCLTY'])
            data['PRIMARY_FNCLTY'] = re.sub(r'개선[^\w\s]+', '개선에 도움/', data['PRIMARY_FNCLTY'])
            data['PRIMARY_FNCLTY'] = re.sub(r'/을', '을', data['PRIMARY_FNCLTY'])
            data['PRIMARY_FNCLTY'] = re.sub(r'이 제품은 ', '', data['PRIMARY_FNCLTY'])
            # # 특수문자 및 도트 처리
            data['PRIMARY_FNCLTY'] = re.sub(r'\s*[\u2460-\u24FF]+\s*', '/', data['PRIMARY_FNCLTY'])
            data['PRIMARY_FNCLTY'] = re.sub(r'\s*·\s+|\s*[.]\s*', '', data['PRIMARY_FNCLTY'])
            data['PRIMARY_FNCLTY'] = re.sub(r'/·', '/', data['PRIMARY_FNCLTY'])
            data['PRIMARY_FNCLTY'] = re.sub(r'·', '에 도움/', data['PRIMARY_FNCLTY'])
            data['PRIMARY_FNCLTY'] = re.sub(r'/･', '/', data['PRIMARY_FNCLTY'])
            data['PRIMARY_FNCLTY'] = re.sub(r'･', '에 도움/', data['PRIMARY_FNCLTY'])
            data['PRIMARY_FNCLTY'] = re.sub(r'\s*-\s*', '', data['PRIMARY_FNCLTY'])
            data['PRIMARY_FNCLTY'] = re.sub(r'\s*[^\w\s,/]\s*', '/', data['PRIMARY_FNCLTY'])
            # 복수의'/' 모두 '/' 변경 후 공백 제거 및 양끝 '/' 제거
            data['PRIMARY_FNCLTY'] = re.sub(r'\s*/+\s*', '/', data['PRIMARY_FNCLTY']).strip().strip('/').strip()
            data['PRIMARY_FNCLTY'] = re.sub(r'\s+', ' ', data['PRIMARY_FNCLTY'])

            # 단위 전처리
            # '/일' 제거
            data['INTK_UNIT'] = re.sub(r'/일', '', data['INTK_UNIT']).strip()

            # str ==> float
            data['DAY_INTK_LOWLIMIT'] = float(data['DAY_INTK_LOWLIMIT'])
            data['DAY_INTK_HIGHLIMIT'] = float(data['DAY_INTK_HIGHLIMIT'])

            return_list.append(data)

    return return_list


def create_raw_material_file():
    url = 'http://openapi.foodsafetykorea.go.kr/api/' + key.functional_food_notification_key + '/I2710/json/1/1000/'
    with req.urlopen(url) as response:
        # print(response)
        # print(type(response))
        data = response.read().decode("utf-8-sig")
        # print(data)
        # print(type(data))
        with open('./data/raw_material.txt', 'x', encoding='utf-8-sig') as f:
            f.write(data)


def insert_raw_material_data(raw_material_list):
    # print(type(raw_material_list[0]['DAY_INTK_LOWLIMIT']), raw_material_list[0]['DAY_INTK_LOWLIMIT'])
    # print(type(raw_material_list[0]['DAY_INTK_HIGHLIMIT']), raw_material_list[0]['DAY_INTK_HIGHLIMIT'])
    # DB에 데이터 입력
    conn = pymysql.connect(
        host='192.168.0.52',
        user='bulcup',
        password='1234',
        database='bulcup',
    )
    cursor = conn.cursor()
    try:
        query = 'INSERT INTO raw_material (id, raw_material_name, caution, functionality, ' \
                'daily_intake_min, daily_intake_max, unit) ' \
                'VALUES (NEXTVAL(raw_material_seq), %(PRDCT_NM)s, %(IFTKN_ATNT_MATR_CN)s, %(PRIMARY_FNCLTY)s, ' \
                '%(DAY_INTK_LOWLIMIT)s, %(DAY_INTK_HIGHLIMIT)s, %(INTK_UNIT)s) '
        cursor.executemany(query, raw_material_list)
    except Exception as e:
        print('예외 발생:', e)
        print(traceback.format_exc())
    else:
        conn.commit()
    finally:
        cursor.close()
        conn.close()


if __name__ == '__main__':
    if not path.isfile('./data/raw_material.txt'):
        create_raw_material_file()

    with open('./data/raw_material.txt', 'r', encoding='utf-8-sig') as f:
        str_data = f.read()
        json_data = json.loads(str_data)
        # print(json_data)
        # print(type(json_data))
        temp_list = json_data['I2710']['row']

    preprocessing_data = raw_material_preprocessing(temp_list)
    insert_raw_material_data(preprocessing_data)
