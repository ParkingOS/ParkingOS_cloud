//
//  TPriceItem.m
//  TingCheBao_user
//
//  Created by apple on 14-9-11.
//  Copyright (c) 2014年 zhenLaiDian. All rights reserved.
//

#import "TPriceItem.h"
#import "TAPIUtility.h"

@implementation TPriceItem

+(TPriceItem*)getItemFromDictionary:(NSDictionary*)dic {
    if (!dic) {
        return nil;
    }
    TPriceItem* item = [[TPriceItem alloc] init];
    if ([dic count] == 1) {
        item.price = @"0.00";
        item.unit = @"0";
        item.b_time = @"7";
        item.e_time = @"21";
        item.first_times = @"60";
        item.fprice = @"0.00";
        item.fpay_type = @"0";
        item.free_time = @"1";
        item.nprice = @"0.00";
        item.nunit = @"0";
        item.nfirst_times = @"60";
        item.nfprice = @"0.00";
        item.nfpay_type = @"0";
        item.nfree_time = @"1";
        item.isnight = @"0";
    } else {
        item.price = [TAPIUtility getValidString:[dic objectForKey:@"price"]];
        item.unit = [TAPIUtility getValidString:[dic objectForKey:@"unit"]];
        item.b_time = [[dic objectForKey:@"b_time"] length] < 2 ? [@"0" stringByAppendingString:[dic objectForKey:@"b_time"]] : [dic objectForKey:@"b_time"];
        
        item.e_time = [[dic objectForKey:@"e_time"] length] < 2 ? [@"0" stringByAppendingString:[dic objectForKey:@"e_time"]] : [dic objectForKey:@"e_time"];
        item.first_times = [dic objectForKey:@"first_times"];
        item.fprice = [dic objectForKey:@"fprice"];
        item.fpay_type = [dic objectForKey:@"fpay_type"];
        item.free_time = [dic objectForKey:@"free_time"];
        
        item.nprice = [[dic objectForKey:@"nprice"] isEqualToString:@"-1"] ? @"0.00" : [dic objectForKey:@"nprice"];
        item.nunit = [[dic objectForKey:@"nunit"] isEqualToString:@"-1"] ? @"0" : [dic objectForKey:@"nunit"];
        item.nfirst_times = [dic objectForKey:@"nfirst_times"] == nil ? @"0" : [dic objectForKey:@"nfirst_times"];
        item.nfprice = [dic objectForKey:@"nfprice"] == nil ? @"0.00" : [dic objectForKey:@"nfprice"];
        item.nfpay_type = [dic objectForKey:@"nfpay_type"] == nil ? @"0" : [dic objectForKey:@"nfpay_type"];
        item.nfree_time = [dic objectForKey:@"nfree_time"] == nil ? @"0" : [dic objectForKey:@"nfree_time"];
        
        item.isnight = [dic objectForKey:@"isnight"];
    }
    return item;
}
@end
