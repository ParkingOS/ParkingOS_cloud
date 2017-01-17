//
//  TCurrenOrderView.m
//  TingCheBao_user
//
//  Created by apple on 14/12/9.
//  Copyright (c) 2014年 zhenLaiDian. All rights reserved.
//

#import "TCurrenOrderView.h"
#import "TAppDelegate.h"
#import "TAPIUtility.h"
#import "TAlarmNoteItem.h"

#define padding 10

@interface TCurrenOrderView()<UIPickerViewDataSource, UIPickerViewDelegate, UITextViewDelegate>

@property(nonatomic, retain) UIWindow* window;
@property(nonatomic, retain) UIView* bgView;
//========订单
@property(nonatomic, retain) UIView* centerOrderView;
@property(nonatomic, retain) UILabel* orderTitleLabel;

//有当前订单
@property(nonatomic, retain) UIView* hasOrderView;
@property(nonatomic, retain) UILabel* nameLabel;
@property(nonatomic, retain) UILabel* moneyLabel;
@property(nonatomic, retain) UILabel* stateLabel;
@property(nonatomic, retain) UILabel* durationLabel;
@property(nonatomic, retain) UILabel* beginTimeLabel;

//没有，扫二维码
@property(nonatomic, retain) UIView* noOrderView;
@property(nonatomic, retain) UIImageView* scanImgView;
@property(nonatomic, retain) UILabel* scanLabel;
@property(nonatomic, retain) UIImageView* arrowImgView;

//分隔线
@property(nonatomic, retain) UIImageView* lineView;

//闹钟
@property(nonatomic, retain) UILabel* alarmLabel;
@property(nonatomic, retain) UIButton* alarmButton;

//备忘
@property(nonatomic, retain) UIView* noteView;
@property(nonatomic, retain) UITextView* noteTextView;
@property(nonatomic, retain) UIView* line1View;
@property(nonatomic, retain) UIView* line2View;
@property(nonatomic, retain) UIImageView* editImgView;

//========时钟
@property(nonatomic, retain) UIView* centerAlarmView;
@property(nonatomic, retain) UILabel* alarmTitleLabel;
//datepicker
@property(nonatomic, retain) UIPickerView* datePicker;
@property(nonatomic, retain) UIView* line3View;
@property(nonatomic, retain) UIView* line4View;
@property(nonatomic, retain) UILabel* hourLabel;
@property(nonatomic, retain) UILabel* miniteLabel;

@property(nonatomic, retain) UIButton* cancelButton;
@property(nonatomic, retain) UIButton* startButton;

@end
@implementation TCurrenOrderView

