//
//  TPayResultView.m
//  TingCheBao_user
//
//  Created by apple on 15/5/12.
//  Copyright (c) 2015年 zhenLaiDian. All rights reserved.
//

#import "TPayResultView.h"
#import "TAPIUtility.h"

#define image_width 100
#define padding 10

@interface TPayResultView()

@property(nonatomic, retain) UIImageView* resultImgView;
@property(nonatomic, retain) UILabel* resultLabel;
@property(nonatomic, retain) UIButton* rightButton;

@property(nonatomic, assign) BOOL success;
@property(nonatomic, assign) RechargeMode mode;

@end
@implementation TPayResultView

- (id)initWithFrame:(CGRect)frame {
    if (self = [super initWithFrame:frame]) {
        self.centerView.height = 280;
        self.centerView.top = (frame.size.height - 280)/2;
        self.closeButton.frame = CGRectMake(self.centerView.right - 20, self.centerView.top - 20, 40, 40);
        
        _resultImgView = [[UIImageView alloc] init];
        
        _resultLabel = [[UILabel alloc] init];
        _resultLabel.textColor = [UIColor blackColor];
        _resultLabel.numberOfLines = 0;
        _resultLabel.textAlignment = NSTextAlignmentCenter;
        _resultLabel.font = [UIFont boldSystemFontOfSize:17];
        
        _leftButton = [UIButton buttonWithType:UIButtonTypeCustom];
        [_leftButton setTitle:@"去评论" forState:UIControlStateNormal];
        _leftButton.layer.borderWidth = 1;
        _leftButton.layer.cornerRadius = 5;
        [_leftButton addTarget:self action:@selector(buttonTouched:) forControlEvents:UIControlEventTouchUpInside];
        
        _rightButton = [UIButton buttonWithType:UIButtonTypeCustom];
        [_rightButton setTitle:@"查看礼包" forState:UIControlStateNormal];
        [_rightButton setTitleColor:[UIColor whiteColor] forState:UIControlStateNormal];
        [_rightButton setBackgroundImage:[TAPIUtility imageWithColor:red_color] forState:UIControlStateNormal];
        _rightButton.layer.cornerRadius = 5;
        _rightButton.clipsToBounds = YES;
        [_rightButton addTarget:self action:@selector(buttonTouched:) forControlEvents:UIControlEventTouchUpInside];
        
        [self.centerView addSubview:_resultImgView];
        [self.centerView addSubview:_resultLabel];
        [self.centerView addSubview:_leftButton];
        [self.centerView addSubview:_rightButton];
    }
    return self;
}

- (void)layoutSubviews {
    [super layoutSubviews];
    
    _resultImgView.frame = CGRectMake((self.centerView.width - image_width)/2, 30, image_width, image_width);
    _resultLabel.frame = CGRectMake(0, _resultImgView.bottom + 25, self.centerView.width,  50);
    _resultLabel.numberOfLines = (_redPackage || _rechargeRedPackage) ? 0 : 1;
    _resultLabel.textColor = [UIColor blackColor];
    
    if (_redPackage) {
        _resultImgView.image = [UIImage imageNamed:@"pay_redpackage.png"];
        CGFloat buttonWidth = (self.centerView.width - 3*padding)/2;
        _leftButton.frame = CGRectMake(padding, _resultLabel.bottom + 25, buttonWidth, 40);
        _leftButton.layer.borderColor = red_color.CGColor;
        [_leftButton setTitle:@"去评论" forState:UIControlStateNormal];
        [_leftButton setTitleColor:red_color forState:UIControlStateNormal];
        
        _rightButton.frame = CGRectMake(_leftButton.right + padding, _leftButton.top, buttonWidth, 40);
        _resultLabel.text = @"支付成功，收费员已确认收款\n恭喜您获得停车券大礼包";
        
    } else if (_rechargeRedPackage) {
        _resultImgView.image = [UIImage imageNamed:@"recharge_red.png"];
        CGFloat buttonWidth = self.centerView.width - 2*padding;
        _leftButton.frame = CGRectZero;
        
        _rightButton.frame = CGRectMake(padding, _resultLabel.bottom + 25, buttonWidth, 40);
        _resultLabel.textColor = red_color;
        _resultLabel.text = @"充值成功\n恭喜您获得停车宝充值大礼包";
        
    } else {
        CGFloat buttonWidth = self.centerView.width - 2*padding;
        _leftButton.frame = CGRectMake(padding, _resultLabel.bottom + 25, buttonWidth, 40);
        _rightButton.frame = CGRectZero;
        if (_success) {
            _resultImgView.image = [UIImage imageNamed:@"pay_success.png"];
            [_leftButton setTitle:_mode == RechargeMode_addMoney ? @"查看帐户" : @"去评论" forState:UIControlStateNormal];
            _leftButton.layer.borderColor = green_color.CGColor;
            [_leftButton setTitleColor:green_color forState:UIControlStateNormal];
            
            _resultLabel.text = @"支付成功";
        } else {
            _resultImgView.image = [UIImage imageNamed:@"pay_error.png"];
            [_leftButton setTitle:@"重新支付" forState:UIControlStateNormal];
            _leftButton.layer.borderColor = red_color.CGColor;
            [_leftButton setTitleColor:red_color forState:UIControlStateNormal];
            
            _resultLabel.text = @"支付失败";
        }
    }
}


- (void)buttonTouched:(UIButton*)button {
    if (button == _leftButton) {
        if (_delegate && [_delegate respondsToSelector:@selector(resultLeftButtonTouched:)]) {
            [_delegate resultLeftButtonTouched:button];
            return;
        }
    }
    if (button == _rightButton) {
        if (_delegate && [_delegate respondsToSelector:@selector(resultRightButtonTouched:)]) {
            [_delegate resultRightButtonTouched:button];
            return;
        }
    }
}

#pragma mark public
- (void)setObjectWithSucc:(BOOL)succ redPackge:(BOOL)redPackge mode:(RechargeMode)mode {
    _success = succ;
    _redPackage = redPackge;
    _mode = mode;
    
    [self setNeedsLayout];
}

//充值礼包
- (void)setObjectWithRechargeRedpackage {
    _rechargeRedPackage = YES;
    [self setObjectWithSucc:YES redPackge:NO mode:RechargeMode_addMoney];
}

@end
