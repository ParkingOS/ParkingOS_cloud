//
//  TCarNumberAddController.m
//  
//
//  Created by apple on 15/7/13.
//
//

#import "TCarNumberAddController.h"
#import <MobileCoreServices/MobileCoreServices.h>
#import "TImageEditViewController.h"
#import "CVAPIRequestModel.h"
#import "TCarNumberExampleController.h"

@interface TCarNumberAddController ()<UIImagePickerControllerDelegate, UINavigationControllerDelegate, UIActionSheetDelegate, MBProgressHUDDelegate, UITextFieldDelegate>

@property(nonatomic, retain) NSString* carNumber;

@property(nonatomic, retain) UIView* section1View;
@property(nonatomic, retain) UIImageView* leftImgView;
@property(nonatomic, retain) UITextField* carNumberTextField;

@property(nonatomic, retain) UIView* section2View;
@property(nonatomic, retain) UILabel* uploadLabel;
@property(nonatomic, retain) UILabel* ruleLabel;
@property(nonatomic, retain) UIButton* cameraButton;
@property(nonatomic, retain) UIButton* checkImgButton;

@property(nonatomic, retain) UIView* section3View;
@property(nonatomic, retain) UIImageView* carNumberImgView;
@property(nonatomic, retain) UIButton* photoLeftButton;
@property(nonatomic, retain) UIButton* photoRightButton;
@property(nonatomic, retain) UILabel* promptLabel;
@property(nonatomic, retain) UIButton* doneButton;

@property(nonatomic, retain) UIActionSheet* actionSheet;
@property(nonatomic, retain) UIImagePickerController* imagePicker;

@property(nonatomic, retain) UIImage* photoImg1;
@property(nonatomic, retain) UIImage* photoImg2;

@property(nonatomic, assign) NSInteger photoIndex;
@property(nonatomic, assign) BOOL isCameraSecond;//是否是拍第二张
@property(nonatomic, retain) CVAPIRequest* request;

@end

@implementation TCarNumberAddController