- (id)initWithFrame:(CGRect)frame bgImage:(UIImage*)bgImage{
    if (self = [super initWithFrame:frame]) {
        
        _bgView = [[UIView alloc] initWithFrame:frame];
        _bgView.backgroundColor = [UIColor whiteColor];
        _bgView.alpha = 0.2;
        UITapGestureRecognizer* gesture = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(handleTapGesture:)];
        [_bgView addGestureRecognizer:gesture];
        
        _blurView = [[TBlurView alloc] initWithFrame:CGRectMake((self.width - 270)/2, (self.height-215)/2, 270, 215) image:bgImage alpha:0.8];
        _blurView.layer.cornerRadius = 10;
        _blurView.clipsToBounds = YES;
        
        //=================centerOrderView=================
        _centerOrderView = [[UIView alloc] initWithFrame:CGRectMake(padding, 0, _blurView.width - 2*padding, _blurView.height)];
        
        _orderTitleLabel = [[UILabel alloc] initWithFrame:CGRectMake(0, 0, _centerOrderView.width, 20)];
        _orderTitleLabel.text = @"当前订单";
        _orderTitleLabel.textColor = [UIColor whiteColor];
        _orderTitleLabel.font = [UIFont systemFontOfSize:14];
        
        _activityView = [[UIActivityIndicatorView alloc] initWithActivityIndicatorStyle:UIActivityIndicatorViewStyleWhiteLarge];
        _activityView.hidesWhenStopped = YES;
        _activityView.frame = CGRectMake(0, _orderTitleLabel.bottom, _centerOrderView.width, 60);
        
        //hasOrderView
        _hasOrderView = [[UIView alloc] initWithFrame:_activityView.frame];
        
        _nameLabel = [[UILabel alloc] init];
        _nameLabel.text = @"";
        _nameLabel.textColor = [UIColor whiteColor];
        _nameLabel.font = [UIFont systemFontOfSize:14];
        _nameLabel.frame = CGRectMake(0, 10, 170, 20);
        
        _moneyLabel = [[UILabel alloc] init];
        _moneyLabel.text = @"";
        _moneyLabel.textColor = [UIColor whiteColor];
        _moneyLabel.font = [UIFont systemFontOfSize:14];
        _moneyLabel.frame = CGRectMake(_hasOrderView.width - 80, _nameLabel.top, 80, 20);
        _moneyLabel.textAlignment = NSTextAlignmentRight;
        
        _stateLabel = [[UILabel alloc] init];
        _stateLabel.text = @"";
        _stateLabel.textColor = [UIColor whiteColor];
        _stateLabel.font = [UIFont systemFontOfSize:14];
        _stateLabel.frame = CGRectMake(0, _moneyLabel.bottom, 80, 20);
        
        _durationLabel = [[UILabel alloc] init];
        _durationLabel.text = @"";
        _durationLabel.textColor = [UIColor whiteColor];
        _durationLabel.font = [UIFont systemFontOfSize:14];
        _durationLabel.frame = CGRectMake(_hasOrderView.width - 140, _moneyLabel.bottom, 140, 20);
        _durationLabel.textAlignment = NSTextAlignmentRight;
        
        _beginTimeLabel = [[UILabel alloc] init];
        _beginTimeLabel.text = @"";
        _beginTimeLabel.textColor = [UIColor whiteColor];
        _beginTimeLabel.font = [UIFont systemFontOfSize:14];
        _beginTimeLabel.frame = CGRectMake(0, _stateLabel.bottom, _hasOrderView.width, 20);
        
        [_hasOrderView addSubview:_nameLabel];
        [_hasOrderView addSubview:_moneyLabel];
        [_hasOrderView addSubview:_stateLabel];
        [_hasOrderView addSubview:_durationLabel];
        [_hasOrderView addSubview:_beginTimeLabel];
        
        //noOrderView
        _noOrderView = [[UIView alloc] initWithFrame:_activityView.frame];
        UITapGestureRecognizer* tapGeesture = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(handleTapGesture:)];
        [_noOrderView addGestureRecognizer:tapGeesture];
        
        _scanImgView = [[UIImageView alloc] initWithFrame:CGRectMake(0, 20, 50, 50)];
        _scanImgView.image = [UIImage imageNamed:@"scan2.png"];
        
        _scanLabel = [[UILabel alloc] init];
        _scanLabel.text = @"扫一扫查看订单";
        _scanLabel.textColor = [UIColor whiteColor];
        _scanLabel.font = [UIFont systemFontOfSize:15];
        _scanLabel.frame = CGRectMake(_scanImgView.right + 40, 32, 120, 20);
        
        _arrowImgView = [[UIImageView alloc] initWithFrame:CGRectMake(_noOrderView.width - 20, 25, 20, 40)];
        _arrowImgView.image = [UIImage imageNamed:@"rightArrow.png"];
        
        [_noOrderView addSubview:_scanImgView];
        [_noOrderView addSubview:_scanLabel];
        [_noOrderView addSubview:_arrowImgView];
        
        //line
