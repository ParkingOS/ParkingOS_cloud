//
//  TParkCommentViewController.m
//  TingCheBao_user
//
//  Created by apple on 15/4/17.
//  Copyright (c) 2015年 zhenLaiDian. All rights reserved.
//

#import "TParkCommentViewController.h"
#import "TCommentItem.h"
#import "TCommentCell.h"
#import "UIScrollView+SVPullToRefresh.h"
#import "TAPIUtility.h"
#import "DTAttributedTextView.h"
#import "TLoginViewController.h"
#import "TPostCommentViewController.h"
#import "TSession.h"

@interface TParkCommentViewController ()<UITableViewDelegate, UITableViewDataSource>

@property(nonatomic, retain) UITableView* tableView;
@property(nonatomic, retain) UIButton* commentButton;

@property(nonatomic, retain) UIImageView* imgView;
@property(nonatomic, retain) UILabel* alertLabel;

@property(nonatomic, retain) NSMutableArray* items;
@property(nonatomic, retain) CVAPIRequest* request;
@property(nonatomic, assign) int page;

@end

@implementation TParkCommentViewController

- (id)init {
    if (self = [super init]) {
        _tableView = [[UITableView alloc] initWithFrame:CGRectMake(0, 0, self.view.width, self.view.height)];
        _tableView.delegate = self;
        _tableView.dataSource = self;
        _tableView.tableFooterView = [[UIView alloc] initWithFrame:CGRectMake(0, 0, 10, 20)];
        _tableView.allowsSelection = NO;
        if (getOS >= 7.0)
            _tableView.separatorInset = UIEdgeInsetsZero;
        if (getOS >= 8.0)
            _tableView.layoutMargins = UIEdgeInsetsZero;
        //下拉刷新
        __unsafe_unretained TParkCommentViewController* weakSelf = self;
        __unsafe_unretained UITableView* weakTableView = self.tableView;
        [self.tableView addPullToRefreshWithActionHandler:^{
            [weakSelf handlePullToRefresh:weakTableView.pullToRefreshView];
        } position:SVPullToRefreshPositionBottom];
        [self.tableView.pullToRefreshView setTitle:@"下拉刷新" forState:SVPullToRefreshStateAll];
        
        _commentButton = [UIButton buttonWithType:UIButtonTypeCustom];
        [_commentButton setBackgroundImage:[UIImage imageNamed:@"post_comment.png"] forState:UIControlStateNormal];
        _commentButton.frame = CGRectMake(self.view.width - 50 - 10, self.view.height - 50 - 10, 50, 50);
        [_commentButton addTarget:self action:@selector(buttonTouched:) forControlEvents:UIControlEventTouchUpInside];
        
        _imgView = [[UIImageView alloc] initWithImage:[UIImage imageNamed:@"img_page_null.png"]];
        _imgView.frame = CGRectMake((self.view.width - 200)/2, 100, 200, 100);
        
        _alertLabel = [[UILabel alloc] initWithFrame:CGRectMake(0, _imgView.bottom + 4, self.view.width, 30)];
        _alertLabel.backgroundColor = [UIColor clearColor];
        _alertLabel.text = @"没有评论哦~";
        _alertLabel.textColor = noData_alert_color;
        _alertLabel.textAlignment = NSTextAlignmentCenter;
        
        _tableView.hidden = YES;
        _imgView.hidden = YES;
        _commentButton.hidden = YES;
        _alertLabel.hidden = YES;
        
        [self.view addSubview:_tableView];
        [self.view addSubview:_imgView];
        [self.view addSubview:_alertLabel];
        [self.view addSubview:_commentButton];
    }
    return self;
}

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view.
    _items = [NSMutableArray array];
    self.titleView.text = @"评论详情";
    _page = 1;
}

- (void)viewWillAppear:(BOOL)animated {
    [super viewWillAppear:animated];
    
    _page = 1;
    _tableView.contentOffset = CGPointMake(0, 0);
    [self requestCommentInfo:1];
}

- (void)viewWillDisappear:(BOOL)animated {
    [super viewWillDisappear:animated];
    [_request cancel];
}

#pragma private

//refresh pull down
- (void)handlePullToRefresh:(SVPullToRefreshView*)refreshView {
    [self requestCommentInfo:_page + 1];
}

