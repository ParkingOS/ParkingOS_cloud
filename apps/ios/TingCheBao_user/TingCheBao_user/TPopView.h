//
//  TPopView.h
//  TingCheBao_user
//
//  Created by apple on 15/1/23.
//  Copyright (c) 2015年 zhenLaiDian. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface TPopView : UIView

@property(nonatomic, retain) UIWindow* myWindow;
@property(nonatomic, retain) UIView* bgView;
@property(nonatomic, retain) UIView* centerView;
@property(nonatomic, retain) UIButton* closeButton;

- (void)show:(BOOL)show;

@end
