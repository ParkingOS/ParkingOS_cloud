//
//  BNLocationManagerProtocol.h
//  baiduNaviSDK
//
//  Created by Baidu on 11/26/13.
//  Copyright (c) 2013 baidu. All rights reserved.
//

#ifndef BaiduNaviSDK_BNLocationManagerProtocol_h
#define BaiduNaviSDK_BNLocationManagerProtocol_h

#import <CoreLocation/CoreLocation.h>
#import "BNLocation.h"

/**
 *  位置管理器
 */
@protocol BNLocationManagerProtocol

@optional

/**
 *  gps点是否来自外部,默认为NO,位置信息从iOS设备的gps模块获取。设置为YES时，gps的信息从currentLocation中获取
 */
@property (nonatomic, assign) BOOL gpsFromExternal;

/**
 *  当前位置，当前仅当gpsFromExternal=YES有效。当外部设置需要自定义gps数据时，可以通过设置该属性。
 */
@property (nonatomic, strong) BNLocation* currentLocation;

@end

#endif
