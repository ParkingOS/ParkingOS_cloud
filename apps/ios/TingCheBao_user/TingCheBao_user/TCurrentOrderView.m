//
//  TCurrentOrderView.m
//  TingCheBao_user
//
//  Created by zhuhao on 15/4/8.
//  Copyright (c) 2015年 zhenLaiDian. All rights reserved.
//

#import "TCurrentOrderView.h"
#import "TAPIUtility.h"
#import "UIKeyboardViewController.h"
#import "TAlarmNoteItem.h"
#import "TFullImageView.h"


@interface TCurrentOrderView()<UITableViewDataSource, UITableViewDelegate, UIPickerViewDataSource, UIPickerViewDelegate, UIKeyboardViewControllerDelegate, UITextViewDelegate>

@property(nonatomic, retain) UIWindow* currentWindow;

@property(nonatomic, retain) UILabel* titleLabel;
@property(nonatomic, retain) UIView* lineView;
@property(nonatomic, retain) UIView* bottomView;

//没有当前订单
@property(nonatomic, retain) UIImageView* bgImgView;
@property(nonatomic, retain) UILabel* noInfoLabel;

@property(nonatomic, retain) UIButton* scanButton;

//有当前订单
@property(nonatomic, retain) UITableView* orderTableView;
@property(nonatomic, retain) UILabel* waitLabel;

//结算订单
@property(nonatomic, retain) UILabel* payMoneyLabel;
@property(nonatomic, retain) UIButton* payButton;

//计时

@property(nonatomic, retain) UILabel* unitLabel;
@property(nonatomic, retain) UILabel* nextTimeLabel;
@property(nonatomic, retain) UIPickerView* datePicker;
@property(nonatomic, retain) UILabel* timeLabel;

@property(nonatomic, retain) UIButton* cancelButton;
@property(nonatomic, retain) UIButton* confirmButton;

//拍照
@property(nonatomic, retain) UIScrollView* photoScrollView;
@property(nonatomic, retain) UIPageControl* pageControl;
@property(nonatomic, retain) UIButton* photoLeftButton;
@property(nonatomic, retain) UIButton* photorightButton;
@property(nonatomic, retain) UITextView* notesView;



@property(nonatomic, retain) TAlarmNoteItem* alarmNoteItem;
@property(nonatomic, assign) currentOrderMode mode;
@property(nonatomic, retain) UIKeyboardViewController* keyboardViewController;
@property(nonatomic, retain) UIButton* selectedImageButton;


@end
@implementation TCurrentOrderView

