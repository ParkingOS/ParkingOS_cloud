//
//  TCarNumberItem.m
//  
//
//  Created by apple on 15/7/13.
//
//

#import "TCarNumberItem.h"

@implementation TCarNumberItem

+ (TCarNumberItem*)getItemFromeDictionary:(NSDictionary*)dic {
    TCarNumberItem* item = [[TCarNumberItem alloc] init];
    item.car_number = GS(dic, @"car_number");
    item.is_auth = GS(dic, @"is_auth");
    return item;
}
@end
