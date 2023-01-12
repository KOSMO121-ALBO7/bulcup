import pymysql
import requests
import json
# import pandas as pd
# from pandas import json_normalize

base_url='http://openapi.foodsafetykorea.go.kr/api/'
key='8ffea9eba4da4bc4a8da'
url_last='/I0030/json/1/1000'
response=requests.get(base_url+key+url_last)
#print(response)
contents=response.text
#print(contents)
#print(type(contents))

data=json.loads(contents)
stat=data['I0030']['row']
#print(origin_data)
print(stat)

#df=json_normalize(stat)

#print(df)
