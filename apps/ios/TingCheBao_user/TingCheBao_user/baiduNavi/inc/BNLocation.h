//
//  BNLocation.h
//  baiduNaviSDK
//
//  Created by Baidu on 14/12/29.
//  Copyright (c) 2014年 baidu. All rights reserved.
//

#import <CoreLocation/CLLocation.h>

/**
 *  位置对象，使用外部GPS功能所需要传入的gps对象
 */
@interface BNLocation : NSObject

/**
 *  wgs84ll格式的经纬度，也即iOS设备所获取到的经纬度
 */
@property (assign, nonatomic) CLLocationCoordinate2D coordinate;

/**
 *  海拔，单位为米
 */
@property (assign, nonatomic) CLLocationDistance altitude;

/**
 *  水平精度，单位为米
 */
@property (assign, nonatomic) CLLocationAccuracy horizontalAccuracy;

/**
 *  垂直精度，单位为米
 */
@property (assign, nonatomic) CLLocationAccuracy verticalAccuracy;

/**
 *  方向角度，单位为度，范围位0.0-359.9，0表示正北
 */
@property (assign, nonatomic) CLLocationDirection course;

/**
 *  速度，单位为米/秒
 */
@property (assign, nonatomic) CLLocationSpeed speed;

@end
