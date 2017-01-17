//
//  TRecommendHistoryItem.m
//  TingCheBao_user
//
//  Created by apple on 14/12/29.
//  Copyright (c) 2014年 zhenLaiDian. All rights reserved.
//

#import "TRecommendHistoryItem.h"

@implementation TRecommendHistoryItem

+ (TRecommendHistoryItem*)getItemFromDic:(NSDictionary*)dic {
    TRecommendHistoryItem* item = [TRecommendHistoryItem new];
    item.uin = [dic objectForKey:@"uin"];
    item.state = [dic objectForKey:@"state"];
    return item;
}

@end