- (id)initWithFrame:(CGRect)frame {
    if (self = [super initWithFrame:frame]) {
        
        self.centerView.height = 360;
        self.centerView.top = (self.height - self.centerView.height)/2;
        self.closeButton.frame = CGRectMake(self.centerView.right - 20, self.centerView.top - 20, 40, 40);
        
        _titleLabel = [[UILabel alloc] initWithFrame:CGRectMake(0, 0, self.centerView.width, 50)];
        _titleLabel.text = @"当前订单";
        _titleLabel.textColor = RGBCOLOR(162, 162, 162);
        _titleLabel.backgroundColor = RGBCOLOR(242, 242, 242);
        _titleLabel.textAlignment = NSTextAlignmentCenter;
        
        _lineView = [[UIView alloc] initWithFrame:CGRectMake(0, _titleLabel.bottom, self.centerView.width, 1)];
//        _lineView.backgroundColor = RGBCOLOR(221, 221, 221);
        _lineView.backgroundColor = bg_line_color;
        
        _bottomView = [[UIView alloc] initWithFrame:CGRectMake(0, _lineView.bottom, self.centerView.width, self.centerView.height - _lineView.bottom)];
        _bottomView.backgroundColor = RGBCOLOR(240, 240, 240);
        
        //--加载
        _loaddingView = [[UIActivityIndicatorView alloc] initWithActivityIndicatorStyle:UIActivityIndicatorViewStyleGray];
        _loaddingView.center = CGPointMake(self.centerView.width/2, self.centerView.height/2);
        
        
        //---没有订单
        _bgImgView = [[UIImageView alloc] initWithImage:[UIImage imageNamed:@"order.png"]];
        _bgImgView.frame = CGRectMake((self.centerView.width - 80)/2, _lineView.bottom + 70, 80, 100);
        
        _noInfoLabel = [[UILabel alloc] initWithFrame:CGRectMake(0, _bgImgView.bottom + 20, self.centerView.width, 20)];
        _noInfoLabel.text = @"您没有订单";
        _noInfoLabel.textColor = RGBCOLOR(211, 211, 211);
        _noInfoLabel.textAlignment = NSTextAlignmentCenter;
        
        _scanButton = [UIButton buttonWithType:UIButtonTypeCustom];
        [_scanButton setTitle:@"直接扫码给收费员付费" forState:UIControlStateNormal];
        _scanButton.titleLabel.font = [UIFont systemFontOfSize:14];
        [_scanButton setTitleColor:green_color forState:UIControlStateNormal];
        _scanButton.layer.borderColor = green_color.CGColor;
        _scanButton.layer.borderWidth = 1;
        _scanButton.layer.cornerRadius = 5;
        _scanButton.clipsToBounds = YES;
        _scanButton.frame = CGRectMake(10, self.centerView.height - 50, self.centerView.width - 10*2, 40);
        _scanButton.backgroundColor = [UIColor whiteColor];
        [_scanButton addTarget:self action:@selector(buttonTouched:) forControlEvents:UIControlEventTouchUpInside];
        
        
        
        //有当前订单
        _orderTableView = [[UITableView alloc] initWithFrame:CGRectMake(0, _lineView.bottom, self.centerView.width, self.centerView.height - _lineView.bottom - 40) style:UITableViewStyleGrouped];
        _orderTableView.delegate = self;
        _orderTableView.dataSource = self;
        _orderTableView.backgroundColor = RGBCOLOR(236, 236, 236);
        _orderTableView.allowsSelection = NO;
        
        _waitLabel = [[UILabel alloc] initWithFrame:CGRectMake(0, self.centerView.height - 40, self.centerView.width, 30)];
        _waitLabel.text = @"等待离场结算...";
        _waitLabel.textColor = RGBCOLOR(162, 162, 162);
        _waitLabel.textAlignment = NSTextAlignmentCenter;
        
        
        
        //结算订单
        _payMoneyLabel = [[UILabel alloc] initWithFrame:CGRectMake(0, self.centerView.height - 100, self.centerView.width, 40)];
        _payMoneyLabel.textAlignment = NSTextAlignmentCenter;
        
        _payButton = [UIButton buttonWithType:UIButtonTypeCustom];
        [_payButton setTitle:@"去支付" forState:UIControlStateNormal];
        _payButton.titleLabel.font = [UIFont systemFontOfSize:14];
        [_payButton setTitleColor:[UIColor whiteColor] forState:UIControlStateNormal];
        [_payButton setBackgroundColor:green_color];
        _payButton.layer.cornerRadius = 5;
        _payButton.clipsToBounds = YES;
        _payButton.frame = CGRectMake(10, self.centerView.height - 50, self.centerView.width - 10*2, 40);
        [_payButton addTarget:self action:@selector(buttonTouched:) forControlEvents:UIControlEventTouchUpInside];
        
        
        
        //计时
//        _unitLabel = [[UILabel alloc] initWithFrame:CGRectMake(0, _titleLabel.bottom, self.centerView.width, 30)];
//        _unitLabel.text = @"计时单位:20分钟";
//        _unitLabel.textColor = RGBCOLOR(162, 162, 162);
//        _unitLabel.textAlignment = NSTextAlignmentCenter;
//        _unitLabel.font = [UIFont systemFontOfSize:14];
//        
//        _nextTimeLabel = [[UILabel alloc] initWithFrame:CGRectMake(0, _unitLabel.bottom, self.centerView.width, 30)];
//        _nextTimeLabel.text = @"20分钟后到达下一个计费点";
//        _nextTimeLabel.textColor = [UIColor blackColor];
//        _nextTimeLabel.textAlignment = NSTextAlignmentCenter;
        
        //datepicker
        _datePicker = [[UIPickerView alloc] initWithFrame:CGRectMake(25, _titleLabel.bottom + 20, self.centerView.width - 2*25, 182)];
        _datePicker.delegate = self;
        _datePicker.dataSource = self;
        _datePicker.backgroundColor = [UIColor clearColor];
        [_datePicker selectRow:0 inComponent:0 animated:NO];
        
        
        _timeLabel = [[UILabel alloc] initWithFrame:CGRectMake(0, _datePicker.bottom + 5, self.centerView.width, 30)];
        NSDateFormatter* formatter = [[NSDateFormatter alloc] init];
        [formatter setDateFormat:@"HH:mm"];
        _timeLabel.text = [NSString stringWithFormat:@"%@ 提醒我", [formatter stringFromDate:[NSDate date]]];
        _timeLabel.textColor = [UIColor blackColor];
        _timeLabel.textAlignment = NSTextAlignmentCenter;
        
        _cancelButton = [UIButton buttonWithType:UIButtonTypeCustom];
        [_cancelButton setTitle:@"取消" forState:UIControlStateNormal];
        _cancelButton.titleLabel.font = [UIFont systemFontOfSize:14];
        [_cancelButton setTitleColor:gray_color forState:UIControlStateNormal];
        _cancelButton.layer.borderColor = RGBCOLOR(157, 157, 157).CGColor;
        _cancelButton.layer.borderWidth = 1;
        _cancelButton.layer.cornerRadius = 5;
        _cancelButton.clipsToBounds = YES;
        _cancelButton.frame = CGRectMake(20, self.centerView.height - 50, (self.centerView.width - 20*3)/2, 40);
        [_cancelButton addTarget:self action:@selector(buttonTouched:) forControlEvents:UIControlEventTouchUpInside];
        
        _confirmButton = [UIButton buttonWithType:UIButtonTypeCustom];
        [_confirmButton setTitle:@"开始" forState:UIControlStateNormal];
        _confirmButton.titleLabel.font = [UIFont systemFontOfSize:14];
        [_confirmButton setTitleColor:green_color forState:UIControlStateNormal];
        _confirmButton.layer.borderColor = green_color.CGColor;
        _confirmButton.layer.borderWidth = 1;
        _confirmButton.layer.cornerRadius = 5;
        _confirmButton.clipsToBounds = YES;
        _confirmButton.frame = CGRectMake(_cancelButton.right + 20, self.centerView.height - 50, (self.centerView.width - 20*3)/2, 40);
        [_confirmButton addTarget:self action:@selector(buttonTouched:) forControlEvents:UIControlEventTouchUpInside];
        
        
        //拍照界面
        _photoScrollView = [[UIScrollView alloc] initWithFrame:CGRectMake(20, _titleLabel.bottom, self.centerView.width - 2*20, 140)];
        _photoScrollView.backgroundColor = RGBCOLOR(220, 220, 220);
        _photoScrollView.pagingEnabled = YES;
        _photoScrollView.showsHorizontalScrollIndicator = NO;
        _photoScrollView.showsVerticalScrollIndicator = NO;
        
//        _pageControl = [[UIPageControl alloc] initWithFrame:CGRectMake(_photoScrollView.left + (_photoScrollView.width - 100)/2, _photoScrollView.top + 2, 100, 20)];
        _pageControl = [[UIPageControl alloc] initWithFrame:CGRectMake(_photoScrollView.left, _photoScrollView.top, _photoScrollView.width, 20)];
        _pageControl.numberOfPages = 4;
        _pageControl.backgroundColor = [UIColor blackColor];
        _pageControl.alpha = 0.6;
        
        _photoLeftButton = [UIButton buttonWithType:UIButtonTypeCustom];
        [_photoLeftButton setTitle:@"重拍" forState:UIControlStateNormal];
        _photoLeftButton.titleLabel.font = [UIFont systemFontOfSize:14];
        [_photoLeftButton setBackgroundColor:[UIColor blackColor]];
        [_photoLeftButton setTitleColor:[UIColor whiteColor] forState:UIControlStateNormal];
        [_photoLeftButton setImage:[TAPIUtility ajustImage:[UIImage imageNamed:@"re_photo.png"] size:CGSizeMake(14, 16)] forState:UIControlStateNormal];
        _photoLeftButton.frame = CGRectMake(_photoScrollView.left, _photoScrollView.bottom - 30, _photoScrollView.width/2 - 1, 30);
//        _photoLeftButton.imageEdgeInsets = UIEdgeInsetsMake(8, 30, 8, _photoLeftButton.width - 30 - 14);
        [_photoLeftButton addTarget:self action:@selector(buttonTouched:) forControlEvents:UIControlEventTouchUpInside];
        _photoLeftButton.alpha = 0.8;
        
        _photorightButton = [UIButton buttonWithType:UIButtonTypeCustom];
        [_photorightButton setTitle:@"再拍一张" forState:UIControlStateNormal];
        _photorightButton.titleLabel.font = [UIFont systemFontOfSize:14];
        [_photorightButton setTitleColor:[UIColor whiteColor] forState:UIControlStateNormal];
        [_photorightButton setImage:[TAPIUtility ajustImage:[UIImage imageNamed:@"again_photo.png"] size:CGSizeMake(14, 14)] forState:UIControlStateNormal];
        _photorightButton.backgroundColor = [UIColor blackColor];
        _photorightButton.frame = CGRectMake(_photoLeftButton.right + 2, _photoLeftButton.top, _photoLeftButton.width, 30);
        [_photorightButton addTarget:self action:@selector(buttonTouched:) forControlEvents:UIControlEventTouchUpInside];
        _photorightButton.alpha = 0.8;
        
        
        _notesView = [[UITextView alloc] initWithFrame:CGRectMake(20, _photoScrollView.bottom + 10, self.centerView.width - 2*20, 100)];
        _notesView.font = [UIFont systemFontOfSize:14];
        _notesView.layer.cornerRadius = 5;
        _notesView.layer.borderColor = RGBCOLOR(237, 237, 237).CGColor;
        _notesView.layer.borderWidth = 1;
        _notesView.clipsToBounds = YES;
        _notesView.returnKeyType = UIReturnKeyDone;
        _notesView.text = @"备注";
        _notesView.delegate = self;
        
        [self updateView:currentOrderMode_loading];
        
        [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(willShowKeyboard:) name:UIKeyboardWillChangeFrameNotification object:nil];
    }
    return self;
}

