//
//  TReaderViewController.m
//  TingCheBao_user
//
//  Created by apple on 14/12/31.
//  Copyright (c) 2014年 zhenLaiDian. All rights reserved.
//

#import "TReaderViewController.h"
#import "UIView+CVUIViewAdditions.h"
#import <AVFoundation/AVFoundation.h>
#import "CVAPIRequestModel.h"
#import "TAPIUtility.h"
#import "THomeViewController.h"
#import "TCurrentOrderItem.h"
#import "TPayCollectorViewController.h"
#import "TScanParkRedPackageItem.h"
#import "TRechargeWaysViewController.h"

#define bg_alpha 0.5

@interface TReaderViewController()<CVAPIModelDelegate>

@property (strong,nonatomic)AVCaptureDevice * device;
@property (strong,nonatomic)AVCaptureDeviceInput * input;
@property (strong,nonatomic)AVCaptureMetadataOutput * output;
@property (strong,nonatomic)AVCaptureSession * session;
@property (strong,nonatomic)AVCaptureVideoPreviewLayer * preview;

@property(nonatomic, retain) UIImageView* scanImgView;
@property(nonatomic, retain) UIImageView* lineImgView;

@property(nonatomic, retain) UIView* topView;
@property(nonatomic, retain) UIView* leftView;
@property(nonatomic, retain) UIView* rightView;
@property(nonatomic, retain) UIView* bottomView;

@property(nonatomic, retain) UILabel* alertLabel;

@property(nonatomic, retain) NSTimer* timer;
@property(nonatomic, retain) NSString* nid;

@property(nonatomic, retain) CVAPIRequest* request;
@property(nonatomic, retain) CVAPIRequest* codeRequest;

@end
@implementation TReaderViewController

- (id)init {
    if (self = [super init]) {
        _nid = @"";
        
        _scanImgView = [[UIImageView alloc] initWithImage:[UIImage imageNamed:@"capture.png"]];
        _scanImgView.frame = CGRectMake((self.view.width - 220)/2, 80, 220, 240);
        //    _scanImgView.frame = CGRectMake(0, 80, 220, 240);
        
        _lineImgView = [[UIImageView alloc] initWithImage:[UIImage imageNamed:@"scan.png"]];
        _lineImgView.frame = CGRectMake((self.view.width - 225)/2, _scanImgView.top, 225, 6);
        
        _topView = [[UIView alloc] initWithFrame:CGRectMake(0, 0, self.view.width, _scanImgView.top)];
        _topView.backgroundColor = [UIColor blackColor];
        _topView.alpha = bg_alpha;
        
        _leftView = [[UIView alloc] initWithFrame:CGRectMake(0, _topView.bottom, _scanImgView.left, _scanImgView.height)];
        _leftView.backgroundColor = [UIColor blackColor];
        _leftView.alpha = bg_alpha;
        
        _rightView = [[UIView alloc] initWithFrame:CGRectMake(_scanImgView.right, _topView.bottom, _leftView.width, _scanImgView.height)];
        _rightView.backgroundColor = [UIColor blackColor];
        _rightView.alpha = bg_alpha;
        
        _bottomView = [[UIView alloc] initWithFrame:CGRectMake(0, _scanImgView.bottom, self.view.width, 400)];
        _bottomView.backgroundColor = [UIColor blackColor];
        _bottomView.alpha = bg_alpha;
        
        _alertLabel = [[UILabel alloc] initWithFrame:CGRectMake(0, _scanImgView.bottom + 20, self.view.width, 30)];
        _alertLabel.backgroundColor = [UIColor clearColor];
        _alertLabel.textColor = [UIColor whiteColor];
        _alertLabel.text = @"将二维码放入框内，即可自动扫描";
        _alertLabel.textAlignment = NSTextAlignmentCenter;
        _alertLabel.font = [UIFont systemFontOfSize:12];
        
        //防止界面卡顿，先隐藏
        [self updateView:NO];
        
        [self.view addSubview:_scanImgView];
        [self.view addSubview:_lineImgView];
        
        [self.view addSubview:_topView];
        [self.view addSubview:_leftView];
        [self.view addSubview:_rightView];
        [self.view addSubview:_bottomView];
        [self.view addSubview:_alertLabel];
        
    }
    return self;
}

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view.
    self.titleView.text = @"二维码扫描";
    self.view.backgroundColor = [UIColor blackColor];
}

