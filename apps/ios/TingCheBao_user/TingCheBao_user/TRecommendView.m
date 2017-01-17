//
//  TRecommend.m
//  TingCheBao_user
//
//  Created by apple on 14-8-22.
//  Copyright (c) 2014年 zhenLaiDian. All rights reserved.
//

#import "TRecommendView.h"

#define padding 10
#define label_height 25
#define deep_white_color [UIColor whiteColor]
#define light_white_color [[UIColor whiteColor] colorWithAlphaComponent:0.8]

@interface TRecommendView()

@property(nonatomic, retain) UILabel* parkLabel;
@property(nonatomic, retain) UILabel* freeLabel;
@property(nonatomic, retain) UILabel* freeNumLabel;
@property(nonatomic, retain) UILabel* unitLabel;
@property(nonatomic, retain) UILabel* priceLabel;
@property(nonatomic, retain) UILabel* priceNumLabel;

@end
@implementation TRecommendView

- (id)initWithFrame:(CGRect)frame
{
    self = [super initWithFrame:frame];
    if (self) {
        // Initialization code
        self.backgroundColor = gray_color;
        self.alpha = 0.9;
        
        _parkLabel = [[UILabel alloc] init];
        _parkLabel.text = @"";
        _parkLabel.textColor = deep_white_color;
        
        _freeLabel = [[UILabel alloc] init];
        _freeLabel.text = @"空闲车位";
        _freeLabel.textColor = light_white_color;
        _freeLabel.font = [UIFont systemFontOfSize:15];
        
        _freeNumLabel = [[UILabel alloc] init];
        _freeNumLabel.text = @"0";
        _freeNumLabel.textColor = deep_white_color;
        _freeNumLabel.textAlignment = NSTextAlignmentCenter;
        
        _unitLabel = [[UILabel alloc] init];
        _unitLabel.text = @"个";
        _unitLabel.textColor = light_white_color;
        
        _priceLabel = [[UILabel alloc] init];
        _priceLabel.text = @"价格";
        _priceLabel.textColor = light_white_color;
        _priceLabel.font = [UIFont systemFontOfSize:15];
        
        _priceNumLabel = [[UILabel alloc] init];
        _priceNumLabel.text = @"0.0元/0分钟";
        _priceNumLabel.textColor = deep_white_color;
        
        [self addSubview:_parkLabel];
        [self addSubview:_freeLabel];
        [self addSubview:_freeNumLabel];
        [self addSubview:_unitLabel];
        [self addSubview:_priceLabel];
        [self addSubview:_priceNumLabel];
    }
    return self;
}

- (void)layoutSubviews {
    [super layoutSubviews];
    _parkLabel.frame = CGRectMake(padding, 0, self.width, label_height);
    
    _freeLabel.frame = CGRectMake(padding, label_height, 70, label_height);
    _freeNumLabel.frame = CGRectMake(_freeLabel.right + 4, label_height, 40, label_height);
    CGSize size = [_freeNumLabel.text sizeWithAttributes:@{NSFontAttributeName: _freeNumLabel.font}];
    _freeNumLabel.width = size.width;
    
    _unitLabel.frame = CGRectMake(_freeNumLabel.right + 4, label_height, 40, label_height);
    _priceLabel.frame = CGRectMake(160, label_height, 32, label_height);
    _priceNumLabel.frame = CGRectMake(_priceLabel.right + 4, label_height, 130, label_height);
}

- (void)setItem:(TRecommendItem *)item {
    _item = item;
    if (!item) {
        _parkLabel.text = @"暂无推荐信息!";
        _freeNumLabel.text = @"0";
        _priceNumLabel.text = @"0元/0分钟";
    } else {
        _parkLabel.text = [NSString stringWithFormat:@"%@%@", item.isRecommend ? @"推荐:" : @"", item.name];
        _freeNumLabel.text = item.freeNum;
        _priceNumLabel.text = item.price;
        if ([_priceNumLabel.text isEqualToString:@"0"])
            _priceNumLabel.text = @"暂无价格信息";
        [self layoutSubviews];
    }
}

/*
// Only override drawRect: if you perform custom drawing.
// An empty implementation adversely affects performance during animation.
- (void)drawRect:(CGRect)rect
{
    // Drawing code
}
*/

@end
