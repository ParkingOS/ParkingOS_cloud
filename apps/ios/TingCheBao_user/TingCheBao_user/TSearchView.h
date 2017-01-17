//
//  TSearchView.h
//  TingCheBao_user
//
//  Created by apple on 15/3/9.
//  Copyright (c) 2015年 zhenLaiDian. All rights reserved.
//

//home页面底部
#import <UIKit/UIKit.h>

typedef enum {
    SearchViewMode_normal = 0,
    SearchViewMode_upload,
    SearchViewMode_money
}SearchViewMode;

@protocol TSearchViewDelegate <NSObject>

- (void)searchViewClicked:(NSInteger)index;

@end
@interface TSearchView : UIView

@property(nonatomic, unsafe_unretained) id<TSearchViewDelegate>delegate;
@property(nonatomic, assign) SearchViewMode mode;
@property(nonatomic, retain) UIButton* addButton;
@property(nonatomic, retain) NSNumber* redNumber;

- (void)setMode:(SearchViewMode)mode;
- (void)resetOrginTop;

@end
