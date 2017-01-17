//
//  TParkMonthViewController.h
//  TingCheBao_user
//
//  Created by apple on 14-9-16.
//  Copyright (c) 2014年 zhenLaiDian. All rights reserved.
//

#import "TBaseViewController.h"
#import <CoreLocation/CoreLocation.h>

@interface TParkMonthViewController : TBaseViewController

@property(nonatomic, retain) NSArray* parkIds;
@property(nonatomic, assign) CLLocationCoordinate2D coordinate;//自己的坐标
@property(nonatomic, retain) NSString* address;

@end
