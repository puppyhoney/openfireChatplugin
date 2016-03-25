insert into ofProperty(name, propValue) values
-- 是否输出debug信息
('plugin.yaowangchat.debug', 'false'),
-- 是否启用群消息离线推送
('plugin.yaowangchat.shutdown', 'false'),
-- 是否推送至离线用户	
('plugin.yaowangchat.can2offline', 'true'),
-- 聊天组配置	
('plugin.yaowangchat.chatgroup', 'chatgroup'),
-- dnw地址
('plugin.yaowangchat.dnwurl', 'http://tiosapi.kkt.com'),
-- 插件接口token
('plugin.yaowangchat.token', '123456');

insert into ofProperty(name, propValue) values
-- 关键字屏蔽
('plugin.yaowangchat.keyword.shield', '触[ |\d]*手'),
-- 关键字过滤
('plugin.yaowangchat.keyword.filtration', '你妈,你妈逼,畜生,禽兽,操,操你妈');


insert into ofProperty(name, propValue) values
-- 关键字过滤
('plugin.yaowangchat.blacklist', '');
