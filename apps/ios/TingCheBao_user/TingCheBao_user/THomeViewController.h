//
//  THomeViewController.h
//  TingCheBao_user
//
//  Created by apple on 14-8-19.
//  Copyright (c) 2014年 zhenLaiDian. All rights reserved.
//
#import <CoreBluetooth/CoreBluetooth.h>
#import "TShakeView.h"
#import <AVFoundation/AVFoundation.h>
//---------------------writeWithYang-------------------

#import <UIKit/UIKit.h>
#import "NSString+CVURLEncoding.h"
#import "TShareItem.h"
#import "TBaseViewController.h"
#import "TCurrentOrderView.h"
#import <BaiduMapAPI/BMKLocationService.h>
#import "TCurrentOrderItem.h"
#import "TScanParkRedPackageItem.h"

@interface THomeViewController : TBaseViewController <CBCentralManagerDelegate,UIAlertViewDelegate,CLLocationManagerDelegate,AVAudioPlayerDelegate>

@property(nonatomic, retain) TCurrentOrderView* currentOrderView;
@property(nonatomic, retain) BMKLocationService* locationService;
@property(nonatomic, assign) BOOL checkCityName;

- (void)requestAllParks;
- (void)checkHoliday;
- (void)clickedQueryCurrOrderButton:(TCurrentOrderItem*)item;

+ (THomeViewController*)share;

- (void)shareRedPackageLeftButton;
- (void)shareRedPackageRightButton:(NSNumber *)succ;

- (void)shareRedPackageToWeixin:(TShareItem *)item;

//更新订单显示状态 绿黄红
- (void)updateOrderState:(int)state;

//查看当前是否有订单
- (void)checkCurrentOrder;

//显示领取收费员红包的界面

- (void)showCollectorRedPackageView:(TScanParkRedPackageItem*)item;

//检查信用额度是否不够
- (void)checkAccount:(void(^)(BOOL isNeedRecharge))handle;

@end
