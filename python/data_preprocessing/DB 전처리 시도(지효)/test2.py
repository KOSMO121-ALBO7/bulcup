f=open('C:/Users/kosmo/Desktop/최종 프로젝트/DB가공자료/건강기능식품 품목제조 신고사항 현황(1001-2000).JSON','rt',encoding='utf-8 sig')
data=f.read()
f.close()

#print(origin_data)

import json
jdata=json.loads(data)

row=jdata['I0030']['row']

#print(type(row))

for i in row :
    functional_food_name=i['PRDLST_NM']
    formulation=i['PRDT_SHAP_CD_NM']
    functionalities=i['PRIMARY_FNCLTY'].split('\n')
    BSSH_NM = i['BSSH_NM']
    # print(PRDLST_NM)
    # print(PRIMARY_FNCLTY)
    # print(BSSH_NM)
    for i in range(len(functionalities)) :
        if functionalities[i].find("관절") != -1:
            print(functional_food_name)
            print(functionalities[i])
            print(BSSH_NM)
        elif functionalities[i].find("눈") != -1:
            print(functional_food_name)
            print(functionalities[i])
            print(BSSH_NM)
        elif functionalities[i].find("간") != -1:
            print(functional_food_name)
            print(functionalities[i])
            print(BSSH_NM)
        elif functionalities[i].find("체지방") != -1:
            print(functional_food_name)
            print(functionalities[i])
            print(BSSH_NM)
        elif functionalities[i].find("에너지") != -1:
            print(functional_food_name)
            print(functionalities[i])
            print(BSSH_NM)
        elif functionalities[i].find("장") != -1:
            print(functional_food_name)
            print(functionalities[i])
            print(BSSH_NM)
