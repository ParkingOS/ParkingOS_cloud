//
//  TRechargeResultView.h
//  TingCheBao_user
//
//  Created by apple on 14/10/17.
//  Copyright (c) 2014年 zhenLaiDian. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "TRechargeWaysViewController.h"

@protocol TRechargeResultViewDelegate <NSObject>

- (void)resultLeftButtonTouched:(UIButton*)button;
- (void)resultRightButtonTouched:(UIButton*)button;

@end
@interface TRechargeResultView : UIView

@property(nonatomic, unsafe_unretained) id<TRechargeResultViewDelegate> delegate;

- (void)setObjectWithSucc:(BOOL)succ money:(NSString*)money mode:(RechargeMode)mode;

@end
