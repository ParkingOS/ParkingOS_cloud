//
//  WQKeychain.h
//  TingCheBao_user
//
//  Created by yangshaojin on 15-3-16.
//  Copyright (c) 2015年 zhenLaiDian. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface WQKeychain : NSObject

+ (NSMutableDictionary *)getKeychainQuery:(NSString *)service;

+ (void)save:(NSString *)service data:(id)data;

+ (id)load:(NSString *)service;

+ (void)delete:(NSString *)service;

@end
