//
//  TAccountDetailViewController.m
//  TingCheBao_user
//
//  Created by apple on 14-9-12.
//  Copyright (c) 2014年 zhenLaiDian. All rights reserved.
//

#import "TAccountDetailViewController.h"
#import "CVAPIRequestModel.h"
#import "TAPIUtility.h"
#import "TAccountDetailItem.h"
#import "TAccountDetailCell.h"
#import "UIScrollView+SVPullToRefresh.h"
#import "TSegmentControl.h"

@interface TAccountDetailViewController ()<UITableViewDataSource, UITableViewDelegate, CVAPIModelDelegate>

@property(nonatomic, retain) TSegmentControl* segmentControl;

@property(nonatomic, retain) UITableView* tableView;
@property(nonatomic, retain) UILabel* alertLabel;
@property(nonatomic, retain) NSMutableArray* items;
@property(nonatomic, assign) int page;
@property(nonatomic, retain) CVAPIRequest* request;

@end

@implementation TAccountDetailViewController

-(id)init {
    if (self = [super init]) {
        _segmentControl = [[TSegmentControl alloc] initWithItems:@[@"全部", @"充值", @"消费"]];
        _segmentControl.backgroundColor = [UIColor whiteColor];
        _segmentControl.frame = CGRectMake(0, 5, self.view.width, 40);
        [_segmentControl addTarget:self action:@selector(segmentControlChanged:)];
        
        _tableView = [[UITableView alloc] initWithFrame:CGRectMake(0, _segmentControl.bottom, self.view.width, self.view.height - _segmentControl.bottom - 20) style:UITableViewStylePlain];
        _tableView.backgroundColor = RGBCOLOR(236, 236, 236);
        _tableView.delegate = self;
        _tableView.dataSource = self;
        _tableView.tableFooterView = [[UIView alloc] initWithFrame:CGRectMake(0, 0, 10, 50)];
        _tableView.allowsSelection = NO;
        _tableView.rowHeight = 80;
        //下拉刷新
        __unsafe_unretained TAccountDetailViewController* weakSelf = self;
        __unsafe_unretained UITableView* weakTableView = self.tableView;
        [self.tableView addPullToRefreshWithActionHandler:^{
            [weakSelf handlePullToRefresh:weakTableView.pullToRefreshView];
        } position:SVPullToRefreshPositionBottom];
        [self.tableView.pullToRefreshView setTitle:@"下拉刷新" forState:SVPullToRefreshStateAll];
        
        _alertLabel = [[UILabel alloc] initWithFrame:_tableView.frame];
        _alertLabel.backgroundColor = [UIColor clearColor];
        _alertLabel.text = @"没有交易记录哦~";
        _alertLabel.textAlignment = NSTextAlignmentCenter;
        _alertLabel.hidden = YES;
        
        [self.view addSubview:_segmentControl];
        [self.view addSubview:_tableView];
        [self.view addSubview:_alertLabel];
    }
    return self;
}

- (void)viewDidLoad
{
    [super viewDidLoad];
    // Do any additional setup after loading the view.
    self.view.backgroundColor = RGBCOLOR(235, 235, 235);
    self.titleView.text = @"帐户明细";
    _items = [NSMutableArray array];
    _page = 1;
}

- (void)viewWillAppear:(BOOL)animated {
    [super viewWillAppear:animated];
    [self requestDetailInfo:1];
}

- (void)viewDidAppear:(BOOL)animated {
    [super viewDidAppear:animated];
}

- (void)viewWillDisappear:(BOOL)animated {
    [super viewWillDisappear:animated];
    [_request cancel];
}

#pragma  mark private

//refresh pull down
- (void)handlePullToRefresh:(SVPullToRefreshView*)refreshView {
    [self requestDetailInfo:_page + 1];
}

- (void)segmentControlChanged:(UISegmentedControl*)segment {
    [self requestDetailInfo:1];
}

- (void)requestDetailInfo:(NSInteger)page {
    
    NSString* type = @"";
    if (_segmentControl.selectedIndex == 0) {
        type = @"2";
    } else if (_segmentControl.selectedIndex == 1) {
        type = @"0";
    } else {
        type = @"1";
    }
    NSString* apiPath = [NSString stringWithFormat:@"carowner.do?action=accountdetail&mobile=%@&type=%@&page=%d", [[NSUserDefaults standardUserDefaults] objectForKey:save_phone], type, page];
//    NSString* apiPath = [NSString stringWithFormat:@"carowner.do?action=accountdetail&mobile=%@&type=%@&page=%d", @"15375242041", type, page];
    _request = [[CVAPIRequest alloc] initWithAPIPath:apiPath];
    _request.hud = _page == 1 ? [MBProgressHUD showHUDAddedTo:self.view animated:YES] : nil;
    
    [self.model sendRequest:_request completion:^(NSDictionary *result, NSError *error) {
        [_tableView.pullToRefreshView stopAnimating];
        if (!result)
            return;
        _page = page;
        
        if (page != 1 && [result count] == 0) {
            [TAPIUtility alertMessage:@"已到最后一页"];
            return;
        }
        if (page == 1) {
            [_items removeAllObjects];
        }
        for (NSDictionary *object in result) {
            TAccountDetailItem* item = [TAccountDetailItem getItemFromDictionary:object];
            [_items addObject:item];
        }
        if ([_items count] == 0) {
            _alertLabel.hidden = NO;
        } else {
            _alertLabel.hidden = YES;
        }
        if (page == 1)
            _tableView.contentOffset = CGPointMake(0, 0);
        [_tableView reloadData];
    }];
}


#pragma mark UITableViewDataSource

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    return [_items count];
}

- (UITableViewCell*)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    static NSString* identifier = @"settingCell";
    TAccountDetailCell* cell = [_tableView dequeueReusableCellWithIdentifier:identifier];
    if (!cell) {
        cell = [[TAccountDetailCell alloc] initWithStyle:UITableViewCellStyleValue1 reuseIdentifier:identifier];
    }
    [cell setItem:[_items objectAtIndex:indexPath.row]];
    return cell;
}

@end
