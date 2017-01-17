//
//  TVersionAlertView.m
//  TingCheBao_user
//
//  Created by apple on 14/11/20.
//  Copyright (c) 2014年 zhenLaiDian. All rights reserved.
//

#import "TVersionAlertView.h"
#import "TAPIUtility.h"

#define web_url      @"itms-services://?action=download-manifest&url=https://dn-tingchebao.qbox.me/tingCheBao.plist"

@implementation TVersionAlertView

- (id)init {
    if (self = [super init]) {
        self.title = nil;
        self.delegate = self;
        [self addButtonWithTitle:@"取消"];
        [self addButtonWithTitle:@"更新"];
    }
    return self;
}

- (void)setVersion:(NSString *)version {
    NSString* message = [NSString stringWithFormat:@"检测到有新的版本:%@,是否更新?", version];
    self.message = message;
    
}

- (void)alertView:(UIAlertView *)alertView clickedButtonAtIndex:(NSInteger)buttonIndex {
    [self dismissWithClickedButtonIndex:buttonIndex animated:YES];
    
    if (buttonIndex == 1) {
        NSURL* urlForInstall = [NSURL URLWithString:[TAPIUtility isEnterpriseVersion] ? web_url : _appStoreUrl];
        [[UIApplication sharedApplication] openURL:urlForInstall];
    }
}

@end
