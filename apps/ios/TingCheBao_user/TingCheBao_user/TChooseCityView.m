//
//  TChooseCityView.m
//  TingCheBao_user
//
//  Created by apple on 14/11/18.
//  Copyright (c) 2014年 zhenLaiDian. All rights reserved.
//

#import "TChooseCityView.h"

@interface TChooseCityView()

@property(nonatomic, retain) UIView* bgView;
@property(nonatomic, retain) UIView* centerView;
@property(nonatomic, retain) UILabel* titleLabel;
@property(nonatomic, retain) UILabel* contentLabel;
@property(nonatomic, retain) UIView* line1View;
@property(nonatomic, retain) UIButton* beijingButton;
@property(nonatomic, retain) UIView* line2View;
@property(nonatomic, retain) UIButton* returnButton;

@end

@implementation TChooseCityView

- (id)initWithFrame:(CGRect)frame {
    if (self = [super initWithFrame:frame]) {
        
        _bgView = [[UIView alloc] initWithFrame:self.frame];
        _bgView.backgroundColor = [UIColor blackColor];
        _bgView.alpha = 0.7;
        
        _centerView = [[UIView alloc] initWithFrame:CGRectMake(30, 60, self.width - 30*2, 210)];
        _centerView.backgroundColor = [UIColor whiteColor];
        
        //contentView
        _titleLabel = [[UILabel alloc] initWithFrame:CGRectMake(0, 10, _centerView.width, 30)];
        _titleLabel.backgroundColor = [UIColor clearColor];
        _titleLabel.text = @"我们暂时没有覆盖这个区域";
        _titleLabel.font = [UIFont boldSystemFontOfSize:20];
        _titleLabel.textAlignment = NSTextAlignmentCenter;
        
        _contentLabel = [[UILabel alloc] initWithFrame:CGRectMake(0, _titleLabel.bottom + 10, _centerView.width, 60)];
        _contentLabel.backgroundColor = [UIColor clearColor];
        _contentLabel.text = @"我们正在努力扩大覆盖范围，\n您可以先体验下面的城市";
        _contentLabel.numberOfLines = 0;
        _contentLabel.textAlignment = NSTextAlignmentCenter;
        _contentLabel.font = [UIFont systemFontOfSize:15];
        
        _line1View = [[UIView alloc] initWithFrame:CGRectMake(0, _contentLabel.bottom + 20, _centerView.width, 1)];
        _line1View.backgroundColor = [UIColor lightGrayColor];
        
        _beijingButton = [UIButton buttonWithType:UIButtonTypeCustom];
        [_beijingButton setTitle:@"北京" forState:UIControlStateNormal];
        [_beijingButton setTitleColor:green_color forState:UIControlStateNormal];
        _beijingButton.frame = CGRectMake(0, _line1View.bottom, _centerView.width, 35);
        [_beijingButton addTarget:self action:@selector(buttonTouched:) forControlEvents:UIControlEventTouchUpInside];
        
        _line2View = [[UIView alloc] initWithFrame:CGRectMake(0, _beijingButton.bottom, _centerView.width, 1)];
        _line2View.backgroundColor = [UIColor lightGrayColor];
        
        _returnButton = [UIButton buttonWithType:UIButtonTypeCustom];
        [_returnButton setTitle:@"返回" forState:UIControlStateNormal];
        [_returnButton setTitleColor:green_color forState:UIControlStateNormal];
        _returnButton.frame = CGRectMake(0, _line2View.bottom, _centerView.width, 35);
        [_returnButton addTarget:self action:@selector(buttonTouched:) forControlEvents:UIControlEventTouchUpInside];
        
        [_centerView addSubview:_titleLabel];
        [_centerView addSubview:_contentLabel];
        [_centerView addSubview:_line1View];
        [_centerView addSubview:_beijingButton];
        [_centerView addSubview:_line2View];
        [_centerView addSubview:_returnButton];
        
        [self addSubview:_bgView];
        [self addSubview:_centerView];
    }
    return self;
}

- (void)buttonTouched:(UIButton*)button {
    int index = 0;
    if (button == _beijingButton) {
        index = 0;
    } else if (button == _returnButton) {
        index = 1;
    }
    if (_delegate && [_delegate respondsToSelector:@selector(chooseCityViewWithIndex:)]) {
        [_delegate chooseCityViewWithIndex:index];
    }
}

@end
