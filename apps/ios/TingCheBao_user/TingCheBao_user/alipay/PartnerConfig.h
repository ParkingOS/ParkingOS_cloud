//
//  PartnerConfig.h
//  AlipaySdkDemo
//
//  Created by ChaoGanYing on 13-5-3.
//  Copyright (c) 2013年 RenFei. All rights reserved.
//
//  提示：如何获取安全校验码和合作身份者id
//  1.用您的签约支付宝账号登录支付宝网站(www.alipay.com)
//  2.点击“商家服务”(https://b.alipay.com/order/myorder.htm)
//  3.点击“查询合作者身份(pid)”、“查询安全校验码(key)”
//

#ifndef MQPDemo_PartnerConfig_h
#define MQPDemo_PartnerConfig_h

//合作身份者id，以2088开头的16位纯数字
#define PartnerID @"2088411488582814"
//收款支付宝账号
#define SellerID  @"caiwu@zhenlaidian.com"

//安全校验码（MD5）密钥，以数字和字母组成的32位字符
#define MD5_KEY @""

//商户私钥，自助生成
#define PartnerPrivKey @"MIICdwIBADANBgkqhkiG9w0BAQEFAASCAmEwggJdAgEAAoGBAMv8f1EqwRN4Zx5WO3lVze6WKWdQQWmOWAnynGHIlsR184UEll1V4859p3CCc9b7rwZaOruj2KnPTUJ/VqQ+xPf7Q8o61PFllfzJgQ1KjNw+GJTJ1ZUTDEf443Xeoa6ieSbZjWcR6uNtQ5ffDFjx62gj3UITIjxTCpRcPumUgBwHAgMBAAECgYBtqshAOP4om5jE5JOA/jKCzNRhqPIh79dBMeAFajQ0Vz2fDAJTF7Qr9b4pbNkegZ1tiuD8tG/ti3f8Aj3we5akzgd5EJJGHLprKMitrlgo/3vkKxaJOgQJUv9nmOCPDNoHz9lFK3js0tB5LoC3XN4i87PKAYrkfzW6sErOUkC90QJBAOWz9eCKdPCArHGbyXOhl9Tu8NRwslsSGAPEme+LGBdUPubQEoJNVAkWxSErNbiPiSF0VICP2cJlgoMrAYUyS5sCQQDjVtgHYSZ0AQsXp0GM+ENaQvEzUmY9D7aRrT+HvVnkx9eOwWu3+6nXI2Rw77L1FFHaNekLKVoFEaJ6oJDvrQYFAkEAsKmr3TofnikYd3f9g/UwNRBgIMNcKTbNSXiXe+haavbcOeClm5mlnCfrDQuSkZOzQAucQhRgwmYX7pHQ5YQ9KQJBAIye+T2HUFvNEWluIdPq9O5uHfha7bazc4Cko3l5HJOxMZqx9cl2N9ZFpClfe1ixWvgZBK/MwkwEXnZvv3chlWkCQH2i1OJi0YBqOWvGti0vkZjNSlb3hxyPdNQSr325vo8t4IOfhN2F2wvJt3lc/Qa1FgCezg1j4d9YW7xYiJHuJkE="


//支付宝公钥
#define AlipayPubKey   @"MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCnxj/9qwVfgoUh/y2W89L6BkRAFljhNhgPdyPuBV64bfQNN1PjbCzkIM6qRdKBoLPXmKKMiFYnkd6rAoprih3/PrQEB/VsW8OoM8fxn67UDYuyBTqA23MML9q1+ilIZwBC2AQ2UBVOrFXfFl75p6/B5KsiNG9zpgmLCUYuLkxpLQIDAQAB"

#endif
