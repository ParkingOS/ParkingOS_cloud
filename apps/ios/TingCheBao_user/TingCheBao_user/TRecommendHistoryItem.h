//
//  TRecommendHistoryItem.h
//  TingCheBao_user
//
//  Created by apple on 14/12/29.
//  Copyright (c) 2014年 zhenLaiDian. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface TRecommendHistoryItem : NSObject

@property(nonatomic, retain) NSString* uin;
@property(nonatomic, retain) NSString* state;

+ (TRecommendHistoryItem*)getItemFromDic:(NSDictionary*)dic;

@end
