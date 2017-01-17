//
//  TShareRedPackageView.h
//  TingCheBao_user
//
//  Created by yangshaojin on 15-3-9.
//  Copyright (c) 2015年 zhenLaiDian. All rights reserved.
//

#import "TRechargeWaysViewController.h"
#import <UIKit/UIKit.h>

@interface TShareRedPackageView : UIView

@property (nonatomic, unsafe_unretained) TRechargeWaysViewController *delegate;

- (void)paymentwithRedpackage:(BOOL)Has;

@end
