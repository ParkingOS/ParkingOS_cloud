//
//  TPayView.m
//  TingCheBao_user
//
//  Created by apple on 14/10/21.
//  Copyright (c) 2014年 zhenLaiDian. All rights reserved.
//

#import "TPayView.h"
#import "TAPIUtility.h"

#define button_height 40

@interface TPayView()

@property(nonatomic, retain) UILabel* payLabel;
@property(nonatomic, retain) UILabel* nameLabel;
@property(nonatomic, retain) UILabel* bticketLabel;
@property(nonatomic, retain) UILabel* welcomeLabel;
@property(nonatomic, retain) UIView* whiteView;
@property(nonatomic, retain) UILabel* moneyLabel;
@property(nonatomic, retain) UILabel* remainLabel;
@property(nonatomic, retain) UIImageView* walletImgView;
@property(nonatomic, retain) UIView* lineView;
@property(nonatomic, retain) UILabel* ticketLabel;
@property(nonatomic, retain) UIButton* zhifubaoButton;
@property(nonatomic, retain) UIButton* weichatButton;
@property(nonatomic, retain) UIButton* yuEBaoButton;
@property(nonatomic, retain) UIButton* cashPayButton;
@property(nonatomic, retain) UIButton* entryOkButton;

@property(nonatomic, retain) NSDictionary* payInfo;
@property(nonatomic, retain) TTicketItem* ticketItem;
@property(nonatomic, retain) NSString* ticketMoney;
@property(nonatomic, retain) NSString* yuE;
@property(nonatomic, retain) NSString* needMoney;
@property(nonatomic, retain) NSString* totalMoney;



//余额是否足够支付
@property(nonatomic, assign) BOOL isMoneyRech;

@end
@implementation TPayView

