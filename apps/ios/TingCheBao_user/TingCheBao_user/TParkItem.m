//
//  TParkItem.m
//  TingCheBao_user
//
//  Created by apple on 14-8-21.
//  Copyright (c) 2014年 zhenLaiDian. All rights reserved.
//

#import "TParkItem.h"
#import "TAPIUtility.h"

@implementation TParkItem

//- (id)initWithCoder:(NSCoder *)aDecoder {
//    if (self = [super init]) {
//        _parkId = [aDecoder decodeObjectForKey:@"parkId"];
//        _company_name = [aDecoder decodeObjectForKey:@"company_name"];
//        _type = [aDecoder decodeObjectForKey:@"type"];
//        _total = [aDecoder decodeObjectForKey:@"total"];
//        _state = [aDecoder decodeObjectForKey:@"state"];
//        _update_time = [aDecoder decodeObjectForKey:@"update_time"];
//        _longitude = [aDecoder decodeObjectForKey:@"longitude"];
//        _latitude = [aDecoder decodeObjectForKey:@"latitude"];
//        _freeNum = [TAPIUtility getValidString:[aDecoder decodeObjectForKey:@"freeNum"]];
//        _epay = [TAPIUtility getValidString:[aDecoder decodeObjectForKey:@"epay"]];
//        _monthlypay = [TAPIUtility getValidString:[aDecoder decodeObjectForKey:@"monthlypay"]];
//    }
//    return self;
//}
//
//- (void)encodeWithCoder:(NSCoder *)aCoder {
//    [aCoder encodeObject:_parkId forKey:@"parkId"];
//    [aCoder encodeObject:_company_name forKey:@"company_name"];
//    [aCoder encodeObject:_type forKey:@"type"];
//    [aCoder encodeObject:_total forKey:@"total"];
//    [aCoder encodeObject:_state forKey:@"state"];
//    [aCoder encodeObject:_update_time forKey:@"update_time"];
//    [aCoder encodeObject:_longitude forKey:@"longitude"];
//    [aCoder encodeObject:_latitude forKey:@"latitude"];
//    [aCoder encodeObject:_freeNum forKey:@"freeNum"];
//    [aCoder encodeObject:_epay forKey:@"epay"];
//    [aCoder encodeObject:_monthlypay forKey:@"monthlypay"];
//}

+ (id)getItemFromDictionary:(NSDictionary *)dic {
    
    TParkItem* item = [[TParkItem alloc] init];
    
    item.parkId = [TAPIUtility getValidString:[dic objectForKey:@"id"]];
    item.name = [TAPIUtility getValidString:[dic objectForKey:@"name"]];
    item.addr = [TAPIUtility getValidString:[dic objectForKey:@"addr"]];
    item.phone = [TAPIUtility getValidString:[dic objectForKey:@"phone"]];
    item.lng = [TAPIUtility getValidString:[dic objectForKey:@"lng"]];
    item.lat = [TAPIUtility getValidString:[dic objectForKey:@"lat"]];
    item.free = [TAPIUtility getValidString:[dic objectForKey:@"free"]];
    item.total = [TAPIUtility getValidString:[dic objectForKey:@"total"]];
    item.epay = [TAPIUtility getValidString:[dic objectForKey:@"epay"]];
    item.monthlypay = [TAPIUtility getValidString:[dic objectForKey:@"monthlypay"]];
    item.price = [TAPIUtility getValidString:[dic objectForKey:@"price"]];
    item.suggested = [TAPIUtility getValidString:[dic objectForKey:@"suggested"]];
    item.photo_url = [dic objectForKey:@"photo_url"];
    
    return item;
}
@end
