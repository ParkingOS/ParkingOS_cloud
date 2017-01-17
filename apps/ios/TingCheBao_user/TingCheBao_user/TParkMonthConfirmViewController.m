//
//  TParkMonthDetailViewController.m
//  TingCheBao_user
//
//  Created by apple on 14-9-15.
//  Copyright (c) 2014年 zhenLaiDian. All rights reserved.
//

#import "TParkMonthConfirmViewController.h"
#import "TAPIUtility.h"
#import "CVAPIRequestModel.h"
#import "TParkItem.h"
#import "TRechargeWaysViewController.h"

@interface TParkMonthConfirmViewController ()<UITableViewDataSource, UITableViewDelegate, UIPickerViewDataSource, UIPickerViewDelegate>

@property(nonatomic, retain) UIView* topView;
@property(nonatomic, retain) UILabel* parkNameLabel;
@property(nonatomic, retain) UILabel* productNameLabel;
@property(nonatomic, retain) UILabel* validTimeLabel;
@property(nonatomic, retain) UITableView* tableView;
@property(nonatomic, retain) UIButton* buyButton;
@property(nonatomic, retain) UIView* dateView;
@property(nonatomic, retain) UIDatePicker* startTimePicker;
@property(nonatomic, retain) UIPickerView* longTimePicker;
@property(nonatomic, retain) UIButton* closeButton;
@property(nonatomic, retain) NSString* parkName;

@property(nonatomic, retain) NSDate* startDate;
@property(nonatomic, retain) NSString* longTime;
@property(nonatomic, retain) NSDate* endDate;

@property(nonatomic, retain) UITableViewCell* selectedCell;

@end

@implementation TParkMonthConfirmViewController

- (id)init {
    if (self = [super init]) {
        _topView = [[UIView alloc] initWithFrame:CGRectMake(0, 0, self.view.width, 100)];
        _topView.backgroundColor = RGBCOLOR(82, 88, 102);
        
        _parkNameLabel = [[UILabel alloc] initWithFrame:CGRectMake(0, 5, self.view.width, 30)];
        _parkNameLabel.backgroundColor = [UIColor clearColor];
        _parkNameLabel.textAlignment = NSTextAlignmentCenter;
        _parkNameLabel.textColor = [UIColor whiteColor];
        
        _productNameLabel = [[UILabel alloc] initWithFrame:CGRectMake(0, _parkNameLabel.bottom, self.view.width, 30)];
        _productNameLabel.backgroundColor = [UIColor clearColor];
        _productNameLabel.textAlignment = NSTextAlignmentCenter;
        _productNameLabel.textColor = [UIColor whiteColor];
        
        _validTimeLabel = [[UILabel alloc] initWithFrame:CGRectMake(0, _productNameLabel.bottom, self.view.width, 30)];
        _validTimeLabel.backgroundColor = [UIColor clearColor];
        _validTimeLabel.textAlignment = NSTextAlignmentCenter;
        _validTimeLabel.textColor = [UIColor whiteColor];
        
        [_topView addSubview:_parkNameLabel];
        [_topView addSubview:_productNameLabel];
        [_topView addSubview:_validTimeLabel];
        
        _tableView = [[UITableView alloc] initWithFrame:CGRectMake(4, _topView.bottom, self.view.width - 2*4, 220) style:UITableViewStyleGrouped];
        _tableView.delegate = self;
        _tableView.dataSource = self;
        _tableView.tableFooterView = [[UIView alloc] initWithFrame:CGRectMake(0, 0, 10, 10)];
        
        _buyButton = [UIButton buttonWithType:UIButtonTypeCustom];
        [_buyButton setTitle:@"确定购买" forState:UIControlStateNormal];
        [_buyButton setTitleColor:[UIColor whiteColor] forState:UIControlStateNormal];
        [_buyButton setBackgroundImage:[TAPIUtility imageWithColor:green_color] forState:UIControlStateNormal];
        _buyButton.layer.cornerRadius = 5;
        _buyButton.clipsToBounds = YES;
        _buyButton.frame = CGRectMake(4, _tableView.bottom + 4, self.view.width - 2*4, 40);
        [_buyButton addTarget:self action:@selector(buyButtonTouched) forControlEvents:UIControlEventTouchUpInside];
        
        _dateView = [[UIView alloc] initWithFrame:CGRectMake(0, self.view.height, self.view.width, 200)];
        _dateView.backgroundColor = [[UIColor whiteColor] colorWithAlphaComponent:0.7];
        
        _closeButton = [UIButton buttonWithType:UIButtonTypeCustom];
        [_closeButton setTitle:@"关闭" forState:UIControlStateNormal];
        [_closeButton setTitleColor:[UIColor whiteColor] forState:UIControlStateNormal];
        _closeButton.frame = CGRectMake(self.view.width - 80, 10, 70, 30);
        [_closeButton setBackgroundImage:[TAPIUtility imageWithColor:green_color] forState:UIControlStateNormal];
        [_closeButton addTarget:self action:@selector(closeButtonTouched) forControlEvents:UIControlEventTouchUpInside];
        
        _startTimePicker = [[UIDatePicker alloc] initWithFrame:CGRectMake(0, 30, self.view.width, 160)];
        _startTimePicker.datePickerMode = UIDatePickerModeDate;
        [_startTimePicker addTarget:self action:@selector(datePickerChanged:) forControlEvents:UIControlEventValueChanged];
        _startTimePicker.backgroundColor = [UIColor clearColor];
        
        _longTimePicker = [[UIPickerView alloc] initWithFrame:CGRectMake(0, 30, self.view.width, 160)];
        _longTimePicker.delegate = self;
        _longTimePicker.dataSource = self;
        
        [_dateView addSubview:_closeButton];
        [_dateView addSubview:_startTimePicker];
        [_dateView addSubview:_longTimePicker];
        
        [self.view addSubview:_topView];
        [self.view addSubview:_tableView];
        [self.view addSubview:_buyButton];
        [self.view addSubview:_dateView];
    }
    return self;
}

