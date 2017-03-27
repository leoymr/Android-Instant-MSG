# IEMS 5722 Assgn4
# 1155080901 Yang Murong
from celery import Celery
from flask import Flask
import requests

def make_celery(app):
    celery = Celery(app.import_name, broker=app.config['CELERY_BROKER_URL'])
    celery.conf.update(app.config)
    TaskBase = celery.Task

    class ContextTask(TaskBase):
        abstract = True

        def __call__(self, *args, **kwargs):
            with app.app_context():
                return TaskBase.__call__(self, *args, **kwargs)
    celery.Task = ContextTask
    return celery

app = Flask(__name__)
app.config.update(
    CELERY_BROKER_URL='amqp://guest@0.0.0.0'
)
celery = make_celery(app)

@celery.task()
def notify(chatroom_id, chatroom_name, msg, token):
	api_key = 'AAAAMpMSMWc:APA91bG3HfFFoPY6LUu9dmVvL3i5A_q6DI-D7Vz47L6KY1wrGZBWEDqKX7S8cQrMfxXobYKfCy8M5kkqIwpe40S75k9BJ2DBCZE-XZufwZEvkYfNiF-rsXIs9poc3wI76Ucek19fjkbq'
	url = 'https://fcm.googleapis.com/fcm/send'

	headers = {
		'Authorization': 'key=' + api_key,
		'Content-Type': 'application/json'
	}

	device_token = token
	payload = {
		'to' : device_token,
		'notification' : {
			"title": chatroom_name,
			"tag": chatroom_id,
			"body": msg
		}
	}

	r = requests.post(url, headers = headers, json = payload)
	if r.status_code == 200:
		print "Request sent to FCM server successfully!"
		
if __name__ == '__main__': 
	app.run(host='0.0.0.0')





