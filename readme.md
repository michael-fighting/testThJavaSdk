tThJavaSdk

```
git clone https://github.com/michael-fighting/testThJavaSdk.git
```
##2.启动并部署一条天河链,默认启动如下端口
```
bash build_chain.sh -l 127.0.0.1:4 -p 30300,20200,8545
```
##3.将链启动的sdk放入testThJavaSdk项目的dist/conf文件夹下（删除原有的，进行替换）
```cgo
root@centos-1:/data/dm/testThJavaSdk/dist/conf# cp -r /data/sdk/* .
```
##4. 在项目的dist文件夹下，使用以下命令，订阅者订阅主题为testTopic的消息
```cgo
java -cp "apps/*:lib/*:conf/" org.tianhe.thbc.sdk.demo.thbcmp.tool.ThbcmpSubscriber testTopic
```
##5.在项目的dist文件夹下再开一个命令行，使用以下命令，发布者发布主题为testTopic的组播消息
```cgo
root@centos-1:/data/dm/testThJavaSdk/dist# java -cp "apps/*:lib/*:conf/" org.tianhe.thbc.sdk.demo.thbcmp.tool.ThbcmpPublisher testTopic true "Tell you something" 1
```
##6. 在订阅端和发布端会看到消息的通信过程，同时在日志文件中可以看到组播消息