- (void)dealloc {
    [[NSNotificationCenter defaultCenter] removeObserver:self name:UIKeyboardWillChangeFrameNotification object:nil];
}

- (void)setItem:(TCurrentOrderItem *)item {
    _item = item;
    if (_item == nil) {
        [self updateView:currentOrderMode_noInfo];
    } else if ([_item.state isEqualToString:@"0"]) {
        [self updateView:currentOrderMode_noComplete];
    } else {
        [self updateView:currentOrderMode_pay];
    }
    
    _alarmNoteItem = [TAlarmNoteItem getFromFileWithOrderId:item.orderid];
    
}

- (void)updateView:(currentOrderMode)mode {
    _mode = mode;
    
    //title
    _titleLabel.text = mode == currentOrderMode_pay ? @"结算订单" : @"当前订单";
    
    //先很所有的view
    for (UIView* subView in self.centerView.subviews) {
        [subView removeFromSuperview];
    }
    [self.centerView addSubview:_titleLabel];
    [self.centerView addSubview:_lineView];
    [self.centerView addSubview:_bottomView];
    
    if (mode == currentOrderMode_loading) {
        
        [_loaddingView startAnimating];
        [self.centerView addSubview:_loaddingView];
        
    } else if (mode == currentOrderMode_noInfo) {
        
        [self.centerView addSubview:_bgImgView];
        [self.centerView addSubview:_noInfoLabel];
        [self.centerView addSubview:_scanButton];
        
    } else if (mode == currentOrderMode_noComplete) {

        _alarmNoteItem = [TAlarmNoteItem getFromFileWithOrderId:_item.orderid];
        
        _orderTableView.height = self.centerView.height - _lineView.bottom - 40;
        
        [self.centerView addSubview:_orderTableView];
        [self.centerView addSubview:_waitLabel];
        
        //tableview重新加载
        [_orderTableView reloadData];
        
    } else if (mode == currentOrderMode_countTime) {
        
//        [self.centerView addSubview:_unitLabel];
//        [self.centerView addSubview:_nextTimeLabel];
        [self.centerView addSubview:_datePicker];
        [self.centerView addSubview:_timeLabel];
        [self.centerView addSubview:_cancelButton];
        [self.centerView addSubview:_confirmButton];
        
    } else if (mode == currentOrderMode_note) {
        
        [self updateImagesScrollView];
        _notesView.text = [_alarmNoteItem.note isEqualToString:@""] ? @"备注" : _alarmNoteItem.note;
        
        [self.centerView addSubview:_photoScrollView];
//        [self.centerView addSubview:_pageControl];
        [self.centerView addSubview:_photoLeftButton];
        [self.centerView addSubview:_photorightButton];
        [self.centerView addSubview:_notesView];
        [self.centerView addSubview:_cancelButton];
        [_confirmButton setTitle:@"完成" forState:UIControlStateNormal];
        [self.centerView addSubview:_confirmButton];
        
        _alarmNoteItem = [TAlarmNoteItem getFromFileWithOrderId:_item.orderid];
        
    } else if (mode == currentOrderMode_pay) {
        
        NSMutableAttributedString* attri = [[NSMutableAttributedString alloc] initWithString:[NSString stringWithFormat:@"停车费 ¥%@", _item.total] attributes:@{NSForegroundColorAttributeName : green_color}];
        [attri addAttributes:@{NSFontAttributeName : [UIFont systemFontOfSize:25]} range:NSMakeRange(3, attri.string.length - 3)];
        _payMoneyLabel.attributedText = attri;
        _orderTableView.height = 170;
        
        [self.centerView addSubview:_orderTableView];
        [self.centerView addSubview:_payMoneyLabel];
        [self.centerView addSubview:_payButton];
        //重新加载tableview
        [_orderTableView reloadData];
        
    }
}
- (void)buttonTouched:(UIButton*)button {
    if (button.tag == 101) {
        //提醒按钮
        [self updateView:currentOrderMode_countTime];
    } else if (button.tag == 102) {
        //拍照按钮
        _selectedImageButton = nil;
        if ([[TAlarmNoteItem getFromFileWithOrderId:_item.orderid].images count])
            [self updateView:currentOrderMode_note];
        else
            self.photoBlock();
    }
    
    if (button == _cancelButton) {
        [self updateView:currentOrderMode_noComplete];
    } else if (button == _confirmButton) {
        if ([_confirmButton.titleLabel.text isEqualToString:@"完成"]) {
            //保存到本地
            [_alarmNoteItem saveToFile];
        } else if ([_confirmButton.titleLabel.text isEqualToString:@"开始"]) {
            if (_item == nil) {
                [self clearHistory];
            } else {
                NSInteger hours = [_datePicker selectedRowInComponent:0];
                NSInteger miniters = [_datePicker selectedRowInComponent:1];
                NSInteger seconds = hours * 60 * 60 + miniters * 60;
                if (seconds == 0)
                    return;
                NSDate* date = [NSDate dateWithTimeIntervalSinceNow:seconds];
                
                //取消前面所有的通知
                [[UIApplication sharedApplication] cancelAllLocalNotifications];
                UILocalNotification *notification = [[UILocalNotification alloc] init];
                notification.alertBody = @"定时提醒：时间到了";
                notification.soundName = UILocalNotificationDefaultSoundName;
                notification.fireDate = [NSDate dateWithTimeIntervalSinceNow:seconds];
                notification.timeZone = [NSTimeZone defaultTimeZone];
                [[UIApplication sharedApplication] scheduleLocalNotification:notification];
                
                TAlarmNoteItem* alarmItem = [TAlarmNoteItem getFromFileWithOrderId:_item.orderid];
                alarmItem.alarmDate = date;
                [alarmItem saveToFile];
            }
        }
        
        [self updateView:currentOrderMode_noComplete];
    } else if (button == _photoLeftButton || button == _photorightButton) {
        _selectedImageButton = button;
        self.photoBlock();
    } else if (button == _payButton) {
        self.payBlock(_item);
    } else if (button == _scanButton) {
        self.scanBlock();
    }
}

