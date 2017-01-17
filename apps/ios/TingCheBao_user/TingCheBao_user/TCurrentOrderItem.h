//
//  TCurrentOrderItem.h
//  TingCheBao_user
//
//  Created by apple on 14/10/18.
//  Copyright (c) 2014年 zhenLaiDian. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface TCurrentOrderItem : NSObject

@property(nonatomic, retain) NSString* total;//车费总额
@property(nonatomic, retain) NSString* parkname;//车场名字
@property(nonatomic, retain) NSString* address;//车场地址
@property(nonatomic, retain) NSString* etime;//结束时间，（就是现在的时间）
@property(nonatomic, retain) NSString* state;//状态 －1 支付失败 0 未结算 1 已结算  2 已支付
@property(nonatomic, retain) NSString* btime;//开始记费时间
@property(nonatomic, retain) NSString* parkid;//车场id
@property(nonatomic, retain) NSString* orderid;//订单id
@property(nonatomic, retain) NSString* collectorId;//收费员id
@property(nonatomic, retain) NSString* collectorName;//收费员名字
@property(nonatomic, retain) NSString* collectorMobile;//收费员手机号

@property(nonatomic, retain) NSString* bonusid;//1有红包 0没有红包
@property(nonatomic, retain) NSString* comment;//1评价过，0未评价
@property(nonatomic, retain) NSString* reward;//1打赏过，0未打赏
@property(nonatomic, retain) NSString* ctype;//如果是4 直接付费 


+ (TCurrentOrderItem*)getItemFromDictionary:(NSDictionary*)dic;

//{"total":"6.0","parkname":"测试中关村停车场3","address":"上地3","etime":"1413614840","state":"0","btime":"1413609167","parkid":"842","orderid":"161442"}

@end
