# DLog
### Step 1. Add the JitPack repository to your build file
Add it in your root build.gradle at the end of repositories:
```
allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
```
Step 2. Add the dependency
```
dependencies {
	        implementation 'com.github.dawn25618:DLog:1.0.0'
	}
```
#### 一、只要打印
直接使用`LcLog.e("123");`
```
E/lk_log: {"time":"2020-01-09 13:54:22 961","Thread":"main","stack":"com.lucky.myapplication.MainActivity.onCreate(MainActivity.java:15)","content":"123"},
```
#### 二、需要bugly检测
在自定义application中设置
```
LogConfiguration logConfiguration = new LogConfiguration.Builder()
                                .setBuglyAppId("b9e182e61e")//设置buglyAppId
                //                .setTag("{自定义TAG}")//自定义tag
                //                .setLogLevel(LogLevel.LOG_ALL)//all:打印所有，realese:打印i,w,e
                //                .setBuglyAppVersion("1.0")//设置bugly版本号
                .build();
        LcLog.init(getApplicationContext(),logConfiguration);
```
#### 三、需要发送邮件
如果app没有检测权限
LcLog.checkPermission(this);
```
 EmailConfiguration emailConfiguration = new EmailConfiguration.Builder()
                .setUserName("{邮箱平台用户名}")
                .setAuthCode("{设置的授权密码}")
                .setFromEmail("{发送邮箱}")
                .setFromName("{发送用户名}")
                .setToEmail("{接受邮箱}")
                .setPlatform("{发送平台}")
                .build();
        LcLog.sendMail(emailConfiguration);
```
