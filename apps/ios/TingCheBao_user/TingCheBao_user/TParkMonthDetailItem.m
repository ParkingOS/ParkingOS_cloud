//
//  TParkMonthDetailItem.m
//  TingCheBao_user
//
//  Created by apple on 14-10-9.
//  Copyright (c) 2014年 zhenLaiDian. All rights reserved.
//

#import "TParkMonthDetailItem.h"

@implementation TParkMonthDetailItem

+(TParkMonthDetailItem*)getItemFromDictionary:(NSDictionary*)dic {
    TParkMonthDetailItem* item = [[TParkMonthDetailItem alloc] init];
    item.resume = [dic objectForKey:@"resume"];
    item.limitday = [dic objectForKey:@"limitday"];
    item.isbuy = [dic objectForKey:@"isbuy"];
    
    NSDictionary* parkInfo = [dic objectForKey:@"parkinfo"];
    item.company_name = [parkInfo objectForKey:@"company_name"];
    item.address = [parkInfo objectForKey:@"address"];
    item.mobile = [parkInfo objectForKey:@"mobile"];
    item.parkId = [parkInfo objectForKey:@"id"];
    item.praiseNum = [parkInfo objectForKey:@"praiseNum"];
    item.disparageNum = [parkInfo objectForKey:@"disparageNum"];
    item.longitude = [parkInfo objectForKey:@"longitude"];
    item.latitude = [parkInfo objectForKey:@"latitude"];
    item.commentnum = [parkInfo objectForKey:@"commentnum"];
    
    return item;
}
@end