- (id)initWithFrame:(CGRect)frame {
    if (self = [super initWithFrame:frame]) {
        self.backgroundColor = gray_color;
        
        _payLabel = [[UILabel alloc] init];
        _payLabel.backgroundColor = [UIColor clearColor];
        _payLabel.text = @"停车费0元";
        _payLabel.textColor = [UIColor whiteColor];
        _payLabel.textAlignment = NSTextAlignmentCenter;
        _payLabel.font = [UIFont systemFontOfSize:22];
        
        _nameLabel = [[UILabel alloc] init];
        _nameLabel.backgroundColor = [UIColor clearColor];
        _nameLabel.text = @"草房停车场";
        _nameLabel.textColor = [UIColor grayColor];
        _nameLabel.textAlignment = NSTextAlignmentCenter;
        _nameLabel.font = [UIFont systemFontOfSize:13];
        
        _bticketLabel = [[UILabel alloc] init];
        _bticketLabel.backgroundColor = [UIColor clearColor];
        _bticketLabel.textAlignment = NSTextAlignmentCenter;
        _bticketLabel.font = [UIFont systemFontOfSize:13];
        _bticketLabel.text = @"入场时间 10:22";
        _bticketLabel.textColor = [UIColor grayColor];
        
        _welcomeLabel = [[UILabel alloc] init];
        _welcomeLabel.backgroundColor = [UIColor clearColor];
        _welcomeLabel.text = @"欢迎进入!";
        _welcomeLabel.textColor = orange_color;
        _welcomeLabel.font = [UIFont systemFontOfSize:35];
        _welcomeLabel.textAlignment = NSTextAlignmentCenter;
        
        _whiteView = [[UIView alloc] init];
        _whiteView.backgroundColor = [UIColor whiteColor];
        
        _walletImgView = [[UIImageView alloc] initWithImage:[UIImage imageNamed:@"img_wallet.png"]];
        
        _remainLabel = [[UILabel alloc] init];
        _remainLabel.backgroundColor = [UIColor clearColor];
        _remainLabel.textAlignment = NSTextAlignmentLeft;
        _remainLabel.font = [UIFont boldSystemFontOfSize:15];
        _remainLabel.text = @"您还需支付";
        _remainLabel.textColor = [UIColor grayColor];
        
        _moneyLabel = [[UILabel alloc] init];
        _moneyLabel.backgroundColor = [UIColor clearColor];
        _moneyLabel.textAlignment = NSTextAlignmentRight;
        _moneyLabel.font = [UIFont boldSystemFontOfSize:30];
        _moneyLabel.text = @"0.00";
        
        _lineView = [[UIView alloc] init];
        _lineView.backgroundColor = light_white_color;
        
        _ticketLabel = [[UILabel alloc] init];
        _ticketLabel.backgroundColor = [UIColor clearColor];
        _ticketLabel.textAlignment = NSTextAlignmentCenter;
        _ticketLabel.font = [UIFont systemFontOfSize:15];
        _ticketLabel.text = @"没有可用的停车券";
        _ticketLabel.textColor = [UIColor grayColor];
        _ticketLabel.adjustsFontSizeToFitWidth = YES;
        
        [_whiteView addSubview:_walletImgView];
        [_whiteView addSubview:_remainLabel];
        [_whiteView addSubview:_moneyLabel];
        [_whiteView addSubview:_lineView];
        [_whiteView addSubview:_ticketLabel];
        
        _yuEBaoButton = [UIButton buttonWithType:UIButtonTypeCustom];
        [_yuEBaoButton setTitle:@"停车宝支付" forState:UIControlStateNormal];
        [_yuEBaoButton setTitleColor:[UIColor whiteColor] forState:UIControlStateNormal];
        _yuEBaoButton.titleLabel.font = [UIFont systemFontOfSize:15];
//        [_yuEBaoButton setTitleEdgeInsets:UIEdgeInsetsMake(0, 60, 0, 100)];
        [_yuEBaoButton setImage:[UIImage imageNamed:@"img_tingchebao.png"] forState:UIControlStateNormal];
        [_yuEBaoButton setImageEdgeInsets:UIEdgeInsetsMake(7, self.width/2 - 55, 7, self.width/2 + 5)];
        [_yuEBaoButton setBackgroundImage:[TAPIUtility imageWithColor:green_color] forState:UIControlStateNormal];
        [_yuEBaoButton addTarget:self action:@selector(buttonTouched:) forControlEvents:UIControlEventTouchUpInside];
        
        _zhifubaoButton = [UIButton buttonWithType:UIButtonTypeCustom];
        [_zhifubaoButton setTitle:@"支付宝支付" forState:UIControlStateNormal];
        [_zhifubaoButton setTitleColor:[UIColor whiteColor] forState:UIControlStateNormal];
        _zhifubaoButton.titleLabel.font = [UIFont systemFontOfSize:15];
        [_zhifubaoButton setImage:[UIImage imageNamed:@"zhifubao.png"] forState:UIControlStateNormal];
        [_zhifubaoButton setImageEdgeInsets:UIEdgeInsetsMake(10, self.width/2 - 50, 10, self.width/2 + 20)];
        [_zhifubaoButton setBackgroundImage:[TAPIUtility imageWithColor:green_color] forState:UIControlStateNormal];
        [_zhifubaoButton addTarget:self action:@selector(buttonTouched:) forControlEvents:UIControlEventTouchUpInside];
        
        _weichatButton = [UIButton buttonWithType:UIButtonTypeCustom];
        [_weichatButton setTitle:@"微信支付" forState:UIControlStateNormal];
        [_weichatButton setTitleColor:[UIColor whiteColor] forState:UIControlStateNormal];
        _weichatButton.titleLabel.font = [UIFont systemFontOfSize:15];
        [_weichatButton setImage:[UIImage imageNamed:@"weichat.png"] forState:UIControlStateNormal];
        [_weichatButton setImageEdgeInsets:UIEdgeInsetsMake(10, self.width/4 - 46, 10, self.width/4 + 10)];
        [_weichatButton setBackgroundImage:[TAPIUtility imageWithColor:RGBCOLOR(67, 80, 109)] forState:UIControlStateNormal];
        [_weichatButton addTarget:self action:@selector(buttonTouched:) forControlEvents:UIControlEventTouchUpInside];
        
        _cashPayButton = [UIButton buttonWithType:UIButtonTypeCustom];
        [_cashPayButton setTitle:@"付现金" forState:UIControlStateNormal];
        [_cashPayButton setTitleColor:[UIColor grayColor] forState:UIControlStateNormal];
        _cashPayButton.titleLabel.font = [UIFont systemFontOfSize:15];
        [_cashPayButton setImage:[UIImage imageNamed:@"rmb_gray.png"] forState:UIControlStateNormal];
        [_cashPayButton setImageEdgeInsets:UIEdgeInsetsMake(10, self.width/4 - 25, 10, self.width/4 + 5)];
        _cashPayButton.layer.borderColor = RGBCOLOR(67, 80, 109).CGColor;
        _cashPayButton.layer.borderWidth = 3;
        [_cashPayButton addTarget:self action:@selector(buttonTouched:) forControlEvents:UIControlEventTouchUpInside];
        
        _entryOkButton = [UIButton buttonWithType:UIButtonTypeCustom];
        [_entryOkButton setTitle:@"确认" forState:UIControlStateNormal];
        [_entryOkButton setTitleColor:[UIColor whiteColor] forState:UIControlStateNormal];
        _entryOkButton.titleLabel.font = [UIFont systemFontOfSize:15];
        [_entryOkButton setBackgroundImage:[TAPIUtility imageWithColor:green_color] forState:UIControlStateNormal];
        [_entryOkButton addTarget:self action:@selector(buttonTouched:) forControlEvents:UIControlEventTouchUpInside];
        
        [self addSubview:_payLabel];
        [self addSubview:_nameLabel];
        [self addSubview:_bticketLabel];
        [self addSubview:_welcomeLabel];
        [self addSubview:_whiteView];
        [self addSubview:_yuEBaoButton];
        [self addSubview:_zhifubaoButton];
        [self addSubview:_weichatButton];
        [self addSubview:_cashPayButton];
        [self addSubview:_entryOkButton];
    }
    return self;
}

