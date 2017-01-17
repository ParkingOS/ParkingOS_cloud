//
//  TMyMonthItem.m
//  TingCheBao_user
//
//  Created by apple on 14-9-13.
//  Copyright (c) 2014年 zhenLaiDian. All rights reserved.
//

#import "TMyMonthItem.h"

@implementation TMyMonthItem

+(TMyMonthItem*) getItemFromeDictionary:(NSDictionary*)dic {
    TMyMonthItem* item = [[TMyMonthItem alloc] init];
    item.parkname = [dic objectForKey:@"name"];
    item.price = [dic objectForKey:@"price"];
    item.limitdate = [dic objectForKey:@"limitdate"];
    item.name = [dic objectForKey:@"parkname"];
    item.limittime = [dic objectForKey:@"limittime"];
    item.limitday = [dic objectForKey:@"limitday"];
    return item;
}

@end