- (void)viewDidLoad
{
    [super viewDidLoad];
    // Do any additional setup after loading the view.
    self.view.backgroundColor = light_white_color;
    
    
}

- (void)viewWillAppear:(BOOL)animated {
    [super viewWillAppear:animated];
    self.titleView.text = @"确认购买";
    _startDate = [NSDate date];
    _longTime = @"1";
    _endDate = [self updateEndTimeWithDate:_startDate longTime:_longTime];
    
    [self initInfo];
}

#pragma mark private

- (void)initInfo {
    //获取 停车场 名字
    for (TParkItem* item in [TAPIUtility getParks]) {
        if ([item.parkId isEqualToString:_parkId]) {
            _parkName = item.name;
            _parkNameLabel.text = _parkName;
            break;
        }
    }
    _productNameLabel.text = _item.name;
    NSDate* date = [NSDate dateWithTimeIntervalSince1970:[_item.limitday integerValue]];
    NSDateFormatter* formatter = [[NSDateFormatter alloc] init];
    [formatter setDateFormat:@"yyyy-MM-dd"];
    _validTimeLabel.text = [NSString stringWithFormat:@"有效期至：%@", [formatter stringFromDate:date]];
}

- (void)buyButtonTouched {
    TRechargeWaysViewController* vc = [[TRechargeWaysViewController alloc] init];
    vc.productId = _item.productId;
    vc.name = [NSString stringWithFormat:@"%@ %@ %d个月", _parkName, _item.name, [_longTime intValue]];
    vc.price = [NSString stringWithFormat:@"%.2f", [_item.price floatValue] * [_longTime floatValue]];
    vc.startDate = _startDate;
    vc.longTime = _longTime;
    [self.navigationController pushViewController:vc animated:YES];
}

- (void)closeButtonTouched {
    [_selectedCell setSelected:NO];
    [UIView animateWithDuration:0.2 animations:^{
        _dateView.top = self.view.height;
    }];
}

- (void)datePickerChanged:(UIDatePicker*)picker {
    NSDate* startDate = picker.date;
    NSDate* endDate = [self updateEndTimeWithDate:startDate longTime:_longTime];
    if (endDate.timeIntervalSince1970 > [_item.limitday integerValue]) {
        [TAPIUtility alertMessage:@"不能超过有效期" success:NO toViewController:self];
    } else {
        _startDate = startDate;
        _endDate = endDate;
        [_tableView reloadData];
    }
}

