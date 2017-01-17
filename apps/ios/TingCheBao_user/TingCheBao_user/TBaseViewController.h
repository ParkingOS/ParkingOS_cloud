//
//  TBaseViewController.h
//  TingCheBao_user
//
//  Created by apple on 14-8-19.
//  Copyright (c) 2014年 zhenLaiDian. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "CVAPIRequestModel.h"

@interface TBaseViewController : UIViewController

@property(nonatomic, retain) UIBarButtonItem* leftItem;
@property(nonatomic, retain) UILabel* titleView;
@property(nonatomic, retain) CVAPIRequestModel* model;

- (void)clickedLeftItem :(UIButton*)button;

@end
