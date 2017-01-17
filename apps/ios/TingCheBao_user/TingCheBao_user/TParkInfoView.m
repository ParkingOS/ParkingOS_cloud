//
//  TParkInfoView.m
//  TingCheBao_user
//
//  Created by apple on 15/3/7.
//  Copyright (c) 2015年 zhenLaiDian. All rights reserved.
//

#import "TParkInfoView.h"
#import "TAPIUtility.h"

#define padding 10

@interface TParkInfoView()

@property(nonatomic, retain) UIImageView* paopaoImgView;
@property(nonatomic, retain) UILabel* nameLabel;
@property(nonatomic, retain) UILabel* detailLabel;
@property(nonatomic, retain) UIImageView* detailImgView;
@property(nonatomic, retain) UIButton* detailButton;
@property(nonatomic, retain) UILabel* priceLabel;
@property(nonatomic, retain) UILabel* freeNumLabel;
@property(nonatomic, retain) UIView* line1View;
@property(nonatomic, retain) UIButton* gpsButton;
@property(nonatomic, retain) UIImageView* gpsImageView;
@property(nonatomic, retain) UILabel* gpsLabel;
@property(nonatomic, retain) UIView* line2View;
@property(nonatomic, retain) UIButton* payButton;
@property(nonatomic, retain) UIImageView* payImageView;
@property(nonatomic, retain) UILabel* payLabel;

@end
@implementation TParkInfoView

- (id)initWithFrame:(CGRect)frame {
    if (self  = [super initWithFrame:frame]) {
        self.backgroundColor = [UIColor clearColor];
        
        _paopaoImgView = [[UIImageView alloc] initWithImage:[[UIImage imageNamed:@"qipao.png"] stretchableImageWithLeftCapWidth:20 topCapHeight:20]];
        
        _nameLabel = [[UILabel alloc] init];
        _nameLabel.backgroundColor = [UIColor clearColor];
        _nameLabel.text = @"上地嘉华大厦停车场";
        _nameLabel.font = [UIFont systemFontOfSize:15];
        
        _detailLabel = [[UILabel alloc] init];
        _detailLabel.backgroundColor = [UIColor clearColor];
        _detailLabel.text = @"详情";
        _detailLabel.font = [UIFont systemFontOfSize:12];
        
        _detailImgView = [[UIImageView alloc] initWithImage:[UIImage imageNamed:@"ic_arrow_right_grey.png"]];
        
        _detailButton = [UIButton buttonWithType:UIButtonTypeCustom];
        [_detailButton addTarget:self action:@selector(buttonTouched:) forControlEvents:UIControlEventTouchUpInside];
        
        _priceLabel = [[UILabel alloc] init];
        _priceLabel.backgroundColor = [UIColor clearColor];
        _priceLabel.text = @"价格: 10元/15分钟";
        _priceLabel.font = [UIFont systemFontOfSize:12];
        
        _freeNumLabel = [[UILabel alloc] init];
        _freeNumLabel.backgroundColor = [UIColor clearColor];
        _freeNumLabel.text = @"车位: 22/99";
        _freeNumLabel.textAlignment = NSTextAlignmentRight;
        _freeNumLabel.font = [UIFont systemFontOfSize:12];
        
        _line1View = [[UIView alloc] init];
        _line1View.backgroundColor = [UIColor lightGrayColor];
        
        //导航
        _gpsButton = [UIButton buttonWithType:UIButtonTypeCustom];
        [_gpsButton addTarget:self action:@selector(buttonTouched:) forControlEvents:UIControlEventTouchUpInside];
        _gpsImageView = [[UIImageView alloc] initWithImage:[UIImage imageNamed:@"navi.png"]];
        _gpsLabel = [[UILabel alloc] init];
        _gpsLabel.text = @"到这去";
        _gpsLabel.textColor = [UIColor blackColor];
        _gpsLabel.font = [UIFont systemFontOfSize:12];
        [_gpsButton addSubview:_gpsImageView];
        [_gpsButton addSubview:_gpsLabel];
        
        _line2View = [[UIView alloc] init];
        _line2View.backgroundColor = [UIColor lightGrayColor];
        
        //付车费
        _payButton = [UIButton buttonWithType:UIButtonTypeCustom];
        [_payButton addTarget:self action:@selector(buttonTouched:) forControlEvents:UIControlEventTouchUpInside];
        _payButton.clipsToBounds = YES;
        _payImageView = [[UIImageView alloc] initWithImage:[UIImage imageNamed:@"rmb_money.png"]];
        _payLabel = [[UILabel alloc] init];
        _payLabel.text = @"付车费";
        _payLabel.textColor = [UIColor blackColor];
        _payLabel.font = [UIFont systemFontOfSize:12];
        [_payButton addSubview:_payImageView];
        [_payButton addSubview:_payLabel];
        
        [self addSubview:_paopaoImgView];
        [self addSubview:_nameLabel];
        [self addSubview:_detailLabel];
        [self addSubview:_detailImgView];
        [self addSubview:_detailButton];
        [self addSubview:_priceLabel];
        [self addSubview:_freeNumLabel];
        [self addSubview:_line1View];
        [self addSubview:_gpsButton];
        [self addSubview:_line2View];
        [self addSubview:_payButton];
    }
    return self;
}

