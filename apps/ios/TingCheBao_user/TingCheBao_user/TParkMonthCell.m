//
//  TParkMonthCell.m
//  TingCheBao_user
//
//  Created by apple on 14-9-13.
//  Copyright (c) 2014年 zhenLaiDian. All rights reserved.
//

#import "TParkMonthCell.h"
#import "TAPIUtility.h"
#import "UIImageView+WebCache.h"

@interface TParkMonthCell()

@property(nonatomic, retain) UIImageView* imgView;
@property(nonatomic, retain) UIImageView* typeImgView;
@property(nonatomic, retain) UILabel* nameLabel;//名称
@property(nonatomic, retain) UILabel* numberLabel;//剩余数量
@property(nonatomic, retain) UILabel* moneyLabel;//钱
@property(nonatomic, retain) UILabel* unitLabel;//元
@property(nonatomic, retain) UILabel* oldMoneyLabel;//原来的钱
@property(nonatomic, retain) UILabel* timeLabel;//有效时段
@property(nonatomic, retain) UIButton* buyButton;
@property(nonatomic, retain) UILabel* detailLabel;
@property(nonatomic, retain) UIImageView* detailImgView;

@end
@implementation TParkMonthCell

- (id)initWithStyle:(UITableViewCellStyle)style reuseIdentifier:(NSString *)reuseIdentifier
{
    self = [super initWithStyle:style reuseIdentifier:reuseIdentifier];
    if (self) {
        // Initialization code
        _imgView = [[UIImageView alloc] init];
        
        _typeImgView = [[UIImageView alloc] init];
        _typeImgView.backgroundColor = [UIColor clearColor];
        
        _timeLabel = [[UILabel alloc] init];
        _timeLabel.backgroundColor = [UIColor blackColor];
        _timeLabel.alpha = 0.8;
        _timeLabel.text = @"00:00-24:00";
        _timeLabel.font = [UIFont systemFontOfSize:15];
        _timeLabel.textColor = [UIColor whiteColor];
        _timeLabel.textAlignment = NSTextAlignmentCenter;
        
        _nameLabel = [[UILabel alloc] init];
        _nameLabel.backgroundColor = [UIColor clearColor];
        _nameLabel.font = [UIFont systemFontOfSize:17];
        
        _detailLabel = [[UILabel alloc] init];
        _detailLabel.backgroundColor = [UIColor clearColor];
        _detailLabel.text = @"详情";
        _detailLabel.font = [UIFont systemFontOfSize:15];
        
        _detailImgView = [[UIImageView alloc] initWithImage:[UIImage imageNamed:@"ic_arrow_grey.png"]];
        
        _numberLabel = [[UILabel alloc] init];
        _numberLabel.backgroundColor = [UIColor clearColor];
        _numberLabel.text = @"(剩余:)";
        _numberLabel.font = [UIFont systemFontOfSize:13];
        _numberLabel.textColor = [UIColor grayColor];
        
        _moneyLabel = [[UILabel alloc] init];
        _moneyLabel.backgroundColor = [UIColor clearColor];
        _moneyLabel.textColor = green_color;
        _moneyLabel.font = [UIFont systemFontOfSize:24];
        _moneyLabel.textAlignment = NSTextAlignmentRight;
        
        _unitLabel = [[UILabel alloc] init];
        _unitLabel.backgroundColor = [UIColor clearColor];
        _unitLabel.font = [UIFont systemFontOfSize:13];
        _unitLabel.text = @"元";
        
        _oldMoneyLabel = [[UILabel alloc] init];
        _oldMoneyLabel.backgroundColor = [UIColor clearColor];
        _oldMoneyLabel.font = [UIFont systemFontOfSize:13];
        _oldMoneyLabel.textColor = [UIColor grayColor];
        
        _buyButton = [UIButton buttonWithType:UIButtonTypeCustom];
        [_buyButton setTitle:@"抢购" forState:UIControlStateNormal];
        [_buyButton setTitleColor:[UIColor whiteColor] forState:UIControlStateNormal];
        [_buyButton setBackgroundImage:[TAPIUtility imageWithColor:orange_color] forState:UIControlStateNormal];
        [_buyButton addTarget:self action:@selector(clickBuyButton) forControlEvents:UIControlEventTouchUpInside];
        _buyButton.layer.cornerRadius = 5;
        _buyButton.clipsToBounds = YES;
        
        [self addSubview:_imgView];
        [self addSubview:_typeImgView];
        [self addSubview:_timeLabel];
        [self addSubview:_nameLabel];
        [self addSubview:_detailLabel];
        [self addSubview:_detailImgView];
        [self addSubview:_moneyLabel];
        [self addSubview:_unitLabel];
        [self addSubview:_numberLabel];
        [self addSubview:_oldMoneyLabel];
        [self addSubview:_buyButton];
    }
    return self;
}

