//
//  ViewController.h
//  Dog
//
//  Created by apple on 14-7-24.
//  Copyright (c) 2014年 zhenLaiDian. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "TLeftViewController.h"
#import "TRightViewController.h"

#define left_menu_max_width 280

@interface TViewController : UIViewController

@property(nonatomic, retain) UINavigationController* centerController;
@property(nonatomic, retain) TLeftViewController* leftController;
@property(nonatomic, retain) UIView* leftView;
@property(nonatomic, retain) TRightViewController* rightController;//right暂没用 可能有问题
@property(nonatomic, retain) UIView* rightView;

//- (void)updateState:(NSString*)viewName show:(BOOL)show;
- (void)showOrHideLeftMenu;
- (void)showOrHideRightMenu;
- (void)requestCarNumber;

+ (TViewController*)share;
+ (void)setInstance:(TViewController*)instance;

@end