- (id)initWithCarNumber:(NSString*)carNumber {
    if (self = [super init]) {
        _carNumber = carNumber;
        
        //----section1View---start
        _section1View = [[UIView alloc] initWithFrame:CGRectMake(0, 15, self.view.width, 40)];
        _section1View.backgroundColor = [UIColor whiteColor];
        
        _leftImgView = [[UIImageView alloc] initWithImage:[UIImage imageNamed:@"carNumber.png"]];
        _leftImgView.frame = CGRectMake(10, (_section1View.height - 25)/2, 25, 25);
        
        _carNumberTextField = [[UITextField alloc] initWithFrame:CGRectMake(_leftImgView.right + 4, 0, 120, 40)];
        _carNumberTextField.clearButtonMode = UITextFieldViewModeWhileEditing;
        _carNumberTextField.placeholder = @"请输入车牌号";
        _carNumberTextField.text = _carNumber;
        _carNumberTextField.delegate = self;
        _carNumberTextField.returnKeyType = UIReturnKeyDone;
        
        [_section1View addSubview:_leftImgView];
        [_section1View addSubview:_carNumberTextField];
        //------section1View-----end
        
        
        //----section2View---start
        _section2View = [[UIView alloc] initWithFrame:CGRectMake(0, _section1View.bottom + 10, self.view.width, 180)];
        _section2View.backgroundColor = [UIColor whiteColor];
        
        _uploadLabel = [[UILabel alloc] initWithFrame:CGRectMake(10, 10, self.view.width - 10, 30)];
        _uploadLabel.text = @"上传行驶证照";
        _uploadLabel.textColor = [UIColor grayColor];
        _uploadLabel.font = [UIFont boldSystemFontOfSize:24];
        
        _ruleLabel = [[UILabel alloc] initWithFrame:CGRectMake(10, _uploadLabel.bottom + 10, self.view.width - 95, 90)];
        _ruleLabel.text = @"1.车牌号需清晰可见，其余可挡\n2.需拍摄两种不同遮挡方式的照片\n3.盗用/伪造行驶证，手机号，账号永久停用";
        _ruleLabel.textColor = [UIColor grayColor];
        _ruleLabel.font = [UIFont systemFontOfSize:14];
        _ruleLabel.numberOfLines = 0;
        
        _cameraButton = [UIButton buttonWithType:UIButtonTypeCustom];
        [_cameraButton setBackgroundImage:[UIImage imageNamed:@"camera_green.png"] forState:UIControlStateNormal];
        [_cameraButton addTarget:self action:@selector(buttonTouched:) forControlEvents:UIControlEventTouchUpInside];
        _cameraButton.frame = CGRectMake(self.view.width - 10 - 70, _ruleLabel.top, 70, 70);
        
        _checkImgButton = [UIButton buttonWithType:UIButtonTypeCustom];
        UILabel* label = [[UILabel alloc] initWithFrame:CGRectMake(0, 0, 90, 40)];
        label.text = @"查看示例照片";
        label.textColor = green_color;
        label.font = [UIFont systemFontOfSize:14];
        UIImageView* imgView = [[UIImageView alloc] initWithImage:[UIImage imageNamed:@"arrow_right_green.png"]];
        imgView.frame = CGRectMake(label.right, (40-12)/2, 6, 12);
        [_checkImgButton addSubview:label];
        [_checkImgButton addSubview:imgView];
        [_checkImgButton addTarget:self action:@selector(buttonTouched:) forControlEvents:UIControlEventTouchUpInside];
        _checkImgButton.frame = CGRectMake(self.view.width - 5 - 100, _cameraButton.bottom + 5, 100, 40);
        //------section2View-----end
        
        
        [_section2View addSubview:_uploadLabel];
        [_section2View addSubview:_ruleLabel];
        [_section2View addSubview:_cameraButton];
        [_section2View addSubview:_checkImgButton];
        
        
        //------section3View-----start
        _section3View = [[UIView alloc] initWithFrame:CGRectMake(0, _section1View.bottom + 10, self.view.width, self.view.height - _section1View.bottom - 60)];
        _section3View.backgroundColor = [UIColor clearColor];
        
        _carNumberImgView = [[UIImageView alloc] initWithFrame:CGRectMake(0, 0, self.view.width, self.view.width/3*2)];
        _carNumberImgView.backgroundColor = [UIColor clearColor];
        
        _photoLeftButton = [UIButton buttonWithType:UIButtonTypeCustom];
        [_photoLeftButton setTitle:@"重拍" forState:UIControlStateNormal];
        _photoLeftButton.titleLabel.font = [UIFont systemFontOfSize:14];
        [_photoLeftButton setBackgroundColor:[UIColor blackColor]];
        [_photoLeftButton setTitleColor:[UIColor whiteColor] forState:UIControlStateNormal];
        [_photoLeftButton setImage:[TAPIUtility ajustImage:[UIImage imageNamed:@"re_photo.png"] size:CGSizeMake(14, 16)] forState:UIControlStateNormal];
        _photoLeftButton.frame = CGRectMake(_carNumberImgView.left, _carNumberImgView.bottom - 40, _carNumberImgView.width/2 - 1, 40);
        //        _photoLeftButton.imageEdgeInsets = UIEdgeInsetsMake(8, 30, 8, _photoLeftButton.width - 30 - 14);
        [_photoLeftButton addTarget:self action:@selector(buttonTouched:) forControlEvents:UIControlEventTouchUpInside];
        _photoLeftButton.alpha = 0.8;
       
        _photoRightButton = [UIButton buttonWithType:UIButtonTypeCustom];
        [_photoRightButton setTitle:@"拍第二张" forState:UIControlStateNormal];
        _photoRightButton.titleLabel.font = [UIFont systemFontOfSize:14];
        [_photoRightButton setTitleColor:[UIColor whiteColor] forState:UIControlStateNormal];
        [_photoRightButton setImage:[TAPIUtility ajustImage:[UIImage imageNamed:@"again_photo.png"] size:CGSizeMake(14, 14)] forState:UIControlStateNormal];
        _photoRightButton.backgroundColor = [UIColor blackColor];
        _photoRightButton.frame = CGRectMake(_photoLeftButton.right + 2, _photoLeftButton.top, _photoLeftButton.width, 40);
        [_photoRightButton addTarget:self action:@selector(buttonTouched:) forControlEvents:UIControlEventTouchUpInside];
        _photoRightButton.alpha = 0.8;
        
        _promptLabel = [[UILabel alloc] initWithFrame:CGRectMake(0, _carNumberImgView.bottom + (isIphone4s ? 10 : 20), self.view.width, 70)];
//        _promptLabel.text = @"为确保真实性\n请换个角度，将行驶证再拍一张";
        NSMutableAttributedString* attr = [[NSMutableAttributedString alloc] initWithString:@"为确保真实性\n\n\n请换个角度，将行驶证再拍一张" attributes:@{NSForegroundColorAttributeName : green_color, NSFontAttributeName : [UIFont systemFontOfSize:16]}];
        [attr addAttributes:@{NSFontAttributeName : [UIFont systemFontOfSize:19]} range:NSMakeRange(8, 14)];
        _promptLabel.attributedText = attr;
        _promptLabel.numberOfLines = 0;
        _promptLabel.textAlignment = NSTextAlignmentCenter;
        
        
        [_section3View addSubview:_carNumberImgView];
        [_section3View addSubview:_photoLeftButton];
        [_section3View addSubview:_photoRightButton];
        [_section3View addSubview:_promptLabel];
        //------section3View-----end
        
        
        _doneButton = [UIButton buttonWithType:UIButtonTypeCustom];
        [_doneButton setTitle:@"上传" forState:UIControlStateNormal];
        [_doneButton setTitleColor:[UIColor whiteColor] forState:UIControlStateNormal];
        [_doneButton setTitleColor:green_color forState:UIControlStateHighlighted];
        [_doneButton setBackgroundImage:[TAPIUtility imageWithColor:green_color] forState:UIControlStateNormal];
        [_doneButton setBackgroundImage:[TAPIUtility imageWithColor:[UIColor whiteColor]] forState:UIControlStateHighlighted];
        [_doneButton addTarget:self action:@selector(buttonTouched:) forControlEvents:UIControlEventTouchUpInside];
        _doneButton.frame = CGRectMake(10, self.view.bottom - 50, self.view.width - 2*10, 40);
        _doneButton.layer.cornerRadius = 5;
        _doneButton.clipsToBounds = YES;
        
        [self.view addSubview:_section1View];
        [self.view addSubview:_section2View];
        [self.view addSubview:_section3View];
        [self.view addSubview:_doneButton];
        
        _section2View.hidden = NO;
        _section3View.hidden = YES;
    }
    return self;
}
- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view.
    self.titleView.text = @"添加车牌号";
    self.view.backgroundColor = RGBCOLOR(236, 236, 236);
}

