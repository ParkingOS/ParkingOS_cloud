//
//  TPriceViewController.h
//  TingCheBao_user
//
//  Created by apple on 14-9-11.
//  Copyright (c) 2014年 zhenLaiDian. All rights reserved.
//

#import "TBaseViewController.h"
#import "TPriceItem.h"

//车场价格详情页面

@interface TPriceViewController : TBaseViewController

@property(nonatomic, retain) NSString* parkId;
@property(nonatomic, retain) TPriceItem* item;

@end
