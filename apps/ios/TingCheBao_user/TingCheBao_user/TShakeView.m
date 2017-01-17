//
//  TShakeView.m
//  TingCheBao_user
//
//  Created by yangshaojin on 15-3-23.
//  Copyright (c) 2015年 zhenLaiDian. All rights reserved.
//

#import "TShakeView.h"

@interface TShakeView ()

@property (nonatomic, readwrite) UIImageView *shakeImageView;
@property (nonatomic, readwrite) UIButton *hiddenButton;
@property (nonatomic, readwrite) UILabel *normalLabel;
@property (nonatomic, readwrite) UILabel *orderLabel;
@property (nonatomic, readwrite) UIActivityIndicatorView *indicatorView;
@end

static TShakeView *shakeView = nil;

@implementation TShakeView

+ (instancetype)getInstance
{
    static dispatch_once_t predicate;
    
    dispatch_once(&predicate, ^{
        shakeView = [[TShakeView alloc] initWithFrame:CGRectMake((mainScreenSize.width - 260)/2, (mainScreenSize.height - 260)/2 - 100, 260, 260)];
    });
    return shakeView;
}

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
        [self addSubview:self.normalLabel];
        [self addSubview:self.orderLabel];
        [self addSubview:self.indicatorView];
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
        _shakeImageView = [[UIImageView alloc] initWithImage:[UIImage imageNamed:@"yao_yao.png"]];
        [_shakeImageView setFrame:CGRectMake(80, 50, 100, 100)];
    }
    return _shakeImageView;
}

- (UILabel *)normalLabel
{
    if (!_normalLabel) {
        _normalLabel = [[UILabel alloc] initWithFrame:CGRectMake(0, 180, 260, 60)];
        _normalLabel.backgroundColor = [UIColor clearColor];
        _normalLabel.textAlignment = NSTextAlignmentCenter;
        _normalLabel.text = @"摇一摇，生成订单";
        _normalLabel.numberOfLines = 0;
        _normalLabel.font = [UIFont boldSystemFontOfSize:20];
        _normalLabel.textColor = RGBCOLOR(149, 149, 149);
    }
    return _normalLabel;
}

- (UILabel *)orderLabel
{
    if (!_orderLabel) {
        _orderLabel = [[UILabel alloc] initWithFrame:CGRectMake(80, 200, 160, 30)];
        _orderLabel.backgroundColor = [UIColor clearColor];
        _orderLabel.textAlignment = NSTextAlignmentLeft;
        _orderLabel.text = @"订单生成中...";
        _orderLabel.textColor = RGBCOLOR(149, 149, 149);
        _orderLabel.font = [UIFont boldSystemFontOfSize:20];
    }
    return _orderLabel;
}

- (UIActivityIndicatorView *)indicatorView
{
    if (!_indicatorView) {
        _indicatorView = [[UIActivityIndicatorView alloc] initWithActivityIndicatorStyle:UIActivityIndicatorViewStyleGray];
        _indicatorView.center = CGPointMake(60, 216);
        [_indicatorView setHidesWhenStopped:YES];
    }
    return _indicatorView;
}

- (UIButton *)hiddenButton
{
    if (!_hiddenButton) {
        _hiddenButton = [UIButton buttonWithType:UIButtonTypeCustom];
        [_hiddenButton setBackgroundColor:[UIColor clearColor]];
        [_hiddenButton setBackgroundImage:[UIImage imageNamed:@"X.png"] forState:UIControlStateNormal];
        [_hiddenButton addTarget:self action:@selector(hiddenButtonClick:) forControlEvents:UIControlEventTouchUpInside];
        _hiddenButton.frame = CGRectMake(212, 15, 30, 30);
    }
    return _hiddenButton;
}

- (void)hiddenButtonClick:(UIButton *)sender
{
    [self setHidden:YES];
}

- (void)normal:(BOOL)inCome
{
    self.hidden = NO;
    self.normalLabel.hidden = NO;
    [self.indicatorView stopAnimating];
    self.orderLabel.hidden = YES;
    
    if (inCome) {
        self.normalLabel.text = @"摇一摇,生成订单";
    }else{
        self.normalLabel.text = @"摇一摇,结算订单";
    }
}

- (void)normalWithText:(NSString *)str
{
    [self normal:YES];
    self.normalLabel.text = str;
}

- (BOOL)start:(BOOL)inCome
{
    if (self.orderLabel.hidden) {
        self.hidden = NO;
        self.normalLabel.hidden = YES;
        [self.indicatorView startAnimating];
        self.orderLabel.hidden = NO;
        
        if (inCome) {
            self.orderLabel.text = @"订单生成中...";
        }else{
            self.orderLabel.text = @"订单结算中...";
        }
    }else{
        return self.orderLabel.hidden;
    }
    return !self.orderLabel.hidden;
}

- (void)moso
{
    self.hidden = NO;
    self.normalLabel.hidden = YES;
    [self.indicatorView startAnimating];
    self.orderLabel.hidden = NO;
    self.orderLabel.text   = @"正在搜索中...";
}

- (void)stop
{
    [self normal:YES];
    self.hidden = YES;
}

- (void)fail
{
    self.hidden = NO;
    self.normalLabel.hidden = NO;
    [self.indicatorView stopAnimating];
    self.orderLabel.hidden = YES;
    
    self.normalLabel.text = @"订单生成失败,请确认\n当前位置在蓝牙车场进口";
}

@end
