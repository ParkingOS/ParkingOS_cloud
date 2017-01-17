//
//  TParkMonthDetailTopView.m
//  TingCheBao_user
//
//  Created by apple on 14-10-10.
//  Copyright (c) 2014年 zhenLaiDian. All rights reserved.
//

#import "TParkMonthDetailTopView.h"
#import "UIImageView+WebCache.h"

@implementation TParkMonthDetailTopView

- (id)initWithFrame:(CGRect)frame {
    if (self = [super initWithFrame:frame]) {
        _infoImgView = [[UIImageView alloc] initWithFrame:CGRectMake(0, 0, 60, 60)];
        
        _imgView = [[UIImageView alloc] initWithImage:[UIImage imageNamed:@"img_monthlypay_default.png"]];
        
        _numberLabel = [[UILabel alloc] init];
        _numberLabel.backgroundColor = [[UIColor blackColor] colorWithAlphaComponent:0.7];
        _numberLabel.text = @"剩余:";
        _numberLabel.textColor = [UIColor whiteColor];
        
        _monthLabel = [[UILabel alloc] init];
        _monthLabel.backgroundColor = [[UIColor blackColor] colorWithAlphaComponent:0.7];
        _monthLabel.text = @"";
        _monthLabel.textColor = [UIColor whiteColor];
        
        [self addSubview:_infoImgView];
        [self addSubview:_imgView];
        [self addSubview:_numberLabel];
        [self addSubview:_monthLabel];
    }
    return self;
}

- (void)layoutSubviews {
    [super layoutSubviews];
    
    _infoImgView.frame = CGRectMake(0, 0, 60, 60);
    _imgView.frame = CGRectMake(0, 0, self.width, self.height);
    _numberLabel.frame = CGRectMake(self.width - 70, 2, 65, 30);
    _monthLabel.frame = CGRectMake(0, self.height - 30, self.width, 30);
}

- (void)setItem:(TParkMonthItem *)item {
    _item = item;
    
    if ([_item.type isEqualToString:@"0"]) {
        _infoImgView.image = [UIImage imageNamed:@"img_monthlypay_full.png"];
    } else if ([_item.type isEqualToString:@"1"]) {
        _infoImgView.image = [UIImage imageNamed:@"img_monthlypay_night.png"];
    } else if ([_item.type isEqualToString:@"2"]) {
        _infoImgView.image = [UIImage imageNamed:@"img_monthlypay_day.png"];
    }
    
    if ([_item.photoUrl count] == 0)
        _imgView.image = [UIImage imageNamed:@"img_monthlypay_default.png"];
    else
        [_imgView sd_setImageWithURL:[NSURL URLWithString:[NSString stringWithFormat:@"http://s.tingchebao.com/zld/%@",[item.photoUrl objectAtIndex:0]]] placeholderImage:[UIImage imageNamed:@"img_monthlypay_default.png"]];
    _numberLabel.text = [NSString stringWithFormat:@"(剩余:%@)", item.number];
}
/*
// Only override drawRect: if you perform custom drawing.
// An empty implementation adversely affects performance during animation.
- (void)drawRect:(CGRect)rect {
    // Drawing code
}
*/

@end
