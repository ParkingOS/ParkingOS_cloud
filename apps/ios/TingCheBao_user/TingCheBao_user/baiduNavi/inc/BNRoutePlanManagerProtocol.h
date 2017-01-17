//
//  BNRoutePlanManagerProtocol.h
//  baiduNaviSDK
//
//  Created by Baidu on 11/10/13.
//  Copyright (c) 2013 baidu. All rights reserved.
//

#ifndef baiduNaviSDK_BNRoutePlanManagerProtocol_h
#define baiduNaviSDK_BNRoutePlanManagerProtocol_h

#import "BNRoutePlanModel.h"

@protocol BNNaviRoutePlanDelegate;

/**
 *  路径规划协议
 */

@protocol BNRoutePlanManagerProtocol

@required

/**
 *  发起算路
 *
 *  @param eMode     算路方式，定义见BNRoutePlanMode
 *  @param naviNodes 算路节点数组，起点、途经点、终点按顺序排列，节点信息为BNRoutePlanNode结构
 *  @param naviTime  发起算路时间，用于优化算路结果,可以为nil
 *  @param delegate  算路委托，用于回调
 *  @param userInfo  用户需要传入的参数
 */
-(void)startNaviRoutePlan:(BNRoutePlanMode)eMode
                naviNodes:(NSArray*)naviNodes
                     time:(BNaviCalcRouteTime*)naviTime
                 delegete:(id<BNNaviRoutePlanDelegate>)delegate
                 userInfo:(NSDictionary*)userInfo;


/**
 *  获得当前节点总数
 *
 *  @return 当前节点总数
 */
-(NSInteger)getCurNodeCount;


/**
 *  获得第index个节点
 *
 *  @param index 节点序号
 *
 *  @return 第index个节点
 */
-(BNRoutePlanNode*)getNaviNodeAtIndex:(NSInteger)index;


/**
 *  设置算路节点
 *
 *  @param naviNodes 算路节点
 */
-(void)setNaviNodes:(NSArray*)naviNodes;


/**
 *  获取当前的路线规划方式
 *
 *  @return 当前的路线规划方式
 */
-(int)getCurRoutePlanMode;

@end

/**
 *  算路回调
 */
@protocol BNNaviRoutePlanDelegate <NSObject>

@optional

/**
 *  算路成功回调
 *
 *  @param userInfo 发起算路时用户传入的参数
 */
- (void)routePlanDidFinished:(NSDictionary*)userInfo;


/**
 *  算路失败回调
 *
 *  @param error    错误对象，可从error.code查看原因
 *  @param userInfo 发起算路时用户传入的参数
 */
- (void)routePlanDidFailedWithError:(NSError *)error andUserInfo:(NSDictionary*)userInfo;


/**
 *  算路取消
 *
 *  @param userInfo 发起算路时用户传入的参数
 */
-(void)routePlanDidUserCanceled:(NSDictionary*)userInfo;


@end

#endif
