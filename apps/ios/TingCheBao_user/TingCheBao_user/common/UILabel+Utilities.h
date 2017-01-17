//
//  UILabel+Utilities.h
//  TingCheBao
//
//  Created by yangshaojin on 14-10-10.
//  Copyright (c) 2014年 zhenLaiDian. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface UILabel (Utilities)

+ (UILabel *)labelWithTitle:(NSString *)Title
                       font:(NSInteger)font
                  textColor:(UIColor *)textColor
            backgroundColor:(UIColor *)backgroundColor
                      Frame:(CGRect)frame;
@end