- (void)layoutSubviews {
    [super layoutSubviews];
    
    _imgView.frame = CGRectMake(2, 14, 98, self.height - 2*14);
    _typeImgView.frame = CGRectMake(_imgView.left, _imgView.top, 60, 60);
    _timeLabel.frame = CGRectMake(_imgView.left, _imgView.bottom - 20, _imgView.width, 20);
    _nameLabel.frame = CGRectMake(_imgView.right + 2, _imgView.top, 220, 20);
    _detailLabel.frame = CGRectMake(self.width - 55, _nameLabel.bottom + 8, 34, 20);
    _detailImgView.frame = CGRectMake(_detailLabel.right, _detailLabel.top + 2, 15, 15);
    
    CGSize size = T_TEXTSIZE(_moneyLabel.text, _moneyLabel.font);
    _moneyLabel.frame = CGRectMake(_imgView.right + 4, 54, size.width, 30);
    _unitLabel.frame = CGRectMake(_moneyLabel.right, 57, 16, 26);
    _numberLabel.frame = CGRectMake(_unitLabel.right, 57, 80, 26);
    
    _oldMoneyLabel.frame = CGRectMake(_imgView.right + 4, _moneyLabel.bottom, 100, 30);
    
    _buyButton.frame = CGRectMake(self.width - 72, self.height - 34 - 14, 67, 34);
}

#pragma mark private

- (void)setItem:(TParkMonthItem *)item {
    _item = item;
    if ([_item.photoUrl count] == 0)
        _imgView.image = [UIImage imageNamed:@"img_monthlypay_default.png"];
     else
        [_imgView sd_setImageWithURL:[NSURL URLWithString:[TAPIUtility getNetworkWithUrl:[item.photoUrl objectAtIndex:0]]] placeholderImage:[UIImage imageNamed:@"img_monthlypay_default.png"]];
    if ([_item.type isEqualToString:@"0"]) {
        _typeImgView.image = [UIImage imageNamed:@"img_monthlypay_full.png"];
    } else if ([_item.type isEqualToString:@"1"]) {
        _typeImgView.image = [UIImage imageNamed:@"img_monthlypay_night.png"];
    } else if ([_item.type isEqualToString:@"2"]) {
        _typeImgView.image = [UIImage imageNamed:@"img_monthlypay_day.png"];
    }
    _timeLabel.text = _item.limittime;
    _nameLabel.text = _item.name;
    _numberLabel.text = [NSString stringWithFormat:@"(剩余:%@)", _item.number];
    _moneyLabel.text = _item.price;
    
    NSAttributedString* attrString = [[NSAttributedString alloc] initWithString:[NSString stringWithFormat:@"原价%@", _item.price0] attributes:@{NSStrikethroughStyleAttributeName : @(NSUnderlineStyleSingle), NSForegroundColorAttributeName : [UIColor grayColor]}];
    _oldMoneyLabel.attributedText = attrString;
         
    if ([_item.isbuy isEqualToString:@"0"]) {
        [_buyButton setBackgroundImage:[TAPIUtility imageWithColor:orange_color] forState:UIControlStateNormal];
        _buyButton.enabled = YES;
    } else {
        [_buyButton setBackgroundImage:[TAPIUtility imageWithColor:[UIColor grayColor]] forState:UIControlStateNormal];
        _buyButton.enabled = NO;
    }
    [self layoutSubviews];
}

- (void)clickBuyButton {
    if (_delegate && [_delegate respondsToSelector:@selector(buyButtonTouched:cell:)]) {
        [_delegate buyButtonTouched:_item cell:self];
    }
}

//- (UIEdgeInsets)layoutMargins
//{
//    return UIEdgeInsetsZero;
//}
@end
