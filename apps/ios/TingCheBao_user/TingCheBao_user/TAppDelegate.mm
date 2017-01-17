//
//  TAppDelegate.m
//  TingCheBao_user
//
//  Created by apple on 14-8-19.
//  Copyright (c) 2014年 zhenLaiDian. All rights reserved.
//

#import "TAppDelegate.h"
#import "TViewController.h"
#import "THomeViewController.h"
#import "TAPIUtility.h"
#import "TParkItem.h"
#import "BNCoreServices.h"

#import <AlipaySDK/AlipaySDK.h>
#import "DataVerifier.h"

#import "ViewController.h"
#import "WXApi.h"
#import "WXApiObject.h"
#import "TRechargeWaysViewController.h"

#import "CVAPIRequestModel.h"

#import "THistoryOrderViewController.h"
#import "TNewGuideViewController.h"

#import "TPush.h"
#import "TWeixin.h"
#import "TAlipay.h"
#import "TRechargeWaysViewController.h"
#import "UMFeedback.h"
#import "MobClick.h"
#import "TAlarmNoteItem.h"
#import "TIbeacon.h"
#import <TencentOpenAPI/TencentOAuth.h>
#import <BaiduMapAPI/BMKMapView.h>
#import "TSession.h"
#import "TNavigationController.h"
#import "IQKeyboardManager.h"


#define splansh_new @"1"//每次递增1

@interface TAppDelegate()


@end

@implementation TAppDelegate

- (BOOL)application:(UIApplication *)application didFinishLaunchingWithOptions:(NSDictionary *)launchOptions
{
    // Override point for customization after application launch.
    [[IQKeyboardManager sharedManager] setEnable:NO];
    [[IQKeyboardManager sharedManager] setEnableAutoToolbar:NO];
    
    //初始化 百度地图
    [self initBaiduMap];
    
    //初始化 微信
    [WXApi registerApp:WXAppI];
    
    //初始化 友盟反馈
    [UMFeedback setAppkey:UMENG_APPKEY];
    
    //初始化 友盟统计
    [self umengSetting];
    
    //APNS
    if ([[[UIDevice currentDevice] systemVersion] floatValue] >= 8.0)
    {
        [[UIApplication sharedApplication] registerUserNotificationSettings:[UIUserNotificationSettings
                                                                             settingsForTypes:(UIUserNotificationTypeSound | UIUserNotificationTypeAlert | UIUserNotificationTypeBadge)
                                                                             categories:nil]];
        [[UIApplication sharedApplication] registerForRemoteNotifications];
    }
    else
    {
        //这里还是原来的代码
        [[UIApplication sharedApplication] registerForRemoteNotificationTypes:
         (UIUserNotificationTypeBadge | UIUserNotificationTypeSound | UIUserNotificationTypeAlert)];
    }
    
    // 获取启动时收到的APNS
    NSDictionary* message = [launchOptions objectForKey:UIApplicationLaunchOptionsRemoteNotificationKey];
    if (message) {
        [[TPush getInstance] handlePush:message];
        NSLog(@"lanuch-----%@", [message description]);
    }
    
    [UIApplication sharedApplication].applicationIconBadgeNumber = 0;
    
    //初始化服务器地址
    if (!GL(save_server_url)) {
        SL(@"s.tingchebao.com", save_server_url);
    }
    
    //如果是第一次安装 需要引导界面
    UIViewController* rootVc = nil;
    if (![[NSUserDefaults standardUserDefaults] objectForKey:firstInstall] || ![[[NSUserDefaults standardUserDefaults] objectForKey:splanshFlag] isEqualToString:splansh_new]) {
        if (![[NSUserDefaults standardUserDefaults] objectForKey:firstInstall]) {
            //初始化用户信息
            [TAPIUtility clearUserInfo];
        }
        
        TNewGuideViewController* vc = [[TNewGuideViewController alloc] init];
        rootVc = vc;
        
        [[NSUserDefaults standardUserDefaults] setObject:@"1" forKey:firstInstall];
        [[NSUserDefaults standardUserDefaults] setObject:splansh_new forKey:splanshFlag];
    } else {
        THomeViewController* home = [THomeViewController share];
        TNavigationController* nv = [[TNavigationController alloc] initWithRootViewController:home];
//            THistoryOrderViewController* home = [[THistoryOrderViewController alloc] init];
//            UINavigationController* nv = [[UINavigationController alloc] initWithRootViewController:home];
        
        TViewController* vc = [[TViewController alloc] init];
        [TViewController setInstance:vc];
        vc.centerController = nv;
        
        [[UINavigationBar appearance] setBarTintColor:RGBCOLOR(254, 254, 254)];
        [UIApplication sharedApplication].statusBarStyle = UIStatusBarStyleDefault;
        [vc setNeedsStatusBarAppearanceUpdate];
        
        rootVc = vc;
    }
    
     [application setApplicationSupportsShakeToEdit:YES];
    
    _window = [[UIWindow alloc] initWithFrame:[UIScreen mainScreen].bounds];
    _window.rootViewController = rootVc;
    [_window makeKeyAndVisible];
    
    [TAPIUtility saveDistance:nil];
    
    
    //测试,需要注释------------begin
    
    if (!GL(save_test_phones)) {
        NSArray* phones = @[@"15801270154",
                            @"15010613623",
                            @"13641309140",
                            @"13677226466",
                            @"15210810614",
                            @"15210932334",
                            ];
        SL(phones, save_test_phones);
    }
    if (GL(save_phone)) {
        if (![GL(save_test_phones) containsObject:GL(save_phone)]) {
            NSMutableArray* phones = [NSMutableArray arrayWithArray:GL(save_test_phones)];
            [phones insertObject:GL(save_phone) atIndex:0];
            SL(phones, save_test_phones);
        }
    }
  
    //测试,需要注释------------end
    
    return YES;
}

