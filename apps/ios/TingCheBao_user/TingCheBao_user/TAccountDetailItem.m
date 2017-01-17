//
//  TAccountDetailItem.m
//  TingCheBao_user
//
//  Created by apple on 14-9-12.
//  Copyright (c) 2014年 zhenLaiDian. All rights reserved.
//

#import "TAccountDetailItem.h"

@implementation TAccountDetailItem

+(TAccountDetailItem*)getItemFromDictionary:(NSDictionary*)dic {
    TAccountDetailItem* item = [[TAccountDetailItem alloc] init];
    
    NSDate* date = [NSDate dateWithTimeIntervalSince1970:[[dic objectForKey:@"create_time"] integerValue]];
    NSDateFormatter* formatter = [[NSDateFormatter alloc] init];
    [formatter setDateFormat:@"yyyy-MM-dd HH:mm:ss"];
    item.create_time = [formatter stringFromDate:date];
    
    item.amount = [dic objectForKey:@"amount"];
    item.type = [dic objectForKey:@"type"];
    item.remark = [dic objectForKey:@"remark"];
    item.pay_name = [dic objectForKey:@"pay_name"];
    return item;
}

@end
