//
//  TCollectorsListCell.m
//  TingCheBao_user
//
//  Created by apple on 15/5/11.
//  Copyright (c) 2015年 zhenLaiDian. All rights reserved.
//

#import "TCollectorsListCell.h"

#define padding 15
#define img_width 45
#define rightArrow_width 10
#define rightArrow_height 16
#define paytime_width 70

@interface TCollectorsListCell()

@property(nonatomic, retain) UIView* centerView;
@property(nonatomic, retain) UIImageView* imgView;
@property(nonatomic, retain) UILabel* nameLabel;
@property(nonatomic, retain) UILabel* addressLabel;
@property(nonatomic, retain) UIImageView* rightArrow;
@property(nonatomic, retain) UILabel* paytimeLabel;

@end
@implementation TCollectorsListCell

- (id)initWithStyle:(UITableViewCellStyle)style reuseIdentifier:(NSString *)reuseIdentifier {
    if (self = [super initWithStyle:style reuseIdentifier:reuseIdentifier]) {
        
        self.backgroundColor = [UIColor clearColor];
        
        _centerView = [[UIView alloc] init];
        _centerView.backgroundColor = [UIColor whiteColor];
        _centerView.layer.cornerRadius = 5;
        _centerView.clipsToBounds = YES;
        
        _imgView = [[UIImageView alloc] init];
        
        _nameLabel = [[UILabel alloc] init];
        
        _paytimeLabel = [[UILabel alloc] init];
        _paytimeLabel.textColor = [UIColor grayColor];
        _paytimeLabel.font = [UIFont systemFontOfSize:12];
        _paytimeLabel.text = @"";
        _paytimeLabel.textAlignment = NSTextAlignmentRight;
        
        _addressLabel = [[UILabel alloc] init];
        _addressLabel.textColor = [UIColor grayColor];
        _addressLabel.font = [UIFont systemFontOfSize:14];
        
        
        _rightArrow = [[UIImageView alloc] initWithImage:[UIImage imageNamed:@"right_arrow_light_grey.png"]];
        
        [self.centerView addSubview:_imgView];
        [self.centerView addSubview:_nameLabel];
        [self.centerView addSubview:_paytimeLabel];
        [self.centerView addSubview:_addressLabel];
        [self.centerView addSubview:_rightArrow];
        
        [self addSubview:_centerView];
    }
    return self;
}

- (void)layoutSubviews {
    [super layoutSubviews];
    
    _centerView.frame = CGRectMake(padding, 10, self.width - 2*padding, 55);
    _imgView.frame = CGRectMake(5, 5, img_width, img_width);
    _nameLabel.frame = CGRectMake(_imgView.right + 5, 5, self.centerView.width - _imgView.right - 5 - rightArrow_width - paytime_width - 6, 20);
    _paytimeLabel.frame = CGRectMake(_nameLabel.right, _nameLabel.top, paytime_width, _nameLabel.height);
    _addressLabel.frame = CGRectMake(_nameLabel.left, _nameLabel.bottom + 5, _nameLabel.width, _nameLabel.height);
    _rightArrow.frame = CGRectMake(_paytimeLabel.right + 2, (self.centerView.height - rightArrow_height)/2, rightArrow_width, rightArrow_height);
}

- (void)setItem:(TCollectorItem *)item {
    _item = item;
    _imgView.image = [UIImage imageNamed:[item.online isEqualToString:@"23"] ? @"collector.png" : @"collector_gray.png"];
    NSMutableAttributedString* attrString = [[NSMutableAttributedString alloc] initWithString:[NSString stringWithFormat:@"%@ (编码: %@)", item.name, item.collectorId]];
    [attrString addAttributes:@{NSForegroundColorAttributeName : [UIColor blackColor]} range:NSMakeRange(0, item.name.length)];
    [attrString addAttributes:@{NSForegroundColorAttributeName : [UIColor grayColor], NSFontAttributeName : [UIFont systemFontOfSize:15]} range:NSMakeRange(item.name.length, attrString.string.length - item.name.length)];
    _nameLabel.attributedText = attrString;
    
    if (![_item.paytime isEqualToString:@""]) {
        _paytimeLabel.text = [TAPIUtility getCommentTime:[NSDate dateWithTimeIntervalSince1970:[_item.paytime doubleValue]]];
    }
    _addressLabel.text = item.address;
}
@end