- (void)clearHistory {
    [[UIApplication sharedApplication] cancelAllLocalNotifications];
    [TAlarmNoteItem removeFile];
}

#pragma mark UIPickerViewDataSource

- (NSInteger)numberOfComponentsInPickerView:(UIPickerView *)pickerView {
    return 2;
}

- (NSInteger)pickerView:(UIPickerView *)pickerView numberOfRowsInComponent:(NSInteger)component {
    if (component == 0) {
        return 24;
    } else
        return 60;
}

- (NSAttributedString*)pickerView:(UIPickerView *)pickerView attributedTitleForRow:(NSInteger)row forComponent:(NSInteger)component {
    NSAttributedString* attrString = nil;
    attrString = [[NSAttributedString alloc] initWithString:[NSString stringWithFormat:@"%ld %@", (long)row, component == 0 ? @"时" : @"分"] attributes:@{NSForegroundColorAttributeName : [UIColor blackColor]}];
    return attrString;
}

- (void)pickerView:(UIPickerView *)pickerView didSelectRow:(NSInteger)row inComponent:(NSInteger)component {
    NSInteger hours = [_datePicker selectedRowInComponent:0];
    NSInteger miniters = [_datePicker selectedRowInComponent:1];
    NSInteger seconds = hours * 60 * 60 + miniters * 60;
    //更新alarmLabel内容
    NSDate* date = [NSDate dateWithTimeIntervalSinceNow:seconds];
    NSDateFormatter* formatter = [[NSDateFormatter alloc] init];
    [formatter setDateFormat:@"HH:mm 提醒我"];
    _timeLabel.text = [formatter stringFromDate:date];
    
    if (seconds == 0)
        _confirmButton.enabled = NO;
    else
        _confirmButton.enabled = YES;
    
}
#pragma mark UITableViewDataSource

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView {
    if (tableView == _orderTableView) {
        if (_mode == currentOrderMode_noComplete) {
            return 2;
        } else if (_mode == currentOrderMode_pay) {
            return 2;
        }
    }
    return 1;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    if (tableView == _orderTableView) {
        if (_mode == currentOrderMode_noComplete) {
            if (section == 0) {
                return 2;
            } else if (section == 1) {
                return 2;
            }
        } else if (_mode == currentOrderMode_pay) {
            if (section == 0) {
                return 2;
            } else if (section == 1) {
                return 1;
            }
        }
    }
    
    return 0;
}

