//
//  HideTableViewCell.m
//  Dog
//
//  Created by apple on 14-7-24.
//  Copyright (c) 2014年 zhenLaiDian. All rights reserved.
//

#import "HideTableViewCell.h"
#import "UIView+CVUIViewAdditions.h"

@interface HideTableViewCell()

@property(nonatomic, retain) UIImageView* imgView;
@property(nonatomic, retain) UIButton* aysnButton;
@property(nonatomic, retain) UIButton* getButton;

@end

@implementation HideTableViewCell

- (id)initWithStyle:(UITableViewCellStyle)style reuseIdentifier:(NSString *)reuseIdentifier
{
    self = [super initWithStyle:style reuseIdentifier:reuseIdentifier];
    if (self) {
        // Initialization code
        _imgView = [[UIImageView alloc] initWithImage:nil];
        _imgView.backgroundColor = [UIColor yellowColor];
        
        _aysnButton = [UIButton buttonWithType:UIButtonTypeSystem];
        [_aysnButton setTitle:@"同步到服务" forState:UIControlStateNormal];
        [_aysnButton setTitleColor:[UIColor whiteColor] forState:UIControlStateNormal];
        _aysnButton.titleLabel.font = [UIFont systemFontOfSize:12];
        
        _getButton = [UIButton buttonWithType:UIButtonTypeCustom];
        [_getButton setTitle:@"从服务器下载" forState:UIControlStateNormal];
        [_getButton setTitleColor:[UIColor whiteColor] forState:UIControlStateNormal];
        _getButton.titleLabel.font = [UIFont systemFontOfSize:12];
        
        [self.contentView addSubview:_imgView];
        [self.contentView addSubview:_aysnButton];
        [self.contentView addSubview:_getButton];
        
        self.contentView.backgroundColor = [UIColor grayColor];
    }
    return self;
}

- (void)layoutSubviews {
    [super layoutSubviews];
    _imgView.frame = CGRectMake(80, 10, 80, 80);
    _aysnButton.frame = CGRectMake(20, 120, 100, 30);
    _getButton.frame = CGRectMake(_aysnButton.right, _aysnButton.top, _aysnButton.width, _aysnButton.height);
}



- (void)setSelected:(BOOL)selected animated:(BOOL)animated
{
    [super setSelected:selected animated:animated];

    // Configure the view for the selected state
}

@end
