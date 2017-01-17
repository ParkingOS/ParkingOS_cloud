//
//  TMyMonthCell.m
//  TingCheBao_user
//
//  Created by apple on 14-9-13.
//  Copyright (c) 2014年 zhenLaiDian. All rights reserved.
//

#import "TMyMonthCell.h"

@interface TMyMonthCell()

@property(nonatomic, retain) UIView* topView;
@property(nonatomic, retain) UILabel* nameLabel;//名称
@property(nonatomic, retain) UILabel* moneyLabel;//钱
@property(nonatomic, retain) UILabel* daysLabel;//剩余天数
@property(nonatomic, retain) UILabel* daysNumLabel;
@property(nonatomic, retain) UILabel* validHourLabel;//有效时段
@property(nonatomic, retain) UILabel* validHourNumLabel;
@property(nonatomic, retain) UILabel* validDayLabel;//有效期限
@property(nonatomic, retain) UILabel* validDayNumLabel;

@end
@implementation TMyMonthCell

- (id)initWithStyle:(UITableViewCellStyle)style reuseIdentifier:(NSString *)reuseIdentifier
{
    self = [super initWithStyle:style reuseIdentifier:reuseIdentifier];
    if (self) {
        // Initialization code
        _topView = [[UIView alloc] init];
        _topView.backgroundColor = light_blue_color;
        
        _nameLabel = [[UILabel alloc] init];
        _nameLabel.backgroundColor = [UIColor clearColor];
        _nameLabel.textColor = [UIColor whiteColor];
        
        _moneyLabel = [[UILabel alloc] init];
        _moneyLabel.backgroundColor = [UIColor clearColor];
        _moneyLabel.textColor = [UIColor whiteColor];
        _moneyLabel.font = [UIFont systemFontOfSize:15];
        _moneyLabel.textAlignment = NSTextAlignmentRight;
        
        [_topView addSubview:_nameLabel];
        [_topView addSubview:_moneyLabel];
        
        _daysLabel = [[UILabel alloc] init];
        _daysLabel.backgroundColor = [UIColor clearColor];
        _daysLabel.text = @"剩余天数:";
        _daysLabel.font = [UIFont systemFontOfSize:15];
        
        _daysNumLabel = [[UILabel alloc] init];
        _daysNumLabel.backgroundColor = [UIColor clearColor];
        _daysNumLabel.textColor = orange_color;
        _daysNumLabel.font = [UIFont systemFontOfSize:20];
        
        _validHourLabel = [[UILabel alloc] init];
        _validHourLabel.backgroundColor = [UIColor clearColor];
        _validHourLabel.text = @"有效时段:";
        _validHourLabel.font = [UIFont systemFontOfSize:15];
        
        _validHourNumLabel = [[UILabel alloc] init];
        _validHourNumLabel.backgroundColor = [UIColor clearColor];
        _validHourNumLabel.font = [UIFont systemFontOfSize:15];
        
        _validDayLabel = [[UILabel alloc] init];
        _validDayLabel.backgroundColor = [UIColor clearColor];
        _validDayLabel.text = @"有效期限:";
        _validDayLabel.font = [UIFont systemFontOfSize:15];
        
        _validDayNumLabel = [[UILabel alloc] init];
        _validDayNumLabel.backgroundColor = [UIColor clearColor];
        _validDayNumLabel.font = [UIFont systemFontOfSize:15];
        
        [self addSubview:_topView];
        [self addSubview:_daysLabel];
        [self addSubview:_daysNumLabel];
        [self addSubview:_validHourLabel];
        [self addSubview:_validHourNumLabel];
        [self addSubview:_validDayLabel];
        [self addSubview:_validDayNumLabel];
    }
    return self;
}

- (void)layoutSubviews {
    [super layoutSubviews];
    _topView.frame = CGRectMake(0, 0, self.width, 40);
    _nameLabel.frame = CGRectMake(5, 0, self.width - 80, _topView.height);
    _moneyLabel.frame = CGRectMake(self.width - 75, 0, 66, _topView.height);
    
    _daysLabel.frame = CGRectMake(5, _topView.bottom, 80, 30);
    _daysNumLabel.frame = CGRectMake(_daysLabel.right + 2, _topView.bottom, 200, 30);
    _validHourLabel.frame = CGRectMake(5, _daysLabel.bottom, _daysLabel.width, _daysLabel.height);
    _validHourNumLabel.frame = CGRectMake(_validHourLabel.right + 2, _validHourLabel.top, 200, _daysLabel.height);
    _validDayLabel.frame = CGRectMake(5, _validHourLabel.bottom, _validHourLabel.width, _daysLabel.height);
    _validDayNumLabel.frame = CGRectMake(_validHourLabel.right + 2, _validHourLabel.bottom, 220, _daysLabel.height);
}

- (void)setItem:(TMyMonthItem *)item {
    _item = item;
    NSString* name = [NSString stringWithFormat:@"%@-%@", _item.parkname, _item.name];
    NSMutableAttributedString* nameAttribute = [[NSMutableAttributedString alloc] initWithString:name];
    [nameAttribute addAttributes:@{NSForegroundColorAttributeName : [UIColor whiteColor]} range:NSMakeRange(0, [_item.parkname length])];
    [nameAttribute addAttributes:@{NSFontAttributeName : [UIFont systemFontOfSize:15], NSForegroundColorAttributeName : [UIColor blackColor]} range:NSMakeRange([_item.parkname length] + 1, [_item.name length])];
    _nameLabel.attributedText = nameAttribute;
    _moneyLabel.text = [NSString stringWithFormat:@"¥%@", _item.price];
    _daysNumLabel.text = _item.limitday;
    _validHourNumLabel.text = _item.limittime;
    _validDayNumLabel.text = _item.limitdate;
}


@end