- (UITableViewCell*)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    static NSString* identifer = @"cell";
    UITableViewCell* cell = [tableView dequeueReusableCellWithIdentifier:identifer];
//    if (!cell) {
        cell = [[UITableViewCell alloc] initWithStyle:UITableViewCellStyleSubtitle reuseIdentifier:identifer];
//    }
    if (tableView == _orderTableView) {
        if (indexPath.section == 0) {
            if (indexPath.row == 0) {
                cell.imageView.image = [TAPIUtility ajustImage:[UIImage imageNamed:@"order_home.png"] size:CGSizeMake(20, 20)];
                cell.textLabel.text = _item.parkname;
                cell.textLabel.textColor = [UIColor grayColor];
            } else if (indexPath.row == 1) {
                cell.imageView.image = [TAPIUtility ajustImage:[UIImage imageNamed:@"order_alarm.png"] size:CGSizeMake(20, 20)];
                
                NSDate* date = [NSDate dateWithTimeIntervalSince1970:[_item.btime integerValue]];
                NSDate* eDate = [NSDate dateWithTimeIntervalSince1970:[_item.etime integerValue]];
                NSDateFormatter* formatter = [[NSDateFormatter alloc] init];
                [formatter setDateFormat:@"HH:mm"];
                
                if (_mode == currentOrderMode_noComplete) {
                    cell.textLabel.text = [NSString stringWithFormat:@"%@ 入场", [formatter stringFromDate:date]];
                } else if (_mode == currentOrderMode_pay) {
                    cell.textLabel.text = [NSString stringWithFormat:@"%@ —— %@", [formatter stringFromDate:date], [formatter stringFromDate:eDate]];
                }
                cell.textLabel.textColor = [UIColor grayColor];
            }
        } else if (indexPath.section == 1) {
            if (indexPath.row == 0) {
                cell.imageView.image = [TAPIUtility ajustImage:[UIImage imageNamed:@"order_stop.png"] size:CGSizeMake(20, 20)];
                NSString* duration = [TAPIUtility getDuration:[_item.etime intValue] - [_item.btime intValue]];
                NSString* head = _mode == currentOrderMode_noComplete ? @"已停" : @"停车";
                cell.textLabel.text = [NSString stringWithFormat:@"%@%@", head, duration];
                cell.textLabel.textColor = [UIColor grayColor];
            } else if (indexPath.row == 1) {
                cell.imageView.image = [TAPIUtility ajustImage:[UIImage imageNamed:@"order_money.png"] size:CGSizeMake(20, 20)];
                cell.textLabel.text = [NSString stringWithFormat:@"当前%@元", _item.total];
                cell.textLabel.textColor = [UIColor grayColor];
//                UIButton* button = [UIButton buttonWithType:UIButtonTypeCustom];
//                button.tag = 101;
//                [button setTitle:@"提醒" forState:UIControlStateNormal];
//                button.titleLabel.font = [UIFont systemFontOfSize:14];
//                [button setTitleColor:[UIColor whiteColor] forState:UIControlStateNormal];
//                [button setBackgroundImage:[TAPIUtility imageWithColor:green_color] forState:UIControlStateNormal];
//                button.layer.cornerRadius = 5;
//                button.clipsToBounds = YES;
//                button.frame = CGRectMake(0, 0, 60, 30);
//                [button addTarget:self action:@selector(buttonTouched:) forControlEvents:UIControlEventTouchUpInside];
//                cell.accessoryView = button;
            }
        }
    }
    cell.textLabel.font = [UIFont systemFontOfSize:14];
    return cell;
}



