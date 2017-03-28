
# FCM Notification

## RabbitMQ + Celery

参考：

http://iems5722.albertauyeung.com/files/assignments/iems5722-assignment-04.pdf

### Extending Android APP

1、先按照pdf，做好准备工作

（ps: 可以在AS里的tools中找到 firebase，通过这个方法来链接回节省一些添加依赖的步骤）

2、扩充安卓端的代码，即实现 FirebaseInstanceIDService 和 FirebaseMessagingService两个方法。

（其中，FirebaseMessagingService 用来处理 FCM 推送的消息；FirebaseInstanceIDService 用来获取每个device的token，并发送到数据库中）

在实现 FirebaseMessagingService 的类中生成 notification 方法，以在虚拟机中显示推送框。

### Extending the Server Application 

1、建一个新的api及table来存device的token

2、安装 RabbitMQ 和 Celery 以及 task.py 中需要的 requests

```
$ sudo apt-get install rabbitmq-server
$ sudo pip install celery
$ sudo pip install requests
```

3、修改 getAPI.py，在 send_message 方法中调用发送消息至FCM的方法，例如 task.py 中的 notify()。

```
notify.delay(···)
```

其中，api_key = 'AAAAMpMA···91bG3HfF' 即FCM console 上的 Server key；token 即通过 getAPI.py 中从 push_tokens 表中获取。

4、配置 Celery worker 

```
$ celery -A task.celery worker --loglevel=DEBUG
```

上述命令可以测试是否可以收到相应的推送消息

若无error，则配置一个新的supervisor conf文件，在conf.d下打开上次的.conf文件，添加新的program配置信息，command 一行即运行 celery worker的命令

```
[program:iems5722_2]
command = celery -A task.celery worker
directory = /home/ubuntu/appFlask
user = ubuntu
autostart = true
autorestart = true
stdout_logfile = /home/ubuntu/appFlask/task.log 
redirect_stderr = true
```
然后重启supervisor以及nginx。

大功告成！！！
