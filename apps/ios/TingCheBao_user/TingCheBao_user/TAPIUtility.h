//
//  TAPIUtility.h
//  TingCheBao
//
//  Created by apple on 14-7-15.
//  Copyright (c) 2014年 zhenLaiDian. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "MyAnnotation.h"
#import "TParkItem.h"

/********User Default ****/
//不清空
#define firstInstall    @"firstInstall"
#define splanshFlag    @"spanshFlag"
#define save_open_notification @"open_notification"//只弹一次
#define save_device_token   @"deviceToken"//每个人都是一样的

#define save_location_level  @"location_level"//停车位置的层数
#define save_location_time   @"location_time"//停车时时间
#define save_location_note   @"location_note"//停车时备注
#define save_location_lat    @"location_lat" //停车经纬度
#define save_location_log    @"location_log" //停车经纬度
#define save_location_right  @"location_right" //停车位置是否准确
#define save_server_url      @"server_url"   //服务器地址
#define save_test_phones     @"test_phones"  //测试手机号
//清空

//---------------------writeWithYang-------------------
#define save_Coor     @"Coordinate" //当前坐标
#define save_disatuce @"disatuce" //获得距离
#define save_first_comeIn @"Income" //第一次进入
//---------------------writeWithYang-------------------
#define save_phone    @"phone"
#define save_msg_id   @"msgID"
#define save_low_recharge @"low_recharge"//需要初始化
#define save_account_money @"account_money"//需要初始化
#define save_holiday_see_time @"holiday_see_time"//见到holiday活动
/*********User Default*****/

static NSString* const warnTimeInterval = @"warnTimeInterval";

@interface TAPIUtility : NSObject

//生成网址
+ (NSString*)getNetworkWithMserver:(BOOL)mserver downLoad:(BOOL)downLoad;
+ (NSString*)getNetworkWithUrl:(NSString*)url;
//检查是否超时
+ (BOOL)isRequestTimeOut:(NSError*)error;
//根据车场空闲车位数决定 颜色
+ (NSString*)colorWithFree:(double)free total:(double)total;
//网络返回的数据要进行纠正兼容
+ (NSString*) getValidString:(NSString*)data;
+ (NSNumber*) getValidNumber:(NSObject*)data;
+ (NSString*) getValidNumberString:(NSObject *)data;
+ (double) getValidTimestamp:(NSObject*)timestamp;
+ (NSArray *)getValidArray:(NSArray *)data;

//---------------------writeWithYang-------------------

+ (void)saveFirstInfo:(NSString *)info;
+ (BOOL)getFirstInfo;

+ (void)saveCoordinate:(CLLocationCoordinate2D)coordinate;
+ (CLLocationCoordinate2D)getCoordinate;

+ (void)saveDistance:(NSString *)dis;
+ (NSString *)getDistance;

//---------------------writeWithYang-------------------

//这三个方法现在没用了
+ (void)saveParks:(NSArray*)items;
+ (NSArray*)getParks;
+ (NSString*)getParksPath;

//提示信息,共四种写法，根据情况调用
+ (void)alertMessage:(NSString *)message;
+ (void)alertMessage:(NSString *)message afterDelay:(NSTimeInterval)delay;
+ (void)alertMessage:(NSString *)message toViewController:(UIViewController*)viewcontroller;
+ (void)alertMessage:(NSString *)message success:(BOOL)success toViewController:(UIViewController*)viewcontroller;
+ (MBProgressHUD*)loaddingView:(UIView*)view;

//车牌格式正则
+ (BOOL)isValidOfCarNumber:(NSString*)number;
//金额格式正则
+ (BOOL)isValidOfMoneyNumber:(NSString*)number;
//手机号格式正则
+ (BOOL)isValidOfPhone:(NSString*)number;

//生成指定颜色1像素的图片
+ (UIImage *)imageWithColor:(UIColor *)color;

//搜索历史记录
+ (NSString*)getHistoryPath;
//调整 image 的大小
+ (UIImage*)ajustImage:(UIImage*)image size:(CGSize)size;
+ (UIImage *) imageCompressForSize:(UIImage *)sourceImage targetSize:(CGSize)size;
+ (UIImage *) imageCompressForWidth:(UIImage *)sourceImage targetWidth:(CGFloat)defineWidth;

//记录单行或多行
+ (CGSize) sizeWithFont:(UIFont*) font size:(CGSize)size text:(NSString*)text;

//把秒转化成时分秒
+ (NSString*)getDuration:(int) durationSeconds;

//清除用户数据
+ (void)clearUserInfo;

//判断是否是企业版
+ (BOOL)isEnterpriseVersion;

//发送token给服务器

+ (void)sendDeviceToken;

//检查是否打开通知
+ (void)checkNotificationSetting;
//检查版本
+ (BOOL)isUpdateVersionWithOld:(NSString*)oldVersion new:(NSString*)newVersion;
//把所有目录添加不上传到icloud标志，否则appstore审核不过
+ (void)modifyBaiduDictory;

//生成日期:今天，昨天 或者 其它日期
+ (NSString *)getCommentTime:(NSDate *)date;

//停车位置拍照
+ (UIImage*)getLoactionImage;
+ (void)saveLocationImage:(UIImage*)image;

//旋转图片
+ (UIImage*)rotateImage:(UIImage*)image degree:(CGFloat)degree;

//检查拍照权限是否打开
+ (BOOL)checkPhotoAuthorization;

//double数字末尾清掉没必要的0
+ (NSString *)clearDoubleZero:(double)value fractionCount:(int)fractionCount;

@end
