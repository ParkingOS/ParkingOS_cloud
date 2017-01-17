//
//  TBlurView.m
//  TingCheBao_user
//
//  Created by apple on 14/12/9.
//  Copyright (c) 2014年 zhenLaiDian. All rights reserved.
//

#import "TBlurView.h"
#import <Accelerate/Accelerate.h>

@interface TBlurView ()

@property(nonatomic, retain) UIImageView* imgView;
@property(nonatomic, retain) UIView* foreView;

@end

@implementation TBlurView

- (id)initWithFrame:(CGRect)frame image:(UIImage*)image alpha:(float)alpha{
    if (self = [super initWithFrame:frame]) {
        _imgView = [[UIImageView alloc] initWithFrame:CGRectMake(0, 0, self.width, self.height)];
        _imgView.image = [self blurryImage:image withBlurLevel:0.2];
        
        _foreView = [[UIView alloc] initWithFrame:_imgView.frame];
        _foreView.backgroundColor = [UIColor blackColor];
        _foreView.alpha = alpha;
        
        [self addSubview:_imgView];
        [self addSubview:_foreView];
    }
    return self;
}

- (UIImage *)blurryImage:(UIImage *)image withBlurLevel:(CGFloat)blur {
    if (blur < 0.f || blur > 1.f) {
        blur = 0.5f;
    }
    int boxSize = (int)(blur * 100);
    boxSize = boxSize - (boxSize % 2) + 1;
    
    CGImageRef img = image.CGImage;
    
    vImage_Buffer inBuffer, outBuffer, outBuffer2;
    vImage_Error error;
    
    void *pixelBuffer;
    
    CGDataProviderRef inProvider = CGImageGetDataProvider(img);
    CFDataRef inBitmapData = CGDataProviderCopyData(inProvider);
    
    inBuffer.width = CGImageGetWidth(img);
    inBuffer.height = CGImageGetHeight(img);
    inBuffer.rowBytes = CGImageGetBytesPerRow(img);
    
    inBuffer.data = (void*)CFDataGetBytePtr(inBitmapData);
    
    pixelBuffer = malloc(CGImageGetBytesPerRow(img) *
                         CGImageGetHeight(img));
    
    if(pixelBuffer == NULL)
        NSLog(@"No pixelbuffer");
    
    outBuffer.data = pixelBuffer;
    outBuffer.width = CGImageGetWidth(img);
    outBuffer.height = CGImageGetHeight(img);
    outBuffer.rowBytes = CGImageGetBytesPerRow(img);
    outBuffer2.data = pixelBuffer;
    outBuffer2.width = CGImageGetWidth(img);
    outBuffer2.height = CGImageGetHeight(img);
    outBuffer2.rowBytes = CGImageGetBytesPerRow(img);
    
    error = vImageBoxConvolve_ARGB8888(&inBuffer,&outBuffer2,NULL,0,0,boxSize,boxSize,NULL,kvImageEdgeExtend);
    error = vImageBoxConvolve_ARGB8888(&outBuffer2,&inBuffer,NULL,0,0,boxSize,boxSize,NULL,kvImageEdgeExtend);
    error = vImageBoxConvolve_ARGB8888(&inBuffer,&outBuffer,NULL,0,0,boxSize,boxSize,NULL,kvImageEdgeExtend);
    
    if (error) {
        NSLog(@"error from convolution %ld", error);
    }
    
    CGColorSpaceRef colorSpace = CGColorSpaceCreateDeviceRGB();
    CGContextRef ctx = CGBitmapContextCreate(
                                             outBuffer.data,
                                             outBuffer.width,
                                             outBuffer.height,
                                             8,
                                             outBuffer.rowBytes,
                                             colorSpace,
                                             kCGImageAlphaNoneSkipLast);
    CGImageRef imageRef = CGBitmapContextCreateImage (ctx);
    UIImage *returnImage = [UIImage imageWithCGImage:imageRef];
    
    //clean up
    CGContextRelease(ctx);
    CGColorSpaceRelease(colorSpace);
    
    free(pixelBuffer);
    CFRelease(inBitmapData);
    
    CGColorSpaceRelease(colorSpace);
    CGImageRelease(imageRef);
    
    return returnImage;
}

@end
