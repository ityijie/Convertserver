# Convertserver

操作日志(Job以长服务驻留)

1.Yarn 冗机的情况Job继续运行

2.job-submit.shell 需要改成Yarn提交

3.测试将Yarn进程kill -9  再提交任务 (submit-port.sh)

4.再次重启yarn时，该Job作废，需要重新提交job

5.后期可以将submit-port.sh 该脚本集成到 spring-boot中.界面化操作

6.只能以client模式提交，不能以cluster模式提交
  
  ps:client是提交的机子作为客户端, cluster是集群随机分配客户端, 如果是集群模式,端口号无法得知要请求那个机子.


