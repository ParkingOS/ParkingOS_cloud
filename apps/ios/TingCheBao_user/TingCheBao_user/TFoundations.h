//
//  TFoundations.h
//  TingCheBao
//
//  Created by apple on 14-7-15.
//  Copyright (c) 2014年 zhenLaiDian. All rights reserved.
//

#import "UIView+CVUIViewAdditions.h"
#import "MBProgressHUD.h"
#import "TAPIUtility.h"


#define notification_msg_payResult  @"notification_msg_payResult"
#define notification_msg_prepare_payOrder  @"notification_msg_prepare_payOrder"

#define getOS [[UIDevice currentDevice].systemVersion floatValue]
#define isIphone6Plus [UIScreen mainScreen].bounds.size.height == 736
#define isIphone6 [UIScreen mainScreen].bounds.size.height == 667
#define isIphone5s [UIScreen mainScreen].bounds.size.height == 568
#define isIphone4s [UIScreen mainScreen].bounds.size.height == 480
#define isIphoneNormal [UIScreen mainScreen].bounds.size.width == 320

#define mainScreenSize [UIScreen mainScreen ].bounds.size

#define RGBCOLOR(r,g,b) [UIColor colorWithRed:(r)/255.0f green:(g)/255.0f blue:(b)/255.0f alpha:1]

#define gray_color RGBCOLOR(52,56,66)
#define bg_view_color RGBCOLOR(247, 247, 247)
#define bg_line_color RGBCOLOR(220, 220, 223)
#define green_color RGBCOLOR(52,157,92)
#define redpackage_color RGBCOLOR(244,144,29)
//#define blue_color RGBCOLOR(26,100,163)
#define light_blue_color RGBCOLOR(75,92,208)
#define blue_color RGBCOLOR(56, 70, 105)
#define light_white_color RGBCOLOR(230, 230, 230)
#define red_color  RGBCOLOR(207, 45, 57)
#define orange_color RGBCOLOR(231, 123, 59)
#define noData_alert_color RGBCOLOR(138, 138, 138)
#define tableView_color RGBCOLOR(239,239,244)

#define UMENG_APPKEY @"5469e905fd98c59b75000693"

//百度地图
#define BaiduMapID [[[[NSBundle mainBundle] infoDictionary] objectForKey:(NSString*)kCFBundleIdentifierKey] isEqualToString:@"com.tingCheBao.enterprise"] ? @"steszMV4V2LrEuj5eHqIlTOM" : @"ayrQxl83UFcfeWTVtXuunaKo"
//微信
#define WXAppI @"wx73454d7f61f862a5"
#define WXPartnerID @"1220886701"
//qq
#define QQID @"1102349481"
//个推
#define GeTuiAppId           @"eDaq9HAQg76LPqTpgf3JW4"
#define GeTuiAppKey          @"AIk7nyJx9j5jjqLpUZDxB"
#define GetuiAppSecret       @"kgi4uMj65R9xyO8PVGOCd5"

#if __IPHONE_OS_VERSION_MIN_REQUIRED >= 70000
#define T_TEXTSIZE(text, font) [text length] > 0 ? [text \
sizeWithAttributes:@{NSFontAttributeName:font}] : CGSizeZero;
#else
#define T_TEXTSIZE(text, font) [text length] > 0 ? [text sizeWithFont:font] : CGSizeZero;
#endif

#define GS(_dict, _key) [TAPIUtility getValidString:[_dict objectForKey:_key]]
#define GL(_key) [[NSUserDefaults standardUserDefaults] objectForKey:_key]
#define SL(_obj, _key)     [[NSUserDefaults standardUserDefaults] setObject:_obj forKey:_key]

#define DLog(fmt, ...) NSLog((@"%s [Line %d]:\n" fmt), __PRETTY_FUNCTION__, __LINE__, ##__VA_ARGS__);

