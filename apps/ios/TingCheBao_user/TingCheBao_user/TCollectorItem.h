//
//  TCollectorItem.h
//  TingCheBao_user
//
//  Created by apple on 14/12/12.
//  Copyright (c) 2014年 zhenLaiDian. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface TCollectorItem : NSObject

@property(nonatomic, retain) NSString* collectorId;
@property(nonatomic, retain) NSString* name;
@property(nonatomic, retain) NSString* online;
@property(nonatomic, retain) NSString* address;
@property(nonatomic, retain) NSString* paytime;

+ (TCollectorItem*)getItemFromDictionary:(NSDictionary*)dic;

@end
