//
//  TNearCollectorCell.m
//  TingCheBao_user
//
//  Created by apple on 15/3/25.
//  Copyright (c) 2015年 zhenLaiDian. All rights reserved.
//

#import "TNearCollectorCell.h"

#define default_gray_color [UIColor grayColor]

@interface TNearCollectorCell()

@property(nonatomic, retain) UIView* centerView;
@property(nonatomic, retain) UILabel* nameLabel;
@property(nonatomic, retain) UILabel* distanceLabel;
@property(nonatomic, retain) UILabel* addressLabel;
@property(nonatomic, retain) UILabel* freeLabel;
@property(nonatomic, retain) UIImageView* arrowImgView;//箭头

@property(nonatomic, retain) UIView* actionView;//留言 打电话 付费 详情
@property(nonatomic, retain) UIButton* messageButton;
@property(nonatomic, retain) UIView* line1View;
@property(nonatomic, retain) UIButton* phoneButton;
@property(nonatomic, retain) UIView* line2View;
@property(nonatomic, retain) UIButton* payButton;
@property(nonatomic, retain) UIView* line3View;
@property(nonatomic, retain) UIButton* detailButton;

@property(nonatomic, retain) NSArray* tagOptions;

@end
@implementation TNearCollectorCell

- (id)initWithStyle:(UITableViewCellStyle)style reuseIdentifier:(NSString *)reuseIdentifier {
    if (self = [super initWithStyle:style reuseIdentifier:reuseIdentifier]) {
        _centerView = [[UIView alloc] init];
        _centerView.backgroundColor = [UIColor whiteColor];
        _centerView.layer.borderColor = RGBCOLOR(213, 213, 213).CGColor;
        _centerView.layer.borderWidth = 0.5;
        
        _nameLabel = [[UILabel alloc] init];
        _nameLabel.text = @"张三(编号 ：10799)";
        _nameLabel.font = [UIFont systemFontOfSize:19];
        
        _distanceLabel = [[UILabel alloc] init];
        _distanceLabel.text = @"226m";
        _distanceLabel.font = [UIFont systemFontOfSize:14];
        _distanceLabel.textColor = default_gray_color;
        _distanceLabel.textAlignment = NSTextAlignmentRight;
        
        _addressLabel = [[UILabel alloc] init];
        _addressLabel.text = @"上地嘉华大夏停车场";
        _addressLabel.font = [UIFont systemFontOfSize:14];
        _addressLabel.textColor = default_gray_color;
        
        _freeLabel = [[UILabel alloc] init];
        _freeLabel.text = @"服务次数:25";
        _freeLabel.font = [UIFont systemFontOfSize:14];
        _freeLabel.textColor = default_gray_color;
        _freeLabel.textAlignment = NSTextAlignmentRight;
        
        _arrowImgView = [[UIImageView alloc] initWithImage:[UIImage imageNamed:@"arrow"]];
        
        _actionView = [[UIView alloc] init];
        _actionView.backgroundColor = RGBCOLOR(226, 242, 232);
        _actionView.clipsToBounds = YES;
        
        _messageButton = [self createButton:@"留言"];
        
        _line1View = [[UIView alloc] init];
        _line1View.backgroundColor = green_color;
        
        _phoneButton = [self createButton:@"打电话"];
        
        _line2View = [[UIView alloc] init];
        _line2View.backgroundColor = green_color;
        
        _payButton = [self createButton:@"付费"];
        
        _line3View = [[UIView alloc] init];
        _line3View.backgroundColor = green_color;
        
        _detailButton = [self createButton:@"详情"];
        
        [_actionView addSubview:_messageButton];
        [_actionView addSubview:_phoneButton];
        [_actionView addSubview:_payButton];
        [_actionView addSubview:_detailButton];
        [_actionView addSubview:_line1View];
        [_actionView addSubview:_line2View];
        [_actionView addSubview:_line3View];
        
        
        [self.centerView addSubview:_nameLabel];
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
    _nameLabel.frame = CGRectMake(5, 0, 200, 30);
    _distanceLabel.frame = CGRectMake(self.width - 80, 0, 60, 30);
    _addressLabel.frame = CGRectMake(5, _nameLabel.bottom, 200, 30);
    _freeLabel.frame = CGRectMake(self.width - 100, _nameLabel.bottom, 80, 30);
    _arrowImgView.frame = CGRectMake(self.width - 5, _nameLabel.bottom, 10, 10);
    
    _actionView.frame = CGRectMake(0, _addressLabel.bottom + 5, self.width, 30);
    _messageButton.frame = CGRectMake(0, 0, self.width/4, 30);
    _phoneButton.frame = CGRectMake(_messageButton.right, _messageButton.top, self.width/4, 30);
    _payButton.frame = CGRectMake(_phoneButton.right, _messageButton.top, self.width/4, 30);
    _detailButton.frame = CGRectMake(_payButton.right, _messageButton.top, self.width/4, 30);
    
    _line1View.frame = CGRectMake(_messageButton.right, _messageButton.top + 5, 1, 20);
    _line2View.frame = CGRectMake(_phoneButton.right, _messageButton.top + 5, 1, 20);
    _line3View.frame = CGRectMake(_payButton.right, _messageButton.top + 5, 1, 20);
    
    NSLog(@"%lf--%lf", _actionView.bottom, _actionView.top);
    
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