#pragma mark UITableViewDelegate

- (CGFloat)tableView:(UITableView *)tableView heightForHeaderInSection:(NSInteger)section {
    return 10;
}

- (CGFloat)tableView:(UITableView *)tableView heightForFooterInSection:(NSInteger)section {
    return 1;
}

#pragma mark UITextViewDelegate
- (void)textViewDidBeginEditing:(UITextView *)textView {
    if ([textView.text isEqualToString:@"备注"])
        textView.text = @"";
}

- (void)textViewDidEndEditing:(UITextView *)textView {
    if ([textView.text isEqualToString:@""]) {
        textView.text = @"备注";
    }
    _alarmNoteItem.note = textView.text;
}

- (BOOL)textView:(UITextView *)textView shouldChangeTextInRange:(NSRange)range replacementText:(NSString *)text {
    if ([text isEqualToString:@"\n"]) {
        [textView endEditing:YES];
        return NO;
    }
    return YES;
}

#pragma mark private

- (void)updateImagesScrollView {
    
    for (UIView* view in _photoScrollView.subviews) {
        [view removeFromSuperview];
    }
    
    _photoScrollView.contentSize = CGSizeMake(_photoScrollView.width * [_alarmNoteItem.images count], _photoScrollView.height);
    for (UIImage* image in _alarmNoteItem.images) {
        NSInteger index = [_alarmNoteItem.images indexOfObject:image];
        TFullImageView* imageView = [[TFullImageView alloc] initWithImage:image];
        imageView.contentMode = UIViewContentModeScaleAspectFit;
        imageView.frame = CGRectMake(_photoScrollView.width * index, 0, _photoScrollView.width, _photoScrollView.height);
        [_photoScrollView addSubview:imageView];
    }
}

