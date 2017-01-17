//
//  TRecommendHistoryCell.m
//  TingCheBao_user
//
//  Created by apple on 14/12/29.
//  Copyright (c) 2014年 zhenLaiDian. All rights reserved.
//

#import "TRecommendHistoryCell.h"

@interface TRecommendHistoryCell()

@end
@implementation TRecommendHistoryCell

- (id)initWithStyle:(UITableViewCellStyle)style reuseIdentifier:(NSString *)reuseIdentifier {
    if (self = [super initWithStyle:style reuseIdentifier:reuseIdentifier]) {
        self.textLabel.text = @"";
        self.textLabel.font = [UIFont systemFontOfSize:14];
    }
    return self;
}

- (void)setItem:(TRecommendHistoryItem *)item {
    NSString* uin = item.uin;
    NSString* state = item.state;
    self.textLabel.text = [NSString stringWithFormat:@"已推荐收费员编号(%@),%@", uin, [state isEqualToString:@"0"] ? @"正在审核中..." : @"获得奖励30元"];
}

@end
