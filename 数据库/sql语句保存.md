### 数据库地址
/data/user/0/com.android.mms/databases/mmssms.db

### 创建rcs_chatbot表
CREATE TABLE IF NOT EXISTS rcs_chatbot (
    _id INTEGER PRIMARY KEY AUTOINCREMENT ,
    service_id TEXT NOT NULL ,  
    service_name TEXT ,    
    service_description TEXT ,   
    service_icon TEXT ,   
    callback_phone_number TEXT ,   
    sms TEXT ,   
    category_list TEXT ,   
    brief INTEGER DEFAULT 0 ,   
    favorite INTEGER DEFAULT 0 ,   
    email TEXT ,   
    website TEXT ,   
    address TEXT ,   
    address_lable TEXT ,   
    last_suggested_list TEXT ,   
    pinyin TEXT ,   
    pinyin_short TEXT ,   
    colour TEXT ,   
    background_image TEXT , 
    verified INTEGER DEFAULT 0 ,    
    verified_by TEXT ,    
    verified_expires TEXT ,    
    expires TEXT ,    
    cache_control TEXT ,    
    e_tag TEXT
    );

### 创建rcs_chatbot_message表，保存小程序会话消息
CREATE TABLE IF NOT EXISTS rcs_chatbot_message (
    _id INTEGER PRIMARY KEY,
    thread_id INTEGER,
    address TEXT,
    m_size INTEGER,
    person INTEGER,
    date INTEGER,
    date_sent INTEGER DEFAULT 0,
    protocol INTEGER,
    read INTEGER DEFAULT 0,
    status INTEGER DEFAULT -1, 
    type INTEGER,
    reply_path_present INTEGER,
    subject TEXT,
    body TEXT,
    service_center TEXT,
    locked INTEGER DEFAULT 0,
    sub_id INTEGER DEFAULT -1, 
    error_code INTEGER DEFAULT 0,
    creator TEXT,  
    seen INTEGER DEFAULT 0,
    is_encrypted INTEGER DEFAULT 0,
    time INTEGER DEFAULT 0,
    dirty INTEGER DEFAULT 1,
    message_mode INTEGER DEFAULT 0 ,
    priority INTEGER DEFAULT -1 ,
    phone_id INTEGER DEFAULT -1 ,
    is_exec_trigger INTEGER DEFAULT 1 ,
    verify_code INTEGER DEFAULT 0 ,
    risk_website INTEGER DEFAULT 0 ,
    bubble TEXT DEFAULT -1 ,
    bubble_type INTEGER DEFAULT 1 ,
    black_type INTEGER DEFAULT -1 ,
    bubble_parse_time INTEGER DEFAULT 0 , 
    rcs_show_time INTEGER DEFAULT 1 ,
    group_id TEXT ,
    block_sms_type INTEGER DEFAULT 0 ,
    sms_extend_type INTEGER DEFAULT 0 ,
    dynamic_bubble TEXT DEFAULT -1 ,
    dynamic_update_date INTEGER DEFAULT 0,
    favourite INTEGER DEFAULT 0,
    rcs_message_id TEXT,
    rcs_file_name TEXT,
    rcs_mime_type TEXT,
    rcs_msg_type INTEGER DEFAULT -1,
    rcs_msg_state INTEGER,
    rcs_chat_type INTEGER DEFAULT -1,
    rcs_conversation_id TEXT,
    rcs_contribution_id TEXT,
    rcs_file_selector TEXT,
    rcs_file_transfered TEXT,
    rcs_file_transfer_id TEXT,
    rcs_file_icon TEXT,
    rcs_burn INTEGER DEFAULT -1,
    rcs_header TEXT,
    rcs_file_path TEXT,
    rcs_is_download INTEGER DEFAULT 0,
    rcs_file_size INTEGER DEFAULT 0,
    rcs_thumb_path TEXT,
    rcs_extend_body TEXT,
    rcs_media_played INTEGER DEFAULT 0,
    rcs_ext_contact TEXT,
    rcs_file_record INTEGER,
    rcs_transfer_date TEXT,
    rcs_group_at_reminds TEXT,
    rcs_audio_read INTEGER DEFAULT 0,
    rcs_send_way INTEGER DEFAULT -1,
    rcs_file_expiration INTEGER DEFAULT 0,
    rcs_file_download_url TEXT,
    rcs_thumb_mime_type TEXT,
    rcs_thumb_expiration TEXT,
    rcs_thumb_download_url TEXT,
    show_msg_category INTEGER DEFAULT 0 
    );