- (void)viewWillAppear:(BOOL)animated {
    [super viewWillAppear:animated];
    
}

- (void)viewDidAppear:(BOOL)animated {
    [super viewDidAppear:animated];
    
    //检测相机权限是否打开
    AVAuthorizationStatus authstatus = [AVCaptureDevice authorizationStatusForMediaType:AVMediaTypeVideo];
    if (authstatus ==AVAuthorizationStatusRestricted || authstatus ==AVAuthorizationStatusDenied) //用户关闭了权限
    {
        UIAlertView *alertView = [[UIAlertView alloc]initWithTitle:@"相机权限未开启" message:@"请在设置中允许打开相机" delegate:self cancelButtonTitle:@"确定" otherButtonTitles:nil];
        alertView.delegate =self;
        [alertView show];
    } else {
        [self setupCamera];
    }
}

- (void)viewWillDisappear:(BOOL)animated {
    [super viewWillDisappear:animated];
    [_request cancel];
    [_codeRequest cancel];
}

- (void)updateView:(BOOL) show{
    _lineImgView.hidden = !show;
    _topView.hidden = !show;
    _leftView.hidden = !show;
    _rightView.hidden = !show;
    _bottomView.hidden = !show;
}

- (void)setupCamera
{
    // Device
    _device = [AVCaptureDevice defaultDeviceWithMediaType:AVMediaTypeVideo];
    
    // Input
    _input = [AVCaptureDeviceInput deviceInputWithDevice:self.device error:nil];
    
    // Output
    _output = [[AVCaptureMetadataOutput alloc]init];
    [_output setMetadataObjectsDelegate:self queue:dispatch_get_main_queue()];
    
    // Session
    _session = [[AVCaptureSession alloc]init];
    [_session setSessionPreset:AVCaptureSessionPresetHigh];
    if ([_session canAddInput:self.input])
    {
        [_session addInput:self.input];
    }
    
    if ([_session canAddOutput:self.output])
    {
        [_session addOutput:self.output];
    }
    
    // 条码类型 AVMetadataObjectTypeQRCode
    //检查是否支持 解决bug：[AVCaptureMetadataOutput setMetadataObjectTypes:] - unsupported type foun
    NSArray* array = [_output availableMetadataObjectTypes];
    if (![array containsObject:AVMetadataObjectTypeQRCode]) {
        UIAlertView* alerView = [[UIAlertView alloc] initWithTitle:@"未知错误" message:@"扫描异常" delegate:nil cancelButtonTitle:@"确定" otherButtonTitles:nil];
        [alerView show];
        return;
    }
    _output.metadataObjectTypes =@[AVMetadataObjectTypeQRCode];
    
    // Preview
    _preview =[AVCaptureVideoPreviewLayer layerWithSession:self.session];
    _preview.videoGravity = AVLayerVideoGravityResizeAspectFill;
    _preview.frame = CGRectMake(0, 0, self.view.width, self.view.height);
    [self.view.layer insertSublayer:self.preview atIndex:0];
    
    // Start
    [_session startRunning];
    
    //显示view
    [self updateView:YES];
    //扫描条 滚动
    _timer = [NSTimer scheduledTimerWithTimeInterval:0.01 target:self selector:@selector(timeUpdate) userInfo:nil repeats:YES];
}

