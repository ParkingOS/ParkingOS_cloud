//
//  TChooseTicketsCell.m
//  TingCheBao_user
//
//  Created by apple on 15/4/27.
//  Copyright (c) 2015年 zhenLaiDian. All rights reserved.
//

#import "TChooseTicketsCell.h"
#import "TAPIUtility.h"

#define padding 5

@interface TChooseTicketsCell()

@property(nonatomic, retain) UIView* centerView;
@property(nonatomic, retain) UIImageView* bgImgView;
@property(nonatomic, retain) UIImageView* bg2ImgView;
@property(nonatomic, retain) UIImageView* specialImgView;
@property(nonatomic, retain) UILabel* moneyLabel;//
@property(nonatomic, retain) UILabel* youhuiLabel;//停车券
@property(nonatomic, retain) UILabel* nameLabel;//车场名字，专用时显示
@property(nonatomic, retain) UILabel* descLabel;//满5元可用
@property(nonatomic, retain) UIImageView* isbuyImgView;//满5元可用

@property(nonatomic, retain) UIView* lineView;//-------

@property(nonatomic, retain) UILabel* timeLabel;//还有3天到期
@property(nonatomic, retain) UILabel* limitLabel;//有效期至
@property(nonatomic, retain) UIImageView* selectedImgView;
@property(nonatomic, retain) UIView* overView;

@property(nonatomic, retain) NSString* type;

@end
@implementation TChooseTicketsCell

- (id)initWithStyle:(UITableViewCellStyle)style reuseIdentifier:(NSString *)reuseIdentifier {
    if (self = [super initWithStyle:style reuseIdentifier:reuseIdentifier]) {
        self.contentView.backgroundColor = [UIColor clearColor];
        self.backgroundColor = [UIColor clearColor];
        _centerView = [[UIView alloc] init];
        
        _bgImgView = [[UIImageView alloc] initWithImage:[UIImage imageNamed:@"bg_listitem_ticket_normal.png"]];
        _bg2ImgView = [[UIImageView alloc] initWithImage:[UIImage imageNamed:@"img_listitem_ticket_car.png"]];
        
        _moneyLabel = [[UILabel alloc] init];
        _moneyLabel.backgroundColor = [UIColor clearColor];
        
        _youhuiLabel = [[UILabel alloc] init];
        _youhuiLabel.text = @"停车优惠券";
        _youhuiLabel.backgroundColor = [UIColor clearColor];
        _youhuiLabel.textColor = RGBCOLOR(135, 135, 135);
        _youhuiLabel.textAlignment = NSTextAlignmentLeft;
        _youhuiLabel.font = [UIFont boldSystemFontOfSize:15];
        
        _nameLabel = [[UILabel alloc] init];
        _nameLabel.backgroundColor = [UIColor clearColor];
        _nameLabel.textColor = green_color;
        _nameLabel.textAlignment = NSTextAlignmentLeft;
        _nameLabel.font = [UIFont boldSystemFontOfSize:14];
        
        _descLabel = [[UILabel alloc] init];
        _descLabel.backgroundColor = [UIColor clearColor];
        _descLabel.textColor = RGBCOLOR(135, 135, 135);
        _descLabel.textAlignment = NSTextAlignmentLeft;
        _descLabel.font = [UIFont boldSystemFontOfSize:12];
        _descLabel.numberOfLines = 2;
        
        _isbuyImgView = [[UIImageView alloc] initWithImage:[UIImage imageNamed:@"buy_ticket.png"]];
        _isbuyImgView.hidden = YES;
        
        //--------
        _lineView = [[UIView alloc] init];
        _lineView.backgroundColor = RGBCOLOR(221, 221, 221);
        //--------
        
        _timeLabel = [[UILabel alloc] init];
        _timeLabel.backgroundColor = [UIColor clearColor];
        _timeLabel.textColor = green_color;
        _timeLabel.font      = [UIFont boldSystemFontOfSize:12];
        _timeLabel.textAlignment = NSTextAlignmentLeft;
        
        _limitLabel = [[UILabel alloc] init];
        _limitLabel.backgroundColor = [UIColor clearColor];
        _limitLabel.textColor = RGBCOLOR(135, 135, 135);
        _limitLabel.textAlignment = NSTextAlignmentRight;
        _limitLabel.font = [UIFont systemFontOfSize:12];
        
        //
        _selectedImgView = [[UIImageView alloc] initWithImage:[UIImage imageNamed:@"ticket_top.png"]];
        
        [self.centerView addSubview:_bgImgView];
        [self.centerView addSubview:_bg2ImgView];
        [self.centerView addSubview:_moneyLabel];
        [self.centerView addSubview:_nameLabel];
        [self.centerView addSubview:_youhuiLabel];
        [self.centerView addSubview:_descLabel];
        [self.centerView addSubview:_isbuyImgView];
        
        [self.centerView addSubview:_lineView];
        
        [self.centerView addSubview:_timeLabel];
        [self.centerView addSubview:_limitLabel];
        
        [self.centerView addSubview:_selectedImgView];
        
        [self.contentView addSubview:_centerView];
    }
    return self;
}

