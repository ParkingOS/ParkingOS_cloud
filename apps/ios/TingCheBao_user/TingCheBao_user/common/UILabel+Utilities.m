//
//  UILabel+Utilities.m
//  TingCheBao
//
//  Created by yangshaojin on 14-10-10.
//  Copyright (c) 2014年 zhenLaiDian. All rights reserved.
//

#import "UILabel+Utilities.h"

@implementation UILabel (Utilities)

+ (UILabel *)labelWithTitle:(NSString *)Title
                       font:(NSInteger)font
                  textColor:(UIColor *)textColor
            backgroundColor:(UIColor *)backgroundColor
                      Frame:(CGRect)frame
{
    UILabel *label = [[UILabel alloc] initWithFrame:frame];
    label.text     = Title;
    label.textColor     = textColor;
    label.textAlignment = NSTextAlignmentCenter;
    
    label.numberOfLines = 0;
    label.lineBreakMode = NSLineBreakByCharWrapping;
    label.font            = [UIFont systemFontOfSize:font];
    label.backgroundColor = backgroundColor;
    return label;
}
@end
