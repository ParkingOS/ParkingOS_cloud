//
//  TPromptImageView.m
//  TingCheBao_user
//
//  Created by yangshaojin on 15-3-24.
//  Copyright (c) 2015年 zhenLaiDian. All rights reserved.
//

#import "TShakeView.h"
#import "TPromptImageView.h"

@interface TPromptImageView ()

@property (nonatomic, readwrite) UIImageView *shakeImageView;
@property (nonatomic, readwrite) UIButton *hiddenButton;

@end

@implementation TPromptImageView

- (instancetype)initWithFrame:(CGRect)frame
{
    if (self = [super initWithFrame:frame]) {
        
        
        self.backgroundColor = [UIColor whiteColor];
        self.layer.masksToBounds = YES;
        self.layer.cornerRadius  = 5;
        self.layer.borderColor   = RGBCOLOR(200, 200, 200).CGColor;
        self.layer.borderWidth   = 1;
        
        [self addSubview:self.hiddenButton];
        [self addSubview:self.shakeImageView];
    }
    return self;
}
/*
// Only override drawRect: if you perform custom drawing.
// An empty implementation adversely affects performance during animation.
- (void)drawRect:(CGRect)rect {
    // Drawing code
}
*/
- (UIImageView *)shakeImageView
{
    if (!_shakeImageView) {
        _shakeImageView = [[UIImageView alloc] initWithImage:[UIImage imageNamed:@"ibeacon_prompt.jpg"]];
        [_shakeImageView setFrame:CGRectMake(0, 0, self.frame.size.width, self.frame.size.height - 70)];
    }
    return _shakeImageView;
}

- (UIButton *)hiddenButton
{
    if (!_hiddenButton) {
        _hiddenButton = [UIButton buttonWithType:UIButtonTypeCustom];
        [_hiddenButton setBackgroundColor:[UIColor clearColor]];
        [_hiddenButton setTitle:@"知道了"  forState:UIControlStateNormal];
        [_hiddenButton setTitleColor:RGBCOLOR(33, 176, 95) forState:UIControlStateNormal];
        [_hiddenButton addTarget:self action:@selector(hiddenButtonClick:) forControlEvents:UIControlEventTouchUpInside];
        _hiddenButton.frame = CGRectMake((self.width - 150)/2, self.frame.size.height - 55, 150, 35);
        
        self.hiddenButton.backgroundColor = [UIColor clearColor];
        self.hiddenButton.layer.masksToBounds = YES;
        self.hiddenButton.layer.borderColor   = RGBCOLOR(33, 176, 95).CGColor;
        self.hiddenButton.layer.borderWidth   = 1;
    }
    return _hiddenButton;
}

- (void)hiddenButtonClick:(UIButton *)sender
{
    [self setHidden:YES];
    if (self.superview) {
        [self removeFromSuperview];
    }
    [[TShakeView getInstance] normal:YES];
}

@end
