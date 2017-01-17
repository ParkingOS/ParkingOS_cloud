//
//  TSegmentControl.m
//  TingCheBao_user
//
//  Created by apple on 15/5/11.
//  Copyright (c) 2015年 zhenLaiDian. All rights reserved.
//

#import "TSegmentControl.h"

#define RGBCOLOR(r,g,b) [UIColor colorWithRed:(r)/255.0f green:(g)/255.0f blue:(b)/255.0f alpha:1]
#define green_color RGBCOLOR(52,157,92)

@interface TSegmentControl()

@property(nonatomic, retain) NSMutableArray* buttons;
@property(nonatomic, retain) NSMutableArray* lines;
@property(nonatomic, retain) UIView* greenView;
@property(nonatomic, retain) id target;
@property(nonatomic, assign) SEL action;

@end
@implementation TSegmentControl

- (id)init {
    if (self = [super init]) {
        _selectedIndex = 0;
        self.items = [NSMutableArray array];
    }
    return self;
}

- (id)initWithFrame:(CGRect)frame {
    if (self = [super initWithFrame:frame]) {
        _selectedIndex = 0;
        self.items = [NSMutableArray array];
    }
    return self;
}

- (id)initWithItems:(NSArray*)items {
    if (self = [super init]) {
        self.items = items;
    }
    return self;
}

- (void)layoutSubviews {
    [super layoutSubviews];
    if ([_items count] == 0) {
        return;
    }
    
    CGFloat buttonWidth = self.width/[_items count];
    for (int i = 0; i < [_items count]; i++) {
        UIButton* button = [_buttons objectAtIndex:i];
        button.frame = CGRectMake(buttonWidth * i, 0, buttonWidth, 40);
        
        if (i < [_items count] - 1) {
            UIView* lineView = [_lines objectAtIndex:i];
            lineView.frame = CGRectMake(button.right - 1, 10, 2, 20);
        }
    }
    
    _greenView.frame = CGRectMake(buttonWidth * _selectedIndex, 38, buttonWidth, 2);
}

- (void)setItems:(NSArray *)items {
    _items = items;
    [self initView];
}

- (void)initView {
    _buttons = [NSMutableArray array];
    _lines = [NSMutableArray array];
    
    for (UIView* subView in self.subviews) {
        [subView removeFromSuperview];
    }
    
    for (int i = 0; i < [_items count]; i++) {
        UIButton* button = [UIButton buttonWithType:UIButtonTypeCustom];
        [button setTitle:[_items objectAtIndex:i] forState:UIControlStateNormal];
        [button setTitleColor:i == _selectedIndex ? green_color : [UIColor grayColor] forState:UIControlStateNormal];
        [button addTarget:self action:@selector(buttonTouched:) forControlEvents:UIControlEventTouchUpInside];
        button.tag = 100+i;
        [_buttons addObject:button];
        [self addSubview:button];
        
        if (i < [_items count] - 1) {
            UIView* lineView = [[UIView alloc] init];
            lineView.backgroundColor = RGBCOLOR(220, 220, 220);
            lineView.tag = 200+i;
            [_lines addObject:lineView];
            [self addSubview:lineView];
        }
    }
    
    _greenView = [[UIView alloc] init];
    _greenView.backgroundColor = green_color;
    [self addSubview:_greenView];
    
    [self setNeedsLayout];
}

- (void)addTarget:(id)target action:(SEL)action {
    _target = target;
    _action = action;
}

- (void)buttonTouched:(UIButton*)button {
    if (_selectedIndex != button.tag - 100) {
        
        UIButton* oldButton = [_buttons objectAtIndex:_selectedIndex];
        UIButton* newButton = [_buttons objectAtIndex:button.tag - 100];
        
        [newButton setTitleColor:green_color forState:UIControlStateNormal];
        [oldButton setTitleColor:[UIColor grayColor] forState:UIControlStateNormal];
        [UIView animateWithDuration:0.1 animations:^{
            _greenView.left = (button.tag - 100) * (self.width/[_items count]);
        } completion:^(BOOL finished) {
        }];
        
        _selectedIndex = button.tag - 100;
        
        if ([_target respondsToSelector:_action])
            [_target performSelector:_action withObject:self];
    }
    
    
}
/*
// Only override drawRect: if you perform custom drawing.
// An empty implementation adversely affects performance during animation.
- (void)drawRect:(CGRect)rect {
    // Drawing code
}
*/

@end
