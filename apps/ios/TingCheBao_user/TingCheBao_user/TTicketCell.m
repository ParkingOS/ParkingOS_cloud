//
//  TTicketCell.m
//  TingCheBao_user
//
//  Created by apple on 14/11/3.
//  Copyright (c) 2014年 zhenLaiDian. All rights reserved.
//

#import "TTicketCell.h"

#define bgImg_width self.width - 10
#define bgImg_height 120
#define border_padding 5

@interface TTicketCell()

@property(nonatomic, retain) UIImageView* bgImgView;
@property(nonatomic, retain) UILabel* moneyLabel;
@property(nonatomic, retain) UILabel* promptLabel;
@property(nonatomic, retain) UILabel* timeLabel;
@property(nonatomic, retain) UIImageView *identifierImageView;

@property(nonatomic, retain) UIImageView *userImageView;
@property(nonatomic, retain) UILabel* userInfo;

@property (nonatomic, retain) UIImageView *onleImageView;
@property (nonatomic, retain) UIImageView *zImageView;
@property (nonatomic, retain) UILabel *zLabel;


@end
@implementation TTicketCell

- (id)initWithStyle:(UITableViewCellStyle)style reuseIdentifier:(NSString *)reuseIdentifier {
    if (self = [super initWithStyle:style reuseIdentifier:reuseIdentifier]) {
        
        self.backgroundColor = [UIColor clearColor];
        
        _bgImgView = [[UIImageView alloc] initWithImage:[UIImage imageNamed:@"park_coupon_current_bg.png"]];
        
        _identifierImageView = [[UIImageView alloc] initWithImage:[UIImage imageNamed:@"park_coupon_curr_used.png"]];
        _identifierImageView.backgroundColor = [UIColor clearColor];
        
        _promptLabel = [[UILabel alloc] init];
        _promptLabel.backgroundColor = [UIColor clearColor];
        _promptLabel.textColor = RGBCOLOR(137, 137, 137);
        _promptLabel.font      = [UIFont boldSystemFontOfSize:18];
        _promptLabel.textAlignment = NSTextAlignmentRight;
        _promptLabel.text = @"停车优惠券";
        
        _moneyLabel = [[UILabel alloc] init];
        _moneyLabel.backgroundColor = [UIColor clearColor];
        _moneyLabel.textColor = [UIColor blackColor];
        _moneyLabel.font = [UIFont boldSystemFontOfSize:38];
        _moneyLabel.textAlignment = NSTextAlignmentRight;
        
        _timeLabel = [[UILabel alloc] init];
        _timeLabel.backgroundColor = [UIColor clearColor];
        _timeLabel.font      = [UIFont boldSystemFontOfSize:13];
        _timeLabel.textAlignment = NSTextAlignmentRight;
        
        _userImageView = [[UIImageView alloc] initWithImage:[UIImage imageNamed:@"park_coupon_cash.png"]];
        
        _userInfo = [[UILabel alloc] init];
        _userInfo.backgroundColor = [UIColor clearColor];
        _userInfo.textAlignment = NSTextAlignmentCenter;
        _userInfo.font = [UIFont systemFontOfSize:22];
        _userInfo.textColor = RGBCOLOR(85, 218, 173);
        _userInfo.adjustsFontSizeToFitWidth = YES;
        
        _onleImageView = [[UIImageView alloc] initWithImage:[UIImage imageNamed:@"park_coupon_his_private"]];
        
        _zLabel = [[UILabel alloc] init];
        _zLabel.backgroundColor = [UIColor clearColor];
        _zLabel.textColor = RGBCOLOR(85, 218, 173);
        _zLabel.font      = [UIFont boldSystemFontOfSize:18];
        _zLabel.textAlignment = NSTextAlignmentLeft;
        
        _zImageView = [[UIImageView alloc] initWithImage:[UIImage imageNamed:@"park_coupon_his_diamond"]];
        
        [self addSubview:_bgImgView];
        [self addSubview:_promptLabel];
        [self addSubview:_moneyLabel];
        [self addSubview:_timeLabel];
        [self addSubview:_identifierImageView];
        [self addSubview:_userImageView];
        [self addSubview:_userInfo];
        
        [self addSubview:_zImageView];
        [self addSubview:_zLabel];
        [self addSubview:_onleImageView];
    }
    return  self;
}

- (void)layoutSubviews {
    [super layoutSubviews];
    _bgImgView.frame = CGRectMake(border_padding, 0, bgImg_width, bgImg_height);
    _promptLabel.frame = CGRectMake(self.width/2 + 20, 30, self.width/2 - 30, 30);
    _moneyLabel.frame = CGRectMake(_bgImgView.left, 30, 100, 50);
    _timeLabel.frame = CGRectMake(self.width/2 - 30,60,self.width/2 + 20,30);
    _identifierImageView.frame = CGRectMake(self.width - 90,30,60,40);
    _onleImageView.frame = CGRectMake(self.width/2 - 30, 40, 50, 50);
    _zImageView.frame = CGRectMake(10, 10, 20, 20);
    _zLabel.frame = CGRectMake(35, 10, 250, 20);
}

