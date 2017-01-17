//
//  TChooseCityView.h
//  TingCheBao_user
//
//  Created by apple on 14/11/18.
//  Copyright (c) 2014年 zhenLaiDian. All rights reserved.
//

#import <UIKit/UIKit.h>

//这个类暂时废弃
//这个类暂时废弃
//这个类暂时废弃
@protocol TChooseCityViewDelegate <NSObject>

- (void)chooseCityViewWithIndex:(int)index;

@end
@interface TChooseCityView : UIView

@property(nonatomic, unsafe_unretained) id<TChooseCityViewDelegate> delegate;

@end