- (void)layoutSubviews {
    [super layoutSubviews];
    
    _lineView.backgroundColor = RGBCOLOR(221, 221, 221);
    
    _centerView.frame =CGRectMake(padding, 10, self.width - 2*padding, 135);
    _bgImgView.frame = CGRectMake(0, 0, _centerView.width, 132);
    _bg2ImgView.frame = CGRectMake(_centerView.width - 110, 20, 100, 60);
//    CGSize size = T_TEXTSIZE(_moneyLabel.text, _nameLabel.font);
    _moneyLabel.frame = CGRectMake(20, 23, 110, 50);
    
    _youhuiLabel.frame = CGRectMake(_moneyLabel.right, 20, 130, 20);
    _nameLabel.frame = CGRectMake(_moneyLabel.right, _youhuiLabel.bottom, self.centerView.width - _moneyLabel.right, 20);
    
    CGSize size = [TAPIUtility sizeWithFont:_descLabel.font size:CGSizeMake(_descLabel.width, 40) text:_descLabel.text];
    _descLabel.frame = CGRectMake(_moneyLabel.right, [_type isEqualToString:@"1"] ? _nameLabel.bottom : _nameLabel.top, self.centerView.width - _moneyLabel.right, MIN(size.height, 40));
    _isbuyImgView.frame = CGRectMake(self.centerView.right - 50, 8, 30, 30);
    
    _lineView.frame = CGRectMake(5, _youhuiLabel.bottom + 50, _centerView.width - 5 -4, 1);
    _timeLabel.frame = CGRectMake(20, _lineView.bottom, 200, 30);
    _limitLabel.frame = CGRectMake(_centerView.width - 155, _lineView.bottom, 140, 30);
    _selectedImgView.frame = CGRectMake(0, 0, _centerView.width, 135);
}

- (void)setItem:(TChooseTicketItem *)item {
    _item = item;
    _type = item.type;
    _nameLabel.text = _item.cname;
    NSString* money = [NSString stringWithFormat:@"¥%@", _item.money];
    NSMutableAttributedString* attr = [[NSMutableAttributedString alloc] initWithString:money attributes:@{NSForegroundColorAttributeName : RGBCOLOR(85, 85, 85), NSFontAttributeName : [UIFont systemFontOfSize:30]}];
    [attr addAttributes:@{NSFontAttributeName : [UIFont systemFontOfSize:60]} range:NSMakeRange(1, money.length - 1)];
    _moneyLabel.attributedText = attr;
    
    //专用券和普通券
    if ([_item.type isEqualToString:@"1"]) {
        _nameLabel.hidden = NO;
        _youhuiLabel.text = @"专用停车券";
        _youhuiLabel.textColor = green_color;
    } else {
        _nameLabel.hidden = YES;
        _youhuiLabel.text = @"普通停车券";
        _youhuiLabel.textColor = RGBCOLOR(135, 135, 135);
    }
    _descLabel.text = _item.desc;

//    是否是购买的停车券
    _isbuyImgView.hidden = [_item.isbuy isEqualToString:@"1"] ? NO : YES;
    
    _timeLabel.text = [self getDuration:_item.limit_day];
    _limitLabel.text = [NSString stringWithFormat:@"有效期至 %@",[self stringWithTime:_item.limit_day]];
    
    self.centerView.alpha = [_item.iscanuse isEqualToString:@"1"] ? 1 : 0.3;
    [self layoutIfNeeded];
}

