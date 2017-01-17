//
//  UIKeyboardView.h
//
//
//  Created by  YFengchen on 13-1-4.
//  Copyright 2013 __zhongyan__. All rights reserved.
//

#import <UIKit/UIKit.h>

@protocol UIKeyboardViewDelegate;

@interface UIKeyboardView : UIView
{
	 __unsafe_unretained id <UIKeyboardViewDelegate> _delegate;
	UIToolbar *keyboardToolbar;
}

@property (nonatomic, assign) id <UIKeyboardViewDelegate> delegate;

@end

@interface UIKeyboardView (UIKeyboardViewAction)

- (UIBarButtonItem *)itemForIndex:(NSInteger)itemIndex;

@end

@protocol UIKeyboardViewDelegate <NSObject>

- (void)toolbarButtonTap:(UIButton *)button;

@optional

@end
