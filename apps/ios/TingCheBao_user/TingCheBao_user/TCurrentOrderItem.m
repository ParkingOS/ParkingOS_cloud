//
//  TCurrentOrderItem.m
//  TingCheBao_user
//
//  Created by apple on 14/10/18.
//  Copyright (c) 2014年 zhenLaiDian. All rights reserved.
//

#import "TCurrentOrderItem.h"
#import "TAPIUtility.h"

@implementation TCurrentOrderItem

+ (TCurrentOrderItem*)getItemFromDictionary:(NSDictionary*)dic {
    TCurrentOrderItem* item = [[TCurrentOrderItem alloc] init];
    item.total = [dic objectForKey:@"total"];
    item.parkname = [dic objectForKey:@"parkname"];
    item.address = [dic objectForKey:@"address"];
    item.etime = [dic objectForKey:@"etime"];
    item.state = [dic objectForKey:@"state"];
    item.btime = [dic objectForKey:@"btime"];
    item.parkid = [dic objectForKey:@"parkid"];
    item.orderid = [dic objectForKey:@"orderid"];
    if ([[dic objectForKey:@"payee"] count] > 0) {
        item.collectorId = [[dic objectForKey:@"payee"] objectForKey:@"id"];
        item.collectorName = [[dic objectForKey:@"payee"] objectForKey:@"name"];
        item.collectorMobile = [[dic objectForKey:@"payee"] objectForKey:@"mobile"];
    }
    item.bonusid = [TAPIUtility getValidString:[dic objectForKey:@"bonusid"]];
    item.reward = [dic objectForKey:@"reward"];
    item.comment = [dic objectForKey:@"comment"];
    item.ctype = [dic objectForKey:@"ctype"];
    return item;
}

@end
