//
//  TSearchView.m
//  TingCheBao_user
//
//  Created by apple on 15/3/9.
//  Copyright (c) 2015年 zhenLaiDian. All rights reserved.
//

#import "TSearchView.h"
#import "TAPIUtility.h"

#define topHeight 30

@interface TSearchView()

@property(nonatomic, retain) UIView* topView;
@property(nonatomic, retain) UILabel* promptLabel;
@property(nonatomic, retain) UILabel* promptLabel2;
@property(nonatomic, retain) UIButton* promptButton;
@property(nonatomic, retain) UIButton* promptButton2;
@property(nonatomic, retain) UIImageView* arrowImgView;

@property(nonatomic, retain) UIButton* payButton;
@property(nonatomic, retain) UIImageView* payImgView;
@property(nonatomic, retain) UILabel* payLabel;

@property(nonatomic, retain) UIButton* allButton;
@property(nonatomic, retain) UIImageView* allImgView;
@property(nonatomic, retain) UILabel* allLabel;


@property(nonatomic, retain) UIButton* uploadButton;
@property(nonatomic, retain) UIImageView* uploadImgView;
@property(nonatomic, retain) UILabel* uploadLabel;

@property(nonatomic, retain) UIButton* searchButton;
@property(nonatomic, retain) UIImageView* searchImgView;
@property(nonatomic, retain) UILabel* searchLabel;



@property(nonatomic, retain) UIView* bottomView;
@property(nonatomic, retain) UIView* lineView;
@property(nonatomic, retain) UIButton* redButton;
@property(nonatomic, retain) UIButton* redNumberButton;
@property(nonatomic, retain) UIButton* gameButton;
@property(nonatomic, retain) UIButton* quickPayButton;

@property(nonatomic, assign) CGFloat orginTop;//normal mode下 view.top值
@property(nonatomic, assign) CGFloat orginHeight;


@end
@implementation TSearchView