//        _lineView = [[UIView alloc] initWithFrame:CGRectMake(-1*padding, _activityView.bottom + 5, _centerOrderView.width + 2*padding, 1)];
//        _lineView.backgroundColor = [UIColor whiteColor];
        _lineView = [[UIImageView alloc] initWithFrame:CGRectMake(-1*padding, _activityView.bottom + 30, _centerOrderView.width + 2*padding, 1)];
        _lineView.image = [UIImage imageNamed:@"order_line.png"];
        
        _alarmLabel = [[UILabel alloc] initWithFrame:CGRectMake(0, _lineView.bottom, _centerOrderView.width, 30)];
        _alarmLabel.text = @"";
        _alarmLabel.textColor = [UIColor whiteColor];
        _alarmLabel.font = [UIFont systemFontOfSize:14];
        _alarmLabel.textAlignment = NSTextAlignmentCenter;
        
        //alram
        _alarmButton = [UIButton buttonWithType:UIButtonTypeCustom];
        [_alarmButton setBackgroundImage:[UIImage imageNamed:@"alarm.png"] forState:UIControlStateNormal];
        [_alarmButton addTarget:self action:@selector(buttonTouched:) forControlEvents:UIControlEventTouchUpInside];
        _alarmButton.frame = CGRectMake(0, _alarmLabel.bottom, 60, 60);
        _alarmButton.userInteractionEnabled = NO;
        
        //noteView
        _noteView = [[UIView alloc] initWithFrame:CGRectMake(_alarmButton.right + 10, _alarmButton.top, 180, 60)];
        _noteView.layer.borderColor = [UIColor whiteColor].CGColor;
        _noteView.layer.borderWidth = 1;
        _noteView.layer.cornerRadius = 5;
        _noteView.clipsToBounds = YES;
        
        //noteTextView
        _noteTextView = [[UITextView alloc] initWithFrame:CGRectMake(0, 0, _noteView.width - 10, 60)];
//        _noteTextView.text = @"这这里是备忘信息这里是备忘信息这里是备忘信息这里是备忘信息这里是备忘信息这里是备忘信息这里是备忘信息这里是备忘信息里是备忘信息";
        _noteTextView.text = @"停哪了？记一下吧～";
        _noteTextView.font = [UIFont systemFontOfSize:14];
        _noteTextView.textColor = [UIColor whiteColor];
        _noteTextView.returnKeyType = UIReturnKeyDone;
        _noteTextView.backgroundColor = [UIColor clearColor];
        _noteTextView.delegate = self;
        _noteTextView.userInteractionEnabled = NO;
        
        _line1View = [[UIView alloc] initWithFrame:CGRectMake(0, 23, _noteView.width, 0.5)];
        _line1View.backgroundColor = [UIColor whiteColor];
        
        _line2View = [[UIView alloc] initWithFrame:CGRectMake(0, 40, _noteView.width, 0.5)];
        _line2View.backgroundColor = [UIColor whiteColor];
        
        [_noteTextView addSubview:_line1View];
        [_noteTextView addSubview:_line2View];
        
        _editImgView = [[UIImageView alloc] initWithFrame:CGRectMake(_noteView.width - 26, _noteView.height - 24, 20, 20)];
