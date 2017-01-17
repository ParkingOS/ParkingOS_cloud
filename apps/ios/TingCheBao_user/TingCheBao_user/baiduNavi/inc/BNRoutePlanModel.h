//
//  BNaviRoutePlanModel.h
//  OfflineNavi
//
//  Created by Baidu on 4/11/13.
//  Copyright (c) 2013 baidu. All rights reserved.
//
//  路线规划节点数据结构类


#import <Foundation/Foundation.h>

/**
 * 坐标系类型
 */
typedef enum
{
    BNCoordinate_OriginalGPS = 0,/**< 原始的经纬度坐标 */
    BNCoordinate_BaiduMapSDK = 1,/**< 从百度地图中获取的sdk */
}BNCoordinate_Type;

/**
 * 坐标对象
 */
@interface BNPosition : NSObject

/**
 * 经度
 */
@property(nonatomic,assign)double x;

/**
 *  纬度
 */
@property(nonatomic,assign)double y;

/**
 *  坐标系类型，默认是BNCoordinate_OriginalGPS
 */
@property(nonatomic,assign)BNCoordinate_Type eType;

@end

/**
 *  路径规划节点对象
 */
@interface BNRoutePlanNode : NSObject
/**
 *  节点位置（经纬度信息）
 */
@property(nonatomic,retain)BNPosition* pos;
/**
 *  节点描述信息
 */
@property(nonatomic,copy)NSString* title;
/**
 *  节点地址信息
 */
@property(nonatomic,copy)NSString* address;

@end

/**
 *  分时段规划，路线的时间对象
 */
@interface BNaviCalcRouteTime : NSObject
/**
 *  小时，24小时制，0-23
 */
@property(nonatomic,assign)int unHour;
/**
 *  分钟，0-59
 */
@property(nonatomic,assign)int unMin;

@end




