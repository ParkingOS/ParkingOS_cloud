//
//  TLocationViewController.h
//  
//
//  Created by apple on 15/6/25.
//
//

//地图上显示找车的路径
typedef enum {
    location_mode_choose,
    location_mode_show
} location_mode;

#import "TBaseViewController.h"

@interface TLocationViewController : TBaseViewController

@property(nonatomic, assign) location_mode mode;

- (id)initWithMode:(location_mode)mode;

@end
