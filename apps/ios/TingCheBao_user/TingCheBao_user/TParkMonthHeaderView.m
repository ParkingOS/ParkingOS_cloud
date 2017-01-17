//
//  TParkMonthHeaderView.m
//  TingCheBao_user
//
//  Created by apple on 14-9-22.
//  Copyright (c) 2014年 zhenLaiDian. All rights reserved.
//

#import "TParkMonthHeaderView.h"

@interface TParkMonthHeaderView()

@property(nonatomic, retain) UIView* lineView;

@end
@implementation TParkMonthHeaderView

- (id)initWithFrame:(CGRect)frame {
    if (self = [super initWithFrame:frame]) {
        self.backgroundColor = [UIColor whiteColor];
        _nameLabel = [[UILabel alloc] init];
        _nameLabel.backgroundColor = [UIColor clearColor];

        _distanceLabel = [[UILabel alloc] init];
        _distanceLabel.backgroundColor = [UIColor clearColor];
        
        _lineView = [[UIView alloc] init];
        _lineView.backgroundColor = light_white_color;
        
        [self addSubview:_nameLabel];
        [self addSubview:_distanceLabel];
        [self addSubview:_lineView];
    }
    return self;
}

- (void)layoutSubviews {
    [super layoutSubviews];
    _nameLabel.frame = CGRectMake(4, 0, 230, 40);
    _distanceLabel.frame = CGRectMake(self.width - 75, 0, 60, 40);
    _lineView.frame = CGRectMake(0, 39.5, self.width , 0.5);
}

@end