#pragma mark AVCaptureMetadataOutputObjectsDelegate
- (void)captureOutput:(AVCaptureOutput *)captureOutput didOutputMetadataObjects:(NSArray *)metadataObjects fromConnection:(AVCaptureConnection *)connection
{
    
    NSString *stringValue;
    
    if ([metadataObjects count] >0)
    {
        AVMetadataMachineReadableCodeObject * metadataObject = [metadataObjects objectAtIndex:0];
        stringValue = metadataObject.stringValue;
    }
    if (stringValue == nil) {
        return;
    }
    NSLog(@"%@",stringValue);
    
    //-----------------------------新版二维码 zld/qr/c/de1012101D2FAe08e70 再后面加上&mobile=15801482643
//    if ([stringValue rangeOfString:@"s.tingchebao.com/zld/qr/c"].length > 0) {
    if ([stringValue rangeOfString:@"/zld/qr/c"].length > 0) {
        [_session stopRunning];
        [_timer invalidate];
        [self requestCodeInfo:[NSString stringWithFormat:@"%@&mobile=%@", [stringValue substringFromIndex:[stringValue rangeOfString:@"qr/c"].location], [[NSUserDefaults standardUserDefaults] objectForKey:save_phone]]];
        return;
    }
    
    //-----------------------------旧版二维码扫描-------------------
    //订单查询
    NSRange range = [stringValue rangeOfString:@"nid="];
    if (range.length > 0) {
        _nid = [stringValue substringFromIndex:range.location + range.length];
        [_session stopRunning];
        [_timer invalidate];
        
        [self requestOrderInfo];
        return;
    }
    
    
    //扫描收费员 直付
    NSString *patternStr = [NSString stringWithFormat:@"^.*\\?pid=(\\d{1,10})&name=(.*)$"];
    NSRegularExpression *regularexpression = [[NSRegularExpression alloc]
                                              initWithPattern:patternStr
                                              options:NSRegularExpressionCaseInsensitive
                                              error:nil];
    //        NSString*link = @"http://www.tingchebao.com?pid=1000005&name=gao";
    NSString* link = stringValue;
    NSTextCheckingResult* result2 = [regularexpression firstMatchInString:link options:NSMatchingReportProgress range:NSMakeRange(0, link.length)];
    if ([result2 numberOfRanges] == 3) {
        //停止扫描
        [_session stopRunning];
        [_timer invalidate];
        
        NSString* pid = [link substringWithRange:[result2 rangeAtIndex:1]];
        NSString* name = [link substringWithRange:[result2 rangeAtIndex:2]];
        
        TPayCollectorViewController* vc = [[TPayCollectorViewController alloc] init];
        vc.parkName = _parkName;
        vc.collectorId = pid;
        vc.collectorName = name;
        UINavigationController* nv = self.navigationController;
        [self.navigationController popViewControllerAnimated:NO];
        [nv pushViewController:vc animated:YES];
        return;
    }
}

#pragma mark reqeust

- (void)requestOrderInfo {
    if (![[NSUserDefaults standardUserDefaults] objectForKey:save_phone]) {
        [TAPIUtility alertMessage:@"请先登录哦~"];
        return;
    }
    
    NSString* apiPath = [NSString stringWithFormat:@"nfchandle.do?action=coswipe&nid=%@&mobile=%@", _nid, [[NSUserDefaults standardUserDefaults] objectForKey:save_phone]];
    _request = [[CVAPIRequest alloc] initWithAPIPath:apiPath];
    _request.hud = [MBProgressHUD showHUDAddedTo:self.view animated:YES];
    _request.hud.labelText = @"已扫描，正在处理...";
    
    [self.model sendRequest:_request completion:^(NSDictionary *result, NSError *error) {
        if (result) {
            [self.navigationController popViewControllerAnimated:NO];
            if ([result count] == 0 || ![[result objectForKey:@"state"] isEqualToString:@"0"]) {
                [TAPIUtility alertMessage:@"您当前没有订单" afterDelay:2];
            } else {
                THomeViewController* home = [THomeViewController share];
                TCurrentOrderItem* item = [TCurrentOrderItem getItemFromDictionary:result];
                [home clickedQueryCurrOrderButton:item];
            }
        }
    }];
}

