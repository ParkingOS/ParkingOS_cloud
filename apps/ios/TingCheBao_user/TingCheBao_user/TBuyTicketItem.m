//
//  TBuyTicketItem.m
//  
//
//  Created by apple on 15/8/26.
//
//

#import "TBuyTicketItem.h"

@implementation TBuyTicketItem

+ (TBuyTicketItem*)getItemFromDic:(NSDictionary*)dic {
    TBuyTicketItem* item = [[TBuyTicketItem alloc] init];
    item.isauth = GS(dic, @"isauth");
    item.auth = GS(dic, @"auth");
    item.notauth = GS(dic, @"notauth");
    return item;
}

@end
