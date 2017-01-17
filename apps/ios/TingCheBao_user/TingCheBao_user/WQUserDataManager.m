//
//  WQUserDataManager.m
//  TingCheBao_user
//
//  Created by yangshaojin on 15-3-16.
//  Copyright (c) 2015年 zhenLaiDian. All rights reserved.
//

#import "WQKeychain.h"
#import "WQUserDataManager.h"

@implementation WQUserDataManager

static NSString * const KEY_IN_KEYCHAIN = @"com.tingCheBao.enterprise.allinfo";
static NSString * const KEY_PASSWORD = @"com.wuqian.app.password";

+(void)savePassWord:(NSString *)password
{
    NSMutableDictionary *usernamepasswordKVPairs = [NSMutableDictionary dictionary];
    [usernamepasswordKVPairs setObject:password forKey:KEY_PASSWORD];
    [WQKeychain save:KEY_IN_KEYCHAIN data:usernamepasswordKVPairs];
}

+(id)readPassWord
{
    NSMutableDictionary *usernamepasswordKVPair = (NSMutableDictionary *)[WQKeychain load:KEY_IN_KEYCHAIN];
    return [usernamepasswordKVPair objectForKey:KEY_PASSWORD];
}

+(void)deletePassWord
{
    [WQKeychain delete:KEY_IN_KEYCHAIN];
}

@end
