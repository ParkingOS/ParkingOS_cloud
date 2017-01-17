//
//  TPriceView.m
//  TingCheBao_user
//
//  Created by apple on 14-9-11.
//  Copyright (c) 2014年 zhenLaiDian. All rights reserved.
//

#import "TPriceView.h"


#define bg_color RGBCOLOR(230, 230, 230)

@interface TPriceView()

//{“id":"524","price":"2.00","unit":"15","pay_type":"0","b_time":"7","e_time":"21","first_times":"60","fprice":"1.00","countless":"0","fpay_type":"0","free_time":"1","nid":"525","nprice":"0.01","nunit":"30","nfirst_times":"60","ncountless":"0","npay_type":"0","nfpay_type":"0","nfree_time":"0","nfprice":"0.02","isnight":"0"}

@property(nonatomic, retain) UIImageView* topView;
@property(nonatomic, retain) UIImageView* imgView;
//日间、夜间
@property(nonatomic, retain) UILabel* dayNightLabel;

//首小时内
@property(nonatomic, retain) UILabel* hourInLabel;
@property(nonatomic, retain) UILabel* hourInNumLabel;

//首小时外
@property(nonatomic, retain) UILabel* hourOutLabel;
@property(nonatomic, retain) UILabel* hourOutNumLabel;

//单位
@property(nonatomic, retain) UILabel* unitLabel;
@property(nonatomic, retain) UILabel* unitNumLabel;

//备注
@property(nonatomic, retain) UILabel* descriptionLabel;

//竖线
@property(nonatomic, retain) UIView* line1View;
@property(nonatomic, retain) UIView* line2View;
//横线
@property(nonatomic, retain) UIView* lineView1;
@property(nonatomic, retain) UIView* lineView2;


@end
@implementation TPriceView

