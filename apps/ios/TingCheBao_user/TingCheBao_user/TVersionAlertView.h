//
//  TVersionAlertView.h
//  TingCheBao_user
//
//  Created by apple on 14/11/20.
//  Copyright (c) 2014年 zhenLaiDian. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface TVersionAlertView : UIAlertView

@property(nonatomic, retain) NSString* appStoreUrl;

- (void)setVersion:(NSString*)version;

@end
