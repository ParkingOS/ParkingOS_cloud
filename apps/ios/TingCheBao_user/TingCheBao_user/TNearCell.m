//
//  TNearCell.m
//  TingCheBao_user
//
//  Created by apple on 15/3/11.
//  Copyright (c) 2015年 zhenLaiDian. All rights reserved.
//

#import "TNearCell.h"

#define default_gray_color [UIColor grayColor]

@interface TNearCell()

@property(nonatomic, retain) UIView* centerView;
@property(nonatomic, retain) UILabel* nameLabel;
@property(nonatomic, retain) UIView* supportedView;// 月 Y
@property(nonatomic, retain) UILabel* distanceLabel;
@property(nonatomic, retain) UILabel* addressLabel;
@property(nonatomic, retain) UILabel* freeLabel;
@property(nonatomic, retain) UIImageView* arrowImgView;//箭头

@property(nonatomic, retain) UIView* actionView;//导航 付费 详情
@property(nonatomic, retain) UIButton* gpsButton;
@property(nonatomic, retain) UIView* line1View;
@property(nonatomic, retain) UIButton* payButton;
@property(nonatomic, retain) UIView* line2View;
@property(nonatomic, retain) UIButton* detailButton;

@property(nonatomic, retain) NSArray* tagOptions;

@end
@implementation TNearCell

- (id)initWithStyle:(UITableViewCellStyle)style reuseIdentifier:(NSString *)reuseIdentifier {
    if (self = [super initWithStyle:style reuseIdentifier:reuseIdentifier]) {
        _centerView = [[UIView alloc] init];
        _centerView.backgroundColor = [UIColor whiteColor];
        _centerView.layer.borderColor = RGBCOLOR(213, 213, 213).CGColor;
        _centerView.layer.borderWidth = 0.5;
        
        _nameLabel = [[UILabel alloc] init];
        _nameLabel.text = @"上地嘉华大厦";
        _nameLabel.font = [UIFont systemFontOfSize:19];
        
        _distanceLabel = [[UILabel alloc] init];
        _distanceLabel.text = @"226m";
        _distanceLabel.font = [UIFont systemFontOfSize:14];
        _distanceLabel.textColor = default_gray_color;
        _distanceLabel.textAlignment = NSTextAlignmentRight;
        
        _addressLabel = [[UILabel alloc] init];
        _addressLabel.text = @"金额xxxx";
        _addressLabel.font = [UIFont systemFontOfSize:14];
        _addressLabel.textColor = default_gray_color;
        
        _freeLabel = [[UILabel alloc] init];
        _freeLabel.text = @"车位 2/23";
        _freeLabel.font = [UIFont systemFontOfSize:14];
        _freeLabel.textColor = default_gray_color;
        _freeLabel.textAlignment = NSTextAlignmentRight;
        
        _arrowImgView = [[UIImageView alloc] initWithImage:[UIImage imageNamed:@"arrow"]];
        
        _actionView = [[UIView alloc] init];
        _actionView.backgroundColor = RGBCOLOR(226, 242, 232);
        _actionView.clipsToBounds = YES;
        
        _gpsButton = [self createButton:@"导航"];
        
        _line1View = [[UIView alloc] init];
        _line1View.backgroundColor = green_color;
        
        _payButton = [self createButton:@"付费"];
        
        _line2View = [[UIView alloc] init];
        _line2View.backgroundColor = green_color;
        
        _detailButton = [self createButton:@"详情"];
        
        [_actionView addSubview:_gpsButton];
        [_actionView addSubview:_payButton];
        [_actionView addSubview:_detailButton];
        [_actionView addSubview:_line1View];
        [_actionView addSubview:_line2View];
        
        
        [self.centerView addSubview:_nameLabel];
        [self.centerView addSubview:_supportedView];
        [self.centerView addSubview:_distanceLabel];
        [self.centerView addSubview:_addressLabel];
        [self.centerView addSubview:_freeLabel];
        [self.centerView addSubview:_arrowImgView];
        [self.centerView addSubview:_actionView];
        
        [self addSubview:_centerView];
        
        self.clipsToBounds = YES;
        
        _tagOptions = @[@"Y", @"月"];
        
    }
    return self;
}

- (void)layoutSubviews {
    [super layoutSubviews];
    
    _centerView.frame = CGRectMake(4, 4, self.width - 2*4, self.height - 2*4);
    _nameLabel.frame = CGRectMake(5, 0, 150, 30);
    _supportedView.frame = CGRectMake(_nameLabel.right, 5, 100, 30);
    _distanceLabel.frame = CGRectMake(self.width - 80, 0, 60, 30);
    _addressLabel.frame = CGRectMake(5, _nameLabel.bottom, 200, 30);
    _freeLabel.frame = CGRectMake(self.width - 100, _nameLabel.bottom, 80, 30);
    _arrowImgView.frame = CGRectMake(self.width - 5, _nameLabel.bottom, 10, 10);
    
    _actionView.frame = CGRectMake(0, _addressLabel.bottom + 5, self.width, 30);
    _gpsButton.frame = CGRectMake(0, 0, self.width/3, 30);
    _payButton.frame = CGRectMake(_gpsButton.right, _gpsButton.top, self.width/3, 30);
    _detailButton.frame = CGRectMake(_payButton.right, _gpsButton.top, self.width/3, 30);
    _line1View.frame = CGRectMake(_gpsButton.right, _gpsButton.top + 5, 1, 20);
    _line2View.frame = CGRectMake(_payButton.right, _gpsButton.top + 5, 1, 20);
    
    _actionView.hidden = self.height < 80 ? YES : NO;
}

- (UIButton*)createButton:(NSString*)title {
    UIButton* button = [UIButton buttonWithType:UIButtonTypeCustom];
    [button setTitle:title forState:UIControlStateNormal];
    [button setTitleColor:green_color forState:UIControlStateNormal];
    button.titleLabel.font = [UIFont systemFontOfSize:14];
    [button addTarget:self action:@selector(buttonTouched:) forControlEvents:UIControlEventTouchUpInside];
    return button;
}

- (void)buttonTouched:(UIButton*) button {
    
}

- (void)reloadSupportedView {
    
//    return _supportedView;
}

- (void)setSelected:(BOOL)selected animated:(BOOL)animated {
    [super setSelected:selected animated:animated];

    // Configure the view for the selected state
}

@end
