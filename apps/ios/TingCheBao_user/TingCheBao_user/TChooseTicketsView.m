//
//  TChooseTicketsView.m
//  TingCheBao_user
//
//  Created by apple on 14/11/3.
//  Copyright (c) 2014年 zhenLaiDian. All rights reserved.
//

#import "TChooseTicketsView.h"
#import "TTicketItem.h"

@interface TChooseTicketsView()<UITableViewDataSource, UITableViewDelegate>

@property(nonatomic, retain) UIView* chooseBgView;
@property(nonatomic, retain) UILabel* chooseLabel;
@property(nonatomic, retain) UIView* lineView;
@property(nonatomic, retain) UITableView* tableView;

@end
@implementation TChooseTicketsView

- (id)initWithFrame:(CGRect)frame {
    if (self = [super initWithFrame:frame]) {
        
        _chooseBgView = [[UIView alloc] initWithFrame:self.frame];
        _chooseBgView.backgroundColor = [UIColor blackColor];
        _chooseBgView.alpha = 0.7;
        
        _chooseLabel = [[UILabel alloc] initWithFrame:CGRectMake(30, 90, self.width - 30*2, 50)];
        _chooseLabel.backgroundColor = [UIColor whiteColor];
        _chooseLabel.text = @"选择停车场";
        _chooseLabel.textColor = green_color;
        
        _lineView = [[UIView alloc] initWithFrame:CGRectMake(30, _chooseLabel.bottom, _chooseLabel.width, 2)];
        _lineView.backgroundColor = green_color;
        
        _tableView = [[UITableView alloc] initWithFrame:CGRectMake(30, _lineView.bottom, _chooseLabel.width, 200) style:UITableViewStylePlain];
        _tableView.delegate = self;
        _tableView.dataSource = self;
        _tableView.tableFooterView = [[UIView alloc] initWithFrame:CGRectMake(0, 0, 10, 10)];
        _tableView.rowHeight = 40;
        
        [self addSubview:_chooseBgView];
        [self addSubview:_chooseLabel];
        [self addSubview:_lineView];
        [self addSubview:_tableView];
    }
    return self;
}

#pragma mark UITableViewDataSource

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    return  [_items count] + 1;
}

- (UITableViewCell*)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    static NSString* identifier = @"accountCell";
    UITableViewCell* cell = [_tableView dequeueReusableCellWithIdentifier:identifier];
    if (!cell) {
        cell = [[UITableViewCell alloc] initWithStyle:UITableViewCellStyleValue1 reuseIdentifier:identifier];
    }
    if (indexPath.row == [_items count]) {
        cell.textLabel.text = @"不使用";
    } else {
        TTicketItem* item = [_items objectAtIndex:indexPath.row];
        NSAttributedString* money = [[NSAttributedString alloc] initWithString:[NSString stringWithFormat:@"%@元", item.money] attributes:@{NSFontAttributeName : [UIFont systemFontOfSize:24]}];
        NSAttributedString* time = [[NSAttributedString alloc] initWithString:[self getTimeString:item.limitday] attributes:@{NSFontAttributeName : [UIFont systemFontOfSize:17], NSForegroundColorAttributeName : [UIColor grayColor]}];
        
        NSMutableAttributedString* text = [[NSMutableAttributedString alloc] initWithAttributedString:money];
        [text appendAttributedString:time];
        cell.textLabel.attributedText = text;
    }
    return cell;
}

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath {
    TTicketItem* item = [[TTicketItem alloc] init];
    if (indexPath.row == [_items count]) {
        item.money = @"0";
    } else {
        item = [_items objectAtIndex:indexPath.row];
    }
    if (_delegate && [_delegate respondsToSelector:@selector(ticketsChoose:)]) {
        [_delegate ticketsChoose:item];
    }
}

#pragma mark private

- (void)setItems:(NSArray *)items {
    [_tableView reloadData];
}

- (NSString*)getTimeString:(NSString*)limitDay {
    NSTimeInterval longTime = [limitDay integerValue] - [[NSDate date] timeIntervalSince1970];
    int day = longTime / (3600*24);
    NSString* dayString = @"";
    if (day == 0) {
        dayString = @"(今天到期)";
    } else {
        dayString = [NSString stringWithFormat:@"(剩%d日到期)", day];
    }
    return dayString;
}

@end
