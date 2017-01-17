//
//  TTicketHelpController.h
//  TingCheBao_user
//
//  Created by apple on 14/11/3.
//  Copyright (c) 2014年 zhenLaiDian. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "TBaseViewController.h"

@interface TTicketHelpController : TBaseViewController<UIWebViewDelegate>

@property(nonatomic, assign) BOOL redNavi;

@property(nonatomic, retain) UIWebView* webView;

- (id)initWithName:(NSString*)name url:(NSString*)url;

@end
