//
//  TShareRedPackageView.m
//  TingCheBao_user
//
//  Created by yangshaojin on 15-3-9.
//  Copyright (c) 2015年 zhenLaiDian. All rights reserved.
//

#define kRedPackageColor [UIColor colorWithRed:244.0/255.0 green:144.0/255.0 blue:29.0/255.0 alpha:1]
#define kColor(r,g,b) [UIColor colorWithRed:r/255.0 green:g/255.0 blue:b/255.0 alpha:1]

#import "TShareRedPackageView.h"
@interface TShareRedPackageView()

@property (nonatomic,readwrite) UIView *contentView;
@property (nonatomic,readwrite) UILabel *titleLabel;
@property (nonatomic,readwrite) UIView *backView;
@property (nonatomic,readwrite) UIImageView *redImageView;
@property (nonatomic,readwrite) UILabel *smallLabel;
@property (nonatomic,readwrite) UILabel *smallMoreLabel;
@property (nonatomic,readwrite) UIButton *leftButton;
@property (nonatomic,readwrite) UIButton *rightButton;
@property (nonatomic,readwrite) UIView *lineView;

@property (nonatomic,readwrite) BOOL succ;

@end
@implementation TShareRedPackageView

- (instancetype)initWithFrame:(CGRect)frame
{
    self = [super initWithFrame:frame];
    if (self) {
        [self addSubview:self.contentView];
        self.backgroundColor = [[UIColor blackColor] colorWithAlphaComponent:0.6];
    }
    return self;
}

- (UIView *)contentView
{
    if (!_contentView) {
        _contentView = [[UIView alloc] initWithFrame:CGRectMake(0, 0, 280, 240)];
        _contentView.backgroundColor = [UIColor whiteColor];
        _contentView.center = self.center;
        
        [_contentView addSubview:self.titleLabel];
        [_contentView addSubview:self.backView];
        [_contentView addSubview:self.redImageView];
        [_contentView addSubview:self.smallLabel];
        [_contentView addSubview:self.smallMoreLabel];
        [_contentView addSubview:self.leftButton];
        [_contentView addSubview:self.rightButton];
        [_contentView addSubview:self.lineView];
    }
    return _contentView;
}

- (UIView *)backView
{
    if (!_backView) {
        _backView = [[UIView alloc] initWithFrame:CGRectMake(0, 40, 280, 160)];
        _backView.backgroundColor = kRedPackageColor;
    }
    return _backView;
}

- (UILabel *)titleLabel
{
    if (!_titleLabel) {
        _titleLabel = [[UILabel alloc] initWithFrame:CGRectMake(0, 0, 280, 40)];
        _titleLabel.backgroundColor = [UIColor clearColor];
        _titleLabel.textAlignment   = NSTextAlignmentCenter;
        _titleLabel.font = [UIFont boldSystemFontOfSize:18];
        _titleLabel.text = @"支付成功,收费员已确认收款";
    }
    return _titleLabel;
}

- (UIImageView *)redImageView
{
    if (!_redImageView) {
        _redImageView = [[UIImageView alloc] initWithFrame:CGRectMake(90, 60, 100, 60)];
    }
    return _redImageView;
}

- (UILabel *)smallLabel
{
    if (!_smallLabel) {
        _smallLabel = [[UILabel alloc] initWithFrame:CGRectMake(0, 130, 280, 30)];
        _smallLabel.backgroundColor = [UIColor clearColor];
        _smallLabel.textAlignment   = NSTextAlignmentCenter;
        _smallLabel.font = [UIFont systemFontOfSize:20];
        _smallLabel.textColor = [UIColor whiteColor];
    }
    return _smallLabel;
}

- (UILabel *)smallMoreLabel
{
    if (!_smallMoreLabel) {
        _smallMoreLabel = [[UILabel alloc] initWithFrame:CGRectMake(0, 170, 280, 30)];
        _smallMoreLabel.backgroundColor = [UIColor clearColor];
        _smallMoreLabel.textAlignment   = NSTextAlignmentCenter;
        _smallMoreLabel.font = [UIFont systemFontOfSize:20];
        _smallMoreLabel.textColor = [UIColor whiteColor];
    }
    return _smallMoreLabel;
}

- (UIButton *)leftButton
{
    if (!_leftButton) {
        _leftButton = [UIButton buttonWithType:UIButtonTypeCustom];
        _leftButton.frame = CGRectMake(0, 200, 140, 40);
        _leftButton.backgroundColor = [UIColor clearColor];
        [_leftButton addTarget:self action:@selector(leftButtonClick:) forControlEvents:UIControlEventTouchUpInside];
        [_leftButton setTitleColor:kColor(147.0, 147.0, 147.0) forState:UIControlStateNormal];
        _leftButton.titleLabel.font = [UIFont systemFontOfSize:15];
    }
    return _leftButton;
}

- (UIButton *)rightButton
{
    if (!_rightButton) {
        _rightButton = [UIButton buttonWithType:UIButtonTypeCustom];
        _rightButton.frame = CGRectMake(140, 200, 140, 40);
        _rightButton.backgroundColor = [UIColor clearColor];
        [_rightButton addTarget:self action:@selector(rightButtonClick:) forControlEvents:UIControlEventTouchUpInside];
        [_rightButton setTitleColor:kRedPackageColor forState:UIControlStateNormal];
        _rightButton.titleLabel.font = [UIFont systemFontOfSize:15];
    }
    return _rightButton;
}

- (void)leftButtonClick:(UIButton *)sender
{
    if (_delegate&&[_delegate respondsToSelector:@selector(shareRedPackageLeftButton)]) {
        [_delegate performSelector:@selector(shareRedPackageLeftButton) withObject:nil];
    }
}

- (void)rightButtonClick:(UIButton *)sender
{
    if (_delegate&&[_delegate respondsToSelector:@selector(shareRedPackageRightButton:)]) {
        [_delegate performSelector:@selector(shareRedPackageRightButton:) withObject:[NSNumber numberWithBool:_succ]];
    }
}

- (UIView *)lineView
{
    if (!_lineView) {
        _lineView = [[UIView alloc] initWithFrame:CGRectMake(140, 205, 1, 30)];
        _lineView.backgroundColor = kColor(197.0, 197.0, 197.0);
    }
    return _lineView;
}
/*
// Only override drawRect: if you perform custom drawing.
// An empty implementation adversely affects performance during animation.
- (void)drawRect:(CGRect)rect {
    // Drawing code
}
*/
- (void)paymentwithRedpackage:(BOOL)suc
{
    _succ = suc;
    if (suc) {
        self.redImageView.image = [UIImage imageNamed:@"redpacket.png"];
        self.smallLabel.text    = @"恭喜获得停车券大礼包";
        self.smallMoreLabel.hidden = YES;
        
        self.smallLabel.frame = CGRectMake(0, 150, 280, 30);
        
        [self.leftButton setTitle:@"不感兴趣" forState:UIControlStateNormal];
        [self.rightButton setTitle:@"查看礼包" forState:UIControlStateNormal];
    }else{
        self.redImageView.image = [UIImage imageNamed:@"noredpacket.png"];
        self.smallLabel.text    = @"很遗憾... ...";
        self.smallMoreLabel.text   = @"本次未能获得停车券礼包";
        self.smallMoreLabel.hidden = NO;
        
        self.smallLabel.frame = CGRectMake(0, 140, 280, 30);
        
        [self.leftButton setTitle:@"知道了" forState:UIControlStateNormal];
        [self.rightButton setTitle:@"查看礼包规则" forState:UIControlStateNormal];
    }
}

@end
