//
//  TRedPackageCell.m
//  TingCheBao_user
//
//  Created by yangshaojin on 15-3-11.
//  Copyright (c) 2015年 zhenLaiDian. All rights reserved.
//

#import "TRedPackageCell.h"

@interface TRedPackageCell ()

@property (nonatomic, readwrite) UIView *centerView;
@property (nonatomic, readwrite) UIView *bgImgView;
@property (nonatomic, readwrite) UIImageView *redImageView;
@property (nonatomic, readwrite) UILabel *packageLabel;
@property (nonatomic, readwrite) UILabel *dateLabel;
@property (nonatomic, readwrite) UILabel *getLabel;

@end

@implementation TRedPackageCell

- (instancetype)initWithStyle:(UITableViewCellStyle)style reuseIdentifier:(NSString *)reuseIdentifier
{
    if (self = [super initWithStyle:style reuseIdentifier:reuseIdentifier]) {
        self.backgroundColor = [UIColor clearColor];
        
        _centerView = [[UIView alloc] init];
        
        _bgImgView = [[UIImageView alloc] initWithImage:[UIImage imageNamed:@"redlist_bg.png"]];
        
        _redImageView = [[UIImageView alloc] init];
        
        _packageLabel = [[UILabel alloc] init];
        _packageLabel.backgroundColor = [UIColor clearColor];
        _packageLabel.textAlignment = NSTextAlignmentCenter;
        _packageLabel.text = @"停车券礼包";
        _packageLabel.font = [UIFont boldSystemFontOfSize:20];
        
        _dateLabel = [[UILabel alloc] init];
        _dateLabel.backgroundColor = [UIColor clearColor];
        _dateLabel.textAlignment = NSTextAlignmentLeft;
        _dateLabel.font = [UIFont systemFontOfSize:12];
        _dateLabel.textColor = [UIColor grayColor];

        _getLabel = [[UILabel alloc] init];
        _getLabel.backgroundColor = [UIColor clearColor];
        _getLabel.textAlignment = NSTextAlignmentLeft;
        _getLabel.font = [UIFont boldSystemFontOfSize:15];
        
        [self.centerView addSubview:_bgImgView];
        [self.centerView addSubview:_packageLabel];
        [self.centerView addSubview:_redImageView];
        [self.centerView addSubview:_dateLabel];
        [self.centerView addSubview:_getLabel];
        
        [self.contentView addSubview:self.centerView];
    }
    return self;
}

- (void)layoutSubviews {
    [super layoutSubviews];
    
    _centerView.frame = CGRectMake(10, 0, self.width - 2*10, self.height - 10);
    _bgImgView.frame = CGRectMake(0, 0, _centerView.width, _centerView.height);
    
    _redImageView.frame = CGRectMake(10, (_centerView.height - 60)/2, 55, 60);
    _packageLabel.frame = CGRectMake(_redImageView.right + 10, _redImageView.top, 150, 20);
    _dateLabel.frame = CGRectMake(_redImageView.right + 10, _packageLabel.bottom + 20, 200, 20);
    _getLabel.frame = CGRectMake(self.centerView.width - 65, _dateLabel.top, 60, 20);
    
}

- (void)setDict:(NSDictionary *)dict
{
    _dict = dict;
    
    _packageLabel.text = [_dict objectForKey:@"title"];
    
    NSString* imgName = @"";
    if ([_packageLabel.text isEqualToString:@"订单停车券礼包"]) {
        imgName = @"redlist_order";
    } else if ([_packageLabel.text isEqualToString:@"认证通过大礼包"]) {
        imgName = @"redlist_check";
    } else if ([_packageLabel.text isEqualToString:@"充值停车券礼包"]) {
        imgName = @"redlist_recharge";
    } else if ([_packageLabel.text isEqualToString:@"游戏停车券礼包"]) {
        imgName = @"redlist_game";
    }
    
    if ([[_dict objectForKey:@"state"] integerValue] == 1) {
        
        self.redImageView.image = [UIImage imageNamed:[NSString stringWithFormat:@"%@.png", imgName]];
        self.getLabel.text = @"立即领取";
        self.getLabel.textColor = RGBCOLOR(247, 167, 53);
        self.dateLabel.text = [self timeStringFromDate:[_dict objectForKey:@"exptime"]];
    }else{
        
        self.redImageView.image = [UIImage imageNamed:[NSString stringWithFormat:@"%@_gray.png", imgName]];
        self.dateLabel.text = [self timeStringFromDate:[_dict objectForKey:@"exptime"]];
        
        if ([[_dict objectForKey:@"state"] integerValue] == 2) {
            self.getLabel.text = @"已领取";
            self.getLabel.textColor = [UIColor redColor];
        }else{
            self.getLabel.text = @"已过期";
            self.getLabel.textColor = RGBCOLOR(137, 137, 137);
        }
    }
}

- (NSString *)timeStringFromDate:(NSString *)time
{
    NSDate *date = [NSDate dateWithTimeIntervalSince1970:[time longLongValue]];
    NSDateFormatter *dateFormatter = [[NSDateFormatter alloc] init];
    [dateFormatter setDateFormat:@"MM月dd日 HH:mm前领取有效"];
    return [dateFormatter stringFromDate:date];
}

@end
