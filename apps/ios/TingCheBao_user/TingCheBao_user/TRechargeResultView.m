//
//  TRechargeResultView.m
//  TingCheBao_user
//
//  Created by apple on 14/10/17.
//  Copyright (c) 2014年 zhenLaiDian. All rights reserved.
//

#import "TRechargeResultView.h"
#import "TAPIUtility.h"
#import "MobClick.h"

#define grayColor RGBCOLOR(164, 164, 164)

@interface TRechargeResultView()

@property(nonatomic, retain) UIView* bgView;
@property(nonatomic, retain) UIView* centerView;
@property(nonatomic, retain) UILabel* titleLabel;
@property(nonatomic, retain) UIView* lineView;
@property(nonatomic, retain) UIImageView* imgView;
@property(nonatomic, retain) UILabel* unitLabel;
@property(nonatomic, retain) UILabel* moneyLabel;
@property(nonatomic, retain) UILabel* infoLabel;
@property(nonatomic, retain) UIButton* leftButton;
@property(nonatomic, retain) UIButton* rightButton;

@property(nonatomic, assign) BOOL success;
@property(nonatomic, retain) NSString* money;
@property(nonatomic, assign) RechargeMode mode;

@property(nonatomic, retain) NSArray* options;
@end
@implementation TRechargeResultView

- (id)initWithFrame:(CGRect)frame {
    if (self = [super initWithFrame:frame]) {
        
        _options = @[@"查看包月产品", @"查看订单详情", @"查看我的帐户", @"关闭", @"分享红包"];
        
        _bgView = [[UIView alloc] init];
        _bgView.backgroundColor = [UIColor blackColor];
        _bgView.alpha = 0.8;
        
        _centerView = [[UIView alloc] init];
        _centerView.backgroundColor = [UIColor whiteColor];
        
        _titleLabel = [[UILabel alloc] init];
        _titleLabel.backgroundColor = [UIColor clearColor];
        
        _lineView = [[UIView alloc] init];
        _lineView.backgroundColor = grayColor;
        
        //------
        _imgView = [[UIImageView alloc] init];
        
        _unitLabel = [[UILabel alloc] init];
        _unitLabel.backgroundColor = [UIColor clearColor];
        _unitLabel.textColor = orange_color;
        _unitLabel.text = @"¥";
        _unitLabel.font = [UIFont boldSystemFontOfSize:19];
        
        _moneyLabel = [[UILabel alloc] init];
        _moneyLabel.backgroundColor = [UIColor clearColor];
        _moneyLabel.font = [UIFont boldSystemFontOfSize:30];
        
        _infoLabel = [[UILabel alloc] init];
        _infoLabel.backgroundColor = [UIColor clearColor];
        _infoLabel.font = [UIFont systemFontOfSize:13];
        _infoLabel.textColor = grayColor;
        _infoLabel.text = @"支付成功";
        _infoLabel.numberOfLines = 3;
        
        //------
        _leftButton = [UIButton buttonWithType:UIButtonTypeCustom];
        [_leftButton setBackgroundImage:[TAPIUtility imageWithColor:green_color] forState:UIControlStateNormal];
        [_leftButton addTarget:self action:@selector(leftButtonTouched:) forControlEvents:UIControlEventTouchUpInside];
        [_leftButton setTitleColor:[UIColor whiteColor] forState:UIControlStateNormal];
        _leftButton.titleLabel.font = [UIFont systemFontOfSize:13];
        
        _rightButton = [UIButton buttonWithType:UIButtonTypeCustom];
        [_rightButton setBackgroundImage:[TAPIUtility imageWithColor:grayColor] forState:UIControlStateNormal];
        [_rightButton addTarget:self action:@selector(rightButtonTouched:) forControlEvents:UIControlEventTouchUpInside];
        [_rightButton setTitleColor:[UIColor whiteColor] forState:UIControlStateNormal];
        [_rightButton setTitle:@"关闭" forState:UIControlStateNormal];
        _rightButton.titleLabel.font = [UIFont systemFontOfSize:13];
        
        //------
        [_centerView addSubview:_titleLabel];
        [_centerView addSubview:_lineView];
        [_centerView addSubview:_imgView];
        
        [_centerView addSubview:_unitLabel];
        [_centerView addSubview:_moneyLabel];
        [_centerView addSubview:_infoLabel];
        
        [_centerView addSubview:_leftButton];
        [_centerView addSubview:_rightButton];
        
        [self addSubview:_bgView];
        [self addSubview:_centerView];
    }
    return self;
}

