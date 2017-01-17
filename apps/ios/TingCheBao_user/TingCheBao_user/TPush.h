//
//  TPush.h
//  TingCheBao_user
//
//  Created by apple on 14/10/30.
//  Copyright (c) 2014年 zhenLaiDian. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface TPush : NSObject

+ (TPush*)getInstance;

- (void)handlePush:(NSDictionary*)userInfo;
- (void)requestMsg:(NSString*)msgId;
@end
