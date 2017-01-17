//
//  TSettingItem.h
//  TingCheBao_user
//
//  Created by apple on 14-9-12.
//  Copyright (c) 2014年 zhenLaiDian. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface TSettingItem : NSObject

@property(nonatomic, retain) NSString* low_recharge;// 0:不提醒，其他：低于时提醒
@property(nonatomic, retain) NSString* auto_cash;// 0:不自动支付，-1总是自动支付,正整数：小于时自动支付

+ (TSettingItem*)getItemFromDictionary:(NSDictionary*)dic;

@end
