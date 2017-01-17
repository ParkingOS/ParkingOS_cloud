//
//  TAccountDetailCell.m
//  TingCheBao_user
//
//  Created by apple on 14-9-12.
//  Copyright (c) 2014年 zhenLaiDian. All rights reserved.
//

#import "TAccountDetailCell.h"

@interface TAccountDetailCell()

@property(nonatomic, retain) UIView* centerView;
@property(nonatomic, retain) UILabel* nameLabel;
@property(nonatomic, retain) UILabel* moneyLabel;
@property(nonatomic, retain) UILabel* timeLabel;
@property(nonatomic, retain) UILabel* typeLabel;

@property(nonatomic, retain) NSArray* typeOptions;

@end

@implementation TAccountDetailCell

- (id)initWithStyle:(UITableViewCellStyle)style reuseIdentifier:(NSString *)reuseIdentifier
{
    self = [super initWithStyle:style reuseIdentifier:reuseIdentifier];
    if (self) {
        // Initialization code
        self.backgroundColor = [UIColor clearColor];
        
        _centerView = [[UIView alloc] init];
        _centerView.backgroundColor = [UIColor whiteColor];
        _centerView.layer.borderColor = bg_line_color.CGColor;
        _centerView.layer.borderWidth = 1;
        
        _nameLabel = [[UILabel alloc] init];
        _nameLabel.backgroundColor = [UIColor clearColor];
        
        _moneyLabel = [[UILabel alloc] init];
        _moneyLabel.backgroundColor = [UIColor clearColor];
        _moneyLabel.textColor = [UIColor redColor];
        _moneyLabel.textAlignment = NSTextAlignmentRight;
        
        _timeLabel = [[UILabel alloc] init];
        _timeLabel.backgroundColor = [UIColor clearColor];
        _timeLabel.textColor = [UIColor grayColor];
        _timeLabel.font = [UIFont systemFontOfSize:15];
        
        _typeLabel = [[UILabel alloc] init];
        _typeLabel.backgroundColor = [UIColor clearColor];
        _typeLabel.textColor = [UIColor grayColor];
        _typeLabel.font = [UIFont systemFontOfSize:15];
        _typeLabel.textAlignment = NSTextAlignmentRight;
        
        [self.centerView addSubview:_nameLabel];
        [self.centerView addSubview:_moneyLabel];
        [self.centerView addSubview:_timeLabel];
        [self.centerView addSubview:_typeLabel];
        
        [self addSubview:_centerView];
    }
    return self;
}

- (void)layoutSubviews {
    [super layoutSubviews];
    
    _centerView.frame = CGRectMake(0, 10, self.width, 70);
    _nameLabel.frame = CGRectMake(5, 5, self.width - 80, 30);
    _moneyLabel.frame = CGRectMake(_nameLabel.right, 5, 70, 30);
    _timeLabel.frame = CGRectMake(5, _nameLabel.bottom, 200, 30);
    _typeLabel.frame = CGRectMake(self.width - 100 - 5, _timeLabel.top, 100, 30);
}

- (void)setItem:(TAccountDetailItem *)item {
    _item = item;
    _nameLabel.text = item.remark;
    if ([item.type isEqualToString:@"0"]) {
        _moneyLabel.text = [NSString stringWithFormat:@"+%@", item.amount];
        _moneyLabel.textColor = green_color;
    } else {
        _moneyLabel.text = [NSString stringWithFormat:@"-%@", item.amount];
        _moneyLabel.textColor = [UIColor redColor];
    }
    
    _timeLabel.text = item.create_time;
    _typeLabel.text = item.pay_name;
}

@end
