//
//  TRecommendItem.m
//  TingCheBao_user
//
//  Created by apple on 14-8-26.
//  Copyright (c) 2014年 zhenLaiDian. All rights reserved.
//

#import "TRecommendItem.h"

@implementation TRecommendItem

+(TRecommendItem*)getItemFromDictionary:(NSDictionary*)dic {
    TRecommendItem* item = [[TRecommendItem alloc] init];
    item.dist = [dic objectForKey:@"dist"];
    item.freeinfo = [dic objectForKey:@"freeinfo"];
    if ([item.freeinfo isEqualToString:@"0"]) {
        item.freeinfo = @"紧张";
    } else if ([item.freeinfo isEqualToString:@"1"]) {
        item.freeinfo = @"较少";
    } else {
        item.freeinfo = @"充足";
    }
    item.name = [dic objectForKey:@"suggest"];
    item.monthids = [dic objectForKey:@"monthids"];
    item.bookids = [dic objectForKey:@"bookids"];
    item.lon = [dic objectForKey:@"lon"];
    item.lat = [dic objectForKey:@"lat"];
    item.parkId = [dic objectForKey:@"id"];
    item.price = [dic objectForKey:@"price"];
    item.freeNum = [dic objectForKey:@"snumber"];
    item.epay = [dic objectForKey:@"epay"];
    item.monthlypay = [dic objectForKey:@"monthlypay"];
    
    return item;
}
@end
