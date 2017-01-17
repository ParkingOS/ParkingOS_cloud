//
//  UIButton+Utilities.m
//  TingCheBao
//
//  Created by yangshaojin on 14-10-10.
//  Copyright (c) 2014年 zhenLaiDian. All rights reserved.
//

#import "UIButton+Utilities.h"

@implementation UIButton (Utilities)

+ (UIButton *)checkButtonWithFrame:(CGRect)frame
                             image:(NSString *)imageName
                       selectImage:(NSString *)selectImageName
                            target:(id)target
                            action:(SEL)action;
{
    UIButton *button = [UIButton buttonWithType:UIButtonTypeCustom];
    [button setFrame:frame];
    [button setImage:[UIImage imageNamed:imageName] forState:UIControlStateNormal];
    [button setImage:[UIImage imageNamed:selectImageName] forState:UIControlStateSelected];
    [button addTarget:target action:action forControlEvents:UIControlEventTouchUpInside];
    return button;
}

+ (UIButton *)imageButtonWithFrame:(CGRect)frame
                             image:(NSString *)imageName
                            target:(id)target
                            action:(SEL)action
{
    UIButton *button = [UIButton checkButtonWithFrame:frame
                                                image:imageName
                                          selectImage:nil
                                               target:target
                                               action:action];
    return button;
}

+ (UIButton *)titleButtonWithFrame:(CGRect)frame
                             title:(NSString *)title
                         textColor:(UIColor *)textColor
                   backgroundColor:(UIColor *)backgroundColor
                            target:(id)target
                            action:(SEL)action
{
    UIButton *button = [UIButton buttonWithType:UIButtonTypeCustom];
    [button setFrame:frame];
    [button setTitle:title forState:UIControlStateNormal];
    [button setTitleColor:textColor forState:UIControlStateNormal];
    [button setBackgroundColor:backgroundColor];
    [button addTarget:target action:action forControlEvents:UIControlEventTouchUpInside];
    return button;
}

@end