//        _editImgView.backgroundColor = [UIColor redColor];
        _editImgView.image = [UIImage imageNamed:@"pen.png"];
        
        [_noteView addSubview:_noteTextView];
        [_noteView addSubview:_editImgView];
        
        [_centerOrderView addSubview:_orderTitleLabel];
        [_centerOrderView addSubview:_activityView];
        [_centerOrderView addSubview:_hasOrderView];
        [_centerOrderView addSubview:_noOrderView];
        [_centerOrderView addSubview:_lineView];
        [_centerOrderView addSubview:_alarmLabel];
        [_centerOrderView addSubview:_alarmButton];
        [_centerOrderView addSubview:_noteView];
        
        //=======================centerAlarmView=============
        _centerAlarmView = [[UIView alloc] initWithFrame:CGRectMake(padding, 0, _blurView.width - 2*padding, _blurView.height)];
        
        _alarmTitleLabel = [[UILabel alloc] initWithFrame:CGRectMake(0, 0, _centerOrderView.width, 20)];
        _alarmTitleLabel.text = @"计时提醒";
        _alarmTitleLabel.textColor = [UIColor whiteColor];
        _alarmTitleLabel.font = [UIFont systemFontOfSize:14];
        _alarmTitleLabel.textAlignment = NSTextAlignmentCenter;
        
        //datepicker
        _datePicker = [[UIPickerView alloc] initWithFrame:CGRectMake(25, _alarmTitleLabel.bottom, 200, 162)];
        _datePicker.delegate = self;
        _datePicker.dataSource = self;
        _datePicker.backgroundColor = [UIColor clearColor];
        [_datePicker selectRow:3 inComponent:0 animated:NO];
        
        _line3View = [[UIView alloc] initWithFrame:CGRectMake(0, 68, _datePicker.width, 0.5)];
        _line3View.backgroundColor = [UIColor whiteColor];
        _line3View.alpha = 0.5;
        
        _line4View = [[UIView alloc] initWithFrame:CGRectMake(0, 94, _datePicker.width, 0.5)];
        _line4View.backgroundColor = [UIColor whiteColor];
        _line4View.alpha = 0.5;
        
        _hourLabel = [[UILabel alloc] initWithFrame:CGRectMake(70, 72, 40, 20)];
        _hourLabel.text = @"小时";
        _hourLabel.textColor = [UIColor whiteColor];
        _hourLabel.font = [UIFont systemFontOfSize:15];
        _hourLabel.alpha = 0.5;
        
        _miniteLabel = [[UILabel alloc] initWithFrame:CGRectMake(172, 72, 40, 20)];
        _miniteLabel.text = @"分钟";
        _miniteLabel.textColor = [UIColor whiteColor];
        _miniteLabel.font = [UIFont systemFontOfSize:15];
        _miniteLabel.alpha = 0.5;
        
        [_datePicker addSubview:_line3View];
        [_datePicker addSubview:_line4View];
        [_datePicker addSubview:_hourLabel];
        [_datePicker addSubview:_miniteLabel];
        
        _cancelButton = [UIButton buttonWithType:UIButtonTypeCustom];
        [_cancelButton setTitle:@"取消" forState:UIControlStateNormal];
        [_cancelButton setTitleColor:[UIColor grayColor] forState:UIControlStateNormal];
        [_cancelButton setTitleColor:[UIColor blackColor] forState:UIControlStateHighlighted];
        _cancelButton.titleLabel.font = [UIFont systemFontOfSize:15];
        [_cancelButton setBackgroundImage:[TAPIUtility imageWithColor:RGBCOLOR(70, 72, 70)] forState:UIControlStateNormal];
        [_cancelButton addTarget:self action:@selector(buttonTouched:) forControlEvents:UIControlEventTouchUpInside];
        _cancelButton.frame = CGRectMake(-1*padding, _datePicker.bottom, _centerAlarmView.width/2 + padding, 35);
        
        _startButton = [UIButton buttonWithType:UIButtonTypeCustom];
        [_startButton setTitle:@"开始计时" forState:UIControlStateNormal];
        [_startButton setTitleColor:[UIColor whiteColor] forState:UIControlStateNormal];
        [_startButton setTitleColor:[UIColor blackColor] forState:UIControlStateHighlighted];
        _startButton.titleLabel.font = [UIFont systemFontOfSize:15];
        [_startButton setBackgroundImage:[TAPIUtility imageWithColor:RGBCOLOR(98, 102, 97)] forState:UIControlStateNormal];
        [_startButton addTarget:self action:@selector(buttonTouched:) forControlEvents:UIControlEventTouchUpInside];
        _startButton.frame = CGRectMake(_cancelButton.right, _cancelButton.top, _centerAlarmView.width/2 + padding, 35);
        
        [_centerAlarmView addSubview:_alarmTitleLabel];
        [_centerAlarmView addSubview:_datePicker];
        [_centerAlarmView addSubview:_cancelButton];
        [_centerAlarmView addSubview:_startButton];
        
        [_blurView addSubview:_centerOrderView];
        [_blurView addSubview:_centerAlarmView];
        
        [self addSubview:_bgView];
        [self addSubview:_blurView];
        
        _hasOrderView.hidden = YES;
        _noOrderView.hidden = YES;
//        _centerOrderView.hidden = YES;
        _centerAlarmView.hidden = YES;
    }
    return self;
}