- (void)layoutSubviews {
    [super layoutSubviews];
    
    _payLabel.frame = CGRectMake(0, 0, self.width, 30);
    _nameLabel.frame = CGRectMake(0, _payLabel.bottom + 4, self.width, 20);
    _bticketLabel.frame = CGRectMake(0, _nameLabel.bottom, self.width, 20);
    
    _whiteView.frame = CGRectMake((self.width - 280)/2, _bticketLabel.bottom + 20, 280, 80);
    _walletImgView.frame = CGRectMake(10, 20, 40, 40);
    _remainLabel.frame = CGRectMake(60, 7, 80, 30);
    _moneyLabel.frame = CGRectMake(_remainLabel.right, 7, 120, 30);
    _lineView.frame = CGRectMake(_remainLabel.left + 2, _remainLabel.bottom + 3, 200, 1);
    _ticketLabel.frame = CGRectMake(_walletImgView.right, _lineView.bottom + 3, 220, 20);
    
    _welcomeLabel.frame = _whiteView.frame;
    
    _yuEBaoButton.frame = CGRectMake(0, _whiteView.bottom + 20, self.width, button_height);
    _zhifubaoButton.frame = _yuEBaoButton.frame;
    _weichatButton.frame = CGRectMake(self.width/2 + 1, _yuEBaoButton.bottom + 1, self.width/2 - 1, button_height);
    _entryOkButton.frame = _yuEBaoButton.frame;
    if (_payMessageMode == PayMessageModeEntry) {
        _welcomeLabel.hidden = NO;
        _whiteView.hidden = YES;
        
        _entryOkButton.hidden = NO;
        _yuEBaoButton.hidden = _cashPayButton.hidden = _zhifubaoButton.hidden = _weichatButton.hidden = _whiteView.hidden = YES;
    } else {
        _welcomeLabel.hidden = YES;
        _whiteView.hidden = NO;
        if (!_isMoneyRech) {
            [_cashPayButton setImageEdgeInsets:UIEdgeInsetsMake(10, self.width/4 - 25, 10, self.width/4 + 5)];
            _cashPayButton.frame = CGRectMake(0, _yuEBaoButton.bottom + 1, self.width/2 - 1, button_height);
            _yuEBaoButton.hidden = _entryOkButton.hidden = YES;
            _zhifubaoButton.hidden = _weichatButton.hidden = _cashPayButton.hidden = NO;
        } else {
            _cashPayButton.frame = CGRectMake(0, _yuEBaoButton.bottom + 1, self.width, button_height);
            [_cashPayButton setImageEdgeInsets:UIEdgeInsetsMake(10, self.width/2 - 25, 10, self.width/2 + 5)];
            _yuEBaoButton.hidden = _cashPayButton.hidden = NO;
            _zhifubaoButton.hidden = _weichatButton.hidden = _entryOkButton.hidden = YES;
        }
    }
}

