const http = require('http');

// 先登录获取 token
const loginData = JSON.stringify({ username: 'zhangsan', password: '1234' });

const loginReq = http.request({
  hostname: 'localhost',
  port: 8080,
  path: '/api/auth/login',
  method: 'POST',
  headers: { 'Content-Type': 'application/json' }
}, (res) => {
  let body = '';
  res.on('data', c => body += c);
  res.on('end', () => {
    const result = JSON.parse(body);
    const token = result.data;
    console.log('Token obtained');

    // 获取群组列表
    const groupReq = http.request({
      hostname: 'localhost',
      port: 8080,
      path: '/api/group/list',
      method: 'GET',
      headers: { 'Authorization': 'Bearer ' + token }
    }, (res2) => {
      let body2 = '';
      res2.on('data', c => body2 += c);
      res2.on('end', () => {
        const groups = JSON.parse(body2);
        console.log('Groups:');
        if (groups.data) {
          groups.data.forEach(g => {
            console.log(`  id=${g.id}, name=${g.name}`);
          });
        } else {
          console.log(JSON.stringify(groups, null, 2));
        }
      });
    });
    groupReq.end();
  });
});
loginReq.write(loginData);
loginReq.end();
