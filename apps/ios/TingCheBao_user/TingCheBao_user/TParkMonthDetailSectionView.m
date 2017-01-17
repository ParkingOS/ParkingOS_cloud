//
//  TParkMonthDetailSectionView.m
//  TingCheBao_user
//
//  Created by apple on 14-10-10.
//  Copyright (c) 2014年 zhenLaiDian. All rights reserved.
//

#import "TParkMonthDetailSectionView.h"
#import "TAPIUtility.h"

@interface TParkMonthDetailSectionView()

@property(nonatomic, retain) UILabel* priceLabel;
@property(nonatomic, retain) UILabel* unitLabel;
@property(nonatomic, retain) UILabel* oldLabel;
@property(nonatomic, retain) UIButton* buyButton;

@end
@implementation TParkMonthDetailSectionView

- (id)initWithFrame:(CGRect)frame {
    if (self = [super initWithFrame:frame]) {
        self.backgroundColor = light_white_color;
        _priceLabel = [[UILabel alloc] init];
        _priceLabel.backgroundColor = [UIColor clearColor];
        _priceLabel.textColor = green_color;
        _priceLabel.font = [UIFont systemFontOfSize:25];
        
        _unitLabel = [[UILabel alloc] init];
        _unitLabel.backgroundColor = [UIColor clearColor];
        _unitLabel.textColor = [UIColor blackColor];
        _unitLabel.text = @"元";
        
        _oldLabel = [[UILabel alloc] init];
        _oldLabel.backgroundColor = [UIColor clearColor];
        _oldLabel.textColor = gray_color;
        _oldLabel.font = [UIFont systemFontOfSize:13];
        
        _buyButton = [UIButton buttonWithType:UIButtonTypeCustom];
        [_buyButton setTitle:@"立即购买" forState:UIControlStateNormal];
        [_buyButton setTitleColor:[UIColor whiteColor] forState:UIControlStateNormal];
        [_buyButton setBackgroundImage:[TAPIUtility imageWithColor:orange_color] forState:UIControlStateNormal];
        _buyButton.clipsToBounds = YES;
        _buyButton.layer.cornerRadius = 5;
        [_buyButton addTarget:self action:@selector(buyButtonTouched) forControlEvents:UIControlEventTouchUpInside];
        
        [self addSubview:_priceLabel];
        [self addSubview:_unitLabel];
        [self addSubview:_buyButton];
        [self addSubview:_oldLabel];
    }
    return self;
}

- (void)layoutSubviews {
    [super layoutSubviews];
    CGSize size = T_TEXTSIZE(_priceLabel.text, _priceLabel.font);
    _priceLabel.frame = CGRectMake(5, 2, size.width, 34);
    _unitLabel.frame = CGRectMake(_priceLabel.right, 0, 15, 40);
    _oldLabel.frame = CGRectMake(_unitLabel.right + 5, 0, 140, 40);
    _buyButton.frame = CGRectMake(self.width - 90, 5, 85, self.height - 5*2);
}

- (void)setItem:(TParkMonthItem *)item {
    _item = item;
    
    _priceLabel.text = item.price;
    
    NSAttributedString* attrString = [[NSAttributedString alloc] initWithString:[NSString stringWithFormat:@"(原价%@元)", _item.price0] attributes:@{NSStrikethroughStyleAttributeName : @(NSUnderlineStyleSingle), NSForegroundColorAttributeName : [UIColor grayColor]}];
    _oldLabel.attributedText = attrString;
    
    if ([_item.isbuy isEqualToString:@"0"]) {
        [_buyButton setBackgroundImage:[TAPIUtility imageWithColor:orange_color] forState:UIControlStateNormal];
        _buyButton.enabled = YES;
    } else {
        [_buyButton setBackgroundImage:[TAPIUtility imageWithColor:[UIColor grayColor]] forState:UIControlStateNormal];
        _buyButton.enabled = NO;
    }
    
    [self layoutSubviews];
}

- (void)buyButtonTouched {
    if (_delegate && [_delegate respondsToSelector:@selector(sectionBuyButtonTouched)]) {
        [_delegate sectionBuyButtonTouched];
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
