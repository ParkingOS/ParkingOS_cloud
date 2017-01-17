//
//  TTicketViewController.m
//  TingCheBao_user
//
//  Created by apple on 14/11/3.
//  Copyright (c) 2014年 zhenLaiDian. All rights reserved.
//

#import "TTicketViewController.h"
#import "CVAPIRequestModel.h"
#import "TAPIUtility.h"
#import "TTicketItem.h"
#import "TChooseTicketsCell.h"
#import "TTicketHelpController.h"
#import "TSegmentControl.h"
#import "TTicketGameViewController.h"
#import "TBuyTicketViewController.h"

#define padding 10

@interface TTicketViewController ()<UITableViewDataSource, UITableViewDelegate>

@property(nonatomic, retain) TSegmentControl* segmentControl;

@property(nonatomic, retain) UIView* lineView;
@property(nonatomic, retain) UIButton* collectorButton;

@property(nonatomic, retain) UILabel* promptLabel;
@property(nonatomic, retain) UITableView* tableView;
@property(nonatomic, retain) UIButton* playButton;
@property(nonatomic, retain) UIButton* buyTicketButton;

@property(nonatomic, retain) UILabel* alertLabel;
@property(nonatomic, retain) UIImageView* imgView;
@property(nonatomic, retain) NSMutableArray* currentItems;
@property(nonatomic, retain) NSMutableArray* historyItems;

@property(nonatomic, retain) CVAPIRequest* request;

@end

@implementation TTicketViewController

-(id)init {
    if (self = [super init]) {
        
        _segmentControl = [[TSegmentControl alloc] initWithFrame:CGRectMake(0, 0, self.view.width, 40)];
        _segmentControl.backgroundColor = [UIColor whiteColor];
        [_segmentControl setItems:@[@"当前", @"历史"]];
        [_segmentControl addTarget:self action:@selector(segmentControlChanged:)];
        
        _promptLabel = [[UILabel alloc] initWithFrame:CGRectMake(30, _segmentControl.bottom + 10, self.view.width  - 60, 20)];
        _promptLabel.backgroundColor = [UIColor clearColor];
        _promptLabel.textAlignment = NSTextAlignmentCenter;
        _promptLabel.textColor = [UIColor orangeColor];
        _promptLabel.font = [UIFont systemFontOfSize:14];
        
        _tableView = [[UITableView alloc] initWithFrame:CGRectMake(0, _promptLabel.bottom + 5, self.view.width, self.view.height - _promptLabel.bottom - 60) style:UITableViewStylePlain];
        _tableView.backgroundColor = [UIColor clearColor];
        _tableView.delegate = self;
        _tableView.dataSource = self;
        _tableView.tableFooterView = [[UIView alloc] initWithFrame:CGRectMake(0, 0, 10, 10)];
        _tableView.allowsSelection = NO;
        _tableView.rowHeight = 135;
        _tableView.separatorStyle = UITableViewCellSelectionStyleNone;
        _tableView.backgroundColor = [UIColor clearColor];
        
        _playButton = [UIButton buttonWithType:UIButtonTypeCustom];
        [_playButton setBackgroundImage:[UIImage imageNamed:@"playGame.png"] forState:UIControlStateNormal];
        _playButton.frame = CGRectMake(self.view.width - 50 - 10, self.tableView.bottom - 50, 50, 50);
        [_playButton addTarget:self action:@selector(playButtonTouched:) forControlEvents:UIControlEventTouchUpInside];
        
        _imgView = [[UIImageView alloc] initWithImage:[UIImage imageNamed:@"img_page_null.png"]];
        _imgView.frame = CGRectMake((self.view.width - 200)/2, 100, 200, 100);
        _imgView.hidden = YES;
        
        _alertLabel = [[UILabel alloc] initWithFrame:CGRectMake(0, _imgView.bottom + 4, self.view.width, 60)];
        _alertLabel.backgroundColor = [UIColor clearColor];
        _alertLabel.text = @"您还没有停车券\n点击右上角\"?\"查看如何获得";
        _alertLabel.textAlignment = NSTextAlignmentCenter;
        _alertLabel.textColor = noData_alert_color;
        _alertLabel.numberOfLines = 2;
        _alertLabel.hidden = YES;
        
        _buyTicketButton = [UIButton buttonWithType:UIButtonTypeCustom];
        [_buyTicketButton setTitle:@"购买停车券" forState:UIControlStateNormal];
        [_buyTicketButton setTitleColor:[UIColor whiteColor] forState:UIControlStateNormal];
        _buyTicketButton.layer.cornerRadius = 5;
        _buyTicketButton.clipsToBounds = YES;
        [_buyTicketButton setBackgroundImage:[TAPIUtility imageWithColor:green_color] forState:UIControlStateNormal];
        _buyTicketButton.frame = CGRectMake(10, self.view.bottom - 40 - 5, self.view.width - 2*10, 40);
        [_buyTicketButton addTarget:self action:@selector(buyTicketButtonTouched:) forControlEvents:UIControlEventTouchUpInside];
        
        [self.view addSubview:_segmentControl];
        [self.view addSubview:_collectorButton];
        [self.view addSubview:_lineView];
        [self.view addSubview:_promptLabel];
        [self.view addSubview:_tableView];
        [self.view addSubview:_imgView];
        [self.view addSubview:_alertLabel];
        [self.view addSubview:_playButton];
        [self.view addSubview:_buyTicketButton];
    }
    return self;
}

- (void)viewDidLoad
{
    [super viewDidLoad];
    // Do any additional setup after loading the view.
    self.view.backgroundColor = [UIColor colorWithRed:236.0/255.0 green:236.0/255.0 blue:236.0/255.0 alpha:1];
    self.titleView.text = @"我的停车券";
}

