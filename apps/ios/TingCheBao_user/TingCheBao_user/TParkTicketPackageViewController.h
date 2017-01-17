//
//  TParkTicketPackageViewController.h
//  TingCheBao_user
//
//  Created by yangshaojin on 15-3-9.
//  Copyright (c) 2015年 zhenLaiDian. All rights reserved.
//

#import "TAPIUtility.h"
#import "CVAPIRequest.h"
#import "CVAPIRequestModel.h"
#import "TBaseViewController.h"

@class TShareItem;

@interface TParkTicketPackageViewController : TBaseViewController <UITextViewDelegate,CVAPIModelDelegate>

@property(nonatomic, retain) NSString* boundId;

@end