- (id)initWithFrame:(CGRect)frame {
    if (self = [super initWithFrame:frame]) {
        self.backgroundColor = [UIColor whiteColor];
        
        //记录初始位置
        _orginTop = self.top;
        _orginHeight = self.height;
        _redNumber = @(0);
        
        //------topView--begin------
        _topView = [[UIView alloc] init];
        _topView.backgroundColor = RGBCOLOR(175, 209, 187);
        _topView.clipsToBounds = YES;
        
        //mode_upload
        _promptLabel = [[UILabel alloc] init];
        _promptLabel.text = @"当前位置车场较少,您可以选择上传一个";
        _promptLabel.textColor = [UIColor whiteColor];
        _promptLabel.font = [UIFont systemFontOfSize:12];
        
        _arrowImgView = [[UIImageView alloc] initWithImage:[UIImage imageNamed:@"down_arrow.png"]];
        _arrowImgView.backgroundColor = [UIColor clearColor];
        
        _promptButton = [UIButton buttonWithType:UIButtonTypeCustom];
        [_promptButton setImage:[UIImage imageNamed:@"request_help.png"] forState:UIControlStateNormal];
        [_promptButton addTarget:self action:@selector(buttonTouched:) forControlEvents:UIControlEventTouchUpInside];
        [_promptButton setImageEdgeInsets:UIEdgeInsetsMake(8, 8, 8, 8)];
        
        //mode_money
        _promptLabel2 = [[UILabel alloc] init];
        _promptLabel2.text = @"可用信用额度不足10元，请尽快充值还款";
        _promptLabel2.textColor = [UIColor whiteColor];
        _promptLabel2.font = [UIFont systemFontOfSize:12];
        
        _promptButton2 = [UIButton buttonWithType:UIButtonTypeCustom];
        [_promptButton2 setTitle:@"去充值" forState:UIControlStateNormal];
        _promptButton2.titleLabel.font = [UIFont systemFontOfSize:12];
        [_promptButton2 setTitleColor:[UIColor whiteColor] forState:UIControlStateNormal];
        [_promptButton2 addTarget:self action:@selector(buttonTouched:) forControlEvents:UIControlEventTouchUpInside];
        _promptButton2.layer.borderColor = [UIColor whiteColor].CGColor;
        _promptButton2.layer.borderWidth = 1;
        _promptButton2.layer.cornerRadius = 4;
        _promptButton2.clipsToBounds = YES;
        
        //------topView--end
        
        _payButton = [UIButton buttonWithType:UIButtonTypeCustom];
        [_payButton addTarget:self action:@selector(buttonTouched:) forControlEvents:UIControlEventTouchUpInside];
        _payButton.tag = 1;
        
        _payImgView = [[UIImageView alloc] initWithImage:[UIImage imageNamed:@""]];

        _payLabel = [[UILabel alloc] init];
        _payLabel.text = @"可支付车场";
        _payLabel.textColor = green_color;
        _payLabel.textAlignment = NSTextAlignmentCenter;
        
        [_payButton addSubview:_payImgView];
        [_payButton addSubview:_payLabel];
        
        
        
        _allButton = [UIButton buttonWithType:UIButtonTypeCustom];
        [_allButton addTarget:self action:@selector(buttonTouched:) forControlEvents:UIControlEventTouchUpInside];
        _allButton.tag = 0;
        
        _allImgView = [[UIImageView alloc] initWithImage:[UIImage imageNamed:@""]];

        _allLabel = [[UILabel alloc] init];
        _allLabel.text = @"全部车场";
        _allLabel.textColor = green_color;
        _allLabel.textAlignment = NSTextAlignmentCenter;
        
        [_allButton addSubview:_allImgView];
        [_allButton addSubview:_allLabel];
        
        _uploadButton = [UIButton buttonWithType:UIButtonTypeCustom];
        [_uploadButton addTarget:self action:@selector(buttonTouched:) forControlEvents:UIControlEventTouchUpInside];
        _uploadButton.hidden = YES;
        
        _uploadImgView = [[UIImageView alloc] initWithImage:[UIImage imageNamed:@"ic_map_upload.png"]];
        
        _uploadLabel = [[UILabel alloc] init];
        _uploadLabel.text = @"上传车场";
        _uploadLabel.textColor = green_color;
        _uploadLabel.textAlignment = NSTextAlignmentCenter;
        _uploadLabel.font = [UIFont systemFontOfSize:15];
        
        [_uploadButton addSubview:_uploadImgView];
        [_uploadButton addSubview:_uploadLabel];
        
        _searchButton = [UIButton buttonWithType:UIButtonTypeCustom];
        _searchButton.titleLabel.font = [UIFont systemFontOfSize:15];
//        [_searchButton setBackgroundImage:[TAPIUtility imageWithColor:[UIColor whiteColor]] forState:UIControlStateNormal];
        _searchButton.layer.borderColor = green_color.CGColor;
        _searchButton.layer.borderWidth = 2;
        _searchButton.layer.cornerRadius = 5;
        
        [_searchButton addTarget:self action:@selector(buttonTouched:) forControlEvents:UIControlEventTouchUpInside];
        
        _searchImgView = [[UIImageView alloc] initWithImage:[UIImage imageNamed:@"search.png"]];

        _searchLabel = [[UILabel alloc] init];
        _searchLabel.text = @"搜索目的地";
        _searchLabel.textColor = [UIColor grayColor];
        _searchLabel.font = [UIFont systemFontOfSize:14];
        
        [_searchButton addSubview:_searchImgView];
        [_searchButton addSubview:_searchLabel];
        
        
        _addButton = [UIButton buttonWithType:UIButtonTypeCustom];
        [_addButton setImage:[UIImage imageNamed:@"add_green2.png"] forState:UIControlStateNormal];
        _addButton.imageEdgeInsets = UIEdgeInsetsMake(5, 5, 5, 5);
        [_addButton addTarget:self action:@selector(buttonTouched:) forControlEvents:UIControlEventTouchUpInside];
        
        
        //bottomView------begin-------------
        _bottomView = [[UIView alloc] init];
        _bottomView.backgroundColor = RGBCOLOR(236, 236, 236);
        
        _lineView = [[UIView alloc] init];
        _lineView.backgroundColor = RGBCOLOR(212, 212, 212);
        
        _redButton = [self createButtonWithTitle:@"领取红包" imgName:@"red_package2.png"];
        _redNumberButton = [UIButton buttonWithType:UIButtonTypeCustom];
        [_redNumberButton setBackgroundImage:[UIImage imageNamed:@"redNumber.png"] forState:UIControlStateNormal];
        _redNumberButton.titleLabel.font = [UIFont systemFontOfSize:10];
        
        _gameButton = [self createButtonWithTitle:@"打飞机" imgName:@"tipPlane.png"];
        _quickPayButton = [self createButtonWithTitle:@"快捷支付" imgName:@"quickPay.png"];
        
        
        [_bottomView addSubview:_lineView];
        [_bottomView addSubview:_redButton];
        [_bottomView addSubview:_redNumberButton];
        [_bottomView addSubview:_gameButton];
        [_bottomView addSubview:_quickPayButton];
        
        
        [self addSubview:_topView];
        [self addSubview:_payButton];
        [self addSubview:_allButton];
        [self addSubview:_uploadButton];
        [self addSubview:_searchButton];
        [self addSubview:_addButton];
        [self addSubview:_bottomView];
    }
    return self;
}