- (void)requestCodeInfo:(NSString*)code {
    if (![[NSUserDefaults standardUserDefaults] objectForKey:save_phone]) {
        [TAPIUtility alertMessage:@"请先登录哦~"];
        return;
    }
    
    _codeRequest = [[CVAPIRequest alloc] initWithAPIPath:code];
    _codeRequest.hud = [MBProgressHUD showHUDAddedTo:self.view animated:YES];
    _codeRequest.hud.labelText = @"已扫描，正在处理...";
    
    [self.model sendRequest:_codeRequest completion:^(NSDictionary *result, NSError *error) {
        if (result) {
            //            [self.navigationController popViewControllerAnimated:NO];
            //            if ([result count] == 0 || ![[result objectForKey:@"state"] isEqualToString:@"0"]) {
            //                [TAPIUtility alertMessage:@"您当前没有订单" afterDelay:2];
            //            } else {
            //                THomeViewController* home = [THomeViewController share];
            //                TCurrentOrderItem* item = [TCurrentOrderItem getItemFromDictionary:result];
            //                [home clickedQueryCurrOrderButton:item];
            //            }
            
            if ([[result objectForKey:@"type"] isEqualToString:@"2"] && [[result objectForKey:@"info"] count] != 0) {
                //当前订单
                //                {"type":"2","info":{"total":"0.0","parkname":"停车宝测试车场（请勿购买产品）","address":"北京市北京市海淀区上地信息路26号","etime":"1430221740","state":"0","btime":"1430190660","parkid":"3","orderid":"786158"}}
                
                THomeViewController* home = [THomeViewController share];
                TCurrentOrderItem* item = [TCurrentOrderItem getItemFromDictionary:[result objectForKey:@"info"]];
                [self.navigationController popToRootViewControllerAnimated:YES];
                [home clickedQueryCurrOrderButton:item];
            } else if ([[result objectForKey:@"type"] isEqualToString:@"1"]) {
                //收费员
                //                {"type":"1","info":{"name":"大牛","uid":"10700","parkname":"停车宝演示停车场（测试）", "total":"2.0"}}
                
                NSString* total = [[result objectForKey:@"info"] objectForKey:@"total"];
                NSString* collectorId = [[result objectForKey:@"info"] objectForKey:@"id"];
                NSString* collectorName = [[result objectForKey:@"info"] objectForKey:@"name"];
                
                if (total && ![total isEqualToString:@""]) {
                    TRechargeWaysViewController* vc = [[TRechargeWaysViewController alloc] init];
                    vc.collectorId = collectorId;
                    vc.name = [NSString stringWithFormat:@"%@:%@", collectorName, collectorId];
                    vc.price = total;
                    vc.rechargeMode = RechargeMode_collector;
                    
                    UINavigationController* nv = self.navigationController;
                    [self.navigationController popViewControllerAnimated:NO];
                    [nv pushViewController:vc animated:YES];
                    return;
                    
                } else {
                    TPayCollectorViewController* vc = [[TPayCollectorViewController alloc] init];
                    vc.parkName = [[result objectForKey:@"info"] objectForKey:@"parkname"];
                    vc.collectorId = collectorId;
                    vc.collectorName = collectorName;
                    
                    UINavigationController* nv = self.navigationController;
                    [self.navigationController popViewControllerAnimated:NO];
                    [nv pushViewController:vc animated:YES];
                    return;
                }
                
                
            } else if ([[result objectForKey:@"type"] isEqualToString:@"3"]) {
                //扫描收费员的红包
                //{"type":"3","info":{fee={id=10700, name=孙晓超}, id=1633818, money=3, type=1, cname=停车宝演示车场（线上/请勿购买}}
                [self.navigationController popToRootViewControllerAnimated:YES];
                
                //两种出错的情况
                NSString* id2 = [[result objectForKey:@"info"] objectForKey:@"id"];
                if ([id2 isEqualToString:@"-2"]) {
                    [TAPIUtility alertMessage:@"停车券已被领取!" afterDelay:2];
                    return;
                } else if ([id2 isEqualToString:@"-1"]) {
                    [TAPIUtility alertMessage:@"二维码已失效!" afterDelay:2];
                    return;
                }
                
                TScanParkRedPackageItem* item = [[TScanParkRedPackageItem alloc] init];
                item.collectorId = [[[result objectForKey:@"info"] objectForKey:@"fee"] objectForKey:@"id"];
                item.collectorName = [[[result objectForKey:@"info"] objectForKey:@"fee"] objectForKey:@"name"];
                item.money = [[result objectForKey:@"info"] objectForKey:@"money"];
                item.cname = [[result objectForKey:@"info"] objectForKey:@"cname"];
                THomeViewController* home = [THomeViewController share];
                [home showCollectorRedPackageView:item];

            } else {
                [TAPIUtility alertMessage:@"没有查到对应的结果" afterDelay:2];
                [self.navigationController popViewControllerAnimated:NO];
            }
        }
    }];
}

//动画
- (void)timeUpdate
{
    _lineImgView.top += 1.5;
    if (_lineImgView.top >= _scanImgView.bottom)
        _lineImgView.top = _scanImgView.top;
}

#pragma mark CVAPIModelDelegate

- (void)modelDidFailWithError:(NSError *)error model:(CVAPIRequestModel *)model request:(CVAPIRequest *)request {
    [_session startRunning];
    
    _timer = [NSTimer scheduledTimerWithTimeInterval:0.01 target:self selector:@selector(timeUpdate) userInfo:nil repeats:YES];
    [TAPIUtility alertMessage:@"加载超时，请重新扫描" success:NO toViewController:self];
}

@end
