//
//  TChooseTicketItem.m
//  TingCheBao_user
//
//  Created by apple on 15/4/27.
//  Copyright (c) 2015年 zhenLaiDian. All rights reserved.
//

#import "TChooseTicketItem.h"

@implementation TChooseTicketItem

+ (TChooseTicketItem*)getItemFromDic:(NSDictionary*)dic {
    TChooseTicketItem* item = [[TChooseTicketItem alloc] init];
    item.ticketId = [dic objectForKey:@"id"];
    item.limit_day = [dic objectForKey:@"limitday"];
    item.money = [dic objectForKey:@"money"];
    item.cname = [dic objectForKey:@"cname"];
    item.type = [dic objectForKey:@"type"];
    item.iscanuse = [dic objectForKey:@"iscanuse"];
    item.desc = [dic objectForKey:@"desc"];
    item.limit = [TAPIUtility getValidString:[dic objectForKey:@"limit"]];
    item.isbuy = [dic objectForKey:@"isbuy"];
    return item;
}

@end