- (void)layoutSubviews {
    [super layoutSubviews];
    
    _topView.frame = CGRectMake(0, 0, self.width, topHeight);
    _promptLabel.frame = CGRectMake(10, 0, 210, topHeight);
    _promptButton.frame = CGRectMake(_promptLabel.right, 0, topHeight, topHeight);
    _arrowImgView.frame = CGRectMake(self.width - 50, 8, 12, 16);
    _promptLabel2.frame = CGRectMake(10, 0, 250, topHeight);
    _promptButton2.frame = CGRectMake(self.width - 60, (topHeight - 22)/2, 50, 22);
    
    if (_mode != SearchViewMode_normal) {
        //移除topView所有subView
        for (UIView* subView in _topView.subviews) {
            [subView removeFromSuperview];
        }
        if (_mode == SearchViewMode_upload) {
            [_topView addSubview:_promptLabel];
            [_topView addSubview:_promptButton];
            [_topView addSubview:_arrowImgView];
            
            _uploadButton.hidden = NO;
            
        } else if (_mode == SearchViewMode_money) {
            [_topView addSubview:_promptLabel2];
            [_topView addSubview:_promptButton2];
            
            _uploadButton.hidden = YES;
        }
    } else if (_mode == SearchViewMode_normal) {
        _topView.height = 0;
        _uploadButton.hidden = YES;
    }
    
    CGFloat width = _mode == SearchViewMode_upload ? self.width - 80 : self.width;
    _payButton.frame = CGRectMake((width - 100*2)/3, _topView.bottom + 10, 100, 50);
    CGFloat imgWidth = _payButton.tag == 1 ? 25 : 10;
    CGFloat labelHeight = 25;
    _payLabel.font = [UIFont systemFontOfSize:_payButton.tag == 1 ? 15 : 12];
    _payLabel.textColor = _payButton.tag == 1 ? green_color : [UIColor grayColor];
    _payImgView.image = [UIImage imageNamed:_payButton.tag == 1 ? @"park_pay.png" : @"park_circle.png"];
    _payImgView.frame = CGRectMake((100 - imgWidth)/2, (25-imgWidth)/2, imgWidth, imgWidth);
    _payLabel.frame = CGRectMake(0, 25, 100, labelHeight);
    
    _allButton.frame = CGRectMake(_payButton.right + (width - 100*2)/3, _payButton.top, _payButton.width, _payButton.height);
    imgWidth = _allButton.tag == 1 ? 25 : 10;
    _allLabel.font = [UIFont systemFontOfSize:_allButton.tag == 1 ? 15 : 12];
    _allLabel.textColor = _allButton.tag == 1 ? green_color : [UIColor grayColor];
    _allImgView.image = [UIImage imageNamed:_allButton.tag == 1 ? @"park_all.png" : @"park_circle.png"];
    _allImgView.frame = CGRectMake((100 - imgWidth)/2, (25-imgWidth)/2, imgWidth, imgWidth);
    _allLabel.frame = CGRectMake(0, 25, 100, labelHeight);
    
    
    _uploadButton.frame = CGRectMake(self.width - 80, _payButton.top, 70, 50);
    _uploadImgView.frame = CGRectMake((_uploadButton.width - 25)/2, 0, 25, 25);
    _uploadLabel.frame = CGRectMake(0, 25, _uploadButton.width, 25);
    
//    _searchButton.frame = CGRectMake(10, _payButton.bottom + 5, self.width - 140, 40);
    _searchButton.frame = CGRectMake(10, _payButton.bottom + 5, self.width - 10 - 60, 40);
    _searchImgView.frame = CGRectMake(10, 12, 16, 16);
    _searchLabel.frame = CGRectMake(30, 0, _searchButton.width - 30, 40);
//    _addButton.frame = CGRectMake(self.width - 45, _searchButton.top + (40 - 30)/2, 30, 30);
    _addButton.frame = CGRectMake(self.width - 45, _searchButton.top, 40, 40);
    
    _bottomView.frame = CGRectMake(0, (self.height - 100), self.width, 100);
    _lineView.frame = CGRectMake(0, 0, self.width, 1);
    CGFloat padding = (self.width - 60*3)/4;
    _redButton.frame = CGRectMake(padding, (_bottomView.height - 70)/2 + 5, 60, 70);
    _redNumberButton.frame = CGRectMake(_redButton.right - 20, _redButton.top - 10, 20, 20);
    _gameButton.frame = CGRectMake(_redButton.right + padding, _redButton.top, _redButton.width, _redButton.height);
    _quickPayButton.frame = CGRectMake(_gameButton.right + padding, _gameButton.top, _redButton.width, _redButton.height);
}