#pragma mark public

- (void)captureImage:(UIImage*)image {
//    image = [TAPIUtility imageCompressForSize:image targetSize:_photoScrollView.frame.size];
    image = [TAPIUtility imageCompressForWidth:image targetWidth:_photoScrollView.frame.size.width];
//    image = [TAPIUtility ajustImage:image size:_photoScrollView.frame.size];
    
    if (_selectedImageButton == nil) {
        [_alarmNoteItem.images addObject:image];
        
        //保存图片到本地
        [_alarmNoteItem saveToFile];
        
        [self updateView:currentOrderMode_note];
        
    } else if (_selectedImageButton == _photoLeftButton) {
        int index = _photoScrollView.contentOffset.x / _photoScrollView.width;
        [_alarmNoteItem.images replaceObjectAtIndex:index withObject:image];
        
        //保存图片到本地
        [_alarmNoteItem saveToFile];

        [self updateImagesScrollView];
    } else if (_selectedImageButton == _photorightButton) {
        [_alarmNoteItem.images addObject:image];
        
        //保存图片到本地
        [_alarmNoteItem saveToFile];

        
        [self updateImagesScrollView];
        _photoScrollView.contentOffset = CGPointMake(_photoScrollView.width * ([_alarmNoteItem.images count] - 1), 0);
    }
}


