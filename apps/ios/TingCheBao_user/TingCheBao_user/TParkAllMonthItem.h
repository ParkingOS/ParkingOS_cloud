//
//  TParkAllMonthItem.h
//  TingCheBao_user
//
//  Created by apple on 14-9-22.
//  Copyright (c) 2014年 zhenLaiDian. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "TParkMonthItem.h"

@interface TParkAllMonthItem : NSObject

@property(nonatomic,retain) NSArray* monthProducts;
@property(nonatomic,retain) NSString* distance;
@property(nonatomic,retain) NSString* company_name;

+ (TParkAllMonthItem*)getItmeFromDictionary:(NSDictionary*)dic;

@end