- (void)viewWillDisappear:(BOOL)animated {
    [super viewWillDisappear:animated];
    [_request cancel];
}

- (void)buttonTouched:(UIButton*)button {
    if (button == _cameraButton || button == _photoLeftButton) {
        _isCameraSecond = NO;
        [self camera];
        
    } else if (button == _checkImgButton) {
        TCarNumberExampleController* vc = [[TCarNumberExampleController alloc] init];
        [self.navigationController pushViewController:vc animated:YES];
        
    } else if (button == _photoRightButton) {
        if ([_photoRightButton.titleLabel.text isEqualToString:@"拍第二张"]) {
            _isCameraSecond = YES;
            [self camera];
            
        } else if ([_photoRightButton.titleLabel.text isEqualToString:@"看另一张"]) {
            _photoIndex = _photoIndex == 0 ? 1 : 0;
            _carNumberImgView.image = _photoIndex == 0 ? _photoImg1 : _photoImg2;
        }
        
    } else if (button == _doneButton) {
        if ([_carNumberTextField.text isEqualToString:@""]) {
            [TAPIUtility alertMessage:@"请输入车牌号!" success:NO toViewController:nil];
            return;
        } else if (![TAPIUtility isValidOfCarNumber:_carNumberTextField.text]) {
            [TAPIUtility alertMessage:@"格式错误,请重新输入" success:NO toViewController:nil];
            return;
        } else if (_photoImg1 && _photoImg2 == nil) {
            [TAPIUtility alertMessage:@"至少选择2张不同角度的图片" success:NO toViewController:nil];
            return;
        }
        
        [self requestSave];
    }
}

- (void)camera {
    if (![TAPIUtility checkPhotoAuthorization]) {
        return;
    }
    
    _actionSheet = [[UIActionSheet alloc] initWithTitle:@"选择" delegate:self cancelButtonTitle:@"取消" destructiveButtonTitle:nil otherButtonTitles:@"拍照", nil];
    [_actionSheet showInView:self.view];
}

