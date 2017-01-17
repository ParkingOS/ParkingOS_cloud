//
//  TReaderViewController.h
//  TingCheBao_user
//
//  Created by apple on 14/12/31.
//  Copyright (c) 2014年 zhenLaiDian. All rights reserved.
//

//二维码描述
#import <UIKit/UIKit.h>
#import "TBaseViewController.h"
#import <AVFoundation/AVFoundation.h>

typedef enum {
    TReaderMode_currentOrder = 0,
    TReaderMode_collector,
    TReaderMode_mix
} TReaderMode;

@interface TReaderViewController : TBaseViewController<AVCaptureMetadataOutputObjectsDelegate>

//mode TreaderMode_collector需要
@property(nonatomic, retain) NSString* parkName;

@end
