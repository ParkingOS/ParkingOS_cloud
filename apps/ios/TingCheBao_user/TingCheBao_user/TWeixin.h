//
//  TWeixin.h
//  TingCheBao_user
//
//  Created by apple on 14/11/5.
//  Copyright (c) 2014年 zhenLaiDian. All rights reserved.
//

#import <Foundation/Foundation.h>

#import "WXApi.h"
#import "WXApiObject.h"
#import "CVAPIRequestModel.h"

@protocol TWeixinDelegate <NSObject>

- (void)weixinFail;

@end
@interface TWeixin : NSObject


+ (id)getInstance;

- (void)sendWithName:(NSString*)name price:(NSString*)price description:(NSString*)description delegate:(id<TWeixinDelegate>)delegate;
- (void)onResp:(BaseResp *)resp;
@end
