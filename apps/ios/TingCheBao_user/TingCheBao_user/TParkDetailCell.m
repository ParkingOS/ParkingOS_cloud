//
//  TParkDetailCell.m
//  TingCheBao_user
//
//  Created by apple on 15/4/16.
//  Copyright (c) 2015年 zhenLaiDian. All rights reserved.
//

#import "TParkDetailCell.h"

@interface TParkDetailCell()

@end
@implementation TParkDetailCell

- (id)initWithStyle:(UITableViewCellStyle)style reuseIdentifier:(NSString *)reuseIdentifier {
    if (self = [super initWithStyle:style reuseIdentifier:reuseIdentifier]) {
        _leftLabel = [[UILabel alloc] init];
        _leftLabel.font = [UIFont systemFontOfSize:14];
        
        _rightLabel = [[UILabel alloc] init];
        _rightLabel.textColor = RGBCOLOR(148, 148, 148);
        _rightLabel.font = [UIFont systemFontOfSize:14];
        
        [self.contentView addSubview:_leftLabel];
        [self.contentView addSubview:_rightLabel];
        
    }
    return self;
}

- (void)layoutSubviews {
    [super layoutSubviews];
    
    _leftLabel.frame = CGRectMake(15, 10, 45, 24);
    _rightLabel.frame = CGRectMake(_leftLabel.right, 0, 230, self.contentView.height);
}


- (void)prepareForReuse {
    [super prepareForReuse];
    
    _rightLabel.numberOfLines = 1;
    _leftLabel.text = @"";
    _rightLabel.text = @"";
    self.userInteractionEnabled = YES;
    
}

- (void)setSelected:(BOOL)selected animated:(BOOL)animated {
    [super setSelected:selected animated:animated];
    
    // Configure the view for the selected state
}

@end
