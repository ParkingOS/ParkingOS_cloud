//
//  TIbeacon.m
//  TingCheBao_user
//
//  Created by apple on 14/12/29.
//  Copyright (c) 2014年 zhenLaiDian. All rights reserved.
//

#import "TIbeacon.h"
#import <CoreLocation/CoreLocation.h>
#import "CVAPIRequestModel.h"
#import <CoreBluetooth/CoreBluetooth.h>
#import "TAPIUtility.h"

@interface TIbeacon()<CLLocationManagerDelegate, CVAPIModelDelegate, CBPeripheralManagerDelegate>

@property(nonatomic, retain) CLLocationManager* locationManager;
@property(nonatomic, retain) CLBeaconRegion* region;
@property(nonatomic, retain) CVAPIRequestModel* model;
@property(nonatomic, retain) CLBeacon* beacon;
@property(nonatomic, retain) CBPeripheralManager* peripheralManager;

@end
@implementation TIbeacon


+ (id)getInstance {
    static dispatch_once_t once;
    static id instance;
    dispatch_once(&once, ^{
        instance = [self new];
    });
    return instance;
}

- (id)init {
    if (self = [super init]) {
        _LastTime = 0;
    }
    return self;
}

- (void)startScan {
    //temp注释
    return;
    _locationManager = [[CLLocationManager alloc] init];
    _locationManager.delegate = self;
    _locationManager.desiredAccuracy = kCLLocationAccuracyBest;
    _locationManager.distanceFilter = 1.0f;
    
    _peripheralManager = [[CBPeripheralManager alloc] initWithDelegate:self queue:nil];
    
    //ios8
    if ([_locationManager respondsToSelector:@selector(requestAlwaysAuthorization)]) {
        //必须是永远定位，否则后台时 无法检查ibeacon
        [_locationManager requestAlwaysAuthorization];
    }
    
    _region = [[CLBeaconRegion alloc] initWithProximityUUID:[[NSUUID alloc] initWithUUIDString:@"61051C01-5B41-4F54-A32D-0CE4E52076C1"] identifier:@"hello"];
    [_locationManager startRangingBeaconsInRegion:_region];
    [_locationManager startMonitoringForRegion:_region];
}

- (void)stopScan {
    //temp注释
    return;
    [_locationManager stopRangingBeaconsInRegion:_region];
    [_locationManager stopMonitoringForRegion:_region];
}

#pragma mark CLLocationManagerDelegate

- (void)peripheralManagerDidUpdateState:(CBPeripheralManager *)peripheral {
//    NSLog(@"---------%d", peripheral.state);
}

- (void)locationManager:(CLLocationManager *)manager didEnterRegion:(CLRegion *)region {
//    //    [self showAlertMessage:@"enter"];
//    NSLog(@"enter");
//    NSLog(@"identifier -%@", region.identifier);
//    [self postNotification:@"enter"];
//    return;
}

- (void)locationManager:(CLLocationManager *)manager didExitRegion:(CLRegion *)region {
    //    [self showAlertMessage:@"exit"];
//    [self postNotification:@"exit"];
//    NSLog(@"exit");
}

- (void)locationManager:(CLLocationManager *)manager didRangeBeacons:(NSArray *)beacons inRegion:(CLBeaconRegion *)region {
    if (![[NSUserDefaults standardUserDefaults] objectForKey:save_phone]) {
        return;
    }
    int proximity = CLProximityFar;
    BOOL flag = NO;
    for (CLBeacon* beacon in beacons) {
        if (beacon.proximity != CLProximityUnknown) {
            flag = YES;
            if (beacon.proximity <= proximity)
                _beacon = beacon;
        }
    }
    
    double nowTime = [[NSDate date] timeIntervalSince1970];
    //30秒内不会扫描
    if (flag) {
        NSString* p;
        switch (proximity) {
            case CLProximityFar:
                p = @"Far";
                break;
            case CLProximityImmediate:
                p = @"Immediate";
                break;
            case CLProximityNear:
                p = @"Near";
                break;
            default:
                p = @"Unknown";
                break;
        }
//        NSLog(@"-------");
        if (nowTime - _LastTime > 15) {
            _LastTime = nowTime;
            
            [self sendRequest];
        }
    }
}

- (void)sendRequest {
//    NSLog(@"send---------");
    static int i = 0;
    if (![[NSUserDefaults standardUserDefaults] objectForKey:save_phone]) {
        i = 0;
        return;
    }
    CVAPIRequest* request = [[CVAPIRequest alloc] initWithAPIPath:[NSString stringWithFormat:@"ibeaconhandle.do?major=%@&minor=%@&action=ibcincom&mobile=%@", _beacon.major, _beacon.minor, [[NSUserDefaults standardUserDefaults] objectForKey:save_phone]] timeout:6.00];
    _model = [[CVAPIRequestModel alloc] init];
    _model.delegate = self;
    _model.hideNetworkView = YES;
    [_model sendRequest:request completion:^(NSDictionary *result, NSError *error) {
//        {"result":"0","state":"0","info":"已存在未结算的订单，不能生成进场订单"}
        //0 失败 1成功 ， state 0 进场，state 出场, info是具体信息
        if (result) {
            BOOL succ = [[result objectForKey:@"result"] isEqualToString:@"1"] ? YES : NO;
            NSString* state = [result objectForKey:@"state"];
            NSString* info = [result objectForKey:@"info"];
            NSString* title = [state isEqualToString:@"0"] ? @"进场" : @"出场";
            title = [title stringByAppendingString:succ ? @"成功" : @"失败" ];
            //前台弹框 后台通知
            if ([UIApplication sharedApplication].applicationState == UIApplicationStateActive) {
                UIAlertView* alert = [[UIAlertView alloc] initWithTitle:title message:info delegate:nil cancelButtonTitle:@"确定" otherButtonTitles:nil];
                [alert show];
            } else {
                [self postNotification:info];
            }
        }
    }];
}

- (void)postNotification :(NSString*)message{
    UILocalNotification *notification = [[UILocalNotification alloc] init];
    notification.alertBody = message;
    notification.soundName = UILocalNotificationDefaultSoundName;
    [[UIApplication sharedApplication] presentLocalNotificationNow:notification];
}

#pragma mark CVAPIModelDelegate

- (void)modelDidFailWithError:(NSError *)error model:(CVAPIRequestModel *)model request:(CVAPIRequest *)request {
    //前台弹框 后台通知
    if ([UIApplication sharedApplication].applicationState == UIApplicationStateActive) {
//    [self sendRequest];
        UIAlertView* alert = [[UIAlertView alloc] initWithTitle:@"" message:@"生成或结算订单时网络超时" delegate:nil cancelButtonTitle:@"确定" otherButtonTitles:nil];
        [alert show];
    } else {
        [self postNotification:@"生成或结算订单时网络超时"];
    }
}

@end
