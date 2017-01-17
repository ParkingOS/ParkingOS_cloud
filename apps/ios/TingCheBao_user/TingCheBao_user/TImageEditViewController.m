//
//  TImageEditViewController.m
//  
//
//  Created by apple on 15/7/14.
//
//

#import "TImageEditViewController.h"

#define scale 1

@interface TImageEditViewController ()<UIScrollViewDelegate>

@property(nonatomic, retain) UIView* mainView;
@property(nonatomic, retain) UIScrollView* imgScrollView;
@property(nonatomic, retain) UIImageView* imgView;

@property(nonatomic, retain) UIImageView* overView;
@property(nonatomic, retain) UIImageView* borderImgView;

@property(nonatomic, copy) ImageEditHandle completeHandle;

@end

@implementation TImageEditViewController

- (id)initWithImage:(UIImage*)image completeHandle:(ImageEditHandle)handle {
    if (self = [super init]) {
        _completeHandle = handle;
        
        _mainView = [[UIView alloc] initWithFrame:self.view.frame];
        _imgScrollView = [[UIScrollView alloc] initWithFrame:self.view.frame];
//        _imgScrollView.contentSize = _imgScrollView.frame.size;
        _imgScrollView.contentSize = CGSizeMake(scale * self.view.width, scale * self.view.height);
        _imgScrollView.backgroundColor = [UIColor clearColor];
        _imgScrollView.delegate = self;
        _imgScrollView.maximumZoomScale = 4.0;
        _imgScrollView.minimumZoomScale = 1/scale;//如果缩放太小，有异常
        
        //图片
        _imgView = [[UIImageView alloc] initWithImage:image];
        _imgView.backgroundColor = [UIColor blackColor];
        _imgView.contentMode = UIViewContentModeScaleAspectFit;
        _imgView.frame = CGRectMake(0, 0, scale * self.view.width, scale * self.view.height);
        
        [_imgScrollView addSubview:_imgView];
        [_mainView addSubview:_imgScrollView];
        
        //灰色背景
        _overView = [[UIImageView alloc] init];
        _overView.frame = self.view.frame;
        _overView.image = [self getOverImg];
        
        //边框
        _borderImgView = [[UIImageView alloc] init];
        _borderImgView.frame = CGRectMake(0, (self.view.height - self.view.width/3*2)/2, self.view.width, self.view.width/3*2);
        _borderImgView.image = [UIImage imageNamed:@"border_edit.png"];
        
        [self.view addSubview:_mainView];
        [self.view addSubview:_overView];
        [self.view addSubview:_borderImgView];
    }
    return self;
}
- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view.
    self.view.backgroundColor = [UIColor blackColor];
}

- (void)viewWillAppear:(BOOL)animated {
    [super viewWillAppear:animated];
    UIBarButtonItem* rightItem = [[UIBarButtonItem alloc] initWithTitle:@"保存" style:UIBarButtonItemStyleDone target:self action:@selector(rightItemTouched:)];
    self.navigationItem.rightBarButtonItem = rightItem;
}

- (void)rightItemTouched:(UIBarButtonItem*)item {
    UIImage* img = [self getPartOfImage];
    [self.navigationController popViewControllerAnimated:YES];
    
    _completeHandle(img);
}

- (UIImage*)getOverImg {
    UIGraphicsBeginImageContext(self.view.frame.size);
    CGContextRef ctx = UIGraphicsGetCurrentContext();
    CGContextSetRGBFillColor(ctx, 0,0,0,0.7);
    CGRect drawRect =CGRectMake(0, 0, self.view.width, self.view.height);
    
    CGContextFillRect(ctx, drawRect);   //draw the transparent layer
    
    drawRect = CGRectMake(0, (self.view.height - self.view.width/3*2)/2, self.view.width, self.view.width/3*2);
    CGContextClearRect(ctx, drawRect);  //clear the center rect  of the layer
    
    
    UIImage* returnimage = UIGraphicsGetImageFromCurrentImageContext();
    UIGraphicsEndImageContext();
    return returnimage;
}

// get part of the image
- (UIImage *)getPartOfImage
{
    UIGraphicsBeginImageContext(self.view.frame.size);
    CGContextRef context = UIGraphicsGetCurrentContext();
    [_mainView.layer renderInContext:context];
    UIImage* img = UIGraphicsGetImageFromCurrentImageContext();
    UIGraphicsEndImageContext();
    
    CGRect rect = CGRectMake(0, (self.view.height - self.view.width/3*2)/2, self.view.width, self.view.width/3*2);

    CGImageRef imageRef = img.CGImage;
    CGImageRef imagePartRef = CGImageCreateWithImageInRect(imageRef, rect);
    UIImage *retImg = [UIImage imageWithCGImage:imagePartRef];
    CGImageRelease(imagePartRef);
    return retImg;
}

#pragma mark UIScrollViewDelegate

- (UIView*)viewForZoomingInScrollView:(UIScrollView *)scrollView {
    return _imgView;
}

@end
