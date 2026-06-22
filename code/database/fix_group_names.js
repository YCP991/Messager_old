const mysql = require('mysql2/promise');

async function fixGroupNames() {
  const conn = await mysql.createConnection({
    host: 'localhost',
    user: 'root',
    password: '1234',
    database: 'maisizhe',
    charset: 'utf8mb4'
  });

  await conn.execute("UPDATE im_group SET name='22级1班交流群' WHERE id=5001");
  await conn.execute("UPDATE im_group SET name='计算机科学与技术' WHERE id=5002");
  await conn.execute("UPDATE im_group SET name='软件工程2班' WHERE id=5003");

  const [rows] = await conn.execute("SELECT id, name FROM im_group");
  rows.forEach(r => console.log(`id=${r.id}, name=${r.name}`));

  await conn.end();
}

fixGroupNames().catch(e => { console.error(e); process.exit(1); });
