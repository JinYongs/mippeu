#!/usr/bin/env python
# coding: utf-8

# In[ ]:


# 최종 대칭유사도 %를 얻는 함수
def image_percent(simili_value):
    import sqlite3
    import pandas as pd
    # 데이터베이스 연결 # 
    con = sqlite3.connect("C:/Users/leejy/안면비대칭/value.db")
    cursor = con.cursor()
    cursor.execute('INSERT INTO face_value VALUES({})'.format(simili_value))
    con.commit()
    # 데이터 프레임으로 만들어 준 후 이를 다시 list로 변경하고 변수에 저장 #
    df = pd.read_sql_query("SELECT * FROM face_value", con)
    image_value = list(df['value'])
    image_value.append(simili_value)
    image_value.sort()
    index = image_value.index(simili_value) +1
    percent = index / len(image_value) * 100
    con.close()
    return (int(round(percent,0)))

