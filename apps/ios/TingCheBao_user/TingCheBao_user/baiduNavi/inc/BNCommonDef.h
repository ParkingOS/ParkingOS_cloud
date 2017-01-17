//
//  BNCommonDef.h
//  baiduNaviSDK
//
//  Created by Baidu on 11/12/13.
//  Copyright (c) 2013 baidu. All rights reserved.
//

#ifndef baiduNaviSDK_BNCommonDef_h
#define baiduNaviSDK_BNCommonDef_h


/**
 *  路线规划结果枚举
 */
typedef enum
{
	BNRoutePlanError_LocationFailed           = 100, /**< 获取地理位置失败 */
	BNRoutePlanError_RoutePlanFailed          = 101, /**< 无法发起算路 */
    BNRoutePlanError_LocationServiceClosed    = 102, /**< 定位服务未开启 */
    BNRoutePlanError_NodesTooNear             = 103, /**< 节点之间距离太近 */
    BNRoutePlanError_NodesInputError          = 104, /**< 节点输入有误 */
    BNRoutePlanError_WaitAMoment              = 105, /**< 上次算路取消了，需要等一会 */
}BNRoutePlanError;


/**
 *  路线计算类型
 */
typedef enum
{
	BNRoutePlanMode_Invalid 			= 0X00000000 ,  /**<  无效值 */
	BNRoutePlanMode_Recommend			= 0X00000001 ,	/**<  推荐 */
    BNRoutePlanMode_Highway             = 0X00000002 ,	/**<  高速优先 */
    BNRoutePlanMode_NoHighway           = 0X00000004 ,	/**<  少走高速 */
}BNRoutePlanMode;

/**
 *  导航类型
 */
typedef enum
{
    BN_NaviTypeReal,      /**< 真实导航*/
    BN_NaviTypeSimulator  /**< 默认模拟导航*/
} BN_NaviType;

/**
 *  播报模式
 */
typedef enum {
    BN_Speak_Mode_High,                /**< 新手模式 */
    BN_Speak_Mode_Mid,                 /**< 专家模式 */
    BN_Speak_Mode_Low,                 /**< 静音模式 */
} BN_Speak_Mode_Enum;


/**
 *  白天，黑夜模式类型
 */
typedef enum
{
    BNDayNight_CFG_Type_Auto,   //自动
    BNDayNight_CFG_Type_Day,    //白天模式
    BNDayNight_CFG_Type_Night,  //黑夜模式
}BNDayNight_CFG_Type;

#endif
