//
//  TMyRedPackageViewController.h
//  TingCheBao_user
//
//  Created by yangshaojin on 15-3-10.
//  Copyright (c) 2015年 zhenLaiDian. All rights reserved.
//

#import "TShareView.h"
#import "CVAPIRequest.h"
#import "CVAPIRequestModel.h"
#import "TRedPackageCell.h"
#import "TParkTicketPackageViewController.h"
#import "TBaseViewController.h"

@interface TMyRedPackageViewController : TBaseViewController <CVAPIModelDelegate,
UITableViewDataSource,UITableViewDelegate,TShareViewDelegate>

- (void)shareRedPackageToWeixin:(TShareItem *)item;

@end
