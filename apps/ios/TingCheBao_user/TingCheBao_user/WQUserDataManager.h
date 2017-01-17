//
//  WQUserDataManager.h
//  TingCheBao_user
//
//  Created by yangshaojin on 15-3-16.
//  Copyright (c) 2015年 zhenLaiDian. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface WQUserDataManager : NSObject

/**
 *  @brief  存储密码
 *
 *  @param  password    密码内容
 */
+(void)savePassWord:(NSString *)password;

/**
 *  @brief  读取密码
 *
 *  @return 密码内容
 */
+(id)readPassWord;

/**
 *  @brief  删除密码数据
 */
+(void)deletePassWord;

@end
