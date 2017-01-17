//
//  TTicketItem.m
//  TingCheBao_user
//
//  Created by apple on 14/11/3.
//  Copyright (c) 2014年 zhenLaiDian. All rights reserved.
//

#import "TTicketItem.h"

@implementation TTicketItem

+ (TTicketItem*)getItemFromDic:(NSDictionary*)dic {
    TTicketItem* item = [[TTicketItem alloc] init];
    item.ticketId = [dic objectForKey:@"id"];
    item.beginday = [dic objectForKey:@"beginday"];
    item.limitday = [dic objectForKey:@"limitday"];
    item.state = [dic objectForKey:@"state"];
    item.money = [dic objectForKey:@"money"];
    item.exp = [dic objectForKey:@"exp"];
    item.cname = [dic objectForKey:@"cname"];
    item.utime = [dic objectForKey:@"utime"];
    item.umoney = [dic objectForKey:@"umoney"];
    item.type   = [dic objectForKey:@"type"];
    item.desc   = [dic objectForKey:@"desc"];
    item.isbuy   = [dic objectForKey:@"isbuy"];
    return item;
}

+ (NSArray*)orderByLimitday:(NSArray*) items{
    for (TTicketItem* item in items) {
        NSLog(@"%@--%@", item.money, [TTicketItem stringWithTime2:item.utime]);
    }
    NSArray* newItems = [items sortedArrayUsingComparator:^NSComparisonResult(TTicketItem* obj1, TTicketItem* obj2) {
        if (![obj1.utime isEqualToString:@"0"] && ![obj2.utime isEqualToString:@"0"]) {
            
            //两张都使用过
            if ([obj1.utime integerValue] > [obj2.utime integerValue]) {
                return (NSComparisonResult)NSOrderedAscending;
            }
            if ([obj1.utime integerValue] < [obj2.utime integerValue]) {
                return (NSComparisonResult)NSOrderedDescending;
            }
        } else if (![obj1.utime isEqualToString:@"0"] && [obj2.utime isEqualToString:@"0"]) {
            return (NSComparisonResult)NSOrderedAscending;
        } else if (![obj2.utime isEqualToString:@"0"] && [obj1.utime isEqualToString:@"0"]) {
            return (NSComparisonResult)NSOrderedDescending;
        }
        return (NSComparisonResult)NSOrderedSame;
        
    }];
    for (TTicketItem* item in newItems) {
        NSLog(@"==%@--%@", item.money, [TTicketItem stringWithTime2:item.utime]);
    }
    return newItems;
}

+ (NSString*)stringWithTime2:(NSString*)time {
    NSDate* date = [NSDate dateWithTimeIntervalSince1970:[time integerValue]];
    NSDateFormatter* formatter = [[NSDateFormatter alloc] init];
    [formatter setDateFormat:@"yyyy-MM-dd HH:mm"];
    return [formatter stringFromDate:date];
}
@end
