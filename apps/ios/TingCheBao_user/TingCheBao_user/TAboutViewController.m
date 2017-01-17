//
//  TAboutViewController.m
//  TingCheBao_user
//
//  Created by apple on 14-9-12.
//  Copyright (c) 2014年 zhenLaiDian. All rights reserved.
//

#import "TAboutViewController.h"
#import "UMFeedback.h"
#import "CVAPIRequestModel.h"
#import "TAPIUtility.h"
#import "TVersionAlertView.h"
#import "TTicketHelpController.h"

@interface TAboutViewController ()<UITableViewDataSource, UITableViewDelegate, UIAlertViewDelegate>

@property(nonatomic, retain) UIImageView* imgView;
@property(nonatomic, retain) UILabel* versionLabel;
@property(nonatomic, retain) UIView* lineView;
@property(nonatomic, retain) UITableView* tableView;

@property(nonatomic, retain) UIAlertView* alert;

@property(nonatomic, retain) CVAPIRequest* request;

@end

@implementation TAboutViewController

- (id)init {
    if (self = [super init]) {
        _imgView = [[UIImageView alloc] initWithImage:[UIImage imageNamed:@"Icon.png"]];
        _imgView.frame = CGRectMake((self.view.width - 70)/2, 40, 70, 70);
        
        _versionLabel = [[UILabel alloc] initWithFrame:CGRectMake(0, _imgView.bottom + 5, self.view.width, 30)];
        _versionLabel.backgroundColor = [UIColor clearColor];
        _versionLabel.text = [NSString stringWithFormat:@"当前版本:V%@", [[[NSBundle mainBundle] infoDictionary] objectForKey:@"CFBundleShortVersionString"]];
        _versionLabel.font = [UIFont systemFontOfSize:15];
        _versionLabel.textAlignment = NSTextAlignmentCenter;
        
        _lineView = [[UIView alloc] initWithFrame:CGRectMake(0, _versionLabel.bottom + 20, self.view.width, 1)];
        _lineView.backgroundColor = light_white_color;
        
        _tableView = [[UITableView alloc] initWithFrame:CGRectMake(0, _lineView.bottom, self.view.width, 180) style:UITableViewStyleGrouped];
        _tableView.delegate = self;
        _tableView.dataSource = self;
        _tableView.tableFooterView = [[UIView alloc] initWithFrame:CGRectMake(0, 0, 10, 10)];
        _tableView.backgroundColor = RGBCOLOR(249, 249, 249);
        
        [self.view addSubview:_imgView];
        [self.view addSubview:_versionLabel];
        [self.view addSubview:_lineView];
        [self.view addSubview:_tableView];
    }
    return self;
}

- (void)viewDidLoad
{
    [super viewDidLoad];
    // Do any additional setup after loading the view.
    self.view.backgroundColor = RGBCOLOR(249, 249, 249);
    self.titleView.text = @"关于";
}

- (void)viewWillAppear:(BOOL)animated {
    [super viewWillAppear:animated];
}

- (void)viewWillDisappear:(BOOL)animated {
    [super viewWillDisappear:animated];
    [_request cancel];
}

#pragma mark UITableViewDataSource

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    return 2;
}

- (UITableViewCell*)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    static NSString* identifier = @"aboutCell";
    UITableViewCell* cell = [_tableView dequeueReusableCellWithIdentifier:identifier];
    if (!cell) {
        cell = [[UITableViewCell alloc] initWithStyle:UITableViewCellStyleValue1 reuseIdentifier:identifier];
    }
    if (indexPath.row == 0) {
        cell.textLabel.text = @"检查更新";
    } else if (indexPath.row == 1) {
        cell.textLabel.text = @"常见问题";
    }
    cell.backgroundColor = [UIColor whiteColor];
    cell.accessoryType = UITableViewCellAccessoryDisclosureIndicator;
    return cell;
}

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath {
    [_tableView deselectRowAtIndexPath:indexPath animated:YES];
    if (indexPath.row == 0)
        [self requestCheck];
    else if (indexPath.row == 1) {
        TTicketHelpController* vc = [[TTicketHelpController alloc] initWithName:@"常见问题" url:[TAPIUtility getNetworkWithUrl:@"help.jsp"]];
        [self.navigationController pushViewController:vc animated:YES];
    }
}

#pragma mark request

- (void)requestCheck {
    NSString* apiPath = [NSString stringWithFormat:@"update/user/ios_user_%@update.txt", [TAPIUtility isEnterpriseVersion] ? @"enterprise_" : @""];
    _request = [[CVAPIRequest alloc] initWithAPIPath:apiPath downLoad:YES];
    _request.hud = [MBProgressHUD showHUDAddedTo:self.view animated:YES];
    
    [self.model sendRequest:_request completion:^(NSDictionary *result, NSError *error) {
        if (!result)
            return;
        
        NSString* newVersion  = [result objectForKey:@"info"];
        NSString* nowVersion = [[[NSBundle mainBundle] infoDictionary] objectForKey:@"CFBundleShortVersionString"];
        
        if (![TAPIUtility isUpdateVersionWithOld:nowVersion new:newVersion]) {
            [TAPIUtility alertMessage:@"已经是最新版本了哦～"];
        } else {
            TVersionAlertView* versionAlert = [[TVersionAlertView alloc] init];
            [versionAlert setVersion:[result objectForKey:@"info"]];
            versionAlert.appStoreUrl = [result objectForKey:@"url"];
            [versionAlert show];
        }
    }];
}

@end