- (void)setItem:(TTicketItem *)item {
    _item = item;
    if ([_item.state isEqualToString:@"1"]||[_item.exp isEqualToString:@"0"]) {
        _zImageView.image = [UIImage imageNamed:@"park_coupon_his_diamond.png"];
        _onleImageView.image = [UIImage imageNamed:@"park_coupon_his_private.png"];
        _zLabel.textColor = RGBCOLOR(212, 212, 212);
        _promptLabel.textColor = RGBCOLOR(212, 212, 212);
        _timeLabel.textColor   = RGBCOLOR(212, 212, 212);
    }else{
        _zImageView.image = [UIImage imageNamed:@"park_coupon_curr_diamond.png"];
        _onleImageView.image = [UIImage imageNamed:@"park_coupon_curr_private.png"];
        _zLabel.textColor = RGBCOLOR(85, 218, 173);
        _promptLabel.textColor = RGBCOLOR(137, 137, 137);
        _timeLabel.textColor   = RGBCOLOR(137, 137, 137);
    }
    
    if ([_item.type intValue] == 1) {
        _zLabel.hidden = NO;
        _zImageView.hidden = NO;
        _onleImageView.hidden = NO;
    } else if ([_item.state isEqualToString:@"1"]) {
        _zLabel.hidden = NO;
        _zImageView.hidden = NO;
        _onleImageView.hidden = YES;
    }else{
        _zLabel.hidden = YES;
        _zImageView.hidden = YES;
        _onleImageView.hidden = YES;
    }
   
    
    _moneyLabel.text = [NSString stringWithFormat:@"¥%@", item.money];
    NSMutableAttributedString *att = [[NSMutableAttributedString alloc] initWithString:_moneyLabel.text];
    [att addAttribute:NSFontAttributeName value:[UIFont systemFontOfSize:25] range:NSMakeRange(0, 1)];
    _moneyLabel.attributedText = att;
    
    _timeLabel.text = [NSString stringWithFormat:@"有效期至 %@",[self stringWithTime:item.limitday]];
    
    _zLabel.text  = _item.cname;
    
    if ([_item.state isEqualToString:@"1"]) {
        if (_item.utime && ![_item.utime isEqualToString:@""]) {
            
            _userInfo.text = [NSString stringWithFormat:@"抵扣了%@元", _item.umoney];
            
            NSDictionary *attribute = @{NSFontAttributeName: [UIFont systemFontOfSize:18]};
            
            CGSize size = [_userInfo.text boundingRectWithSize:CGSizeMake(1000, 0) options: NSStringDrawingTruncatesLastVisibleLine | NSStringDrawingUsesLineFragmentOrigin | NSStringDrawingUsesFontLeading attributes:attribute context:nil].size;
            _userInfo.frame = CGRectMake(self.width - 10 - size.width, 85, size.width, 30);
            _userImageView.frame = CGRectMake(_userInfo.left - 23, 92.5, 18, 18);
            
            _userInfo.hidden = NO;
            _userImageView.hidden = NO;
            
            _identifierImageView.hidden = NO;
            _identifierImageView.image  = [UIImage imageNamed:@"park_coupon_curr_used.png"];
        } else {
            _identifierImageView.hidden = NO;
            _identifierImageView.image  = [UIImage imageNamed:@"park_coupon_curr_used.png"];
            _userImageView.hidden = YES;
            _userInfo.hidden = YES;
        }
        
        _bgImgView.image = [UIImage imageNamed:@"park_coupon_history_bg.png"];
    } else if ([_item.exp isEqualToString:@"0"]) {
        _identifierImageView.hidden = NO;
        _identifierImageView.image  = [UIImage imageNamed:@"park_coupon_beoverdue.png"];
        _userImageView.hidden = YES;
        _userInfo.hidden = YES;
        _bgImgView.image = [UIImage imageNamed:@"park_coupon_history_bg.png"];
    } else {
        _identifierImageView.hidden = YES;
        _userImageView.hidden = YES;
        _userInfo.hidden = YES;
        _bgImgView.image = [UIImage imageNamed:@"park_coupon_current_bg.png"];
    }
}

- (NSString*)stringWithTime:(NSString*)time {
    NSDate* date = [NSDate dateWithTimeIntervalSince1970:[time integerValue]];
    NSDateFormatter* formatter = [[NSDateFormatter alloc] init];
    [formatter setDateFormat:@"yyyy-MM-dd"];
    return [formatter stringFromDate:date];
}


- (NSString*)stringWithTime2:(NSString*)time {
    NSDate* date = [NSDate dateWithTimeIntervalSince1970:[time integerValue]];
    NSDateFormatter* formatter = [[NSDateFormatter alloc] init];
    [formatter setDateFormat:@"MM-dd HH:mm"];
    return [formatter stringFromDate:date];
}
@end