- (void)show:(BOOL)show {
    if (show) {
        _currentWindow = [[UIWindow alloc] initWithFrame:[UIScreen mainScreen].bounds];
        _currentWindow.backgroundColor = [UIColor clearColor];
        
        [_currentWindow addSubview:self];
        [_currentWindow makeKeyAndVisible];
    } else {
        //清除 window
        NSMutableArray* windows = [NSMutableArray arrayWithArray:[UIApplication sharedApplication].windows];
        [windows removeObject:_currentWindow];
        //遍历window,选出正常的window
        [windows enumerateObjectsWithOptions:NSEnumerationReverse usingBlock:^(UIWindow* obj, NSUInteger idx, BOOL *stop) {
            if ([obj isKindOfClass:[UIWindow class]] && obj.windowLevel == UIWindowLevelNormal) {
                [obj makeKeyAndVisible];
                *stop = YES;
            }
        }];
        _currentWindow = nil;
    }
}

#pragma mark super

- (void)closeButtonTouched:(UIButton*)button {
    [self show:NO];
}

#pragma mark keyboard
- (void)willShowKeyboard:(NSNotification*)notification {
    NSDictionary *notificationInfo = [notification userInfo];
    
    // Get the end frame of the keyboard in screen coordinates.
    CGRect finalKeyboardFrame = [[notificationInfo objectForKey:UIKeyboardFrameEndUserInfoKey] CGRectValue];
    CGRect keyboardFrame = finalKeyboardFrame;
    
    // Convert the finalKeyboardFrame to view coordinates to take into account any rotation
    // factors applied to the window’s contents as a result of interface orientation changes.
    finalKeyboardFrame = [self.centerView convertRect:finalKeyboardFrame fromView:self.centerView.window];
    
    // Get the animation curve and duration
    UIViewAnimationCurve animationCurve = (UIViewAnimationCurve) [[notificationInfo objectForKey:UIKeyboardAnimationCurveUserInfoKey] integerValue];
    NSTimeInterval animationDuration = [[notificationInfo objectForKey:UIKeyboardAnimationDurationUserInfoKey] doubleValue];
    
    // Animate view size synchronously with the appearance of the keyboard.
    [UIView beginAnimations:nil context:nil];
    [UIView setAnimationDuration:animationDuration];
    [UIView setAnimationCurve:animationCurve];
    [UIView setAnimationBeginsFromCurrentState:YES];
    //    CGFloat height = _currentOrderView.bottom - finalKeyboardFrame.origin.y;
    //    _currentOrderView.blurView.bottom = MIN(([UIScreen mainScreen].bounds.size.height + 215)/2, finalKeyboardFrame.origin.y);
    CGFloat height = _notesView.bottom - finalKeyboardFrame.origin.y;
    if (keyboardFrame.origin.y != [UIScreen mainScreen].bounds.size.height) {
        //show
        if (height > 0) {
            self.top += -1 *height;
        }
    } else//hide
        self.top = 0;
    
    [UIView commitAnimations];
}

/*
// Only override drawRect: if you perform custom drawing.
// An empty implementation adversely affects performance during animation.
- (void)drawRect:(CGRect)rect {
    // Drawing code
}
*/

@end
