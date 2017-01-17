//
//  TCollectorsListViewController.h
//  TingCheBao_user
//
//  Created by apple on 14/12/12.
//  Copyright (c) 2014年 zhenLaiDian. All rights reserved.
//

#import "TBaseViewController.h"

typedef enum {
    TCollectorsListMode_normal = 0,
    TCollectorsListMode_recent
} TCollectorsListMode;

@interface TCollectorsListViewController : TBaseViewController

@property(nonatomic, retain) NSString* parkId;
@property(nonatomic, retain) NSString* parkName;
@property(nonatomic, assign) TCollectorsListMode mode;

@end
