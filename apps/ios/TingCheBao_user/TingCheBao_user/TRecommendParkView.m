//
//  TNearView.m
//  TingCheBao_user
//
//  Created by apple on 14-8-22.
//  Copyright (c) 2014年 zhenLaiDian. All rights reserved.
//

#import "TRecommendParkView.h"
#import "TAPIUtility.h"

#define padding 10
#define label_height 20
#define deep_white_color [UIColor whiteColor]
#define button_width   80
#define button_color   RGBCOLOR(206, 226, 223)

@interface TRecommendParkView()

@property(nonatomic, retain) UIView* topView;
@property(nonatomic, retain) UIView* lineView;
@property(nonatomic, retain) UIView* bottomView;

@property(nonatomic, retain) UILabel* nearLabel;
@property(nonatomic, retain) UILabel* freeInfoHeadLabel;
@property(nonatomic, retain) UILabel* freeInfoLabel;
@property(nonatomic, retain) UIButton* monthButton;
@property(nonatomic, retain) UILabel* monthLabel;
@property(nonatomic, retain) UIButton* bookButton;
@property(nonatomic, retain) UILabel* nearFreeView;
@property(nonatomic, retain) UILabel* nearFreeNumLabel;


@property(nonatomic, retain) UILabel* parkLabel;
@property(nonatomic, retain) UILabel* freeLabel;
@property(nonatomic, retain) UILabel* freeNumLabel;
@property(nonatomic, retain) UILabel* unitLabel;
@property(nonatomic, retain) UILabel* priceLabel;
@property(nonatomic, retain) UIImageView* payImgView;
@property(nonatomic, retain) UIImageView* monthImgView;
@property(nonatomic, retain) UIImageView* rightArrowImgView;
@property(nonatomic, retain) UILabel* priceNumLabel;
@property(nonatomic, retain) UIView* lineView2;

@end
@implementation TRecommendParkView

