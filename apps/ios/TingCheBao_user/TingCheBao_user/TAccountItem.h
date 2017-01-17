//
//  TAccountItem.h
//  TingCheBao_user
//
//  Created by apple on 14-9-12.
//  Copyright (c) 2014年 zhenLaiDian. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface TAccountItem : NSObject

@property(nonatomic, retain) NSString* balance;
@property(nonatomic, retain) NSString* carNumber;
@property(nonatomic, retain) NSString* mobile;
@property(nonatomic, retain) NSString* limit;//信用总额度
@property(nonatomic, retain) NSString* limit_balan;//信用可用额度
@property(nonatomic, retain) NSString* state;//0未认证，1已认证 2认证中 -1审核不通过
@property(nonatomic, retain) NSString* limit_warn;//信用警告额度

+ (TAccountItem*)getItemFromeDictionary:(NSDictionary*)dic;

@end
