//
//  THolidayView.m
//  TingCheBao_user
//
//  Created by apple on 14/12/25.
//  Copyright (c) 2014年 zhenLaiDian. All rights reserved.
//

#import "THolidayView.h"

@interface THolidayView()

@property(nonatomic, retain) UIView* bgView;

@property(nonatomic, retain) UIButton* closeButton;

@end
@implementation THolidayView

- (id)initWithFrame:(CGRect)frame {
    if (self = [super initWithFrame:frame]) {
        _bgView = [[UIView alloc] initWithFrame:self.frame];
        _bgView.backgroundColor = [UIColor blackColor];
        _bgView.alpha = 0.7;
        
        
        CGFloat width = self.width - 2*5;
        CGFloat height = width / 3 * 2;
        _holidayButton = [UIButton buttonWithType:UIButtonTypeCustom];
        _holidayButton.frame = CGRectMake(5, (self.height - height)/2, width, height);
        [_holidayButton setImage:[UIImage imageNamed:@""] forState:UIControlStateNormal];
        [_holidayButton addTarget:self action:@selector(buttonTouched:) forControlEvents:UIControlEventTouchUpInside];
        _holidayButton.userInteractionEnabled = NO;//默认不能点击
        
        _closeButton = [UIButton buttonWithType:UIButtonTypeCustom];
        _closeButton.frame = CGRectMake(_holidayButton.right - 40, _holidayButton.top, 40, 40);
        [_closeButton setImage:[UIImage imageNamed:@"close_gray.png"] forState:UIControlStateNormal];
        [_closeButton addTarget:self action:@selector(buttonTouched:) forControlEvents:UIControlEventTouchUpInside];
        
        [self addSubview:_bgView];
        [self addSubview:_holidayButton];
        [self addSubview:_closeButton];
    }
    return self;
}

- (void)buttonTouched:(UIButton*)button {
    if (_delegate) {
        if (button == _closeButton) {
            [_delegate holidayCloseTouched];
        } else if (button == _holidayButton) {
            [_delegate holidayShareTouched];
        }
    }
}
@end