- (void)handleTapGesture :(UITapGestureRecognizer*)gesture{
    [self endEditing:YES];
    
    if (gesture.view == _bgView) {
        [UIView animateWithDuration:0.2 animations:^{
//            _blurView.frame = CGRectMake(278, 335, 1, 1);
            _blurView.frame = _litleFrame;
            _blurView.alpha = 0;
        } completion:^(BOOL finished) {
//            //移除window
//            NSMutableArray* windows = [NSMutableArray arrayWithArray:[UIApplication sharedApplication].windows];
//            [windows removeObject:_window];
//            [self removeFromSuperview];
//            _window = nil;
//            
            //显示原来的window
            UIWindow* window = ((TAppDelegate*)[UIApplication sharedApplication].delegate).window;
            [window makeKeyAndVisible];
        }];
        
        _closeBlock();
    } else if (gesture.view == _noOrderView) {
        _scanBlock();
        //显示原来的window
        UIWindow* window = ((TAppDelegate*)[UIApplication sharedApplication].delegate).window;
        [window makeKeyAndVisible];
    }
}

- (void)show {
    _hasOrderView.hidden = YES;
    _noOrderView.hidden = YES;
    //        _centerOrderView.hidden = YES;
    _centerAlarmView.hidden = YES;
    [self updateAlarmUIWithText:@""];
    _noteTextView.text = @"";
    
    _window = [[UIWindow alloc] initWithFrame:[UIScreen mainScreen].bounds];
    [_window addSubview:self];
    [_window makeKeyAndVisible];
    _blurView.frame = _litleFrame;
    _blurView.alpha = 0;
    [UIView animateWithDuration:0.2 animations:^{
        _blurView.frame = CGRectMake((self.width - 270)/2, (self.height - 215)/2, 270, 215);
        _blurView.alpha = 1;
    }];
}

- (void)buttonTouched:(UIButton*)button {
    if (button == _alarmButton) {
        if (_item != nil) {
            if (_alarmButton.tag == 1) {
                TAlarmNoteItem* item = [TAlarmNoteItem getFromFile];
                item.alarmDate = nil;
                [item saveToFile];
                [[UIApplication sharedApplication] cancelAllLocalNotifications];
                
                [self updateAlarmUIWithText:@""];
            } else {
                _centerOrderView.hidden = YES;
                _centerAlarmView.hidden = NO;
            }
        } else {
        }
    } else {
        _centerOrderView.hidden = NO;
        _centerAlarmView.hidden = YES;
        if (button == _startButton) {
            int hours = [_datePicker selectedRowInComponent:0];
            int miniters = [_datePicker selectedRowInComponent:1];
            int seconds = hours * 60 * 60 + miniters * 60;
            if (seconds == 0)
                return;
            
            //更新alarmLabel内容 
            NSDate* date = [NSDate dateWithTimeIntervalSinceNow:seconds];
            NSDateFormatter* formatter = [[NSDateFormatter alloc] init];
            [formatter setDateFormat:@"将于dd日 HH:mm 提醒"];
            [self updateAlarmUIWithText:[formatter stringFromDate:date]];
            
            //取消前面所有的通知
            [[UIApplication sharedApplication] cancelAllLocalNotifications];
            UILocalNotification *notification = [[UILocalNotification alloc] init];
            notification.alertBody = @"定时提醒：时间到了";
            notification.soundName = UILocalNotificationDefaultSoundName;
            notification.fireDate = [NSDate dateWithTimeIntervalSinceNow:seconds];
            notification.timeZone = [NSTimeZone defaultTimeZone];
            [[UIApplication sharedApplication] scheduleLocalNotification:notification];
            
            TAlarmNoteItem* item = [[TAlarmNoteItem alloc] init];
            item.orderId = _item.orderid;
            item.alarmDate = date;
            item.note = _noteTextView.text;
            [item saveToFile];
//            notification.userInfo = @{@"info" : item};
            
        }
    }
}