- (void)layoutSubviews {
    [super layoutSubviews];
    
    _paopaoImgView.frame = CGRectMake(0, 0, self.width, self.height);
    _nameLabel.frame = CGRectMake(padding, 0, 170, 30);
    _detailLabel.frame = CGRectMake(self.width - 40 - padding, 0, 30, 30);
    _detailImgView.frame = CGRectMake(_detailLabel.right, 10, 10, 10);
    _detailButton.frame = CGRectMake(padding, 0, self.width - 2*padding, 30);
    _priceLabel.frame = CGRectMake(padding, _nameLabel.bottom, 120, 30);
    _freeNumLabel.frame = CGRectMake(self.width - padding - 80, _priceLabel.top, 80, 30);
    _line1View.frame = CGRectMake(padding, _priceLabel.bottom, self.width - 2*padding, 0.5);
    if ([_item.epay isEqualToString:@"1"]) {
        _gpsButton.frame = CGRectMake(padding, _line1View.bottom, (self.width - 2*padding) / 2, 30);
        _gpsImageView.frame = CGRectMake(20, 7, 15, 16);
        _gpsLabel.frame = CGRectMake(45, 0, 60, 30);
        
        _line2View.frame = CGRectMake(_gpsButton.right - 1, _line1View.bottom + 5, 0.5, 30 -2*5);
        
        _payButton.frame = CGRectMake(_gpsButton.right, _gpsButton.top, _gpsButton.width, _gpsButton.height);
        _payImageView.frame = CGRectMake(25, 7, 15, 16);
        _payLabel.frame = CGRectMake(50, 0, 60, 30);
        
    } else {
        _gpsButton.frame = CGRectMake(padding, _line1View.bottom, self.width - 2*padding, 30);
        _gpsImageView.frame = CGRectMake(70, 7, 15, 16);
        _gpsLabel.frame = CGRectMake(95, 0, 60, 30);
        _line2View.frame = CGRectZero;
        _payButton.frame = CGRectZero;
    }
}

- (void)setItem:(TParkItem *)item {
    _item = item;
    
    _nameLabel.text = item.name;
    
    
    NSString* price = @"";
    NSMutableAttributedString* attr = nil;
    
    UIColor* freeColor = nil;
    if ([[TAPIUtility colorWithFree:[item.free doubleValue] total:[item.total doubleValue]] isEqualToString:@"red"]) {
        freeColor = red_color;
    } else {
        freeColor = green_color;
    }
    
    if ([item.price integerValue] < 0) {
        price = @"免费";
        attr = [[NSMutableAttributedString alloc] initWithString:@"车位:未知"];
        [attr addAttributes:@{NSForegroundColorAttributeName : gray_color} range:NSMakeRange(0, [attr.string length])];
    } else if ([item.price integerValue] > 0) {
        price = item.price;
        attr = [[NSMutableAttributedString alloc] initWithString:[NSString stringWithFormat:@"车位:%@/%@", item.free, item.total]];
        [attr addAttributes:@{NSForegroundColorAttributeName : gray_color} range:NSMakeRange(0, [attr.string length])];
        [attr addAttributes:@{NSForegroundColorAttributeName :  freeColor} range:NSMakeRange(3, [item.free length])];
    } else {
        price = @"没有价格信息";
        attr = [[NSMutableAttributedString alloc] initWithString:[NSString stringWithFormat:@"车位:%@/%@", item.free, item.total]];
        [attr addAttributes:@{NSForegroundColorAttributeName : gray_color} range:NSMakeRange(0, [attr.string length])];
        [attr addAttributes:@{NSForegroundColorAttributeName : freeColor} range:NSMakeRange(3, [item.free length])];
    }
    _priceLabel.text = [NSString stringWithFormat:@"价格:%@", price];
    _freeNumLabel.attributedText = attr;
    
    [self setNeedsLayout];
}

- (void)buttonTouched:(UIButton*)button {
    if (_delegate && [_delegate respondsToSelector:@selector(parkInfoViewWithItem:text:)]) {
        NSString* title = nil;
        if (button == _detailButton) {
            title = @"详情";
        } else if (button == _gpsButton){
            title = _gpsLabel.text;
        } else if (button == _payButton){
            title = _payLabel.text;
        }
        [_delegate parkInfoViewWithItem:_item text:title];
    }
}
/*
 // Only override drawRect: if you perform custom drawing.
 // An empty implementation adversely affects performance during animation.
 - (void)drawRect:(CGRect)rect {
 // Drawing code
 }
 */

@end
