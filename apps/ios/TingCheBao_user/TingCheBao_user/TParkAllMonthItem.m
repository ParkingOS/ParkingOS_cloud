//
//  TParkAllMonthItem.m
//  TingCheBao_user
//
//  Created by apple on 14-9-22.
//  Copyright (c) 2014年 zhenLaiDian. All rights reserved.
//

#import "TParkAllMonthItem.h"

@implementation TParkAllMonthItem

+ (TParkAllMonthItem*)getItmeFromDictionary:(NSDictionary*)dic {
    TParkAllMonthItem* item = [[TParkAllMonthItem alloc] init];
    item.distance = [dic objectForKey:@"distance"];
    item.company_name = [dic objectForKey:@"company_name"];
    
    NSMutableArray* monthProducts = [NSMutableArray array];
    NSArray* months = [dic objectForKey:@"monthProducts"];
    for (NSDictionary*dic in months) {
        TParkMonthItem* item = [TParkMonthItem getItemFromDictionary:dic];
        [monthProducts addObject:item];
    }
    item.monthProducts = monthProducts;
    return item;
}

@end