### 创建rcs_chatbot_threads表，保存小程序会话列表信息
CREATE TABLE IF NOT EXISTS rcs_chatbot_threads (
    _id INTEGER PRIMARY KEY AUTOINCREMENT ,
    date INTEGER DEFAULT 0 ,  
    message_count INTEGER DEFAULT 0 ,    
    readcount INTEGER DEFAULT 0 ,    
    recipient_ids TEXT ,      
    snippet TEXT ,     
    snippet_cs INTEGER DEFAULT 0 ,    
    read INTEGER DEFAULT 1 ,    
    archived INTEGER DEFAULT 0 ,    
    type INTEGER DEFAULT 0 ,      
    error INTEGER DEFAULT 0 ,   
    has_attachment INTEGER DEFAULT 0 ,   
    is_encrypted INTEGER DEFAULT 0 ,   
    unreadcount INTEGER DEFAULT 0 ,   
    sub_id INTEGER DEFAULT -1 ,   
    message_mode INTEGER DEFAULT 0 ,   
    status INTEGER DEFAULT 0 , 
    time INTEGER DEFAULT 0 ,     
    snippet_verify_code TEXT , 
    verify_code INTEGER DEFAULT 0 ,   
    color INTEGER DEFAULT 0 ,   
    topindex INTEGER DEFAULT 0 ,     
    v_address_name TEXT ,      
    v_address_type INTEGER DEFAULT 0 ,   
    v_address_from INTEGER DEFAULT -1 ,     
    thread_key TEXT ,    
    biz_type INTEGER DEFAULT -1 ,   
    extend_type INTEGER DEFAULT 0 ,  
    rcs_top INTEGER DEFAULT 0 ,  
    rcs_top_time INTEGER DEFAULT 0 ,  
    rcs_number INTEGER DEFAULT 0 ,  
    last_msg_id INTEGER DEFAULT -1 ,  
    last_msg_type INTEGER DEFAULT -1 ,    
    msg_chat_type INTEGER DEFAULT -1 ,    
    rcs_chatbot_id INTEGER DEFAULT -1  
    );

### 往rcs_chatbot_threads表添加数据
insert into rcs_chatbot_threads(topindex,message_count,recipient_ids,snippet,snippet_cs,error,is_encrypted,unreadcount,sub_id,time,date,v_address_name,v_address_type,v_address_from,read,has_attachment,rcs_top,rcs_top_time,last_msg_id,last_msg_type,msg_chat_type,rcs_number,biz_type) values (1,11,'recipient_ids1','snippet1',111,1111,11111,111111,1111111,1573463965226,1573463971111,'v_address_name1',0,0,0,0,0,1573463972111,-1,-1,-1,11111111,-1);	
db.execSQL("insert into rcs_chatbot_threads(topindex,message_count,recipient_ids,snippet,snippet_cs,error,is_encrypted,unreadcount,sub_id,time,date,v_address_name,v_address_type,v_address_from,read,has_attachment,rcs_top,rcs_top_time,last_msg_id,last_msg_type,msg_chat_type,rcs_number,biz_type) values (1,11,'recipient_ids1','snippet1',111,1111,11111,111111,1111111,1573463965226,1573463971111,'v_address_name1',0,0,0,0,0,1573463972111,-1,-1,-1,11111111,-1)");

insert into rcs_chatbot_threads(
topindex,message_count,recipient_ids,snippet,snippet_cs,error,is_encrypted,unreadcount,sub_id,time,date,v_address_name,v_address_type,
v_address_from,read,has_attachment,rcs_top,rcs_top_time,last_msg_id,last_msg_type,msg_chat_type,rcs_number,biz_type) 
values (
1,11,'recipient_ids1','snippet1',111,1111,11111,111111,1111111,1573463965226,1573463971111,'v_address_name1',0,0,0,0,0,1573463972111,-1,-1,-1,11111111,-1
);


insert into rcs_chatbot_threads(topindex,message_count,recipient_ids,snippet,snippet_cs,error,is_encrypted,unreadcount,sub_id,time,date,v_address_name,v_address_type,v_address_from,read,has_attachment,rcs_top,rcs_top_time,last_msg_id,last_msg_type,msg_chat_type,rcs_number,biz_type) values (1,11,'recipient_ids1','snippet1',111,1111,11111,111111,1111111,1573463965226,1573463971111,'v_address_name1',0,0,0,0,0,1573463972111,-1,-1,-1,11111111,-1);	


SELECT * FROM rcs_chatbot WHERE (rcs_chatbot.service_name LIKE '%'+'和'+'%')

SELECT * FROM rcs_chatbot WHERE (rcs_chatbot.favorite = 1) AND ((rcs_chatbot.service_name LIKE '%火车票%') OR (rcs_chatbot.service_description LIKE '%火车票%'))

SELECT * FROM rcs_chatbot WHERE ((rcs_chatbot.service_description LIKE '%和%'))

SELECT * FROM [user] WHERE u_name LIKE '%三%'


SELECT * FROM rcs_chatbot WHERE (rcs_chatbot.service_name LIKE '%"+"和"+"%')

SELECT * FROM rcs_chatbot WHERE (rcs_chatbot.favorite = 1)



