- (void)layoutSubviews {
    [super layoutSubviews];
    _bgView.frame = self.frame;
    _centerView.frame = CGRectMake((self.width - 260)/2, (self.height - 200)/2, 260, 200);
    _titleLabel.frame = CGRectMake(10, 0, self.width, 40);
    _lineView.frame = CGRectMake(0, _titleLabel.bottom, _centerView.width, 2);
    _imgView.frame = CGRectMake(30, _lineView.bottom + 20, 50, 50);
    _unitLabel.frame = CGRectMake(_imgView.right + 4, _imgView.top  - 4, 20, 30);
    _moneyLabel.frame = CGRectMake(_unitLabel.right, _unitLabel.top, 100, 30);
    
    CGSize size = [TAPIUtility sizeWithFont:_infoLabel.font size:CGSizeMake(160, 80) text:_infoLabel.text];
    _infoLabel.frame = CGRectMake(_unitLabel.left, _unitLabel.bottom, 160, size.height);
    
    if (_mode == RechargeMode_collector && _success) {
        _leftButton.frame = CGRectMake(0, 160, _centerView.width, 40);
        _rightButton.frame = CGRectMake(_leftButton.right, 160, 0, 40);
    } else {
        _leftButton.frame = CGRectMake(0, 160, _centerView.width/2, 40);
        _rightButton.frame = CGRectMake(_leftButton.right, 160, _centerView.width/2, 40);
    }
}

- (void)setObjectWithSucc:(BOOL)succ money:(NSString*)money mode:(RechargeMode)mode {
    _success = succ;
    _money = money;
    _mode = mode;
    
    if (succ) {
        _titleLabel.text = @"支付成功";
        _imgView.image = [UIImage imageNamed:@"img_pay_success.png"];
        _infoLabel.text = @"支付成功";
        _infoLabel.textColor = grayColor;
        _infoLabel.font = [UIFont systemFontOfSize:13];
        
        [_rightButton setTitle:@"关闭" forState:UIControlStateNormal];
        [_leftButton setTitle:[_options objectAtIndex:_mode] forState:UIControlStateNormal];
        [_leftButton setBackgroundImage:[TAPIUtility imageWithColor:green_color] forState:UIControlStateNormal];
        _rightButton.tag = 1;
        
    } else {
        
        _titleLabel.text = @"支付失败";
        _imgView.image = [UIImage imageNamed:@"img_pay_failure.png"];
        
        _infoLabel.text = @"支付失败";
        _infoLabel.font = [UIFont systemFontOfSize:13];
        _infoLabel.textColor = grayColor;
        
        [_leftButton setTitle:@"选择其他支付方式" forState:UIControlStateNormal];
        [_leftButton setBackgroundImage:[TAPIUtility imageWithColor:orange_color] forState:UIControlStateNormal];
        
        [_rightButton setTitle:@"关闭" forState:UIControlStateNormal];
        _rightButton.tag = 0;
    }
    _moneyLabel.text = money;
    
    [self layoutSubviews];
    //自定义事件
//    NSArray* events = @[@"月卡", @"订单", @"充值", @"直付"];
//    [MobClick event:@"1" attributes:@{@"product" : [events objectAtIndex:mode], @"result" : succ ? @"成功" : @"失败"}];
}

- (void)leftButtonTouched:(UIButton*)button {
    if (_delegate && [_delegate respondsToSelector:@selector(resultLeftButtonTouched:)]) {
        [_delegate resultLeftButtonTouched:button];
    }
}

- (void)rightButtonTouched:(UIButton*)button {
    if (_delegate && [_delegate respondsToSelector:@selector(resultRightButtonTouched:)]) {
        [_delegate resultRightButtonTouched:button];
    }
}
@end
