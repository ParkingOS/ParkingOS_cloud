
关于车主端的所有资料，回头我会上传到svn车主端根目录下（https://svn.zld.cn/svn/zld_andorid/user/resources）

1、检查更新功能说明：（已过时，使用Umeng自动更新SDK替代）

    update.xml文件内容如下：

        <?xml version="1.0" encoding="utf-8"?>
        <content>
            <force>1</force>
            <remind>1</remind>
            <md5>36f3de8acdf436ffbc70a12bec6bb7e8</md5>
            <versionCode>1901</versionCode>
            <versionName>v2.0-beta1</versionName>
            <description>（）修复：版本升级导致应用闪退的BUG。</description>
            <apkurl>http://d.tingchebao.com/downfiles/tingchebao_v2.0beta.apk</apkurl>
        </content>

        字段描述：
            force:0表示强制更新，其他不强制
            remind:0不提醒更新，其他提醒
            md5:新版本APK文件的MD5值，请用当前目录下MD5.jar工具生成（源文件为：渠道website版本）
            versionCode:版本号，与build.gradle中版本号保持一致
            versionName:版本名，与build.gradle中版本名保持一致
            description:更新说明，多条的话，中间以英文分号(;)隔开
            apkurl:新版本的APK下载地址（正式版与公测版不一样，需注意）

            另：
                正式版更新信息请编辑update.xml文件，公测版更新请编辑update_beta.xml,请勿修改文件名
                update.xml文件多一个version字段，目的是为了兼容老版本，2.0版本以后将不再解析这个字段，用versionCode和versionName替代

2、build.gradle文件说明

    buildTypes:
        debug:开发版本
            特性：
                （1）可切换服务器地址
                （2）自动登录（参看TCBApp代码，主要就是在config的Preferences中保存手机号）
                （3）控制台日志输出
                （4）设置－关于－》版本号带debug字样
                （5）keystore采用studio默认签名，不能进行微信相关测试，如分享，支付等
                （6）不混淆代码
        alpha:内部测试人员测试
            特性：
                （1）可切换服务器地址
                （2）不自动登录
                （3）控制台日志输出
                （4）设置－关于－》版本号带alpha字样
                （5）keystore采用正式签名，能进行微信相关测试，如分享，支付等
                （6）不混淆代码
        beta:小范围公测
            特性：
                （1）不可切换服务器地址
                （2）不自动登录
                （3）不输出日志
                （4）设置－关于－》版本号带beta字样
                （5）keystore采用正式签名，能进行微信相关测试，如分享，支付等
                （6）软件名带内测版字样
                （7）与正式版没什么本质区别
        release:正式版

    productFlavor：
        各友盟版本渠道包

    正式发布版本打包后在项目根目录下，以_Baidu,_Tencent等结尾，表示各个渠道包，/build/...目录下的各个包是未经过zip压缩优化的

3、新版本版本更新问题：
    从2.0.1版本后采用友盟自动更新SDK更新软件，每次将打包好的各渠道包除上传各市场外，还应往友盟自动更新后台上传一份，按渠道更新

4、关于资源文件的一些说明

        drawable：
            一般图标类命名格式为：ic_xxx，
            背景类图片：img_xxx,
            xxx为图片正式命名，说明图片用途，后面可能会跟上_disable,_normal等状态说明

            selector选择器：
                selector_用途_状态，类似格式

            shape形状资源：
                以shape_开头，格式同上

        color:
            统一放在color.xml文件中，方便统一

        string：
            一般xml文件中直接硬编码字符串了（因为不涉及国际化），少数例外：如引用简单html代码时，

            shared_prefs_keys.xml:存放preferences文件中key值，在代码中一律通过getString()引用作为键

5、关于代码的一些说明：

    （1）TCBApp(Application Context)中存放一些全局变量：

        服务器地址：mServerUrl，http://xxx/zld/

        手机号：mMobile，当前用户手机号，作为用户是否登录的唯一标志，为空表示用户未登录

        软件配置文件（Preferences）有两个：一个名为config.xml，保存软件通用配置信息，一个名为：手机号.xml，其中手机号为当前登录用户手机号，用来保存登录用户的一些配置信息，如车牌号等
            一般情况下，操作config.xml文件时，尽量使用TCBApp类中提供的通用方法即可（可保存int，boolean等，可同步保存或异步保存）
            获取手机号.xml文件时，也可使用TCBApp中提供的方法

        提供了静态工厂方法获取ApplicationContext，方便在普通java类中获取Context引用

    （2）关于网络访问框架：
        之前用的是AQuery，后面换成了Volley，未能完全替换，推荐新接口使用volley，同样，TCBApp中全局保存了一个RequestQueue，
        一般情况下，将Request添加到那个全局的RequestQueue中即可，也可使用TCBApp中提供的方法

        上传文件：请使用UploadRequest。protocal子包下面

    （3）日志输出：
        请使用util子包下面LogUtils工具类输出日志

    （4）参看一下util包下工具类，能用那里面的工具尽量用那里面的工具类，保持一致，特别是URLUtils类

    （5）轮询服务器用的protocal子包下面的PollingProtocal类，采用Handler＋Volley实现的，暂时主要用在三方支付完成轮询服务器获取支付结果用，也可用于主动发送短信登录那一块

    （6）jni有一个tingchebao.so文件，主要存放支付宝的私钥信息，就几个字符串，一般不用管

    （7）界面之前全部是Activity，后面大部分改成Fragment，未改完，其中MainActivity主要用来挂载单个Fragment，
        NetWorkFragment抽取了网络访问的Fragment的一些公共逻辑（因为大部分界面都是：初始化界面 -->> 访访问网络 -->>更新界面的公共逻辑 ）简单抽取了下，不完善

6、待完善的一些问题

    （1）友盟里面2.0＋的bug，主要是是Fragment生命周期问题

    （2）项目分包混乱，当然能重新架构最好

    （3）网络访问问题，全部替换成volley最好，(第二个交接留言， 由于关于部分关于交易使用的android-query完成，并不能全部替换成volley)

    （4）二维码问题：既用了zbar（扫描），又用了zxing（生成），统一用某一种就行了.(不需要改，只有项目重构第三版才能，改成zxing)

    （5）登录机制不完善：目前就是本地config的Preferences文件中存手机号表示已登录

    （6）支付宝SDK可能需要更新

    （7）NetWorkFragment实现不好，没有解决好Fragment和Activity通信的问题，
        特别是那个EmptyFragment的加入，会导致Fragment管理混乱

        ...
     (8) 百度地图覆盖物 可能需要更新lib.
     
7. 签名在/user/tingchebao_user/tingchebao.keystore中，密码在/user/tingchebao_user/gradle.properties 别名 停车宝。