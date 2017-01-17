//
//  UIKeyboardViewController.m
// 
//
//  Created by  YFengchen on 13-1-4.
//  Copyright 2013 __zhongyan__. All rights reserved.
//

#import "UIKeyboardViewController.h"
#define IOS_VERSION [[[UIDevice currentDevice] systemVersion] floatValue]
static CGFloat kboardHeight = 254.0f;
static CGFloat keyBoardToolbarHeight = 38.0f;
static CGFloat spacerY = 10.0f;
static CGFloat viewFrameY = 0;

@interface UIKeyboardViewController () 

- (void)animateView:(BOOL)isShow textField:(id)textField heightforkeyboard:(CGFloat)kheight;
- (void)addKeyBoardNotification;
- (void)removeKeyBoardNotification;
- (void)checkBarButton:(id)textField;
- (id)firstResponder:(UIView *)navView;
- (NSArray *)allSubviews:(UIView *)theView;
- (void)resignKeyboard:(UIView *)resignView;

@end

@implementation UIKeyboardViewController

@synthesize boardDelegate = _boardDelegate;

- (void)dealloc {
    _boardDelegate = nil;
	[self removeKeyBoardNotification];
//	[super dealloc];
}

//监听键盘隐藏和显示事件
- (void)addKeyBoardNotification {
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(keyboardWillShowOrHide:) name:UIKeyboardWillShowNotification object:nil];
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(keyboardWillShowOrHide:) name:UIKeyboardWillHideNotification object:nil];
}

//注销监听事件
- (void)removeKeyBoardNotification {
	[[NSNotificationCenter defaultCenter] removeObserver:self name:UIKeyboardWillShowNotification object:nil];
	[[NSNotificationCenter defaultCenter] removeObserver:self name:UIKeyboardWillHideNotification object:nil];
}

//计算当前键盘的高度
-(void)keyboardWillShowOrHide:(NSNotification *)notification
{
#if __IPHONE_OS_VERSION_MAX_ALLOWED >= __IPHONE_3_2
	if (UI_USER_INTERFACE_IDIOM() == UIUserInterfaceIdiomPad)
    {
#endif
		kboardHeight = 264.0f + keyBoardToolbarHeight;
	}
	NSValue *keyboardBoundsValue;
	if (IOS_VERSION >= 3.2)
    {
		keyboardBoundsValue = [[notification userInfo] objectForKey:UIKeyboardFrameEndUserInfoKey];
	}
	else {
		keyboardBoundsValue = [[notification userInfo] objectForKey:UIKeyboardBoundsUserInfoKey];
	}
	[keyboardBoundsValue getValue:&keyboardBounds];
	BOOL isShow = [[notification name] isEqualToString:UIKeyboardWillShowNotification] ? YES : NO;
	if ([self firstResponder:objectView])
    {
		[self animateView:isShow textField:[self firstResponder:objectView]
		heightforkeyboard:keyboardBounds.size.height];
	}
}
-(void)textViewDidBeginEditing:(UITextView *)textView
{
//textView.text=@"";
    textView.textColor=[UIColor blackColor];

}
//输入框上移防止键盘遮挡
- (void)animateView:(BOOL)isShow textField:(id)textField heightforkeyboard:(CGFloat)kheight {
	kboardHeight = kheight;
	[self checkBarButton:textField];
	CGRect rect = objectView.frame;
	[UIView beginAnimations:nil context:NULL];
	[UIView setAnimationDuration:0.3];
	if (isShow) {
		if ([textField isKindOfClass:[UITextField class]]) {
			UITextField *newText = ((UITextField *)textField);
			CGPoint textPoint = [newText convertPoint:CGPointMake(0, newText.frame.size.height + spacerY) toView:objectView];
			if (rect.size.height - textPoint.y < kheight)
				rect.origin.y = rect.size.height - textPoint.y - kheight + viewFrameY;
			else rect.origin.y = viewFrameY;
		}
		else {
			UITextView *newView = ((UITextView *)textField);
			CGPoint textPoint = [newView convertPoint:CGPointMake(0, newView.frame.size.height + spacerY) toView:objectView];
			if (rect.size.height - textPoint.y < kheight) 
				rect.origin.y = rect.size.height - textPoint.y - kheight + viewFrameY;
			else rect.origin.y = viewFrameY;
		}
	}
	else rect.origin.y = viewFrameY;
	objectView.frame = rect;
	[UIView commitAnimations];
}

//输入框获得焦点
- (id)firstResponder:(UIView *)navView {
	for (id aview in [self allSubviews:navView]) {
		if ([aview isKindOfClass:[UITextField class]] && [(UITextField *)aview isFirstResponder]) {
			return (UITextField *)aview;
		}
		else if ([aview isKindOfClass:[UITextView class]] && [(UITextView *)aview isFirstResponder]) {
			return (UITextView *)aview;
		}
	}
	return NO;
}

//找出所有的subview
- (NSArray *)allSubviews:(UIView *)theView {
	NSArray *results = [theView subviews];
	for (UIView *eachView in [theView subviews]) {
		NSArray *riz = [self allSubviews:eachView];
		if (riz) {
			results = [results arrayByAddingObjectsFromArray:riz];
		}
	}
	return results;
}

//输入框失去焦点，隐藏键盘
- (void)resignKeyboard:(UIView *)resignView {
	[[[UIApplication sharedApplication] keyWindow] endEditing:YES];
}

