//
//  TLeftViewController.h
//  TingCheBao_user
//
//  Created by apple on 14-8-19.
//  Copyright (c) 2014年 zhenLaiDian. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface TLeftViewController : UIViewController



- (void)selectIndex:(int)index;
- (void)selectHomePage;

- (void)startRequestCarNumber:(BOOL)start isAuth:(NSString*)isAuth;
- (void)updateLoginState:(BOOL)login;

@end
