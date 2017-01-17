//
//  TDetailParkItem.m
//  TingCheBao_user
//
//  Created by apple on 14-9-3.
//  Copyright (c) 2014年 zhenLaiDian. All rights reserved.
//

#import "TDetailParkItem.h"
#import "TAPIUtility.h"

@implementation TDetailParkItem

+ (TDetailParkItem*)getItemFromDictionary:(NSDictionary*)dictionary {
    if (!dictionary) 
        return nil;
    TDetailParkItem* item = [[TDetailParkItem alloc] init];
    item.total = [dictionary objectForKey:@"total"];
    item.updatetime = [dictionary objectForKey:@"updatetime"];
    item.parking_type = [dictionary objectForKey:@"parking_type"];
    item.etc = [dictionary objectForKey:@"etc"];
    item.hasPraise = [dictionary objectForKey:@"hasPraise"];
    item.currentPrice = [dictionary objectForKey:@"currentPrice"];
    item.navi = [dictionary objectForKey:@"navi"];
    item.stop_type = [dictionary objectForKey:@"stop_type"];
    item.disparageNum = [dictionary objectForKey:@"disparageNum"];
    item.freeSpace = [dictionary objectForKey:@"freeSpace"];
    item.praiseNum = [dictionary objectForKey:@"praiseNum"];
    item.monthlyPay = [dictionary objectForKey:@"monthlyPay"];
    item.address = [dictionary objectForKey:@"address"];
    item.descri = [dictionary objectForKey:@"description"];
    if ([item.descri isEqualToString:@""]) {
        item.descri = @"本车场环境优雅，车位多，价格优惠！欢迎光临!";
    }
    item.book = [dictionary objectForKey:@"book"];
    item.nfc = [dictionary objectForKey:@"nfc"];
    NSArray* photos = [dictionary objectForKey:@"photoUrl"];
    item.photoUrl = [photos count] ? [NSString stringWithFormat:@"%@%@", [TAPIUtility getNetworkWithMserver:NO downLoad:NO], [photos objectAtIndex:0]]: @"";
    item.mobile = [dictionary objectForKey:@"mobile"];
    
    return item;
}
@end
