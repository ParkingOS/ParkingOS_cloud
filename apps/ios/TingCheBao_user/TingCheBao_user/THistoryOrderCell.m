//
//  THistoryOrderCell.m
//  TingCheBao_user
//
//  Created by apple on 14/10/19.
//  Copyright (c) 2014年 zhenLaiDian. All rights reserved.
//

#import "THistoryOrderCell.h"

#define padding 10

@interface THistoryOrderCell()

@property(nonatomic, retain) UIView* whiteView;
@property(nonatomic, retain) UILabel* dateLabel;
@property(nonatomic, retain) UILabel* parkNameLabel;
@property(nonatomic, retain) UILabel* moneyLabel;
@property(nonatomic, retain) UIImageView* moneyImgView;
@property(nonatomic, retain) UILabel* stateLabel;
@property(nonatomic, retain) UIImageView* arrowImgView;

@end
@implementation THistoryOrderCell

- (id)initWithStyle:(UITableViewCellStyle)style reuseIdentifier:(NSString *)reuseIdentifier {
    if (self = [super initWithStyle:style reuseIdentifier:reuseIdentifier]) {
        
        self.backgroundColor = [UIColor clearColor];
        //leftViwe
        _whiteView= [[UIView alloc] init];
        _whiteView.backgroundColor = [UIColor whiteColor];
        _whiteView.layer.borderColor = bg_line_color.CGColor;
        _whiteView.layer.borderWidth = 1;
        
        _moneyImgView = [[UIImageView alloc] init];
        _moneyImgView.backgroundColor = [UIColor clearColor];
        _moneyImgView.image = [UIImage imageNamed:@"ic_history_item_money.png"];
        
        _moneyLabel = [[UILabel alloc] init];
        _moneyLabel.backgroundColor = [UIColor clearColor];
        _moneyLabel.textColor = [UIColor blackColor];
        _moneyLabel.font = [UIFont systemFontOfSize:22];
        
        _parkNameLabel = [[UILabel alloc] init];
        _parkNameLabel.backgroundColor = [UIColor clearColor];
        _parkNameLabel.textColor = [UIColor lightGrayColor];
        _parkNameLabel.font = [UIFont systemFontOfSize:14];
        
        _dateLabel = [[UILabel alloc] init];
        _dateLabel.backgroundColor = [UIColor clearColor];
        _dateLabel.textColor = [UIColor lightGrayColor];
        _dateLabel.font = [UIFont systemFontOfSize:13];
        _dateLabel.textAlignment = NSTextAlignmentRight;
        
        _stateLabel = [[UILabel alloc] init];
        _stateLabel.backgroundColor = [UIColor clearColor];
        _stateLabel.textColor = green_color;
        _stateLabel.text = @"已支付";
        _stateLabel.font = [UIFont systemFontOfSize:13];
        
        _arrowImgView = [[UIImageView alloc] init];
        _arrowImgView.backgroundColor = [UIColor clearColor];
        _arrowImgView.image = [UIImage imageNamed:@"right_arrow_light_grey"];
        
        self.selectedBackgroundView = [[UIView alloc] init];
        self.selectedBackgroundView.backgroundColor = tableView_color;
        
        [self addSubview:_whiteView];
        [self addSubview:_moneyImgView];
        [self addSubview:_moneyLabel];
        [self addSubview:_parkNameLabel];
        [self addSubview:_dateLabel];
        [self addSubview:_stateLabel];
        [self addSubview:_arrowImgView];
    }
    return self;
}

- (void)layoutSubviews {
    [super layoutSubviews];
    
    self.selectedBackgroundView.frame = CGRectMake(0, 0, self.width, 85);
    
    _whiteView.frame = CGRectMake(0, 0, self.width, 85);
    _moneyImgView.frame = CGRectMake(padding, 10, 18, 18);
    _moneyLabel.frame = CGRectMake(_moneyImgView.right + 6, 10, 120, 18);
    _parkNameLabel.frame = CGRectMake(padding, _moneyLabel.bottom + 7, 200, 20);
    _dateLabel.frame = CGRectMake(self.width - padding - 120, _parkNameLabel.top, 120, _parkNameLabel.height);
    _stateLabel.frame = CGRectMake(padding, _parkNameLabel.bottom + 5, 90, 20);
    _arrowImgView.frame = CGRectMake(self.width - padding - 10, _stateLabel.top, 10, 15);
}

- (void)setItem:(THistoryOrderItem *)item {
    _item = item;
    _moneyLabel.text = [NSString stringWithFormat:@"%.2f", [_item.total floatValue]];
    _parkNameLabel.text = _item.parkname;
    _dateLabel.text = _item.date;
    
    [self layoutSubviews];
}

@end
