# IEMS 5722 Assgn3
# 1155080901 Yang Murong

from flask import jsonify,Flask,request
from celery import Celery
from task import notify
import MyDatabase
import time
import math

app = Flask(__name__)

# Get chatroom list
@app.route('/api/asgn3/get_chatrooms', methods=['GET'])
def get_chatrooms():
	mydb = MyDatabase.MyDatabase()

	query = "SELECT * FROM chatrooms"
	mydb.cursor.execute(query)

	chatroom_list = mydb.cursor.fetchall()
	
	return jsonify(status="OK",data=chatroom_list)

# Get chatroom messages
@app.route('/api/asgn3/get_messages', methods=['GET'])
def get_messages():
	mydb = MyDatabase.MyDatabase()
	message_list = []

	chatroom_id = request.args.get("chatroom_id", 0, type=int) 
	page = request.args.get("page", 1, type=int)

	query = "SELECT chatroom_id,user_id,name,message,timestamp FROM messages WHERE chatroom_id = %s ORDER BY  id desc" % chatroom_id
	mydb.cursor.execute(query)

	count = 0
	total_page = 1
	count_msg = 0
	while 1:
		message = mydb.cursor.fetchone()
		if message is None :
			break
		else :
			message_list.append(message)
		count+=1
		
		if count > 10*total_page:
			total_page = total_page + 1
		if count > 10:
			count_msg = count - (total_page-1)*10
		else:
			count_msg+=1
	
	chat_message = []
	if page == total_page:
		for j in range(count_msg):
			chat_message.append(message_list[(page-1)*10+j])
	elif page < total_page:
		for i in range(10):
			chat_message.append(message_list[(page-1)*10+i])
		
	return jsonify(status="OK",total_pages=total_page,page = page,data=chat_message)

# Send msg to database and FCM
@app.route('/api/asgn3/send_message', methods=['POST'])
def send_message():
	mydb = MyDatabase.MyDatabase()
	msg = request.form.get("message")
	name = request.form.get("name")
	chatroom_id = request.form.get("chatroom_id")
	user_id = request.form.get("user_id")

	# Get chatroom name, select name as chatroom_name from chatrooms where id = chatroom_id
	select_chatroomname_query = "SELECT name from chatrooms where id = %s" % chatroom_id
	mydb.cursor.execute(select_chatroomname_query)
	chatroom_name_json = mydb.cursor.fetchone()
	chatroom_name = chatroom_name_json['name']

	if msg == None or chatroom_id == None or chatroom_id == '' or not chatroom_id.isdigit() or name == None or user_id == None :
		return jsonify(status="ERROR", message="missing parameters")
	else :
		insert_query = "INSERT INTO messages (chatroom_id,user_id,name,message,timestamp) VALUES (%s,%s,%s,%s,%s)"
		
		timestamp = time.strftime("%Y-%m-%d %H:%M:%S", time.localtime(time.time()+8*3600))
		params = (chatroom_id,user_id,name,msg,timestamp)
		
		mydb.cursor.execute(insert_query,params)
		mydb.db.commit()
		
		select_token_query = "SELECT token FROM push_tokens"
		mydb.cursor.execute(select_token_query)
		while 1:
			token_json = mydb.cursor.fetchone()
			if token_json is None:
				break
			else :
				token = token_json['token']
				notify.delay(chatroom_id, chatroom_name, msg, token)
		
		return jsonify(status="OK")
		
# Send device token to database	
@app.route('/api/asgn4/submit_push_token', methods=['POST'])
def submit_push_token():
	mydb = MyDatabase.MyDatabase()
	user_id = request.form.get("user_id")
	token = request.form.get("token")

	if token == None or user_id == None :
		return jsonify(status="ERROR", message="missing parameters")
	else :
		insert_query = "INSERT INTO push_tokens (user_id,token) VALUES (%s,%s)"
		params = (user_id, token)
		
		mydb.cursor.execute(insert_query,params)
		mydb.db.commit()
		
		return jsonify(status="OK")
		
if __name__ == '__main__': 
	app.run(host='0.0.0.0')





