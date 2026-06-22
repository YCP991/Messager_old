import pymysql

conn = pymysql.connect(
    host='localhost',
    user='root',
    password='1234',
    database='maisizhe',
    charset='utf8mb4'
)

cursor = conn.cursor()
cursor.execute("UPDATE im_group SET name='22级1班交流群' WHERE id=5001")
cursor.execute("UPDATE im_group SET name='计算机科学与技术' WHERE id=5002")
cursor.execute("UPDATE im_group SET name='软件工程2班' WHERE id=5003")
conn.commit()

cursor.execute("SELECT id, name FROM im_group")
for row in cursor.fetchall():
    print(f"id={row[0]}, name={row[1]}")

cursor.close()
conn.close()
