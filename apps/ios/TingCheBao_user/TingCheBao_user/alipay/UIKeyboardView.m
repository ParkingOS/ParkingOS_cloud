//
//  UIKeyboardView.m
//
//
//  Created by  YFengchen on 13-1-4.
//  Copyright 2013 __zhongyan__. All rights reserved.
//

#import "UIKeyboardView.h"

@interface UIKeyboardView()

@property(nonatomic, retain) UIImageView* leftImageView;
@property(nonatomic, retain) UIImageView* rightImageView;

@end

@implementation UIKeyboardView

@synthesize delegate = _delegate;

- (id)initWithFrame:(CGRect)frame {
    
    self = [super initWithFrame:frame];
    if (self) {
		keyboardToolbar = [[UIToolbar alloc] initWithFrame:frame];
		
		keyboardToolbar.barStyle = UIBarStyleDefault;
		
//		UIBarButtonItem *previousBarItem = [[UIBarButtonItem alloc] initWithTitle:NSLocalizedString(@"上一行", @"")
//																			style:UIBarButtonItemStyleBordered
//																		   target:self
//																		   action:@selector(toolbarButtonTap:)];
        UIButton* leftView = [[UIButton alloc] initWithFrame:CGRectMake(0, 0, 30, frame.size.height)];
		leftView.tag=1;
        [leftView addTarget:self action:@selector(toolbarButtonTap:) forControlEvents:UIControlEventTouchUpInside];
        _leftImageView = [[UIImageView alloc] initWithImage:[UIImage imageNamed:@"left_gray_keyboard"]];
        _leftImageView.frame = CGRectMake(9, 10, 12, 22);
        [leftView addSubview:_leftImageView];
        UIBarButtonItem* previousBarItem = [[UIBarButtonItem alloc] initWithCustomView:leftView];
		
//		UIBarButtonItem *nextBarItem = [[UIBarButtonItem alloc] initWithTitle:NSLocalizedString(@"下一行", @"")
//																		style:UIBarButtonItemStyleBordered
//																	   target:self
//																	   action:@selector(toolbarButtonTap:)];
        UIButton* rightView = [[UIButton alloc] initWithFrame:CGRectMake(0, 0, 30, frame.size.height)];
		rightView.tag=2;
        _rightImageView = [[UIImageView alloc] initWithImage:[UIImage imageNamed:@"right_gray_keyboard"]];
        _rightImageView.frame = CGRectMake(9, 10, 12, 22);
        [rightView addTarget:self action:@selector(toolbarButtonTap:) forControlEvents:UIControlEventTouchUpInside];
        [rightView addSubview:_rightImageView];
        UIBarButtonItem* nextBarItem = [[UIBarButtonItem alloc] initWithCustomView:rightView];
		
		UIBarButtonItem *spaceBarItem = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemFlexibleSpace
																					  target:nil
																					  action:nil];
		
		UIBarButtonItem *doneBarItem = [[UIBarButtonItem alloc] initWithTitle:NSLocalizedString(@"完成", @"")
																		style:UIBarButtonItemStyleDone
																	   target:self
																	   action:@selector(toolbarButtonTap:)];
		doneBarItem.tag=3;
		
		[keyboardToolbar setItems:[NSArray arrayWithObjects:previousBarItem, nextBarItem, spaceBarItem, doneBarItem, nil]];
        [self addSubview:keyboardToolbar];
    }
    return self;
}

- (void)toolbarButtonTap:(UIButton *)button {
	if ([self.delegate respondsToSelector:@selector(toolbarButtonTap:)]) {
		[self.delegate toolbarButtonTap:button];
	}
}

@end

@implementation UIKeyboardView (UIKeyboardViewAction)

//根据index找出对应的UIBarButtonItem
- (UIBarButtonItem *)itemForIndex:(NSInteger)itemIndex {
	if (itemIndex < [[keyboardToolbar items] count]) {
		return [[keyboardToolbar items] objectAtIndex:itemIndex];
	}
	return nil;
}

@end
