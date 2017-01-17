//
//  TParkMonthItem.m
//  TingCheBao_user
//
//  Created by apple on 14-9-13.
//  Copyright (c) 2014年 zhenLaiDian. All rights reserved.
//

#import "TParkMonthItem.h"

@implementation TParkMonthItem

+ (TParkMonthItem*)getItemFromDictionary:(NSDictionary*)dic {
    TParkMonthItem* item = [[TParkMonthItem alloc] init];
    item.productId = [dic objectForKey:@"id"];
    item.price0 = [dic objectForKey:@"price0"];
    item.isbuy = [dic objectForKey:@"isbuy"];
    item.reserved = [dic objectForKey:@"reserved"];
    item.price = [dic objectForKey:@"price"];
    item.name = [dic objectForKey:@"name"];
    item.number = [dic objectForKey:@"number"];
    item.limittime = [dic objectForKey:@"limittime"];
    item.limitday = [dic objectForKey:@"limitday"];
    item.photoUrl = [dic objectForKey:@"photoUrl"];
    item.type = [dic objectForKey:@"type"];
    return item;
}
@end