- (void)requestCommentInfo :(int)page{
    NSString* apiPath = @"";
    if (_mode == Comment_mode_park) {
        NSString* mobile = @"";
        if ([[NSUserDefaults standardUserDefaults] objectForKey:save_phone]) {
            mobile = [[NSUserDefaults standardUserDefaults] objectForKey:save_phone];
        }
        apiPath = [NSString stringWithFormat:@"carinter.do?action=getcomment&comid=%@&page=%d&mobile=%@", _parkId, page, mobile];
    } else if (_mode == Comment_mode_collector){
        apiPath = [NSString stringWithFormat:@"carinter.do?action=pusrcomments&uid=%@&page=%d", _collectorId, page];
    }
    _request = [[CVAPIRequest alloc] initWithAPIPath:apiPath];
    _request.hud = page == 1 ? [MBProgressHUD showHUDAddedTo:self.view animated:YES] : nil;
    [self.model sendRequest:_request completion:^(NSDictionary *result, NSError *error) {
        [self.tableView.pullToRefreshView stopAnimating];
        if (!result)
            return;
        _page = page;
        
        if (page != 1 && [result count] == 0) {
            [TAPIUtility alertMessage:@"已到最后一页" success:NO toViewController:self];
            return;
        }
        if (page == 1) {
            [_items removeAllObjects];
        }
        
        for (NSDictionary* dic in result) {
            TCommentItem* item = [TCommentItem getItemWithDictionary:dic];
            [_items addObject:item];
        }
        if ([_items count] == 0 ) {
            [self updateState:YES];
        } else {
            [self updateState:NO];
        }
        [_tableView reloadData];
        
        
        //更改评论按钮状态
        BOOL hasComment = NO;
        if ([_items count]) {
            TCommentItem* item = [_items firstObject];
            if ([[TSession shared].carNumbers containsObject:item.oldUser]) {
                hasComment = YES;
            }
        }
        [_commentButton setBackgroundImage:[UIImage imageNamed:hasComment ? @"edit_comment.png" : @"post_comment.png"] forState:UIControlStateNormal];
        
    }];
}

- (void)updateState:(BOOL)isNoData {
    if (!isNoData) {
        _tableView.hidden = NO;
        _imgView.hidden = YES;
        _alertLabel.hidden = YES;
    } else {
        _tableView.hidden = YES;
        _imgView.hidden = NO;
        _alertLabel.hidden = NO;
    }
    
    if (_mode == Comment_mode_park)
        _commentButton.hidden = NO;
}

- (void)buttonTouched:(UIButton*)button {
    if (button == _commentButton) {
        if (![[NSUserDefaults standardUserDefaults] objectForKey:save_phone]) {
            //没登录的，先登录
            TLoginViewController* login = [[TLoginViewController alloc] init];
            [self.navigationController pushViewController:login animated:YES];
            return;
        }
        TPostCommentViewController* vc = [[TPostCommentViewController alloc] init];
        vc.parkId = _parkId;
        [self.navigationController pushViewController:vc animated:YES];
    }
}
#pragma mark UITableViewDataSource

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    return [_items count];
}

- (UITableViewCell*)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    static NSString* identifier = @"commentCell";
    TCommentCell* cell = [_tableView dequeueReusableCellWithIdentifier:identifier];
    if (!cell) {
        cell = [[TCommentCell alloc] initWithStyle:UITableViewCellStyleDefault reuseIdentifier:identifier];
    }
    if (getOS >= 8.0)
        cell.layoutMargins = UIEdgeInsetsZero;
    cell.item = [_items objectAtIndex:indexPath.row];
    return cell;
}

- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath {
    TCommentItem* item = [_items objectAtIndex:indexPath.row];
    DTAttributedTextView* textView = [[DTAttributedTextView alloc] init];
    
    NSMutableAttributedString* comment = [[NSMutableAttributedString alloc] initWithString:[NSString stringWithFormat:@"%@", item.info] attributes:@{NSFontAttributeName : [UIFont systemFontOfSize:15]}];
    textView.attributedString = comment;
    
    CGFloat suggestedWidth = 200;
    if (isIphoneNormal == NO) {
        suggestedWidth = 300;
    }
    CGSize suggestedSize = [textView.attributedTextContentView suggestedFrameSizeToFitEntireStringConstraintedToWidth:suggestedWidth];
    return suggestedSize.height + 40;
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
