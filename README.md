# 介绍
![1](image.png)\
安卓平台全自动信息编码解码软件

## 特点
基于无障碍服务,需要授权无障碍权限和悬浮窗权限,建议关闭省电模式并开启自启动\
能自动解码基本所有软件中的文本信息,并弹出快捷编码对话窗口\
一键发送功能可直接识别当前界面中的发送按钮和输入框，做到一键编码后发送

## 功能
### 一键加密
一键加密并发送您输入的内容
### 自动解密
点击文字时自动解密并显示内容对话框
### 密钥列表
可自定义添加多条密钥,解密时会自动尝试所有密钥\
支持密钥锁定,使用自定义密码一键加密并锁定密钥列表\
支持自动锁定,规定时间未使用解密功能自动加密并锁定所有密钥
### 移位加密
特点: 每次加密后结果都不相同.只比原文多三个字符,需要密钥正确才能解密\
缺点：只能加密中文英文和数字,特殊字符无法加密\
例如: 加密中文"你好"二字100次,后面的"哈哈牛逼"开头的为密钥字符串\
每次加密结果都不相同,并且只比原文多五个字,后方密钥只要错一个字符,就无法解密出任何内容\
![Alt text](image-1.png)
![Alt text](image-2.png)

### 其他加密
其他加密默认为使用特定字符集作为进制编码移位加密后的数据,拥有移位加密相同的随机结果特性

### 启动白屏
支持启动后白屏,双指放大解锁
### APP伪装
支持更改图标和应用名称伪装成其他APP

## 缺陷
一键发送功能具有缓存,可能导致发送失败,由于安卓无障碍服务不会返回到底有没有成功点击\
所以无法知道发送结果,失败自行重试即可,如果无法发送请手动发送一条消息后再试\
切换软件后,缓存会刷新,所以第一次可能会失败,保持停留在当前页面后面的操作都会成功