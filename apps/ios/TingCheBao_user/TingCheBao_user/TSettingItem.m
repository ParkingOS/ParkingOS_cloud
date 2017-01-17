//
//  TSettingItem.m
//  TingCheBao_user
//
//  Created by apple on 14-9-12.
//  Copyright (c) 2014年 zhenLaiDian. All rights reserved.
//

#import "TSettingItem.h"
#import "TAPIUtility.h"

@implementation TSettingItem

+ (TSettingItem*)getItemFromDictionary:(NSDictionary*)dic {
    TSettingItem* item = [[TSettingItem alloc] init];
    
    item.low_recharge = [TAPIUtility getValidString:[dic objectForKey:@"low_recharge"]];
    NSArray* lowRechargeKeyOptions = @[@"10", @"25", @"50", @"100", @"0"];
    if(![lowRechargeKeyOptions containsObject: item.low_recharge])
        item.low_recharge = @"0";
    
    item.auto_cash = [TAPIUtility getValidString:[dic objectForKey:@"limit_money"]];
    return item;
}

@end
