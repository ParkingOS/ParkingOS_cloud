//
//  TCollectorItem.m
//  TingCheBao_user
//
//  Created by apple on 14/12/12.
//  Copyright (c) 2014年 zhenLaiDian. All rights reserved.
//

#import "TCollectorItem.h"

@implementation TCollectorItem

+ (TCollectorItem*)getItemFromDictionary:(NSDictionary*)dic {
    if (dic == nil)
        return nil;
    TCollectorItem* item = [[TCollectorItem alloc] init];
    item.collectorId = [dic objectForKey:@"id"];
    item.name = [dic objectForKey:@"name"];
    item.online = [dic objectForKey:@"online"];
    item.address = GS(dic, @"parkname");
    item.paytime = GS(dic, @"paytime");
    return item;
}

@end