//设置previousBarItem或nextBarItem是否允许点击
- (void)checkBarButton:(id)textField {
	int i = 0,j = 0;
	UIBarButtonItem *previousBarItem = [keyboardToolbar itemForIndex:0];
    UIBarButtonItem *nextBarItem = [keyboardToolbar itemForIndex:1];
	for (id aview in [self allSubviews:objectView]) {
		if ([aview isKindOfClass:[UITextField class]] && ((UITextField*)aview).userInteractionEnabled && ((UITextField*)aview).enabled) {
			i++;
			if ([(UITextField *)aview isEqual:textField]) {
				j = i;
			}
		}
		else if ([aview isKindOfClass:[UITextView class]] && ((UITextView*)aview).userInteractionEnabled && ((UITextView*)aview).editable) {
			i++;
			if ([(UITextView *)aview isEqual:textField]) {
				j = i;
			}
		}
	}
	[previousBarItem setEnabled:j > 1 ? YES : NO];
    [((UIImageView*)previousBarItem.customView.subviews[0]) setImage:[UIImage imageNamed:j>1 ? @"left_blue_keyboard" : @"left_gray_keyboard"]];
	[nextBarItem setEnabled:j < i ? YES : NO];
    [((UIImageView*)nextBarItem.customView.subviews[0]) setImage:[UIImage imageNamed:j<i ? @"right_blue_keyboard" : @"right_gray_keyboard"]];
}

//toolbar button点击事件
#pragma mark - UIKeyboardView delegate methods
-(void)toolbarButtonTap:(UIButton *)button {
	NSInteger buttonTag = button.tag;
	NSMutableArray *textFieldArray=[NSMutableArray arrayWithCapacity:10];
	for (id aview in [self allSubviews:objectView]) {
		if ([aview isKindOfClass:[UITextField class]] && ((UITextField*)aview).userInteractionEnabled && ((UITextField*)aview).enabled) {
			[textFieldArray addObject:(UITextField *)aview];
		}
		else if ([aview isKindOfClass:[UITextView class]] && ((UITextView*)aview).userInteractionEnabled && ((UITextView*)aview).editable) {
			[textFieldArray addObject:(UITextView *)aview];
		}
	}
	for (int i = 0; i < [textFieldArray count]; i++) {
		id textField = [textFieldArray objectAtIndex:i];
		if ([textField isKindOfClass:[UITextField class]]) {
			textField = ((UITextField *)textField);
		}
		else {
			textField = ((UITextView *)textField);
		}
		if ([textField isFirstResponder]) {
			if (buttonTag == 1) {
				if (i > 0) {
					[[textFieldArray objectAtIndex:--i] becomeFirstResponder];
					[self animateView:YES textField:[textFieldArray objectAtIndex:i] heightforkeyboard:kboardHeight];
				}
			}
			else if (buttonTag == 2) {
				if (i < [textFieldArray count] - 1) {
					[[textFieldArray objectAtIndex:++i] becomeFirstResponder];
					[self animateView:YES textField:[textFieldArray objectAtIndex:i] heightforkeyboard:kboardHeight];
				}
			}
		}
	}
	if (buttonTag == 3) 
		[self resignKeyboard:objectView];
}


#pragma mark - TextField delegate methods
- (void)textFieldDidBeginEditing:(UITextField *)textField {
	[self checkBarButton:textField];
}

- (BOOL)textFieldShouldReturn:(UITextField *)textField {
	[textField resignFirstResponder];
	return YES;
}

- (void)textFieldDidEndEditing:(UITextField *)textField {
	if ([self.boardDelegate respondsToSelector:@selector(alttextFieldDidEndEditing:)]) {
		[self.boardDelegate alttextFieldDidEndEditing:textField];
	}
}

#pragma mark - UITextView delegate methods
- (BOOL)textView:(UITextView *)textView shouldChangeTextInRange:(NSRange)range replacementText:(NSString *)text {    
	if ([text isEqualToString:@"\n"]) {    
		[textView resignFirstResponder];    
		return NO;    
	}
	return YES;    
}

- (void)textViewDidEndEditing:(UITextView *)textView {
	if ([self.boardDelegate respondsToSelector:@selector(alttextViewDidEndEditing:)]) {
		[self.boardDelegate alttextViewDidEndEditing:textView];
	}
}

@end

@implementation UIKeyboardViewController (UIKeyboardViewControllerCreation)

- (id)initWithControllerDelegate:(id <UIKeyboardViewControllerDelegate>)delegateObject {
	if (self = [super init]) {
		self.boardDelegate = delegateObject;
        if ([self.boardDelegate isKindOfClass:[UIViewController class]]) {
			objectView = [(UIViewController *)[self boardDelegate] view];
		}
		else if ([self.boardDelegate isKindOfClass:[UIView class]]) {
			objectView = (UIView *)[self boardDelegate];
		}
        viewFrameY = objectView.frame.origin.y;
		[self addKeyBoardNotification];
	}
	return self;
}

@end

@implementation UIKeyboardViewController (UIKeyboardViewControllerAction)

//给键盘加上toolbar
- (void)addToolbarToKeyboard {
	keyboardToolbar = [[UIKeyboardView alloc] initWithFrame:CGRectMake(0, 0, objectView.frame.size.width, keyBoardToolbarHeight)];
	keyboardToolbar.delegate = self;
	for (id aview in [self allSubviews:objectView]) {
		if ([aview isKindOfClass:[UITextField class]]) {
			((UITextField *)aview).inputAccessoryView = keyboardToolbar;
			((UITextField *)aview).delegate = self;
		}
		else if ([aview isKindOfClass:[UITextView class]]) {
			((UITextView *)aview).inputAccessoryView = keyboardToolbar;
			((UITextView *)aview).delegate = self;
		}
	}
//	[keyboardToolbar release];
}

@end
