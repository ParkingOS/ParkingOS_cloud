//
//  TLeftMenuCell.m
//  TingCheBao
//
//  Created by apple on 14-7-11.
//  Copyright (c) 2014年 zhenLaiDian. All rights reserved.
//

#import "TLeftMenuCell.h"
#import "UIView+CVUIViewAdditions.h"

@implementation TLeftMenuCell

- (id)initWithStyle:(UITableViewCellStyle)style reuseIdentifier:(NSString *)reuseIdentifier
{
    self = [super initWithStyle:style reuseIdentifier:reuseIdentifier];
    if (self) {
        // Initialization code
        _imgView = [[UIImageView alloc] init];
        _imgView.contentMode = UIViewContentModeScaleAspectFit;
        
        _nameLabel = [[UILabel alloc] init];
        _nameLabel.backgroundColor = [UIColor clearColor];
        _nameLabel.font = [UIFont systemFontOfSize:14];
        
        _lineView = [[UIView alloc] init];
        _lineView.backgroundColor = RGBCOLOR(222, 222, 222);
        
        [self.contentView addSubview:_imgView];
        [self.contentView addSubview:_nameLabel];
        [self.contentView addSubview:_lineView];
    }
    return self;
}

- (void)layoutSubviews {
    [super layoutSubviews];
    
    _imgView.frame = CGRectMake(30, 14 , 24, 24);
    _nameLabel.frame = CGRectMake(_imgView.right + 20, 10, 200, 30);
    _lineView.frame = CGRectMake(_nameLabel.left, self.height - 1, self.width - _nameLabel.left, 1);
}

- (void)prepareForReuse {
    _nameLabel.textColor = RGBCOLOR(97, 97, 97);
}

- (void)setSelected:(BOOL)selected animated:(BOOL)animated
{
    [super setSelected:selected animated:animated];

    // Configure the view for the selected state
    if (selected) {
        _nameLabel.textColor = [UIColor whiteColor];
    } else {
        _nameLabel.textColor = RGBCOLOR(97, 97, 97);
    }
}

@end
