//
//  TParkMonthDetailTopCell.m
//  TingCheBao_user
//
//  Created by apple on 14-10-11.
//  Copyright (c) 2014年 zhenLaiDian. All rights reserved.
//

#import "TParkMonthDetailTopCell.h"
#import "TAPIUtility.h"
#import "UIImageView+WebCache.h"

@interface TParkMonthDetailTopCell()

@property(nonatomic, retain) UIImageView* infoImgView;
@property(nonatomic, retain) UIImageView* imgView;
@property(nonatomic, retain) UILabel* numberLabel;
@property(nonatomic, retain) UILabel* nameLabel;

@end
@implementation TParkMonthDetailTopCell

- (id)initWithStyle:(UITableViewCellStyle)style reuseIdentifier:(NSString *)reuseIdentifier {
    if (self = [super initWithStyle:style reuseIdentifier:reuseIdentifier]) {
        _imgView = [[UIImageView alloc] initWithImage:[UIImage imageNamed:@"img_monthlypay_default.png"]];
        
        _infoImgView = [[UIImageView alloc] initWithFrame:CGRectMake(0, 0, 60, 60)];
        
        _numberLabel = [[UILabel alloc] init];
        _numberLabel.backgroundColor = [[UIColor blackColor] colorWithAlphaComponent:0.7];
        _numberLabel.text = @"剩余:";
        _numberLabel.textColor = [UIColor whiteColor];
        
        _nameLabel = [[UILabel alloc] init];
        _nameLabel.backgroundColor = [[UIColor blackColor] colorWithAlphaComponent:0.7];
        _nameLabel.text = @"";
        _nameLabel.textColor = [UIColor whiteColor];
        
        [self addSubview:_imgView];
        [self addSubview:_infoImgView];
        [self addSubview:_numberLabel];
        [self addSubview:_nameLabel];
    }
    return self;
}

- (void)layoutSubviews {
    [super layoutSubviews];
    
    _infoImgView.frame = CGRectMake(0, 0, 80, 80);
    _imgView.frame = CGRectMake(0, 0, self.width, self.height);
    CGSize size = T_TEXTSIZE(_numberLabel.text, _numberLabel.font);
    _numberLabel.frame = CGRectMake(self.width - size.width - 5, 2, size.width, 30);
    _nameLabel.frame = CGRectMake(0, self.height - 30, self.width, 30);
}

- (void)setItem:(TParkMonthItem *)item {
    _item = item;
    
    if ([_item.photoUrl count] == 0)
        _imgView.image = [UIImage imageNamed:@"img_monthlypay_default.png"];
    else
        [_imgView sd_setImageWithURL:[NSURL URLWithString:[TAPIUtility getNetworkWithUrl:[item.photoUrl objectAtIndex:0]]] placeholderImage:[UIImage imageNamed:@"img_monthlypay_default.png"]];
    
    if ([_item.type isEqualToString:@"0"]) {
        _infoImgView.image = [UIImage imageNamed:@"img_monthlypay_full.png"];
    } else if ([_item.type isEqualToString:@"1"]) {
        _infoImgView.image = [UIImage imageNamed:@"img_monthlypay_night.png"];
    } else if ([_item.type isEqualToString:@"2"]) {
        _infoImgView.image = [UIImage imageNamed:@"img_monthlypay_day.png"];
    }
    
    _numberLabel.text = [NSString stringWithFormat:@"(剩余:%@)", item.number];
    _nameLabel.text = [NSString stringWithFormat:@" 名称:%@", item.name];
    
    [self layoutSubviews];
}

@end
