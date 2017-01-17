//
//  TCommentCell.m
//  TingCheBao_user
//
//  Created by apple on 14-9-11.
//  Copyright (c) 2014年 zhenLaiDian. All rights reserved.
//

#import "TCommentCell.h"

#import "DTAttributedTextView.h"
#import "DTLazyImageView.h"
#import "DTTextBlock.h"
#import "NSAttributedString+HTML.h"
#import "DTImageTextAttachment.h"
#import "DTLinkButton.h"
#import "TAPIUtility.h"
#import "TSession.h"

#define padding 20

@interface TCommentCell()<DTAttributedTextContentViewDelegate>

@property(nonatomic, retain) UILabel* numberLabel;
@property(nonatomic, retain) UILabel* dateLabel;
@property(nonatomic, retain) DTAttributedTextView* textView;

@end
@implementation TCommentCell

- (id)initWithStyle:(UITableViewCellStyle)style reuseIdentifier:(NSString *)reuseIdentifier
{
    self = [super initWithStyle:style reuseIdentifier:reuseIdentifier];
    if (self) {
        // Initialization code
        
        _numberLabel = [[UILabel alloc] init];
        _numberLabel.backgroundColor = [UIColor clearColor];
        _numberLabel.font = [UIFont systemFontOfSize:13];
        _numberLabel.textColor = [UIColor grayColor];
        
        _dateLabel = [[UILabel alloc] init];
        _dateLabel.backgroundColor = [UIColor clearColor];
        _dateLabel.font = [UIFont systemFontOfSize:13];
        _dateLabel.textColor = [UIColor grayColor];
        _dateLabel.textAlignment = NSTextAlignmentRight;
        
        _textView = [[DTAttributedTextView alloc] init];
        _textView.backgroundColor = [UIColor clearColor];
        _textView.shouldDrawImages = YES;
        _textView.shouldDrawLinks = YES;
        _textView.textDelegate = self;

        
        [self addSubview:_numberLabel];
        [self addSubview:_dateLabel];
        [self addSubview:_textView];
    }
    return self;
}

- (void)layoutSubviews {
    [super layoutSubviews];
    
    _numberLabel.frame = CGRectMake(padding, 5, 200, 25);
    _dateLabel.frame = CGRectMake(self.width - 200 - padding, 5, 200, 25);
    CGFloat suggestedWidth = 200;
    if (isIphoneNormal == NO) {
        suggestedWidth = 300;
    }
    CGSize suggestedSize = [_textView.attributedTextContentView suggestedFrameSizeToFitEntireStringConstraintedToWidth:suggestedWidth];
    _textView.frame = CGRectMake(padding, _dateLabel.bottom + 5, suggestedWidth, suggestedSize.height);
}

- (void)setItem:(TCommentItem *)item {
    _item = item;
    _numberLabel.text = item.user;
    if ([[TSession shared].carNumbers containsObject:item.oldUser]) {
        _numberLabel.text = @"我的评论";
    }
//
//    NSCalendar *myCal =  [[NSCalendar alloc]initWithCalendarIdentifier:NSGregorianCalendar];
//    unsigned units  = NSMonthCalendarUnit|NSDayCalendarUnit|NSYearCalendarUnit;
//    NSDateComponents *nowComp = [myCal components:units fromDate:[NSDate date]];
//    NSInteger nowWeekDay = [nowComp weekday];
//    
//    NSDateComponents *commentComp = [myCal components:units fromDate:[NSDate date]];
//    NSInteger commentWeekDay = [commentComp weekday];
    NSDate* date = [NSDate dateWithTimeIntervalSince1970:[item.time doubleValue]];
    _dateLabel.text = [TAPIUtility getCommentTime:date];
    
    NSMutableAttributedString* comment = [[NSMutableAttributedString alloc] initWithString:[NSString stringWithFormat:@"%@", item.info] attributes:@{NSFontAttributeName : [UIFont systemFontOfSize:15]}];
    _textView.attributedString = comment;
}
@end