- (UIButton*)createButtonWithTitle:(NSString*)title imgName:(NSString*)imageName {
    UIButton* button = [UIButton buttonWithType:UIButtonTypeCustom];
    UIImageView* imgView = [[UIImageView alloc] initWithImage:[UIImage imageNamed:imageName]];
    CGFloat width = 60, height = 70, buttonWidth = 40;
    imgView.frame = CGRectMake((width - buttonWidth)/2, 0, buttonWidth, buttonWidth);
    
    UILabel* label = [[UILabel alloc] init];
    label.text = title;
    label.textColor = [UIColor grayColor];
    label.textAlignment = NSTextAlignmentCenter;
    label.font = [UIFont systemFontOfSize:15];
    label.frame = CGRectMake(0, imgView.bottom, width, height - buttonWidth);
    
    [button addSubview:imgView];
    [button addSubview:label];
    
    [button addTarget:self action:@selector(buttonTouched:) forControlEvents:UIControlEventTouchUpInside];
    
    return button;
}

- (void)buttonTouched:(UIButton*)button {
    NSInteger index = -1;
    
    if (button == _payButton && button.tag != 1) {
        _payButton.tag = 1;
        _allButton.tag = 0;
        
        index = 0;
    }
    
    if (button == _allButton && button.tag != 1) {
        _payButton.tag = 0;
        _allButton.tag = 1;
        
        index = 1;
    }
    
    if (button == _uploadButton) {
        index = 2;
    }
    
    if (button == _searchButton) {
        index = 3;
    }
    
    if (button == _promptButton) {
        index = 5;
    }
    
    if (button == _promptButton2) {
        index = 6;
    }
    
    if (button == _addButton) {
        //展示或收缩底部
        index = 7;
        if (button.tag == 0) {
            button.tag = 1;
            [button setImage:[UIImage imageNamed:@"close_green.png"] forState:UIControlStateNormal];
        } else {
            button.tag = 0;
            [button setImage:[UIImage imageNamed:[_redNumber integerValue] > 0 ? @"add_green_red.png" :@"add_green2.png"] forState:UIControlStateNormal];
        }
    }
    
    if (button == _redButton) {
        index = 8;
        //关闭底部
        [self buttonTouched:_addButton];
    }
    
    if (button == _gameButton) {
        index = 9;
        //关闭底部
        [self buttonTouched:_addButton];
    }
    
    if (button == _quickPayButton) {
        index = 10;
        //关闭底部
        [self buttonTouched:_addButton];
    }
    
    if (index != -1) {
        [self setNeedsLayout];
        
        if (_delegate && [_delegate respondsToSelector:@selector(searchViewClicked:)]) {
            [_delegate searchViewClicked:index];
        }
    }
}

#pragma mark public

- (void)setMode:(SearchViewMode)mode {
    if (mode == _mode) {
        return;
    }
    _mode = mode;
    
    if (_mode == SearchViewMode_upload || _mode == SearchViewMode_money) {
        
        self.top = _orginTop - topHeight;
        self.height = _orginHeight + topHeight;
        
        if (_mode == SearchViewMode_upload) {
            
            //闪烁动画
            [UIView animateWithDuration:0.5 animations:^{
                _arrowImgView.alpha = 0;
            } completion:^(BOOL finished) {
                [UIView animateWithDuration:0.5 animations:^{
                    _arrowImgView.alpha = 1;
                } completion:^(BOOL finished) {
                    [UIView animateWithDuration:0.5 animations:^{
                        _arrowImgView.alpha = 0;
                    } completion:^(BOOL finished) {
                        [UIView animateWithDuration:0.5 animations:^{
                            _arrowImgView.alpha = 1;
                        } completion:^(BOOL finished) {
                            
                        }];
                    }];
                }];
            }];
        }
        
    } else if (_mode == SearchViewMode_normal) {
        
        self.top = _orginTop;
        self.height = _orginHeight;
    }
    
    
    [self setNeedsLayout];
}

- (void)resetOrginTop {
    if (_mode ==  SearchViewMode_normal) {
        _orginTop = self.top;
    } else {
        _orginTop = self.top + topHeight;
    }
}

- (void)setRedNumber:(NSNumber *)redNumber {
    _redNumber = redNumber;
    if ([_redNumber integerValue] > 0) {
        [_addButton setImage:[UIImage imageNamed:@"add_green_red.png"] forState:UIControlStateNormal];
        [_redNumberButton setTitle:[_redNumber stringValue] forState:UIControlStateNormal];
        _redNumberButton.hidden = NO;
    } else {
        [_addButton setImage:[UIImage imageNamed:@"add_green2.png"] forState:UIControlStateNormal];
        _redNumberButton.hidden = YES;
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
