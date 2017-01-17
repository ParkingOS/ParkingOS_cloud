//
//  TShakeView.h
//  TingCheBao_user
//
//  Created by yangshaojin on 15-3-23.
//  Copyright (c) 2015年 zhenLaiDian. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface TShakeView : UIView

+ (instancetype)getInstance;

- (void)normal:(BOOL)inCome;
- (BOOL)start:(BOOL)inCome;
- (void)stop;
- (void)fail;
- (void)moso;
- (void)normalWithText:(NSString *)str;

@end
