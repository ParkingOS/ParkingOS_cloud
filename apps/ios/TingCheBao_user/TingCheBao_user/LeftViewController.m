//
//  LeftViewController.m
//  Dog
//
//  Created by apple on 14-7-24.
//  Copyright (c) 2014年 zhenLaiDian. All rights reserved.
//

#import "LeftViewController.h"
#import "UIView+CVUIViewAdditions.h"
#import "CenterViewController.h"
#import "ViewController.h"

@interface LeftViewController ()<UITableViewDelegate, UITableViewDataSource>

@property(nonatomic, retain) UITableView* tableView;
@property(nonatomic, retain) NSArray* rows;

@end

@implementation LeftViewController

- (void)viewDidLoad
{
    [super viewDidLoad];
    // Do any additional setup after loading the view.
    self.edgesForExtendedLayout = UIRectEdgeNone;
    self.view.top = 0;
    self.view.height = 560;
    self.view.backgroundColor = [UIColor whiteColor];
    
    _tableView = [[UITableView alloc] initWithFrame:CGRectMake(0, 20, 240, self.view.height)];
    _tableView.delegate = self;
    _tableView.dataSource = self;
    _tableView.tableFooterView = [[UIView alloc] initWithFrame:CGRectMake(0, 0, 10, 10)];
    
    
    [self.view addSubview:_tableView];
    
    _rows = @[@"menu1", @"menu2", @"menu3", @"menu4", @"menu5"];
}

#pragma mark UITableViewDataSource
- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    return [_rows count];
}

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView {
    return 1;
}

- (UITableViewCell*)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    static NSString* identifier = @"cell";
    UITableViewCell* cell = [_tableView dequeueReusableCellWithIdentifier:identifier];
    if (!cell) {
        cell = [[UITableViewCell alloc] initWithStyle:UITableViewCellStyleDefault reuseIdentifier:identifier];
    }
    cell.textLabel.text = [_rows objectAtIndex:indexPath.row];
    cell.textLabel.textColor = [UIColor whiteColor];
    cell.contentView.backgroundColor = [UIColor grayColor];
    cell.textLabel.font = [UIFont systemFontOfSize:12];
    return cell;
}

- (UIView*)tableView:(UITableView *)tableView viewForHeaderInSection:(NSInteger)section {
    UIView* view = [[UIView alloc] initWithFrame:CGRectMake(0, 0, 200, 40)];
    UILabel* label = [[UILabel alloc] initWithFrame:CGRectMake(0, 0, 200, 40)];
    label.text = @"menu";
    [view addSubview:label];
    return view;
}

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath {
    CenterViewController* center = [[CenterViewController alloc] init];
    UINavigationController* nv = [[UINavigationController alloc] initWithRootViewController:center];
    [ViewController share].centerView = nv.view;
}
- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath {
    return 40;
}

- (CGFloat)tableView:(UITableView *)tableView heightForHeaderInSection:(NSInteger)section {
    return 40;
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

/*
#pragma mark - Navigation

// In a storyboard-based application, you will often want to do a little preparation before navigation
- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender
{
    // Get the new view controller using [segue destinationViewController].
    // Pass the selected object to the new view controller.
}
*/

@end
