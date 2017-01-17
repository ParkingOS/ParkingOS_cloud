//
//  TPayResultView.h
//  TingCheBao_user
//
//  Created by apple on 15/5/12.
//  Copyright (c) 2015年 zhenLaiDian. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "TPopView.h"
#import "TRechargeWaysViewController.h"

@protocol TPayResultViewDelegate <NSObject>

- (void)resultLeftButtonTouched:(UIButton*)button;
- (void)resultRightButtonTouched:(UIButton*)button;

@end

@interface TPayResultView : TPopView

@property(nonatomic, unsafe_unretained) id<TPayResultViewDelegate> delegate;
@property(nonatomic, retain) UIButton* leftButton;

@property(nonatomic, assign, readonly) BOOL redPackage;//付车费红包
@property(nonatomic, assign, readonly) BOOL rechargeRedPackage;//充值礼包

- (void)setObjectWithSucc:(BOOL)succ redPackge:(BOOL)redPackge mode:(RechargeMode)mode;
//充值礼包
- (void)setObjectWithRechargeRedpackage;

@end