- (void)setItem:(TCurrentOrderItem *)item {
    _hasOrderView.hidden = item ? NO : YES;
    _noOrderView.hidden = item ? YES : NO;
    _centerOrderView.hidden = NO;
    _centerAlarmView.hidden = YES;
    if (item) {
        _item = item;
        _nameLabel.text = item.parkname;
        _moneyLabel.text = [NSString stringWithFormat:@"¥%@", item.total];
        _stateLabel.text = @"未结算";
        _durationLabel.text = [TAPIUtility getDuration:([_item.etime integerValue] - [_item.btime integerValue])];
        
        NSDate* date = [NSDate dateWithTimeIntervalSince1970:[_item.btime integerValue]];
        NSDateFormatter* formatter = [[NSDateFormatter alloc] init];
        [formatter setDateFormat:@"yyyy-MM-dd HH:mm 入场"];
        _beginTimeLabel.text = [formatter stringFromDate:date];
        
        TAlarmNoteItem* alarmAndNote = [TAlarmNoteItem getFromFile];
        if (alarmAndNote && [alarmAndNote.orderId isEqualToString:item.orderid]) {
            //有订单且订单号相同 才可以显示
            if (alarmAndNote.alarmDate) {
                NSDate* alarmDate = alarmAndNote.alarmDate;
                NSDateFormatter* formatter = [[NSDateFormatter alloc] init];
                [formatter setDateFormat:@"将于dd日 HH:mm 提醒"];
                [self updateAlarmUIWithText:[formatter stringFromDate:alarmDate]];
            } else {
                [self updateAlarmUIWithText:@""];
            }
            _noteTextView.text = alarmAndNote.note;
        } else {
            [self updateAlarmUIWithText:@""];
            _noteTextView.text = @"";
            [self clearAlarmNote];
        }
        _alarmButton.userInteractionEnabled = YES;
        _noteTextView.userInteractionEnabled = YES;
    } else {
        _alarmButton.userInteractionEnabled = NO;
        _noteTextView.userInteractionEnabled = NO;
        [self clearAlarmNote];
    }
}

- (void)clearAlarmNote {
    [[UIApplication sharedApplication] cancelAllLocalNotifications];
    [TAlarmNoteItem removeFile];
}

- (void)updateAlarmUIWithText:(NSString*)text {
    _alarmLabel.text = text;
    if ([text isEqualToString:@""]) {
        [_alarmButton setBackgroundImage:[UIImage imageNamed:@"alarm.png"] forState:UIControlStateNormal];
        _alarmButton.tag = 0;
    } else {
        [_alarmButton setBackgroundImage:[UIImage imageNamed:@"alarm_cancel.png"] forState:UIControlStateNormal];
        _alarmButton.tag = 1;
    }
}

#pragma mark UITextViewDelegate
- (void)textViewDidBeginEditing:(UITextView *)textView {
    if ([textView.text isEqualToString:@"停哪了？记一下吧～"])
        textView.text = @"";
}

- (void)textViewDidEndEditing:(UITextView *)textView {
    if ([textView.text isEqualToString:@""]) {
        textView.text = @"停哪了？记一下吧～";
    }
}

- (BOOL)textView:(UITextView *)textView shouldChangeTextInRange:(NSRange)range replacementText:(NSString *)text {
    if ([text isEqualToString:@"\n"]) {
        [textView endEditing:YES];
        return NO;
    }
    return YES;
}

- (void)textViewDidChange:(UITextView *)textView {
//    NSLog(@"%d--", textView.text.length);
//    if (textView.text.length > 60) {
//        [TAPIUtility alertMessage:@"字数不能大于60"];
//        NSString* text = textView.text;
//        NSString* text2 = [text substringToIndex:60];
//        textView.text = text2;
//    }
    TAlarmNoteItem* item = [TAlarmNoteItem getFromFile];
    if (!item)
        item = [[TAlarmNoteItem alloc] init];
    item.orderId = _item.orderid;
    item.note = _noteTextView.text;
    [item saveToFile];
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
    attrString = [[NSAttributedString alloc] initWithString:[NSString stringWithFormat:@"%ld", (long)row] attributes:@{NSForegroundColorAttributeName : [UIColor whiteColor]}];
    return attrString;
}

#pragma mark public

@end