- (id)initWithFrame:(CGRect)frame
{
    self = [super initWithFrame:frame];
    if (self) {
        // Initialization code
        //初始化nearView
        
        //top
        _topView = [[UIView alloc] init];
        _topView.backgroundColor = [UIColor whiteColor];
        _topView.alpha = 0.8;
        
        _nearLabel = [[UILabel alloc] init];
        _nearLabel.backgroundColor = [UIColor clearColor];
        _nearLabel.text = @"我的位置附近";
        _nearLabel.textColor = green_color;
        _nearLabel.font = [UIFont systemFontOfSize:15];
        _nearLabel.adjustsFontSizeToFitWidth = YES;
        
        
        _freeInfoHeadLabel = [[UILabel alloc] init];
        _freeInfoHeadLabel.backgroundColor = [UIColor clearColor];
        _freeInfoHeadLabel.textColor = [UIColor grayColor];
        _freeInfoHeadLabel.font = [UIFont systemFontOfSize:13];
        _freeInfoHeadLabel.text = @"预计0分钟后车位";
        _freeInfoHeadLabel.adjustsFontSizeToFitWidth = YES;
        
        _freeInfoLabel = [[UILabel alloc] init];
        _freeInfoLabel.backgroundColor = [UIColor clearColor];
        _freeInfoLabel.textColor = green_color;
        _freeInfoLabel.text = @"";
        _freeInfoLabel.textAlignment = NSTextAlignmentLeft;
        _freeInfoLabel.font = [UIFont systemFontOfSize:13];
        
        
        _monthLabel = [[UILabel alloc] init];
        _monthLabel.backgroundColor = green_color;
//        _monthLabel.font = [UIFont systemFontOfSize:14];
        _monthLabel.textColor = [UIColor whiteColor];
        _monthLabel.textAlignment = NSTextAlignmentCenter;
        _monthLabel.text = @"团";
        _monthLabel.hidden = YES;
        
        _monthButton = [UIButton buttonWithType:UIButtonTypeCustom];
        _monthButton.titleLabel.font = [UIFont systemFontOfSize:14];
        [_monthButton setBackgroundImage:[TAPIUtility imageWithColor:[UIColor whiteColor]] forState:UIControlStateNormal];
        _monthButton.layer.borderColor = green_color.CGColor;
        _monthButton.layer.borderWidth = 1;
        [_monthButton setTitleColor:green_color forState:UIControlStateNormal];
        [_monthButton addTarget:self action:@selector(buttonTouched:) forControlEvents:UIControlEventTouchUpInside];
        _monthButton.hidden = YES;
        
        _bookButton = [UIButton buttonWithType:UIButtonTypeCustom];
        [_bookButton setTitle:@"预订" forState:UIControlStateNormal];
        [_bookButton setTitleColor:[UIColor blackColor] forState:UIControlStateNormal];
        [_bookButton setBackgroundImage:[TAPIUtility imageWithColor:button_color] forState:UIControlStateNormal];
        _bookButton.layer.cornerRadius = 5;
        _bookButton.titleLabel.font = [UIFont systemFontOfSize:14];
        _bookButton.clipsToBounds = YES;
        [_bookButton addTarget:self action:@selector(buttonTouched:) forControlEvents:UIControlEventTouchUpInside];
        
        [_topView addSubview:_nearLabel];
        [_topView addSubview:_freeInfoHeadLabel];
        [_topView addSubview:_freeInfoLabel];
        [_topView addSubview:_monthLabel];
        [_topView addSubview:_monthButton];
        
        //lineView
        _lineView = [[UIView alloc] init];
        _lineView.backgroundColor = recommend_line_color;
        
        //bottom
        _bottomView = [[UIView alloc] init];
        _bottomView.backgroundColor = [UIColor whiteColor];
        UITapGestureRecognizer* tapGesture = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(handleTapGestrue:)];
        [_bottomView addGestureRecognizer:tapGesture];
       
        _parkLabel = [[UILabel alloc] init];
        _parkLabel.backgroundColor = [UIColor clearColor];
        _parkLabel.text = @"推荐:";
        _parkLabel.textColor = [UIColor blackColor];
        _parkLabel.font = [UIFont systemFontOfSize:15];
        
        _freeLabel = [[UILabel alloc] init];
        _freeLabel.backgroundColor = [UIColor clearColor];
        _freeLabel.text = @"空闲车位";
        _freeLabel.textColor = [UIColor grayColor];
        _freeLabel.font = [UIFont systemFontOfSize:13];
        
        _freeNumLabel = [[UILabel alloc] init];
        _freeNumLabel.backgroundColor = [UIColor clearColor];
        _freeNumLabel.text = @"0";
        _freeNumLabel.textColor = [UIColor blackColor];
        _freeNumLabel.textAlignment = NSTextAlignmentCenter;
        _freeNumLabel.font = [UIFont systemFontOfSize:13];
        
        _unitLabel = [[UILabel alloc] init];
        _unitLabel.backgroundColor = [UIColor clearColor];
        _unitLabel.text = @"个";
        _unitLabel.textColor = [UIColor grayColor];
        _unitLabel.font = [UIFont systemFontOfSize:13];
        
        _priceLabel = [[UILabel alloc] init];
        _priceLabel.backgroundColor = [UIColor clearColor];
        _priceLabel.text = @"价格";
        _priceLabel.textColor = [UIColor grayColor];
        _priceLabel.font = [UIFont systemFontOfSize:13];
        
        _priceNumLabel = [[UILabel alloc] init];
        _priceNumLabel.backgroundColor = [UIColor clearColor];
        _priceNumLabel.text = @"0.0元/0分钟";
        _priceNumLabel.textColor = [UIColor blackColor];
        _priceNumLabel.font = [UIFont systemFontOfSize:13];
        
        _payImgView = [[UIImageView alloc] initWithImage:[UIImage imageNamed:@"supportMobilePay.png"]];
        _payImgView.hidden = YES;
        
        _monthImgView = [[UIImageView alloc] initWithImage:[UIImage imageNamed:@"supportMonth.png"]];
        _monthImgView.hidden = YES;
        
        _rightArrowImgView = [[UIImageView alloc] initWithImage:[UIImage imageNamed:@"ic_arrow_grey.png"]];
        
        //lineView
        _lineView2 = [[UIView alloc] init];
        _lineView2.backgroundColor = recommend_line_color;
        
        [_bottomView addSubview:_parkLabel];
        [_bottomView addSubview:_freeLabel];
        [_bottomView addSubview:_freeNumLabel];
        [_bottomView addSubview:_unitLabel];
        [_bottomView addSubview:_priceLabel];
        [_bottomView addSubview:_priceNumLabel];
        [_bottomView addSubview:_payImgView];
        [_bottomView addSubview:_monthImgView];
        [_bottomView addSubview:_rightArrowImgView];
        [_bottomView addSubview:_lineView2];
        
        [self addSubview:_topView];
        [self addSubview:_lineView];
        [self addSubview:_bottomView];
    }
    return self;
}

