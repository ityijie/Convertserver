#!/bin/bash
port=9999
time=`date '+%s'`
# 提交任务
convert=`curl -o /dev/null -m 10 -s w %{http_code} "http://127.0.0.1${port}/convert?time=${time}"`

# 检测job是否存在,不存在则重启
# curl "http://172.20.1.101:9999/ping" >> ./log