- (NSDate*)updateEndTimeWithDate:(NSDate*)startDate longTime:(NSString*)longTime{
    NSCalendar *myCal =  [[NSCalendar alloc]initWithCalendarIdentifier:NSGregorianCalendar];
    unsigned units  = NSMonthCalendarUnit|NSDayCalendarUnit|NSYearCalendarUnit;
    NSDateComponents *comp = [myCal components:units fromDate:startDate];
    NSInteger month = [comp month];
    [comp setMonth:(month + [longTime integerValue])];
    return [myCal dateFromComponents:comp];
}

#pragma mark UIPickerViewDataSource

- (NSInteger)numberOfComponentsInPickerView:(UIPickerView *)pickerView {
    return 1;
}

- (NSInteger)pickerView:(UIPickerView *)pickerView numberOfRowsInComponent:(NSInteger)component {
    return 12;
}

#pragma mark UIPickerViewDelegate

- (NSString*)pickerView:(UIPickerView *)pickerView titleForRow:(NSInteger)row forComponent:(NSInteger)component {
    return [NSString stringWithFormat:@"%d", row + 1];
}

- (void)pickerView:(UIPickerView *)pickerView didSelectRow:(NSInteger)row inComponent:(NSInteger)component {
    NSString* longTime = [NSString stringWithFormat:@"%d", row + 1];
    
    NSDate* endDate = [self updateEndTimeWithDate:_startDate longTime:longTime];
    if (endDate.timeIntervalSince1970 > [_item.limitday integerValue]) {
        [TAPIUtility alertMessage:@"不能超过有效期" success:NO toViewController:self];
    } else {
        _longTime = longTime;
        _endDate = endDate;
        [_tableView reloadData];
    }
}

#pragma mark UITableViewDataSource

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    return 4;
}

- (UITableViewCell*)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    static NSString* identifier = @"monthDetailCell";
    UITableViewCell* cell = [_tableView dequeueReusableCellWithIdentifier:identifier];
    if (!cell) {
        cell = [[UITableViewCell alloc] initWithStyle:UITableViewCellStyleValue1 reuseIdentifier:identifier];
    }
    if (indexPath.row == 0) {
        cell.textLabel.text = @"起始日期";
        
        NSDateFormatter* formatter = [[NSDateFormatter alloc] init];
        [formatter setDateFormat:@"yyyy-MM-dd"];
        cell.detailTextLabel.text =[formatter stringFromDate:_startDate];
        cell.accessoryType = UITableViewCellAccessoryDisclosureIndicator;
    } else if (indexPath.row == 1) {
        cell.textLabel.text = @"包月时长";
        cell.detailTextLabel.text = [NSString stringWithFormat:@"%@个月",_longTime];
        cell.accessoryType = UITableViewCellAccessoryDisclosureIndicator;
    } else if (indexPath.row == 2) {
        cell.textLabel.text = @"结束日期";
        cell.selectionStyle = UITableViewCellSelectionStyleNone;
        
        NSDateFormatter* formatter = [[NSDateFormatter alloc] init];
        [formatter setDateFormat:@"yyyy-MM-dd"];
        cell.detailTextLabel.text =[formatter stringFromDate:_endDate];
    } else {
        cell.textLabel.text = @"价格";
        cell.selectionStyle = UITableViewCellSelectionStyleNone;
        
        NSString* price = [NSString stringWithFormat:@"¥%.2f", [_item.price floatValue] * [_longTime floatValue]];
        NSMutableAttributedString* priceAttribute = [[NSMutableAttributedString alloc] initWithString:price];
        [priceAttribute addAttributes:@{NSForegroundColorAttributeName : [UIColor redColor]} range:NSMakeRange(0, 1)];
        [priceAttribute addAttributes:@{NSFontAttributeName : [UIFont systemFontOfSize:20], NSForegroundColorAttributeName : [UIColor blackColor]} range:NSMakeRange(1, [price length] - 1)];
        
        cell.detailTextLabel.attributedText = priceAttribute;
    }
    return cell;
}

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath {
    _selectedCell = [_tableView cellForRowAtIndexPath:indexPath];
    
    if (indexPath.row == 0) {
        _startTimePicker.hidden = NO;
        _longTimePicker.hidden = YES;
        _startTimePicker.minimumDate = [NSDate date];
        [UIView animateWithDuration:0.2 animations:^{
            _dateView.bottom = self.view.height;
        }];
    } else if (indexPath.row == 1){
        _startTimePicker.hidden = YES;
        _longTimePicker.hidden = NO;
        [UIView animateWithDuration:0.2 animations:^{
            _dateView.bottom = self.view.height;
        }];
    }
}


@end
