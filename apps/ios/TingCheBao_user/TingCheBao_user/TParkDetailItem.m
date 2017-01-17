//
//  TParkDetailItem.m
//  TingCheBao_user
//
//  Created by apple on 15/4/16.
//  Copyright (c) 2015年 zhenLaiDian. All rights reserved.
//

#import "TParkDetailItem.h"
#import "TAPIUtility.h"

@implementation TParkDetailItem

+ (TParkDetailItem*)getItemFromDictionary:(NSDictionary*)dictionary {
    TParkDetailItem* item = [[TParkDetailItem alloc] init];
    item.parkId = [TAPIUtility getValidString:[dictionary objectForKey:@"id"]];
    item.name = [TAPIUtility getValidString:[dictionary objectForKey:@"name"]];
    item.lng = [TAPIUtility getValidString:[dictionary objectForKey:@"lng"]];
    item.lat = [TAPIUtility getValidString:[dictionary objectForKey:@"lat"]];
    item.free = [TAPIUtility getValidString:[dictionary objectForKey:@"free"]];
    item.price = [TAPIUtility getValidString:[dictionary objectForKey:@"price"]];
    item.total = [TAPIUtility getValidString:[dictionary objectForKey:@"total"]];
    item.addr = [TAPIUtility getValidString:[dictionary objectForKey:@"addr"]];
    item.phone = [TAPIUtility getValidString:[dictionary objectForKey:@"phone"]];
    item.epay = [TAPIUtility getValidString:[dictionary objectForKey:@"epay"]];
    item.desc = [TAPIUtility getValidString:[dictionary objectForKey:@"desc"]];
    if ([item.desc isEqualToString:@""])
        item.desc = @"本车场环境优雅，车位多，价格优惠！欢迎光临!";
    
    NSArray* photos = [dictionary objectForKey:@"photo_url"];
    item.photo_url = [photos count] ? [TAPIUtility getNetworkWithUrl:[photos objectAtIndex:0]]: @"";
    
    return item;
}

@end
