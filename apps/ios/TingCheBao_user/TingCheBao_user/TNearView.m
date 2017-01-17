//
//  TNearView.m
//  TingCheBao_user
//
//  Created by apple on 14-8-22.
//  Copyright (c) 2014年 zhenLaiDian. All rights reserved.
//

#import "TNearView.h"
#import "TAPIUtility.h"

#define padding 10
#define label_height 25
#define deep_white_color [UIColor whiteColor]
#define button_width   80
#define button_color   RGBCOLOR(206, 226, 223)

@interface TNearView()

@property(nonatomic, retain) TRecommendItem* item;

@property(nonatomic, retain) UILabel* nearLabel;
@property(nonatomic, retain) UILabel* freeInfoHeadLabel;
@property(nonatomic, retain) UILabel* freeInfoLabel;
@property(nonatomic, retain) UIButton* monthButton;
@property(nonatomic, retain) UILabel* monthLabel;
@property(nonatomic, retain) UIButton* bookButton;
@property(nonatomic, retain) UILabel* nearFreeView;
@property(nonatomic, retain) UILabel* nearFreeNumLabel;

@end
@implementation TNearView

- (id)initWithFrame:(CGRect)frame
{
    self = [super initWithFrame:frame];
    if (self) {
        // Initialization code
        //初始化nearView
        self.backgroundColor = gray_color;
        self.alpha = 0.9;
        
        _nearLabel = [[UILabel alloc] init];
        _nearLabel.text = @"我的位置附近";
        _nearLabel.textColor = deep_white_color;
//        _nearLabel.font = [UIFont systemFontOfSize:15];
        _nearLabel.adjustsFontSizeToFitWidth = YES;
        
        
        _freeInfoHeadLabel = [[UILabel alloc] init];
        _freeInfoHeadLabel.textColor = light_white_color;
        _freeInfoHeadLabel.font = [UIFont systemFontOfSize:15];
        _freeInfoHeadLabel.text = @"预计0分钟后车位";
        _freeInfoHeadLabel.adjustsFontSizeToFitWidth = YES;
        
        _freeInfoLabel = [[UILabel alloc] init];
        _freeInfoLabel.textColor = deep_white_color;
        _freeInfoLabel.text = @"";
        _freeInfoLabel.textAlignment = NSTextAlignmentCenter;
        
        
        _monthButton = [UIButton buttonWithType:UIButtonTypeCustom];
        [_monthButton setBackgroundImage:[UIImage imageNamed:@"bg_monthlypay.png"] forState:UIControlStateNormal];
//        _monthButton.layer.cornerRadius = 5;
//        _monthButton.clipsToBounds = YES;
        [_monthButton addTarget:self action:@selector(buttonTouched:) forControlEvents:UIControlEventTouchUpInside];
        _monthButton.hidden = YES;
        
        _monthLabel = [[UILabel alloc] init];
        _monthLabel.backgroundColor = [UIColor clearColor];
        _monthLabel.font = [UIFont systemFontOfSize:14];
        _monthLabel.textColor = green_color;
        _monthLabel.textAlignment = NSTextAlignmentCenter;
        [_monthButton addSubview:_monthLabel];
        
        _bookButton = [UIButton buttonWithType:UIButtonTypeCustom];
        [_bookButton setTitle:@"预订" forState:UIControlStateNormal];
        [_bookButton setTitleColor:[UIColor blackColor] forState:UIControlStateNormal];
        [_bookButton setBackgroundImage:[TAPIUtility imageWithColor:button_color] forState:UIControlStateNormal];
        _bookButton.layer.cornerRadius = 5;
        _bookButton.titleLabel.font = [UIFont systemFontOfSize:14];
        _bookButton.clipsToBounds = YES;
        [_bookButton addTarget:self action:@selector(buttonTouched:) forControlEvents:UIControlEventTouchUpInside];

        [self addSubview:_nearLabel];
        [self addSubview:_freeInfoHeadLabel];
        [self addSubview:_freeInfoLabel];
        [self addSubview:_monthButton];
//        [self addSubview:_bookButton];
    }
    return self;
}

- (void)layoutSubviews {
    [super layoutSubviews];
    _nearLabel.frame = CGRectMake(padding, 0, 190, label_height);
    _freeInfoHeadLabel.frame = CGRectMake(padding, _nearLabel.bottom, 140, label_height);
    _freeInfoLabel.frame = CGRectMake(_freeInfoHeadLabel.right, _freeInfoHeadLabel.top, 40, label_height);
//    _monthButton.frame = CGRectMake(self.width - 2*button_width - 2*3, 5, button_width, self.height - 2*8);
    _monthButton.frame = CGRectMake(self.width - button_width - 3, 8, button_width, self.height - 2*8);
    _monthLabel.frame = CGRectMake(10, 0, _monthButton.width - 10, _monthButton.height);
    
}

- (void)setItem:(TRecommendItem *)item name:(NSString*)name{
    if (!name || [name isEqualToString:@""]) {
        name = @"我的位置";
    }
    _nearLabel.text = [NSString stringWithFormat:@"%@附近", name];
    if (item.hour == 0) {
        _freeInfoHeadLabel.text = [NSString stringWithFormat:@"预计%d分钟的车位", item.minite];
    } else {
        if (item.minite == 0) {
            _freeInfoHeadLabel.text = [NSString stringWithFormat:@"预计%d小时的车位", item.hour];
        } else {
            _freeInfoHeadLabel.text = [NSString stringWithFormat:@"预计%d小时%d分钟的车位", item.hour, item.minite];
        }
    }
    _freeInfoLabel.text = item.freeinfo;
    //更新颜色
    [self updateColor:_freeInfoLabel.text];
    
    if ([item.monthids count] == 0) {
        _monthButton.hidden = YES;
    } else {
        _monthLabel.text = [NSString stringWithFormat:@"包月(%d)", [item.monthids count]];
        _monthButton.hidden = NO;
    }
    if ([item.bookids count] == 0) {
        _bookButton.hidden = YES;
    } else {
        [_bookButton setTitle:[NSString stringWithFormat:@"预定(%d)", [item.bookids count]] forState:UIControlStateNormal];
        _bookButton.hidden = NO;
    }
}

- (void)updateColor :(NSString*)text{
    if ([text isEqualToString:@"紧张"]) {
        _freeInfoLabel.textColor = red_color;
    } else if ([text isEqualToString:@"充足"]) {
        _freeInfoLabel.textColor = green_color;
    } else {
        _freeInfoLabel.textColor = [UIColor orangeColor];
    }
}

- (void)buttonTouched:(UIButton*)button {
    if (_delegate) {
        if (button == _monthButton) {
            if ([_delegate respondsToSelector:@selector(monthButtonTouched)]) {
                [_delegate monthButtonTouched];
            } else if ([_delegate respondsToSelector:@selector(bookButtonTouched)]) {
                [_delegate bookButtonTouched];
            }
        }
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
