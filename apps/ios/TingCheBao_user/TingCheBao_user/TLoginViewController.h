//
//  TLoginViewController.h
//  TingCheBao_user
//
//  Created by apple on 14-9-4.
//  Copyright (c) 2014年 zhenLaiDian. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "TBaseViewController.h"
typedef void(^LoginCompleter)();

@interface TLoginViewController : TBaseViewController

@property(nonatomic, copy) LoginCompleter completer;

@end
