//
//  TCollectorDetailItem.m
//  
//
//  Created by apple on 15/6/18.
//
//

#import "TCollectorDetailItem.h"
#import "TAPIUtility.h"

@implementation TCollectorDetailItem

+ (TCollectorDetailItem*)getItemFromDic:(NSDictionary*)dic {
    TCollectorDetailItem* item = [[TCollectorDetailItem alloc] init];
    item.rcount = GS(dic, @"rcount");
    if ([item.rcount isEqualToString:@""])
        item.rcount = @"0";
    item.money = GS(dic, @"money");
    item.scount = GS(dic, @"scount");
    item.wcount = GS(dic, @"wcount");
    item.ccount = GS(dic, @"ccount");
    item.mobile = GS(dic, @"mobile");
    
    return item;
}

@end
