//
//  TCollectorsListViewController.m
//  TingCheBao_user
//
//  Created by apple on 14/12/12.
//  Copyright (c) 2014年 zhenLaiDian. All rights reserved.
//

#import "TCollectorsListViewController.h"
#import "TAPIUtility.h"
#import "CVAPIRequestModel.h"
#import "TCollectorItem.h"
#import "TPayCollectorViewController.h"
#import "TReaderViewController.h"
#import "TCollectorsListCell.h"

@interface TCollectorsListViewController ()<UITableViewDataSource, UITableViewDelegate>

@property(nonatomic, retain) UITableView* tableView;
@property(nonatomic, retain) UIButton* scanButton;
@property(nonatomic, retain) NSMutableArray* items;

@property(nonatomic, assign) BOOL firstLoad;
@property(nonatomic, retain) CVAPIRequest* request;
@end

@implementation TCollectorsListViewController

- (id)init {
    if (self = [super init]) {
        _mode = TCollectorsListMode_normal;
        _firstLoad = YES;
        
        _tableView = [[UITableView alloc] initWithFrame:CGRectMake(0, 0, self.view.width, self.view.height - 70) style:UITableViewStylePlain];
        _tableView.delegate = self;
        _tableView.dataSource = self;
        _tableView.separatorStyle = UITableViewCellSeparatorStyleNone;
        _tableView.backgroundColor = [UIColor clearColor];
        _tableView.rowHeight = 65;
        
        _scanButton = [UIButton buttonWithType:UIButtonTypeCustom];
        [_scanButton setTitle:@"扫描收费员二维码付款" forState:UIControlStateNormal];
        [_scanButton setTitleColor:[UIColor whiteColor] forState:UIControlStateNormal];
        [_scanButton setTitleColor:[UIColor blackColor] forState:UIControlStateHighlighted];
        [_scanButton setBackgroundImage:[TAPIUtility imageWithColor:green_color] forState:UIControlStateNormal];
        _scanButton.layer.cornerRadius = 5;
        _scanButton.clipsToBounds = YES;
        _scanButton.frame = CGRectMake(10, self.view.height - 60, self.view.width - 2*10, 40);
        [_scanButton addTarget:self action:@selector(buttonTouched:) forControlEvents:UIControlEventTouchUpInside];
        
        [self.view addSubview:_tableView];
        [self.view addSubview:_scanButton];
        
        _tableView.hidden = _scanButton.hidden = YES;
    }
    return self;
}

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view.
    self.view.backgroundColor = RGBCOLOR(230, 230, 230);
    self.titleView.text = @"直接向收费员付款";
    _items = [NSMutableArray array];
}

- (void)viewWillAppear:(BOOL)animated {
    [super viewWillAppear:animated];
    if (_firstLoad) {
        [self requestCollectorsInfo];
        _firstLoad = NO;
    }
}

- (void)viewWillDisappear:(BOOL)animated {
    [super viewWillDisappear:animated];
    [_request cancel];
}

#pragma mark requeest

- (void)requestCollectorsInfo {
    //清除device token
    NSString* apiPath = @"";
    if (_mode == TCollectorsListMode_normal) {
         apiPath = [NSString stringWithFormat:@"carowner.do?action=getparkusers&comid=%@&mobile=%@", _parkId, [[NSUserDefaults standardUserDefaults] objectForKey:save_phone]];
    } else if (_mode == TCollectorsListMode_recent) {
         apiPath = [NSString stringWithFormat:@"carinter.do?action=quickpay&mobile=%@", [[NSUserDefaults standardUserDefaults] objectForKey:save_phone]];
    }
    
    _request = [[CVAPIRequest alloc] initWithAPIPath:apiPath];
    _request.hud = [MBProgressHUD showHUDAddedTo:self.view animated:YES];
    [self.model sendRequest:_request completion:^(NSDictionary *result, NSError *error) {
        [_items removeAllObjects];
        for (NSDictionary* object in result) {
            TCollectorItem* item = [TCollectorItem getItemFromDictionary:object];
            
            //正常选择收费员的车场名字是由外面传进来的
            if (_mode == TCollectorsListMode_normal) {
                item.address = _parkName;
            }
            [_items addObject:item];
        }
        _scanButton.hidden = NO;
        _tableView.hidden = NO;
        [_tableView reloadData];
    }];
}

#pragma mark UITableViewDataSource

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    return [_items count];
}

- (UITableViewCell*)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    static NSString* identifier = @"cell";
    TCollectorsListCell* cell = [_tableView dequeueReusableCellWithIdentifier:identifier];
    if (!cell) {
        cell = [[TCollectorsListCell alloc] initWithStyle:UITableViewCellStyleSubtitle reuseIdentifier:identifier];
    }
    TCollectorItem* item = [_items objectAtIndex:indexPath.row];
    [cell setItem:item];
    return cell;
}

- (NSString*)tableView:(UITableView *)tableView titleForHeaderInSection:(NSInteger)section {
    return _mode == TCollectorsListMode_normal ? @"请选择收费员:" : @"您最近支付过的收费员:";
}

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath {
    [tableView deselectRowAtIndexPath:indexPath animated:NO];
    TCollectorItem* item = [_items objectAtIndex:indexPath.row];
    TPayCollectorViewController* vc = [[TPayCollectorViewController alloc] init];
    vc.parkName = item.address;
    vc.collectorId = item.collectorId;
    vc.collectorName = item.name;
    [self.navigationController pushViewController:vc animated:YES];
}

- (void)buttonTouched:(UIButton*)button {
    TReaderViewController* vc = [[TReaderViewController alloc] init];
    [self.navigationController pushViewController:vc animated:YES];
}

@end
