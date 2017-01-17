//
//  MyAnnotation.h
//  BaiduMap
//
//  Created by apple on 14-8-13.
//  Copyright (c) 2014年 zhenLaiDian. All rights reserved.
//

#import <BaiduMapAPI/BMKPointAnnotation.h>
#import "TParkItem.h"

@interface MyAnnotation : BMKPointAnnotation

@property(nonatomic, retain) TParkItem* item;

@end
