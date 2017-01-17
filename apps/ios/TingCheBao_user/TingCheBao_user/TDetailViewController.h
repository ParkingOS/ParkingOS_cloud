//
//  TDetailViewController.h
//  TingCheBao_user
//
//  Created by apple on 14-9-2.
//  Copyright (c) 2014年 zhenLaiDian. All rights reserved.
//

#import "TBaseViewController.h"
#import <CoreLocation/CoreLocation.h>


@interface TDetailViewController : TBaseViewController

@property(nonatomic, retain) NSString* parkId;
@property(nonatomic, retain) NSString* parkName;

@property(nonatomic, assign) CLLocationCoordinate2D parkLocation;
@property(nonatomic, assign) CLLocationCoordinate2D selfLocation;

@end