- (void)layoutSubviews {
    [super layoutSubviews];
    //top
    _topView.frame = CGRectMake(0, 0, self.width, 50);
    _nearLabel.frame = CGRectMake(padding, 4, self.width, label_height);
    CGSize suggestSize = T_TEXTSIZE(_freeInfoHeadLabel.text, _freeInfoHeadLabel.font);
    _freeInfoHeadLabel.frame = CGRectMake(padding, _nearLabel.bottom + 3, suggestSize.width, label_height);
    _freeInfoLabel.frame = CGRectMake(_freeInfoHeadLabel.right, _freeInfoHeadLabel.top, 40, label_height);
    _monthLabel.frame = CGRectMake(self.width - 100, 10, 30, 30);
    _monthButton.frame = CGRectMake(_monthLabel.right, 10, 60, 30);
    
    //lineView
    _lineView.frame = CGRectMake(0, 50, self.width, 0.5);
    
    //bottom
    _bottomView.frame = CGRectMake(0, _lineView.bottom, self.width, 50);
    
    if (_mode == RecommendModeNoRecommendInfo) {
        _parkLabel.textAlignment = NSTextAlignmentCenter;
        _parkLabel.frame = CGRectMake(0, 2, self.width, 50);
    } else {
        _parkLabel.textAlignment = NSTextAlignmentLeft;
        if (_payImgView.hidden == NO || _monthImgView.hidden == NO) {
            _parkLabel.frame = CGRectMake(padding, 4, 240, label_height);
        } else {
            _parkLabel.frame = CGRectMake(padding, 4, self.width, label_height);
        }
        _freeLabel.frame = CGRectMake(padding, _parkLabel.bottom + 3, 55, label_height);
        _freeNumLabel.frame = CGRectMake(_freeLabel.right, _freeLabel.top, 40, label_height);
        CGSize size = T_TEXTSIZE(_freeNumLabel.text, _freeNumLabel.font);
        _freeNumLabel.width = size.width;
        
        _unitLabel.frame = CGRectMake(_freeNumLabel.right, _freeLabel.top, 40, label_height);
        _priceLabel.frame = CGRectMake(120, _freeLabel.top, 28, label_height);
        _priceNumLabel.frame = CGRectMake(_priceLabel.right, _freeLabel.top, 90, label_height);
        _payImgView.frame = CGRectMake(_priceNumLabel.right, 12, 25, 25);
        _monthImgView.frame = CGRectMake(_payImgView.right + 2, 12, 25, 25);
        _rightArrowImgView.frame = CGRectMake(self.width - 24, 14, 20, 20);
    }
    _freeLabel.hidden = _freeNumLabel.hidden = _unitLabel.hidden = _priceLabel.hidden = _priceNumLabel.hidden = _rightArrowImgView.hidden = (_mode == RecommendModeNoRecommendInfo ? YES : NO);
    
    _lineView2.frame = CGRectMake(padding, _bottomView.height - 0.5, self.width - 2*padding, 0.5);
    
    if (_mode != RecommendModeShowAll) {
        _topView.hidden = YES;
        _lineView.hidden = YES;
        _bottomView.top = 0;
    } else {
        _topView.hidden = NO;
        _lineView.hidden = NO;
        _bottomView.top = _lineView.bottom;
    }
}

- (void)setItem:(TRecommendItem *)item {
    _item = item;
    if (!_item.locationName || [_item.locationName isEqualToString:@""]) {
        _item.locationName = @"我的位置";
    }
    
    //top
    if (_mode == RecommendModeShowAll) {
        _nearLabel.text = [NSString stringWithFormat:@"%@附近", _item.locationName];
        
        NSMutableString* time = [[NSMutableString alloc] initWithString:@"预计"];
        if (item.hour != 0)
            [time appendFormat:@"%d小时", item.hour];
        [time appendFormat:@"%d分钟", item.minite];
        [time appendString:@"的车位"];
        if ([time isEqualToString:@"预计0分钟的车位"])
            time = [[NSMutableString alloc] initWithString:@"车位"];
        _freeInfoHeadLabel.text = time;
        
        _freeInfoLabel.text = item.freeinfo;
        
        //紧张 颜色
        [self updateColor:_freeInfoLabel.text];
        
        //包月 预定
        if ([item.monthids count] == 0) {
            _monthLabel.hidden = YES;
            _monthButton.hidden = YES;
        } else {
            [_monthButton setTitle:[NSString stringWithFormat:@"包月(%d)", [item.monthids count]] forState:UIControlStateNormal];
            _monthLabel.hidden = NO;
            _monthButton.hidden = NO;
        }
        if ([item.bookids count] == 0) {
            _bookButton.hidden = YES;
        } else {
            [_bookButton setTitle:[NSString stringWithFormat:@"预定(%d)", [item.bookids count]] forState:UIControlStateNormal];
            _bookButton.hidden = NO;
        }
    }
    
    //bottom
    if (_mode == RecommendModeNoRecommendInfo) {
        _parkLabel.text = [NSString stringWithFormat:@"%@ 附近500米内暂无停车宝停车场哦!", _item.locationName];
        _payImgView.hidden = _monthImgView.hidden = YES;
    } else {
        _parkLabel.text = [NSString stringWithFormat:@"%@%@",  _mode != RecommendModeOnlyShowBottom? @"推荐:" : @"", item.name];
        _freeNumLabel.text = item.freeNum;
        _priceNumLabel.text = item.price;
        _payImgView.hidden = [item.epay isEqualToString:@"1"] ? NO : YES;
        _monthImgView.hidden = [item.monthlypay isEqualToString:@"1"] ? NO : YES;
        if ([_priceNumLabel.text isEqualToString:@"0"])
            _priceNumLabel.text = @"暂无价格信息";
    }
    [self layoutSubviews];
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

- (void)handleTapGestrue:(UITapGestureRecognizer*)gesture {
    if (_delegate && [_delegate respondsToSelector:@selector(recommendDetailButtonTouched)]) {
        [_delegate recommendDetailButtonTouched];
    }
}

@end
