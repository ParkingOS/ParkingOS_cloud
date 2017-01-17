//
//  THistoryOrderViewController.m
//  TingCheBao_user
//
//  Created by apple on 14/10/18.
//  Copyright (c) 2014年 zhenLaiDian. All rights reserved.
//

#import "THistoryOrderViewController.h"
#import "CVAPIRequestModel.h"
#import "TAPIUtility.h"
#import "THistoryOrderCell.h"
#import "UIScrollView+SVPullToRefresh.h"
#import "TCurrentOrderViewController.h"

@interface THistoryOrderViewController ()<UITableViewDataSource, UITableViewDelegate, CVAPIModelDelegate>

@property(nonatomic, retain) UILabel* notifiLabel;
@property(nonatomic, retain) UITableView* tableView;
@property(nonatomic, retain) UIImageView* imgView;
@property(nonatomic, retain) UILabel* alertLabel;

@property(nonatomic, retain) NSMutableArray* items;
@property(nonatomic, assign) int page;

@property(nonatomic, assign) BOOL firstLoad;
@property(nonatomic, assign) BOOL lastPage;
@property(nonatomic, retain) CVAPIRequest* request;

@end

@implementation THistoryOrderViewController

- (id)init {
    if (self = [super init]) {
        _firstLoad = YES;
        
        _notifiLabel = [[UILabel alloc] initWithFrame:CGRectMake(10, 0, self.view.width, 40)];
        _notifiLabel.text = @"注意: 仅显示\"电子支付\"的订单(非\"现金支付\")";
        _notifiLabel.font = [UIFont systemFontOfSize:14];
        _notifiLabel.textColor = orange_color;
        
        _tableView = [[UITableView alloc] initWithFrame:CGRectMake(0, _notifiLabel.bottom, self.view.width, self.view.height - _notifiLabel.height) style:UITableViewStylePlain];
        _tableView.delegate = self;
        _tableView.dataSource = self;
        _tableView.backgroundColor = RGBCOLOR(236, 236, 236);
        _tableView.separatorStyle = UITableViewCellSeparatorStyleNone;
        _tableView.tableFooterView = [[UIView alloc] initWithFrame:CGRectMake(0, 0, 10, 50)];
        _tableView.rowHeight = 95;
        //下拉刷新
        __unsafe_unretained THistoryOrderViewController* weakSelf = self;
        __unsafe_unretained UITableView* weakTableView = self.tableView;
        [self.tableView addPullToRefreshWithActionHandler:^{
            [weakSelf handlePullToRefresh:weakTableView.pullToRefreshView];
        } position:SVPullToRefreshPositionBottom];
        [self.tableView.pullToRefreshView setTitle:@"加载更多" forState:SVPullToRefreshStateAll];
        
        _imgView = [[UIImageView alloc] initWithImage:[UIImage imageNamed:@"img_page_null.png"]];
        _imgView.frame = CGRectMake((self.view.width - 200)/2, 100, 200, 100);
        
        _alertLabel = [[UILabel alloc] initWithFrame:CGRectMake(0, _imgView.bottom + 4, self.view.width, 30)];
        _alertLabel.backgroundColor = [UIColor clearColor];
        _alertLabel.text = @"暂无历史订单哦~";
        _alertLabel.textColor = noData_alert_color;
        _alertLabel.textAlignment = NSTextAlignmentCenter;
        
        _notifiLabel.hidden = YES;
        _tableView.hidden = YES;
        _imgView.hidden = YES;
        _alertLabel.hidden = YES;
        
        [self.view addSubview:_notifiLabel];
        [self.view addSubview:_tableView];
        [self.view addSubview:_imgView];
        [self.view addSubview:_alertLabel];
    }
    return self;
}

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view.
    self.view.backgroundColor = RGBCOLOR(236, 236, 236);
    _items = [NSMutableArray array];
    _page = 1;
}

- (void)viewWillAppear:(BOOL)animated {
    [super viewWillAppear:animated];
    self.titleView.text = @"历史订单";
    if (_firstLoad == YES) {
        [self requestOrderInfo:1];
        _firstLoad = NO;
    }
}

- (void)viewWillDisappear:(BOOL)animated {
    [super viewWillDisappear:animated];
    [_request cancel];
}

#pragma mark request

//refresh pull down
- (void)handlePullToRefresh:(SVPullToRefreshView*)refreshView {
    if (_lastPage) {
        //已到最后一页
        [self.tableView.pullToRefreshView stopAnimating];
        
    } else {
        //加载下一页
        [self requestOrderInfo:_page + 1];
    }
}

- (void)requestOrderInfo:(NSInteger)page {
    
    NSString* apiPath = [NSString stringWithFormat:@"carowner.do?action=historyroder&page=%d&size=10&mobile=%@", page, [[NSUserDefaults standardUserDefaults] objectForKey:save_phone]];
    _request = [[CVAPIRequest alloc] initWithAPIPath:apiPath];
    _request.hud = page == 1 ? [MBProgressHUD showHUDAddedTo:self.view animated:YES] : nil;
    [self.model sendRequest:_request completion:^(NSDictionary *result, NSError *error) {
        [self.tableView.pullToRefreshView stopAnimating];
        if (!result)
            return;
        _page = page;
        
        if (page != 1 && [result count] == 0) {
            [self.tableView.pullToRefreshView setTitle:@"已显示全部" forState:SVPullToRefreshStateAll];
            [TAPIUtility alertMessage:@"已到最后一页" success:NO toViewController:self];
            _lastPage = YES;
            return;
        }
        if (page == 1) {
            [_items removeAllObjects];
        }
        for (NSDictionary* dic in result) {
            THistoryOrderItem* item = [THistoryOrderItem getItemFromDictionary:dic];
            [_items addObject:item];
        }
        if ([_items count] == 0 ) {
            [self updateState:YES];
        } else {
            [self updateState:NO];
        }
        [_tableView reloadData];
    }];
}

- (void)updateState:(BOOL)isNoData {
    if (!isNoData) {
        _notifiLabel.hidden = NO;
        _tableView.hidden = NO;
        _imgView.hidden = YES;
        _alertLabel.hidden = YES;
    } else {
        _notifiLabel.hidden = YES;
        _tableView.hidden = YES;
        _imgView.hidden = NO;
        _alertLabel.hidden = NO;
    }
}
#pragma mark UITableViewDataSource

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    return [_items count];;
}

- (UITableViewCell*)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    static NSString* identifier = @"orderCell";
    THistoryOrderCell* cell = [_tableView dequeueReusableCellWithIdentifier:identifier];
    if (!cell) {
        cell = [[THistoryOrderCell alloc] initWithStyle:UITableViewCellStyleDefault reuseIdentifier:identifier];
    }
    [cell setItem:[_items objectAtIndex:indexPath.row]];
    return cell;
}

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath {
    [tableView deselectRowAtIndexPath:indexPath animated:NO];
    
    THistoryOrderItem* item = [_items objectAtIndex:indexPath.row];
    TCurrentOrderViewController* vc = [[TCurrentOrderViewController alloc] init];
    vc.historyOrderid = item.orderid;
    
    [self.navigationController pushViewController:vc animated:YES];
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