- (void)setItem2:(TTicketItem *)item2 {
    _item2 = item2;
    _type = item2.type;
    
    _nameLabel.text = _item2.cname;
    NSString* money = [NSString stringWithFormat:@"¥%@", _item2.money];
    NSMutableAttributedString* attr = [[NSMutableAttributedString alloc] initWithString:money attributes:@{NSForegroundColorAttributeName : RGBCOLOR(85, 85, 85), NSFontAttributeName : [UIFont systemFontOfSize:30]}];
    [attr addAttributes:@{NSFontAttributeName : [UIFont systemFontOfSize:60]} range:NSMakeRange(1, money.length - 1)];
    _moneyLabel.attributedText = attr;
    
    //专用券和普通券
    if ([_item2.type isEqualToString:@"1"]) {
        _nameLabel.hidden = NO;
        _youhuiLabel.text = @"专用停车券";
        _youhuiLabel.textColor = green_color;
    } else {
        _nameLabel.hidden = YES;
        _youhuiLabel.text = @"普通停车券";
        _youhuiLabel.textColor = RGBCOLOR(135, 135, 135);
    }
    _descLabel.text = _item2.desc;
    
    //是否是购买的停车券
    _isbuyImgView.hidden = [_item2.isbuy isEqualToString:@"1"] ? NO : YES;
    
    //状态，已使用，已过期
    if ([_item2.state isEqualToString:@"0"] && [_item2.exp isEqualToString:@"1"]) {
        _timeLabel.textColor = green_color;
        _timeLabel.text = [self getDuration:_item2.limitday];
        
        _bgImgView.image = [UIImage imageNamed:@"bg_listitem_ticket_normal.png"];
    } else {
        if ([_item2.state isEqualToString:@"1"]) {
            _timeLabel.textColor = green_color;
            _timeLabel.text = @"已使用";
        } else {
            _timeLabel.textColor = red_color;
            _timeLabel.text = @"已过期";
        }
        _bgImgView.image = [UIImage imageNamed:@"bg_listitem_ticket_disabled.png"];
    }
    
    _limitLabel.text = [NSString stringWithFormat:@"有效期至 %@",[self stringWithTime:_item2.limitday]];
    
    _selectedImgView.hidden = YES;
    [self layoutIfNeeded];
}

- (NSString*)getDuration:(NSString*)limitDay {
    NSDate* date = [NSDate date];
    NSDateFormatter* formatter = [[NSDateFormatter alloc] init];
    [formatter setDateFormat:@"HH"];
    int hour = [[formatter stringFromDate:date] intValue];
    [formatter setDateFormat:@"mm"];
    int minite = [[formatter stringFromDate:date] intValue];
    [formatter setDateFormat:@"ss"];
    int second = [[formatter stringFromDate:date] intValue];
    
    int new_day = [[NSDate date] timeIntervalSince1970] - hour*3600 - minite*60- second;
    
    NSTimeInterval longTime = [limitDay integerValue] - new_day;
    int day = longTime / (3600*24) + 1;
    if (day != 1)
        return [NSString stringWithFormat:@"还剩%d天过期", day];
    else
        return @"今天将要过期";
}


- (NSString*)stringWithTime:(NSString*)time {
    NSDate* date = [NSDate dateWithTimeIntervalSince1970:[time integerValue]];
    NSDateFormatter* formatter = [[NSDateFormatter alloc] init];
    [formatter setDateFormat:@"yyyy-MM-dd"];
    return [formatter stringFromDate:date];
}

- (void)setSelected:(BOOL)selected animated:(BOOL)animated {
    [super setSelected:selected animated:animated];
    if (_item) {
        self.selectedBackgroundView.hidden = YES;
        if (selected) {
            _selectedImgView.hidden = NO;
        } else {
            _selectedImgView.hidden = YES;
        }
    }
    // Configure the view for the selected state
}

@end