- (void)setPayInfo:(NSDictionary *)payInfo item:(TTicketItem*)ticketItem yuE:(NSString*)yuE {
//    yuE = @"0.09";
//    payInfo = @{@"btime" : @"1413859451",
//                @"etime" : @"1413859539",
//                @"orderid" : @"180224",
//                @"parkname" : @"划房停车场",
//                @"state" : @"0",
//                @"total" : @"1.50"};
    //            @"collectorId" : @"32424"//如果是从收费员那里跳过来的,只有这种情况是orderId = 0
    _payMessageMode = [[payInfo objectForKey:@"state"] isEqualToString:@"0"] ? PayMessageModeEntry : PayMessageModeExit;
    _totalMoney = [payInfo objectForKey:@"total"];
    _payInfo = payInfo;
    _ticketItem = ticketItem;
    _yuE =yuE;
    _ticketMoney = ticketItem ? ticketItem.money : @"0";
    _isMoneyRech = [[_payInfo objectForKey:@"total"] floatValue] > [_ticketMoney floatValue] + [_yuE floatValue] ? NO : YES;
    if (_isMoneyRech)
        _needMoney = [NSString stringWithFormat:@"%.2f", [[payInfo objectForKey:@"total"] floatValue] - [_ticketMoney floatValue] >=0 ? [[payInfo objectForKey:@"total"] floatValue] - [_ticketMoney floatValue] : 0.00];
    else
        _needMoney = [NSString stringWithFormat:@"%.2f", [[payInfo objectForKey:@"total"] floatValue] - [_ticketMoney floatValue] - [_yuE floatValue] >=0 ? [[payInfo objectForKey:@"total"] floatValue] - [_ticketMoney floatValue] - [_yuE floatValue] : 0.00];
    
    
    
    NSString* pay = [NSString stringWithFormat:@"停车费%@元", [payInfo objectForKey:@"total"]];
    NSMutableAttributedString* payAttribute = [[NSMutableAttributedString alloc] initWithString:pay];
    [payAttribute addAttributes:@{NSForegroundColorAttributeName : orange_color} range:NSMakeRange(3, [[payInfo objectForKey:@"total"] length])];
    _payLabel.attributedText = payAttribute;
    
    _nameLabel.text = [payInfo objectForKey:@"parkname"];
    
    NSDate* date = [NSDate dateWithTimeIntervalSince1970:[[payInfo objectForKey:@"btime"] integerValue]];
    NSDateFormatter* formatter = [[NSDateFormatter alloc] init];
    [formatter setDateFormat:@"yyyy-MM-dd HH:mm:ss"];
    
    NSString* btime = nil;
    if ([[payInfo objectForKey:@"orderid"] isEqualToString:@"0"])//从收费员跳过来的情况
        btime = [NSString stringWithFormat:@"付款时间: %@", [formatter stringFromDate:date]];
    else
        btime = [NSString stringWithFormat:@"入场时间: %@", [formatter stringFromDate:date]];
    NSMutableAttributedString* btimeAttribute = [[NSMutableAttributedString alloc] initWithString:btime];
    [btimeAttribute addAttributes:@{NSForegroundColorAttributeName : [UIColor grayColor]} range:NSMakeRange(0, 6)];
    [btimeAttribute addAttributes:@{NSForegroundColorAttributeName : green_color} range:NSMakeRange(6, btime.length - 6)];
    _bticketLabel.attributedText = btimeAttribute;
    
    NSString* money = [NSString stringWithFormat:@"¥%@", _needMoney];
    NSMutableAttributedString* moneyAttribute = [[NSMutableAttributedString alloc] initWithString:money];
    [moneyAttribute addAttributes:@{NSForegroundColorAttributeName : [UIColor redColor]} range:NSMakeRange(0, 1)];
//    [moneyAttribute addAttributes:@{NSFontAttributeName : [UIFont systemFontOfSize:22]} range:NSMakeRange(1, money.length - 1)];
    _moneyLabel.attributedText = moneyAttribute;
    if (_ticketItem) {
        if (_isMoneyRech) {
            NSString* ticket = [NSString stringWithFormat:@"已使用 停车券%.2f元", [_ticketMoney floatValue]];
            NSMutableAttributedString* ticketAttribute = [[NSMutableAttributedString alloc] initWithString:ticket];
                [ticketAttribute addAttributes:@{NSForegroundColorAttributeName : orange_color} range:NSMakeRange(7, [[NSString stringWithFormat:@"%.2f", [_ticketMoney floatValue]] length])];
            _ticketLabel.attributedText = ticketAttribute;
        } else {
            NSString* ticket = [NSString stringWithFormat:@"已使用 停车券%.2f元 停车宝余额%.2f元", [_ticketMoney floatValue], [_yuE floatValue]];
            NSRange range = [ticket rangeOfString:@"余额"];
            NSMutableAttributedString* ticketAttribute = [[NSMutableAttributedString alloc] initWithString:ticket];
            [ticketAttribute addAttributes:@{NSForegroundColorAttributeName : orange_color} range:NSMakeRange(7, [[NSString stringWithFormat:@"%.2f", [_ticketMoney floatValue]] length])];
            [ticketAttribute addAttributes:@{NSForegroundColorAttributeName : orange_color} range:NSMakeRange(range.location + range.length, [[NSString stringWithFormat:@"%.2f", [_yuE floatValue]] length])];
            _ticketLabel.attributedText = ticketAttribute;
        }
    } else {
        if (_isMoneyRech)
            _ticketLabel.text = @"没有可用的停车券";
        else {
            NSString* ticket = [NSString stringWithFormat:@"没有可用的停车券, 停车宝余额%.2f元", [_yuE floatValue]];
            NSRange range = [ticket rangeOfString:@"余额"];
            NSMutableAttributedString* ticketAttribute = [[NSMutableAttributedString alloc] initWithString:ticket];
            [ticketAttribute addAttributes:@{NSForegroundColorAttributeName : orange_color} range:NSMakeRange(range.location + range.length, [[NSString stringWithFormat:@"%.2f", [_yuE floatValue]] length])];
            _ticketLabel.attributedText = ticketAttribute;
        }
    }
    [self layoutSubviews];
}

- (void)buttonTouched:(UIButton*)button {
    if (_delegate && [_delegate respondsToSelector:@selector(payButtonTouched:orderId:money:ticketId:total:collectorId:)]) {
        NSString* ticketId = _ticketItem ? _ticketItem.ticketId : @"-1";
        [_delegate payButtonTouched:button orderId:[_payInfo objectForKey:@"orderid"] money:_needMoney ticketId:ticketId total:_totalMoney collectorId:[_payInfo objectForKey:@"collectorId"]];
    }
}
/*
// Only override drawRect: if you perform custom drawing.
// An empty implementation adversely affects performance during animation.
- (void)drawRect:(CGRect)rect {
    // Drawing code
}
*/

@end
