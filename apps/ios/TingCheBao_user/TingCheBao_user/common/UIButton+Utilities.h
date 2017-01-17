//
//  UIButton+Utilities.h
//  TingCheBao
//
//  Created by yangshaojin on 14-10-10.
//  Copyright (c) 2014年 zhenLaiDian. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface UIButton (Utilities)

+ (UIButton *)checkButtonWithFrame:(CGRect)frame
                             image:(NSString *)imageName
                       selectImage:(NSString *)selectImageName
                            target:(id)target
                            action:(SEL)action;

+ (UIButton *)imageButtonWithFrame:(CGRect)frame
                             image:(NSString *)imageName
                            target:(id)target
                            action:(SEL)action;

+ (UIButton *)titleButtonWithFrame:(CGRect)frame
                             title:(NSString *)title
                         textColor:(UIColor *)textColor
                   backgroundColor:(UIColor *)backgroundColor
                            target:(id)target
                            action:(SEL)action;
@end
