//
//  BNStrategyManagerProtocol.h
//  baiduNaviSDK
//
//  Created by Baidu on 11/10/13.
//  Copyright (c) 2013 baidu. All rights reserved.
//

#ifndef baiduNaviSDK_BNStrategyManagerProtocol_h
#define baiduNaviSDK_BNStrategyManagerProtocol_h

#import "BNCommonDef.h"

/**
 *  策略管理器
 */
@protocol BNStrategyManagerProtocol


/**
 *  设置播报模式
 *
 *  @param speakMode 播报模式，默认为BN_Speak_Mode_High（新手模式）
 */
- (void)setSpeakMode:(BN_Speak_Mode_Enum)speakMode;


/**
 *  设置路况是否开启，路况开启需要联网，没有网络，开启路况会失败
 *
 *  @param showTraffic 是否显示路况，默认显示
 *  @param success     成功的回调
 *  @param fail        失败的回调
 */
- (void)trySetShowTraffic:(BOOL)showTraffic success:(void (^)(void))success  fail:(void (^)(void))fail;

/**
 *  设置白天黑夜模式
 *
 *  @param dayNightType 白天黑夜模式
 */
- (void)setDayNightType:(BNDayNight_CFG_Type)dayNightType;

@end

#endif