- (id)initWithFrame:(CGRect)frame
{
    self = [super initWithFrame:frame];
    if (self) {
        // Initialization code
        //topView
        self.backgroundColor = [UIColor whiteColor];
        self.layer.cornerRadius = 5;
        self.clipsToBounds = YES;
        
        _topView = [[UIImageView alloc] initWithFrame:CGRectMake(0, 0, self.width, 50)];
        
        _imgView = [[UIImageView alloc] initWithImage:[UIImage imageNamed:@"ic_price_day.png"]];
        _imgView.frame = CGRectMake(20, 12, 26, 26);
       
        _dayNightLabel = [[UILabel alloc] initWithFrame:CGRectMake(_imgView.right + 10, 0, 200, _topView.height)];
        _dayNightLabel.backgroundColor = [UIColor clearColor];
        _dayNightLabel.text = @"日间 (07:00 - 21:00)";
        _dayNightLabel.textColor = [UIColor whiteColor];
        
        [_topView addSubview:_imgView];
        [_topView addSubview:_dayNightLabel];
        
        _hourInLabel = [[UILabel alloc] initWithFrame:CGRectMake(0, _topView.bottom, self.width/3, _topView.height)];
        _hourInLabel.backgroundColor = [UIColor clearColor];
        _hourInLabel.text = @"首小时内";
        _hourInLabel.textAlignment = NSTextAlignmentCenter;
        
        _hourOutLabel = [[UILabel alloc] initWithFrame:CGRectMake(_hourInLabel.right, _topView.bottom, self.width/3, _topView.height)];
        _hourOutLabel.backgroundColor = [UIColor clearColor];
        _hourOutLabel.text = @"首小时外";
        _hourOutLabel.textAlignment = NSTextAlignmentCenter;
        
        _unitLabel = [[UILabel alloc] initWithFrame:CGRectMake(_hourOutLabel.right, _topView.bottom, self.width/3, _topView.height)];
        _unitLabel.backgroundColor = [UIColor clearColor];
        _unitLabel.text = @"计价单位";
        _unitLabel.textAlignment = NSTextAlignmentCenter;
        
        _hourInNumLabel = [[UILabel alloc] initWithFrame:CGRectMake(0, _hourInLabel.bottom, self.width/3, _topView.height)];
        _hourInNumLabel.backgroundColor = [UIColor clearColor];
        _hourInNumLabel.text = @"0.0";
        _hourInNumLabel.textAlignment = NSTextAlignmentCenter;
        
        _hourOutNumLabel = [[UILabel alloc] initWithFrame:CGRectMake(_hourInNumLabel.right, _hourInLabel.bottom, self.width/3, _topView.height)];
        _hourOutNumLabel.backgroundColor = [UIColor clearColor];
        _hourOutNumLabel.text = @"0.0";
        _hourOutNumLabel.textAlignment = NSTextAlignmentCenter;
        
        _unitNumLabel = [[UILabel alloc] initWithFrame:CGRectMake(_hourOutNumLabel.right, _hourInLabel.bottom, self.width/3, _topView.height)];
        _unitNumLabel.backgroundColor = [UIColor clearColor];
        _unitNumLabel.text = @"元/15分钟";
        _unitNumLabel.textAlignment = NSTextAlignmentCenter;
        
        _descriptionLabel = [[UILabel alloc] initWithFrame:CGRectMake(10, _hourInNumLabel.bottom, self.width - 2*10, 50)];
        _descriptionLabel.backgroundColor = [UIColor clearColor];
        NSMutableAttributedString* desc = [[NSMutableAttributedString alloc] initWithString:@"备注：实际价格信息可能根据现场有一定调整。"];
        [desc addAttributes:@{NSFontAttributeName : [UIFont boldSystemFontOfSize:18]} range:NSMakeRange(0, 3)];
        _descriptionLabel.attributedText = desc;
        _descriptionLabel.numberOfLines = 2;
        
        _line1View = [[UIView alloc] initWithFrame:CGRectMake(self.width/3, _hourInLabel.top, 1, _topView.height*2)];
        _line1View.backgroundColor = bg_color;
        _line2View = [[UIView alloc] initWithFrame:CGRectMake(self.width/3*2, _hourInLabel.top, 1, _topView.height*2)];
        _line2View.backgroundColor = bg_color;
        _lineView1 = [[UIView alloc] initWithFrame:CGRectMake(0, _hourInLabel.bottom, self.width, 1)];
        _lineView1.backgroundColor = bg_color;
        _lineView2 = [[UIView alloc] initWithFrame:CGRectMake(0, _hourInNumLabel.bottom, self.width, 1)];
        _lineView2.backgroundColor = bg_color;
        
        [self addSubview:_topView];
        [self addSubview:_hourInLabel];
        [self addSubview:_hourOutLabel];
        [self addSubview:_unitLabel];
        [self addSubview:_hourInNumLabel];
        [self addSubview:_hourOutNumLabel];
        [self addSubview:_unitNumLabel];
        [self addSubview:_descriptionLabel];
        [self addSubview:_line1View];
        [self addSubview:_line2View];
        [self addSubview:_lineView1];
        [self addSubview:_lineView2];
    }
    return self;
}

