//
//  TShareView.m
//  TingCheBao_user
//
//  Created by apple on 14/12/25.
//  Copyright (c) 2014年 zhenLaiDian. All rights reserved.
//

#import "TShareView.h"

#define button_width 60

@interface TShareView()

@property(nonatomic, retain) UIView* bgView;

@property(nonatomic, retain) UILabel* titleLabel;
@property(nonatomic, retain) UIButton* friendButton;
@property(nonatomic, retain) UILabel* friendLabel;
@property(nonatomic, retain) UIButton* circleButton;
@property(nonatomic, retain) UILabel* circleLabel;

@end
@implementation TShareView

- (id)initWithFrame:(CGRect)frame {
    if (self = [super initWithFrame:frame]) {
        _bgView = [[UIView alloc] initWithFrame:self.frame];
        _bgView.backgroundColor = [UIColor blackColor];
        _bgView.alpha = 0.7;
        UITapGestureRecognizer* gesture = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(handleTapGesture)];
        [_bgView addGestureRecognizer:gesture];
        
        //centerView
        _centerView = [[UIView alloc] initWithFrame:CGRectMake(0, self.height - 170, self.width, 170)];
        _centerView.backgroundColor = RGBCOLOR(235, 235, 235);
        _centerView.clipsToBounds = YES;
        
        _titleLabel = [[UILabel alloc] initWithFrame:CGRectMake(0, 0, _centerView.width, 40)];
        _titleLabel.textColor = gray_color;
        _titleLabel.text = @"分享红包";
        _titleLabel.textAlignment = NSTextAlignmentCenter;
//        _titleLabel.font = [UIFont systemFontOfSize:13];
        
        _friendButton = [UIButton buttonWithType:UIButtonTypeCustom];
        _friendButton.frame = CGRectMake((self.width - 3*button_width)/2, _titleLabel.bottom + 20, button_width, button_width);
        [_friendButton setImage:[UIImage imageNamed:@"weixin.png"] forState:UIControlStateNormal];
        [_friendButton addTarget:self action:@selector(buttonTouched:) forControlEvents:UIControlEventTouchUpInside];
        
        _friendLabel = [[UILabel alloc] initWithFrame:CGRectMake(_friendButton.left, _friendButton.bottom + 10, button_width, 20)];
        _friendLabel.textColor = gray_color;
        _friendLabel.text = @"微信好友";
        _friendLabel.font = [UIFont systemFontOfSize:13];
        
        _circleButton = [UIButton buttonWithType:UIButtonTypeCustom];
        _circleButton.frame = CGRectMake(_friendButton.right + button_width, _titleLabel.bottom + 20, button_width, button_width);
        [_circleButton setImage:[UIImage imageNamed:@"friendCircle.png"] forState:UIControlStateNormal];
        [_circleButton addTarget:self action:@selector(buttonTouched:) forControlEvents:UIControlEventTouchUpInside];
        
        _circleLabel = [[UILabel alloc] initWithFrame:CGRectMake(_circleButton.left - 5, _circleButton.bottom + 10, button_width + 10, 20)];
        _circleLabel.textColor = gray_color;
        _circleLabel.text = @"微信朋友圈";
        _circleLabel.font = [UIFont systemFontOfSize:13];
        
        [_centerView addSubview:_titleLabel];
        [_centerView addSubview:_friendButton];
        [_centerView addSubview:_friendLabel];
        [_centerView addSubview:_circleButton];
        [_centerView addSubview:_circleLabel];
        
        [self addSubview:_bgView];
        [self addSubview:_centerView];
    }
    return self;
}

- (void)buttonTouched:(UIButton*)button {
    if (_delegate) {
        NSInteger index = 0;
        if (button == _friendButton) {
            index = 0;
        } else if (button == _circleButton) {
            index = 1;
        }
        [_delegate shareViewTouched:self index:index];
    }
}

- (void)handleTapGesture {
    [UIView animateWithDuration:0.3 animations:^{
        _centerView.top = self.bottom;
    } completion:^(BOOL finished) {
        [self removeFromSuperview];
    }];
}

@end