- (void)requestSave {
    NSDictionary* dic = @{ @"action" : @"upuserpic",
                           @"mobile" : [[NSUserDefaults standardUserDefaults] objectForKey:save_phone],
                           @"carnumber" : _carNumberTextField.text,
                           @"old_carnumber" : _carNumber ? _carNumber : @""
                           };
    NSString* apiPath = [NSString stringWithFormat:@"carinter.do%@", [CVAPIRequest GETParamString:dic]];
    _request = [[CVAPIRequest alloc] initWithAPIPath:apiPath];
    
    if (_photoImg1 && _photoImg2) {
        NSData* data = UIImageJPEGRepresentation(_photoImg1, 0.7);
        NSData* data2 = UIImageJPEGRepresentation(_photoImg2, 0.7);
        [_request setUploadFileParamString:data data2:data2 mimeType:@"image/jpg"];
    }
    
    _request.hud = [MBProgressHUD showHUDAddedTo:self.view animated:YES];
    [self.model sendRequest:_request completion:^(NSDictionary *result, NSError *error) {
        if ([[result objectForKey:@"result"] isEqualToString:@"1"]) {
            [TAPIUtility alertMessage:[result objectForKey:@"errmsg"] success:YES toViewController:self];
            
        } else {
            [TAPIUtility alertMessage:[result objectForKey:@"errmsg"] success:NO toViewController:nil];
        }
    }];
}
#pragma mark UIActionSheetDelegate

- (void)actionSheet:(UIActionSheet *)actionSheet clickedButtonAtIndex:(NSInteger)buttonIndex {
    if (buttonIndex == _actionSheet.cancelButtonIndex) {
        return;
    }
    
    _imagePicker = [[UIImagePickerController alloc] init];
    _imagePicker.delegate = self;
    
    _imagePicker.sourceType = UIImagePickerControllerSourceTypeCamera;
    _imagePicker.mediaTypes = @[(NSString*)kUTTypeImage];
        
    _imagePicker.allowsEditing = NO;
    
    [self presentViewController:_imagePicker animated:YES completion:nil];
}
#pragma mark UIImagePickerControllerDelegate

- (void)imagePickerController:(UIImagePickerController *)picker didFinishPickingMediaWithInfo:(NSDictionary *)info {
    UIImage* image = (UIImage*) [info objectForKey:UIImagePickerControllerOriginalImage];
    [picker dismissViewControllerAnimated:YES completion:nil];
    
    TImageEditViewController* editVc = [[TImageEditViewController alloc] initWithImage:image completeHandle:^(UIImage *clipsImg) {
        //如果点击的是 "拍第二张"按钮 index 1
        if (_isCameraSecond) {
            _photoIndex = 1;
        }
        
        
        if (_photoIndex == 0) {
            _photoImg1 = clipsImg;
            
        } else {
            _photoImg2 = clipsImg;
            _promptLabel.hidden = YES;
        }
        _carNumberImgView.image = clipsImg;
        _section3View.hidden = NO;
        _section2View.hidden = YES;
        [_photoRightButton setTitle:_photoImg2 ? @"看另一张" : @"拍第二张" forState:UIControlStateNormal];
        [_photoRightButton setImage:[TAPIUtility ajustImage:[UIImage imageNamed:_photoImg2 ? @"eye.png" : @"again_photo.png"] size:CGSizeMake(14, _photoImg2 ? 10 : 14)] forState:UIControlStateNormal];
    }];
    [self.navigationController pushViewController:editVc animated:NO];
}

- (void)imagePickerControllerDidCancel:(UIImagePickerController *)picker {
    [picker dismissViewControllerAnimated:YES completion:nil];
}

#pragma mark UITextFieldDelegate

- (BOOL)textField:(UITextField *)textField shouldChangeCharactersInRange:(NSRange)range replacementString:(NSString *)string {
    if ([string isEqualToString:@"\n"]) {
        [textField endEditing:YES];
        return NO;
    }
    return YES;
}
#pragma mark MBProgressHUDDelegate

- (void)hudWasHidden:(MBProgressHUD *)hud {
    [self.navigationController popViewControllerAnimated:YES];
}
@end
