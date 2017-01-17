//
//  TAccountItem.m
//  TingCheBao_user
//
//  Created by apple on 14-9-12.
//  Copyright (c) 2014年 zhenLaiDian. All rights reserved.
//

#import "TAccountItem.h"

@implementation TAccountItem

+ (TAccountItem*)getItemFromeDictionary:(NSDictionary*)dic {
    TAccountItem* item = [[TAccountItem alloc] init];
    item.balance = [dic objectForKey:@"balance"];
    item.carNumber = [dic objectForKey:@"carNumber"];
    item.mobile = [dic objectForKey:@"mobile"];
    item.limit = [TAPIUtility clearDoubleZero:[[dic objectForKey:@"limit"] doubleValue] fractionCount:2];//清除末尾的0
    item.limit_balan = [TAPIUtility clearDoubleZero:[[dic objectForKey:@"limit_balan"] doubleValue] fractionCount:2];//清除末尾的0
    item.limit_warn = [TAPIUtility clearDoubleZero:[[dic objectForKey:@"limit_warn"] doubleValue] fractionCount:2];
    item.state = [dic objectForKey:@"state"];
    return item;
}

@end