- (void)initBaiduMap {
    //百度基础地图
    _mapManager = [[BMKMapManager alloc] init];
    BOOL ret = [_mapManager start:BaiduMapID generalDelegate:nil];
    if (!ret) {
        NSLog(@"manager start failed");
    }
//     初始化导航SDK引擎
    [BNCoreServices_Instance initServices:BaiduMapID];
    
//    开启引擎，传入默认的TTS类
    [BNCoreServices_Instance startServicesAsyn:nil fail:nil];
    [TAPIUtility modifyBaiduDictory];
}

- (void)umengSetting {
//    [MobClick setLogEnabled:YES];  // 打开友盟sdk调试，注意Release发布时需要注释掉此行,减少io消耗
    [MobClick setAppVersion:XcodeAppVersion]; //参数为NSString * 类型,自定义app版本信息，如果不设置，默认从CFBundleVersion里取
    
    NSString* channelId = [TAPIUtility isEnterpriseVersion] ? @"enterprise" : @"";
    [MobClick startWithAppkey:UMENG_APPKEY reportPolicy:(ReportPolicy) REALTIME channelId: channelId];
}

- (void)application:(UIApplication *)application didRegisterForRemoteNotificationsWithDeviceToken:(NSData*)deviceToken
{
    NSString *token = [[deviceToken description] stringByTrimmingCharactersInSet:[NSCharacterSet characterSetWithCharactersInString:@"<>"]];
    token = [token stringByReplacingOccurrencesOfString:@" " withString:@""];
//    NSLog(@"deviceToken:%@", token);
    [[NSUserDefaults standardUserDefaults] setObject:token forKey:save_device_token];
    [TAPIUtility sendDeviceToken];
}

- (void)application:(UIApplication *)application didFailToRegisterForRemoteNotificationsWithError:(NSError*)error
{
    NSLog(@"APNS Error");
}

- (void)application:(UIApplication *)application didReceiveRemoteNotification:(NSDictionary *)userinfo
{
    [UIApplication sharedApplication].applicationIconBadgeNumber = 0;
    
    TPush* push = [TPush getInstance];
    [push handlePush:userinfo];
    
    NSLog(@"apns--%@", [userinfo description]);
}

- (void)application:(UIApplication *)application didReceiveLocalNotification:(UILocalNotification *)notification {
    UIAlertView* alert = [[UIAlertView alloc] initWithTitle:@"闹钟" message:@"定时提醒：时间到了哦～" delegate:nil cancelButtonTitle:nil otherButtonTitles:@"OK", nil];
    [alert show];
}

- (void)applicationWillResignActive:(UIApplication *)application
{
    // Sent when the application is about to move from active to inactive state. This can occur for certain types of temporary interruptions (such as an incoming phone call or SMS message) or when the user quits the application and it begins the transition to the background state.
    // Use this method to pause ongoing tasks, disable timers, and throttle down OpenGL ES frame rates. Games should use this method to pause the game.
    [BMKMapView willBackGround];
}

- (void)applicationDidEnterBackground:(UIApplication *)application
{
    // Use this method to release shared resources, save user data, invalidate timers, and store enough application state information to restore your application to its current state in case it is terminated later. 
    // If your application supports background execution, this method is called instead of applicationWillTerminate: when the user quits.
//    [[TIbeacon getInstance] stopScan];
}

- (void)applicationWillEnterForeground:(UIApplication *)application
{
    // Called as part of the transition from the background to the inactive state; here you can undo many of the changes made on entering the background.
    TViewController* vc = [TViewController share];
    THomeViewController* home = [THomeViewController share];
    [home checkHoliday];//检查holiday
    home.checkCityName = YES;//每次检测一下所在的城市
    UINavigationController* center = vc.centerController;
    if ([center.topViewController isKindOfClass:[TRechargeWaysViewController class]]) {
        TPush* push = [TPush getInstance];
        [push requestMsg:nil];
    } else if ([center.topViewController isKindOfClass:[THomeViewController class]] && vc.leftView.alpha == 0) {
        [home checkCurrentOrder];
        [home requestAllParks];//更新添加或删的停车场，更新空闲车位数
    }
}


- (void)applicationDidBecomeActive:(UIApplication *)application
{
    // Restart any tasks that were paused (or not yet started) while the application was inactive. If the application was previously in the background, optionally refresh the user interface.
    [UIApplication sharedApplication].applicationIconBadgeNumber = 0;
    [BMKMapView didForeGround];
}

- (void)applicationWillTerminate:(UIApplication *)application
{
    // Called when the application is about to terminate. Save data if appropriate. See also applicationDidEnterBackground:.
    TIbeacon* ibeacon = [TIbeacon getInstance];
    [ibeacon stopScan];
}

//独立客户端回调函数
- (BOOL)application:(UIApplication *)application handleOpenURL:(NSURL *)url {
    
    if (url != nil && [[url host] compare:@"safepay"] == 0) {
        //支付宝
        
        [[AlipaySDK defaultService] processOrderWithPaymentResult:url standbyCallback:^(NSDictionary *resultDic) {
            NSLog(@"result = %@",resultDic);
        }];
    } else if ([url.scheme isEqualToString:@"wx73454d7f61f862a5"]){
//        if ([url.absoluteString rangeOfString:@"pay/"].length > 0) {
            //微信
            [WXApi handleOpenURL:url delegate:[TWeixin getInstance]];
//        } else {
//            NSLog(@"share");
//        }
    }
    return YES;
}

@end