- (void)setItem:(TPriceItem *)item isDay:(BOOL)isDay{
    if (isDay) {
        _topView.image = [UIImage imageNamed:@"bkg_price_day.png"];
        _imgView.image = [UIImage imageNamed:@"ic_price_day.png"];
        _dayNightLabel.text = @"日间 (07:00 - 21:00)";
    } else {
        _topView.image = [UIImage imageNamed:@"bkg_price_night.png"];
        _imgView.image = [UIImage imageNamed:@"ic_price_night.png"];
        _dayNightLabel.text = @"夜间 (07:00 - 21:00)";
    }
    
    if (!item) {
        return;
    }
    _item = item;
    NSMutableAttributedString* desc = nil;
    if (isDay) {
        _dayNightLabel.text = [NSString stringWithFormat:@"日间 (%@:00 - %@:00)", item.b_time, item.e_time];
        if ([item.first_times integerValue] > 0 && [item.first_times integerValue] % 60 == 0) {
            int minite = [item.first_times integerValue];
            int hour = minite / 60;
            if (hour == 1) {
                _hourInLabel.text = [NSString stringWithFormat:@"首小时内"];
                _hourOutLabel.text = [NSString stringWithFormat:@"首小时外"];
            }
            else {
                _hourInLabel.text = [NSString stringWithFormat:@"首%d小时内", hour];
                _hourOutLabel.text = [NSString stringWithFormat:@"首%d小时外", hour];
            }
        } else {
            _hourInLabel.text = [NSString stringWithFormat:@"首%@分钟内", item.first_times];
            _hourOutLabel.text = [NSString stringWithFormat:@"首%@分钟外", item.first_times];
        }
        _hourInNumLabel.text = item.fprice;
        _hourOutNumLabel.text = item.price;
        _unitNumLabel.text = [NSString stringWithFormat:@"元/%@分钟", item.unit];
        
        if ([item.free_time isEqualToString:@"0"]) {
            desc = [[NSMutableAttributedString alloc] initWithString:@"备注：实际价格信息可能根据现场有一定调整。"];
        } else {
            if ([item.fpay_type isEqualToString:@"0"]) {
                //收费
                desc = [[NSMutableAttributedString alloc] initWithString:[NSString stringWithFormat:@"备注：%@分钟内停车免费，但超过%@分钟,这%@分钟收费", item.free_time, item.free_time, item.free_time]];
            } else {
                desc = [[NSMutableAttributedString alloc] initWithString:[NSString stringWithFormat:@"备注：%@分钟内停车免费，超过%@分钟,这%@分钟也免费", item.free_time, item.free_time, item.free_time]];
            }
        }
        [desc addAttributes:@{NSFontAttributeName : [UIFont boldSystemFontOfSize:18]} range:NSMakeRange(0, 3)];
        _descriptionLabel.attributedText = desc;
        
    } else {
        _dayNightLabel.text = [NSString stringWithFormat:@"夜间 (%@:00 - %@:00)", item.e_time, item.b_time];
        if ([item.nfirst_times integerValue] > 0 && [item.nfirst_times integerValue] % 60 == 0) {
            int minite = [item.nfirst_times integerValue];
            int hour = minite / 60;
            if (hour == 1) {
                _hourInLabel.text = [NSString stringWithFormat:@"首小时内"];
                _hourOutLabel.text = [NSString stringWithFormat:@"首小时外"];
            } else {
                _hourInLabel.text = [NSString stringWithFormat:@"首%d小时内", hour];
                _hourOutLabel.text = [NSString stringWithFormat:@"首%d小时外", hour];
            }
        } else {
            _hourInLabel.text = [NSString stringWithFormat:@"首%@分钟内", item.nfirst_times];
            _hourOutLabel.text = [NSString stringWithFormat:@"首%@分钟外", item.nfirst_times];
        }
        _hourInNumLabel.text = item.nfprice;
        _hourOutNumLabel.text = item.nprice;
        _unitNumLabel.text = [NSString stringWithFormat:@"元/%@分钟", item.nunit];
        if ([item.nfree_time isEqualToString:@"0"]) {
            desc = [[NSMutableAttributedString alloc] initWithString:@"备注：实际价格信息可能根据现场有一定调整。"];
        } else {
            if ([item.nfpay_type isEqualToString:@"0"]) {
                //收费
                desc = [[NSMutableAttributedString alloc] initWithString:[NSString stringWithFormat:@"备注：%@分钟内停车免费，但超过%@分钟,这%@分钟收费", item.nfree_time, item.nfree_time, item.nfree_time]];
            } else {
                desc = [[NSMutableAttributedString alloc] initWithString:[NSString stringWithFormat:@"备注：%@分钟内停车免费，超过%@分钟,这%@分钟也免费", item.nfree_time, item.nfree_time, item.nfree_time]];
            }
        }
        [desc addAttributes:@{NSFontAttributeName : [UIFont boldSystemFontOfSize:18]} range:NSMakeRange(0, 3)];
        _descriptionLabel.attributedText = desc;
    }
}

@end
