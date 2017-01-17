//
//  TSignUpViewController.h
//  TingCheBao_user
//
//  Created by apple on 14-9-9.
//  Copyright (c) 2014年 zhenLaiDian. All rights reserved.
//

#import "TBaseViewController.h"
#import "TLoginViewController.h"

@interface TSignUpViewController : TBaseViewController

@property(nonatomic, retain) NSString* phoneNum;

@property(nonatomic, copy)  LoginCompleter completer;

@end
