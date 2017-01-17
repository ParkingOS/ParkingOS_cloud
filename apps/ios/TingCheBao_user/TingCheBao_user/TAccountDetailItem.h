//
//  TAccountDetailItem.h
//  TingCheBao_user
//
//  Created by apple on 14-9-12.
//  Copyright (c) 2014年 zhenLaiDian. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface TAccountDetailItem : NSObject

@property(nonatomic, retain) NSString* create_time;//时间
@property(nonatomic, retain) NSString* amount;//金额
@property(nonatomic, retain) NSString* type;//0：充值，1：消费 2：全部
@property(nonatomic, retain) NSString* remark;//产品名称
@property(nonatomic, retain) NSString* pay_name;//（从0开始） "余额", "支付宝", "微信", "网银","余额+支付宝", "余额+微信", "余额+网银", "停车宝充值"


+(TAccountDetailItem*)getItemFromDictionary:(NSDictionary*)dic;

//[{"create_time":"1409735061","amount":"0.01","type":"1","remark":"月卡包月测试演示停车场 购买-半月测试产品","pay_type":"0"},
@end
