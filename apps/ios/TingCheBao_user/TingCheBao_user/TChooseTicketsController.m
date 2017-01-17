//
//  TChooseTicketsController.m
//  TingCheBao_user
//
//  Created by apple on 15/4/27.
//  Copyright (c) 2015年 zhenLaiDian. All rights reserved.
//

#import "TChooseTicketsController.h"
#import "CVAPIRequestModel.h"
#import "TAPIUtility.h"
#import "TChooseTicketItem.h"
#import "TChooseTicketsCell.h"
#import "TRechargeWaysViewController.h"


@interface TChooseTicketsController ()<UITableViewDataSource, UITableViewDelegate>

@property(nonatomic, retain) UITableView* tableView;
@property(nonatomic, retain) NSMutableArray* items;
@property(nonatomic, retain) CVAPIRequest* request;

@property(nonatomic, retain) UIImageView* imgView;
@property(nonatomic, retain) UILabel* alertLabel;

@end

@implementation TChooseTicketsController

- (id)init {
    if (self = [super init]) {
        _tableView = [[UITableView alloc] initWithFrame:CGRectMake(0, 0, self.view.width, self.view.height) style:UITableViewStyleGrouped];
        _tableView.backgroundColor = [UIColor clearColor];
        _tableView.delegate = self;
        _tableView.dataSource = self;
        _tableView.rowHeight = 145;
        _tableView.tableFooterView = [[UIView alloc] initWithFrame:CGRectMake(0, 0, 10, 10)];
        _tableView.separatorStyle = UITableViewCellSeparatorStyleNone;
        
        _imgView = [[UIImageView alloc] initWithImage:[UIImage imageNamed:@"img_page_null.png"]];
        _imgView.frame = CGRectMake((self.view.width - 200)/2, 100, 200, 100);
        
        _alertLabel = [[UILabel alloc] initWithFrame:CGRectMake(0, _imgView.bottom + 4, self.view.width, 30)];
        _alertLabel.backgroundColor = [UIColor clearColor];
        _alertLabel.text = @"没有可用的停车券哦~";
        _alertLabel.textColor = noData_alert_color;
        _alertLabel.textAlignment = NSTextAlignmentCenter;
        
        _tableView.hidden = YES;
        _imgView.hidden = YES;
        _alertLabel.hidden = YES;
        
        [self.view addSubview:_tableView];
        [self.view addSubview:_imgView];
        [self.view addSubview:_alertLabel];
    }
    return self;
}

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view.
    self.titleView.text = @"选择停车券";
    self.view.backgroundColor = RGBCOLOR(236, 236, 236);
    _items = [NSMutableArray array];
}

- (void)viewWillAppear:(BOOL)animated {
    [super viewWillAppear:animated];
    [self requestMonthInfo];
}

- (void)viewWillDisappear:(BOOL)animated {
    [super viewWillDisappear:animated];
    [_request cancel];
    for (UIViewController* vc  in self.navigationController.viewControllers) {
        NSLog(@"%@", vc);
    }
    
    UIViewController* lastVc = [self.navigationController.viewControllers objectAtIndex:[self.navigationController.viewControllers count] - 1];
    if ([lastVc isKindOfClass:[TRechargeWaysViewController class]]) {
        TChooseTicketItem* selectItem = nil;
        for (TChooseTicketItem* item in _items) {
            if ([item.ticketId isEqualToString:_ticketId]) {
                selectItem = item;
                break;
            }
        }
        ((TRechargeWaysViewController*)lastVc).ticketItem = selectItem;
    }
}

#pragma mark private

- (void)requestMonthInfo {
    
    NSString* apiPath = [NSString stringWithFormat:@"carinter.do?action=usetickets&mobile=%@&total=%@&orderid=%@&uid=%@&preid=%@&ptype=%@&utype=2",[[NSUserDefaults standardUserDefaults] objectForKey:save_phone], _money, _orderId ? _orderId : @"", _collectorId ? _collectorId : @"", _ticketId ? _ticketId : @"", _isReward ? @"4" : @""];
    _request = [[CVAPIRequest alloc] initWithAPIPath:apiPath];
    _request.hud = [MBProgressHUD showHUDAddedTo:self.view animated:YES];
    
    [self.model sendRequest:_request completion:^(NSDictionary *result, NSError *error) {
        if (!result)
            return;
        [_items removeAllObjects];
        for (NSDictionary *object in result) {
            TChooseTicketItem* item = [TChooseTicketItem getItemFromDic:object];
            [_items addObject:item];
        }
        if ([_items count] == 0) {
            _alertLabel.hidden = _imgView.hidden = NO;
            _tableView.hidden = YES;
        } else {
            _alertLabel.hidden = _imgView.hidden = YES;
            _tableView.hidden = NO;
        }
        [_tableView reloadData];
    }];
}

#pragma mark UITableViewDataSource

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    return [_items count];
}

- (UITableViewCell*)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    static NSString* identifier = @"cell";
    TChooseTicketsCell* cell = [_tableView dequeueReusableCellWithIdentifier:identifier];
    if (!cell) {
        cell = [[TChooseTicketsCell alloc] initWithStyle:UITableViewCellStyleValue1 reuseIdentifier:identifier];
    }
    TChooseTicketItem* item = [_items objectAtIndex:indexPath.row];
    [cell setItem:[_items objectAtIndex:indexPath.row]];
    cell.userInteractionEnabled = [item.iscanuse isEqualToString:@"0"] ? NO : YES;
    
    if ([item.ticketId isEqualToString:_ticketId]) {
        [tableView selectRowAtIndexPath:indexPath animated:NO scrollPosition:0];
    }
    return cell;
}

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath {
    TChooseTicketItem* item = [_items objectAtIndex:indexPath.row];
    TChooseTicketsCell* cell = (TChooseTicketsCell*)[_tableView cellForRowAtIndexPath:indexPath];
    if ([_ticketId isEqualToString:item.ticketId]) {
        _ticketId = @"";
        [_tableView deselectRowAtIndexPath:indexPath animated:NO];
    } else {
        _ticketId = item.ticketId;
    }
    
    [self.navigationController popViewControllerAnimated:YES];

}

- (NSString*)tableView:(UITableView *)tableView titleForHeaderInSection:(NSInteger)section {
    return @"选择停车券";
}

/*
#pragma mark - Navigation

// In a storyboard-based application, you will often want to do a little preparation before navigation
- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender {
    // Get the new view controller using [segue destinationViewController].
    // Pass the selected object to the new view controller.
}
*/

@end
