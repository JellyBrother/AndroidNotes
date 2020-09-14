### 创建表
CREATE TABLE IF NOT EXISTS rcs_chatbot_threads (
    _id INTEGER PRIMARY KEY AUTOINCREMENT ,
    date INTEGER DEFAULT 0 ,      
    snippet TEXT ,  
    snippet_verify_code TEXT ,  
    rcs_number INTEGER DEFAULT 0 ,     
    rcs_chatbot_id INTEGER DEFAULT -1  
    );

### 添加数据
* 语法1:  insert into 表名(列名1,列名2,列名3,列名4) values (数据1,数据2,数据3,数据4)
insert into stuinfo(sid,sname,saddress,sclass,ssex) values (1,'码仙1','火星',1001,'男');
* 语法2：  insert into 表名values(数据1,数据2,数据3,数据4,数据5)   
使用限制：插入的是表中的全部列时才可以使用
insert into stuinfo values (2,'码仙2','火星',1002,'女');
* 语法3： （插入部分数据）insert  into 表名(列名1,列名2) values (数据1,数据2)
使用限制：主键和非空约束列必须添加数据
insert into stuinfo(sid,sname,sclass) values (3,'码仙3',1003);