- (void)viewWillAppear:(BOOL)animated {
    [super viewWillAppear:animated];
    UIButton* rightButton = [UIButton buttonWithType:UIButtonTypeCustom];
    [rightButton setBackgroundImage:[UIImage imageNamed:@"help_deepgary.png"] forState:UIControlStateNormal];
    rightButton.frame = CGRectMake(0, 0, 25, 25);
    [rightButton addTarget:self action:@selector(rightBarButtonItem:) forControlEvents:UIControlEventTouchUpInside];
    UIBarButtonItem* item = [[UIBarButtonItem alloc] initWithCustomView:rightButton];
    self.navigationItem.rightBarButtonItem = item;
    
    _currentItems = [NSMutableArray array];
    _historyItems = [NSMutableArray array];
    [self requestTicketInfo];
}

- (void)viewDidAppear:(BOOL)animated {
    [super viewDidAppear:animated];
}

- (void)viewWillDisappear:(BOOL)animated {
    [super viewWillDisappear:animated];
    [_request cancel];
}

#pragma  mark private


- (void)segmentControlChanged:(TSegmentControl*)segment {
    [self updateData];
}

- (void)requestTicketInfo {
    
    NSString* apiPath = [NSString stringWithFormat:@"carowner.do?action=gettickets&mobile=%@", [[NSUserDefaults standardUserDefaults] objectForKey:save_phone]];
    _request = [[CVAPIRequest alloc] initWithAPIPath:apiPath];
    _request.hud = [MBProgressHUD showHUDAddedTo:self.view animated:YES];
    
    [self.model sendRequest:_request completion:^(NSDictionary *result, NSError *error) {
        if (!result)
            return;
        
        [_currentItems removeAllObjects];
        [_historyItems removeAllObjects];
        
        for (NSDictionary *object in result) {
            TTicketItem* item = [TTicketItem getItemFromDic:object];
            if ([item.state isEqualToString:@"0"] && [item.exp isEqualToString:@"1"]) {
                [_currentItems addObject:item];
            }
            else {
                [_historyItems addObject:item];
            }
        }
        
        //历史券 要按时间排序 暂时注释
//        _historyItems = [NSMutableArray arrayWithArray:[TTicketItem orderByLimitday:_historyItems]];
        
        [self updateData];
    }];
}

#pragma mark private
- (void)updateData {
    //数据空时
    if (_segmentControl.selectedIndex == 0 && [_currentItems count] == 0) {
        _alertLabel.hidden = NO;
        _imgView.hidden = NO;
        _alertLabel.text = @"您还没有停车券\n点击右上角\"?\"查看如何获得";
    } else if (_segmentControl.selectedIndex == 1 && [_historyItems count] == 0){
        _alertLabel.text = @"您还没有历史停车券哦～";
        _alertLabel.hidden = NO;
        _imgView.hidden = NO;
    } else {
        _alertLabel.hidden = YES;
        _imgView.hidden = YES;
    }
    //是否显示 提示
    if (_segmentControl.selectedIndex == 0) {
        _promptLabel.text = [NSString stringWithFormat:@"您共有%d张代金券", [_currentItems count]];
        _promptLabel.hidden = NO;
        _tableView.frame = CGRectMake(0, _promptLabel.bottom + 5, self.view.width, self.view.height - _promptLabel.bottom - 60);
    } else {
        _promptLabel.hidden = YES;
        _tableView.frame = CGRectMake(0, _promptLabel.bottom + 5 - 20, self.view.width, self.view.height - _promptLabel.bottom - 60 + 20);
    }
    _tableView.contentOffset = CGPointMake(0, 0);
    [_tableView reloadData];
}

- (void)rightBarButtonItem:(UIBarButtonItem*)item {
    TTicketHelpController* helper = [[TTicketHelpController alloc] initWithName:@"停车券帮助" url:@"http://mp.weixin.qq.com/s?__biz=MzA4MTAxMzA2Mg==&mid=208427587&idx=1&sn=6cec3794e585e4d31b5079f919b01614#rd"];
    [self.navigationController pushViewController:helper animated:YES];
}

- (void)playButtonTouched:(UIButton*)button {
    NSString* url = [TAPIUtility getNetworkWithUrl:[NSString stringWithFormat:@"cargame.do?action=playgame&mobile=%@", [[NSUserDefaults standardUserDefaults] objectForKey:save_phone]]];
    TTicketGameViewController* palyVc = [[TTicketGameViewController alloc] initWithName:@"游戏" url:url];
    [self.navigationController pushViewController:palyVc animated:NO];
}

- (void)buyTicketButtonTouched:(UIButton*)button {
    TBuyTicketViewController* vc = [[TBuyTicketViewController alloc] init];
    [self.navigationController pushViewController:vc animated:YES];
}

#pragma mark UITableViewDataSource

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    if (_segmentControl.selectedIndex == 0) {
        return [_currentItems count];
    } else {
        return [_historyItems count];
    }
}

- (UITableViewCell*)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    static NSString* identifier = @"ticketCell";
    TChooseTicketsCell* cell = [_tableView dequeueReusableCellWithIdentifier:identifier];
    if (!cell) {
        cell = [[TChooseTicketsCell alloc] initWithStyle:UITableViewCellStyleValue1 reuseIdentifier:identifier];
    }
    [cell setItem2:_segmentControl.selectedIndex == 0 ? [_currentItems objectAtIndex:indexPath.row] : [_historyItems objectAtIndex:indexPath.row]];
    return cell;
}

@end
