//
//  TSession.h
//  TingCheBao_user
//
//  Created by apple on 14-8-27.
//  Copyright (c) 2014年 zhenLaiDian. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface TSession : NSObject

@property(nonatomic, retain) NSArray* carNumbers;
//@property(nonatomic, retain) NSString* carAuth;

- (void)removeAll;

+ (TSession*)shared;

@end
