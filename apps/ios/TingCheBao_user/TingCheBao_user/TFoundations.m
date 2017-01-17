//
//  TFoundations.m
//  TingCheBao
//
//  Created by apple on 14-7-15.
//  Copyright (c) 2014年 zhenLaiDian. All rights reserved.
//

#import "TFoundations.h"

void alertErrorMessage(NSString* message) {
    NSLog(@"%@", message);
    MBProgressHUD* hud = [MBProgressHUD showHUDAddedTo:[UIApplication sharedApplication].keyWindow animated:NO];
    UIImageView* imageView = [[UIImageView alloc] initWithImage:[UIImage imageNamed:@"alert.png"]];
    imageView.frame = CGRectMake(0, 0, 50, 50);
    hud.customView = imageView;
    hud.labelText = message;
    hud.mode = MBProgressHUDModeCustomView;
    hud.removeFromSuperViewOnHide = YES;
    [hud hide:YES afterDelay:1];
}
