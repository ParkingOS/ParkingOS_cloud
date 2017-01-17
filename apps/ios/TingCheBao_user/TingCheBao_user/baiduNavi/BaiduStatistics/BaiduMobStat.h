//
//  BaiduMobStat.h
//  BaiduMobStat
//
//  Created by jaygao on 11-11-1.
//  Copyright (c) 2011年 Baidu. All rights reserved.
//

#import <Foundation/Foundation.h>

@class UIViewController;

typedef enum _BaiduMobStatLogStrategy {
    BaiduMobStatLogStrategyAppLaunch = 0, //每次程序启动
    BaiduMobStatLogStrategyDay = 1, //每天的程序第一次进入前台
    BaiduMobStatLogStrategyCustom = 2, //根据开发者设定的时间间隔接口发送
} BaiduMobStatLogStrategy;


/**
 *  百度移动应用统计接口,更多信息请查看[百度移动统计](http://mtj.baidu.com)
 *  
 */

@interface BaiduMobStat : NSObject{
    BOOL exceptionLogEnabled_;
}

/**
 *  获取统计对象的实例
 */
+ (BaiduMobStat*) defaultStat;

/**
 *  设置应用的appkey (在[百度移动统计](http://mtj.baidu.com)获取)，在其他api调用以前必须先调用该api.
 *  此处AppId即为应用的appKey
 */
-(void) startWithAppId:(NSString*) appId;

/**
 *  记录一次事件的点击，eventId请在网站上创建。未创建的evenId记录将无效。eventId与eventLabel必须是有内容的字符串，不可为nil或者空字符串。 [百度移动统计](http://mtj.baidu.com)
 */
-(void) logEvent:(NSString*) eventId eventLabel:(NSString*)eventLabel;
/**
 *  v3.0 新增
 *  记录一次事件的时长，eventId请在网站上创建。未创建的evenId记录将无效。eventId与eventLabel必须是有内容的字符串，不可为nil或者空字符串。
 */
-(void) logEventWithDurationTime:(NSString*) eventId eventLabel:(NSString*)eventLabel durationTime: (unsigned long)duration;
/**
 *  v3.0 新增 
 *  记录一次事件的开始，eventId请在网站上创建。未创建的evenId记录将无效。eventId与eventLabel必须是有内容的字符串，不可为nil或者空字符串。
 */
-(void) eventStart:(NSString*) eventId eventLabel:(NSString*)eventLabel;
/**
 *  v3.0 新增 
 *  记录一次事件的结束，eventId请在网站上创建。未创建的evenId记录将无效。eventId与eventLabel必须是有内容的字符串，不可为nil或者空字符串。
 */
-(void) eventEnd:(NSString*) eventId eventLabel:(NSString*)eventLabel;
/**
 *  标识某个页面访问的开始，请参见Example程序，在合适的位置调用。
 */
-(void) pageviewStartWithName:(NSString*) name;
/**
 *  标识某个页面访问的结束，与pageviewStartWithName配对使用，请参见Example程序，在合适的位置调用。
 */
-(void) pageviewEndWithName:(NSString*) name;

/**
 *  v1.1 新增
 *  设置或者获取渠道Id。
 *  可以不设置, 此时系统会处理为AppStore渠道
 */
@property (nonatomic,retain) NSString* channelId;

/**
 *  是否启用异常日志收集
 */
@property (nonatomic) BOOL enableExceptionLog;

/**
 *  v2.0 新增
 *  是否只在wifi连接下才发送日志
 *  默认值为 NO, 不管什么网络都发送日志
 */
@property (nonatomic) BOOL logSendWifiOnly;

/**
 *  v3.0 新增
 *  设置应用进入后台再回到前台为同一次session的间隔时间[0~600s],超过600s则设为600s，默认为30s  
 */
@property (nonatomic) int sessionResumeInterval;

/**
 *  v2.0 新增
 *  设置日志发送策略, 默认采用BaiduMobStatLogStrategyAppLaunch：启动发送
 */
@property (nonatomic) BaiduMobStatLogStrategy logStrategy;

/**
 *  v2.0 新增
 *  设置日志发送时间间隔，当logStrategy设置为BaiduMobStatLogStrategyCustom时生效
 *  单位为小时，有效值为 1 <= logSendInterval <= 24
 *  默认值 为1
 */
@property (nonatomic) int logSendInterval;

/**
 *  v3.1  新增
 *  开发者可以调用此接口来设置app的版本号，该版本号由开发者获取后传入该函数。
 *  可以不设置，那么系统将会读取@CFBundleVersion
 *  原因：Xcode4有两个版本号，一个是Version,另一个是Build,对应于Info.plist的字段名分别为
 *  CFBundleShortVersionString,CFBundleVersion。为了兼容Xcode3的工程，默认取的是Build号，
 *  如果需要取Xcode4的Version，可以使用下面的方法。
 *  使用方法：
 *   NSString *version = [[[NSBundle mainBundle] infoDictionary] objectForKey:@"CFBundleShortVersionString"];
 *   statTracker.shortAppVersion  = version;
 */
@property (nonatomic,retain) NSString* shortAppVersion;

/**
 *  v3.2  新增
 *  开发这可以调用此接口来打印SDK中的日志，用于调试
 */
@property (nonatomic) BOOL enableDebugOn;


/**
 *  v3.22  新增
 *  让开发者来填写adid，让统计更加精确
 */
@property (nonatomic, retain) NSString* adid; //added by 2014-01-12


@end
