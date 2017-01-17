//
//  THistoryOrderItem.m
//  TingCheBao_user
//
//  Created by apple on 14/10/19.
//  Copyright (c) 2014年 zhenLaiDian. All rights reserved.
//

#import "THistoryOrderItem.h"

@implementation THistoryOrderItem

+ (THistoryOrderItem*)getItemFromDictionary:(NSDictionary*)dic {
    THistoryOrderItem* item = [[THistoryOrderItem alloc] init];
    item.total = [dic objectForKey:@"total"];
    item.parkname = [dic objectForKey:@"parkname"];
    item.orderid = [dic objectForKey:@"orderid"];
    item.date = [dic objectForKey:@"date"];
    
    return item;
}
@end
